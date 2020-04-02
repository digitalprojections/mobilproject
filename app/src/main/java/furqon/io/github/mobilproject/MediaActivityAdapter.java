package furqon.io.github.mobilproject;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;


public class MediaActivityAdapter extends RecyclerView.Adapter<MediaActivityAdapter.PlayListViewHolder> {

    String TAG = "PlaylistAdapter";
    private ArrayList<Track> trackList;
    private final LayoutInflater mInflater;
    Context mContext;


    public MediaActivityAdapter(Context mediaActivity) {
        mContext = mediaActivity;
        mInflater = LayoutInflater.from(mContext);
    }

    @NonNull
    @Override
    public PlayListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = mInflater.from(parent.getContext())
                .inflate(R.layout.playlist_item, parent, false);
        return new PlayListViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PlayListViewHolder holder, int position) {
        Track file = trackList.get(position);

        Log.i(TAG, file.getName());

        holder.pl_title.setText(file.getName());
        holder.pl_time.setText(file.getDuration());
        holder.pl_description.setText(QuranMap.SURAHNAMES[position]);

    }

    @Override
    public int getItemCount() {
        Log.i(TAG, "tracklist size " + trackList.size());
        return trackList.size();
    }

    class PlayListViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private TextView pl_title;
        private TextView pl_time;
        private TextView pl_description;
        private LinearLayout container;


        public PlayListViewHolder(@NonNull View playlistItem) {
            super(playlistItem);
            pl_title = playlistItem.findViewById(R.id.pl_title);
            pl_time = playlistItem.findViewById(R.id.pl_time);
            pl_description = playlistItem.findViewById(R.id.pl_description);
            container = playlistItem.findViewById(R.id.pl_container);

            container.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {

        }
    }

    void setTitles(ArrayList<Track> trackList) {
        Log.i(TAG, trackList.size() + " tracklist");
        this.trackList = trackList;
        notifyDataSetChanged();
    }
}

