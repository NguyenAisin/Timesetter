package TimeSetter;

public final class TimerFormatter {
    public static final long DAY = 86_400_000L;
    public static final long HOUR = 3_600_000L;
    public static final long MINUTE = 60_000L;
    public static final long SECOND = 1_000L;
    public static final long MAX_MILLIS = 99L * DAY + 23L * HOUR
            + 59L * MINUTE + 59L * SECOND + 999L;

    private TimerFormatter() {}

    public static long[] split(long millis) {
        if (millis < 0 || millis > MAX_MILLIS) {
            throw new IllegalArgumentException("Time out of range");
        }
        long days = millis / DAY;
        millis %= DAY;
        long hours = millis / HOUR;
        millis %= HOUR;
        long minutes = millis / MINUTE;
        millis %= MINUTE;
        long seconds = millis / SECOND;
        long ms = millis % SECOND;
        return new long[] {days, hours, minutes, seconds, ms};
    }

    public static String format(long millis) {
        long[] t = split(millis);
        return String.format(java.util.Locale.ROOT, "%02d:%02d:%02d:%02d:%03d",
                t[0], t[1], t[2], t[3], t[4]);
    }

    public static long parse(String value) {
        if (value == null || !value.trim().matches("\\d{1,2}:\\d{1,2}:\\d{1,2}:\\d{1,2}:\\d{1,3}")) {
            throw new IllegalArgumentException("Use DD:HH:MM:SS:SSS");
        }
        String[] parts = value.trim().split(":");
        long[] t = new long[5];
        for (int i = 0; i < 5; i++) {
            t[i] = Long.parseLong(parts[i]);
        }
        if (t[0] > 99 || t[1] > 23 || t[2] > 59 || t[3] > 59 || t[4] > 999) {
            throw new IllegalArgumentException("Time value out of range");
        }
        return t[0] * DAY + t[1] * HOUR + t[2] * MINUTE
                + t[3] * SECOND + t[4];
    }
}
