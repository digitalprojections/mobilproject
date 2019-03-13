package furqon.io.github.mobilproject;

import android.content.Context;
import android.database.Cursor;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

public class AyahListAdapter extends RecyclerView.Adapter<AyahListAdapter.AyahListViewHolder> {
    private Context mContext;
    private Cursor mCursor;
    private ArrayList<String> mArrayList;

    AyahListAdapter(Context context, Cursor cursor){
        mContext = context;
        mCursor = cursor;
    }


    public static class AyahListViewHolder extends RecyclerView.ViewHolder implements OnClickListener{
        TextView ayatext;
        TextView arabictext;
        TextView ayahnumber;
        TextView arabic_ayahnumber;


        AyahListViewHolder(@NonNull View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
            ayahnumber = itemView.findViewById(R.id.ayah_no);
            ayatext = itemView.findViewById(R.id.ayah_text);
            arabictext = itemView.findViewById(R.id.arab_ayah_text);
            arabic_ayahnumber = itemView.findViewById(R.id.arab_ayah_no);
        }


        @Override
        public void onClick(View view) {
            int position=getAdapterPosition();
            Log.d("CLICK", ayatext.getText().toString());

        }
    }

    @NonNull
    @Override
    public AyahListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View view = inflater.inflate(R.layout.ayat, parent, false);

        mArrayList = new ArrayList<>();
        return new AyahListViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AyahListViewHolder holder, int i) {
        if(!mCursor.moveToPosition(i)){
            return;
        }

        String ttext = mCursor.getString(1);
        String artext = mCursor.getString(0);
        String numb = mCursor.getString(2);

        holder.ayatext.setText(collapseBraces(ttext));

        holder.arabictext.setText(artext);
        holder.ayahnumber.setText(String.valueOf(numb));
        holder.arabic_ayahnumber.setText(String.valueOf(numb));
        mArrayList.add(numb);

    }
    private String collapseBraces(String t){
        String retval;
        if(t.indexOf("(")>0)
        {
            retval = t.substring(0, t.indexOf("(")-1);
        }
        else {
            retval = t;
        }

        return retval;
    }

    @Override
    public int getItemCount() {
        return mCursor.getCount();
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
