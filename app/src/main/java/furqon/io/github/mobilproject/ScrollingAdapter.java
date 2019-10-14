package furqon.io.github.mobilproject;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.viewpager.widget.PagerAdapter;

import com.google.android.material.tabs.TabLayout;

import java.util.Random;

public class ScrollingAdapter extends PagerAdapter {
    private Context context;
    private LayoutInflater layoutInflater;


    private String[] titles;

    private String[] descriptionText;

    public ScrollingAdapter(Context context){
        this.context = context;
        descriptionText = new String[]{
                this.context.getString(R.string.desc_ayah_of_the_day), this.context.getString(R.string.desc_comments), this.context.getString(R.string.desc_audio_seperate)
        };
        titles = new String[]{
          context.getString(R.string.title_daily_ayah), context.getString(R.string.title_color_comments), context.getString(R.string.title_separate_audio)
        };


    }

    @Override
    public int getCount() {
        return titles.length;
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view==(ConstraintLayout) object;
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        layoutInflater = (LayoutInflater) context.getSystemService(context.LAYOUT_INFLATER_SERVICE);
        View view = layoutInflater.inflate(R.layout.scroll_page, container,false);

        ImageView imageView = view.findViewById(R.id.screen_img);
        TextView textViewTitle = view.findViewById(R.id.screen_title);
        TextView textViewDesc = view.findViewById(R.id.screen_description);

        textViewTitle.setText(titles[position]);
        textViewDesc.setText(descriptionText[position]);

        container.addView(view);


        return view;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((ConstraintLayout)object);
    }
}
