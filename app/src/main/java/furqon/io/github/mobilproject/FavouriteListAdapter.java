package furqon.io.github.mobilproject;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Typeface;
import android.text.Html;
import android.util.Log;
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
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;


public class FavouriteListAdapter extends RecyclerView.Adapter<FavouriteListAdapter.FavouriteListViewHolder> {
    private Context mContext;
    //private Cursor mCursor;
    private ArrayList<String> mArrayList;
    //private DatabaseAccess mDatabase;

    //DONE create share/boomark/favourite and add programmatically
    private ImageButton sharebut;
    private ImageButton favbut;
    private ImageButton bookbut;

    private int position;


    private String chaptername;//Sura nomi
    private String chapternumber;
    private String versenumber;//oyat nomeri
    private String ayahtext;//oyat matni
    private int ayah_position;


    private Typeface madina;


    private ViewGroup.LayoutParams lp; // Height of TextView
    private ViewGroup.LayoutParams lpmar; // Height of TextView
    private ViewGroup.LayoutParams lpartxt; // Height of TextView
    private ViewGroup.LayoutParams toplayout; // Height of TextView
    private Animation scaler;
    private sharedpref sharedPref;

    FavouriteListAdapter(Context context, String suraname, String chapter) {
        sharedPref = sharedpref.getInstance();
        chapternumber = chapter;

        mContext = context;
//        mCursor = cursor;
//        mDatabase = DatabaseAccess.getInstance(mContext);
        scaler = AnimationUtils.loadAnimation(mContext, R.anim.bounce);

    }


    public class FavouriteListViewHolder extends RecyclerView.ViewHolder implements OnClickListener {
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


        FavouriteListViewHolder(@NonNull View itemView) {
            super(itemView);

            linearLayout1 = itemView.findViewById(R.id.uzbektranslation);
            linearLayout2 = itemView.findViewById(R.id.arabictranslation);
            linearLayout3 = itemView.findViewById(R.id.actions);
            top = itemView.findViewById(R.id.v_top);

            sharebut = itemView.findViewById(R.id.sharebut);
            favbut = itemView.findViewById(R.id.favouritebut);
            bookbut = itemView.findViewById(R.id.bookmarkbut);

            sharebut.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    takeAction(view);
                }
            });
            favbut.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    takeAction(view);
                }
            });
            bookbut.setOnClickListener(new OnClickListener() {
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

            madina = ResourcesCompat.getFont(mContext, R.font.maddina);


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
            arabictext.setGravity(Gravity.END);
            arabictext.setTextColor(Color.BLACK);
            arabictext.setShadowLayer(1.5f, 0, 0, Color.BLACK);
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
                ((ViewGroup) favbut.getParent()).removeView(favbut);
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
            linearLayout3.addView(favbut);
            linearLayout3.setVisibility(View.GONE);
        }


        @Override
        public void onClick(View view) {
            position = getAdapterPosition();
            versenumber = String.valueOf(ayahnumber.getText());
            Log.d("CLICK", versenumber);
            //Log.d("FAV TAG", favbut.getTag());
            chapternumber = String.valueOf(ayahnumber.getTag());

            bookbut = ((ViewGroup) view.getParent()).findViewById(R.id.actions).findViewById(R.id.bookmarkbut);
            bookbut.setImageResource(R.drawable.ic_bookmark_border_black_24dp);

            if (linearLayout3.getVisibility() == View.GONE) {
                linearLayout3.setVisibility(View.VISIBLE);
                ayahtext = String.valueOf(ayatext.getText());
                ayah_position = sharedPref.read("xatchup" + chaptername, 0);
                if (ayah_position == Integer.parseInt(versenumber)) {
                    bookbut = ((ViewGroup) view.getParent()).findViewById(R.id.actions).findViewById(R.id.bookmarkbut);
                    bookbut.setImageResource(R.drawable.ic_turned_in_black_24dp);
                }

                Log.d("verse number", versenumber + " " + ayah_position);
            } else {
                linearLayout3.setVisibility(View.GONE);
            }

        }
    }

    private void takeAction(View view) {

        LinearLayout ll = ((ViewGroup) view.getParent()).findViewById(R.id.uzbektranslation);

        switch (view.getId()) {
            case R.id.sharebut:
                Log.d("CLICK SHARE", ayahtext);
                Intent sendIntent = new Intent();
                sendIntent.setAction(Intent.ACTION_SEND);
                sendIntent.putExtra(Intent.EXTRA_TEXT, ayahtext + "\n(" + chaptername + ", " + versenumber + ")\nhttps://goo.gl/sXBkNt\nFurqon dasturi, Android");
                sendIntent.setType("text/plain");
                mContext.startActivity(Intent.createChooser(sendIntent, mContext.getResources().getText(R.string.shareayah)));
                break;
            case R.id.favouritebut:
                //favourite add to sqlite
                favbut = ((ViewGroup) view.getParent()).findViewById(R.id.favouritebut);
                addToFavourites(view);
                favbut.startAnimation(scaler);
                break;
            case R.id.bookmarkbut:




                //recolor the bookmark
                bookbut = ((ViewGroup) view.getParent()).findViewById(R.id.bookmarkbut);
                if(bookbut.getTag() == "unselected") {
                    bookbut.setImageResource(R.drawable.ic_turned_in_black_24dp);
                    bookbut.setTag("selected");
                    sharedPref.write("xatchup" + chaptername, Integer.parseInt(versenumber));
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
        //TODO manage sqlite creation and data addition
        Log.i("AYAT FAVOURITED", String.valueOf(view));

            if (favbut.getTag() == "1") {
                //mDatabase.removeFromFavs(chapternumber, versenumber, "0");
                favbut.setImageResource(R.drawable.ic_favorite_border_black_24dp);
                favbut.setTag("0");
                //mCursor = mDatabase.loadFavourites();
            }
            notifyItemRemoved(position);
            notifyDataSetChanged();
    }

    @NonNull
    @Override
    public FavouriteListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View view = inflater.inflate(R.layout.fav_verse, parent, false);



        lp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, // Width of TextView
                ViewGroup.LayoutParams.WRAP_CONTENT);

        lpmar = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, // Width of TextView
                ViewGroup.LayoutParams.WRAP_CONTENT, 0.1f);
        lpartxt = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, // Width of TextView
                ViewGroup.LayoutParams.WRAP_CONTENT, 10.0f);
        toplayout = new ConstraintLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );

        mArrayList = new ArrayList<>();
        return new FavouriteListViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FavouriteListViewHolder holder, int i) {

//        chapternumber = mCursor.getString(1);
        String numb = "";
        String artext = "";
        String ttext = "";
        String rtext = "";
        String etext = "";

        int is_fav = 0;
        chaptername = "";
        versenumber = numb;


        //Log.i("TAG FAVOURITE", String.valueOf(is_fav==1));
        if (sharedPref.getDefaults("ar")) {
            holder.arabic_ayahnumber.setVisibility(View.VISIBLE);
            holder.arabictext.setVisibility(View.VISIBLE);
        }
        if(is_fav ==1)
        {
            favbut = holder.linearLayout3.findViewById(R.id.favouritebut);
            favbut.setImageResource(R.drawable.ic_favorite_black_24dp);
            favbut.setTag("1");

        }else {
            favbut.setTag("0");
        }

        holder.chapterTitle.setText(chaptername);
        //holder.arabic_text.setTextDirection(View.TEXT_DIRECTION_ANY_RTL);
        holder.arabictext.setGravity(Gravity.END);

        holder.arabictext.setText(artext);
        holder.arabic_ayahnumber.setText(String.valueOf(numb));

        if (sharedPref.getDefaults("uz")) {
            //holder.ayah_number.setVisibility(View.VISIBLE);
            holder.ayatext.setVisibility(View.VISIBLE);
            holder.ayatext.setText(Html.fromHtml(collapseBraces(ttext)));
            holder.ayahnumber.setText(String.valueOf(numb));

            holder.ayahnumber.setTag(chapternumber);
        }
        if (sharedPref.getDefaults("ru")) {
            holder.ayah_text_ru.setVisibility(View.VISIBLE);
            holder.ayah_text_ru.setText(Html.fromHtml(collapseBraces(rtext)));
            holder.ayahnumber.setText(String.valueOf(numb));
            holder.ayahnumber.setTag(chapternumber);
        }
        if (sharedPref.getDefaults("en")) {
            holder.ayah_text_en.setVisibility(View.VISIBLE);
            holder.ayah_text_en.setText(Html.fromHtml(collapseBraces(etext)));
            holder.ayahnumber.setText(String.valueOf(numb));
            holder.ayahnumber.setTag(chapternumber);
        }
        Log.i("SURANAME", String.valueOf(chaptername));
        mArrayList.add(numb);



    }

    private String collapseBraces(String t) {
        String retval;

        if (t.indexOf("(") > 0) {
            //all logic here
            retval = t.replace("(", "<br><font color='#517D43'>");
            Log.i("ARRAY", retval);
            retval = retval.replace(")", "</font>");
        } else {
            retval = t;
        }

        return retval;
    }

    @Override
    public int getItemCount() {
        int c = 0;


        return c;
    }
}
