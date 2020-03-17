package furqon.io.github.mobilproject;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import java.util.List;

public class MessageList extends AppCompatActivity {

    private RecyclerView recyclerView;
    private MessageListAdapter listAdapter;
    private TitleViewModel messageViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message_list);
        //============================================

        Toolbar toolbar = findViewById(R.id.tool_bar);
        setSupportActionBar(toolbar);

        recyclerView = findViewById(R.id.message_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        listAdapter = new MessageListAdapter(this);
        recyclerView.setAdapter(listAdapter);

        messageViewModel = ViewModelProviders.of(this).get(TitleViewModel.class);
        messageViewModel.getMessages().observe(this, new Observer<List<MessageTable>>() {
            @Override
            public void onChanged(List<MessageTable> messageTables) {
                Log.e("MESSAGETABLE", messageTables.size() + " long");
                if(messageTables.size()>0){
                    listAdapter.setItems(messageTables);

                }

            }
        });

        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0,
                ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                messageViewModel.deleteMessage(listAdapter.getItemAt(viewHolder.getAdapterPosition()));
                Toast.makeText(MessageList.this, "Message has been deleted", Toast.LENGTH_SHORT).show();
            }
        }).attachToRecyclerView(recyclerView);

    }

    @Override
    protected void onPause() {
        super.onPause();
        messageViewModel.markAllAsRead();
    }
}
