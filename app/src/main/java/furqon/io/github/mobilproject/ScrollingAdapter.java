package furqon.io.github.mobilproject;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.viewpager.widget.PagerAdapter;

public class ScrollingAdapter extends PagerAdapter {
    private Context context;
    private LayoutInflater layoutInflater;


    private String[] titles;

    private String[] descriptionText;

    ScrollingAdapter(Context context){
        this.context = context;
        descriptionText = new String[]{
                this.context.getString(R.string.furqan_definition),
                this.context.getString(R.string.desc_ayah_of_the_day), this.context.getString(R.string.desc_comments), this.context.getString(R.string.desc_audio_seperate)
        };
        titles = new String[]{
          "", context.getString(R.string.title_daily_ayah), context.getString(R.string.title_color_comments), context.getString(R.string.title_separate_audio)
        };


    }

    @Override
    public int getCount() {
        return titles.length;
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view== object;
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
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
