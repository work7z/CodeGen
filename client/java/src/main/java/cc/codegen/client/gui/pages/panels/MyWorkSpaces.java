package cc.codegen.client.gui.pages.panels;

import cc.codegen.client.gui.frame.ViewLoggingFrame;
import cc.codegen.client.gui.ref.SysRef;
import cc.codegen.client.service.AppService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import static cc.codegen.client.util.I18nUtils.t;

public class MyWorkSpaces extends JPanel {
    public MyWorkSpaces() {
        JPanel jPanel = new JPanel();
        setLayout(new BorderLayout());
        setAlignmentY(TOP_ALIGNMENT);
        setAlignmentX(LEFT_ALIGNMENT);
        // north panel
        JPanel topNorthPanel = new JPanel();
        JLabel jLabel = new JLabel(t("Workspaces: "));
        final JComboBox workspace_combo = new JComboBox();
        SysRef.workspace_combo = workspace_combo;
        topNorthPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        topNorthPanel.add(jLabel);
        topNorthPanel.add(workspace_combo);
        // wrap north
        JPanel wrapNorthPanel = new JPanel();
        wrapNorthPanel.setLayout(new BorderLayout());
        JPanel innerPanel = new JPanel();
        innerPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
        JButton aNew = new JButton(t("New"));
        JButton aEdit = new JButton(t("Edit"));
        SysRef.new_btn = aNew;
        SysRef.edit_btn = aEdit;
        innerPanel.add(aNew);
        innerPanel.add(aEdit);
        wrapNorthPanel.add(innerPanel, BorderLayout.EAST);
        wrapNorthPanel.add(topNorthPanel, BorderLayout.CENTER);
        add(wrapNorthPanel, BorderLayout.NORTH);
        // south panel, controls
        JPanel btnPanel = new JPanel();
        btnPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
        final JButton start_or_stop = new JButton(t("Start Service"));
        final JButton open = new JButton(t("Open ToolBox"));
        SysRef.service_start_or_stop = start_or_stop;
        SysRef.service_open = open;
        final JButton viewLogs = new JButton(t("View Logs"));
        btnPanel.add(start_or_stop);
        btnPanel.add(viewLogs);
        btnPanel.add(open);
        viewLogs.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new ViewLoggingFrame();
            }
        });
        add(btnPanel, BorderLayout.SOUTH);
        // center panel
        JPanel centerPanel = new JPanel();
        SysRef.service_center_panel = centerPanel;
//        add(new JScrollPane(centerPanel), BorderLayout.CENTER);
        add((centerPanel), BorderLayout.CENTER);
        centerPanel.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        setBorder(new EmptyBorder(5, 5, 5, 5));
        AppService.reloadCenterPanel();
    }
}
