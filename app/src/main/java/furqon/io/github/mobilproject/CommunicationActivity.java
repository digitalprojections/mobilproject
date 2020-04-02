package furqon.io.github.mobilproject;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class CommunicationActivity extends AppCompatActivity {

    TextView message_txt;
    Button send;
    RecyclerView message_rv;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_communication);

        message_rv = findViewById(R.id.communication_rv);
        message_txt = findViewById(R.id.message_to_author_et);
        send = findViewById(R.id.send_btn);

        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
    }
}
