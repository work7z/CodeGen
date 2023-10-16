package com.denote.client.handler.extra;


import com.denote.client.concurrent.WebHandleServletHolder;
import org.apache.catalina.Context;
import org.apache.catalina.Globals;
import org.apache.catalina.WebResource;
import org.apache.catalina.WebResourceRoot;
import org.apache.catalina.connector.ResponseFacade;
import org.apache.catalina.core.Constants;
import org.apache.catalina.util.ServerInfo;
import org.apache.catalina.util.URLEncoder;
import org.apache.commons.io.FileUtils;
import org.apache.http.HttpHeaders;
import org.apache.tomcat.util.res.StringManager;
import org.apache.tomcat.util.security.Escape;
import org.apache.tomcat.util.security.PrivilegedGetTccl;
import org.apache.tomcat.util.security.PrivilegedSetTccl;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.ext.EntityResolver2;

import javax.servlet.*;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.AccessController;
import java.security.cert.Certificate;
import java.util.*;
import java.util.jar.Manifest;


/**
 * <p>The default resource-serving servlet for most web applications,
 * used to serve static resources such as HTML pages and images.
 * </p>
 * <p>
 * This servlet is intended to be mapped to <em>/</em> e.g.:
 * </p>
 * <pre>
 *   &lt;servlet-mapping&gt;
 *       &lt;servlet-name&gt;default&lt;/servlet-name&gt;
 *       &lt;url-pattern&gt;/&lt;/url-pattern&gt;
 *   &lt;/servlet-mapping&gt;
 * </pre>
 * <p>It can be mapped to sub-paths, however in all cases resources are served
 * from the web application resource root using the full path from the root
 * of the web application context.
 * <br>e.g. given a web application structure:
 * </p>
 * <pre>
 * /context
 *   /images
 *     tomcat2.jpg
 *   /static
 *     /images
 *       tomcat.jpg
 * </pre>
 * <p>
 * ... and a servlet mapping that maps only <code>/static/*</code> to the default servlet:
 * </p>
 * <pre>
 *   &lt;servlet-mapping&gt;
 *       &lt;servlet-name&gt;default&lt;/servlet-name&gt;
 *       &lt;url-pattern&gt;/static/*&lt;/url-pattern&gt;
 *   &lt;/servlet-mapping&gt;
 * </pre>
 * <p>
 * Then a request to <code>/context/static/images/tomcat.jpg</code> will succeed
 * while a request to <code>/context/images/tomcat2.jpg</code> will fail.
 * </p>
 *
 * @author Craig R. McClanahan
 * @author Remy Maucherat
 */
public class DefaultServletFromTomcat extends HttpServlet {


    private static final long serialVersionUID = 1L;

    /**
     * The string manager for this package.
     */
    protected static final StringManager sm = StringManager.getManager(Constants.Package);

    private static final DocumentBuilderFactory factory;

    private static final SecureEntityResolver secureEntityResolver;

    /**
     * Full range marker.
     */
    protected static final ArrayList<Range> FULL = new ArrayList<>();

    /**
     * MIME multipart separation string
     */
    protected static final String mimeSeparation = "CATALINA_MIME_BOUNDARY";

    /**
     * JNDI resources name.
     *
     * @deprecated Unused. Will be removed in Tomcat 9.
     */
    @Deprecated
    protected static final String RESOURCES_JNDI_NAME = "java:/comp/Resources";

    /**
     * Size of file transfer buffer in bytes.
     */
    protected static final int BUFFER_SIZE = 4096;


    // ----------------------------------------------------- Static Initializer

    static {
        if (Globals.IS_SECURITY_ENABLED) {
            factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            factory.setValidating(false);
            secureEntityResolver = new SecureEntityResolver();
        } else {
            factory = null;
            secureEntityResolver = null;
        }
    }


    // ----------------------------------------------------- Instance Variables

    /**
     * The debugging detail level for this servlet.
     */
    protected int debug = 0;

    /**
     * The input buffer size to use when serving resources.
     */
    protected int input = 2048;

    /**
     * Should we generate directory listings?
     */
    protected boolean listings = false;

    /**
     * Read only flag. By default, it's set to true.
     */
    protected boolean readOnly = true;

    /**
     * List of compression formats to serve and their preference order.
     */
    protected CompressionFormat[] compressionFormats;

    /**
     * The output buffer size to use when serving resources.
     */
    protected int output = 2048;

    /**
     * Allow customized directory listing per directory.
     */
    protected String localXsltFile = null;

    /**
     * Allow customized directory listing per context.
     */
    protected String contextXsltFile = null;

    /**
     * Allow customized directory listing per instance.
     */
    protected String globalXsltFile = null;

    /**
     * Allow a readme file to be included.
     */
    protected String readmeFile = null;

    /**
     * The complete set of web application resources
     */
//    protected transient WebResourceRoot resources = null;

    /**
     * File encoding to be used when reading static files. If none is specified
     * the platform default is used.
     */
    protected String fileEncoding = null;

    /**
     * Minimum size for sendfile usage in bytes.
     */
    protected int sendfileSize = 48 * 1024;

    /**
     * Should the Accept-Ranges: bytes header be send with static resources?
     */
    protected boolean useAcceptRanges = true;

    /**
     * Flag to determine if server information is presented.
     */
    protected boolean showServerInfo = true;


    // --------------------------------------------------------- Public Methods

    /**
     * Finalize this servlet.
     */
    @Override
    public void destroy() {
        // NOOP
    }

    @Override
    public ServletConfig getServletConfig() {
        return WebHandleServletHolder.SYS_CONFIG.get();
    }

    @Override
    public ServletContext getServletContext() {
        return WebHandleServletHolder.SYS_CONTEXT.get();
    }

    /**
     * Initialize this servlet.
     */
    @Override
    public void init() throws ServletException {

        if (getServletConfig().getInitParameter("debug") != null)
            debug = Integer.parseInt(getServletConfig().getInitParameter("debug"));

        if (getServletConfig().getInitParameter("input") != null)
            input = Integer.parseInt(getServletConfig().getInitParameter("input"));

        if (getServletConfig().getInitParameter("output") != null)
            output = Integer.parseInt(getServletConfig().getInitParameter("output"));

        listings = Boolean.parseBoolean(getServletConfig().getInitParameter("listings"));

        if (getServletConfig().getInitParameter("readonly") != null)
            readOnly = Boolean.parseBoolean(getServletConfig().getInitParameter("readonly"));

        compressionFormats = parseCompressionFormats(
                getServletConfig().getInitParameter("precompressed"),
                getServletConfig().getInitParameter("gzip"));

        if (getServletConfig().getInitParameter("sendfileSize") != null)
            sendfileSize =
                    Integer.parseInt(getServletConfig().getInitParameter("sendfileSize")) * 1024;

        fileEncoding = getServletConfig().getInitParameter("fileEncoding");

        globalXsltFile = getServletConfig().getInitParameter("globalXsltFile");
        contextXsltFile = getServletConfig().getInitParameter("contextXsltFile");
        localXsltFile = getServletConfig().getInitParameter("localXsltFile");
        readmeFile = getServletConfig().getInitParameter("readmeFile");

        if (getServletConfig().getInitParameter("useAcceptRanges") != null)
            useAcceptRanges = Boolean.parseBoolean(getServletConfig().getInitParameter("useAcceptRanges"));

        // Sanity check on the specified buffer sizes
        if (input < 256)
            input = 256;
        if (output < 256)
            output = 256;

        if (debug > 0) {
            log("DefaultServlet.init:  input buffer size=" + input +
                    ", output buffer size=" + output);
        }

        // Load the web resources
//        resources = (WebResourceRoot) getServletContext().getAttribute(
//                Globals.RESOURCES_ATTR);

//        if (resources == null) {
//            throw new UnavailableException("No resources");
//        }

        if (getServletConfig().getInitParameter("showServerInfo") != null) {
            showServerInfo = Boolean.parseBoolean(getServletConfig().getInitParameter("showServerInfo"));
        }
    }

    private CompressionFormat[] parseCompressionFormats(String precompressed, String gzip) {
        List<CompressionFormat> ret = new ArrayList<>();
        if (precompressed != null && precompressed.indexOf('=') > 0) {
            for (String pair : precompressed.split(",")) {
                String[] setting = pair.split("=");
                String encoding = setting[0];
                String extension = setting[1];
                ret.add(new CompressionFormat(extension, encoding));
            }
        } else if (precompressed != null) {
            if (Boolean.parseBoolean(precompressed)) {
                ret.add(new CompressionFormat(".br", "br"));
                ret.add(new CompressionFormat(".gz", "gzip"));
            }
        } else if (Boolean.parseBoolean(gzip)) {
            // gzip handling is for backwards compatibility with Tomcat 8.x
            ret.add(new CompressionFormat(".gz", "gzip"));
        }
        return ret.toArray(new CompressionFormat[ret.size()]);
    }


    // ------------------------------------------------------ Protected Methods


    /**
     * Return the relative path associated with this servlet.
     *
     * @param request The servlet request we are processing
     * @return the relative path
     */
    protected String getRelativePath(HttpServletRequest request) {
        return getRelativePath(request, false);
    }

    protected String getRelativePath(HttpServletRequest request, boolean allowEmptyPath) {
        // IMPORTANT: DefaultServlet can be mapped to '/' or '/path/*' but always
        // serves resources from the web app root with context rooted paths.
        // i.e. it cannot be used to mount the web app root under a sub-path
        // This method must construct a complete context rooted path, although
        // subclasses can change this behaviour.

        String servletPath;
        String pathInfo;

        if (request.getAttribute(RequestDispatcher.INCLUDE_REQUEST_URI) != null) {
            // For includes, get the info from the attributes
            pathInfo = (String) request.getAttribute(RequestDispatcher.INCLUDE_PATH_INFO);
            servletPath = (String) request.getAttribute(RequestDispatcher.INCLUDE_SERVLET_PATH);
        } else {
            pathInfo = request.getPathInfo();
            servletPath = request.getServletPath();
        }

        StringBuilder result = new StringBuilder();
        if (servletPath.length() > 0) {
            result.append(servletPath);
        }
        if (pathInfo != null) {
            result.append(pathInfo);
        }
        if (result.length() == 0 && !allowEmptyPath) {
            result.append('/');
        }

        return result.toString();
    }


    /**
     * Determines the appropriate path to prepend resources with
     * when generating directory listings. Depending on the behaviour of
     * {@link #getRelativePath(HttpServletRequest)} this will change.
     *
     * @param request the request to determine the path for
     * @return the prefix to apply to all resources in the listing.
     */
    protected String getPathPrefix(final HttpServletRequest request) {
        return request.getContextPath();
    }


    @Override
    public void service(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        if (req.getDispatcherType() == DispatcherType.ERROR) {
            doGet(req, resp);
        } else {
            super.service(req, resp);
        }
    }


    /**
     * Process a GET request for the specified resource.
     *
     * @param request  The servlet request we are processing
     * @param response The servlet response we are creating
     * @throws IOException      if an input/output error occurs
     * @throws ServletException if a servlet-specified error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response)
            throws IOException, ServletException {

        // Serve the requested resource, including the data content
        serveResource(request, response, true, fileEncoding);

    }


    /**
     * Process a HEAD request for the specified resource.
     *
     * @param request  The servlet request we are processing
     * @param response The servlet response we are creating
     * @throws IOException      if an input/output error occurs
     * @throws ServletException if a servlet-specified error occurs
     */
    @Override
    protected void doHead(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
        // Serve the requested resource, without the data content unless we are
        // being included since in that case the content needs to be provided so
        // the correct content length is reported for the including resource
        boolean serveContent = DispatcherType.INCLUDE.equals(request.getDispatcherType());
        serveResource(request, response, serveContent, fileEncoding);
    }


    /**
     * Override default implementation to ensure that TRACE is correctly
     * handled.
     *
     * @param req  the {@link HttpServletRequest} object that
     *             contains the request the client made of
     *             the servlet
     * @param resp the {@link HttpServletResponse} object that
     *             contains the response the servlet returns
     *             to the client
     * @throws IOException      if an input or output error occurs
     *                          while the servlet is handling the
     *                          OPTIONS request
     * @throws ServletException if the request for the
     *                          OPTIONS cannot be handled
     */
    @Override
    protected void doOptions(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        StringBuilder allow = new StringBuilder();
        // There is a doGet method
        allow.append("GET, HEAD");
        // There is a doPost
//        allow.append(", POST");
        // There is a doPut
//        allow.append(", PUT");
        // There is a doDelete
//        allow.append(", DELETE");
        // Trace - assume disabled unless we can prove otherwise
//        if (req instanceof RequestFacade &&
//                ((RequestFacade) req).getAllowTrace()) {
//            allow.append(", TRACE");
//        }
        // Always allow options
        allow.append(", OPTIONS");

        resp.setHeader("Allow", allow.toString());
    }


    /**
     * Process a POST request for the specified resource.
     *
     * @param request  The servlet request we are processing
     * @param response The servlet response we are creating
     * @throws IOException      if an input/output error occurs
     * @throws ServletException if a servlet-specified error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request,
                          HttpServletResponse response)
            throws IOException, ServletException {
        doGet(request, response);
    }


    /**
     * Process a PUT request for the specified resource.
     *
     * @param req  The servlet request we are processing
     * @param resp The servlet response we are creating
     * @throws IOException      if an input/output error occurs
     * @throws ServletException if a servlet-specified error occurs
     */
    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        if (true) {
            // no support put
            return;
        }

        if (readOnly) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        String path = getRelativePath(req);

        WebResource resource = null;//resources.getResource(path);

        Range range = parseContentRange(req, resp);

        InputStream resourceInputStream = null;

        try {
            // Append data specified in ranges to existing content for this
            // resource - create a temp. file on the local filesystem to
            // perform this operation
            // Assume just one range is specified for now
            if (range != null) {
                File contentFile = executePartialPut(req, range, path);
                resourceInputStream = new FileInputStream(contentFile);
            } else {
                resourceInputStream = req.getInputStream();
            }

            // no support
//            if (resources.write(path, resourceInputStream, true)) {
//                if (resource.exists()) {
//                    resp.setStatus(HttpServletResponse.SC_NO_CONTENT);
//                } else {
//                    resp.setStatus(HttpServletResponse.SC_CREATED);
//                }
//            } else {
//                resp.sendError(HttpServletResponse.SC_CONFLICT);
//            }
        } finally {
            if (resourceInputStream != null) {
                try {
                    resourceInputStream.close();
                } catch (IOException ioe) {
                    // Ignore
                }
            }
        }
    }


    /**
     * Handle a partial PUT.  New content specified in request is appended to
     * existing content in oldRevisionContent (if present). This code does
     * not support simultaneous partial updates to the same resource.
     *
     * @param req   The Servlet request
     * @param range The range that will be written
     * @param path  The path
     * @return the associated file object
     * @throws IOException an IO error occurred
     */
    protected File executePartialPut(HttpServletRequest req, Range range,
                                     String path)
            throws IOException {

        // Append data specified in ranges to existing content for this
        // resource - create a temp. file on the local filesystem to
        // perform this operation
        File tempDir = (File) getServletContext().getAttribute
                (ServletContext.TEMPDIR);
        // Convert all '/' characters to '.' in resourcePath
        String convertedResourcePath = path.replace('/', '.');
        File contentFile = new File(tempDir, convertedResourcePath);
        if (contentFile.createNewFile()) {
            // Clean up contentFile when Tomcat is terminated
            contentFile.deleteOnExit();
        }

        try (RandomAccessFile randAccessContentFile =
                     new RandomAccessFile(contentFile, "rw");) {

            WebResource oldResource = null;//resources.getResource(path);

            // Copy data in oldRevisionContent to contentFile
            if (oldResource.isFile()) {
                try (BufferedInputStream bufOldRevStream =
                             new BufferedInputStream(oldResource.getInputStream(),
                                     BUFFER_SIZE);) {

                    int numBytesRead;
                    byte[] copyBuffer = new byte[BUFFER_SIZE];
                    while ((numBytesRead = bufOldRevStream.read(copyBuffer)) != -1) {
                        randAccessContentFile.write(copyBuffer, 0, numBytesRead);
                    }

                }
            }

            randAccessContentFile.setLength(range.length);

            // Append data in request input stream to contentFile
            randAccessContentFile.seek(range.start);
            int numBytesRead;
            byte[] transferBuffer = new byte[BUFFER_SIZE];
            try (BufferedInputStream requestBufInStream =
                         new BufferedInputStream(req.getInputStream(), BUFFER_SIZE);) {
                while ((numBytesRead = requestBufInStream.read(transferBuffer)) != -1) {
                    randAccessContentFile.write(transferBuffer, 0, numBytesRead);
                }
            }
        }

        return contentFile;
    }


    /**
     * Process a DELETE request for the specified resource.
     *
     * @param req  The servlet request we are processing
     * @param resp The servlet response we are creating
     * @throws IOException      if an input/output error occurs
     * @throws ServletException if a servlet-specified error occurs
     */
    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        if (readOnly) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        String path = getRelativePath(req);

        WebResource resource = null;//resources.getResource(path);

        if (resource.exists()) {
            if (resource.delete()) {
                resp.setStatus(HttpServletResponse.SC_NO_CONTENT);
            } else {
                resp.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
            }
        } else {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
        }

    }


    /**
     * Check if the conditions specified in the optional If headers are
     * satisfied.
     *
     * @param request  The servlet request we are processing
     * @param response The servlet response we are creating
     * @param resource The resource
     * @return <code>true</code> if the resource meets all the specified
     * conditions, and <code>false</code> if any of the conditions is not
     * satisfied, in which case request processing is stopped
     * @throws IOException an IO error occurred
     */
    protected boolean checkIfHeaders(HttpServletRequest request,
                                     HttpServletResponse response,
                                     WebResource resource)
            throws IOException {

        return checkIfMatch(request, response, resource)
                && checkIfModifiedSince(request, response, resource)
                && checkIfNoneMatch(request, response, resource)
                && checkIfUnmodifiedSince(request, response, resource);

    }

    private File staticFile = null;

    public DefaultServletFromTomcat(File staticFile) {
        this.staticFile = staticFile;
    }

    /**
     * URL rewriter.
     *
     * @param path Path which has to be rewritten
     * @return the rewritten path
     */
    protected String rewriteUrl(String path) {
        return URLEncoder.DEFAULT.encode(path, StandardCharsets.UTF_8);
    }


    /**
     * Serve the specified resource, optionally including the data content.
     *
     * @param request  The servlet request we are processing
     * @param response The servlet response we are creating
     * @param content  Should the content be included?
     * @param encoding The encoding to use if it is necessary to access the
     *                 source as characters rather than as bytes
     * @throws IOException      if an input/output error occurs
     * @throws ServletException if a servlet-specified error occurs
     */
    protected void serveResource(HttpServletRequest request,
                                 HttpServletResponse response,
                                 boolean content,
                                 String encoding)
            throws IOException, ServletException {

        boolean serveContent = content;

        // Identify the requested resource path
        String path = staticFile.getAbsolutePath();
//         getRelativePath(request, true);

        if (debug > 0) {
            if (serveContent)
                log("DefaultServlet.serveResource:  Serving resource '" +
                        path + "' headers and data");
            else
                log("DefaultServlet.serveResource:  Serving resource '" +
                        path + "' headers only");
        }

        if (path.length() == 0) {
            // Context root redirect
//            doDirectoryRedirect(request, response);
            return;
        }

        WebResource resource = getResource(request, staticFile);
        boolean isError = false; // DispatcherType.ERROR == request.getDispatcherType();

        // no run here
        if (false && !resource.exists()) {
            // Check if we're included so we can return the appropriate
            // missing resource name in the error
            String requestUri = (String) request.getAttribute(
                    RequestDispatcher.INCLUDE_REQUEST_URI);
            if (requestUri == null) {
                requestUri = request.getRequestURI();
            } else {
                // We're included
                // SRV.9.3 says we must throw a FNFE
                throw new FileNotFoundException(sm.getString(
                        "defaultServlet.missingResource", requestUri));
            }

            if (isError) {
                response.sendError(((Integer) request.getAttribute(
                        RequestDispatcher.ERROR_STATUS_CODE)).intValue());
            } else {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, requestUri);
            }
            return;
        }

        // no run here
        if (false && !resource.canRead()) {
            // Check if we're included so we can return the appropriate
            // missing resource name in the error
            String requestUri = (String) request.getAttribute(
                    RequestDispatcher.INCLUDE_REQUEST_URI);
            if (requestUri == null) {
                requestUri = request.getRequestURI();
            } else {
                // We're included
                // Spec doesn't say what to do in this case but a FNFE seems
                // reasonable
                throw new FileNotFoundException(sm.getString(
                        "defaultServlet.missingResource", requestUri));
            }

            if (isError) {
                response.sendError(((Integer) request.getAttribute(
                        RequestDispatcher.ERROR_STATUS_CODE)).intValue());
            } else {
                response.sendError(HttpServletResponse.SC_FORBIDDEN, requestUri);
            }
            return;
        }

        boolean included = false;
        // Check if the conditions specified in the optional If headers are
        // satisfied.
        // no run here
        if (false && resource.isFile()) {
            // Checking If headers
            included = (request.getAttribute(
                    RequestDispatcher.INCLUDE_CONTEXT_PATH) != null);
            if (!included && !isError && !checkIfHeaders(request, response, resource)) {
                return;
            }
        }

        // Find content type.
        String contentType = resource.getMimeType();
        if (contentType == null) {
            contentType = getServletContext().getMimeType(resource.getName());
            resource.setMimeType(contentType);
        }

        // These need to reflect the original resource, not the potentially
        // precompressed version of the resource so get them now if they are going to
        // be needed later
        String eTag = null;
        String lastModifiedHttp = null;
        if (resource.isFile() && !isError) {
            eTag = resource.getETag();
            lastModifiedHttp = resource.getLastModifiedHttp();
        }


        // Serve a precompressed version of the file if present
        boolean usingPrecompressedVersion = false;
        if (compressionFormats.length > 0 && !included && resource.isFile() &&
                !pathEndsWithCompressedExtension(path)) {
            List<PrecompressedResource> precompressedResources =
                    getAvailablePrecompressedResources(request, path);
            if (!precompressedResources.isEmpty()) {
                Collection<String> varyHeaders = response.getHeaders("Vary");
                boolean addRequired = true;
                for (String varyHeader : varyHeaders) {
                    if ("*".equals(varyHeader) ||
                            "accept-encoding".equalsIgnoreCase(varyHeader)) {
                        addRequired = false;
                        break;
                    }
                }
                if (addRequired) {
                    response.addHeader("Vary", "accept-encoding");
                }
                PrecompressedResource bestResource =
                        getBestPrecompressedResource(request, precompressedResources);
                if (bestResource != null) {
                    response.addHeader("Content-Encoding", bestResource.format.encoding);
                    resource = bestResource.resource;
                    usingPrecompressedVersion = true;
                }
            }
        }

        ArrayList<Range> ranges = null;
        long contentLength = -1L;

        if (resource.isDirectory()) {
            if (!path.endsWith("/")) {
                doDirectoryRedirect(request, response);
                return;
            }

            // Skip directory listings if we have been configured to
            // suppress them
            if (!listings) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND,
                        request.getRequestURI());
                return;
            }
            contentType = "text/html;charset=UTF-8";
        } else {
            if (!isError) {
                if (useAcceptRanges) {
                    // Accept ranges header
                    response.setHeader("Accept-Ranges", "bytes");
                }

                // Parse range specifier
                ranges = parseRange(request, response, resource);

                // ETag header
                response.setHeader("ETag", eTag);

                // Last-Modified header
                response.setHeader("Last-Modified", lastModifiedHttp);
            }

            // Get content length
            contentLength = resource.getContentLength();
            // Special case for zero length files, which would cause a
            // (silent) ISE when setting the output buffer size
            if (contentLength == 0L) {
                serveContent = false;
            }
        }

        ServletOutputStream ostream = null;
        PrintWriter writer = null;

        if (serveContent) {
            // Trying to retrieve the servlet output stream
            try {
                ostream = response.getOutputStream();
            } catch (IllegalStateException e) {
                // If it fails, we try to get a Writer instead if we're
                // trying to serve a text file
                if (!usingPrecompressedVersion &&
                        ((contentType == null) ||
                                (contentType.startsWith("text")) ||
                                (contentType.endsWith("xml")) ||
                                (contentType.contains("/javascript")))
                ) {
                    writer = response.getWriter();
                    // Cannot reliably serve partial content with a Writer
                    ranges = FULL;
                } else {
                    throw e;
                }
            }
        }

        // Check to see if a Filter, Valve of wrapper has written some content.
        // If it has, disable range requests and setting of a content length
        // since neither can be done reliably.
        ServletResponse r = response;
        long contentWritten = 0;
        while (r instanceof ServletResponseWrapper) {
            r = ((ServletResponseWrapper) r).getResponse();
        }
        if (r instanceof ResponseFacade) {
            contentWritten = ((ResponseFacade) r).getContentWritten();
        }
        if (contentWritten > 0) {
            ranges = FULL;
        }

        if (resource.isDirectory() ||
                isError ||
                ((ranges == null || ranges.isEmpty())
                        && request.getHeader("Range") == null) ||
                ranges == FULL) {

            // Set the appropriate output headers
            if (contentType != null) {
                if (debug > 0)
                    log("DefaultServlet.serveFile:  contentType='" +
                            contentType + "'");
                response.setContentType(contentType);
            }
            if (resource.isFile() && contentLength >= 0 &&
                    (!serveContent || ostream != null)) {
                if (debug > 0)
                    log("DefaultServlet.serveFile:  contentLength=" +
                            contentLength);
                // Don't set a content length if something else has already
                // written to the response.
                if (contentWritten == 0) {
                    response.setContentLengthLong(contentLength);
                }
            }

            if (serveContent) {
                try {
                    response.setBufferSize(output);
                } catch (IllegalStateException e) {
                    // Silent catch
                }
                InputStream renderResult = null;
                if (ostream == null) {
                    // Output via a writer so can't use sendfile or write
                    // content directly.
                    if (resource.isDirectory()) {
                        renderResult = render(getPathPrefix(request), resource, encoding);
                    } else {
                        renderResult = resource.getInputStream();
                    }
                    copy(renderResult, writer, encoding);
                } else {
                    // Output is via an InputStream
                    if (resource.isDirectory()) {
                        renderResult = render(getPathPrefix(request), resource, encoding);
                    } else {
                        // Output is content of resource
                        if (!checkSendfile(request, response, resource,
                                contentLength, null)) {
                            // sendfile not possible so check if resource
                            // content is available directly
                            byte[] resourceBody = resource.getContent();
                            if (resourceBody == null) {
                                // Resource content not available, use
                                // inputstream
                                renderResult = resource.getInputStream();
                            } else {
                                // Use the resource content directly
                                ostream.write(resourceBody);
                            }
                        }
                    }
                    // If a stream was configured, it needs to be copied to
                    // the output (this method closes the stream)
                    if (renderResult != null) {
                        copy(renderResult, ostream);
                    }
                }
            }

        } else {

            if ((ranges == null) || (ranges.isEmpty()))
                return;

            // Partial content response.

            response.setStatus(HttpServletResponse.SC_PARTIAL_CONTENT);

            if (ranges.size() == 1) {

                Range range = ranges.get(0);
                response.addHeader("Content-Range", "bytes "
                        + range.start
                        + "-" + range.end + "/"
                        + range.length);
                long length = range.end - range.start + 1;
                response.setContentLengthLong(length);

                if (contentType != null) {
                    if (debug > 0)
                        log("DefaultServlet.serveFile:  contentType='" +
                                contentType + "'");
                    response.setContentType(contentType);
                }

                if (serveContent) {
                    try {
                        response.setBufferSize(output);
                    } catch (IllegalStateException e) {
                        // Silent catch
                    }
                    if (ostream != null) {
                        if (!checkSendfile(request, response, resource,
                                range.end - range.start + 1, range))
                            copy(resource, ostream, range);
                    } else {
                        // we should not get here
                        throw new IllegalStateException();
                    }
                }
            } else {
                response.setContentType("multipart/byteranges; boundary="
                        + mimeSeparation);
                if (serveContent) {
                    try {
                        response.setBufferSize(output);
                    } catch (IllegalStateException e) {
                        // Silent catch
                    }
                    if (ostream != null) {
                        copy(resource, ostream, ranges.iterator(), contentType);
                    } else {
                        // we should not get here
                        throw new IllegalStateException();
                    }
                }
            }
        }
    }

    private WebResource getResource(HttpServletRequest request, File staticFile) {
        return new WebResource() {
            @Override
            public long getLastModified() {
                return staticFile.lastModified();
            }

            @Override
            public String getLastModifiedHttp() {
                return null;
            }

            @Override
            public boolean exists() {
                return staticFile.exists();
            }

            @Override
            public boolean isVirtual() {
                return false;
            }

            @Override
            public boolean isDirectory() {
                return false;
            }

            @Override
            public boolean isFile() {
                return true;
            }

            @Override
            public boolean delete() {
                return false;
            }

            @Override
            public String getName() {
                return staticFile.getName();
            }

            @Override
            public long getContentLength() {
                return staticFile.length();
            }

            @Override
            public String getCanonicalPath() {
                try {
                    return staticFile.getCanonicalPath();
                } catch (IOException e) {
                    e.printStackTrace();
                    throw new RuntimeException(e);
                }
            }

            @Override
            public boolean canRead() {
                return staticFile.canRead();
            }

            @Override
            public String getWebappPath() {
                return null;
            }

            @Override
            public String getETag() {
                return request.getHeader(HttpHeaders.ETAG);
            }

            @Override
            public void setMimeType(String mimeType) {
            }

            @Override
            public String getMimeType() {
                return null;
//                String contentType = new MimetypesFileTypeMap().getContentType(staticFile.getName());
//                return contentType;
            }

            @Override
            public InputStream getInputStream() {
                try {
                    return new FileInputStream(staticFile);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                    throw new RuntimeException(e);
                }
            }

            @Override
            public byte[] getContent() {
                try {
                    return FileUtils.readFileToByteArray(staticFile);
                } catch (IOException e) {
                    e.printStackTrace();
                    throw new RuntimeException(e);
                }
            }

            @Override
            public long getCreation() {
                return 0;
            }

            @Override
            public URL getURL() {
                return null;
            }

            @Override
            public URL getCodeBase() {
                return null;
            }

            @Override
            public WebResourceRoot getWebResourceRoot() {
                return null;
            }

            @Override
            public Certificate[] getCertificates() {
                return new Certificate[0];
            }

            @Override
            public Manifest getManifest() {
                return null;
            }
        };
    }

    private boolean pathEndsWithCompressedExtension(String path) {
        for (CompressionFormat format : compressionFormats) {
            if (path.endsWith(format.extension)) {
                return true;
            }
        }
        return false;
    }

    private List<PrecompressedResource> getAvailablePrecompressedResources(HttpServletRequest request, String path) {
        List<PrecompressedResource> ret = new ArrayList<>(compressionFormats.length);
        for (CompressionFormat format : compressionFormats) {
//            resources.getResource
            WebResource precompressedResource = getResource(request, new File(path + format.extension));
            if (precompressedResource.exists() && precompressedResource.isFile()) {
                ret.add(new PrecompressedResource(precompressedResource, format));
            }
        }
        return ret;
    }

    /**
     * Match the client preferred encoding formats to the available precompressed resources.
     *
     * @param request                The servlet request we are processing
     * @param precompressedResources List of available precompressed resources.
     * @return The best matching precompressed resource or null if no match was found.
     */
    private PrecompressedResource getBestPrecompressedResource(HttpServletRequest request,
                                                               List<PrecompressedResource> precompressedResources) {
        Enumeration<String> headers = request.getHeaders("Accept-Encoding");
        PrecompressedResource bestResource = null;
        double bestResourceQuality = 0;
        int bestResourcePreference = Integer.MAX_VALUE;
        while (headers.hasMoreElements()) {
            String header = headers.nextElement();
            for (String preference : header.split(",")) {
                double quality = 1;
                int qualityIdx = preference.indexOf(';');
                if (qualityIdx > 0) {
                    int equalsIdx = preference.indexOf('=', qualityIdx + 1);
                    if (equalsIdx == -1) {
                        continue;
                    }
                    quality = Double.parseDouble(preference.substring(equalsIdx + 1).trim());
                }
                if (quality >= bestResourceQuality) {
                    String encoding = preference;
                    if (qualityIdx > 0) {
                        encoding = encoding.substring(0, qualityIdx);
                    }
                    encoding = encoding.trim();
                    if ("identity".equals(encoding)) {
                        bestResource = null;
                        bestResourceQuality = quality;
                        bestResourcePreference = Integer.MAX_VALUE;
                        continue;
                    }
                    if ("*".equals(encoding)) {
                        bestResource = precompressedResources.get(0);
                        bestResourceQuality = quality;
                        bestResourcePreference = 0;
                        continue;
                    }
                    for (int i = 0; i < precompressedResources.size(); ++i) {
                        PrecompressedResource resource = precompressedResources.get(i);
                        if (encoding.equals(resource.format.encoding)) {
                            if (quality > bestResourceQuality || i < bestResourcePreference) {
                                bestResource = resource;
                                bestResourceQuality = quality;
                                bestResourcePreference = i;
                            }
                            break;
                        }
                    }
                }
            }
        }
        return bestResource;
    }

    private void doDirectoryRedirect(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        StringBuilder location = new StringBuilder(request.getRequestURI());
        location.append('/');
        if (request.getQueryString() != null) {
            location.append('?');
            location.append(request.getQueryString());
        }
        response.sendRedirect(response.encodeRedirectURL(location.toString()));
    }

    /**
     * Parse the content-range header.
     *
     * @param request  The servlet request we a)re processing
     * @param response The servlet response we are creating
     * @return the range object
     * @throws IOException an IO error occurred
     */
    protected Range parseContentRange(HttpServletRequest request,
                                      HttpServletResponse response)
            throws IOException {

        // Retrieving the content-range header (if any is specified
        String rangeHeader = request.getHeader("Content-Range");

        if (rangeHeader == null)
            return null;

        // bytes is the only range unit supported
        if (!rangeHeader.startsWith("bytes")) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return null;
        }

        rangeHeader = rangeHeader.substring(6).trim();

        int dashPos = rangeHeader.indexOf('-');
        int slashPos = rangeHeader.indexOf('/');

        if (dashPos == -1) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return null;
        }

        if (slashPos == -1) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return null;
        }

        Range range = new Range();

        try {
            range.start = Long.parseLong(rangeHeader.substring(0, dashPos));
            range.end =
                    Long.parseLong(rangeHeader.substring(dashPos + 1, slashPos));
            range.length = Long.parseLong
                    (rangeHeader.substring(slashPos + 1, rangeHeader.length()));
        } catch (NumberFormatException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return null;
        }

        if (!range.validate()) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return null;
        }

        return range;

    }


    /**
     * Parse the range header.
     *
     * @param request  The servlet request we are processing
     * @param response The servlet response we are creating
     * @param resource The resource
     * @return a list of ranges
     * @throws IOException an IO error occurred
     */
    protected ArrayList<Range> parseRange(HttpServletRequest request,
                                          HttpServletResponse response,
                                          WebResource resource) throws IOException {

        // Checking If-Range
        String headerValue = request.getHeader("If-Range");

        if (headerValue != null) {

            long headerValueTime = (-1L);
            try {
                headerValueTime = request.getDateHeader("If-Range");
            } catch (IllegalArgumentException e) {
                // Ignore
            }

            String eTag = resource.getETag();
            long lastModified = resource.getLastModified();

            if (headerValueTime == (-1L)) {

                // If the ETag the client gave does not match the entity
                // etag, then the entire entity is returned.
                if (!eTag.equals(headerValue.trim()))
                    return FULL;

            } else {

                // If the timestamp of the entity the client got is older than
                // the last modification date of the entity, the entire entity
                // is returned.
                if (lastModified > (headerValueTime + 1000))
                    return FULL;

            }

        }

        long fileLength = resource.getContentLength();

        if (fileLength == 0)
            return null;

        // Retrieving the range header (if any is specified
        String rangeHeader = request.getHeader("Range");

        if (rangeHeader == null)
            return null;
        // bytes is the only range unit supported (and I don't see the point
        // of adding new ones).
        if (!rangeHeader.startsWith("bytes")) {
            response.addHeader("Content-Range", "bytes */" + fileLength);
            response.sendError
                    (HttpServletResponse.SC_REQUESTED_RANGE_NOT_SATISFIABLE);
            return null;
        }

        rangeHeader = rangeHeader.substring(6);

        // Vector which will contain all the ranges which are successfully
        // parsed.
        ArrayList<Range> result = new ArrayList<>();
        StringTokenizer commaTokenizer = new StringTokenizer(rangeHeader, ",");

        // Parsing the range list
        while (commaTokenizer.hasMoreTokens()) {
            String rangeDefinition = commaTokenizer.nextToken().trim();

            Range currentRange = new Range();
            currentRange.length = fileLength;

            int dashPos = rangeDefinition.indexOf('-');

            if (dashPos == -1) {
                response.addHeader("Content-Range", "bytes */" + fileLength);
                response.sendError
                        (HttpServletResponse.SC_REQUESTED_RANGE_NOT_SATISFIABLE);
                return null;
            }

            if (dashPos == 0) {

                try {
                    long offset = Long.parseLong(rangeDefinition);
                    currentRange.start = fileLength + offset;
                    currentRange.end = fileLength - 1;
                } catch (NumberFormatException e) {
                    response.addHeader("Content-Range",
                            "bytes */" + fileLength);
                    response.sendError
                            (HttpServletResponse
                                    .SC_REQUESTED_RANGE_NOT_SATISFIABLE);
                    return null;
                }

            } else {

                try {
                    currentRange.start = Long.parseLong
                            (rangeDefinition.substring(0, dashPos));
                    if (dashPos < rangeDefinition.length() - 1)
                        currentRange.end = Long.parseLong
                                (rangeDefinition.substring
                                        (dashPos + 1, rangeDefinition.length()));
                    else
                        currentRange.end = fileLength - 1;
                } catch (NumberFormatException e) {
                    response.addHeader("Content-Range",
                            "bytes */" + fileLength);
                    response.sendError
                            (HttpServletResponse
                                    .SC_REQUESTED_RANGE_NOT_SATISFIABLE);
                    return null;
                }

            }

            if (!currentRange.validate()) {
                response.addHeader("Content-Range", "bytes */" + fileLength);
                response.sendError
                        (HttpServletResponse.SC_REQUESTED_RANGE_NOT_SATISFIABLE);
                return null;
            }

            result.add(currentRange);
        }

        return result;
    }


    /**
     * Decide which way to render. HTML or XML.
     *
     * @param contextPath The path
     * @param resource    The resource
     * @return the input stream with the rendered output
     * @throws IOException      an IO error occurred
     * @throws ServletException rendering error
     * @deprecated Unused. Will be removed in Tomcat 9
     */
    @Deprecated
    protected InputStream render(String contextPath, WebResource resource)
            throws IOException, ServletException {
        return render(contextPath, resource, null);
    }

    protected InputStream render(String contextPath, WebResource resource, String encoding)
            throws IOException, ServletException {

        Source xsltSource = findXsltSource(resource);

        if (xsltSource == null) {
            return renderHtml(contextPath, resource, encoding);
        }
        return renderXml(contextPath, resource, xsltSource, encoding);
    }


    /**
     * Return an InputStream to an XML representation of the contents this
     * directory.
     *
     * @param contextPath Context path to which our internal paths are relative
     * @param resource    The associated resource
     * @param xsltSource  The XSL stylesheet
     * @return the XML data
     * @throws IOException      an IO error occurred
     * @throws ServletException rendering error
     * @deprecated Unused. Will be removed in Tomcat 9
     */
    @Deprecated
    protected InputStream renderXml(String contextPath, WebResource resource, Source xsltSource)
            throws IOException, ServletException {
        return renderXml(contextPath, resource, xsltSource, null);
    }

    protected InputStream renderXml(String contextPath, WebResource resource, Source xsltSource,
                                    String encoding)
            throws IOException, ServletException {

        StringBuilder sb = new StringBuilder();

        sb.append("<?xml version=\"1.0\"?>");
        sb.append("<listing ");
        sb.append(" contextPath='");
        sb.append(contextPath);
        sb.append("'");
        sb.append(" directory='");
        sb.append(resource.getName());
        sb.append("' ");
        sb.append(" hasParent='").append(!resource.getName().equals("/"));
        sb.append("'>");

        sb.append("<entries>");

        String[] entries = null;//resources.list(resource.getWebappPath());

        // rewriteUrl(contextPath) is expensive. cache result for later reuse
        String rewrittenContextPath = rewriteUrl(contextPath);
        String directoryWebappPath = resource.getWebappPath();

        for (String entry : entries) {

            if (entry.equalsIgnoreCase("WEB-INF") ||
                    entry.equalsIgnoreCase("META-INF") ||
                    entry.equalsIgnoreCase(localXsltFile))
                continue;

            if ((directoryWebappPath + entry).equals(contextXsltFile))
                continue;

            WebResource childResource =
                    null;//resources.getResource(directoryWebappPath + entry);
            if (!childResource.exists()) {
                continue;
            }

            sb.append("<entry");
            sb.append(" type='")
                    .append(childResource.isDirectory() ? "dir" : "file")
                    .append("'");
            sb.append(" urlPath='")
                    .append(rewrittenContextPath)
                    .append(rewriteUrl(directoryWebappPath + entry))
                    .append(childResource.isDirectory() ? "/" : "")
                    .append("'");
            if (childResource.isFile()) {
                sb.append(" size='")
                        .append(renderSize(childResource.getContentLength()))
                        .append("'");
            }
            sb.append(" date='")
                    .append(childResource.getLastModifiedHttp())
                    .append("'");

            sb.append(">");
            sb.append(Escape.htmlElementContent(entry));
            if (childResource.isDirectory())
                sb.append("/");
            sb.append("</entry>");
        }
        sb.append("</entries>");

        String readme = getReadme(resource, encoding);

        if (readme != null) {
            sb.append("<readme><![CDATA[");
            sb.append(readme);
            sb.append("]]></readme>");
        }

        sb.append("</listing>");

        // Prevent possible memory leak. Ensure Transformer and
        // TransformerFactory are not loaded from the web application.
        ClassLoader original;
        if (Globals.IS_SECURITY_ENABLED) {
            PrivilegedGetTccl pa = new PrivilegedGetTccl();
            original = AccessController.doPrivileged(pa);
        } else {
            original = Thread.currentThread().getContextClassLoader();
        }
        try {
            if (Globals.IS_SECURITY_ENABLED) {
                PrivilegedSetTccl pa =
                        new PrivilegedSetTccl(DefaultServletFromTomcat.class.getClassLoader());
                AccessController.doPrivileged(pa);
            } else {
                Thread.currentThread().setContextClassLoader(
                        DefaultServletFromTomcat.class.getClassLoader());
            }

            TransformerFactory tFactory = TransformerFactory.newInstance();
            Source xmlSource = new StreamSource(new StringReader(sb.toString()));
            Transformer transformer = tFactory.newTransformer(xsltSource);

            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            OutputStreamWriter osWriter = new OutputStreamWriter(stream, "UTF8");
            StreamResult out = new StreamResult(osWriter);
            transformer.transform(xmlSource, out);
            osWriter.flush();
            return (new ByteArrayInputStream(stream.toByteArray()));
        } catch (TransformerException e) {
            throw new ServletException("XSL transformer error", e);
        } finally {
            if (Globals.IS_SECURITY_ENABLED) {
                PrivilegedSetTccl pa = new PrivilegedSetTccl(original);
                AccessController.doPrivileged(pa);
            } else {
                Thread.currentThread().setContextClassLoader(original);
            }
        }
    }

    /**
     * Return an InputStream to an HTML representation of the contents of this
     * directory.
     *
     * @param contextPath Context path to which our internal paths are relative
     * @param resource    The associated resource
     * @return the HTML data
     * @throws IOException an IO error occurred
     * @deprecated Unused. Will be removed in Tomcat 9
     */
    @Deprecated
    protected InputStream renderHtml(String contextPath, WebResource resource)
            throws IOException {
        return renderHtml(contextPath, resource, null);
    }

    protected InputStream renderHtml(String contextPath, WebResource resource, String encoding)
            throws IOException {

        // Prepare a writer to a buffered area
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        OutputStreamWriter osWriter = new OutputStreamWriter(stream, "UTF8");
        PrintWriter writer = new PrintWriter(osWriter);

        StringBuilder sb = new StringBuilder();

        String[] entries = null;//resources.list(resource.getWebappPath());

        // rewriteUrl(contextPath) is expensive. cache result for later reuse
        String rewrittenContextPath = rewriteUrl(contextPath);
        String directoryWebappPath = resource.getWebappPath();

        // Render the page header
        sb.append("<html>\r\n");
        sb.append("<head>\r\n");
        sb.append("<title>");
        sb.append(sm.getString("directory.title", directoryWebappPath));
        sb.append("</title>\r\n");
        sb.append("<STYLE><!--");
        sb.append(org.apache.catalina.util.TomcatCSS.TOMCAT_CSS);
        sb.append("--></STYLE> ");
        sb.append("</head>\r\n");
        sb.append("<body>");
        sb.append("<h1>");
        sb.append(sm.getString("directory.title", directoryWebappPath));

        // Render the link to our parent (if required)
        String parentDirectory = directoryWebappPath;
        if (parentDirectory.endsWith("/")) {
            parentDirectory =
                    parentDirectory.substring(0, parentDirectory.length() - 1);
        }
        int slash = parentDirectory.lastIndexOf('/');
        if (slash >= 0) {
            String parent = directoryWebappPath.substring(0, slash);
            sb.append(" - <a href=\"");
            sb.append(rewrittenContextPath);
            if (parent.equals(""))
                parent = "/";
            sb.append(rewriteUrl(parent));
            if (!parent.endsWith("/"))
                sb.append("/");
            sb.append("\">");
            sb.append("<b>");
            sb.append(sm.getString("directory.parent", parent));
            sb.append("</b>");
            sb.append("</a>");
        }

        sb.append("</h1>");
        sb.append("<HR size=\"1\" noshade=\"noshade\">");

        sb.append("<table width=\"100%\" cellspacing=\"0\"" +
                " cellpadding=\"5\" align=\"center\">\r\n");

        // Render the column headings
        sb.append("<tr>\r\n");
        sb.append("<td align=\"left\"><font size=\"+1\"><strong>");
        sb.append(sm.getString("directory.filename"));
        sb.append("</strong></font></td>\r\n");
        sb.append("<td align=\"center\"><font size=\"+1\"><strong>");
        sb.append(sm.getString("directory.size"));
        sb.append("</strong></font></td>\r\n");
        sb.append("<td align=\"right\"><font size=\"+1\"><strong>");
        sb.append(sm.getString("directory.lastModified"));
        sb.append("</strong></font></td>\r\n");
        sb.append("</tr>");

        boolean shade = false;
        for (String entry : entries) {
            if (entry.equalsIgnoreCase("WEB-INF") ||
                    entry.equalsIgnoreCase("META-INF"))
                continue;

            WebResource childResource =
                    null;//resources.getResource(directoryWebappPath + entry);
            if (!childResource.exists()) {
                continue;
            }

            sb.append("<tr");
            if (shade)
                sb.append(" bgcolor=\"#eeeeee\"");
            sb.append(">\r\n");
            shade = !shade;

            sb.append("<td align=\"left\">&nbsp;&nbsp;\r\n");
            sb.append("<a href=\"");
            sb.append(rewrittenContextPath);
            sb.append(rewriteUrl(directoryWebappPath + entry));
            if (childResource.isDirectory())
                sb.append("/");
            sb.append("\"><tt>");
            sb.append(Escape.htmlElementContent(entry));
            if (childResource.isDirectory())
                sb.append("/");
            sb.append("</tt></a></td>\r\n");

            sb.append("<td align=\"right\"><tt>");
            if (childResource.isDirectory())
                sb.append("&nbsp;");
            else
                sb.append(renderSize(childResource.getContentLength()));
            sb.append("</tt></td>\r\n");

            sb.append("<td align=\"right\"><tt>");
            sb.append(childResource.getLastModifiedHttp());
            sb.append("</tt></td>\r\n");

            sb.append("</tr>\r\n");
        }

        // Render the page footer
        sb.append("</table>\r\n");

        sb.append("<HR size=\"1\" noshade=\"noshade\">");

        String readme = getReadme(resource, encoding);
        if (readme != null) {
            sb.append(readme);
            sb.append("<HR size=\"1\" noshade=\"noshade\">");
        }

        if (showServerInfo) {
            sb.append("<h3>").append(ServerInfo.getServerInfo()).append("</h3>");
        }
        sb.append("</body>\r\n");
        sb.append("</html>\r\n");

        // Return an input stream to the underlying bytes
        writer.write(sb.toString());
        writer.flush();
        return (new ByteArrayInputStream(stream.toByteArray()));

    }


    /**
     * Render the specified file size (in bytes).
     *
     * @param size File size (in bytes)
     * @return the formatted size
     */
    protected String renderSize(long size) {

        long leftSide = size / 1024;
        long rightSide = (size % 1024) / 103;   // Makes 1 digit
        if ((leftSide == 0) && (rightSide == 0) && (size > 0))
            rightSide = 1;

        return ("" + leftSide + "." + rightSide + " kb");

    }


    /**
     * Get the readme file as a string.
     *
     * @param directory The directory to search
     * @return the readme for the specified directory
     * @deprecated Unused. Will be removed in Tomcat 9
     */
    @Deprecated
    protected String getReadme(WebResource directory) {
        return getReadme(directory, null);
    }

    /**
     * Get the readme file as a string.
     *
     * @param directory The directory to search
     * @param encoding  The readme encoding
     * @return the readme for the specified directory
     */
    protected String getReadme(WebResource directory, String encoding) {

        if (readmeFile != null) {
            WebResource resource = null;//resources.getResource(
            // directory.getWebappPath() + readmeFile);
            if (resource.isFile()) {
                StringWriter buffer = new StringWriter();
                InputStreamReader reader = null;
                try (InputStream is = resource.getInputStream();) {
                    if (encoding != null) {
                        reader = new InputStreamReader(is, encoding);
                    } else {
                        reader = new InputStreamReader(is);
                    }
                    copyRange(reader, new PrintWriter(buffer));
                } catch (IOException e) {
                    log("Failure to close reader", e);
                } finally {
                    if (reader != null) {
                        try {
                            reader.close();
                        } catch (IOException e) {
                        }
                    }
                }
                return buffer.toString();
            } else {
                if (debug > 10)
                    log("readme '" + readmeFile + "' not found");

                return null;
            }
        }

        return null;
    }


    /**
     * Return a Source for the xsl template (if possible).
     *
     * @param directory The directory to search
     * @return the source for the specified directory
     * @throws IOException an IO error occurred
     */
    protected Source findXsltSource(WebResource directory)
            throws IOException {

        if (localXsltFile != null) {
            WebResource resource = null;//resources.getResource(
            //directory.getWebappPath() + localXsltFile);
            if (resource.isFile()) {
                InputStream is = resource.getInputStream();
                if (is != null) {
                    if (Globals.IS_SECURITY_ENABLED) {
                        return secureXslt(is);
                    } else {
                        return new StreamSource(is);
                    }
                }
            }
            if (debug > 10) {
                log("localXsltFile '" + localXsltFile + "' not found");
            }
        }

        if (contextXsltFile != null) {
            InputStream is =
                    getServletContext().getResourceAsStream(contextXsltFile);
            if (is != null) {
                if (Globals.IS_SECURITY_ENABLED) {
                    return secureXslt(is);
                } else {
                    return new StreamSource(is);
                }
            }

            if (debug > 10)
                log("contextXsltFile '" + contextXsltFile + "' not found");
        }

        /*  Open and read in file in one fell swoop to reduce chance
         *  chance of leaving handle open.
         */
        if (globalXsltFile != null) {
            File f = validateGlobalXsltFile();
            if (f != null) {
                try (FileInputStream fis = new FileInputStream(f)) {
                    byte b[] = new byte[(int) f.length()]; /* danger! */
                    fis.read(b);
                    return new StreamSource(new ByteArrayInputStream(b));
                }
            }
        }

        return null;
    }


    private File validateGlobalXsltFile() {
        Context context = null;//resources.getContext();

        File baseConf = new File(context.getCatalinaBase(), "conf");
        File result = validateGlobalXsltFile(baseConf);
        if (result == null) {
            File homeConf = new File(context.getCatalinaHome(), "conf");
            if (!baseConf.equals(homeConf)) {
                result = validateGlobalXsltFile(homeConf);
            }
        }

        return result;
    }


    private File validateGlobalXsltFile(File base) {
        File candidate = new File(globalXsltFile);
        if (!candidate.isAbsolute()) {
            candidate = new File(base, globalXsltFile);
        }

        if (!candidate.isFile()) {
            return null;
        }

        // First check that the resulting path is under the provided base
        try {
            if (!candidate.getCanonicalPath().startsWith(base.getCanonicalPath())) {
                return null;
            }
        } catch (IOException ioe) {
            return null;
        }

        // Next check that an .xsl or .xslt file has been specified
        String nameLower = candidate.getName().toLowerCase(Locale.ENGLISH);
        if (!nameLower.endsWith(".xslt") && !nameLower.endsWith(".xsl")) {
            return null;
        }

        return candidate;
    }


    private Source secureXslt(InputStream is) {
        // Need to filter out any external entities
        Source result = null;
        try {
            DocumentBuilder builder = factory.newDocumentBuilder();
            builder.setEntityResolver(secureEntityResolver);
            Document document = builder.parse(is);
            result = new DOMSource(document);
        } catch (ParserConfigurationException | SAXException | IOException e) {
            if (debug > 0) {
                log(e.getMessage(), e);
            }
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    // Ignore
                }
            }
        }
        return result;
    }


    // -------------------------------------------------------- protected Methods

    /**
     * Check if sendfile can be used.
     *
     * @param request  The Servlet request
     * @param response The Servlet response
     * @param resource The resource
     * @param length   The length which will be written (will be used only if
     *                 range is null)
     * @param range    The range that will be written
     * @return <code>true</code> if sendfile should be used (writing is then
     * delegated to the endpoint)
     */
    protected boolean checkSendfile(HttpServletRequest request,
                                    HttpServletResponse response,
                                    WebResource resource,
                                    long length, Range range) {
        String canonicalPath;
        if (sendfileSize > 0
                && length > sendfileSize
                && (Boolean.TRUE.equals(request.getAttribute(Globals.SENDFILE_SUPPORTED_ATTR)))
                && (request.getClass().getName().equals("org.apache.catalina.connector.RequestFacade"))
                && (response.getClass().getName().equals("org.apache.catalina.connector.ResponseFacade"))
                && resource.isFile()
                && ((canonicalPath = resource.getCanonicalPath()) != null)
        ) {
            request.setAttribute(Globals.SENDFILE_FILENAME_ATTR, canonicalPath);
            if (range == null) {
                request.setAttribute(Globals.SENDFILE_FILE_START_ATTR, Long.valueOf(0L));
                request.setAttribute(Globals.SENDFILE_FILE_END_ATTR, Long.valueOf(length));
            } else {
                request.setAttribute(Globals.SENDFILE_FILE_START_ATTR, Long.valueOf(range.start));
                request.setAttribute(Globals.SENDFILE_FILE_END_ATTR, Long.valueOf(range.end + 1));
            }
            return true;
        }
        return false;
    }


    /**
     * Check if the if-match condition is satisfied.
     *
     * @param request  The servlet request we are processing
     * @param response The servlet response we are creating
     * @param resource The resource
     * @return <code>true</code> if the resource meets the specified condition,
     * and <code>false</code> if the condition is not satisfied, in which case
     * request processing is stopped
     * @throws IOException an IO error occurred
     */
    protected boolean checkIfMatch(HttpServletRequest request,
                                   HttpServletResponse response, WebResource resource)
            throws IOException {

        String eTag = resource.getETag();
        String headerValue = request.getHeader("If-Match");
        if (headerValue != null) {
            if (headerValue.indexOf('*') == -1) {

                StringTokenizer commaTokenizer = new StringTokenizer
                        (headerValue, ",");
                boolean conditionSatisfied = false;

                while (!conditionSatisfied && commaTokenizer.hasMoreTokens()) {
                    String currentToken = commaTokenizer.nextToken();
                    if (currentToken.trim().equals(eTag))
                        conditionSatisfied = true;
                }

                // If none of the given ETags match, 412 Precondition failed is
                // sent back
                if (!conditionSatisfied) {
                    response.sendError
                            (HttpServletResponse.SC_PRECONDITION_FAILED);
                    return false;
                }

            }
        }
        return true;
    }


    /**
     * Check if the if-modified-since condition is satisfied.
     *
     * @param request  The servlet request we are processing
     * @param response The servlet response we are creating
     * @param resource The resource
     * @return <code>true</code> if the resource meets the specified condition,
     * and <code>false</code> if the condition is not satisfied, in which case
     * request processing is stopped
     */
    protected boolean checkIfModifiedSince(HttpServletRequest request,
                                           HttpServletResponse response, WebResource resource) {
        try {
            long headerValue = request.getDateHeader("If-Modified-Since");
            long lastModified = resource.getLastModified();
            if (headerValue != -1) {

                // If an If-None-Match header has been specified, if modified since
                // is ignored.
                if ((request.getHeader("If-None-Match") == null)
                        && (lastModified < headerValue + 1000)) {
                    // The entity has not been modified since the date
                    // specified by the client. This is not an error case.
                    response.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
                    response.setHeader("ETag", resource.getETag());

                    return false;
                }
            }
        } catch (IllegalArgumentException illegalArgument) {
            return true;
        }
        return true;
    }


    /**
     * Check if the if-none-match condition is satisfied.
     *
     * @param request  The servlet request we are processing
     * @param response The servlet response we are creating
     * @param resource The resource
     * @return <code>true</code> if the resource meets the specified condition,
     * and <code>false</code> if the condition is not satisfied, in which case
     * request processing is stopped
     * @throws IOException an IO error occurred
     */
    protected boolean checkIfNoneMatch(HttpServletRequest request,
                                       HttpServletResponse response, WebResource resource)
            throws IOException {

        String eTag = resource.getETag();
        String headerValue = request.getHeader("If-None-Match");
        if (headerValue != null) {

            boolean conditionSatisfied = false;

            if (!headerValue.equals("*")) {

                StringTokenizer commaTokenizer =
                        new StringTokenizer(headerValue, ",");

                while (!conditionSatisfied && commaTokenizer.hasMoreTokens()) {
                    String currentToken = commaTokenizer.nextToken();
                    if (currentToken.trim().equals(eTag))
                        conditionSatisfied = true;
                }

            } else {
                conditionSatisfied = true;
            }

            if (conditionSatisfied) {

                // For GET and HEAD, we should respond with
                // 304 Not Modified.
                // For every other method, 412 Precondition Failed is sent
                // back.
                if (("GET".equals(request.getMethod()))
                        || ("HEAD".equals(request.getMethod()))) {
                    response.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
                    response.setHeader("ETag", eTag);

                    return false;
                }
                response.sendError(HttpServletResponse.SC_PRECONDITION_FAILED);
                return false;
            }
        }
        return true;
    }

    /**
     * Check if the if-unmodified-since condition is satisfied.
     *
     * @param request  The servlet request we are processing
     * @param response The servlet response we are creating
     * @param resource The resource
     * @return <code>true</code> if the resource meets the specified condition,
     * and <code>false</code> if the condition is not satisfied, in which case
     * request processing is stopped
     * @throws IOException an IO error occurred
     */
    protected boolean checkIfUnmodifiedSince(HttpServletRequest request,
                                             HttpServletResponse response, WebResource resource)
            throws IOException {
        try {
            long lastModified = resource.getLastModified();
            long headerValue = request.getDateHeader("If-Unmodified-Since");
            if (headerValue != -1) {
                if (lastModified >= (headerValue + 1000)) {
                    // The entity has not been modified since the date
                    // specified by the client. This is not an error case.
                    response.sendError(HttpServletResponse.SC_PRECONDITION_FAILED);
                    return false;
                }
            }
        } catch (IllegalArgumentException illegalArgument) {
            return true;
        }
        return true;
    }


    /**
     * Copy the contents of the specified input stream to the specified
     * output stream, and ensure that both streams are closed before returning
     * (even in the face of an exception).
     *
     * @param resource The source resource
     * @param is       The input stream to read the source resource from
     * @param ostream  The output stream to write to
     * @throws IOException if an input/output error occurs
     * @deprecated Unused. This will be removed in Tomcat 9.
     * Use {@link #copy(InputStream, ServletOutputStream)}
     */
    @Deprecated
    protected void copy(WebResource resource, InputStream is,
                        ServletOutputStream ostream)
            throws IOException {
        copy(is, ostream);
    }


    /**
     * Copy the contents of the specified input stream to the specified
     * output stream, and ensure that both streams are closed before returning
     * (even in the face of an exception).
     *
     * @param is      The input stream to read the source resource from
     * @param ostream The output stream to write to
     * @throws IOException if an input/output error occurs
     */
    protected void copy(InputStream is, ServletOutputStream ostream) throws IOException {

        IOException exception = null;
        InputStream istream = new BufferedInputStream(is, input);

        // Copy the input stream to the output stream
        exception = copyRange(istream, ostream);

        // Clean up the input stream
        istream.close();

        // Rethrow any exception that has occurred
        if (exception != null)
            throw exception;
    }


    /**
     * Copy the contents of the specified input stream to the specified
     * output stream, and ensure that both streams are closed before returning
     * (even in the face of an exception).
     *
     * @param resource The source resource
     * @param is       The input stream to read the source resource from
     * @param writer   The writer to write to
     * @param encoding The encoding to use when reading the source input stream
     * @throws IOException if an input/output error occurs
     * @deprecated Unused. This will be removed in Tomcat 9.
     * Use {@link #copy(InputStream, PrintWriter, String)}
     */
    @Deprecated
    protected void copy(WebResource resource, InputStream is, PrintWriter writer,
                        String encoding) throws IOException {
        copy(is, writer, encoding);
    }


    /**
     * Copy the contents of the specified input stream to the specified
     * output stream, and ensure that both streams are closed before returning
     * (even in the face of an exception).
     *
     * @param is       The input stream to read the source resource from
     * @param writer   The writer to write to
     * @param encoding The encoding to use when reading the source input stream
     * @throws IOException if an input/output error occurs
     */
    protected void copy(InputStream is, PrintWriter writer, String encoding) throws IOException {
        IOException exception = null;

        Reader reader;
        if (encoding == null) {
            reader = new InputStreamReader(is);
        } else {
            reader = new InputStreamReader(is, encoding);
        }

        // Copy the input stream to the output stream
        exception = copyRange(reader, writer);

        // Clean up the reader
        reader.close();

        // Rethrow any exception that has occurred
        if (exception != null) {
            throw exception;
        }
    }


    /**
     * Copy the contents of the specified input stream to the specified
     * output stream, and ensure that both streams are closed before returning
     * (even in the face of an exception).
     *
     * @param resource The source resource
     * @param ostream  The output stream to write to
     * @param range    Range the client wanted to retrieve
     * @throws IOException if an input/output error occurs
     */
    protected void copy(WebResource resource, ServletOutputStream ostream,
                        Range range)
            throws IOException {

        IOException exception = null;

        InputStream resourceInputStream = resource.getInputStream();
        InputStream istream =
                new BufferedInputStream(resourceInputStream, input);
        exception = copyRange(istream, ostream, range.start, range.end);

        // Clean up the input stream
        istream.close();

        // Rethrow any exception that has occurred
        if (exception != null)
            throw exception;

    }


    /**
     * Copy the contents of the specified input stream to the specified
     * output stream, and ensure that both streams are closed before returning
     * (even in the face of an exception).
     *
     * @param resource    The source resource
     * @param ostream     The output stream to write to
     * @param ranges      Enumeration of the ranges the client wanted to
     *                    retrieve
     * @param contentType Content type of the resource
     * @throws IOException if an input/output error occurs
     */
    protected void copy(WebResource resource, ServletOutputStream ostream,
                        Iterator<Range> ranges, String contentType)
            throws IOException {

        IOException exception = null;

        while ((exception == null) && (ranges.hasNext())) {

            InputStream resourceInputStream = resource.getInputStream();
            try (InputStream istream = new BufferedInputStream(resourceInputStream, input)) {

                Range currentRange = ranges.next();

                // Writing MIME header.
                ostream.println();
                ostream.println("--" + mimeSeparation);
                if (contentType != null)
                    ostream.println("Content-Type: " + contentType);
                ostream.println("Content-Range: bytes " + currentRange.start
                        + "-" + currentRange.end + "/"
                        + currentRange.length);
                ostream.println();

                // Printing content
                exception = copyRange(istream, ostream, currentRange.start,
                        currentRange.end);
            }
        }

        ostream.println();
        ostream.print("--" + mimeSeparation + "--");

        // Rethrow any exception that has occurred
        if (exception != null)
            throw exception;

    }


    /**
     * Copy the contents of the specified input stream to the specified
     * output stream, and ensure that both streams are closed before returning
     * (even in the face of an exception).
     *
     * @param istream The input stream to read from
     * @param ostream The output stream to write to
     * @return Exception which occurred during processing
     */
    protected IOException copyRange(InputStream istream,
                                    ServletOutputStream ostream) {

        // Copy the input stream to the output stream
        IOException exception = null;
        byte buffer[] = new byte[input];
        int len = buffer.length;
        while (true) {
            try {
                len = istream.read(buffer);
                if (len == -1)
                    break;
                ostream.write(buffer, 0, len);
            } catch (IOException e) {
                exception = e;
                len = -1;
                break;
            }
        }
        return exception;

    }


    /**
     * Copy the contents of the specified input stream to the specified
     * output stream, and ensure that both streams are closed before returning
     * (even in the face of an exception).
     *
     * @param reader The reader to read from
     * @param writer The writer to write to
     * @return Exception which occurred during processing
     */
    protected IOException copyRange(Reader reader, PrintWriter writer) {

        // Copy the input stream to the output stream
        IOException exception = null;
        char buffer[] = new char[input];
        int len = buffer.length;
        while (true) {
            try {
                len = reader.read(buffer);
                if (len == -1)
                    break;
                writer.write(buffer, 0, len);
            } catch (IOException e) {
                exception = e;
                len = -1;
                break;
            }
        }
        return exception;

    }


    /**
     * Copy the contents of the specified input stream to the specified
     * output stream, and ensure that both streams are closed before returning
     * (even in the face of an exception).
     *
     * @param istream The input stream to read from
     * @param ostream The output stream to write to
     * @param start   Start of the range which will be copied
     * @param end     End of the range which will be copied
     * @return Exception which occurred during processing
     */
    protected IOException copyRange(InputStream istream,
                                    ServletOutputStream ostream,
                                    long start, long end) {

        if (debug > 10)
            log("Serving bytes:" + start + "-" + end);

        long skipped = 0;
        try {
            skipped = istream.skip(start);
        } catch (IOException e) {
            return e;
        }
        if (skipped < start) {
            return new IOException(sm.getString("defaultservlet.skipfail",
                    Long.valueOf(skipped), Long.valueOf(start)));
        }

        IOException exception = null;
        long bytesToRead = end - start + 1;

        byte buffer[] = new byte[input];
        int len = buffer.length;
        while ((bytesToRead > 0) && (len >= buffer.length)) {
            try {
                len = istream.read(buffer);
                if (bytesToRead >= len) {
                    ostream.write(buffer, 0, len);
                    bytesToRead -= len;
                } else {
                    ostream.write(buffer, 0, (int) bytesToRead);
                    bytesToRead = 0;
                }
            } catch (IOException e) {
                exception = e;
                len = -1;
            }
            if (len < buffer.length)
                break;
        }

        return exception;

    }


    protected static class Range {

        public long start;
        public long end;
        public long length;

        /**
         * Validate range.
         *
         * @return true if the range is valid, otherwise false
         */
        public boolean validate() {
            if (end >= length)
                end = length - 1;
            return (start >= 0) && (end >= 0) && (start <= end) && (length > 0);
        }
    }

    protected static class CompressionFormat implements Serializable {
        private static final long serialVersionUID = 1L;
        public final String extension;
        public final String encoding;

        public CompressionFormat(String extension, String encoding) {
            this.extension = extension;
            this.encoding = encoding;
        }
    }

    private static class PrecompressedResource {
        public final WebResource resource;
        public final CompressionFormat format;

        private PrecompressedResource(WebResource resource, CompressionFormat format) {
            this.resource = resource;
            this.format = format;
        }
    }

    /**
     * This is secure in the sense that any attempt to use an external entity
     * will trigger an exception.
     */
    private static class SecureEntityResolver implements EntityResolver2 {

        @Override
        public InputSource resolveEntity(String publicId, String systemId)
                throws SAXException, IOException {
            throw new SAXException(sm.getString("defaultServlet.blockExternalEntity",
                    publicId, systemId));
        }

        @Override
        public InputSource getExternalSubset(String name, String baseURI)
                throws SAXException, IOException {
            throw new SAXException(sm.getString("defaultServlet.blockExternalSubset",
                    name, baseURI));
        }

        @Override
        public InputSource resolveEntity(String name, String publicId,
                                         String baseURI, String systemId) throws SAXException,
                IOException {
            throw new SAXException(sm.getString("defaultServlet.blockExternalEntity2",
                    name, publicId, baseURI, systemId));
        }
    }
}
