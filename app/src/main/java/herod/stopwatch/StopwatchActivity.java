package herod.stopwatch;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Created by Matthew Herod on 16/03/15.
 */
public class StopwatchActivity extends ActionBarActivity implements Runnable {

    private final String TAG = StopwatchActivity.class.getSimpleName();

    // Front end should get time and carry on counting front end

    private boolean stopwatchActive = false;

    private StopwatchService mStopwatchService;

    private Toolbar mToolbar;

    private TextView mStopwatchTextView;

    private ImageView mPlayImageButton; // Used for both start and pause actions
    private ImageView mResetImageButton; // Used for both lap and reset actions

    private RecyclerView mLapRecyclerView;

    private StopwatchLapAdapter mStopwatchLapAdapter;

    private Thread mStopwatchThread;

    private boolean requestThreadStop = false;

    private String currentTimerText = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timer);

        mStopwatchThread = new Thread(this);

        mToolbar = (Toolbar) findViewById(R.id.toolbar_main);
        setSupportActionBar(mToolbar); // Use support action bar to support backwards of API 21

        mStopwatchTextView = (TextView) findViewById(R.id.timer_text);

        mPlayImageButton = (ImageButton) findViewById(R.id.imagebutton_play);
        mResetImageButton = (ImageButton) findViewById(R.id.imagebutton_reset);

        mPlayImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getStopwatch().toggle();
                syncComponents(); // Sync components after toggle stopwatch
            }
        });
        mResetImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Stopwatch stopwatch = getStopwatch();
                if (stopwatch.isActive(false)) {
                    stopwatch.lap();
                } else {
                    stopwatch.reset();
                }
                syncComponents(); // Sync components after lap/reset stopwatch
                syncLapRecyclerView(); // Sync lap records after lap/reset
            }
        });

        mLapRecyclerView = (RecyclerView) findViewById(R.id.timer_lap_recycler);

        mLapRecyclerView.setHasFixedSize(true);
        mLapRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        mStopwatchLapAdapter = new StopwatchLapAdapter();
        mLapRecyclerView.setAdapter(mStopwatchLapAdapter);
    }

    @Override
    protected void onResume() {
        super.onResume();

        try { // Start the background activity thread
            mStopwatchThread.start();
        } catch (IllegalThreadStateException iste) {
            // Thrown gracefully if the thread has already started
        }

        startService(new Intent(this, StopwatchService.class)); // Create the service
        // Without this Android will kill the service after unbinding, where we would like
        // to keep it in case we're running an ongoing timer

        bindService( // Bind to the service
                new Intent(this, StopwatchService.class),
                mServiceConnection,
                Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onPause() {
        if (mServiceConnection != null) {
            unbindService(mServiceConnection);
            // Unbind when the activity is paused as connection is not necessary and
            // the service will be notified that a client is not bound and so will
            // trigger the ongoing mode.
        }
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        requestThreadStop = true;
        // Tell the thread to stop to quit it gracefully and
        // ensure it doesn't run away
        super.onDestroy();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // No menu actions are necessary
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // No menu actions are implemented
        return super.onOptionsItemSelected(item);
    }

    /**
     * Update the stopwatchTextView with the current timer value. This runnable must be handled
     * on the same thread as the UI
     */
    final Runnable updateStopwatch = new Runnable() {
        public void run() {
            mStopwatchTextView.setText(currentTimerText);
        }
    };

    /**
     * Background thread operation for the activity. Handles the updating of the stopwatch time.
     */
    @Override
    public void run() {
        while (!requestThreadStop) {
            try {
                Thread.sleep(20);
            } catch (InterruptedException exception) {

            }
            long currentTime;
            try {
                currentTime = getStopwatch().getCurrentTime();
            } catch (NullPointerException npe) {
                continue;
            }
            currentTimerText = Stopwatch.formatElapsedTime(currentTime, true);
            runOnUiThread(updateStopwatch);
        }
    }

    /**
     * Get a reference to the service stopwatch. This should never be stored globally and only
     * used at local scope to mitigate possibility of a leak. The service will always be available
     * whilst the activity is bound to it so unless for another reason the service is stopped
     * (in which case we'd have bigger problems) this method will perform correctly.
     *
     * @return A reference to the service stopwatch
     */
    public Stopwatch getStopwatch() {
        return mStopwatchService.getStopwatch();
    }

    public void syncComponents() {
        if (getStopwatch().isActive(false)) {
            mPlayImageButton.setImageDrawable(getResources().getDrawable(R.drawable.ic_pause_light));
            mPlayImageButton.setContentDescription(getString(R.string.action_pause));

            mResetImageButton.setImageDrawable(getResources().getDrawable(R.drawable.ic_add_light));
            mResetImageButton.setContentDescription(getString(R.string.action_lap));
        } else {
            mPlayImageButton.setImageDrawable(getResources().getDrawable(R.drawable.ic_play_light));
            mPlayImageButton.setContentDescription(getString(R.string.action_play));

            mResetImageButton.setImageDrawable(getResources().getDrawable(R.drawable.ic_reset_light));
            mResetImageButton.setContentDescription(getString(R.string.action_reset));
        }
    }

    public void syncLapRecyclerView() {
        mStopwatchLapAdapter.setLapTimes(getStopwatch().getLapTimes());
    }

    public final ServiceConnection mServiceConnection = new ServiceConnection() {

        public void onServiceConnected(ComponentName className, IBinder binder) {
            Log.d(TAG, "Connected to StopwatchService");
            mStopwatchService = ((StopwatchService.ServiceBinder) binder).getService();
            mStopwatchService.notifyClientAttached();

            // On connect synchronise the components to correctly represent the current stopwatch
            // information
            syncComponents();
            syncLapRecyclerView();
        }

        public void onServiceDisconnected(ComponentName className) {
            Log.d(TAG, "Disconnected from Stopwatch Service");
            mStopwatchService = null;
        }
    };
}
