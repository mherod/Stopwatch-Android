package herod.stopwatch;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Created by Matthew Herod on 16/03/15.
 */
public class StopwatchActivity extends ActionBarActivity implements Runnable {

    // Front end should get time and carry on counting front end

    private Toolbar mToolbar;

    private TextView mStopwatchTextView;

    private Button mResetButton;
    private Button mStartButton;
    private Button mStopButton;

    private ImageView mPlayImageView;

    private Thread mStopwatchThread;

    private boolean requestThreadStop = false;

    int count = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timer);

        mStopwatchThread = new Thread(this);
        mStopwatchThread.start();

        // mToolbar = (Toolbar) findViewById(R.id.toolbar_actionbar);
        // setSupportActionBar(mToolbar);
        // getSupportActionBar().setDisplayShowHomeEnabled(true);

        mStopwatchTextView = (TextView) findViewById(R.id.timer_text);
        mResetButton = (Button) findViewById(R.id.button_reset);
        mStartButton = (Button) findViewById(R.id.button_start);
        mStopButton = (Button) findViewById(R.id.button_stop);

        mPlayImageView = (ImageView) findViewById(R.id.play_imgbtn);

        mPlayImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                mPlayImageView.setImageDrawable(getResources().getDrawable(R.drawable.ic_pause_light));
                mPlayImageView.setScaleType(ImageView.ScaleType.CENTER);
            }
        });

        mResetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                count = 0;
            }
        });
        mStartButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
            }
        });
        mStopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {

            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        // TODO : update counter from service
    }

    @Override
    protected void onStop() {
        super.onStop();

        requestThreadStop = true;
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_timer, menu);
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
            mStopwatchTextView.setText(count+ ":" + count);
        }
    };

    @Override
    public void run() {
        while (!requestThreadStop) {
            count = ++count % 60;
            try {
                Thread.sleep(100);
            } catch (Exception e) {

            }
            runOnUiThread(updateStopwatch);
        }
    }
}
