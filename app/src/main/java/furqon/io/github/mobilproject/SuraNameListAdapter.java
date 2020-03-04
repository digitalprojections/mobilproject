package furqon.io.github.mobilproject;

import android.Manifest;
import android.app.Activity;
import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.opengl.Visibility;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.reward.RewardedVideoAd;
import com.google.android.gms.ads.rewarded.RewardedAd;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.util.ArrayList;


public class SuraNameListAdapter extends RecyclerView.Adapter<SuraNameListAdapter.SuraListViewHolder> {
    private Context mContext;
    private Cursor mCursor;

    //created according to the available downloaded files
    private ArrayList<String> trackList = new ArrayList<String>();
    private ArrayList<String> enabledList = new ArrayList<String>();
    private ArrayList<String> mArrayList;
    private RewardAd mRewardedVideoAd;

    private Track track;

    SuraNameListAdapter(Context context, Cursor cursor, ArrayList<String> track, ArrayList<String> downloadables){
        mContext = context;
        mCursor = cursor;
        mRewardedVideoAd = new RewardAd(mContext);

        if(track.size()>0){
            //generate tracks
            for (String i:track
                 ) {
                trackList.add(i);
            }
        }
        enabledList = downloadables;
    }




    public class SuraListViewHolder extends RecyclerView.ViewHolder implements OnClickListener{
        TextView suraName;
        TextView arabic_name;
        TextView suraNumber;
        ImageView downloadButton;
        ProgressBar progressBar;


        SuraListViewHolder(@NonNull View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);

            suraName = itemView.findViewById(R.id.sura_name_item);
            arabic_name = itemView.findViewById(R.id.arabic);
            suraNumber = itemView.findViewById(R.id.sura_number_item);
            downloadButton = itemView.findViewById(R.id.button_download);
            progressBar = itemView.findViewById(R.id.progressBar_download);

            Log.i("DOWNLOAD BUTTON", " " + downloadButton.getTag());

            downloadButton.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    //Toast.makeText(mContext,"Download surah number " + suraNumber.getText().toString(), Toast.LENGTH_SHORT).show();
                    //String url = "https://mobilproject.github.io/furqon_web_express/by_sura/" + suranomer + ".mp3"; // your URL here
                    switch (downloadButton.getTag().toString()){
                        case "1"://red
                            ShowRewardAdForThisItem(view);
                            break;
                        case "2"://green
                            StartDownload(view);
                            break;
                    }

                }
            });
            progressBar.setVisibility(View.GONE);

        }


        @Override
        public void onClick(View view) {

            int position=this.getAdapterPosition();
            String suranomer = suraNumber.getText().toString();
            String suranomi = suraName.getText().toString();

            Log.d("CLICK", suranomer);
            Intent intent;
            intent = new Intent(mContext, AyahList.class);
            intent.putExtra("SURANAME",suranomi+":"+suranomer);
            mContext.startActivity(intent);



        }
        private void ShowRewardAdForThisItem(View view) {
            //int position=this.getAdapterPosition();
            String suranomer = suraNumber.getText().toString();
            String suranomi = suraName.getText().toString();
            mRewardedVideoAd.SHOW(suranomer);
        }
        private void StartDownload(View view) {
            MyListener myListener;
            if(mContext instanceof MyListener){
                myListener = (MyListener) mContext;
                myListener.DownloadThis(suraNumber.getText().toString());
            }

        }
    }

    @NonNull
    @Override
    public SuraListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View view = inflater.inflate(R.layout.sura_title, parent, false);
        mArrayList = new ArrayList<>();
        return new SuraListViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SuraListViewHolder holder, int i) {
        if(!mCursor.moveToPosition(i)){
            return;
        }

        String name = mCursor.getString(2);
        String arname = mCursor.getString(1);
        String numb = mCursor.getString(0);
        holder.suraName.setText(name);
        holder.arabic_name.setText(arname);
        holder.suraNumber.setText(String.valueOf(numb));
        if(!nameNotFound(name)){
            mArrayList.add(name);
        }
        if(TrackDownloaded(numb)){
            //set by the actually available audio files
            holder.downloadButton.setImageResource(R.drawable.ic_file_available);
            holder.downloadButton.setFocusable(false);
            holder.downloadButton.setTag(3);
            holder.progressBar.setVisibility(View.VISIBLE);
        }else{
            if(DownloadEnabled(numb)){
                //download allowed. Active within the session only. Forgotten on restart
                holder.downloadButton.setImageResource(R.drawable.ic_file_download_done);
                holder.downloadButton.setFocusable(true);
                holder.downloadButton.setTag(2);
                holder.progressBar.setVisibility(View.GONE);
            }else{
                holder.downloadButton.setImageResource(R.drawable.ic_file_download_red);
                holder.downloadButton.setFocusable(true);
                holder.downloadButton.setTag(1);
                holder.progressBar.setVisibility(View.GONE);
            }
        }

    }

    private boolean DownloadEnabled(String numb) {
        Log.i("VIDEO AD WATCHED", numb);
        boolean retval = false;
        for (String i:enabledList
        ) {
            if(i.equals(numb)){
                //match found
                retval = true;
            }
            Log.i("DOWNLOADED ENABLED", numb + " " + i + " " + (i.equals(numb)));
        }
        return retval;
    }

    private boolean TrackDownloaded(String v) {
        Log.i("TRACK DOWNLOADED?", v);
        boolean retval = false;
        for (String i:trackList
             ) {
            if(i.equals(v)){
                //match found
                retval = true;

            }
            Log.i("TRACK DOWNLOADED?", v + " " + i + " " + (i.equals(v)));
        }
        return retval;
    }

    private boolean nameNotFound(String name) {
        boolean retval = false;
        for (String i:mArrayList
             ) {
            if(i==name){
                retval = true;
            }
        }
        return retval;
    }


    @Override
    public int getItemCount() {
        int c;
        try{
            c  = mCursor.getCount();
        }
        catch (NullPointerException e){
            c = 0;
        }
        return c;
    }

    public void swapCursor(Cursor newCursor){
        if(mCursor!=null){
            mCursor.close();
        }

        mCursor = newCursor;
        if(newCursor!=null){
            notifyDataSetChanged();
        }
    }

}
