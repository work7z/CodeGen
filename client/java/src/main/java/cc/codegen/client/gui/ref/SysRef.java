package cc.codegen.client.gui.ref;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

public class SysRef {
    public static Thread downloading_dependencies_thread;
    public static JComboBox workspace_combo;
    public static String open_toolbox_link;
    public static JButton service_start_or_stop;
    public static JButton service_open;
    public static JPanel service_center_panel;
    public static boolean init_service_center_before = false;
    public static JButton new_btn;
    public static JButton edit_btn;
    public static JLabel download_process_label;
    public static JTextArea view_status_downloading_textarea;
    public static JTextArea current_workspace_running_loggings;

    public static void pushText(String s) {
        pushText(view_status_downloading_textarea, s);
    }

    public static void pushText(JTextArea view_status_downloading_textarea, String s) {
        if (view_status_downloading_textarea == null) {
            return;
        }
        String[] latest = (view_status_downloading_textarea.getText() + s).split("\n");
        List<String> newArr = new ArrayList<String>();
        int idx = 0;
        String myarr = "";
        for (String s1 : latest) {
            if (false && idx > 200) {
//                break;
            } else {
                newArr.add(s1);
                myarr += (s1 + "\n");
            }
            idx++;
        }
        if (myarr.length() > 500000) {
            myarr = myarr.substring(500000 - 100, myarr.length());
        }
        view_status_downloading_textarea.setText(myarr);
        view_status_downloading_textarea.setCaretPosition(view_status_downloading_textarea.getDocument().getLength());
    }
}
