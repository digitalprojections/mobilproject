package furqon.io.github.mobilproject;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;

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

        recyclerView = findViewById(R.id.message_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        listAdapter = new MessageListAdapter(this);
        recyclerView.setAdapter(listAdapter);

        messageViewModel = ViewModelProviders.of(this).get(TitleViewModel.class);
        messageViewModel.getMessages().observe(this, new Observer<List<MessageTable>>() {
            @Override
            public void onChanged(List<MessageTable> messageTables) {
                listAdapter.setItems(messageTables);
            }
        });

    }
}
