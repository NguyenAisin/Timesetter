package TimeSetter;

//Author, NguyenAisin, Discord: Nguyennes

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

public class Timersetter {
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
        }
        SwingUtilities.invokeLater(() -> new frm_Main().setVisible(true));
    }
}
