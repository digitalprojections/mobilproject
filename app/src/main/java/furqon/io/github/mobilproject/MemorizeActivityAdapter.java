package furqon.io.github.mobilproject;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class MemorizeActivityAdapter extends RecyclerView.Adapter<MemorizeActivityAdapter.AyahViewHolder> {
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
    public void onBindViewHolder(@NonNull AyahViewHolder holder, int position, @NonNull List<Object> payloads) {
        super.onBindViewHolder(holder, position, payloads);
    }

    @Override
    public int getItemCount() {
        return 0;
    }

    class AyahViewHolder extends RecyclerView.ViewHolder {
        public AyahViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }
}
