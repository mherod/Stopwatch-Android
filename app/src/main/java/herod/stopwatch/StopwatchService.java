package herod.stopwatch;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Binder;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

/**
 * Created by Matthew Herod on 16/03/15.
 */
public class StopwatchService extends Service implements Runnable {

    private final String TAG = StopwatchService.class.getSimpleName();

    private final IBinder mServiceBinder = new ServiceBinder();

    private Thread mStopwatchOngoingThread;

    private final int NOTIFICATION_ID = 1;

    private NotificationManager mNotificationManager;
    private NotificationCompat.Builder mNotificationBuilder;

    private Stopwatch mStopwatch;

    private boolean serviceActive = true;
    private boolean clientAttached = false;

    public StopwatchService() {

    }

    @Override
    public void onCreate() {

        mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        mStopwatchOngoingThread = new Thread(this);
        mStopwatchOngoingThread.setName("stopwatchOngoingThread");

        mStopwatch = new Stopwatch();

        mNotificationBuilder = createNotification(this);

        Log.d(TAG, "Service created");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        try {
            mStopwatchOngoingThread.start();
        } catch (IllegalThreadStateException iste) {
            // Thrown gracefully if the thread has already started
        }

        // If started via intent (not bound) we want to run the service until
        // explicitly stopped

        Log.d(TAG, "Service started");
        return START_STICKY;
    }

    public Stopwatch getStopwatch() {
        return mStopwatch;
    }

    /**
     * Used to explicitly tell the service that a client is attached (which will cause ongoing
     * background process to stop)
     */
    public void notifyClientAttached() {
        clientAttached = true;
    }

    @Override
    public IBinder onBind(Intent intent) {
        clientAttached = true;

        Log.d(TAG, "New client bound");

        return mServiceBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        clientAttached = false;

        Log.d(TAG, "All clients unbound");

        // All clients are unbound from the service
        // We need to start the service in the foreground if our stopwatch is still active
        // otherwise we're all done and the service should quit
        if (mStopwatch.isActive()) {

        } else {

            serviceActive = false;
        }

        // mStopwatch = null; // null this instances reference to prevent leaks and radical threads
        return true;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        Log.d(TAG, "onDestroy");
    }

    public NotificationCompat.Builder createNotification(Context context) {
        final Resources res = context.getResources();
        final Bitmap picture = BitmapFactory.decodeResource(res, R.drawable.ic_stopwatch_light);

        final NotificationCompat.Builder builder = new NotificationCompat.Builder(context)
                .setSmallIcon(R.drawable.ic_stopwatch_light)
                .setContentTitle(res.getString(R.string.app_name))
                .setContentText(res.getString(R.string.app_name))
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setLargeIcon(picture)
                .setAutoCancel(true)
                .setTicker(null)
                // .setNumber(1) // TODO: number of active timers or saved lap
                .setShowWhen(false)
                .setContentIntent(
                        PendingIntent.getActivity(context, 0,
                                new Intent(context, StopwatchActivity.class),
                                PendingIntent.FLAG_UPDATE_CURRENT))
                .addAction(
                        R.drawable.ic_pause_light,
                        res.getString(R.string.action_pause),
                        PendingIntent.getActivity(context, 0,
                                new Intent(context, StopwatchActivity.class),
                                PendingIntent.FLAG_UPDATE_CURRENT)
                )
                .addAction(
                        R.drawable.ic_reset_light,
                        res.getString(R.string.action_reset),
                        PendingIntent.getActivity(context, 0,
                                new Intent(context, StopwatchActivity.class),
                                PendingIntent.FLAG_UPDATE_CURRENT)
                );

        return builder;
    }

    @Override
    public void run() {
        Log.d(mStopwatchOngoingThread.getName(), "Starting thread");

        while (serviceActive) {

            if (activateOngoingStopwatch()) {

                Notification notification = mNotificationBuilder
                        .setOngoing(true)
                        .build();

                mNotificationManager.notify(NOTIFICATION_ID, notification);

                startForeground(NOTIFICATION_ID, notification);

                 do {

                    String timerText = "";
                    try {
                        timerText = Stopwatch.formatElapsedTime(mStopwatch.getCurrentTime(), false);
                    } catch (NullPointerException npe) {
                        // the unlikely event that mStopwatch becomes null straight after entry condition
                    }

                     notification = mNotificationBuilder
                             .setContentText(timerText)
                             .build();
                    mNotificationManager.notify(NOTIFICATION_ID, notification);

                    try { // Throttle notification updates to be friendly to Android
                        Thread.sleep(500);
                    } catch (InterruptedException ie) {
                        continue;
                    }
                } while (activateOngoingStopwatch());

                stopForeground(true);

            }

            try {
                Thread.sleep(100);
            } catch (InterruptedException ie) {
                continue;
            }

        }

        Log.d(mStopwatchOngoingThread.getName(), "Service stopping");

        stopSelf();

        // Shouldn't be necessary
        // mNotificationManager.cancel(NOTIFICATION_ID);
        // mNotificationManager.cancelAll();

    }

    private boolean activateOngoingStopwatch() {
        return mStopwatch != null && mStopwatch.isActive() && clientAttached == false;
    }

    public class ServiceBinder extends Binder {
        StopwatchService getService() {
            return StopwatchService.this;
        }
    }

}
