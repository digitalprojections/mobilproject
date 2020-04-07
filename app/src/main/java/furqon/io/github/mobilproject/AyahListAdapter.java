package furqon.io.github.mobilproject;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Build;
import android.text.Html;
import android.text.SpannableStringBuilder;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class AyahListAdapter extends RecyclerView.Adapter<AyahListAdapter.AyahListViewHolder> {
    private static final String TAG = "AYAHLISTADAPTER";
    private final SharedPreferences sharedPref;
    //private final Animation ayah_close_anim;
    //private final Animation ayah_open_anim;
    private Context mContext;
    //private Cursor mCursor;
    private ArrayList<String> mArrayList;
    //private DatabaseAccess mDatabase;

    private List<AllTranslations> mText = new ArrayList<>();

    //DONE create share/boomark/favourite and add programmatically
    ImageButton share_button;
    ImageButton fav_button;
    ImageButton book_button;

    private String chaptername;//Sura nomi
    private String chapter_number;
    private String verse_number;//oyat nomeri
    private String ayah_txt_uz;//oyat matni uzbek


    SpannableStringBuilder ssb;

    int ayah_position;


    Typeface madina;

    private ViewGroup.LayoutParams lp; // Height of TextView
    private ViewGroup.LayoutParams lpmar; // Height of TextView
    private ViewGroup.LayoutParams lpartxt; // Height of TextView
    private Animation scaler;


    @Override
    public void onViewDetachedFromWindow(@NonNull AyahListViewHolder holder) {
        super.onViewDetachedFromWindow(holder);
        //Log.i(TAG, String.valueOf(this));

    }

    AyahListAdapter(Context context, String suraname, String chapter) {
        sharedPref = SharedPreferences.getInstance();

        chapter_number = chapter;
        chaptername = suraname;
        mContext = context;
        scaler = AnimationUtils.loadAnimation(mContext, R.anim.bounce);

        ssb = new SpannableStringBuilder();
        ssb.clear();


        //ayah_open_anim = AnimationUtils.loadAnimation(mContext, R.anim.fab_open);
        //ayah_close_anim = AnimationUtils.loadAnimation(mContext, R.anim.fab_close);


    }


    public class AyahListViewHolder extends RecyclerView.ViewHolder implements OnClickListener {
        TextView ayah_text_uz;
        TextView ayah_text_ru;
        TextView ayah_text_en;
        TextView arabic_text;
        TextView ayah_number;
        TextView arabic_ayah_number;
        TextView comment;

        LinearLayout uzbek_text_lin_layout;
        LinearLayout ruen_text_lin_layout;
        LinearLayout arabic_text_lin_layout;
        LinearLayout actions_lin_layout;


        AyahListViewHolder(@NonNull View itemView) {
            super(itemView);

            arabic_text_lin_layout = itemView.findViewById(R.id.v_arabictranslation);
            uzbek_text_lin_layout = itemView.findViewById(R.id.uzbektranslation);
            ruen_text_lin_layout = itemView.findViewById(R.id.landscaper);
            actions_lin_layout = itemView.findViewById(R.id.actions);

            share_button = itemView.findViewById(R.id.f_sharebut);
            fav_button = itemView.findViewById(R.id.favouritebut);
            book_button = itemView.findViewById(R.id.f_bookmarkbut);

            share_button.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    takeAction(view);
                }
            });
            fav_button.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    takeAction(view);
                }
            });
            book_button.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    takeAction(view);
                }
            });

            arabic_text_lin_layout.setGravity(Gravity.END);

            ayah_number = itemView.findViewById(R.id.oyat_raqam);
            ayah_text_uz = itemView.findViewById(R.id.oyat_matn);
            ayah_text_ru = itemView.findViewById(R.id.oyat_ru);
            ayah_text_en = itemView.findViewById(R.id.oyat_en);

            ayah_text_uz.setOnClickListener(this);
            ayah_text_ru.setOnClickListener(this);
            ayah_text_en.setOnClickListener(this);

            arabic_text = itemView.findViewById(R.id.arab_txt);
            arabic_ayah_number = itemView.findViewById(R.id.arab_num);

            madina = ResourcesCompat.getFont(mContext, R.font.maddina);

            ((LinearLayout.LayoutParams) lpmar).setMargins(5, 5, 5, 5);
            ((LinearLayout.LayoutParams) lp).setMargins(0, 0, 1, 1);
            ((LinearLayout.LayoutParams) lpartxt).setMargins(10, 0, 1, 1);

            ayah_number.setTextSize(20);
            ayah_number.setLayoutParams(lp);
            ayah_number.setGravity(Gravity.CENTER);
            ayah_text_uz.setVisibility(View.GONE);
            ayah_text_ru.setVisibility(View.GONE);
            ayah_text_en.setVisibility(View.GONE);
            ayah_number.setVisibility(View.GONE);
            arabic_text.setVisibility(View.GONE);
            arabic_text.setLayoutParams(lpartxt);
            arabic_text.setTextSize(30);
            arabic_text.setGravity(Gravity.END);
            arabic_text.setTextColor(Color.BLACK);
            //arabic_text.setShadowLayer(1.5f, 0, 0, Color.BLACK);

            arabic_text.setTypeface(madina);
            book_button.setTag("unselected");
            arabic_ayah_number.setLayoutParams(lpmar);
            //arabic_ayah_number.setBackgroundResource(ic_ayahsymbolayahsymbol);

            arabic_ayah_number.setGravity(Gravity.CENTER);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                arabic_text.setTextDirection(View.TEXT_DIRECTION_ANY_RTL);
            }

            if (arabic_text.getParent() != null) {
                ((ViewGroup) arabic_text.getParent()).removeView(arabic_text);
                ((ViewGroup) arabic_ayah_number.getParent()).removeView(arabic_ayah_number);
            }
            if (ayah_text_uz.getParent() != null) {
                ((ViewGroup) ayah_number.getParent()).removeView(ayah_number);
                ((ViewGroup) ayah_text_uz.getParent()).removeView(ayah_text_uz);

            }
            if (ayah_text_ru.getParent() != null) {

                ((ViewGroup) ayah_text_ru.getParent()).removeView(ayah_text_ru);
                ((ViewGroup) ayah_text_en.getParent()).removeView(ayah_text_en);
            }
            if (share_button.getParent() != null) {
                ((ViewGroup) share_button.getParent()).removeView(share_button);
                ((ViewGroup) book_button.getParent()).removeView(book_button);
                ((ViewGroup) fav_button.getParent()).removeView(fav_button);
            }
            uzbek_text_lin_layout.addView(ayah_number);
            uzbek_text_lin_layout.addView(ayah_text_uz);

            ruen_text_lin_layout.addView(ayah_text_ru);
            ruen_text_lin_layout.addView(ayah_text_en);

            arabic_text_lin_layout.addView(arabic_ayah_number);
            arabic_text_lin_layout.addView(arabic_text);
            if ((sharedPref.getDefaults("uz") || sharedPref.getDefaults("ru") || sharedPref.getDefaults("en")) && !sharedPref.getDefaults("ar")) {
                ayah_number.setVisibility(View.VISIBLE);
                arabic_text_lin_layout.setVisibility(View.GONE);
            }


            actions_lin_layout.addView(share_button);
            actions_lin_layout.addView(book_button);
            actions_lin_layout.addView(fav_button);
            actions_lin_layout.setVisibility(View.GONE);
            actions_lin_layout.setScaleY(0);
        }


        @Override
        public void onClick(View view) {
            int position = getAdapterPosition();
            //Log.d("CLICK", ayah_number.getText() + " position: " + position);
            verse_number = String.valueOf(ayah_number.getText());
            book_button = ((ViewGroup) view.getParent().getParent()).findViewById(R.id.actions).findViewById(R.id.f_bookmarkbut);
            //book_button = view;
            book_button.setImageResource(R.drawable.ic_bookmark_border_black_24dp);



            if (actions_lin_layout.getVisibility()==View.GONE) {
                actions_lin_layout.setVisibility(View.VISIBLE);
                actions_lin_layout.setScaleY(1);
                //actions_lin_layout.setAnimation(ayah_open_anim);
                ayah_txt_uz = String.valueOf(ayah_text_uz.getText());
                ayah_position = sharedPref.read("xatchup" + chaptername, 0);
                if (ayah_position == Integer.parseInt(verse_number)) {
                    //book_button = ((ViewGroup) view.getParent().getParent()).findViewById(R.id.actions).findViewById(R.id.f_bookmarkbut);
                    //book_button = (ImageButton) view;
                    book_button.setImageResource(R.drawable.ic_turned_in_black_24dp);
                    book_button.setTag("selected");
                }

                //Log.d("verse number", verse_number + " " + ayah_position);
            } else {
                //actions_lin_layout.setAnimation(ayah_close_anim);
                actions_lin_layout.setVisibility(View.GONE);
                actions_lin_layout.setScaleY(0);
            }

        }
    }

    private void takeAction(View view) {

        switch (view.getId()) {
            case R.id.f_sharebut:
                Intent sendIntent = new Intent();
                sendIntent.setAction(Intent.ACTION_SEND);
                sendIntent.putExtra(Intent.EXTRA_TEXT, ayah_txt_uz + "\n(" + chaptername + ", " + verse_number + ")\nhttps://goo.gl/sXBkNt\nFurqon dasturi, Android");
                sendIntent.setType("text/plain");
                mContext.startActivity(Intent.createChooser(sendIntent, mContext.getResources().getText(R.string.shareayah)));

                break;
            case R.id.favouritebut:
                fav_button = ((ViewGroup) view.getParent().getParent()).findViewById(R.id.favouritebut);
                addToFavourites(view);
                fav_button.startAnimation(scaler);
                break;
            case R.id.f_bookmarkbut:
                book_button = ((ViewGroup) view.getParent().getParent()).findViewById(R.id.f_bookmarkbut);
                if (book_button.getTag() == "unselected") {
                    book_button.setImageResource(R.drawable.ic_turned_in_black_24dp);
                    book_button.setTag("selected");
                    sharedPref.write("xatchup" + chaptername, Integer.parseInt(verse_number));
                    sharedPref.write("xatchup", chaptername + ":" + chapter_number);
                } else {
                    book_button.setImageResource(R.drawable.ic_bookmark_border_black_24dp);
                    book_button.setTag("unselected");
                    sharedPref.write("xatchup" + chaptername, 0);
                    sharedPref.write("xatchup", "");
                }
                book_button.startAnimation(scaler);
                break;
        }
    }

    private void addToFavourites(View view) {
        fav_button = (ImageButton) view;
        ManageSpecials manageSpecials;
        if(mContext instanceof ManageSpecials) {
            manageSpecials = (ManageSpecials) mContext;
            AllTranslations allTranslations = getTextAt(Integer.parseInt(verse_number) - 1);
            if (fav_button.getTag() == "1") {
                fav_button.setTag("0");
                allTranslations.favourite = 0;
            } else {
                fav_button.setTag("1");
                allTranslations.favourite = 1;
            }
            ChapterTextTable text = MapTextObjects(allTranslations);
            manageSpecials.UpdateSpecialItem(text);
            notifyDataSetChanged();
        }
    }

    private ChapterTextTable MapTextObjects(AllTranslations allTranslations) {
        ChapterTextTable ctext = new ChapterTextTable(allTranslations.sura_id, allTranslations.verse_id, allTranslations.favourite, 1, allTranslations.order_no, allTranslations.ar_text, allTranslations.comments_text, allTranslations.surah_type);
        ctext.setId(allTranslations.id);
        return ctext;
    }


    @NonNull
    @Override
    public AyahListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View view = inflater.inflate(R.layout.ayat, parent, false);
        lp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, // Width of TextView
                ViewGroup.LayoutParams.WRAP_CONTENT);
        lpmar = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, // Width of TextView
                ViewGroup.LayoutParams.WRAP_CONTENT, 0.1f);
        lpartxt = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, // Width of TextView
                ViewGroup.LayoutParams.WRAP_CONTENT, 10.0f);
        mArrayList = new ArrayList<>();
        return new AyahListViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AyahListViewHolder holder, int i) {
        AllTranslations current = mText.get(i);

        String en_text;
        String ru_text;
        String uz_text;
        String ar_text;
        String numb;


        en_text = current.en_text;
        ru_text = current.ru_text;
        uz_text = current.uz_text;
        ar_text = current.ar_text;

        numb = String.valueOf(current.verse_id);
        int is_fav = current.favourite;
        verse_number = numb;
        fav_button = holder.actions_lin_layout.findViewById(R.id.favouritebut);
        fav_button.setImageResource(R.drawable.ic_favorite_border_black_24dp);
        fav_button.setTag("0");
        book_button.setImageResource(R.drawable.ic_bookmark_border_black_24dp);
        book_button.setTag("unselected");
        //Log.i("TAG FAVOURITE AYAH", numb + " " + is_fav + " " + current.favourite + " " + current.sura_id + " ");
        if (is_fav == 1) {
            fav_button.setImageResource(R.drawable.ic_favorite_black_24dp);
            fav_button.setTag("1");
            //Log.i("FAVOURITE AYAH ****** ", numb + " " + is_fav);
        }

        if(current.favourite==0){
            holder.actions_lin_layout.setVisibility(View.GONE);
        }else{
            holder.actions_lin_layout.setVisibility(View.VISIBLE);
        }


        //holder.arabic_text.setTextDirection(View.TEXT_DIRECTION_ANY_RTL);

        if (sharedPref.getDefaults("ar")) {
            holder.arabic_text.setGravity(Gravity.END);
            holder.arabic_text.setText(ar_text);
            holder.arabic_ayah_number.setText(String.valueOf(numb));
            holder.arabic_ayah_number.setVisibility(View.VISIBLE);
            holder.arabic_text.setVisibility(View.VISIBLE);
            holder.ayah_number.setVisibility(View.GONE);
        }else{
            holder.arabic_ayah_number.setVisibility(View.GONE);
            holder.arabic_text.setVisibility(View.GONE);
            holder.ayah_number.setVisibility(View.VISIBLE);
        }
        if (sharedPref.getDefaults("uz")) {
            //holder.ayah_number.setVisibility(View.VISIBLE);
            holder.ayah_text_uz.setVisibility(View.VISIBLE);
            holder.ayah_text_uz.setText(Html.fromHtml(collapseBraces(uz_text)));
            holder.ayah_number.setText(String.valueOf(numb));
        }else{
            holder.ayah_text_uz.setVisibility(View.GONE);
        }
        if (sharedPref.getDefaults("ru")) {
            holder.ayah_text_ru.setVisibility(View.VISIBLE);
            holder.ayah_text_ru.setText(Html.fromHtml(collapseBraces(ru_text)));
            holder.ayah_number.setText(String.valueOf(numb));
        }else{
            holder.ayah_text_ru.setVisibility(View.GONE);
        }
        if (sharedPref.getDefaults("en")) {
            holder.ayah_text_en.setVisibility(View.VISIBLE);
            holder.ayah_text_en.setText(Html.fromHtml(collapseBraces(en_text)));
            holder.ayah_number.setText(String.valueOf(numb));
        }else{
            holder.ayah_text_en.setVisibility(View.GONE);
        }
        //Log.i("AYAT NUMBER", String.valueOf(numb));
        mArrayList.add(numb);

    }
    void setText(List<AllTranslations> text){
        mText = text;
        notifyDataSetChanged();
    }
    private String collapseBraces(String t) {
        String retval;

        if(t!=null){
            if (t.indexOf("(") > 0) {
                //all logic here
                retval = t.replace("(", "<br><font color='#517D43'>");
                //Log.i("ARRAY", retval);
                retval = retval.replace(")", "</font>");

            } else {
                retval = t;
            }
        }else{
            retval = "";
        }


        return retval;
    }

    public AllTranslations getTextAt(int position){


        return mText.get(position);
    }


    @Override
    public int getItemCount() {
        int c = 0;
        if(mText!=null)
        {
            c = mText.size();
        }
        return c;
    }
}
