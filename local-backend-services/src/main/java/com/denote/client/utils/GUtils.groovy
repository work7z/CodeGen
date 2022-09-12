package com.denote.client.utils


import org.apache.commons.io.FileUtils

import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class GUtils {

    public static String getDateStr(Date date, String formatStr) {
        SimpleDateFormat sDateFormat = new SimpleDateFormat(formatStr);
        String format = sDateFormat.format(date);
        return format;
    }

    public static String getDateStr(Date date) {
        SimpleDateFormat sDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String format = sDateFormat.format(date);
        return format;
    }


    public static String getDateStrForNow() {
        return getDateStr(Calendar.getInstance().getTime())
    }

    public static File getCodeProjectDir() {
        return new File("/Users/jerrylai/Documents/PersonalProjects/denote-be")
    }

    public static File get_USER_HOME_DIR() {
        String currentUsersHomeDir = System.getProperty("user.home");
        return new File(currentUsersHomeDir);
    }

    public static final String globalAppName = ".codegen";

    public static File getAppHomeDir() {
        String currentUsersHomeDir = System.getProperty("user.home");
        return new File(currentUsersHomeDir, globalAppName);
    }

    public static File getAppDownloadDir() {
        String currentUsersHomeDir = System.getProperty("user.home");
        def file = new File(currentUsersHomeDir, ".codegen_repository")
        if (!file.exists()) {
            file.mkdirs()
        }
        def a = []
        a.size()
        return file;
    }
    public static File getDriversDir() {
        String currentUsersHomeDir = System.getProperty("user.home");
        def file = new File(new File(currentUsersHomeDir, ".codegen"),"drivers")
        if (!file.exists()) {
            file.mkdirs()
        }
        return file;
    }

    private static Boolean isDevFlag = null;
    private static Boolean isDevServerFlag = null;

    public static Boolean setDevFlag(Boolean val) {
        this.isDevFlag = val
    }

    public static boolean isDevMode() {
        if (isDevFlag == null) {
            isDevFlag = new File(getAppHomeDir(), ".dev").exists()
//            isDevFlag = new File(getHomeDir(), "Documents/PersonalProjects/denote-be/src/main/java").exists();
        }
        return isDevFlag.booleanValue();
    }

    public static boolean isDevServerMode() {
        if (isDevServerFlag == null) {
            isDevServerFlag = new File(getAppHomeDir(), ".dev_server").exists()
//            isDevFlag = new File(getHomeDir(), "Documents/PersonalProjects/denote-be/src/main/java").exists();
        }
        return isDevServerFlag.booleanValue();
    }

    public static String uuid() {
        return UUID.randomUUID().toString().replaceAll("-", "");
    }

    static File createTempDir(String s) {
        return File.createTempDir(s, "-server");
    }

    static String md5(String source) {
        StringBuffer sb = new StringBuffer(32);

        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] array = md.digest(source.getBytes("utf-8"));

            for (int i = 0; i < array.length; i++) {
                sb.append(Integer.toHexString((array[i] & 0xFF) | 0x100).toUpperCase().substring(1, 3));
            }
        } catch (Exception e) {
            return null;
        }

        return (sb.toString().toLowerCase());
//        return Md5Crypt.md5Crypt(bytes.getBytes())
    }

    static String getErrToStr(Throwable throwable) {
        def writer = new StringWriter()
        throwable.printStackTrace(new PrintWriter(writer))
        def fin_err = writer.getBuffer().toString()
        return fin_err
    }

    public static String readClzFile(String path) throws IOException {
        return FileUtils.readFileToString(getClzFile(path), "UTF-8");
    }

    private static File myTempDir = File.createTempDir();

    public static File getTempDir() {
        return myTempDir
    }

    public static File getClzFile(String path) {
        Class aClass = GUtils.class;
        URL resource = aClass.getResource(path);
        if (resource == null) {
            resource = aClass.getResource("/" + path);
        }
        File file = new File(resource.getFile());
        String absolutePath = file.getAbsolutePath();
        if (absolutePath.contains("jar!")) {
            // 获取jar包文件位置
            File clzRootFile = new File(GUtils.class.getProtectionDomain().getCodeSource().getLocation().getPath());
            File newfile = new File(new File(clzRootFile.getParentFile(), "classes"), path);
            System.out.println("switch to " + newfile.getAbsolutePath());
            file = newfile;
            System.out.println(absolutePath);
        }
        String myfilename = GUtils.md5(file.getAbsolutePath())
        File parentFolder = new File(getTempDir(), myfilename)
        if (!parentFolder.exists()) {
            parentFolder.mkdirs()
        }
        File finalFile = new File(parentFolder, file.getName())
        if (!finalFile.exists()) {
            FileUtils.copyFile(file, finalFile)
        }
        println "returning file ${finalFile.getAbsolutePath()}"
        return finalFile;
    }

    public static Boolean isWindowCache = null;

    public static boolean isWindows() {
        if (isWindowCache != null) {
            return isWindowCache;
        }
        String s = System.getProperty("os.name").toLowerCase();
        boolean windows = s.indexOf("windows") >= 0;
        isWindowCache = windows;
        return windows;
    }

    public static String getMavenHome() {
        return "/Users/jerrylai/Sundry/infrastructure/software/apache-maven-3.6.0"
    }


    public static String runCmd(String cmd) {
        def exec = cmd.execute();
        def b = new StringBuffer()
        exec.consumeProcessErrorStream(b)
        def finResForMvnList = exec.text
        return finResForMvnList
    }


    static void highrun(Runnable runnable) {
        Thread thread = new Thread(runnable);
        thread.setPriority(Thread.MAX_PRIORITY)
        thread.start()
    }

    private static def pushRef = Executors.newFixedThreadPool(5);

    static void highrun_Wait(Runnable runnable) {
        pushRef.submit(runnable)
    }

    static String readFile(File file) {
        def lines = file.readLines().join("\n")
        return lines;
    }
}
