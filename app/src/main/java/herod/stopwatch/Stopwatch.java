package herod.stopwatch;

import android.os.SystemClock;
import android.util.Log;

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

        Log.d("Stopwatch", "New stopwatch instance");
    }

    public boolean isActive() {
        return active;
    }

    public void reset() {
        totalTime = 0;
        startTime = 0;
        active = false;
    }

    public void start() {
        active = true;
        startTime = SystemClock.elapsedRealtime();
        // TODO : consider changes to the system time mid use
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

    public static String formatElapsedTime(long time) {
        int extra = (int) (time % 1000) / 10;
        int secs = (int) (time / 1000);
        int mins = secs / 60;
        int hours = (int) (mins / 60);
        return formatTimePart(hours) + ":" +
                formatTimePart(mins) + ":" +
                formatTimePart(secs % 60) + "." +
                formatTimePart(extra);
    }

    private static String formatTimePart(int i) {
        if (i < 10) {
            return "0" + i;
        }
        return String.valueOf(i);
    }

}
