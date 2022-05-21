package cc.codegen.dsl.dto.utils

class GenUtils {

    public static String getPathJoinChar() {
        return isWindows() ? "\\" : "/"
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

    private static Boolean isWindowCache = null;

}
