package furqon.io.github.mobilproject;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class MediaActivityAdapter extends RecyclerView.Adapter<MediaActivityAdapter.PlayListViewHolder> {

    private ArrayList<String> trackList;
    private final LayoutInflater mInflater;
    Context mContext;


    public MediaActivityAdapter(Context mediaActivity, ArrayList<String> tracklist) {
        mContext = mediaActivity;
        this.trackList = tracklist;
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

    }

    @Override
    public int getItemCount() {
        return 0;
    }

    class PlayListViewHolder extends RecyclerView.ViewHolder {


        public PlayListViewHolder(@NonNull View playlistItem) {
            super(playlistItem);
        }
    }
}

