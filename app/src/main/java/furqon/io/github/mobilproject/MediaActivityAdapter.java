package furqon.io.github.mobilproject;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;


public class MediaActivityAdapter extends RecyclerView.Adapter<MediaActivityAdapter.PlayListViewHolder> {

    private String TAG = "PlaylistAdapter";
    private ArrayList<Track> trackList;
    private List<ChapterTitleTable> mTitles = new ArrayList<>(); // Cached copy of titles
    private boolean download_view;


    MediaActivityAdapter(Context mediaActivity) {
        //LayoutInflater mInflater = LayoutInflater.from(mediaActivity);

    }

    @NonNull
    @Override
    public PlayListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.playlist_item, parent, false);
        return new PlayListViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PlayListViewHolder holder, int position) {

        if(download_view){
            ChapterTitleTable current = mTitles.get(position);
            String name = current.uzbek;
            //String arname = current.arabic;
            int numb = current.chapter_id;
            holder.pl_description.setText(name);
            holder.pl_time.setVisibility(View.INVISIBLE);
            holder.pl_title.setText(String.valueOf(numb));
        }else{
            Track file = trackList.get(position);
            Log.i(TAG, file.getName());
            holder.pl_time.setVisibility(View.VISIBLE);
            holder.pl_title.setText(file.getName());
            holder.pl_time.setText(file.getDuration());
            holder.pl_description.setText(QuranMap.SURAHNAMES[Integer.parseInt(file.getName()) - 1]);
    }
    }
    void setTitles(List<ChapterTitleTable> titles){
        mTitles = titles;
        notifyDataSetChanged();
    }
    public ChapterTitleTable getTitleAt(int position){
        try{
            return mTitles.get(position);
        }catch (IndexOutOfBoundsException iobx){
            throw new IndexOutOfBoundsException();
        }

    }
    private boolean TrackDownloaded(String v) {
        boolean retval = false;
        for (Track i : trackList
        ) {
            if (i.equals(v)) {
                //match found
                Log.i("TRACK DOWNLOADED?", String.valueOf(v) + " " + i + " " + (i.equals(v)));
                retval = true;
            }

        }
        return retval;
    }
    @Override
    public int getItemCount() {

        Log.i(TAG, "tracklist size " + trackList.size());
        int c=0;
        if(download_view){
            if(mTitles!=null)
            {
                c = mTitles.size();
            }
        }else{
            c = trackList.size();
        }
        return c;
    }

    public void setDownload_view(boolean download_view) {
        this.download_view = download_view;
    }

    static class PlayListViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private TextView pl_title;
        private TextView pl_time;
        private TextView pl_description;


        PlayListViewHolder(@NonNull View playlistItem) {
            super(playlistItem);
            pl_title = playlistItem.findViewById(R.id.pl_title);
            pl_time = playlistItem.findViewById(R.id.pl_time);
            pl_description = playlistItem.findViewById(R.id.pl_description);
            LinearLayout container = playlistItem.findViewById(R.id.pl_container);

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

