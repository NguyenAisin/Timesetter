package TimeSetter;

import java.nio.file.Files;
import java.nio.file.Path;

public class TimerSelfTest {
    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }

    public static void main(String[] args) throws Exception {
        check(TimerFormatter.parse("00:00:05:30:500") == 330_500L, "parse format");
        check(TimerFormatter.format(330_500L).equals("00:00:05:30:500"), "format");
        check(TimerFormatter.parse("99:23:59:59:999") == TimerFormatter.MAX_MILLIS, "max");
        try {
            TimerFormatter.parse("00:30:90:01:999");
            throw new AssertionError("invalid value accepted");
        } catch (IllegalArgumentException expected) {}

        TimerConfig config = new TimerConfig(5_432, 3, true, "Đi học\nĐừng quên bài tập!");
        Path temp = Files.createTempFile("timesetter", ".timer");
        try {
            TimerStorage.save(temp, config);
            TimerConfig loaded = TimerStorage.load(temp);
            check(loaded.getDurationMillis() == 5_432L, "saved duration");
            check(loaded.getRepeatCount() == 3, "saved repeat");
            check(loaded.isLoop(), "saved loop");
            check(loaded.getMessage().equals(config.getMessage()), "saved multiline Unicode message");
        } finally {
            Files.deleteIfExists(temp);
        }

        TimerEngine e = new TimerEngine();
        e.configure(200, 1, false);
        check(e.start(), "starts");
        Thread.sleep(45);
        long beforePause = e.getRemainingMillis();
        check(beforePause > 0 && beforePause < 200, "remaining goes down");
        e.pause();
        long pausedAt = e.getRemainingMillis();
        Thread.sleep(40);
        check(e.getRemainingMillis() == pausedAt, "pause freezes");
        check(e.start(), "resume");
        Thread.sleep(210);
        check(e.tick(), "one cycle completion");
        check(e.isRunning(), "repeat runs");
        check(e.getRepeatsLeft() == 0, "repeat count used");
        Thread.sleep(210);
        check(e.tick(), "second cycle completion");
        check(!e.isRunning(), "repeat finishes");
        check(e.getRemainingMillis() == 0, "reaches zero");
        check(e.restart(), "restart");
        check(e.isRunning(), "restart running");
        e.stop();
        check(!e.isRunning(), "stopped");
        check(e.getRemainingMillis() == 0, "stopped display cleared");
        check(e.start(), "start after stop from duration");

        e.configure(80, 0, true);
        e.start();
        Thread.sleep(100);
        check(e.tick(), "loop first cycle");
        check(e.isRunning(), "loop running");
        e.setLoop(false);
        Thread.sleep(100);
        check(e.tick(), "loop last cycle");
        check(!e.isRunning(), "loop disabled finishes");

        e.configure(1_000, 0, false);
        e.adjustMillis(60_000);
        check(e.getRemainingMillis() == 61_000, "plus minute");
        e.adjustMillis(-1_000);
        check(e.getRemainingMillis() == 60_000, "minus second");

        System.out.println("OK: formatting, parse, config UTF-8, pause, repeat, loop, start/stop/restart, +/-");
    }
}
