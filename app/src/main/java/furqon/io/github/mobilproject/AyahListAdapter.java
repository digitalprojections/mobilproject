package furqon.io.github.mobilproject;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import static furqon.io.github.mobilproject.R.color.cardview_dark_background;
import static furqon.io.github.mobilproject.R.color.colorArabic;
import static furqon.io.github.mobilproject.R.drawable.*;
import static furqon.io.github.mobilproject.Settings.*;

public class AyahListAdapter extends RecyclerView.Adapter<AyahListAdapter.AyahListViewHolder> {
    private Context mContext;
    private Cursor mCursor;
    private ArrayList<String> mArrayList;
    private DatabaseAccess mDatabase;

    //DONE create share/boomark/favourite and add programmatically
    private ImageButton sharebut;
    private ImageButton favbut;
    private ImageButton bookbut;

    private String chaptername;//Sura nomi
    private String chapternumber;
    private String versenumber;//oyat nomeri
    private String ayahtext;//oyat matni
    private int ayah_position;


    SharedPreferences sharedPreferences;

    Typeface madina;

    private boolean sw_ar;
    private boolean sw_uz;
    private ViewGroup.LayoutParams lp; // Height of TextView
    private ViewGroup.LayoutParams lpmar; // Height of TextView
    private ViewGroup.LayoutParams lpartxt; // Height of TextView


    AyahListAdapter(Context context, Cursor cursor, String suraname, String chapter) {
        chapternumber = chapter;
        chaptername = suraname;
        mContext = context;
        mCursor = cursor;
        mDatabase = DatabaseAccess.getInstance(mContext);;
    }


    public class AyahListViewHolder extends RecyclerView.ViewHolder implements OnClickListener {
        TextView ayatext;
        TextView arabictext;
        TextView ayahnumber;
        TextView arabic_ayahnumber;
        TextView comment;


        LinearLayout linearLayout1;
        LinearLayout linearLayout2;
        LinearLayout linearLayout3;


        AyahListViewHolder(@NonNull View itemView) {
            super(itemView);

            linearLayout1 = itemView.findViewById(R.id.uzbektranslation);
            linearLayout2 = itemView.findViewById(R.id.arabictranslation);
            linearLayout3 = itemView.findViewById(R.id.actions);

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

            ayahnumber = itemView.findViewById(R.id.oyat_raqam);
            ayatext = itemView.findViewById(R.id.oyat_matn);

            ayatext.setOnClickListener(this);

            arabictext = itemView.findViewById(R.id.arab_txt);
            arabic_ayahnumber = itemView.findViewById(R.id.arab_num);

            madina = ResourcesCompat.getFont(mContext, R.font.maddina);

            ((LinearLayout.LayoutParams) lpmar).setMargins(1, -5, 1, 1);
            ((LinearLayout.LayoutParams) lp).setMargins(0, 0, 1, 1);
            ((LinearLayout.LayoutParams) lpartxt).setMargins(10, 0, 1, 1);
            //lpmar.width = 32;
            /*
            ayahnumber.setBackgroundResource(ic_ayahsymbolayahsymbol);
            ayatext.setLayoutParams(lp);
            ayatext.setTextSize(18);
            ayatext.setPadding(0, 5, 0, 5);
            */
            ayahnumber.setTextSize(15);
            ayahnumber.setLayoutParams(lp);
            ayahnumber.setGravity(Gravity.CENTER);
            ayatext.setVisibility(View.GONE);
            ayahnumber.setVisibility(View.GONE);
            arabictext.setLayoutParams(lpartxt);
            arabictext.setTextSize(30);
            arabictext.setGravity(Gravity.END | Gravity.RIGHT);
            arabictext.setTextColor(Color.BLACK);
            arabictext.setShadowLayer(1.5f, 0, 0, Color.BLACK);
            arabictext.setVisibility(View.GONE);
            arabictext.setTypeface(madina);
            bookbut.setTag("unselected");
            arabic_ayahnumber.setLayoutParams(lpmar);
            //arabic_ayahnumber.setBackgroundResource(ic_ayahsymbolayahsymbol);

            arabic_ayahnumber.setGravity(Gravity.CENTER);
            arabic_ayahnumber.setVisibility(View.GONE);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                arabictext.setTextDirection(View.TEXT_DIRECTION_ANY_RTL);
            }

            if (arabictext.getParent() != null) {
                ((ViewGroup) arabictext.getParent()).removeView(arabictext);
                ((ViewGroup) arabic_ayahnumber.getParent()).removeView(arabic_ayahnumber);
            }
            if (ayatext.getParent() != null) {
                ((ViewGroup) ayahnumber.getParent()).removeView(ayahnumber);
                ((ViewGroup) ayatext.getParent()).removeView(ayatext);
            }
            if (sharebut.getParent() != null) {
                ((ViewGroup) sharebut.getParent()).removeView(sharebut);
                ((ViewGroup) bookbut.getParent()).removeView(bookbut);
                ((ViewGroup) favbut.getParent()).removeView(favbut);
            }
            linearLayout1.addView(ayahnumber);
            linearLayout1.addView(ayatext);

            linearLayout2.addView(arabic_ayahnumber);
            linearLayout2.addView(arabictext);

            linearLayout3.addView(sharebut);
            linearLayout3.addView(bookbut);
            linearLayout3.addView(favbut);
            linearLayout3.setVisibility(View.GONE);
        }


        @Override
        public void onClick(View view) {
            int position = getAdapterPosition();
            Log.d("CLICK", ayahnumber.getText().toString());
            versenumber = ayahnumber.getText().toString();
            bookbut = ((ViewGroup) view.getParent()).findViewById(R.id.actions).findViewById(R.id.bookmarkbut);
            bookbut.setImageResource(R.drawable.ic_bookmark_border_black_24dp);

            if (linearLayout3.getVisibility() == View.GONE) {
                linearLayout3.setVisibility(View.VISIBLE);
                ayahtext = ayatext.getText().toString();
                ayah_position = sharedPreferences.getInt("xatchup" + chaptername, 0);
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

    public void takeAction(View view) {
        //Log.d("CLICK", view.toString());
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
                //TODO favourite add to sqlite
                //call the function
                favbut = ((ViewGroup) view.getParent()).findViewById(R.id.favouritebut);
                addToFavourites(view);
                break;
            case R.id.bookmarkbut:
                sharedPreferences = mContext.getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
                SharedPreferences.Editor editor;
                editor = sharedPreferences.edit();



                //recolor the bookmark
                bookbut = ((ViewGroup) view.getParent()).findViewById(R.id.bookmarkbut);
                if(bookbut.getTag().toString() == "unselected") {
                    bookbut.setImageResource(R.drawable.ic_turned_in_black_24dp);
                    bookbut.setTag("selected");
                    editor.putInt("xatchup" + chaptername, Integer.parseInt(versenumber));

                    editor.putString("xatchup", chaptername + ":"+chapternumber);
                    Log.i("BOOKMARK", bookbut.getTag().toString());
                }
                else {
                    bookbut.setImageResource(R.drawable.ic_bookmark_border_black_24dp);
                    bookbut.setTag("unselected");
                    editor.putInt("xatchup" + chaptername, 0);
                    editor.putInt("xatchup", 0);
                }
                editor.apply();
                break;
        }
    }

    private void addToFavourites(View view){
        //TODO manage sqlite creation and data addition
        Log.i("AYAT FAVOURITED", view.toString());
        if(mDatabase==null) {
            mDatabase.open();
        }
        if(favbut.getTag() == "1"){
            mDatabase.saveToFavs(chapternumber, versenumber, "0");
            favbut.setImageResource(R.drawable.ic_favorite_border_black_24dp);
            favbut.setTag("0");
        }else {
            mDatabase.saveToFavs(chapternumber, versenumber, "1");
            favbut.setImageResource(R.drawable.ic_favorite_black_24dp);
            favbut.setTag("1");
        }


    }

    @NonNull
    @Override
    public AyahListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View view = inflater.inflate(R.layout.ayat, parent, false);

        sharedPreferences = mContext.getSharedPreferences(Settings.SHARED_PREFS, MODE_PRIVATE);

        lp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, // Width of TextView
                ViewGroup.LayoutParams.WRAP_CONTENT);

        lpmar = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, // Width of TextView
                ViewGroup.LayoutParams.WRAP_CONTENT, 0.1f);
        lpartxt = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, // Width of TextView
                ViewGroup.LayoutParams.WRAP_CONTENT, 10.0f);
        loadData();

        mArrayList = new ArrayList<>();
        return new AyahListViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AyahListViewHolder holder, int i) {
        if (!mCursor.moveToPosition(i)) {
            return;
        }


        String ttext = mCursor.getString(1);
        String artext = mCursor.getString(0);
        String numb = mCursor.getString(2);
        String is_fav = mCursor.getString(4);
        versenumber = numb;


        holder.arabic_ayahnumber.setVisibility(View.VISIBLE);
        holder.arabictext.setVisibility(View.VISIBLE);
        if(is_fav =="1")
        {
            favbut = holder.linearLayout3.findViewById(R.id.favouritebut);
            favbut.setImageResource(R.drawable.ic_favorite_black_24dp);
            favbut.setTag("1");

        }else {
            favbut.setTag("0");
        }

        //holder.arabictext.setTextDirection(View.TEXT_DIRECTION_ANY_RTL);
        holder.arabictext.setGravity(Gravity.END | Gravity.RIGHT);

        holder.arabictext.setText(artext);
        holder.arabic_ayahnumber.setText(String.valueOf(numb));

        if (sw_uz) {
            holder.ayahnumber.setVisibility(View.VISIBLE);
            holder.ayatext.setVisibility(View.VISIBLE);
            holder.ayatext.setText(Html.fromHtml(collapseBraces(ttext)));
            holder.ayahnumber.setText(String.valueOf(numb));
        }
        Log.i("AYAT NUMBER", String.valueOf(numb));
        mArrayList.add(numb);

    }

    private String collapseBraces(String t) {
        String retval;

        if (t.indexOf("(") > 0) {
            //all logic here
            retval = t.replace("(", "<br><font color='#517D43'>");
            Log.i("ARRAY", String.valueOf(retval));
            retval = retval.replace(")", "</font>");
        } else {
            retval = t;
        }

        return retval;
    }

    @Override
    public int getItemCount() {
        return mCursor.getCount();
    }

    public void swapCursor(Cursor newCursor) {
        if (mCursor != null) {
            mCursor.close();
        }

        mCursor = newCursor;
        if (newCursor != null) {
            notifyDataSetChanged();
        }
    }

    private void loadData() {
        sw_ar = sharedPreferences.getBoolean(Settings.SWITCH1, false);
        sw_uz = sharedPreferences.getBoolean(Settings.SWITCH2, false);

        Log.i("SHARED DATA", String.valueOf(sw_ar));
        Log.i("SHARED DATA", String.valueOf(sw_uz));
    }

}
