package cc.codegen.client.gui.ref;

import cc.codegen.client.service.vm.BootDetail;
import cc.codegen.client.util.SysUtils;
import com.alibaba.fastjson.JSON;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class SysStatus {
    public static SysStatus ref = new SysStatus();
    public File currentWorkSpaceFile;
    public File currentWorkSpaceConfigJsonFile;
    public Map<String, String> currentWorkSpaceConfigJson;
    public boolean usingChinaMirrors = true;
    public BootDetail currentBootDetail;
    public Map<String, WorkSpaceUIData> workSpaceUIDataMap = new HashMap<>();

    public static void saveCurrentWorkSpaceConfigJson() {
        if (ref.currentWorkSpaceFile != null) {
            SysUtils.writeStrToFile(ref.currentWorkSpaceFile, JSON.toJSONString(ref.currentWorkSpaceConfigJson));
        }
    }
}
