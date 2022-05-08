package cc.codegen.client.gui.pages;

import cc.codegen.client.gui.pages.panels.AboutSoftware;
import cc.codegen.client.gui.pages.panels.MyWorkSpaces;
import cc.codegen.client.gui.pages.panels.UserSettings;

import javax.swing.*;

import java.awt.*;

import static cc.codegen.client.util.I18nUtils.t;

public class MainPage extends JPanel {
    public MainPage() {
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.add(t("My WorkSpaces"), (new MyWorkSpaces()));
        tabbedPane.add(t("User Settings"), (new UserSettings()));
        tabbedPane.add(t("About Software"), (new AboutSoftware()
//                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
//                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER
        ));
        setLayout(new BorderLayout());
        tabbedPane.setSelectedIndex(0);
        add(tabbedPane, BorderLayout.CENTER);
    }
}
