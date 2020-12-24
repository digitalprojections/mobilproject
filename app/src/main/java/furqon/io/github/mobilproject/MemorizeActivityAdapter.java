package furqon.io.github.mobilproject;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.IllegalFormatException;
import java.util.List;

public class MemorizeActivityAdapter extends RecyclerView.Adapter<MemorizeActivityAdapter.AyahViewHolder> {

    private static final String TAG = MemorizeActivityAdapter.class.getSimpleName();
    Context mContext;
    SharedPreferences sharedPreferences;

    private ArrayList<String> mArrayList;
    private List<AyahRange> mAyahList;

    private String verse_number;//oyat nomeri
    private String sura_number;

    private ViewGroup.LayoutParams lpartxt; // Height of TextView
    private ArrayList<Track> trackList;
    private int playingTrackIndex = -1;

    public MemorizeActivityAdapter(MemorizeActivity memorizeActivity, String sura_number) {
        mContext = memorizeActivity;
        sharedPreferences = SharedPreferences.getInstance();
    }

    @NonNull
    @Override
    public MemorizeActivityAdapter.AyahViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.memorize_ayat_item, parent, false);
        lpartxt = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, // Width of TextView
                ViewGroup.LayoutParams.WRAP_CONTENT,
                0.0f);
        return new AyahViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AyahViewHolder holder, int position) {
        AyahRange current = mAyahList.get(position);

        String ayah_txt = current.ar_text;
        verse_number = String.valueOf(current.verse_id);
        sura_number = String.valueOf(current.sura_id);
        String download_progress = Double.toString(Math.ceil(current.audio_progress));
        holder.arabic_text.setGravity(Gravity.START);
        holder.arabic_text.setText(ayah_txt);
        holder.ayah_number.setText(verse_number);
        if(playingTrackIndex==position){
            Log.d(TAG, "PLAYING INDEX FOUND");
            //"#A103A9F4"
            //#6D03A9F4
            holder.arabic_text_lin_layout.setBackgroundColor(Color.parseColor("#A103A9F4"));
        }else{
            holder.arabic_text_lin_layout.setBackgroundColor(Color.parseColor("#6D03A9F4"));
        }

        if(current.audio_progress>0 && current.audio_progress<100)
        {
            holder.progressBar.setVisibility(View.VISIBLE);

            if(holder.TrackDownloaded(String.valueOf(current.verse_id)) && current.audio_progress!=100)
            {
                current.audio_progress = 100;
                MyListener myListener;
                myListener = (MyListener) mContext;
                myListener.MarkAsDownloaded(current.verse_id);
                Log.d(TAG, "DOWNLOADED ayah " + current.verse_id);
            }
        }else if(current.audio_progress==100){
            holder.progressBar.setVisibility(View.GONE);
            holder.audio_file.setBackgroundResource(R.drawable.ic_audio_symbol);
        }else{
            holder.progressBar.setVisibility(View.GONE);
            holder.audio_file.setBackgroundResource(R.drawable.ic_audio_missing);
        }
        //Log.d(TAG, "RANGE ayah " + current.verse_id + ", audio downloaded % - " + current.audio_progress);
    }


    @Override
    public int getItemCount() {
        int size = 0;
        if(mAyahList!=null)
            size = mAyahList.size();
        return size;
    }
    public AyahRange getTitleAt(int position){

            if (mAyahList!=null && mAyahList.size()>=position+1)
                return mAyahList.get(position);
            else
                return null;
    }
    public void setText(List<AyahRange> ayahRanges) {

            if(mAyahList!=null && ayahRanges==null){
                mAyahList.clear();
        }else{
                mAyahList = ayahRanges;
        }
            notifyDataSetChanged();
    }
    public void setTrackList(ArrayList<Track> tracks){
        trackList = tracks;
    }

    public void markAsPlaying(String suraNumber2Play) {
        if(suraNumber2Play!=null) {
            int verseNumber = Integer.parseInt(suraNumber2Play) % 1000;
            if (mAyahList != null) {
                Log.d(TAG, "ayahlist " + verseNumber + " in " + mAyahList.size());
                for (AyahRange i : mAyahList) {
                    //Log.d(TAG, "ayahlist item: " + i.verse_id);
                    if (i.verse_id == verseNumber) {
                        //match found
                        Log.i(TAG, "TRACK found " + suraNumber2Play + " vs " + mAyahList.indexOf(i));
                        playingTrackIndex = mAyahList.indexOf(i);
                        if(i.audio_progress<100){
                            MyListener myListener;
                            myListener = (MyListener) mContext;
                            myListener.MarkAsDownloaded(verseNumber);
                        }
                    }
                }
            }
        }else{
            playingTrackIndex = -1;
        }
    }

    class AyahViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        TextView audio_file;
        TextView arabic_text;
        TextView ayah_number;
        TextView translit;
        LinearLayout arabic_text_lin_layout;
        ProgressBar progressBar;


        public AyahViewHolder(@NonNull View itemView) {
            super(itemView);
            audio_file = itemView.findViewById(R.id.audio_file_icon);
            arabic_text_lin_layout = itemView.findViewById(R.id.h_mem_ar_layout);
            arabic_text = itemView.findViewById(R.id.memorization_arabic_tv);
            ayah_number = itemView.findViewById(R.id.arab_number);
            translit = itemView.findViewById(R.id.transliteration_tv);
            progressBar = itemView.findViewById(R.id.progressBarVerse);

            arabic_text.setOnClickListener(this);

            Typeface madina;
            if (sharedPreferences.contains(sharedPreferences.FONT)) {
                switch (sharedPreferences.read(sharedPreferences.FONT, "")) {
                    case "madina":
                        madina = ResourcesCompat.getFont(mContext, R.font.maddina);
                        break;
                    case "usmani":
                        madina = ResourcesCompat.getFont(mContext, R.font.al_uthmani);
                        break;
                    case "qalam":
                        madina = ResourcesCompat.getFont(mContext, R.font.al_qalam);
                        break;
                    default:
                        madina = ResourcesCompat.getFont(mContext, R.font.al_qalam);
                        break;
                }
            } else {
                madina = ResourcesCompat.getFont(mContext, R.font.al_qalam);
            }
            ((LinearLayout.LayoutParams) lpartxt).setMargins(10, 0, 1, 1);
            translit.setVisibility(View.GONE);
            audio_file.setBackgroundResource(R.drawable.ic_audio_missing);
            arabic_text.setTextSize(30);
            if (sharedPreferences.contains(SharedPreferences.FONTSIZE)) {
                float fs = (float) sharedPreferences.read(SharedPreferences.FONTSIZE, 0);
                arabic_text.setTextSize(TypedValue.COMPLEX_UNIT_PX, fs);
            }
            arabic_text_lin_layout.setGravity(Gravity.END | Gravity.CENTER_VERTICAL | Gravity.RIGHT);
            //arabic_text.setLayoutParams(lpartxt);
            arabic_text.setTextDirection(View.TEXT_DIRECTION_ANY_RTL);
            arabic_text.setGravity(Gravity.START);
            arabic_text.setTypeface(madina);
            //arabic_text.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_END);
            progressBar.setVisibility(View.GONE);
        }

        @Override
        public void onClick(View v) {
            //TODO repeat the verse on click
            Log.d(TAG, "clicked " + this.ayah_number.getText());
            //TODO check against the existing files in the matching folder and play or download

            StartDownload(this.ayah_number.getText().toString());
        }

        private void StartDownload(String verse) {
            if (!TrackDownloaded(verse)) {
                MyListener myListener;
                myListener = (MyListener) mContext;
                myListener.DownloadThis(verse);
                myListener.MarkAsDownloading(Integer.parseInt(verse));
                //getTitleAt(Integer.parseInt(snumber)-1).;
                progressBar.setVisibility(View.VISIBLE);
                //downloadButton.setVisibility(View.GONE);
            } else {
                Log.e(TAG, "track already downloaded");
                SetSuraNumber myListener;
                myListener = (SetSuraNumber) mContext;
                myListener.SetSurahNumber(verse);

                Playable playable;
                playable = (Playable) mContext;
                playable.OnTrackPlay();

            }
        }
        private boolean TrackDownloaded(String v) {

            String ayahReferenceNumber = ARG.makeAyahRefName(v);
            boolean retval = false;


            if (trackList != null) {
                Log.d(TAG, "tracklist " + trackList.size());
                for (Track i : trackList) {

                    if (i.getName().compareTo(ayahReferenceNumber)==0) {
                        //match found
                        //
                        Log.i(TAG, "TRACK DOWNLOADED " + String.valueOf(v) + " - " + ayahReferenceNumber + " " + i.getName());
                        retval = true;
                    }
                }
                }
            return retval;
        }
    }
}
