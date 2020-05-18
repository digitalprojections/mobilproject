package furqon.io.github.mobilproject;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;

public class ScrollingActivity extends AppCompatActivity {
    ScrollingAdapter scrollingAdapter;
    SharedPreferences sharedPref;
    ViewPager viewPager;
    TabLayout tabLayout;
    private TabLayout.Tab tabItem;
    CheckBox dontshow;
    Button button_done;


    public ScrollingActivity() {
        sharedPref = SharedPreferences.getInstance();
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

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        dontshow = findViewById(R.id.dontshowagain_checkBox);

        button_done = findViewById(R.id.button_Done);
        button_done.setVisibility(View.INVISIBLE);
        View.OnClickListener clickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(dontshow.isChecked()){
                    sharedPref.setFirstRun(false);
                }
                //Intent intent = new Intent(getApplicationContext(), AyahOfTheDay.class);
                //startActivity(intent);
                ScrollingActivity.super.onBackPressed();
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
