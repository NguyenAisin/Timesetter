package TimeSetter;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.JToggleButton;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.Timer;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;

public class frm_Main extends JFrame {
    private static final Color PAGE = new Color(245, 248, 253);
    private static final Color WHITE = Color.WHITE;
    private static final Color TEXT = new Color(25, 39, 64);
    private static final Color MUTED = new Color(104, 120, 144);
    private static final Color BLUE = new Color(36, 112, 226);
    private static final Color BLUE_PALE = new Color(232, 241, 255);
    private static final Color BORDER = new Color(222, 230, 242);
    private static final Font FONT = new Font("Segoe UI", Font.PLAIN, 14);
    private static final Font BOLD = new Font("Segoe UI", Font.BOLD, 14);

    private final TimerEngine engine = new TimerEngine();
    private final JLabel[] timeLabels = new JLabel[5];
    private final JTextArea messageField = new JTextArea(4, 32);
    private final JSpinner repeatInput = new JSpinner(new SpinnerNumberModel(0, 0, 100, 1));
    private final JToggleButton loopInput = new JToggleButton("Loop OFF");
    private final JLabel stateLabel = new JLabel("Ready to start");
    private JButton startButton;
    private JButton pauseButton;
    private NotificationService notifications;
    private Timer uiTimer;

    public frm_Main() {
        setTitle("Timesetter Alpha 1.2");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(950, 770);
        setMinimumSize(new Dimension(780, 680));
        setLocationRelativeTo(null);
        setContentPane(buildInterface());
        notifications = new NotificationService(this);
        uiTimer = new Timer(20, e -> {
            boolean completed = engine.tick();
            refreshTimeDisplay();
            if (completed) {
                notifications.notifyFinished(this, getMessage());
                stateLabel.setText(engine.isRunning() ? "Cycle finished — repeating..." : "Timeup");
            }
            refreshButtons();
        });
        uiTimer.start();
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                uiTimer.stop();
                notifications.close();
            }
        });
        engine.configure(0, 0, false);
        refreshTimeDisplay();
        refreshButtons();
    }

    private JPanel buildInterface() {
        JPanel page = new JPanel(new BorderLayout(0, 16));
        page.setBackground(PAGE);
        page.setBorder(new EmptyBorder(24, 28, 24, 28));

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        JLabel title = new JLabel("◷  Timesetter Alpha 1.2");
        title.setForeground(TEXT);
        title.setFont(new Font("Segoe UI", Font.BOLD, 27));
        header.add(title, BorderLayout.WEST);
        JLabel detail = new JLabel("Personal countdown timer");
        detail.setFont(FONT);
        detail.setForeground(MUTED);
        header.add(detail, BorderLayout.EAST);
        page.add(header, BorderLayout.NORTH);

        JPanel center = new JPanel(new GridBagLayout());
        center.setOpaque(false);
        GridBagConstraints section = new GridBagConstraints();
        section.gridx = 0;
        section.weightx = 1;
        section.fill = GridBagConstraints.HORIZONTAL;
        section.gridy = 0;
        section.insets = new Insets(0, 0, 14, 0);
        center.add(makeClockCard(), section);
        section.gridy = 1;
        center.add(makeActions(), section);
        section.gridy = 2;
        center.add(makeSettings(), section);
        section.gridy = 3;
        section.insets = new Insets(0, 0, 0, 0);
        center.add(makeMessage(), section);
        page.add(center, BorderLayout.CENTER);

        JPanel footer = new JPanel(new BorderLayout());
        footer.setOpaque(false);
        stateLabel.setFont(FONT);
        stateLabel.setForeground(MUTED);
        footer.add(stateLabel, BorderLayout.WEST);
        JLabel tip = new JLabel("DD : HH : MM : SS : MS");
        tip.setForeground(MUTED);
        tip.setFont(FONT);
        footer.add(tip, BorderLayout.EAST);
        page.add(footer, BorderLayout.SOUTH);
        return page;
    }

    private JPanel makeClockCard() {
        JPanel card = cardPanel();
        card.setLayout(new BorderLayout(0, 12));
        JLabel heading = new JLabel("TIME REMAINING");
        heading.setForeground(MUTED);
        heading.setFont(new Font("Segoe UI", Font.BOLD, 12));
        card.add(heading, BorderLayout.NORTH);

        JPanel digits = new JPanel(new GridLayout(1, 5, 9, 0));
        digits.setOpaque(false);
        String[] names = {"DAYS", "HOURS", "MINUTES", "SECONDS", "MILLISECONDS"};
        long[] steps = {TimerFormatter.DAY, TimerFormatter.HOUR, TimerFormatter.MINUTE,
                TimerFormatter.SECOND, 1L};
        for (int i = 0; i < 5; i++) {
            digits.add(createUnitPanel(names[i], steps[i], i));
        }
        card.add(digits, BorderLayout.CENTER);
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 190));
        card.setPreferredSize(new Dimension(890, 185));
        return card;
    }

    private JPanel createUnitPanel(String name, long step, int index) {
        JPanel panel = new JPanel(new BorderLayout(0, 4));
        panel.setBackground(BLUE_PALE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER, 1, true),
                new EmptyBorder(6, 5, 6, 5)));
        JLabel title = new JLabel(name, SwingConstants.CENTER);
        title.setForeground(MUTED);
        title.setFont(new Font("Segoe UI", Font.BOLD, 10));
        panel.add(title, BorderLayout.NORTH);

        JLabel digit = new JLabel("00", SwingConstants.CENTER);
        digit.setForeground(TEXT);
        digit.setFont(new Font("Consolas", Font.BOLD, index == 4 ? 28 : 32));
        timeLabels[index] = digit;
        panel.add(digit, BorderLayout.CENTER);

        JPanel adjust = new JPanel(new GridLayout(1, 2, 5, 0));
        adjust.setOpaque(false);
        JButton minus = button("−", false);
        JButton plus = button("+", false);
        minus.setMargin(new Insets(1, 2, 1, 2));
        plus.setMargin(new Insets(1, 2, 1, 2));
        minus.addActionListener(e -> adjustTime(-step));
        plus.addActionListener(e -> adjustTime(step));
        adjust.add(minus);
        adjust.add(plus);
        panel.add(adjust, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel makeActions() {
        JPanel actions = new JPanel(new GridLayout(1, 5, 10, 0));
        actions.setOpaque(false);
        actions.setMaximumSize(new Dimension(Integer.MAX_VALUE, 48));
        actions.setPreferredSize(new Dimension(890, 48));
        JButton set = button("Set time", false);
        startButton = button("Start", true); startButton.setForeground(Color.BLACK);
        pauseButton = button("Pause", false);
        JButton stop = button("Stop", false);
        JButton restart = button("Restart", false);
        set.addActionListener(e -> setTime());
        startButton.addActionListener(e -> start());
        pauseButton.addActionListener(e -> pause());
        stop.addActionListener(e -> {
            engine.stop();
            stateLabel.setText("Stopped");
            refresh();
        });
        restart.addActionListener(e -> {
            if (engine.restart()) {
                stateLabel.setText("Counting down...");
            }
            refresh();
        });
        actions.add(set);
        actions.add(startButton);
        actions.add(pauseButton);
        actions.add(stop);
        actions.add(restart);
        return actions;
    }

    private JPanel makeSettings() {
        JPanel row = new JPanel(new GridLayout(1, 2, 14, 0));
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 168));
        row.setPreferredSize(new Dimension(890, 168));

        JPanel presets = cardPanel();
        presets.setLayout(new BorderLayout(0, 12));
        presets.add(sectionTitle("Quick Add Presets"), BorderLayout.NORTH);
        JPanel grid = new JPanel(new GridLayout(2, 3, 8, 8));
        grid.setOpaque(false);
        addPreset(grid, "1 Min", 60_000);
        addPreset(grid, "2 Min", 120_000);
        addPreset(grid, "5 Min", 300_000);
        addPreset(grid, "10 Min", 600_000);
        addPreset(grid, "30 Min", 1_800_000);
        addPreset(grid, "1 Hour", 3_600_000);
        presets.add(grid, BorderLayout.CENTER);
        row.add(presets);

        JPanel repeat = cardPanel();
        repeat.setLayout(new BorderLayout(0, 10));
        repeat.add(sectionTitle("Repeat Settings"), BorderLayout.NORTH);
        JPanel controls = new JPanel(new FlowLayout(FlowLayout.LEFT, 9, 8));
        controls.setOpaque(false);
        JLabel quantity = new JLabel("Repeat extra times:");
        quantity.setFont(FONT);
        quantity.setForeground(TEXT);
        controls.add(quantity);
        repeatInput.setPreferredSize(new Dimension(66, 30));
        repeatInput.setFont(BOLD);
        controls.add(repeatInput);
        JButton apply = button("Repeat", false);
        apply.addActionListener(e -> applyRepeat());
        controls.add(apply);
        repeat.add(controls, BorderLayout.CENTER);
        JPanel loopRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 9, 0));
        loopRow.setOpaque(false);
        loopInput.setFont(BOLD);
        loopInput.addActionListener(e -> {
            engine.setLoop(loopInput.isSelected());
            loopInput.setText(loopInput.isSelected() ? "Loop ON" : "Loop OFF");
        });
        loopRow.add(loopInput);
        JLabel hint = new JLabel("Repeat 0 = run once");
        hint.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        hint.setForeground(MUTED);
        loopRow.add(hint);
        repeat.add(loopRow, BorderLayout.SOUTH);
        row.add(repeat);
        return row;
    }

    private JPanel makeMessage() {
        JPanel panel = cardPanel();
        panel.setLayout(new BorderLayout(12, 10));
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 180));
        panel.setPreferredSize(new Dimension(890, 180));
        JPanel heading = new JPanel(new BorderLayout());
        heading.setOpaque(false);
        heading.add(sectionTitle("Notification Message"), BorderLayout.WEST);
        JLabel note = new JLabel("Empty message → Timeup");
        note.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        note.setForeground(MUTED);
        heading.add(note, BorderLayout.EAST);
        panel.add(heading, BorderLayout.NORTH);

        messageField.setFont(FONT);
        messageField.setLineWrap(true);
        messageField.setWrapStyleWord(true);
        messageField.setBorder(new EmptyBorder(8, 8, 8, 8));
        JScrollPane scroller = new JScrollPane(messageField);
        scroller.setBorder(BorderFactory.createLineBorder(BORDER));
        panel.add(scroller, BorderLayout.CENTER);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        actions.setOpaque(false);
        JButton preview = button("Set Message / Preview", false);
        JButton save = button("Save File", false);
        JButton open = button("Open File", false);
        preview.addActionListener(e -> JOptionPane.showMessageDialog(this,
                getMessage(), "Notification preview", JOptionPane.INFORMATION_MESSAGE));
        save.addActionListener(e -> saveFile());
        open.addActionListener(e -> openFile());
        actions.add(preview);
        actions.add(save);
        actions.add(open);
        panel.add(actions, BorderLayout.SOUTH);
        return panel;
    }

    private void addPreset(JPanel grid, String title, long value) {
        JButton btn = button(title, false);
        btn.setForeground(BLUE);
        btn.addActionListener(e -> adjustTime(value));
        grid.add(btn);
    }

    private static JPanel cardPanel() {
        JPanel panel = new JPanel();
        panel.setBackground(WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER, 1, true),
                new EmptyBorder(14, 16, 14, 16)));
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);
        return panel;
    }

    private static JLabel sectionTitle(String text) {
        JLabel title = new JLabel(text);
        title.setFont(new Font("Segoe UI", Font.BOLD, 17));
        title.setForeground(TEXT);
        return title;
    }

    private static JButton button(String text, boolean primary) {
        JButton result = new JButton(text);
        result.setFont(BOLD);
        result.setFocusPainted(false);
        result.setBackground(primary ? BLUE : WHITE);
        result.setForeground(primary ? WHITE : TEXT);
        result.setOpaque(true);
        result.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(primary ? BLUE : BORDER, 1, true),
                new EmptyBorder(7, 11, 7, 11)));
        result.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        return result;
    }

    private void applyRepeat() {
        engine.setRepeatCount((Integer) repeatInput.getValue());
        stateLabel.setText("Repeat setting saved");
    }

    private void adjustTime(long delta) {
        engine.adjustMillis(delta);
        stateLabel.setText(engine.isRunning() ? "Counting down..." : "Time adjusted");
        refresh();
    }

    private void setTime() {
        String input = JOptionPane.showInputDialog(this,
                "Enter DD:HH:MM:SS:SSS", TimerFormatter.format(engine.getRemainingMillis()));
        if (input == null) return;
        try {
            engine.setDuration(TimerFormatter.parse(input));
            stateLabel.setText("Time set");
            refresh();
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, "Invalid value. Example: 00:00:05:30:500",
                    "Invalid time", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void start() {
        if (!engine.start()) {
            JOptionPane.showMessageDialog(this, "Please set a duration first.");
        } else {
            stateLabel.setText("Counting down...");
        }
        refresh();
    }

    private void pause() {
        engine.pause();
        stateLabel.setText("Paused");
        refresh();
    }

    private String getMessage() {
        String value = messageField.getText().trim();
        return value.isEmpty() ? "Timeup" : value;
    }

    private void refreshTimeDisplay() {
        long[] t = TimerFormatter.split(engine.getRemainingMillis());
        for (int i = 0; i < 5; i++) {
            timeLabels[i].setText(String.format(java.util.Locale.ROOT,
                    i == 4 ? "%03d" : "%02d", t[i]));
        }
    }

    private void refreshButtons() {
        startButton.setEnabled(!engine.isRunning());
        pauseButton.setEnabled(engine.isRunning());
    }

    private void refresh() {
        refreshTimeDisplay();
        refreshButtons();
    }

    private TimerConfig collectConfig() {
        return new TimerConfig(engine.getDurationMillis(), (Integer) repeatInput.getValue(),
                loopInput.isSelected(), messageField.getText());
    }

    private void saveFile() {
        JFileChooser chooser = makeChooser();
        chooser.setSelectedFile(new File("timesetter.timer"));
        if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;
        File file = chooser.getSelectedFile();
        if (!file.getName().toLowerCase(java.util.Locale.ROOT).endsWith(".timer")) {
            file = new File(file.getParentFile(), file.getName() + ".timer");
        }
        if (file.exists() && JOptionPane.showConfirmDialog(this,
                "Overwrite " + file.getName() + "?", "Confirm",
                JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) return;
        try {
            TimerStorage.save(file.toPath(), collectConfig());
            stateLabel.setText("Saved: " + file.getName());
        } catch (IOException ex) {
            showFileError("Save failed", ex);
        }
    }

    private void openFile() {
        JFileChooser chooser = makeChooser();
        if (chooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) return;
        Path file = chooser.getSelectedFile().toPath();
        try {
            TimerConfig settings = TimerStorage.load(file);
            engine.configure(settings.getDurationMillis(), settings.getRepeatCount(), settings.isLoop());
            repeatInput.setValue(settings.getRepeatCount());
            loopInput.setSelected(settings.isLoop());
            loopInput.setText(settings.isLoop() ? "Loop ON" : "Loop OFF");
            messageField.setText(settings.getMessage());
            stateLabel.setText("Opened: " + file.getFileName());
            refresh();
        } catch (IOException ex) {
            showFileError("Open failed", ex);
        }
    }

    private static JFileChooser makeChooser() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter(new FileNameExtensionFilter("Timesetter Configuration (*.timer)", "timer"));
        return chooser;
    }

    private void showFileError(String header, IOException ex) {
        JOptionPane.showMessageDialog(this, header + ": " + ex.getMessage(),
                "File error", JOptionPane.ERROR_MESSAGE);
    }
}
