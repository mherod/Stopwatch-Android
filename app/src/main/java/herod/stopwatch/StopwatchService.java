package herod.stopwatch;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
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

    private final String CMD = "cmd";
    private final String CMD_TOGGLE = "toggle";
    private final String CMD_RESET = "reset";

    private ServiceBroadcastReceiver mServiceBroadcastReceiver;

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
        Log.d(TAG, "onCreate");

        mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        mServiceBroadcastReceiver = new ServiceBroadcastReceiver();

        mStopwatchOngoingThread = new Thread(this);
        mStopwatchOngoingThread.setName("stopwatchOngoingThread");

        mStopwatch = new Stopwatch();

        mNotificationBuilder = createNotification(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(CMD_TOGGLE);
        intentFilter.addAction(CMD_RESET);
        registerReceiver(mServiceBroadcastReceiver, intentFilter);

        try {
            mStopwatchOngoingThread.start();
        } catch (IllegalThreadStateException iste) {
            // Thrown gracefully if the thread has already started
        }

        // When started via intent (not bound) we want to run the service until
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
        Log.d(TAG, "New client bound");

        clientAttached = true;

        return mServiceBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.d(TAG, "All clients unbound");

        clientAttached = false;

        serviceActive = activateOngoingStopwatch();

        // mStopwatch = null; // null this instances reference to prevent leaks and radical threads
        return true;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy");

        unregisterReceiver(mServiceBroadcastReceiver);
    }

    public NotificationCompat.Builder createNotification(Context context) {

        final PendingIntent resetPendingIntent = PendingIntent.getBroadcast(
                this, 0, new Intent(CMD_RESET), PendingIntent.FLAG_UPDATE_CURRENT);

        int lapCount = getStopwatch().getLapTimes().size();

        final Bitmap picture = BitmapFactory.decodeResource(getResources(), R.drawable.ic_stopwatch_light);

        final NotificationCompat.Builder builder = new NotificationCompat.Builder(context)
                .setSmallIcon(R.drawable.ic_stopwatch_light)
                .setContentTitle(getString(R.string.app_name))
                .setContentText(getString(R.string.app_name))
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setLargeIcon(picture)
                .setAutoCancel(true)
                .setTicker(null)
                .setNumber(lapCount)
                .setShowWhen(false)
                .setContentIntent(
                        PendingIntent.getActivity(context, 0,
                                new Intent(context, StopwatchActivity.class),
                                PendingIntent.FLAG_UPDATE_CURRENT))
                .addAction(
                        R.drawable.ic_reset_light,
                        getString(R.string.action_reset),
                        resetPendingIntent
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
        return mStopwatch != null && mStopwatch.isActive(true) && clientAttached == false;
    }

    public class ServiceBinder extends Binder {
        StopwatchService getService() {
            return StopwatchService.this;
        }
    }

    public class ServiceBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "onReceive");

            String action = intent.getAction();

            if (action == null) {
                return;
            } else if (action.equals(CMD_RESET)) {
                mStopwatch.reset();
            }

        }
    }

}
