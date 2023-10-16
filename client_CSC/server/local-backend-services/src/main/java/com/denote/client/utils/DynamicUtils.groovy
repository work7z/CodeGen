package com.denote.client.utils

import org.apache.commons.io.FileUtils

class DynamicUtils {

    private static Map<String, List<Object>> dynamicContentForDevEnvByLen = new HashMap<>()
//    private static Map<String, Pair<Script, Long>> dynamicContentForDevEnvByLen = new HashMap<>()

    public static Object run(Class clz, Object args) {
        String project_home = "Documents/PersonalProjects/denote-be/src/main/java";
        def name = clz.getName();
        String groovyFile = project_home + "/" + (name.replaceAll("\\.", "/")) + ".groovy"
        String currentUsersHomeDir = System.getProperty("user.home");
        File file = new File(currentUsersHomeDir, groovyFile);
        def fileLen = file.lastModified() + file.length();
        def oldLenPair = dynamicContentForDevEnvByLen.get(name)
        boolean isNeedReGenerate = true;
        def wfunc = "wfunc";
        Script script = null;

        if (oldLenPair != null) {
            if ((oldLenPair[1] as Long).longValue() == fileLen) {
                isNeedReGenerate = false;
                script = oldLenPair[0] as Script
            }
        }

        if (isNeedReGenerate) {
            def str = file.readLines().join("\n");
            def myarr = name.split("\\.");
            String clzName = myarr[myarr.length - 1];
            str += """
def ${wfunc}(def param){
    def obj = new ${clzName}();
    return obj.handle(param);
}
"""
            def uuid = GUtils.uuid();
            File newFile = new File("/tmp/proxy/" + uuid, "rand.groovy");
            if (!newFile.getParentFile().exists()) {
                newFile.getParentFile().mkdirs();
            }
            FileUtils.writeStringToFile(newFile, str);
            GroovyScriptEngine engine = new GroovyScriptEngine(newFile.getParentFile().getAbsolutePath(), DynamicUtils.getClassLoader());
            script = engine.createScript(newFile.getName(), new Binding());

            dynamicContentForDevEnvByLen.put(name, [script, fileLen]);
        }
        Object test = script.invokeMethod(wfunc, args);
        return test;
    }
}
