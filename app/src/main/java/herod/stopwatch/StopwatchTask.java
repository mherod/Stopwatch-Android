package herod.stopwatch;

/**
 * Created by Matthew Herod on 17/03/15.
 */
public class StopwatchTask {

    private long totalTime, startTime;

    public StopwatchTask() {
        totalTime = 0;
        startTime = 0;
    }

    public void start() {
        startTime = System.currentTimeMillis();
        // TODO : consider changes to the system time mid use
    }

    private void pause() {
        totalTime += System.currentTimeMillis() - startTime;
    }

    @Deprecated
    public void stop() {
        // stopTime = System.currentTimeMillis();
    }

    public long getCurrentTime() {
        return totalTime;
    }

}
