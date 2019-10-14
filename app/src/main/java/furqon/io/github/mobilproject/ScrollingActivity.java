package furqon.io.github.mobilproject;

import android.content.Intent;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;

import android.util.Log;
import android.view.View;
import android.widget.Button;

import hotchemi.android.rate.AppRate;
import hotchemi.android.rate.OnClickButtonListener;

public class ScrollingActivity extends AppCompatActivity {
    private ScrollingAdapter scrollingAdapter;
    SharedPref sharedPref;
    ViewPager viewPager;
    TabLayout tabLayout;
    Button button_done;


    public ScrollingActivity() {
        sharedPref = SharedPref.getInstance();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scrolling);



        tabLayout = findViewById(R.id.tabLayout2);

        //sharedPref.setFirstRun(true);
        //findViewById(R.id.pager)
        viewPager = (ViewPager) findViewById(R.id.pager);

        scrollingAdapter = new ScrollingAdapter(this);
        viewPager.setAdapter(scrollingAdapter);

        button_done = findViewById(R.id.button_Done);
        button_done.setVisibility(View.INVISIBLE);
        View.OnClickListener clickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sharedPref.setFirstRun(false);
                Intent intent = new Intent(getApplicationContext(), AyahOfTheDay.class);
                startActivity(intent);
            }
        };
        button_done.setOnClickListener(clickListener);

        ViewPager.OnPageChangeListener onPageChangeListener = null;


        onPageChangeListener = new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                tabLayout.selectTab(tabLayout.getTabAt(position));
                if(position==tabLayout.getTabCount()-1){
                    button_done.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onPageSelected(int position) {

            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        };

        viewPager.addOnPageChangeListener(onPageChangeListener);
    }
}
