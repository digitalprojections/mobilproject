package furqon.io.github.mobilproject;


import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.view.View;
import android.widget.Button;
import android.widget.EditText;


public class MainActivity extends AppCompatActivity {
    public EditText name;
    public Button suralar_but;
    public Button davomi_but;
    public Button fav_but;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        suralar_but = findViewById(R.id.suralar);
        davomi_but = findViewById(R.id.davomi);
        fav_but = findViewById(R.id.favorites);

        suralar_but.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                openSuraNames(view);


                //create query

            }
        });
        fav_but.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openFavourites();
            }
        });



    }
    public void openSuraNames(View view){
        Intent intent = new Intent(this, SuraNameList.class);
        startActivity(intent);
    }
    public void openFavourites(){
        Intent intent = new Intent(this, Favourites.class);
        startActivity(intent);

    }

}
