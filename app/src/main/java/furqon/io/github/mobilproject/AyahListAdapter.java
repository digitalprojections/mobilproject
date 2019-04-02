package furqon.io.github.mobilproject;

import android.content.Context;
import android.content.Intent;
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

public class AyahListAdapter extends RecyclerView.Adapter<AyahListAdapter.AyahListViewHolder> {
    private Context mContext;
    private Cursor mCursor;
    private ArrayList<String> mArrayList;
    private DatabaseAccess mDatabase;

    //DONE create share/boomark/favourite and add programmatically
    private ImageButton share_button;
    private ImageButton fav_button;
    private ImageButton book_button;

    private String chaptername;//Sura nomi
    private String chapter_number;
    private String verse_number;//oyat nomeri
    private String ayah_txt_uz;//oyat matni uzbek
    private String ru_text;//oyat matni uzbek
    private String en_text;//oyat matni uzbek

    private int ayah_position;


    Typeface madina;

    private ViewGroup.LayoutParams lp; // Height of TextView
    private ViewGroup.LayoutParams lpmar; // Height of TextView
    private ViewGroup.LayoutParams lpartxt; // Height of TextView




    AyahListAdapter(Context context, Cursor cursor, String suraname, String chapter) {
        chapter_number = chapter;
        chaptername = suraname;
        mContext = context;
        mCursor = cursor;
        mDatabase = DatabaseAccess.getInstance(mContext);;
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
        LinearLayout arabic_text_lin_layout;
        LinearLayout actions_lin_layout;


        AyahListViewHolder(@NonNull View itemView) {
            super(itemView);

            arabic_text_lin_layout = itemView.findViewById(R.id.arabictranslation);
            uzbek_text_lin_layout = itemView.findViewById(R.id.uzbektranslation);
            actions_lin_layout = itemView.findViewById(R.id.actions);

            share_button = itemView.findViewById(R.id.sharebut);
            fav_button = itemView.findViewById(R.id.favouritebut);
            book_button = itemView.findViewById(R.id.bookmarkbut);

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
            //lpmar.width = 32;
            /*
            ayah_number.setBackgroundResource(ic_ayahsymbolayahsymbol);
            ayah_text_uz.setLayoutParams(lp);
            ayah_text_uz.setTextSize(18);
            ayah_text_uz.setPadding(0, 5, 0, 5);
            */
            ayah_number.setTextSize(20);
            ayah_number.setLayoutParams(lp);
            ayah_number.setGravity(Gravity.CENTER);
            ayah_text_uz.setVisibility(View.GONE);
            ayah_text_ru.setVisibility(View.GONE);
            ayah_text_en.setVisibility(View.GONE);
            ayah_number.setVisibility(View.GONE);
            arabic_text.setLayoutParams(lpartxt);
            arabic_text.setTextSize(30);
            arabic_text.setGravity(Gravity.END | Gravity.RIGHT);
            arabic_text.setTextColor(Color.BLACK);
            arabic_text.setShadowLayer(1.5f, 0, 0, Color.BLACK);
            arabic_text.setVisibility(View.GONE);
            arabic_text.setTypeface(madina);
            book_button.setTag("unselected");
            arabic_ayah_number.setLayoutParams(lpmar);
            //arabic_ayah_number.setBackgroundResource(ic_ayahsymbolayahsymbol);

            arabic_ayah_number.setGravity(Gravity.CENTER);
            arabic_ayah_number.setVisibility(View.GONE);

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
            uzbek_text_lin_layout.addView(ayah_text_ru);
            uzbek_text_lin_layout.addView(ayah_text_en);

            arabic_text_lin_layout.addView(arabic_ayah_number);
            arabic_text_lin_layout.addView(arabic_text);
            if(SharedPref.getDefaults("uz") || SharedPref.getDefaults("ru") || SharedPref.getDefaults("en")){

            }else {
                arabic_text_lin_layout.setVisibility(View.GONE);
            }


            actions_lin_layout.addView(share_button);
            actions_lin_layout.addView(book_button);
            actions_lin_layout.addView(fav_button);
            actions_lin_layout.setVisibility(View.GONE);
        }


        @Override
        public void onClick(View view) {
            int position = getAdapterPosition();
            Log.d("CLICK", ayah_number.getText().toString());
            verse_number = ayah_number.getText().toString();
            book_button = ((ViewGroup) view.getParent()).findViewById(R.id.actions).findViewById(R.id.bookmarkbut);
            book_button.setImageResource(R.drawable.ic_bookmark_border_black_24dp);

            if (actions_lin_layout.getVisibility() == View.GONE) {
                actions_lin_layout.setVisibility(View.VISIBLE);
                ayah_txt_uz = ayah_text_uz.getText().toString();
                ayah_position = SharedPref.read("xatchup" + chaptername, 0);
                if (ayah_position == Integer.parseInt(verse_number)) {
                    book_button = ((ViewGroup) view.getParent()).findViewById(R.id.actions).findViewById(R.id.bookmarkbut);
                    book_button.setImageResource(R.drawable.ic_turned_in_black_24dp);
                }

                Log.d("verse number", verse_number + " " + ayah_position);
            } else {
                actions_lin_layout.setVisibility(View.GONE);
            }

        }
    }

    public void takeAction(View view) {
        //Log.d("CLICK", view.toString());
        LinearLayout ll = ((ViewGroup) view.getParent()).findViewById(R.id.uzbektranslation);

        switch (view.getId()) {
            case R.id.sharebut:
                Log.d("CLICK SHARE", ayah_txt_uz);
                Intent sendIntent = new Intent();
                sendIntent.setAction(Intent.ACTION_SEND);
                sendIntent.putExtra(Intent.EXTRA_TEXT, ayah_txt_uz + "\n(" + chaptername + ", " + verse_number + ")\nhttps://goo.gl/sXBkNt\nFurqon dasturi, Android");
                sendIntent.setType("text/plain");
                mContext.startActivity(Intent.createChooser(sendIntent, mContext.getResources().getText(R.string.shareayah)));
                break;
            case R.id.favouritebut:
                // favourite add to sqlite
                //call the function
                fav_button = ((ViewGroup) view.getParent()).findViewById(R.id.favouritebut);
                addToFavourites(view);
                break;
            case R.id.bookmarkbut:



                //recolor the bookmark
                book_button = ((ViewGroup) view.getParent()).findViewById(R.id.bookmarkbut);
                if(book_button.getTag().toString() == "unselected") {
                    book_button.setImageResource(R.drawable.ic_turned_in_black_24dp);
                    book_button.setTag("selected");
                    SharedPref.write("xatchup" + chaptername, Integer.parseInt(verse_number));

                    SharedPref.write("xatchup", chaptername + ":"+ chapter_number);
                    Log.i("BOOKMARK", book_button.getTag().toString());
                }
                else {
                    book_button.setImageResource(R.drawable.ic_bookmark_border_black_24dp);
                    book_button.setTag("unselected");
                    SharedPref.write("xatchup" + chaptername, 0);
                    SharedPref.write("xatchup", 0);
                }
                break;
        }
    }

    private void addToFavourites(View view){
        // manage sqlite creation and data addition
        Log.i("AYAT FAVOURITED", view.toString());
        if(mDatabase==null) {
            mDatabase.open();
        }
        if(fav_button.getTag() == "1"){
            mDatabase.saveToFavs(chapter_number, verse_number, "0");
            fav_button.setImageResource(R.drawable.ic_favorite_border_black_24dp);
            fav_button.setTag("0");
        }else {
            mDatabase.saveToFavs(chapter_number, verse_number, "1");
            fav_button.setImageResource(R.drawable.ic_favorite_black_24dp);
            fav_button.setTag("1");
        }


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
        if (!mCursor.moveToPosition(i)) {
            return;
        }
        String etext = mCursor.getString(6);
        String rtext = mCursor.getString(5);
        String ttext = mCursor.getString(4);
        String artext = mCursor.getString(3);
        String numb = mCursor.getString(2);
        int is_fav = mCursor.getInt(4);
        verse_number = numb;
        Log.i("TAG FAVOURITE AYAH", String.valueOf(is_fav));

        if(SharedPref.getDefaults("ar")){
            holder.arabic_ayah_number.setVisibility(View.VISIBLE);
            holder.arabic_text.setVisibility(View.VISIBLE);

        }

        if(is_fav ==1)
        {
            fav_button = holder.actions_lin_layout.findViewById(R.id.favouritebut);
            fav_button.setImageResource(R.drawable.ic_favorite_black_24dp);
            fav_button.setTag("1");

        }else {
            fav_button.setTag("0");
        }

        //holder.arabic_text.setTextDirection(View.TEXT_DIRECTION_ANY_RTL);
        holder.arabic_text.setGravity(Gravity.END | Gravity.RIGHT);

        holder.arabic_text.setText(artext);
        holder.arabic_ayah_number.setText(String.valueOf(numb));

        if (SharedPref.getDefaults("uz")) {
            //holder.ayah_number.setVisibility(View.VISIBLE);
            holder.ayah_text_uz.setVisibility(View.VISIBLE);
            holder.ayah_text_uz.setText(Html.fromHtml(collapseBraces(ttext)));
            holder.ayah_number.setText(String.valueOf(numb));
        }
        if(SharedPref.getDefaults("ru")){
            holder.ayah_text_ru.setVisibility(View.VISIBLE);
            holder.ayah_text_ru.setText(Html.fromHtml(collapseBraces(rtext)));
        }
if(SharedPref.getDefaults("en")){
            holder.ayah_text_en.setVisibility(View.VISIBLE);
            holder.ayah_text_en.setText(Html.fromHtml(collapseBraces(etext)));
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



}
