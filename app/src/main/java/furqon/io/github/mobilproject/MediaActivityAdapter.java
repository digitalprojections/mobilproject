package furqon.io.github.mobilproject;

import android.app.DownloadManager;
import android.content.Context;
import android.database.Cursor;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;


public class MediaActivityAdapter extends RecyclerView.Adapter<MediaActivityAdapter.PlayListViewHolder> {

    private String TAG = "PlaylistAdapter";
    private ArrayList<Track> trackList;
    private List<ChapterTitleTable> mTitles = new ArrayList<>(); // Cached copy of titles
    private boolean download_view;
    private RewardAd mRewardedVideoAd;
    private TextView pl_title;
    Context mContext;


    MediaActivityAdapter(Context mediaActivity) {
        //LayoutInflater mInflater = LayoutInflater.from(mediaActivity);
        mContext = mediaActivity;
        mRewardedVideoAd = new RewardAd(mContext);
    }

    @NonNull
    @Override
    public PlayListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.playlist_item, parent, false);
        pl_title = view.findViewById(R.id.pl_title);
        return new PlayListViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PlayListViewHolder holder, int position) {

        if(download_view){
            holder.download_view = true;
            ChapterTitleTable current = mTitles.get(position);
            String name = current.uzbek;
            //Log.i(TAG, name);
            //String arname = current.arabic;
            int numb = current.chapter_id;
            holder.pl_description.setText(name);
            holder.pl_time.setVisibility(View.GONE);
            holder.pl_title.setText(String.valueOf(numb));
            holder.down_cont.setVisibility(View.VISIBLE);

            if (TrackDownloaded(current.chapter_id + "")) {
                //set by the actually available audio files
                holder.downloadButton.setImageResource(R.drawable.ic_file_available);
                //Log.i(TAG, " DOWNLOADED, " + current.chapter_id + " " + current.uzbek);
                holder.downloadButton.setFocusable(false);
                holder.downloadButton.setTag(3);
                holder.progressBar.setVisibility(View.INVISIBLE);
                holder.downloadButton.setVisibility(View.VISIBLE);

            } else {
                if (current.status.equals("2")) {
                    //download allowed. Active within the session only. Forgotten on restart
                    holder.downloadButton.setImageResource(R.drawable.ic_file_download_black_24dp);
                    holder.downloadButton.setFocusable(true);
                    holder.downloadButton.setTag(2);
                    holder.progressBar.setVisibility(View.INVISIBLE);
                    holder.downloadButton.setVisibility(View.VISIBLE);
                } else if (current.status.equals("4")) {
                    holder.downloadButton.setVisibility(View.GONE);
                    holder.downloadButton.setFocusable(false);
                    holder.downloadButton.setTag(4);
                    holder.progressBar.setVisibility(View.VISIBLE);
                }
//            else if(itemDownloading(current.chapter_id)){
//                //downloading
//                Log.i(TAG, " DOWNLOADING, " + current.chapter_id + " " + current.uzbek);
//                holder.downloadButton.setTag(5);
//                holder.progressBar.setVisibility(View.VISIBLE);
//                holder.downloadButton.setVisibility(View.GONE);
//            }
                else {
//                    holder.downloadButton.setImageResource(R.drawable.ic_file_download_black_24dp);
//                    holder.downloadButton.setFocusable(true);
//                    holder.downloadButton.setTag(2);
//                    holder.progressBar.setVisibility(View.INVISIBLE);
//                    holder.downloadButton.setVisibility(View.VISIBLE);
                    holder.downloadButton.setImageResource(R.drawable.ic_unlock);
                    holder.downloadButton.setFocusable(true);
                    holder.downloadButton.setTag(1);
                    holder.downloadButton.setVisibility(View.VISIBLE);
                    holder.progressBar.setVisibility(View.INVISIBLE);
                }
            }

        }else{
            holder.download_view = false;
            Track file = trackList.get(position);
            //Log.i(TAG, file.getUri());
            holder.pl_title.setText(file.getName());
            holder.pl_time.setText(file.getDuration());
            holder.pl_description.setText(QuranMap.SURAHNAMES[Integer.parseInt(file.getName()) - 1]);
            holder.pl_time.setVisibility(View.VISIBLE);
            holder.downloadButton.setVisibility(View.GONE);
            holder.progressBar.setVisibility(View.GONE);
            holder.down_cont.setVisibility(View.GONE);
        }
    }

    private boolean itemDownloading(int sid) {
        DownloadManager downloadManager = (DownloadManager) mContext.getSystemService(mContext.DOWNLOAD_SERVICE);
        DownloadManager.Query query = new DownloadManager.Query();
        long did = (long) SharedPreferences.getInstance().read("downloading_surah_" + sid, 0);
        //query.setFilterById(did);
        Cursor cursor = downloadManager.query(query);
        Log.i(TAG, cursor.getCount() + " cursor count " + did);
        if (cursor.moveToFirst()) {
            int columnIndex = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS);
            int status = cursor.getInt(columnIndex);
            Log.i(TAG, status + " cursor status");
            if (status == DownloadManager.STATUS_SUCCESSFUL) {
                return true;
            }
        }
        return false;
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

    private String prependZero(String s) {
        String retval = s;
        switch (s.length()) {
            case 1:
                retval = "00" + s;
                break;
            case 2:
                retval = "0" + s;
                break;
            case 3:
                retval = s;
                break;
        }
        return retval;
    }
    private boolean TrackDownloaded(String v) {
        //v = prependZero(v);
        boolean retval = false;
        if (trackList != null) {
            for (Track i : trackList
            ) {
                if (i.getName().equals(v)) {
                    //match found
                    //Log.i("TRACK DOWNLOADED?", String.valueOf(v) + " " + i + " " + (i.equals(v)));
                    retval = true;
                }

            }
        }
        return retval;
    }
    @Override
    public int getItemCount() {

        //Log.i(TAG, "tracklist size " + trackList.size());
        int c = 0;
        if (download_view) {
            if (mTitles != null) {
                c = mTitles.size();
            }
        } else {
            if (trackList != null) {
                c = trackList.size();
            }
        }


        return c;
    }

    public void setDownload_view(boolean download_view) {
        this.download_view = download_view;
    }

    public boolean getDownload_view() {
        return download_view;
    }
    class PlayListViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        boolean download_view;
        private TextView pl_title;
        private TextView pl_time;
        private TextView pl_description;
        ImageView downloadButton;
        ProgressBar progressBar;
        CardView down_cont;
        LinearLayout container;

        PlayListViewHolder(@NonNull View playlistItem) {
            super(playlistItem);
            pl_title = playlistItem.findViewById(R.id.pl_title);
            pl_time = playlistItem.findViewById(R.id.pl_time);
            pl_description = playlistItem.findViewById(R.id.pl_description);
            container = playlistItem.findViewById(R.id.pl_container);
            downloadButton = playlistItem.findViewById(R.id.button_download);
            progressBar = playlistItem.findViewById(R.id.progressBar_download);
            down_cont = playlistItem.findViewById(R.id.download_cont);

            container.setOnClickListener(this);
        }

        private void ShowRewardAdForThisItem(View view) {

            int position = this.getAdapterPosition();

            mRewardedVideoAd.SHOW(pl_title.getText().toString());

        }
        @Override
        public void onClick(View v) {
            if (download_view) {
                switch (downloadButton.getTag().toString()) {
                    case "1"://red arrow
                        ShowRewardAdForThisItem(v);
                        break;
                    case "2"://green arrow
                        StartDownload(v, pl_title.getText().toString());
                        break;
                }

            } else {
                //TODO play track
                SetSuraNumber myListener;
                myListener = (SetSuraNumber) mContext;
                myListener.SetSurahNumber(pl_title.getText().toString());

                Playable playable;
                playable = (Playable) mContext;
                playable.OnTrackPlay();
            }
        }

        private void StartDownload(View view, String title) {

            String snumber = title;
            if (!TrackDownloaded(title)) {
                MyListener myListener;
                myListener = (MyListener) mContext;
                myListener.DownloadThis(snumber);
                //myListener.MarkAsDownloading(Integer.parseInt(snumber));
                //getTitleAt(Integer.parseInt(snumber)-1).;
                progressBar.setVisibility(View.VISIBLE);
                downloadButton.setVisibility(View.GONE);
            } else {
                Log.e(TAG, "track already downloaded");
            }
        }
    }

    void setTitles(ArrayList<Track> trackList) {
        Log.i(TAG, trackList.size() + " tracklist");
        this.trackList = trackList;
        notifyDataSetChanged();
    }

}

