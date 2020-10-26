package furqon.io.github.mobilproject;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;

public class MemorizeDownloadActivity extends AppCompatActivity {

    //TODO UI elements
    RecyclerView recyclerView;

    //TODO DATABASE connection


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_memorize_download);
    }
}