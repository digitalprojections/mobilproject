package furqon.io.github.mobilproject;

import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;


public class TitleListAdapter extends RecyclerView.Adapter<TitleListAdapter.SuraListViewHolder> {

    private static final String TAG = "TITLELISTADAPTER";
    //created according to the available downloaded files
    private ArrayList<String> trackList;
    //private ArrayList<String> enabledList = new ArrayList<String>();
    //private List<ChapterTitle> mTitleList = new ArrayList<>();
    //private RewardAd mRewardedVideoAd;
    private Context mContext;

    private final LayoutInflater mInflater;
    private List<ChapterTitleTable> mTitles = new ArrayList<>(); // Cached copy of titles



    TitleListAdapter(Context context, ArrayList<String> trackLst){
        //mRewardedVideoAd = new RewardAd(context);
        mInflater = LayoutInflater.from(context);
        mContext = context;
        this.trackList = trackLst;
    }






    @NonNull
    @Override
    public SuraListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {
        //View view = mInflater.inflate(R.layout.sura_title, parent, false);
        View view = mInflater.from(parent.getContext())
                .inflate(R.layout.sura_title, parent, false);
        return new SuraListViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SuraListViewHolder holder, int position) {

        ChapterTitleTable current = mTitles.get(position);

        ///Log.i("TITLES", "ddddddddddddddd " + current.status + " " + current.chapter_id);

        String name = current.uzbek;
        String arname = current.arabic;
        int numb = current.chapter_id;
        holder.suraName.setText(name);
        holder.arabic_name.setText(arname);
        holder.suraNumber.setText(String.valueOf(numb));

        //TODO deciding the state of the item is tricky, when the download initiated and untill it ends.
        /*
        Since it may be impossible to listen to the result of the download, tracking is necessary
        customized, multi stage download status check
        downloading list
        downloaded files list
        if the suranomer not found in either of the above, then it is missing
        we can mark missing files as regular LOCK icon or DOWNLOAD icon respectively
        */

        //Log.i(TAG, sharedpref.getInstance().read("download_" + current.chapter_id, 0).toString() + " download ID for " + current.chapter_id);

        if (TrackDownloaded(current.chapter_id + "")) {
            //set by the actually available audio files
            holder.downloadButton.setImageResource(R.drawable.ic_file_available);
            Log.i(TAG, " DOWNLOADED, " + current.chapter_id + " " + current.uzbek);
            holder.downloadButton.setFocusable(false);
            //holder.down_cont.setVisibility(View.INVISIBLE);
            holder.downloadButton.setTag(3);
            holder.progressBar.setVisibility(View.INVISIBLE);

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
            /*else if(itemDownloading(current.chapter_id)){
                //downloading
                Log.i(TAG, " DOWNLOADING, " + current.chapter_id + " " + current.uzbek);
                holder.downloadButton.setTag(5);
                holder.progressBar.setVisibility(View.VISIBLE);
                holder.downloadButton.setVisibility(View.GONE);
            }*/
            else {
                holder.downloadButton.setImageResource(R.drawable.ic_file_download_black_24dp);
                holder.downloadButton.setFocusable(true);
                holder.downloadButton.setTag(2);
                holder.progressBar.setVisibility(View.INVISIBLE);
                holder.downloadButton.setVisibility(View.VISIBLE);
//                holder.downloadButton.setImageResource(R.drawable.ic_unlock);
//                holder.downloadButton.setFocusable(true);
//                holder.downloadButton.setTag(1);
//                holder.downloadButton.setVisibility(View.VISIBLE);
//                holder.progressBar.setVisibility(View.INVISIBLE);
            }
        }
    }
    private boolean itemDownloading(int sid){
        DownloadManager downloadManager = (DownloadManager) mContext.getSystemService(mContext.DOWNLOAD_SERVICE);
        DownloadManager.Query query = new DownloadManager.Query();
        long did = (long) SharedPreferences.getInstance().read("downloading_surah_"+sid, 0);
        //query.setFilterById(did);
            Cursor cursor = downloadManager.query(query);
            Log.i(TAG, cursor.getCount() + " cursor count " + did);
            if(cursor.moveToFirst()){
                int columnIndex = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS);
                int status = cursor.getInt(columnIndex);
                Log.i(TAG, status + " cursor status");
                if(status == DownloadManager.STATUS_SUCCESSFUL){
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
    private boolean DownloadEnabled(String numb) {
        Log.i("VIDEO AD WATCHED", numb);
        boolean retval = false;
//        for (String i:enabledList
//        ) {
//            if(i.equals(numb)){
//                //match found
//                retval = true;
//            }
//            Log.i("DOWNLOADED ENABLED", numb + " " + i + " " + (i.equals(numb)));
//        }
        return retval;
    }

    private boolean TrackDownloaded(String v) {
        boolean retval = false;
        for (String i : trackList
        ) {
            if (i.equals(v)) {
                //match found
                Log.i("TRACK DOWNLOADED?", String.valueOf(v) + " " + i + " " + (i.equals(v)));
                retval = true;
            }

        }
        return retval;
    }

    private boolean nameNotFound(String name) {
        boolean retval = false;
        /*for (String i:mArrayList
             ) {
            if(i==name){
                retval = true;
            }
        }*/
        return retval;
    }


    @Override
    public int getItemCount() {
        int c = 0;
        if(mTitles!=null)
        {
            c = mTitles.size();
        }
        return c;
    }

    public class SuraListViewHolder extends RecyclerView.ViewHolder implements OnClickListener {
        TextView suraName;
        TextView arabic_name;
        TextView suraNumber;
        ImageView downloadButton;
        ProgressBar progressBar;
        CardView down_cont;

        SuraListViewHolder(@NonNull View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
            suraName = itemView.findViewById(R.id.sura_name_item);
            arabic_name = itemView.findViewById(R.id.arabic);
            suraNumber = itemView.findViewById(R.id.sura_number_item);
            downloadButton = itemView.findViewById(R.id.button_download);
            progressBar = itemView.findViewById(R.id.progressBar_download);
            down_cont = itemView.findViewById(R.id.download_cont);

            Log.i("DOWNLOAD BUTTON", " " + downloadButton.getTag());

            downloadButton.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    //Toast.makeText(mContext,"Download surah number " + suraNumber.getText().toString(), Toast.LENGTH_SHORT).show();
                    //String url = "https://mobilproject.github.io/furqon_web_express/by_sura/" + suranomer + ".mp3"; // your URL here
                    switch (downloadButton.getTag().toString()) {
                        case "1"://red arrow
                            ShowRewardAdForThisItem(view);
                            break;
                        case "2"://blue arrow
                            StartDownload(view);
                            break;
                    }

                }
            });
            progressBar.setVisibility(View.GONE);

        }


        @Override
        public void onClick(View view) {

            int position = this.getAdapterPosition();
            String suranomer = suraNumber.getText().toString();
            String suranomi = suraName.getText().toString();

            Log.d("CLICK", suranomer + " " + position);
            Intent intent;
            intent = new Intent(mContext, AyahList.class);
            intent.putExtra("SURANAME", suranomi + ":" + suranomer);
            mContext.startActivity(intent);


        }

        private void ShowRewardAdForThisItem(View view) {

            int position = this.getAdapterPosition();
            String suranomer = suraNumber.getText().toString();
            //String suranomi = suraName.getText().toString();
            //mRewardedVideoAd.SHOW(suranomer);

        }


        private void StartDownload(View view) {
            MyListener myListener;
            String snumber = suraNumber.getText().toString();
            if (mContext instanceof MyListener) {
                myListener = (MyListener) mContext;
                myListener.DownloadThis(snumber);
                myListener.MarkAsDownloading(Integer.parseInt(snumber));
                //getTitleAt(Integer.parseInt(snumber)-1).;
                progressBar.setVisibility(View.VISIBLE);
                downloadButton.setVisibility(View.GONE);
            }

        }
    }
}
