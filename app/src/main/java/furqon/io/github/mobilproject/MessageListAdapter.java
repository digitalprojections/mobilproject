package furqon.io.github.mobilproject;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class MessageListAdapter extends RecyclerView.Adapter<MessageListAdapter.MessageListViewHolder>{

    private List<MessageTable> mMessages = new ArrayList<>();
    private final LayoutInflater mInflater;
    private ViewGroup.LayoutParams fontStyle;

    public MessageListAdapter(Context context) {
        mInflater = LayoutInflater.from(context);

    }
    @NonNull
    @Override
    public MessageListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = mInflater.from(parent.getContext())
                .inflate(R.layout.message_card_item, parent, false);
        return new MessageListViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageListViewHolder holder, int position) {

        MessageTable current = mMessages.get(position);

        holder.titleTextView.setText(current.message_title);
        holder.bodyTextView.setText(current.message_body);
        holder.dateTextView.setText(current.date_time);
        if(current.message_read==0){
            ((LinearLayout.LayoutParams) fontStyle).weight = 500;
            holder.titleTextView.setLayoutParams(fontStyle);
        }
        Log.e("MESSAGES ONBIND", current.message_body);

    }



    @Override
    public int getItemCount() {
        Log.e("MESSAGES on get count", mMessages.size() + " long");
        return mMessages.size();
    }
    void setItems(List<MessageTable> items){
        mMessages = items;
        Log.e("MESSAGES on set", mMessages.size() + " long");
        notifyDataSetChanged();
    }
    public MessageTable getItemAt(int position){
        return mMessages.get(position);
    }

    public class MessageListViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView titleTextView;
        TextView bodyTextView;
        TextView dateTextView;


        MessageListViewHolder(@NonNull View itemView) {
            super(itemView);
            Log.e("ITEMVIEW on MLVH", mMessages.size() + " long");
            titleTextView = itemView.findViewById(R.id.title_txt);
            bodyTextView = itemView.findViewById(R.id.mBodyText);
            dateTextView = itemView.findViewById(R.id.textViewDate);
        }

        @Override
        public void onClick(View view) {

        }
    }
}
