package herod.stopwatch;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

/**
 * Created by Matthew Herod on 16/03/15.
 */
public class StopwatchService extends Service {
    public StopwatchService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
