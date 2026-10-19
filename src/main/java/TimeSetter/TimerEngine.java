package TimeSetter;

public final class TimerEngine {
    private long durationMillis;
    private long remainingMillis;
    private long deadlineNanos;
    private int repeatCount;
    private int repeatsLeft;
    private boolean loop;
    private boolean running;
    private boolean paused;

    public TimerEngine() {}

    public long getDurationMillis() { return durationMillis; }
    public long getRemainingMillis() { return running ? readRemaining() : remainingMillis; }
    public boolean isRunning() { return running; }
    public boolean isPaused() { return paused; }
    public int getRepeatCount() { return repeatCount; }
    public int getRepeatsLeft() { return repeatsLeft; }
    public boolean isLoop() { return loop; }

    private long readRemaining() {
        long nanosLeft = deadlineNanos - System.nanoTime();
        return nanosLeft <= 0 ? 0 : (nanosLeft + 999_999L) / 1_000_000L;
    }

    public void configure(long duration, int repeats, boolean shouldLoop) {
        if (duration < 0 || duration > TimerFormatter.MAX_MILLIS || repeats < 0 || repeats > 100) {
            throw new IllegalArgumentException("Invalid timer settings");
        }
        running = false;
        paused = false;
        durationMillis = duration;
        remainingMillis = duration;
        repeatCount = repeats;
        repeatsLeft = repeats;
        loop = shouldLoop;
    }

    public void setRepeatCount(int count) {
        if (count < 0 || count > 100) throw new IllegalArgumentException("Repeat must be 0-100");
        repeatCount = count;
        if (!running && !paused) repeatsLeft = count;
    }

    public void setLoop(boolean value) { loop = value; }

    public void setDuration(long duration) {
        if (duration < 0 || duration > TimerFormatter.MAX_MILLIS) {
            throw new IllegalArgumentException("Duration out of range");
        }
        running = false;
        paused = false;
        durationMillis = duration;
        remainingMillis = duration;
        repeatsLeft = repeatCount;
    }

    public void adjustMillis(long adjustment) {
        long current = getRemainingMillis();
        long newValue = Math.max(0, Math.min(TimerFormatter.MAX_MILLIS, current + adjustment));
        long difference = newValue - current;
        remainingMillis = newValue;
        durationMillis = Math.max(0,
                Math.min(TimerFormatter.MAX_MILLIS, durationMillis + difference));
        if (running) {
            if (newValue == 0) {
                running = false;
                paused = false;
            } else {
                deadlineNanos = System.nanoTime() + newValue * 1_000_000L;
            }
        }
    }

    public boolean start() {
        if (running) return true;
        if (remainingMillis == 0) remainingMillis = durationMillis;
        if (remainingMillis == 0) return false;
        if (!paused) repeatsLeft = repeatCount;
        paused = false;
        running = true;
        deadlineNanos = System.nanoTime() + remainingMillis * 1_000_000L;
        return true;
    }

    public void pause() {
        if (!running) return;
        remainingMillis = readRemaining();
        running = false;
        paused = remainingMillis > 0;
    }

    public void stop() {
        running = false;
        paused = false;
        remainingMillis = 0;
        repeatsLeft = repeatCount;
    }

    public boolean restart() {
        running = false;
        paused = false;
        remainingMillis = durationMillis;
        repeatsLeft = repeatCount;
        return start();
    }

    // Returns true exactly once when a countdown cycle finishes.
    public boolean tick() {
        if (!running) return false;
        if (System.nanoTime() < deadlineNanos) {
            remainingMillis = readRemaining();
            return false;
        }
        remainingMillis = 0;
        running = false;
        paused = false;
        if (durationMillis > 0 && (loop || repeatsLeft > 0)) {
            if (!loop) repeatsLeft--;
            remainingMillis = durationMillis;
            running = true;
            deadlineNanos = System.nanoTime() + durationMillis * 1_000_000L;
        }
        return true;
    }
}
