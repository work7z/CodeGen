package cc.codegen.client.gui.pages.panels;

import cc.codegen.client.util.SysInfo;
import cn.hutool.core.util.RuntimeUtil;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

import static cc.codegen.client.util.I18nUtils.t;
import static cc.codegen.client.util.SysUtils.html;
import static javax.swing.BoxLayout.Y_AXIS;

public class AboutSoftware extends JPanel {
    public AboutSoftware() {
        BoxLayout lo = new BoxLayout(this, Y_AXIS);
        setLayout(lo);
        setBorder(new EmptyBorder(10, 20, 10, 20));
        setAlignmentX(CENTER_ALIGNMENT);
        add((new JLabel("<html><h2>" + t("Current Version") + "</h2></html>")));
        add(
                (new JLabel(html("<b>" + t("Client Toolkit:") + "</b>" + " " + SysInfo.CLIENT_VERSION)))
        );
        add(
                (new JLabel(html(bold(t("Binary Distribution:")) + " " + t("CodeGen Toolkit was built from the CI/CD process on Github"))))
        );
        add(
                (new JLabel(
                        html(
                                bold(t("Source Code:")) + " "
                                        + (t("The client source code of CodeGen Toolkit has been published now, for more details please refer to {0}", " https://github.com/work7z/CodeGen")
                                )
                        )))
        );
        add((new JLabel("<html><h2>" + t("Basic Information") + "</h2></html>")));
        add(new JLabel(html(bold(t("Official Website:")) + " https://cloud.codegen.cc")));
        add(new JLabel(html(bold(t("Portal for CodeGen:")) + " https://portal.codegen.cc")));
        add(new JLabel(html(bold(t("Docs for CodeGen:")) + " https://cloud.codegen.cc/documentation")));
        add(new JLabel(html(bold(t("Contact Developer:")) + " work7z@outlook.com")));
        add(new JLabel(html(bold(t("Release an Issue:")) + " https://github.com/work7z/CodeGen/issues")));
        add((new JLabel("<html><h2>" + t("Our Commitment") + "</h2></html>")));
        add(new JLabel(html((t("We will never ever forever infringe upon user's privacy and interests, we hold the opinion that privacy matters above all things, meanwhile, to achieve this commitment, we hereby guarantee you the following items, but not limited to the following items: ") + "<br/>" +
                        "<ol>" +
                        "<li>" + t("CodeGen will NEVER analyze or upload your files on this PC.") + "</li>" +
                        "<li>" + t("CodeGen will NEVER use the device to launch any kind of attack or abuse.") + "</li>" +
                        "<li>" + t("CodeGen is aiming to improve developer’s efficiency and save time as much as it can.") + "</li>" +
                        "<li>" + t("Without the user’s definite permission, CodeGen will NEVER read/write your files or execute any unexpected operation.") + "</li>" +
                        "<li>" + t("CodeGen is offline-able software, which means you can use it as well without the Internet.") + "</li>" +
                        "</ol>")
                )
                )
        );
    }

    private Component center(JLabel jLabel) {
        jLabel.setAlignmentX(CENTER_ALIGNMENT);
        return jLabel;
    }

    public static String bold(String str) {
        return "<b>" + str + "</b>";
    }
}
