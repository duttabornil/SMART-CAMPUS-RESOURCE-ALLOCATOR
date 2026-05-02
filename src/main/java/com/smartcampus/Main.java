package com.smartcampus;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import java.awt.Font;
import javax.swing.plaf.FontUIResource;


public class Main {
    public static void main(String[] args) {
        try {
<<<<<<< HEAD
            UIManager.setLookAndFeel(new com.formdev.flatlaf.FlatLightLaf());
            UIManager.put("defaultFont", new Font("SansSerif", Font.PLAIN, 15));
            UIManager.put("Button.arc", 8);
            UIManager.put("Component.arc", 8);
            UIManager.put("TextComponent.arc", 8);
            UIManager.put("TabbedPane.showTabSeparators", true);
=======
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }

            Font myFont = new Font("SansSerif", Font.PLAIN, 18);
            FontUIResource fontRes = new FontUIResource(myFont);
            UIManager.getLookAndFeelDefaults().put("defaultFont", fontRes);
            java.util.Enumeration<Object> keys = UIManager.getDefaults().keys();
            while (keys.hasMoreElements()) {
                Object key = keys.nextElement();
                Object value = UIManager.get(key);
                if (value instanceof FontUIResource) {
                    UIManager.put(key, fontRes);
                }
            }
>>>>>>> 931c2b08b0f80c4aec1628d7f2ae6881bca5144c
        } catch (Exception e) {
            e.printStackTrace();
        }

        SwingUtilities.invokeLater(() -> {
            MainFrame mainFrame = new MainFrame();
            mainFrame.setVisible(true);
        });
    }
}
