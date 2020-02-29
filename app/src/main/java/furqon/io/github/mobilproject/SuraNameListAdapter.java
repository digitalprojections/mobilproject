package furqon.io.github.mobilproject;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.opengl.Visibility;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
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
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;

public class SuraNameListAdapter extends RecyclerView.Adapter<SuraNameListAdapter.SuraListViewHolder> {
    private Context mContext;
    private Cursor mCursor;
    private RewardedVideoAd mRewardedAd;
    private ArrayList<String> mArrayList;
    SuraNameListAdapter(Context context, Cursor cursor, RewardedVideoAd mRewardedAd){
        mContext = context;
        mCursor = cursor;
        this.mRewardedAd = mRewardedAd;
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
            downloadButton.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    //Toast.makeText(mContext,"Download surah number " + suraNumber.getText().toString(), Toast.LENGTH_SHORT).show();
                    //ca-app-pub-3838820812386239/1790049383
                    mRewardedAd.loadAd(mContext.getString(R.string.surahAudioUnlockAd),
                            new AdRequest.Builder().build());
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
        mArrayList.add(name);

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
