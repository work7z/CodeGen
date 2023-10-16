package cc.codegen.client.util;

import cc.codegen.client.dto.BasisRepository;
import cc.codegen.client.gui.frame.UserOperationFrame;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import javax.swing.*;
import java.io.*;
import java.net.URL;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import static cc.codegen.client.util.I18nUtils.t;

public class SysUtils {

    public static String html(String text) {
        return "<html><div>" + text + "</div></html>";
    }

    static Map parseMap(String s) {
        return JSON.parseObject(s, Map.class);
    }

    public static String getFileBytesForStr(String path) {
        try {
            return new String(getFileBytes(path), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    public static byte[] getFileBytes(String path) {
        if (!path.startsWith("/")) {
            path = "/" + path;
        }
        try {
            InputStream resourceAsStream = UserOperationFrame.class.getResourceAsStream(path);
            if (resourceAsStream == null) {
                URL resource = UserOperationFrame.class.getResource(path);
                if (resource == null) {
                    throw new RuntimeException("no valid resources");
                }
                resourceAsStream = new FileInputStream(resource.getFile());
            }
            return IOUtils.toByteArray((InputStream) resourceAsStream);
        } catch (Throwable e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    public static boolean isDevMode() {
        return new File("/users/jerrylai").exists();
    }

    public static File getJarCurrentFolder() {
        String file = I18nUtils.class.getProtectionDomain().getCodeSource().getLocation().getFile();
        return new File(file);
    }

    public static File getOriginalUnExactFilesFolder() {
        if (SysUtils.isDevMode()) {
            return new File("/Users/jerrylai/Documents/PersonalProjects/denote-be/pal/clients/src/main/extra");
        }
        return getJarCurrentFolder();
    }

    public static File getProcedureFolder() {
        return new File(getJarCurrentFolder(), "procedures");
    }

    public static void writeStrToFile(File configFile, String appDefaultsJson) {
        try {
            FileUtils.writeStringToFile(configFile, appDefaultsJson);
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    public static Map toMap(String appDefaultsJson) {
        try {
            Map map = JSONObject.parseObject(appDefaultsJson, Map.class);
            return map;
        } catch (Throwable throwable) {
            throwable.printStackTrace();
            return new HashMap();
        }
    }

    public static String readFileToStr(File configFile) {
        try {
            return FileUtils.readFileToString(configFile);
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    public static void restart() {
        UserOperationFrame.startAndStopBefore();
    }

    public static void makeBorder(JPanel jPanel) {
        jPanel.setBorder(BorderFactory.createRaisedBevelBorder());
    }

    public static String getLanguage() {
        Locale locale = Locale.getDefault();
        return locale.getLanguage();
    }

    public static String getCountry() {
        Locale locale = Locale.getDefault();
        return locale.getCountry();
    }

    public static void mkdirs(File workspaces) {
        if (!workspaces.exists()) {
            workspaces.mkdirs();
        }
    }

    public static BasisRepository getBasicRepositoryDefine() {
        String fileBytesForStr = SysUtils.getFileBytesForStr("/internal/basis-repository.json");
        return JSON.parseObject(fileBytesForStr, BasisRepository.class);
    }

    public static File getWorkSpaceRootDir() {
        return new File(SysUtils.getJarCurrentFolder(), "data/workspaces");
    }

    public static void sleep(int i) {
        try {
            Thread.sleep(i);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void alert(String x) {
        JOptionPane.showMessageDialog(null, x, t("Friendly Reminder"), JOptionPane.INFORMATION_MESSAGE);
    }

    public static boolean confirm(String s) {
        return JOptionPane.showConfirmDialog(null, s, t("Operation Confirmation"), JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION;
    }

    public static File createTempFile(String codegen, String logging) {
        try {
            return File.createTempFile(codegen, logging);
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    public static void rm(File dest) {
        if (dest.exists()) {
            FileUtils.deleteQuietly(dest);
        }
    }
}
