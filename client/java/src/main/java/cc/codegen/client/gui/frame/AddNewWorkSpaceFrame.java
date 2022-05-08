package cc.codegen.client.gui.frame;

import cc.codegen.client.service.AppService;
import cc.codegen.client.util.SysUtils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import static cc.codegen.client.gui.pages.panels.UserSettings.*;
import static cc.codegen.client.util.I18nUtils.t;
import static cc.codegen.client.util.SysUtils.makeBorder;

public class AddNewWorkSpaceFrame extends JDialog {
    public AddNewWorkSpaceFrame(Frame owner) {
        super(owner, t("Create New WorkSpace"));
        final File workspaces = SysUtils.getWorkSpaceRootDir();
        setLayout(new BorderLayout());
        // form logic
        JPanel form_panel = new JPanel();
        GridBagLayout mgr = new GridBagLayout();
        form_panel.setLayout(mgr);
        // 1.2.1
        addFormTitle(form_panel, mgr, "Workspace Name");
        // 1.2.2
        final JTextField jTextField = new JTextField();
        addDirectWithOneLine(form_panel, mgr, jTextField);
        // 1.final
        add(form_panel, BorderLayout.NORTH);
        form_panel.setBorder(new EmptyBorder(10, 10, 10, 10));
        // controls logic
        JPanel control_panel = new JPanel();
        makeBorder(control_panel);
        control_panel.setLayout(new FlowLayout(FlowLayout.RIGHT));
        JButton cancel = new JButton(t("Cancel"));
        final JDialog ref = this;
        cancel.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ref.setVisible(false);
                ref.dispose();
            }
        });
        control_panel.add(cancel);
        JButton create = new JButton(t("Create"));
        create.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String trim = jTextField.getText().trim();
                if (trim.equalsIgnoreCase("")) {
                    JOptionPane.showMessageDialog(null, t("Name cannot be empty."));
                    return;
                }
                File saveFolder = new File(workspaces, trim);
                if (saveFolder.exists()) {
                    JOptionPane.showMessageDialog(null, t("The name was occupied on your workspaces."));
                    return;
                }
                saveFolder.mkdirs();
                JOptionPane.showMessageDialog(null, t("Created new workspace successfully!"));
                AppService.reloadCenterPanel();
                ref.setVisible(false);
                ref.dispose();
            }
        });

        control_panel.add(create);
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
