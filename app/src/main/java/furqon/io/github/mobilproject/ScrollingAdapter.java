package furqon.io.github.mobilproject;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.viewpager.widget.PagerAdapter;

import static furqon.io.github.mobilproject.R.drawable.ic_asset_32furqon_logo;
import static furqon.io.github.mobilproject.R.drawable.ic_unlock;

public class ScrollingAdapter extends PagerAdapter {
    private Context context;
    private LayoutInflater layoutInflater;
    ImageView imageView;
    TextView textViewTitle;
    TextView textViewDesc;

    private String[] titles;
    private int[] images;

    private String[] descriptionText;

    ScrollingAdapter(Context context){
        this.context = context;
        descriptionText = new String[]{
                this.context.getString(R.string.about_text),
                this.context.getString(R.string.furqan_definition),
                this.context.getString(R.string.download_audio_description),
                this.context.getString(R.string.free_coin_awards),
                this.context.getString(R.string.desc_ayah_of_the_day),
                this.context.getString(R.string.desc_comments),
                this.context.getString(R.string.desc_audio_seperate)
        };
        titles = new String[]{
                context.getString(R.string.thankyou),
                context.getString(R.string.app_name),
                context.getString(R.string.coins),
                context.getString(R.string.best_user_awards),
                context.getString(R.string.title_daily_ayah),
                context.getString(R.string.title_color_comments),
                context.getString(R.string.title_separate_audio)
        };
        images = new int[]{
                R.mipmap.ic_launcher2_round,
                R.mipmap.ic_launcher2_round,
                R.mipmap.gold_coin,
                R.mipmap.gold_coin,
                R.mipmap.random,
                R.mipmap.rangli_comment,
                R.mipmap.sound_wave
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


        imageView = view.findViewById(R.id.screen_img);


        textViewTitle = view.findViewById(R.id.screen_title);

        textViewDesc = view.findViewById(R.id.screen_description);

        textViewTitle.setText(titles[position]);
        textViewDesc.setText(descriptionText[position]);
        imageView.setImageResource(images[position]);

        container.addView(view);


        return view;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((View) object);
    }
}
