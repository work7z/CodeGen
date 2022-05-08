package cc.codegen.client.util;

import cc.codegen.client.gui.frame.UserOperationFrame;
import com.alibaba.fastjson.JSON;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import javax.swing.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class I18nUtils {
    public static final Map<String, Map<String, String>> lang_mapping = new HashMap<String, Map<String, String>>();

    public static String[] langList = new String[]{"en_US", "zh_CN", "zh_HK"};

    public static int getCurrentLangIndex() {
        String lang = getCrtLang();
        int idx = 0;
        for (String eachLang : langList) {
            if (eachLang.equalsIgnoreCase(lang)) {
                return idx;
            }
            idx++;
        }
        return 0;
    }

    public static String t(String source, String... array) {
        boolean isDevType = SysUtils.isDevMode();
        String lang = getCrtLang();
        if (lang_mapping.size() == 0 || isDevType) {
            synchronized ("all-init".intern()) {
                String[] allJsonFile = new String[]{"zh_CN", "zh_HK", "en_US"};
                for (int i = 0; i < allJsonFile.length; i++) {
                    String it = allJsonFile[i];
                    String mylang = (SysUtils.getFileBytesForStr("lang/" + it + ".json"));
                    Map mylang_map = SysUtils.parseMap(mylang);

                    String mylang_overwrite = (SysUtils.getFileBytesForStr("lang/" + it + "_overwrite.json"));
                    Map mylang_overwrite_map = SysUtils.parseMap(mylang_overwrite);

                    mylang_map.putAll(mylang_overwrite_map);
                    lang_mapping.put(it, mylang_map);
                }
            }
        }
        if ("en_US".equalsIgnoreCase(lang)) {
            recordUnTraSource(source);
            return formatWithLists(source, array);
        }
        Object langObj = lang_mapping.get(lang);
        if (langObj == null) {
            return formatWithLists(source, array);
        }
        String rawfiles = lang_mapping.get(lang).get(source);
        if (rawfiles == null) {
            recordUnTraSource(source);
            return formatWithLists(source, array);
        }
        return formatWithLists(rawfiles, array);
    }


    private static String formatWithLists(String source, String[] array) {
        for (int i = 0; i < array.length; i++) {
            String entry = array[i];
            source = source.replaceAll("\\{" + i + "\\}", entry);
        }
        return source;
    }

    private static synchronized void recordUnTraSource(String source) {
        if (!SysUtils.isDevMode()) {
            return;
        }
        File notHandleFiles = new File("/Users/jerrylai/Documents/PersonalProjects/denote-be/pal/clients/src/main/resources/lang/not_handle_yet.json");
        if (un_handle_json.size() == 0) {
            String mylang_overwrite = null;
            try {
                mylang_overwrite = IOUtils.toString(new FileInputStream(notHandleFiles), "UTF-8");
                Map mylang_overwrite_map = SysUtils.parseMap(mylang_overwrite);
                un_handle_json = mylang_overwrite_map;
                if (un_handle_json == null) {
                    un_handle_json = new HashMap<>();
                }
            } catch (IOException e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
        }
        un_handle_json.put(source, "");
        try {
            FileUtils.writeStringToFile(notHandleFiles, JSON.toJSONString(un_handle_json));
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    private static Map<String, String> un_handle_json = new HashMap<String, String>();

    public static void updateLangByIndex(int selectedIndex) {
        updateLang(langList[selectedIndex]);
        int friendly_reminder = JOptionPane.showConfirmDialog(null, t("Set the language successfully! Would you like to restart this application so as to apply the entire changes?"), t("Friendly Reminder"), JOptionPane.YES_NO_OPTION);
        if (friendly_reminder == JOptionPane.YES_OPTION) {
            System.out.println("restart now");
            UserOperationFrame.startAndStopBefore();
        }
    }

    private static void updateLang(String lang) {
        getAllConfig().put("lang", lang);
        saveConfig();
    }

    public static void saveConfig() {
        File configFile = getConfigFile();
        SysUtils.writeStrToFile(configFile, JSON.toJSONString(getAllConfig(), true));
    }

    public static String getCrtLang() {
        return getAllConfig().get("lang");
    }

    public static Map<String, String> getAllConfig() {
        if (app_configs.size() == 0) {
            reInitConfig();
        }
        return app_configs;
    }

    public static void reInitConfig() {
        String appDefaultsJson = getAppDefaultJsonContent();
        File configFile = getConfigFile(appDefaultsJson);
        Map<String, String> map_appDefaultsJson = SysUtils.toMap(appDefaultsJson);
        Map<String, String> map_appJson = SysUtils.toMap(SysUtils.readFileToStr(configFile));
        Iterator<String> iterator = map_appDefaultsJson.keySet().iterator();
        while (iterator.hasNext()) {
            String next = iterator.next();
            Object obj = map_appJson.get(next);
            if (obj == null) {
                map_appJson.put(next, map_appDefaultsJson.get(next));
            }
        }
        app_configs.putAll(map_appJson);
    }

    private static String getAppDefaultJsonContent() {
        return SysUtils.getFileBytesForStr("/internal/app_defaults.json");
    }

    private static File getConfigFile(String appDefaultsJson) {
        File jarCurrentFolder = SysUtils.getJarCurrentFolder();
        File configFile = new File(jarCurrentFolder, "data/app.json");
        if (!configFile.getParentFile().exists()) {
            configFile.getParentFile().mkdirs();
        }
        if (!configFile.exists()) {
            SysUtils.writeStrToFile(configFile, appDefaultsJson);
        }
        return configFile;
    }

    private static final Map<String, String> app_configs = new HashMap<String, String>();

    public static void rollbackConfig() {
        File configFile = getConfigFile();
        SysUtils.writeStrToFile(
                configFile,
                getAppDefaultJsonContent()
        );
    }

    public static File getConfigFile() {
        String appDefaultsJson = getAppDefaultJsonContent();
        File configFile = getConfigFile(appDefaultsJson);
        return configFile;
    }

//    public static void saveConfig(){
//        File configFile = getConfigFile();
//        SysUtils.writeStrToFile(
//                configFile,
//                getAppDefaultJsonContent()
//        );
//    }
}
