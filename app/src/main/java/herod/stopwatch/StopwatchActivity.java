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
import android.view.MotionEvent;
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
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        mStopwatchTextView = (TextView) findViewById(R.id.timer_text);

        mPlayImageButton = (ImageButton) findViewById(R.id.imagebutton_play);
        mResetImageButton = (ImageButton) findViewById(R.id.imagebutton_reset);

        mPlayImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                getStopwatch().toggle();
                syncComponents();
            }
        });
        mResetImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                getStopwatch().reset();
                syncComponents();
            }
        });

        mLapRecyclerView = (RecyclerView) findViewById(R.id.timer_lap_recycler);

        mLapRecyclerView.setHasFixedSize(true);
        mLapRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        mStopwatchLapAdapter = new StopwatchLapAdapter();

        mLapRecyclerView.setAdapter(mStopwatchLapAdapter);
        mLapRecyclerView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                Log.d("", "TOUCH ---  " + motionEvent.getActionMasked());
                return false;
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        try {
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
        }
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        requestThreadStop = true;

        super.onDestroy();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        // getMenuInflater().inflate(R.menu.menu_timer, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        // if (id == R.id.action_settings) {
        //    return true;
        // }

        return super.onOptionsItemSelected(item);
    }

    final Runnable updateStopwatch = new Runnable() {
        public void run() {
            mStopwatchTextView.setText(currentTimerText);
        }
    };

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

    public Stopwatch getStopwatch() {
        return mStopwatchService.getStopwatch();
    }

    public void syncComponents() {
        Stopwatch stopwatch = getStopwatch();

        if (stopwatch.isActive(false)) {
            mPlayImageButton.setImageDrawable(getResources().getDrawable(R.drawable.ic_pause_light));
        } else {
            mPlayImageButton.setImageDrawable(getResources().getDrawable(R.drawable.ic_play_light));
        }

        // mStopwatchLapAdapter.

    }

    public final ServiceConnection mServiceConnection = new ServiceConnection() {

        public void onServiceConnected(ComponentName className, IBinder binder) {
            Log.d(TAG, "Connected to StopwatchService");
            mStopwatchService = ((StopwatchService.ServiceBinder) binder).getService();
            mStopwatchService.notifyClientAttached();

            syncComponents();
        }

        public void onServiceDisconnected(ComponentName className) {
            Log.d(TAG, "Disconnected from Stopwatch Service");
            mStopwatchService = null;
        }
    };
}
