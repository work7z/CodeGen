package cc.codegen.client.gui.frame;

import cc.codegen.client.gui.layout.BasicUserViewLayout;
import cc.codegen.client.gui.pages.MainPage;
import cc.codegen.client.gui.ref.SysRef;
import cc.codegen.client.util.I18nUtils;
import cc.codegen.client.util.SysUtils;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.lang.reflect.Method;

import static cc.codegen.client.util.I18nUtils.t;

public class UserOperationFrame extends JFrame {
    public UserOperationFrame() throws HeadlessException, IOException {
        Image image = new ImageIcon(SysUtils.getFileBytes("/img/icon.png")).getImage();
        setIconImage(image);
        try {
            Object r = Class.forName("com.apple.eawt.Application")
                    .getMethod("getApplication")
                    .invoke(null);
            Method mr = r.getClass().getMethod("setDockIconImage", Class.forName("java.awt.Image"));
            mr.invoke(r, image);
        } catch (Throwable throwable) {
            // do nothing here
        }
        // set layout
        setLayout(new BasicUserViewLayout());
        // concrete components
        JLabel my_workSpace = new JLabel(t("v1.3.0_102"));
        JPanel jPanel = new JPanel();
        jPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        jPanel.setAlignmentY(Component.CENTER_ALIGNMENT);
        jPanel.add(my_workSpace);
        add(jPanel, BorderLayout.SOUTH);
        jPanel.setBorder(BorderFactory.createRaisedBevelBorder());
        MainPage comp = new MainPage();
        add(comp, BorderLayout.CENTER);
        // other settings
        setSize(420, 600);
        setLocationRelativeTo(null);
        setTitle(t("CodeGen ToolBox"));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setVisible(true);
    }

    public static UserOperationFrame ref;

    public static void startAndStopBefore() {
        try {
            SysRef.init_service_center_before = false;
            I18nUtils.reInitConfig();
            if (ref != null) {
                ref.setVisible(false);
                ref.dispose();
            }
            UserOperationFrame userOperationPanel = new UserOperationFrame();
            ref = userOperationPanel;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
