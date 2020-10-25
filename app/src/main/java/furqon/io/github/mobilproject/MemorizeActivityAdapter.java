package furqon.io.github.mobilproject;

import android.content.Context;
import android.graphics.Typeface;
import android.os.Build;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class MemorizeActivityAdapter extends RecyclerView.Adapter<MemorizeActivityAdapter.AyahViewHolder> {

    private static final String TAG = MemorizeActivityAdapter.class.getSimpleName();
    Context mContext;
    SharedPreferences sharedPreferences;


    private ArrayList<String> mArrayList;
    private List<AyahRange> mAyahList;

    private String verse_number;//oyat nomeri
    private ViewGroup.LayoutParams lpartxt; // Height of TextView

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
                ViewGroup.LayoutParams.WRAP_CONTENT, 10.0f);
        return new AyahViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AyahViewHolder holder, int position) {
        AyahRange current = mAyahList.get(position);

        String ayah_txt = current.ar_text;
        String verse_number = String.valueOf(current.verse_id);
        String download_progress = Double.toString(Math.ceil(current.audio_progress));
        holder.arabic_text.setGravity(Gravity.END);
        holder.arabic_text.setText(ayah_txt);
        holder.ayah_number.setText(verse_number);
        Log.d(TAG, "RANGE " + verse_number);
    }


    @Override
    public int getItemCount() {
        int size = 0;
        if(mAyahList!=null)
            size = mAyahList.size();
        return size;
    }

    public void setText(List<AyahRange> ayahRanges) {
            mAyahList = ayahRanges;
            notifyDataSetChanged();
    }

    class AyahViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        TextView arabic_text;
        TextView ayah_number;
        LinearLayout arabic_text_lin_layout;

        public AyahViewHolder(@NonNull View itemView) {
            super(itemView);
            arabic_text_lin_layout = itemView.findViewById(R.id.h_mem_ar_layout);
            arabic_text = itemView.findViewById(R.id.memorization_arabic_tv);
            ayah_number = itemView.findViewById(R.id.arab_number);
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
            arabic_text.setTextSize(30);
            if (sharedPreferences.contains(sharedPreferences.FONTSIZE)) {
                float fs = (float) sharedPreferences.read(sharedPreferences.FONTSIZE, 0);
                arabic_text.setTextSize(TypedValue.COMPLEX_UNIT_PX, fs);
            }
            arabic_text_lin_layout.setGravity(Gravity.END);
            arabic_text.setLayoutParams(lpartxt);
            arabic_text.setTextDirection(View.TEXT_DIRECTION_ANY_RTL);
            arabic_text.setGravity(Gravity.END);
            arabic_text.setTypeface(madina);


        }

        @Override
        public void onClick(View v) {
            //TODO repeat the verse on click
        }
    }
}
