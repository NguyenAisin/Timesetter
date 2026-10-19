package TimeSetter;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.SystemTray;
import java.awt.Toolkit;
import java.awt.TrayIcon;
import java.awt.image.BufferedImage;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineEvent;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

public final class NotificationService implements AutoCloseable {
    private static final Logger LOG = Logger.getLogger(NotificationService.class.getName());
    private TrayIcon icon;

    public NotificationService(JFrame window) {
        if (!SystemTray.isSupported()) return;
        BufferedImage picture = new BufferedImage(32, 32, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = picture.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(new Color(43, 119, 237));
        g.fillOval(1, 1, 30, 30);
        g.setColor(Color.WHITE);
        g.setStroke(new BasicStroke(2.4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.drawOval(7, 7, 18, 18);
        g.drawLine(16, 16, 16, 10);
        g.drawLine(16, 16, 21, 19);
        g.dispose();
        icon = new TrayIcon(picture, "Timesetter Alpha");
        icon.setImageAutoSize(true);
        icon.addActionListener(e -> javax.swing.SwingUtilities.invokeLater(() -> {
            window.setVisible(true);
            window.setState(JFrame.NORMAL);
            window.toFront();
        }));
        try {
            SystemTray.getSystemTray().add(icon);
        } catch (java.awt.AWTException ex) {
            LOG.log(Level.WARNING, "System tray not available", ex);
            icon = null;
        }
    }

    public void notifyFinished(JFrame window, String message) {
        String text = message == null || message.isBlank() ? "Timeup" : message.trim();
        if (icon != null) {
            icon.displayMessage("Timesetter Alpha", text, TrayIcon.MessageType.INFO);
        } else {
            JOptionPane.showMessageDialog(window, text, "Timesetter Alpha",
                    JOptionPane.INFORMATION_MESSAGE);
        }
        Thread soundThread = new Thread(this::playSound, "Timesetter-Notification-Audio");
        soundThread.setDaemon(true);
        soundThread.start();
    }

    private void playSound() {
        URL sound = NotificationService.class.getResource("/sounds/notification.wav");
        if (sound == null) {
            Toolkit.getDefaultToolkit().beep();
            return;
        }
        try (AudioInputStream stream = AudioSystem.getAudioInputStream(sound)) {
            Clip clip = AudioSystem.getClip();
            clip.addLineListener(event -> {
                if (event.getType() == LineEvent.Type.STOP) clip.close();
            });
            clip.open(stream);
            clip.start();
        } catch (Exception ex) {
            LOG.log(Level.WARNING, "Cannot play notification", ex);
            Toolkit.getDefaultToolkit().beep();
        }
    }

    @Override
    public void close() {
        if (icon != null && SystemTray.isSupported()) {
            SystemTray.getSystemTray().remove(icon);
            icon = null;
        }
    }
}
