package TimeSetter;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

public final class TimerStorage {
    private TimerStorage() {}

    public static void save(Path path, TimerConfig config) throws IOException {
        Properties props = new Properties();
        props.setProperty("durationMillis", Long.toString(config.getDurationMillis()));
        props.setProperty("repeatCount", Integer.toString(config.getRepeatCount()));
        props.setProperty("loop", Boolean.toString(config.isLoop()));
        props.setProperty("message", config.getMessage());
        try (Writer writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
            props.store(writer, "Timesetter configuration (UTF-8)");
        }
    }

    public static TimerConfig load(Path path) throws IOException {
        Properties props = new Properties();
        try (Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            props.load(reader);
        }
        try {
            String duration = props.getProperty("durationMillis");
            String repeats = props.getProperty("repeatCount", "0");
            String loop = props.getProperty("loop", "false");
            if (duration == null || !loop.equals("true") && !loop.equals("false")) {
                throw new IllegalArgumentException("Invalid configuration");
            }
            return new TimerConfig(Long.parseLong(duration), Integer.parseInt(repeats),
                    Boolean.parseBoolean(loop), props.getProperty("message", ""));
        } catch (IllegalArgumentException ex) {
            throw new IOException("Invalid Timesetter file", ex);
        }
    }
}
