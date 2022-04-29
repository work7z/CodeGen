package cc.codegen.client.gui.pages.panels;

import cc.codegen.client.gui.frame.UserOperationFrame;
import cc.codegen.client.util.I18nUtils;
import cc.codegen.client.util.SysUtils;
import org.apache.commons.io.FileUtils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import static cc.codegen.client.util.I18nUtils.t;
import static cc.codegen.client.util.SysUtils.html;

public class UserSettings extends JPanel {

    public UserSettings() {
        setAlignmentY(TOP_ALIGNMENT);
        setAlignmentX(LEFT_ALIGNMENT);

        JTabbedPane jTabbedPane = new JTabbedPane();
        JPanel languagePanel = addLanguagePanel();
        JPanel servicePanel = addAutorunPanel();
        JPanel autoSettings = addAppSettingsPanel();
        jTabbedPane.add(t("Language"), languagePanel);
        // TODO: service panel will be added in the following versions
//        jTabbedPane.add(t("Services"), servicePanel);
        jTabbedPane.add(t("Configuration"), autoSettings);
        setLayout(new BorderLayout());
        add(jTabbedPane, BorderLayout.CENTER);
//        setLayout(new BoxLayout(this, Y_AXIS));
//        add(languagePanel);
//        add(servicePanel);
//        add(autoSettings);
//        setBorder(new EmptyBorder(10, 20, 10, 20));
    }

    public JPanel addLanguagePanel() {
        JPanel languagePanel = new JPanel();
        GridBagLayout layout = new GridBagLayout();
        languagePanel.setLayout(layout);
        languagePanel.setBorder(new EmptyBorder(0, 0, 0, 0));
        Component label = newTitleLabel(t("Language"));
        final JComboBox cmb = new JComboBox();
        cmb.addItem("English");
        cmb.addItem("简体中文(Simplified Chinese)");
        cmb.addItem("繁體中文(Traditional Chinese)");
        cmb.setSelectedIndex(I18nUtils.getCurrentLangIndex());
        cmb.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.out.println(cmb.getSelectedIndex());
                I18nUtils.updateLangByIndex(cmb.getSelectedIndex());
            }
        });
        layout.addLayoutComponent(label, getOneLine());
        languagePanel.add(label);
        JPanel btnPanel = addOneComponent(layout, createJPanelWithEmbedComponent(cmb));
        languagePanel.add(btnPanel);
        JPanel innerPanel = new JPanel();
        innerPanel.setLayout(new BorderLayout());
        innerPanel.add(languagePanel, BorderLayout.NORTH);
        innerPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        return innerPanel;
    }

    public JPanel addAutorunPanel() {
        GridBagLayout layout = new GridBagLayout();
        JPanel autoRunPanel = new JPanel();
        final JComboBox autoRunCombo = new JComboBox();
        autoRunCombo.addItem(t("Yes, start all services automatically."));
        autoRunCombo.addItem(t("No, start the service manually."));
        autoRunCombo.setSelectedIndex(0);
        autoRunCombo.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
            }
        });
        autoRunPanel.setLayout(layout);
        autoRunPanel.setBorder(new EmptyBorder(0, 0, 0, 0));
        // label
        JLabel label = newTitleLabel(t("AutoRun Services"));
        label.setBorder(new EmptyBorder(0, 0, 0, 0));
        JPanel innerPanelForLabel = createJPanelWithEmbedComponent(label);
        layout.addLayoutComponent(innerPanelForLabel, getOneLine());
        autoRunPanel.add(innerPanelForLabel);
        // components
        JPanel innerPanelForComponent = createJPanelWithEmbedComponent(autoRunCombo);
        JComponent innerPanelForComponentWithOneLine = addOneComponent(layout, innerPanelForComponent);
        layout.addLayoutComponent(innerPanelForComponentWithOneLine, getOneLine());
        autoRunPanel.add(innerPanelForComponentWithOneLine);
        JPanel innerPanel = new JPanel();
        innerPanel.setLayout(new BorderLayout());
        innerPanel.add(autoRunPanel, BorderLayout.NORTH);
        innerPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        return innerPanel;
    }

    private JPanel createJPanelWithEmbedComponent(JComponent autoRunCombo) {
        JPanel innerPanel = new JPanel();
        innerPanel.setBorder(new EmptyBorder(0, 0, 0, 0));
        innerPanel.setLayout(new BorderLayout());
        innerPanel.setAlignmentX(LEFT_ALIGNMENT);
        innerPanel.add(autoRunCombo, BorderLayout.SOUTH);
        return innerPanel;
    }


    public JPanel addAppSettingsPanel() {
//        GridBagLayout layout = new GridBagLayout();
        JPanel appSettingsPanel = new JPanel();
        appSettingsPanel.setAlignmentX(LEFT_ALIGNMENT);
        JLabel child_language = newTitleLabel(t("App Settings"));
//        layout.addLayoutComponent(child_language, getOneLine());
        JButton reset_config = new JButton(t("Reset Config"));
        JButton reload_app = new JButton(t("Reload Toolkit"));
        reset_config.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (JOptionPane.showConfirmDialog(null, t("The app configuration will be reinstated after you clicked the confirm button, it will only rollback app config, not including these workspaces and related data. Are you sure you want to proceed?"), t("PLEASE BE NOTED THIS MESSAGE."), JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                    I18nUtils.rollbackConfig();
                    File procedureFolder = SysUtils.getProcedureFolder();
                    FileUtils.deleteQuietly(procedureFolder);
                    JOptionPane.showMessageDialog(null, t("Done. CodeGen will exit to apply this changes"));
                    System.exit(0);
                }
            }
        });
        reload_app.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                UserOperationFrame.startAndStopBefore();
            }
        });
        JPanel innerPanelForBtn = new JPanel();
        innerPanelForBtn.setBorder(new EmptyBorder(0, 0, 0, 0));
        innerPanelForBtn.setLayout(new FlowLayout(FlowLayout.LEFT));
        innerPanelForBtn.add(reset_config);
        innerPanelForBtn.add(reload_app);
//        JPanel child_btn_panel = addOneComponent(layout, innerPanelForBtn);
        JPanel child_btn_panel = createJPanelWithEmbedComponent(innerPanelForBtn);
        appSettingsPanel.setLayout(new BoxLayout(appSettingsPanel, BoxLayout.Y_AXIS));
        appSettingsPanel.setBorder(new EmptyBorder(0, 0, 0, 0));
//        layout.addLayoutComponent(child_language, getOneLine());
//        layout.addLayoutComponent(child_btn_panel, getOneLine());
        appSettingsPanel.add(createJPanelWithEmbedComponent(child_language));
        appSettingsPanel.add(child_btn_panel);
        JPanel innerPanel = new JPanel();
        innerPanel.setLayout(new BorderLayout());
        innerPanel.add(appSettingsPanel, BorderLayout.NORTH);
        // Eventually I realized how much I love css layout , I cannot endure such a layout... suffering
        innerPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        return innerPanel;
    }

    private JPanel addOneComponent(GridBagLayout mgr, JComponent cmb) {
        JPanel btnPanel = new JPanel();
        btnPanel.setAlignmentX(LEFT_ALIGNMENT);
        btnPanel.setLayout(new BorderLayout());
        btnPanel.add(cmb, BorderLayout.WEST);
        mgr.addLayoutComponent(btnPanel, getOneLine());
        return btnPanel;
    }

    public static GridBagConstraints getOneLine() {
        GridBagConstraints gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.fill = GridBagConstraints.BOTH;
        gridBagConstraints.gridwidth = GridBagConstraints.REMAINDER;
        gridBagConstraints.weightx = 1;
        gridBagConstraints.weighty = 1;
        return gridBagConstraints;
    }

    public static JLabel newTitleLabel(String language) {
        return new JLabel(html("<h2>" + (language) + "</h2>"));
    }

    public static JLabel newTitleLabel_h3(String language) {
        return new JLabel(html("<h3>" + (language) + "</h3>"));
    }

}
