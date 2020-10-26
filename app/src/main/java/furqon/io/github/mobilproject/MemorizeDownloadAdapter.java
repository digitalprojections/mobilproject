package furqon.io.github.mobilproject;

import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class MemorizeDownloadAdapter extends RecyclerView.Adapter<MemorizeDownloadAdapter.DownloadListViewHolder> {
    Arra

    @NonNull
    @Override

    public MemorizeDownloadAdapter.DownloadListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return null;
    }

    @Override
    public void onBindViewHolder(@NonNull MemorizeDownloadAdapter.DownloadListViewHolder holder, int position) {

    }

    @Override
    public int getItemCount() {
        return 0;
    }

    public class DownloadListViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public DownloadListViewHolder(@NonNull View itemView) {
            super(itemView);
        }

        @Override
        public void onClick(View v) {

        }
    }
}
