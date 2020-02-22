package furqon.io.github.mobilproject;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;

public class ScrollingActivity extends AppCompatActivity {
    private ScrollingAdapter scrollingAdapter;
    sharedpref sharedPref;
    ViewPager viewPager;
    TabLayout tabLayout;
    private TabLayout.Tab tabItem;
    Button button_done;


    public ScrollingActivity() {
        sharedPref = sharedpref.getInstance();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scrolling);



        tabLayout = findViewById(R.id.tabLayout2);




        //sharedPref.setFirstRun(true);
        //findViewById(R.id.pager)
        viewPager = findViewById(R.id.pager);

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

        ViewPager.OnPageChangeListener onPageChangeListener;


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
