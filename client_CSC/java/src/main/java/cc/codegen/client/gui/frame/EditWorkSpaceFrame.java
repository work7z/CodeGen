package cc.codegen.client.gui.frame;

import cc.codegen.client.service.AppService;
import cc.codegen.client.gui.ref.SysStatus;
import cc.codegen.client.util.SysUtils;
import org.apache.commons.io.FileUtils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;

import static cc.codegen.client.gui.pages.panels.UserSettings.getOneLine;
import static cc.codegen.client.gui.pages.panels.UserSettings.newTitleLabel_h3;
import static cc.codegen.client.util.I18nUtils.t;
import static cc.codegen.client.util.SysUtils.makeBorder;

public class EditWorkSpaceFrame extends JDialog {
    public EditWorkSpaceFrame(Frame owner) {
        super(owner, t("Edit Current WorkSpace"));
        final File workspaces = SysUtils.getWorkSpaceRootDir();
        if (SysStatus.ref.currentWorkSpaceFile == null || !SysStatus.ref.currentWorkSpaceFile.exists()) {
            SysUtils.alert(t("It seems there's no selected workspace can be edited, please create an accessible workspace firstly"));
            setVisible(false);
            return;
        }
        setLayout(new BorderLayout());
        // form logic
        JPanel form_panel = new JPanel();
        GridBagLayout mgr = new GridBagLayout();
        form_panel.setLayout(mgr);
        // 1.2.1
        addFormTitle(form_panel, mgr, "FilePath");
        // 1.2.2
        final JTextField jTextField = new JTextField();
        jTextField.setText(SysStatus.ref.currentWorkSpaceFile.getAbsolutePath());
        addDirectWithOneLine(form_panel, mgr, jTextField);
        // 1.final
        add(form_panel, BorderLayout.NORTH);
        form_panel.setBorder(new EmptyBorder(10, 10, 10, 10));
        // controls logic
        JPanel control_panel = new JPanel();
        makeBorder(control_panel);
        control_panel.setLayout(new FlowLayout(FlowLayout.RIGHT));
        JButton close = new JButton(t("Close"));
        final JDialog ref = this;
        close.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ref.setVisible(false);
                ref.dispose();
            }
        });
        control_panel.add(close);
        JButton delete = new JButton(t("Delete"));
        delete.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (SysUtils.confirm(t("Are you sure that you want to delete this workspace entirely? This operation is irreversible."))) {
                    FileUtils.deleteQuietly(SysStatus.ref.currentWorkSpaceFile);
                    SysStatus.ref.currentWorkSpaceFile = null;
                    SysStatus.ref.currentWorkSpaceConfigJsonFile = null;
                    SysStatus.ref.currentWorkSpaceConfigJson = null;
                    AppService.reloadCenterPanel();
                    ref.setVisible(false);
                    ref.dispose();
                }
            }
        });
        JButton duplicate = new JButton(t("Duplicate"));
        duplicate.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (SysUtils.confirm(t("Are you sure that you want to create a copy of the workspace?"))) {
                    File crtWorkSpaceFolder = SysStatus.ref.currentWorkSpaceFile;
                    File newWorkSpaceFolder = new File(crtWorkSpaceFolder.getAbsoluteFile() + "_copy");
                    try {
                        FileUtils.copyDirectoryToDirectory(crtWorkSpaceFolder, newWorkSpaceFolder);
                        SysUtils.alert(t("Copied a new copy of the workspace, including its data and configuration."));
                        AppService.reloadCenterPanel();
                        ref.setVisible(false);
                        ref.dispose();
                    } catch (IOException ex) {
                        ex.printStackTrace();
                        SysUtils.alert("Encountered an error in the process, please check " + ex.getMessage());
                    }
                }
            }
        });
        control_panel.add(delete);
        control_panel.add(duplicate);
        add(control_panel, BorderLayout.SOUTH);
        // other
        setSize(500, 200);
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void addDirectWithOneLine(JPanel form_panel, GridBagLayout mgr, JTextField jTextField) {
        mgr.addLayoutComponent(jTextField, getOneLine());
        form_panel.add(jTextField);
    }

    private void addFormTitle(JPanel form_panel, GridBagLayout mgr, String mytext) {
        Component name = newTitleLabel_h3(t(mytext));
        mgr.addLayoutComponent(name, getOneLine());
        form_panel.add(name);
    }

}
