package cc.codegen.client.gui.ref;

import cc.codegen.client.service.WorkspaceService;

import javax.swing.*;

import static cc.codegen.client.service.WorkspaceService.getCurrentUIData;

public class WorkSpaceUIData {
    private String currentButtonText;
    private String currentLoggingText;

    public String getCurrentButtonText() {
        return currentButtonText;
    }

    public void setCurrentButtonText(String currentButtonText) {
        this.currentButtonText = currentButtonText;
    }

    public String getCurrentLoggingText() {
        return currentLoggingText;
    }

    public void setCurrentLoggingText(String currentLoggingText) {
        this.currentLoggingText = currentLoggingText;
    }

    public static void syncCurrentWorkspace(String workSpaceName) {
        WorkSpaceUIData uiDataByWorkspaceName = WorkspaceService.getUIDataByWorkspaceName(workSpaceName);
        if (uiDataByWorkspaceName != null) {
            if (SysRef.service_start_or_stop != null) {
                uiDataByWorkspaceName.setCurrentButtonText(SysRef.service_start_or_stop.getText());
            }
            JTextArea current_workspace_running_loggings = SysRef.current_workspace_running_loggings;
            if (current_workspace_running_loggings != null) {
                uiDataByWorkspaceName.setCurrentLoggingText(current_workspace_running_loggings.getText());
            }
        }
    }
}
