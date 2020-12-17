package furqon.io.github.mobilproject;

import android.content.Context;
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
    private ViewGroup.LayoutParams lpartxt; // Height of TextView
    private ArrayList<Track> trackList;

    public MemorizeActivityAdapter(MemorizeActivity memorizeActivity) {
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
        String verse_number = String.valueOf(current.verse_id);
        String download_progress = Double.toString(Math.ceil(current.audio_progress));
        holder.arabic_text.setGravity(Gravity.START);
        holder.arabic_text.setText(ayah_txt);
        holder.ayah_number.setText(verse_number);

        if(current.audio_progress>0 && current.audio_progress<100)
        {
            holder.progressBar.setVisibility(View.VISIBLE);
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
        try{
            return mAyahList.get(position);
        }catch (IndexOutOfBoundsException iobx){
            //throw new IndexOutOfBoundsException();
            return null;
        }
    }
    public void setText(List<AyahRange> ayahRanges) {
        mAyahList = ayahRanges;
        notifyDataSetChanged();
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
            if (sharedPreferences.contains(sharedPreferences.FONTSIZE)) {
                float fs = (float) sharedPreferences.read(sharedPreferences.FONTSIZE, 0);
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

            StartDownload(fixZeroes(this.ayah_number.getText().toString()));
        }
        private String fixZeroes(String s){
            String retVal = "";
            int tempVal;
            //try to parse the string into integer
            try{
                tempVal = Integer.parseInt(s);
                if(tempVal<10){
                    retVal = "00"+tempVal;
                }else if(tempVal>9&&tempVal<100){
                    retVal = "0"+tempVal;
                }else {
                    //it is higher than 99
                    retVal = s;
                }
            }catch (IllegalFormatException ignore){

            }

            return retVal;
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
            }
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
    }
}
