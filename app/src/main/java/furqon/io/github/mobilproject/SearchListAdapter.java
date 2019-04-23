package furqon.io.github.mobilproject;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class SearchListAdapter extends RecyclerView.Adapter<SearchListAdapter.SearchListViewHolder> {
    Cursor mCursor;
    Context mContext;
    public SearchListAdapter(Context context, Cursor cursor) {
        mContext = context;
        mCursor = cursor;
        SharedPref.init(context);
    }

    @NonNull
    @Override
    public SearchListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View view = inflater.inflate(R.layout.search_result_row, parent, false);
        Search.setProgressBarState(getItemCount());
        return new SearchListAdapter.SearchListViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SearchListViewHolder holder, int position) {
        if(!mCursor.moveToPosition(position)){
            return;
        }

        String name = mCursor.getString(0);
        String ayahno = mCursor.getString(2);
        String surahno = mCursor.getString(1);
        String uz = mCursor.getString(4);
        String ru = mCursor.getString(5);
        String en = mCursor.getString(6);
        String fav = mCursor.getString(7);
        holder.suraName.setText(name);
        holder.suraName.setTag(surahno);
        holder.ayahNumber.setText(ayahno);
        holder.uz_tv.setText(String.valueOf(uz));
        holder.ru_tv.setText(String.valueOf(ru));
        holder.en_tv.setText(String.valueOf(en));

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

    public class SearchListViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView suraName;
        TextView ayahNumber;
        TextView uz_tv;
        TextView ru_tv;
        TextView en_tv;


        //TODO
        public SearchListViewHolder(@NonNull View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
            suraName = itemView.findViewById(R.id.search_row_title);
            ayahNumber = itemView.findViewById(R.id.oyat_raqam);
            uz_tv = itemView.findViewById(R.id.oyat_matn);
            ru_tv = itemView.findViewById(R.id.oyat_ru);
            en_tv = itemView.findViewById(R.id.oyat_en);

        }

        @Override
        public void onClick(View view) {
            int position=this.getAdapterPosition();
            String ayano = ayahNumber.getText().toString();
            String suranomi = suraName.getText().toString();
            String suranumber = String.valueOf(suraName.getTag());
            SharedPref.write("xatchup" + suranomi, Integer.parseInt(ayano));
            SharedPref.write("xatchup", suranomi + ":" + suranumber);
            continueReading();
        }
    }
    private void continueReading() {
        if (SharedPref.contains(SharedPref.XATCHUP)) {
            String xatchup = SharedPref.read(SharedPref.XATCHUP, "");
            if (xatchup.length() > 0) {
                Intent intent;
                intent = new Intent(mContext, AyahList.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra("SURANAME", xatchup);
                mContext.startActivity(intent);
            }
        } else {
            Toast.makeText(mContext, mContext.getString(R.string.no_bookmarks), Toast.LENGTH_LONG).show();
        }

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
