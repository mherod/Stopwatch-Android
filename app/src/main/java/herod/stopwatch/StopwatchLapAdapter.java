package herod.stopwatch;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class StopwatchLapAdapter extends RecyclerView.Adapter<StopwatchLapAdapter.ViewHolder> {

    private static final String TAG = StopwatchLapAdapter.class.getSimpleName();

    private Context mContext;

    private final List<Long> mLapTimes = new ArrayList<Long>();

    public StopwatchLapAdapter() {
        addLap(0, 102345, 383873);

    }

    public void addLap(long... lapTimes) {
        for (long lapTime : lapTimes) {
            mLapTimes.add(0, lapTime);
        }

        notifyDataSetChanged();
    }


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        mContext = viewGroup.getContext();

        View v = LayoutInflater.from(mContext)
                .inflate(R.layout.timer_lap_entry, viewGroup, false);

        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final ViewHolder vh, int i) {
        int lapNumber = mLapTimes.size() - i;
        String lapText = mContext.getString(R.string.action_lap) + " " +
                lapNumber + " - " +
                Stopwatch.formatElapsedTime(mLapTimes.get(i), true);
        vh.mLapTextView.setText(lapText);
    }

    @Override
    public int getItemCount() {
        return mLapTimes.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        protected final TextView mLapTextView;

        public ViewHolder(View v) {
            super(v);
            mLapTextView = (TextView) v.findViewById(R.id.textview_lap_timer);
        }
    }

}
