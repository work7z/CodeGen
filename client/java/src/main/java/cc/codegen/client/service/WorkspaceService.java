package cc.codegen.client.service;

import cc.codegen.client.gui.ref.SysRef;
import cc.codegen.client.gui.ref.SysStatus;
import cc.codegen.client.gui.ref.WorkSpaceUIData;
import cc.codegen.client.service.vm.BootDetail;
import cc.codegen.client.util.SysUtils;

import java.io.File;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static cc.codegen.client.service.AppService.getCurrentWorkSpaceName;

public class WorkspaceService {
    public static Map<String, BootDetail> workspace_boot_detail_mapping = new ConcurrentHashMap<String, BootDetail>();

    public static void registerAndBootWorkSpaceIfNotBootedBefore(final File currentActiveWorkSpace) {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                synchronized (currentActiveWorkSpace.getAbsoluteFile().toString().intern()) {
                    // start handling the related service
                    String key = currentActiveWorkSpace.getAbsolutePath();
                    BootDetail bootDetail = workspace_boot_detail_mapping.get(key);
                    if (bootDetail != null) {
                        return;
                    }
                    bootDetail = new BootDetail(currentActiveWorkSpace);
                    workspace_boot_detail_mapping.put(key, bootDetail);
//                    bootDetail.start();
                }
            }
        });
        thread.setPriority(Thread.MAX_PRIORITY);
        thread.start();
    }

    public static WorkSpaceUIData getCurrentUIData() {
        String workspaceName = getCurrentWorkSpaceName();
        return getUIDataByWorkspaceName(workspaceName);
    }

    public static WorkSpaceUIData getUIDataByWorkspaceName(String workspaceName) {
        File workspaceRootDir = SysUtils.getWorkSpaceRootDir();
        File currentWorkspaceFile = new File(workspaceRootDir, workspaceName);
        WorkSpaceUIData workSpaceUIData = SysStatus.ref.workSpaceUIDataMap.get(currentWorkspaceFile.getAbsolutePath());
        if (workSpaceUIData == null) {
            workSpaceUIData = new WorkSpaceUIData();
        }
        SysStatus.ref.workSpaceUIDataMap.put(currentWorkspaceFile.getAbsolutePath(), workSpaceUIData);
        return workSpaceUIData;
    }

}
