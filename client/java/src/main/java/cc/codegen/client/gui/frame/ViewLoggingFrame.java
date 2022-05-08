package cc.codegen.client.gui.frame;

import cc.codegen.client.gui.ref.SysStatus;
import cc.codegen.client.service.vm.BootDetail;
import cc.codegen.client.util.SysUtils;
import cn.hutool.core.io.FileUtil;

import javax.swing.*;
import java.awt.*;

public class ViewLoggingFrame extends JFrame {
    public ViewLoggingFrame() throws HeadlessException {
        setSize(800, 600);
        JTextArea viewLogTextArea = new JTextArea();
        setLayout(new BorderLayout());
        add(new JScrollPane(viewLogTextArea), BorderLayout.CENTER);
        BootDetail currentBootDetail = SysStatus.ref.currentBootDetail;
        if (currentBootDetail != null) {
            viewLogTextArea.setText(SysUtils.readFileToStr(currentBootDetail.getLogFile()));
        }
        setLocationRelativeTo(null);
        setVisible(true);
    }
}
