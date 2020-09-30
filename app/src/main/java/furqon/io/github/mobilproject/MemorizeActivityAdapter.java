package furqon.io.github.mobilproject;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class MemorizeActivityAdapter extends RecyclerView.Adapter<MemorizeActivityAdapter.AyahViewHolder> {

    Context mContext;
    SharedPreferences sharedPreferences;

    private ArrayList<String> mArrayList;

    private String verse_number;//oyat nomeri

    public MemorizeActivityAdapter(MemorizeActivity memorizeActivity) {
        mContext = memorizeActivity;
        sharedPreferences = SharedPreferences.getInstance();
    }

    @NonNull
    @Override
    public MemorizeActivityAdapter.AyahViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.memorize_ayat_item, parent, false);
        return new AyahViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AyahViewHolder holder, int position) {

    }


    @Override
    public int getItemCount() {
        return 0;
    }

    class AyahViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        TextView arabic_text;
        TextView ayah_number;

        public AyahViewHolder(@NonNull View itemView) {
            super(itemView);

            arabic_text = itemView.findViewById(R.id.memorization_arabic_tv);
            ayah_number = itemView.findViewById(R.id.arab_number);
            //TODO implement the font and size


        }

        @Override
        public void onClick(View v) {
            //TODO repeat the verse on click
        }
    }
}
