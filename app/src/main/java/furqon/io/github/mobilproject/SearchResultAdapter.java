package furqon.io.github.mobilproject;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class SearchResultAdapter extends RecyclerView.Adapter<SearchResultAdapter.ViewHolder> {

    public static final String TAG = MemorizeActivity.class.getSimpleName();
    private Context mContext;
    //private Cursor mCursor;
    //private DatabaseAccess mDatabase;

    //DONE create share/boomark/favourite and add programmatically
    private ImageButton sharebut;
    private ImageButton fav_button;
    private ImageButton bookbut;

    private int position;
    private List<SearchResult> mText = new ArrayList<>();
    private String chaptername;//Sura nomi
    private String chapternumber;
    //private String versenumber;//oyat nomeri
    //private String ayahtext;//oyat matni
    //private int ayah_position;


    private Typeface madina;

    private ViewGroup.LayoutParams lp; // Height of TextView
    private ViewGroup.LayoutParams lpmar; // Height of TextView
    private ViewGroup.LayoutParams lpartxt; // Height of TextView
    private ViewGroup.LayoutParams toplayout; // Height of TextView
    private Animation scaler;
    private SharedPreferences sharedPref;
    private List<SearchResult> searchResults;

    SearchResultAdapter (Context context){
        sharedPref = SharedPreferences.getInstance();
        mContext = context;
        scaler = AnimationUtils.loadAnimation(mContext, R.anim.bounce);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return null;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

    }

    @Override
    public int getItemCount() {
        return 0;
    }

    @Override
    public void onViewDetachedFromWindow(@NonNull ViewHolder holder) {
        super.onViewDetachedFromWindow(holder);
    }

    public class ViewHolder  extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView ayatext;
        TextView ayah_text_ru;
        TextView ayah_text_en;
        TextView chapterTitle;
        TextView arabictext;
        TextView ayahnumber;
        TextView arabic_ayahnumber;
        TextView comment;


        LinearLayout linearLayout1;
        LinearLayout linearLayout2;
        LinearLayout linearLayout3;
        ConstraintLayout top;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            linearLayout1 = itemView.findViewById(R.id.uzbektranslation);
            linearLayout2 = itemView.findViewById(R.id.arabictranslation);
            linearLayout3 = itemView.findViewById(R.id.actions);
            top = itemView.findViewById(R.id.v_top);

            sharebut = itemView.findViewById(R.id.sharebut);
            fav_button = itemView.findViewById(R.id.favouritebut);
            bookbut = itemView.findViewById(R.id.bookmarkbut);

            sharebut.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    takeAction(view);
                }
            });
            fav_button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    takeAction(view);
                }
            });
            bookbut.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    takeAction(view);
                }
            });

            linearLayout2.setGravity(Gravity.END);

            chapterTitle = itemView.findViewById(R.id.v_title);



            ayahnumber = itemView.findViewById(R.id.oyat_raqam);
            ayatext = itemView.findViewById(R.id.oyat_matn);
            ayah_text_ru = itemView.findViewById(R.id.oyat_ru);
            ayah_text_en = itemView.findViewById(R.id.oyat_en);

            ayatext.setOnClickListener(this);
            ayah_text_ru.setOnClickListener(this);
            ayah_text_en.setOnClickListener(this);

            arabictext = itemView.findViewById(R.id.arab_txt);
            arabic_ayahnumber = itemView.findViewById(R.id.arab_num);

            if (sharedPref.contains(sharedPref.FONT)) {
                switch (sharedPref.read(sharedPref.FONT, "")) {
                    case "madina":
                        madina = ResourcesCompat.getFont(mContext, R.font.maddina);
                        break;
                    case "usmani":
                        madina = ResourcesCompat.getFont(mContext, R.font.al_uthmani);
                        break;
                    case "qalam":
                        madina = ResourcesCompat.getFont(mContext, R.font.al_qalam);
                        break;
                    default:
                        madina = ResourcesCompat.getFont(mContext, R.font.al_qalam);
                        break;
                }
            } else {
                madina = ResourcesCompat.getFont(mContext, R.font.al_qalam);
            }



            ((LinearLayout.LayoutParams) lpmar).setMargins(1, -5, 1, 1);
            ((LinearLayout.LayoutParams) lp).setMargins(0, 0, 1, 1);
            ((LinearLayout.LayoutParams) lpartxt).setMargins(10, 0, 1, 1);
            //lpmar.width = 32;
            /*


            ayah_number.setBackgroundResource(ic_ayahsymbolayahsymbol);


            ayah_text_uz.setLayoutParams(lp);
            ayah_text_uz.setTextSize(18);
            ayah_text_uz.setPadding(0, 5, 0, 5);
            */

            chapterTitle.setLayoutParams(toplayout);
            ayahnumber.setTextSize(15);
            ayahnumber.setLayoutParams(lp);
            ayahnumber.setGravity(Gravity.CENTER);
            ayatext.setVisibility(View.GONE);
            ayahnumber.setVisibility(View.GONE);
            ayah_text_ru.setVisibility(View.GONE);
            ayah_text_en.setVisibility(View.GONE);
            arabictext.setLayoutParams(lpartxt);
            arabictext.setTextSize(30);
            if (sharedPref.contains(sharedPref.FONTSIZE)) {
                float fs = (float) sharedPref.read(sharedPref.FONTSIZE, 0);
                arabictext.setTextSize(TypedValue.COMPLEX_UNIT_PX, fs);
            }
            arabictext.setGravity(Gravity.END);
            arabictext.setTextColor(Color.BLACK);
            //arabictext.setShadowLayer(1.5f, 0, 0, Color.BLACK);
            arabictext.setVisibility(View.GONE);
            arabictext.setTypeface(madina);
            bookbut.setTag("unselected");
            arabic_ayahnumber.setLayoutParams(lpmar);
            //arabic_ayah_number.setBackgroundResource(ic_ayahsymbolayahsymbol);

            arabic_ayahnumber.setGravity(Gravity.CENTER);
            arabic_ayahnumber.setVisibility(View.GONE);


            arabictext.setTextDirection(View.TEXT_DIRECTION_ANY_RTL);

            if (arabictext.getParent() != null) {
                ((ViewGroup) arabictext.getParent()).removeView(arabictext);
                ((ViewGroup) arabic_ayahnumber.getParent()).removeView(arabic_ayahnumber);
            }
            if(chapterTitle.getParent() != null){
                ((ViewGroup)chapterTitle.getParent()).removeView(chapterTitle);
            }
            if (ayatext.getParent() != null) {
                ((ViewGroup) ayahnumber.getParent()).removeView(ayahnumber);
                ((ViewGroup) ayatext.getParent()).removeView(ayatext);
            }
            if (ayah_text_ru.getParent() != null) {

                ((ViewGroup) ayah_text_ru.getParent()).removeView(ayah_text_ru);
                ((ViewGroup) ayah_text_en.getParent()).removeView(ayah_text_en);
            }
            if (sharebut.getParent() != null) {
                ((ViewGroup) sharebut.getParent()).removeView(sharebut);
                ((ViewGroup) bookbut.getParent()).removeView(bookbut);
                ((ViewGroup) fav_button.getParent()).removeView(fav_button);
            }

            top.addView(chapterTitle);

            linearLayout1.addView(ayahnumber);
            linearLayout1.addView(ayatext);
            linearLayout1.addView(ayah_text_ru);
            linearLayout1.addView(ayah_text_en);

            linearLayout2.addView(arabic_ayahnumber);
            linearLayout2.addView(arabictext);
            linearLayout2.setTag("0");


            linearLayout3.addView(sharebut);
            linearLayout3.addView(bookbut);
            linearLayout3.addView(fav_button);
            linearLayout3.setVisibility(View.GONE);
        }

        @Override
        public void onClick(View view) {
            position = getAdapterPosition();
            //versenumber = String.valueOf(ayahnumber.getText());
            //Log.d("CLICK", versenumber);
            //Log.d("FAV TAG", favbut.getTag());
            chapternumber = String.valueOf(ayahnumber.getTag());

            bookbut = ((ViewGroup) view.getParent()).findViewById(R.id.actions).findViewById(R.id.bookmarkbut);
            bookbut.setImageResource(R.drawable.ic_bookmark_border_black_24dp);

            if (linearLayout3.getVisibility() == View.GONE) {
                linearLayout3.setVisibility(View.VISIBLE);
                //ayahtext = String.valueOf(ayatext.getText());
                //ayah_position = sharedPref.read("xatchup" + chaptername, 0);
//                if (ayah_position == Integer.parseInt(versenumber)) {
//                    bookbut = ((ViewGroup) view.getParent()).findViewById(R.id.actions).findViewById(R.id.bookmarkbut);
//                    bookbut.setImageResource(R.drawable.ic_turned_in_black_24dp);
//                }
//
//                Log.d("verse number", versenumber + " " + ayah_position);
            } else {
                linearLayout3.setVisibility(View.GONE);
            }
        }
    }
    private void takeAction(View view) {

        LinearLayout ll = ((ViewGroup) view.getParent()).findViewById(R.id.uzbektranslation);

        switch (view.getId()) {
            case R.id.sharebut:
                Log.d("CLICK SHARE", view.toString());
                Intent sendIntent = new Intent();
                sendIntent.setAction(Intent.ACTION_SEND);
                //sendIntent.putExtra(Intent.EXTRA_TEXT, ayahtext + "\n(" + chaptername + ", " + versenumber + ")\nhttps://goo.gl/sXBkNt\nFurqon dasturi, Android");
                sendIntent.setType("text/plain");
                mContext.startActivity(Intent.createChooser(sendIntent, mContext.getResources().getText(R.string.shareayah)));
                break;
            case R.id.favouritebut:
                //favourite add to sqlite
                fav_button = ((ViewGroup) view.getParent()).findViewById(R.id.favouritebut);
                addToFavourites(view);
                fav_button.startAnimation(scaler);
                break;
            case R.id.bookmarkbut:
                //recolor the bookmark
                bookbut = ((ViewGroup) view.getParent()).findViewById(R.id.bookmarkbut);
                if(bookbut.getTag() == "unselected") {
                    bookbut.setImageResource(R.drawable.ic_turned_in_black_24dp);
                    bookbut.setTag("selected");
                    //sharedPref.write("xatchup" + chaptername, Integer.parseInt(versenumber));
                    //mDatabase.removeFromFavs(chapternumber, versenumber, "0");
                    Log.i("BOOKMARK", String.valueOf(bookbut.getTag()));
                }
                else {
                    //mDatabase.removeFromFavs(chapternumber, versenumber, "1");
                    bookbut.setImageResource(R.drawable.ic_bookmark_border_black_24dp);
                    bookbut.setTag("unselected");
                    sharedPref.write("xatchup" + chaptername, 0);
                }
                bookbut.startAnimation(scaler);
                break;
        }
    }
    private void addToFavourites(View view){
        // manage sqlite creation and data addition
        LinearLayout ll = (LinearLayout) view.getParent();
        //Log.i("AYAT FAVOURITED", String.valueOf());
        //fav_button = ((ViewGroup) view.getParent().getParent()).findViewById(R.id.favouritebut);
        fav_button = (ImageButton) view;
        ManageSpecials manageSpecials;



        if(mContext instanceof ManageSpecials) {
            manageSpecials = (ManageSpecials) mContext;
            SearchResult searchResult = getTextAt(Integer.parseInt(ll.getTag().toString()));
            if (fav_button.getTag() == "1") {
                //mDatabase.removeFromFavs(chapter_number, verse_number, "0");
                //fav_button.setImageResource(R.drawable.ic_favorite_border_black_24dp);
                fav_button.setTag("0");
                searchResult.favourite = 0;
            } else {
                //mDatabase.saveToFavs(chapter_number, verse_number, "1");
                //fav_button.setImageResource(R.drawable.ic_favorite_black_24dp);
                fav_button.setTag("1");
                searchResult.favourite = 1;
            }
            //ChapterTextTable text = MapTextObjects(searchResult);
            //manageSpecials.UpdateSpecialItem(text);
            notifyDataSetChanged();
            //mCursor = mDatabase.getSuraText(mCursor.getString(1));
        }
    }
    public void setResults(List<SearchResult> searchResults) {
        this.searchResults = searchResults;
        notifyDataSetChanged();
    }

    private SearchResult getTextAt(int position) {
        return searchResults.get(position);
    }

}
