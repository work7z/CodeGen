package cc.codegen.dsl.dto.utils

class GenUtils {

    public static String getPathJoinChar() {
        return "/"
//        return isWindows() ? "\\\\" : "/"
    }

    public static boolean isWindows() {
        if (isWindowCache != null) {
            return isWindowCache;
        }
        String s = System.getProperty("os.name").toLowerCase();
        boolean windows = s.indexOf("windows") >= 0;
        isWindowCache = windows;
        return windows;
    }


    static String getErrToStr(Throwable throwable) {
        def writer = new StringWriter()
        throwable.printStackTrace(new PrintWriter(writer))
        def fin_err = writer.getBuffer().toString()
        return fin_err
    }

    private static Boolean isWindowCache = null;

}
