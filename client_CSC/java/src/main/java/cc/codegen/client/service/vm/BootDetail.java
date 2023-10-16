package cc.codegen.client.service.vm;

import cc.codegen.client.gui.ref.SysRef;
import cc.codegen.client.gui.ref.WorkSpaceUIData;
import cc.codegen.client.service.NetService;
import cc.codegen.client.util.SysUtils;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.core.util.ArrayUtil;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import java.io.*;
import java.util.*;

import static cc.codegen.client.gui.ref.SysRef.current_workspace_running_loggings;
import static cc.codegen.client.gui.ref.SysRef.pushText;
import static cc.codegen.client.service.WorkspaceService.getCurrentUIData;
import static cc.codegen.client.util.I18nUtils.getConfigFile;
import static cc.codegen.client.util.I18nUtils.t;

public class BootDetail {
    /**
     * no need to create redundancy getter/setter methods, I meant the simple makes the perfect :P
     */
    File currentWorkspaceFolder;

    public void start() {
        if (current_workspace_running_loggings == null) {
            throw new RuntimeException("shouldn't be empty here");
        }
        File configFile = getConfigFile();
        File dest = new File(currentWorkspaceFolder, "app_shadow.json");
        SysUtils.rm(dest);
        FileUtil.copyFile(configFile, dest);
        SysRef.service_start_or_stop.setText(t("Stop Service"));
        current_workspace_running_loggings.setText("Starting the server...\n");
        WorkSpaceUIData.syncCurrentWorkspace(currentWorkspaceFolder.getName());
//        updateCurrentRelatedButtonByWorkSpaceName();
        File procedureFolder = SysUtils.getProcedureFolder();
        // start running
        File coreFile = getFirstExistFileFromArr(new File(procedureFolder, "boot/core/core"), new File(procedureFolder, "boot/core"));
        File runClientFile = getFirstExistFileFromArr(new File(procedureFolder, "boot/run-client/run-client"), new File(procedureFolder, "boot/run-client"));
        String javaHome = System.getProperty("java.home");
        System.out.println(javaHome);
        File javaHomeFile = new File(javaHome);
        Iterator<File> iterator_javahome = FileUtils.iterateFiles(javaHomeFile, new String[]{"jar"}, true);
        Iterator<File> iterator_core = FileUtils.iterateFiles(coreFile, new String[]{"jar"}, true);
        List<File> allJarList = new ArrayList<File>();
        while (iterator_javahome.hasNext()) {
            allJarList.add(iterator_javahome.next());
        }
        while (iterator_core.hasNext()) {
            allJarList.add(iterator_core.next());
        }
        List<File> allFilesInJavaHome = FileUtil.loopFiles(javaHomeFile);
        File javaOrJavaExeFile = null;
        for (File eachFile : allFilesInJavaHome) {
            if (eachFile.getName().equalsIgnoreCase("java") || eachFile.getName().equalsIgnoreCase("java.exe")) {
                javaOrJavaExeFile = eachFile;
                break;
            }
        }
        if (javaOrJavaExeFile == null) {
            pushText(current_workspace_running_loggings, "Failed to start, there's no available java command to be used.");
            return;
        }
        File processFolder = new File(currentWorkspaceFolder, "process");
        FileUtils.deleteQuietly(processFolder);
        // run command
        Set<String> parentFolder = new HashSet<String>();
        boolean isWin = FileUtil.isWindows();
        for (File file : allJarList) {
            if (isWin) {
                parentFolder.add(file.getParentFile().getAbsolutePath() + File.separatorChar + "*");
            } else {
                parentFolder.add(file.getAbsolutePath());
            }
        }
        String clzPath = ArrayUtil.join(parentFolder.toArray(), File.pathSeparator);
        clzPath = clzPath + File.pathSeparator + runClientFile.getAbsolutePath();
        File apphome = currentWorkspaceFolder;
        File appdata = new File(apphome, "data");
        final File tempFile = SysUtils.createTempFile("codegen", "logging");
        logFile = tempFile;
        if (!tempFile.exists()) {
            FileUtil.touch(tempFile);
            FileUtil.writeUtf8String("", tempFile);
        }
        String[] cmdArr = new String[]{javaOrJavaExeFile.getAbsolutePath(), "-Dfile.encoding=utf-8", "-cp", String.format("%s", clzPath), "com.denote.client.LightingBoot", NetService.getPort(), "prod", apphome.getAbsolutePath(), appdata.getAbsolutePath(), "myuid", tempFile.getAbsolutePath()};
        String cmdFullStr = ArrayUtil.join(cmdArr, " ");
        System.out.println(cmdFullStr);
        File bootFlagFile = getBootFlagFile();
        FileUtils.deleteQuietly(bootFlagFile);
        catchBooted = false;
        try {
            ThreadUtil.execAsync(new Runnable() {
                @Override
                public void run() {
                    long lastLength = -1;
                    while (true) {
                        if (lastLength != tempFile.length()) {
                            lastLength = tempFile.length();
                            idx++;
                            if (isBooted()) {
                                if (!catchBooted) {
                                    current_workspace_running_loggings.setText("The Server is Booted.");
                                }
                                catchBooted = true;
                                WorkSpaceUIData.syncCurrentWorkspace(currentWorkspaceFolder.getName());
                                break;
                            } else {
                                if (true || idx % 1 == 0) {
                                    String[] split = SysUtils.readFileToStr(tempFile).split("\n");
                                    if (split == null) {
                                        split = new String[0];
                                    }
                                    if (split.length != 0) {
                                        String s = split[split.length - 1];
                                        System.out.println(s);
                                        pushText(current_workspace_running_loggings, s);
                                    }
                                }
                            }
                        }
                        WorkSpaceUIData.syncCurrentWorkspace(currentWorkspaceFolder.getName());
                        SysUtils.sleep(300);
                    }
//                    FileUtil.tail(tempFile, new LineHandler() {
//                        @Override
//                        public void handle(String s) {
//                        }
//                    });
                }
            });
            ProcessBuilder processBuilder = new ProcessBuilder(cmdArr);
            process = processBuilder.start();
            process.waitFor();
            String errStr = IOUtils.toString(process.getErrorStream(), "UTF-8");
            String iptStr = IOUtils.toString(process.getInputStream(), "UTF-8");
            System.out.println(errStr);
            System.out.println(iptStr);
            FileUtils.deleteQuietly(getBootFlagFile());
//            updateCurrentRelatedButtonByWorkSpaceName();
            process.destroy();
            pushText(current_workspace_running_loggings, "Exited");
            System.out.println("finished");
        } catch (Throwable e) {
            e.printStackTrace();
        } finally {
            SysRef.service_start_or_stop.setText(t("Start Service"));
            WorkSpaceUIData.syncCurrentWorkspace(currentWorkspaceFolder.getName());
            destroy();
        }
    }

    int idx = 0;

    private File getBootFlagFile() {
        File bootFlagFile = new File(currentWorkspaceFolder, "bootflag.txt");
        return bootFlagFile;
    }

    boolean catchBooted = false;

    Process process;
    File logFile;

    public void destroy() {
        FileUtils.deleteQuietly(getBootFlagFile());
        if (process != null) {
            process.destroy();
        }
        current_workspace_running_loggings.setText("The Server is Stopped.");
        WorkSpaceUIData.syncCurrentWorkspace(currentWorkspaceFolder.getName());
//        updateCurrentRelatedButtonByWorkSpaceName();
    }

    public boolean isBooted() {
        return getBootFlagFile().exists();
    }

    private File getFirstExistFileFromArr(File... file) {
        for (File eachFile : file) {
            if (eachFile.exists()) {
                return eachFile;
            }
        }
        return null;
    }

    public BootDetail(File currentWorkspaceFolder) {
        SysUtils.mkdirs(currentWorkspaceFolder);
        this.currentWorkspaceFolder = currentWorkspaceFolder;
        FileUtils.deleteQuietly(getBootFlagFile());
    }

    public File getCurrentWorkspaceFolder() {
        return currentWorkspaceFolder;
    }

    public void setCurrentWorkspaceFolder(File currentWorkspaceFolder) {
        this.currentWorkspaceFolder = currentWorkspaceFolder;
    }

    public Process getProcess() {
        return process;
    }

    public void setProcess(Process process) {
        this.process = process;
    }

    public File getLogFile() {
        return logFile;
    }

    public void setLogFile(File logFile) {
        this.logFile = logFile;
    }
}

