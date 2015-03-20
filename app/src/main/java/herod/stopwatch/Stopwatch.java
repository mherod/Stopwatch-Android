package herod.stopwatch;

import android.os.SystemClock;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Matthew Herod on 17/03/15.
 */
public class Stopwatch implements Serializable {

    private List<Long> lapTimes = new ArrayList<Long>();

    private boolean active = false;

    private long totalTime, startTime;

    public Stopwatch() {
        reset();
    }

    public boolean isActive(boolean orPaused) {
        return active || (orPaused && startTime > 0);
    }

    /**
     * Reset the stopwatch
     */
    public void reset() {
        totalTime = 0;
        startTime = 0;
        active = false;
        lapTimes.clear();
    }

    /**
     * Start the stopwatch
     */
    public void start() {
        active = true;
        startTime = SystemClock.elapsedRealtime();
    }

    /**
     * Pause the stopwatch
     */
    public void pause() {
        active = false;
        totalTime += getElapsedTime();
    }

    /**
     * Toggle between active and not active
     */
    public void toggle() {
        if (active) {
            pause();
        } else {
            start();
        }
    }

    /**
     * Record a new lap entry
     */
    public void lap() {
        lapTimes.add(getElapsedTime());
    }

    /**
     * @return The current time of the stopwatch as should be represented on any client displaying it
     */
    public long getCurrentTime() {
        return active ? getElapsedTime() + totalTime : totalTime;
    }

    /**
     * @return The List of lap time longs
     */
    public List<Long> getLapTimes() { return lapTimes; }

    /**
     * @return Elapsed time since the last stopwatch start
     */
    private long getElapsedTime() {
        return SystemClock.elapsedRealtime() - startTime;
    }

    /**
     * Format time appropriately for a ms time value
     * @param time a long representing a time in milliseconds
     * @param ms whether or not the time should include millisecond
     * @return
     */
    public static String formatElapsedTime(long time, boolean ms) {
        int extra = (int) (time % 1000) / 10; // 2 places
        int secs = (int) (time / 1000);
        int mins = secs / 60;
        int hours = (int) (mins / 60);
        return (hours > 0 ? formatTimePart(hours) + ":" : "") + // Only display hours when there are hours
                formatTimePart(mins) + ":" +
                formatTimePart(secs % 60) + // Mod 60 to leave correct seconds
                (ms ? "." + formatTimePart(extra) : "");
    }

    /**
     * Used to pad each part of the time so that zeros are where necessary
     * @param i
     * @return A padded time part
     */
    private static String formatTimePart(int i) {
        if (i < 10) {
            return "0" + i;
        }
        return String.valueOf(i);
    }

}
