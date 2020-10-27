package furqon.io.github.mobilproject;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class MemorizeDownloadAdapter extends RecyclerView.Adapter<MemorizeDownloadAdapter.DownloadListViewHolder> {

    String TAG = "MemorizeAdapter";
    private Context mContext;
    private List<ChapterTitleTable> mTitles = new ArrayList<>(); // Cached copy of titles

    @NonNull
    @Override

    public MemorizeDownloadAdapter.DownloadListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.sura_name_list, parent, false);
        return new MemorizeDownloadAdapter.DownloadListViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MemorizeDownloadAdapter.DownloadListViewHolder holder, int position) {
        ChapterTitleTable current = mTitles.get(position);
        String name = current.uzbek;
        String arname = current.arabic;
        int numb = current.chapter_id;
        holder.suraName.setText(name);
        holder.arabic_name.setText(arname);
        holder.suraNumber.setText(String.valueOf(numb));
        //Log.e(TAG, "SHOWING TITLES");
    }

    void setTitles(List<ChapterTitleTable> titles) {
        mTitles = titles;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        int c = 0;
        if (mTitles != null) {
            c = mTitles.size();
        }
        return c;
    }

    public class DownloadListViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView suraName;
        TextView arabic_name;
        TextView suraNumber;
        TextView verseCount;

        public DownloadListViewHolder(@NonNull View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
            suraName = itemView.findViewById(R.id.sura_name_item);
            arabic_name = itemView.findViewById(R.id.arabic);
            suraNumber = itemView.findViewById(R.id.sura_number_item);
            verseCount = itemView.findViewById(R.id.verse_count_tv);

            verseCount.setVisibility(View.GONE);
        }

        @Override
        public void onClick(View v) {
            int position = this.getAdapterPosition();
            Log.d(TAG, position + "");

        }
    }
}
