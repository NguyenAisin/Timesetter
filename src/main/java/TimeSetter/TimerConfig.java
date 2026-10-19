package TimeSetter;

public final class TimerConfig {
    private final long durationMillis;
    private final int repeatCount;
    private final boolean loop;
    private final String message;

    public TimerConfig(long durationMillis, int repeatCount, boolean loop, String message) {
        if (durationMillis < 0 || durationMillis > TimerFormatter.MAX_MILLIS) {
            throw new IllegalArgumentException("Duration out of range");
        }
        if (repeatCount < 0 || repeatCount > 100) {
            throw new IllegalArgumentException("Repeat must be between 0 and 100");
        }
        this.durationMillis = durationMillis;
        this.repeatCount = repeatCount;
        this.loop = loop;
        this.message = message == null ? "" : message;
    }

    public long getDurationMillis() { return durationMillis; }
    public int getRepeatCount() { return repeatCount; }
    public boolean isLoop() { return loop; }
    public String getMessage() { return message; }
    public String getNotificationMessage() {
        return message.isBlank() ? "Timeup" : message.trim();
    }
}
