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

    public void reset() {
        totalTime = 0;
        startTime = 0;
        active = false;
    }

    public void start() {
        active = true;
        startTime = SystemClock.elapsedRealtime();
    }

    public void pause() {
        active = false;
        totalTime += getElapsedTime();
    }

    public void toggle() {
        if (active) {
            pause();
        } else {
            start();
        }
    }

    public void lap() {
        lapTimes.add(getElapsedTime());
    }

    public long getCurrentTime() {
        return active ? getElapsedTime() + totalTime : totalTime;
    }

    public List<Long> getLapTimes() { return lapTimes; }

    private long getElapsedTime() {
        return SystemClock.elapsedRealtime() - startTime;
    }

    public static String formatElapsedTime(long time, boolean ms) {
        int extra = (int) (time % 1000) / 10;
        int secs = (int) (time / 1000);
        int mins = secs / 60;
        int hours = (int) (mins / 60);
        return (hours > 0 ? formatTimePart(hours) + ":" : "") +
                formatTimePart(mins) + ":" +
                formatTimePart(secs % 60) +
                (ms ? "." + formatTimePart(extra) : "");
    }

    private static String formatTimePart(int i) {
        if (i < 10) {
            return "0" + i;
        }
        return String.valueOf(i);
    }

}
