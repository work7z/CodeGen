package cc.codegen.client.service;

import cc.codegen.client.dto.BasisRepository;
import cc.codegen.client.dto.Core;
import cc.codegen.client.gui.frame.AddNewWorkSpaceFrame;
import cc.codegen.client.gui.frame.EditWorkSpaceFrame;
import cc.codegen.client.gui.frame.UserOperationFrame;
import cc.codegen.client.gui.ref.SysRef;
import cc.codegen.client.gui.ref.SysStatus;
import cc.codegen.client.gui.ref.WorkSpaceUIData;
import cc.codegen.client.service.vm.BootDetail;
import cc.codegen.client.util.I18nUtils;
import cc.codegen.client.util.SysUtils;
import cn.hutool.core.io.StreamProgress;
import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.ZipUtil;
import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson.JSON;
import org.apache.commons.io.FileUtils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.*;
import java.util.List;

import static cc.codegen.client.gui.ref.SysRef.*;
import static cc.codegen.client.service.WorkspaceService.getCurrentUIData;
import static cc.codegen.client.util.I18nUtils.getAllConfig;
import static cc.codegen.client.util.I18nUtils.t;
import static cc.codegen.client.util.SysUtils.*;
import static java.awt.Component.CENTER_ALIGNMENT;
import static javax.swing.BoxLayout.Y_AXIS;

public class AppService {

    public static void reloadCenterPanel() {
        File workspaces = SysUtils.getWorkSpaceRootDir();
        SysUtils.mkdirs(workspaces);
        // add workspaces
        List<String> workspaces_dirname = new ArrayList<String>();
        File[] files = workspaces.listFiles();
        workspace_combo.removeAllItems();
        for (int i = 0; i < files.length; i++) {
            workspaces_dirname.add(files[i].getName());
            checkForEachWorkSpace(files[i]);
            workspace_combo.addItem(files[i].getName());
        }
        if (!SysRef.init_service_center_before) {
            SysStatus.ref = new SysStatus();
            SysRef.init_service_center_before = true;
            final CardLayout card_mgr = new CardLayout();
            SysRef.service_center_panel.setLayout(card_mgr);
            // no workspace
            createNoWorkspacePanel();
            // init data panel
            createInitDataPanel(card_mgr);
            // downloading_dependencies
            createDownloadingDependenciesPanel(card_mgr);
            // main_logic_panel
            createMainLogicPanel();
            // init start/stop button
            initButtonEventForService();
        }
        CardLayout layout_for_center = (CardLayout) SysRef.service_center_panel.getLayout();
        if (workspaces_dirname.size() == 0) {
            layout_for_center.show(service_center_panel, "no_workspace");
        } else {
            boolean isInitDependencies = getAllConfig().get("is_init_dependencies").equalsIgnoreCase("false");
            if (isInitDependencies) {
                layout_for_center.show(service_center_panel, "download_dependencies");
            } else {
                int selectedIndex = workspace_combo.getSelectedIndex();
                if (selectedIndex == -1) {
                    workspace_combo.setSelectedIndex(0);
                    selectedIndex = 0;
                }
                updateWorkSpaceWhenItemChanged();
                // start handling the logic panel
                layout_for_center.show(service_center_panel, "main_logic_panel");
            }
        }
    }

    private static void initButtonEventForService() {
        service_start_or_stop.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ThreadUtil.execAsync(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            BootDetail currentBootDetail = SysStatus.ref.currentBootDetail;
                            if (currentBootDetail.isBooted()) {
                                currentBootDetail.destroy();
                            } else {
                                currentBootDetail.start();
                            }
                        } catch (Throwable throwable) {
                            throwable.printStackTrace();
                        }
                    }
                });
            }
        });
    }

    private static void createMainLogicPanel() {
        JPanel added_jpanel_for_each_card = new JPanel();
        added_jpanel_for_each_card.setLayout(new BorderLayout());
        JPanel btmMainLogicControlPanel = new JPanel();
        JTextArea current_workspace_running_loggings = new JTextArea();
        current_workspace_running_loggings.setText("Service is ready to start.");
        WorkSpaceUIData.syncCurrentWorkspace(getCurrentWorkSpaceName());
        SysRef.current_workspace_running_loggings = current_workspace_running_loggings;
        btmMainLogicControlPanel.setLayout(new BorderLayout());
        btmMainLogicControlPanel.add(new JScrollPane(current_workspace_running_loggings), BorderLayout.CENTER);
        // border layout -> south logic code
        added_jpanel_for_each_card.add(btmMainLogicControlPanel, BorderLayout.CENTER);
        service_center_panel.add((added_jpanel_for_each_card), "main_logic_panel");
    }

    private static void createNoWorkspacePanel() {
        JPanel added_jpanel_for_each_card = new JPanel();
        updateLayout(added_jpanel_for_each_card, SysRef.service_center_panel);
        added_jpanel_for_each_card.add(new JLabel(html(t("Please create your first workspace to start."))));
        added_jpanel_for_each_card.add(new JLabel(html(t("The create button is positioned on the right top corner."))));
        service_center_panel.add((added_jpanel_for_each_card), "no_workspace");
    }

    private static void createInitDataPanel(final CardLayout card_mgr) {
        JPanel added_jpanel_for_each_card;
        added_jpanel_for_each_card = new JPanel();
        updateLayout(added_jpanel_for_each_card, SysRef.service_center_panel);
        added_jpanel_for_each_card.setBorder(new EmptyBorder(10, 20, 10, 20));
        added_jpanel_for_each_card.setAlignmentX(CENTER_ALIGNMENT);
        added_jpanel_for_each_card.add((new JLabel("<html><h2>" + t("Verifying dependencies") + "</h2></html>")));
        added_jpanel_for_each_card.add(new JLabel((t("CodeGen needs to verify these necessary dependencies firstly."))));
        added_jpanel_for_each_card.add(new JLabel((t("Please click the button below to start."))));
        JButton download_now = new JButton(t("Start"));
        download_now.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                final File temp = new File(SysUtils.getJarCurrentFolder(), "temp/jars");
                FileUtils.deleteQuietly(temp);
                final File originalUnExacctFolder = SysUtils.getOriginalUnExactFilesFolder();
                final File saveJarInResources = new File(originalUnExacctFolder, "dependencies");
                card_mgr.show(service_center_panel, "downloading_dependencies");
                Thread thread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            BasisRepository basicRepositoryDefine = getBasicRepositoryDefine();
                            File bootCoreZip = new File(getOriginalUnExactFilesFolder(), "boot/core.zip");
                            // PART-1
                            if (bootCoreZip.exists() && bootCoreZip.length() != 0) {
                                // do nothing here
                            } else {
                                Core core = basicRepositoryDefine.getCore();
                                List<String> baseMirrorArr = null;
                                int mirror_settings = JOptionPane.showOptionDialog(null, t("Please select the mirror before downloading according to your area, meanwhile, please be noted that the downloading speeds will depend consequently on your choice."), t("Mirror Settings"), JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE, null, new Object[]{t("China Mirror"), t("Global Mirror"), t("Auto")}, t("Auto"));
                                if (mirror_settings == 2) {
                                    mirror_settings = "CN".equalsIgnoreCase(SysUtils.getCountry()) ? 0 : 1;
                                    JOptionPane.showMessageDialog(null, t("CodeGen has decided to use {0}" + ("") + " as the source for the following process. If the download speed isn't satisfying, please kindly choose another option manually.", t(mirror_settings == 0 ? "China Mirror" : "Global Mirror")), t("Friendly Reminder"), JOptionPane.INFORMATION_MESSAGE);
                                }
                                if (mirror_settings == 0) {
                                    SysStatus.ref.usingChinaMirrors = true;
                                } else {
                                    SysStatus.ref.usingChinaMirrors = false;
                                }
                                view_status_downloading_textarea.setText(t("Using {0} as the download mirror", mirror_settings == 0 ? t("China Mirror") : t("Global Mirror")) + "\n");
                                if (SysStatus.ref.usingChinaMirrors) {
                                    baseMirrorArr = core.getChina();
                                } else {
                                    baseMirrorArr = core.getGlobal();
                                }
                                int rdx = RandomUtil.randomInt(0, baseMirrorArr.size());
                                String baseHome = baseMirrorArr.get(rdx);
                                ArrayList<String> jars = basicRepositoryDefine.getJars();
                                int idx = 0;
                                for (final String jar : jars) {
                                    idx++;
                                    download_process_label.setText(t("Download Process") + ": " + idx + "/" + jars.size());
                                    final String completeLink = baseHome + jar;
                                    SysUtils.mkdirs(temp);
                                    final String viewFile = jar.split("/")[jar.split("/").length - 1];
                                    File tempFile = new File(temp, UUID.randomUUID().toString() + ".tmp");
                                    File saveFile = new File(saveJarInResources, viewFile);
                                    System.out.println(completeLink);
                                    SysRef.pushText("Downloading " + viewFile + "...");
                                    if (saveFile.exists() && saveFile.length() != 0) {
                                        // do nothing
                                        SysRef.pushText("Downloaded " + viewFile);
                                    } else {
                                        HttpUtil.downloadFile(completeLink, tempFile, new StreamProgress() {
                                            @Override
                                            public void start() {
                                            }

                                            @Override
                                            public void progress(long l1, long l) {
                                                System.out.println(completeLink);
                                                SysRef.pushText("Downloading the file... " + calc(l, l1) + "%(" + l + "/" + l1 + ")");
                                            }

                                            @Override
                                            public void finish() {
                                            }
                                        });
                                        SysRef.pushText("Downloaded " + viewFile);
                                        SysRef.pushText("Moving file...");
                                        FileUtils.copyFile(tempFile, saveFile);
                                        SysRef.pushText("Moved");
                                        FileUtils.deleteQuietly(tempFile);
                                    }
                                }
                            }
                            // PART-2
                            // exacting resources folder
                            download_process_label.setText(t("Verification Progress"));
                            view_status_downloading_textarea.setText("Start decompressing the files of local services..." + "\n");
                            exactByName("drivers", originalUnExacctFolder);
                            exactByName("core", originalUnExacctFolder);
                            exactByName("run-client", originalUnExacctFolder);
                            pushText("Verified.");
                            pushText("Done.");
                            getAllConfig().put("is_init_dependencies", "true");
                            I18nUtils.saveConfig();
                            card_mgr.show(service_center_panel, "main_logic_panel");
                            updateWorkSpaceWhenItemChanged();
                        } catch (Throwable throwable) {
                            SysRef.pushText("Failed to proceed, please check the error " + throwable.getMessage());
                            throwable.printStackTrace();
                        }
                    }
                });
                thread.setPriority(Thread.MAX_PRIORITY);
                SysRef.downloading_dependencies_thread = thread;
                thread.start();
            }
        });
        added_jpanel_for_each_card.add(download_now);
        service_center_panel.add((added_jpanel_for_each_card), "download_dependencies");
    }

    private static void createDownloadingDependenciesPanel(final CardLayout card_mgr) {
        JPanel downloading_deps = new JPanel();
        downloading_deps.setLayout(new BorderLayout());
        JLabel download_process = new JLabel(t("Please wait a moments"));
        JPanel ix = new JPanel();
        ix.setAlignmentX(CENTER_ALIGNMENT);
        ix.add(download_process);
        SysRef.download_process_label = download_process;
        downloading_deps.add(ix, BorderLayout.NORTH);
        JTextArea view_status_downloading_textarea = new JTextArea();
        SysRef.view_status_downloading_textarea = view_status_downloading_textarea;
        downloading_deps.add(new JScrollPane(view_status_downloading_textarea), BorderLayout.CENTER);
        JPanel control_downloading_pages = new JPanel();
        control_downloading_pages.setLayout(new FlowLayout(FlowLayout.RIGHT));
        JButton cancel = new JButton(t("Cancel"));
        cancel.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                card_mgr.show(service_center_panel, "download_dependencies");
                SysRef.downloading_dependencies_thread.stop();
            }
        });
        control_downloading_pages.add(cancel);
        downloading_deps.add(control_downloading_pages, BorderLayout.SOUTH);
        service_center_panel.add(downloading_deps, "downloading_dependencies");
        // register events
        workspace_combo.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                updateWorkSpaceWhenItemChanged();
            }
        });
        // new button register
        new_btn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new AddNewWorkSpaceFrame(UserOperationFrame.ref);
            }
        });
        edit_btn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new EditWorkSpaceFrame(UserOperationFrame.ref);
            }
        });
    }

    private static void exactByName(final String exactName, final File originalUnExacctFolder) throws IOException {
        File temp_drivers_folder = new File(getProcedureFolder(), "boot/" + exactName + "_temp");
        File final_drivers_folder = new File(getProcedureFolder(), "boot/" + exactName);
        SysUtils.mkdirs(temp_drivers_folder);
        SysUtils.mkdirs(final_drivers_folder);
        // unzipping the file
        pushText(("Detected the resource " + exactName));
        pushText(("Start decompressing files..."));
        ZipUtil.unzip(new File(originalUnExacctFolder, "boot/" + exactName + ".zip"), temp_drivers_folder);
        System.out.println(final_drivers_folder.getAbsoluteFile());
        // delete folder
        FileUtils.deleteQuietly(final_drivers_folder);
        FileUtils.moveDirectory(temp_drivers_folder, final_drivers_folder);
        FileUtils.deleteQuietly(temp_drivers_folder);
        pushText(("Decompressed."));
    }

    private static void updateLayout(JPanel jpanel_noworkspace, JPanel service_center_panel) {
        BoxLayout mgr = new BoxLayout(jpanel_noworkspace, Y_AXIS);
        jpanel_noworkspace.setLayout(mgr);
        jpanel_noworkspace.setBorder(new EmptyBorder(10, 10, 10, 10));
    }

    public static void updateWorkSpaceWhenItemChanged() {
        String workspaceName = getCurrentWorkSpaceName();
        if (workspaceName == null) return;
        File workspaces = SysUtils.getWorkSpaceRootDir();
        File currentActiveWorkSpace = new File(workspaces, workspaceName);
        checkForEachWorkSpace(currentActiveWorkSpace);
        File workspaceJson = new File(currentActiveWorkSpace, "workspace.json");
        Map<String, String> finalWorkSpaceForCurrent = SysUtils.toMap(SysUtils.readFileToStr(workspaceJson));
        SysStatus.ref.currentWorkSpaceConfigJsonFile = workspaceJson;
        SysStatus.ref.currentWorkSpaceConfigJson = finalWorkSpaceForCurrent;
        SysStatus.ref.currentWorkSpaceFile = currentActiveWorkSpace;
        updateCurrentRelatedButtonByWorkSpaceName();
    }

    public static String getCurrentWorkSpaceName() {
        int selectedIndex = workspace_combo.getSelectedIndex();
        Object itemAt = workspace_combo.getItemAt(selectedIndex);
        if (itemAt == null) {
            return null;
        }
        String workspaceName = (String) itemAt;
        return workspaceName;
    }

    public static void updateCurrentRelatedButtonByWorkSpaceName() {
        String workspaceName = getCurrentWorkSpaceName();
        if (workspaceName == null) {
            return;
        }
        if (current_workspace_running_loggings == null) {
            return;
        }
        File crtWorkSpaceDir = new File(getWorkSpaceRootDir(), workspaceName);
        BootDetail bootDetail = WorkspaceService.workspace_boot_detail_mapping.get(crtWorkSpaceDir.getAbsolutePath());
        SysStatus.ref.currentBootDetail = bootDetail;
        if (bootDetail != null) {
            service_start_or_stop.setVisible(true);
            SysRef.service_start_or_stop.setText(t(bootDetail.isBooted() ? "Stop Service" : "Start Service"));
            WorkSpaceUIData.syncCurrentWorkspace(workspaceName);
            try {
                File logFile = bootDetail.getLogFile();
                if (logFile == null || !logFile.exists()) {
                    current_workspace_running_loggings.setText(t("Click the button below to start.") + "\n");
                } else {
                    current_workspace_running_loggings.setText((""));
//                    current_workspace_running_loggings.setText(FileUtils.readFileToString(logFile));
                }
                WorkSpaceUIData.syncCurrentWorkspace(workspaceName);
//                current_workspace_running_loggings.setCaretPosition(current_workspace_running_loggings.getDocument().getLength());
            } catch (Throwable e) {
                e.printStackTrace();
                current_workspace_running_loggings.setText("Err: " + e.getMessage());
                WorkSpaceUIData.syncCurrentWorkspace(workspaceName);
            }
        } else {
            service_start_or_stop.setVisible(true);
        }
    }

    private static void checkForEachWorkSpace(File checkWorkSpace) {
        SysUtils.mkdirs(checkWorkSpace);
        // handling work spaces logic
        File workspaceJson = new File(checkWorkSpace, "workspace.json");
        Map<String, String> defaultWorkSpaceDefaultsJson = SysUtils.toMap(SysUtils.getFileBytesForStr("/internal/workspace_defaults.json"));
        if (!workspaceJson.exists()) {
            SysUtils.writeStrToFile(workspaceJson, JSON.toJSONString(defaultWorkSpaceDefaultsJson, true));
        }
        boolean isInitDependencies = getAllConfig().get("is_init_dependencies").equalsIgnoreCase("true");
        if (isInitDependencies) {
            WorkspaceService.registerAndBootWorkSpaceIfNotBootedBefore(checkWorkSpace);
        }
    }


    public static String calc(long a, long d) {
        double ratio = (a / (double) d) * 100;
        DecimalFormat ratioFormat = new DecimalFormat("#.##");
        DecimalFormat percentFormat = new DecimalFormat("#.##%");
//        System.out.println("ratio = " + ratioFormat.format(ratio));
//        System.out.println("percent = " + percentFormat.format(ratio));
        String format = ratioFormat.format(ratio);
        if (!format.contains(".")) {
            format = format + ".00";
        }
        return format;
    }

}
