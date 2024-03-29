package furqon.io.github.mobilproject;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.text.Html;
import android.text.SpannableStringBuilder;
import android.util.Log;
import android.util.TypedValue;
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

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.dynamiclinks.DynamicLink;
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks;
import com.google.firebase.dynamiclinks.ShortDynamicLink;

import java.util.ArrayList;
import java.util.List;

public class AyahListActivityAdapter extends RecyclerView.Adapter<AyahListActivityAdapter.AyahListViewHolder> {
    private static final String TAG = AyahListActivityAdapter.class.getSimpleName();
    private final SharedPreferences sharedPref;
    //private final Animation ayah_close_anim;
    //private final Animation ayah_open_anim;
    private Context mContext;
    private ArrayList<String> mArrayList;

    private List<AllTranslations> mText = new ArrayList<>();

    private ImageButton fav_button;
    private ImageButton book_button;

    private final String chaptername;//Sura nomi
    private final String chapter_number;
    private String verse_number;//oyat nomeri
    private String ayah_txt_uz;//oyat matni uzbek


    private ViewGroup.LayoutParams lp; // Height of TextView
    private ViewGroup.LayoutParams lpmar; // Height of TextView
    private ViewGroup.LayoutParams lpartxt; // Height of TextView
    private final Animation scaler;


    @Override
    public void onViewDetachedFromWindow(@NonNull AyahListViewHolder holder) {
        super.onViewDetachedFromWindow(holder);
        //Log.i(TAG, String.valueOf(this));

    }

    AyahListActivityAdapter(Context context, String suraname, String chapter) {
        sharedPref = SharedPreferences.getInstance();

        chapter_number = chapter;
        chaptername = suraname;
        mContext = context;
        scaler = AnimationUtils.loadAnimation(mContext, R.anim.bounce);

        SpannableStringBuilder ssb = new SpannableStringBuilder();
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

        boolean arabic;
        boolean english;
        boolean uzbek;
        boolean russian;
        boolean languageIsSelected;



        AyahListViewHolder(@NonNull View itemView) {
            super(itemView);

            arabic = sharedPref.getDefaults(sharedPref.ARSW);
            english = sharedPref.getDefaults(sharedPref.ENSW);
            uzbek = sharedPref.getDefaults(sharedPref.UZSW);
            russian = sharedPref.getDefaults(sharedPref.RUSW);
            languageIsSelected = arabic || english || uzbek || russian;
            Log.i("AyahList", String.valueOf(languageIsSelected));
            arabic_text_lin_layout = itemView.findViewById(R.id.v_arabictranslation);
            uzbek_text_lin_layout = itemView.findViewById(R.id.uzbektranslation);
            ruen_text_lin_layout = itemView.findViewById(R.id.landscaper);
            actions_lin_layout = itemView.findViewById(R.id.actions);

            //DONE create share/boomark/favourite and add programmatically
            ImageButton share_button = itemView.findViewById(R.id.f_sharebut);
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

            Typeface madina;
            if (sharedPref.contains(SharedPreferences.FONT)) {
                switch (sharedPref.read(SharedPreferences.FONT, "")) {
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
            if (sharedPref.contains(SharedPreferences.FONTSIZE)) {
                float fs = (float) sharedPref.read(SharedPreferences.FONTSIZE, 0);
                arabic_text.setTextSize(TypedValue.COMPLEX_UNIT_PX, fs);
            }
            arabic_text.setGravity(Gravity.END);
            arabic_text.setTextColor(Color.BLACK);
            //arabic_text.setShadowLayer(1.5f, 0, 0, Color.BLACK);

            arabic_text.setTypeface(madina);
            book_button.setTag("unselected");
            arabic_ayah_number.setLayoutParams(lpmar);
            //arabic_ayah_number.setBackgroundResource(ic_ayahsymbolayahsymbol);

            arabic_ayah_number.setGravity(Gravity.CENTER);
            arabic_text.setTextDirection(View.TEXT_DIRECTION_ANY_RTL);

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
            /*if ((sharedPref.getDefaults("uz") || sharedPref.getDefaults("ru") || sharedPref.getDefaults("en")) && !sharedPref.getDefaults("ar")) {
                ayah_number.setVisibility(View.VISIBLE);
                arabic_text_lin_layout.setVisibility(View.GONE);
            }
*/

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
                int ayah_position = sharedPref.read("xatchup" + chaptername, 0);
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

    public void createDynamicLink_Basic() {
        // [START create_link_basic]
        DynamicLink dynamicLink = FirebaseDynamicLinks.getInstance().createDynamicLink()
                .setLink(Uri.parse("https://mobilproject.github.io/furqon_web_express/?chapter=" + chapter_number+"&verse="+verse_number))
                .setDomainUriPrefix("https://furqon.page.link")
                // Open links with this app on Android
                .setAndroidParameters(new DynamicLink.AndroidParameters.Builder().build())
                // Open links with com.example.ios on iOS
                .setIosParameters(new DynamicLink.IosParameters.Builder("https://mobilproject.github.io/furqon_web_express").build())
                .buildDynamicLink();

        Uri dynamicLinkUri = dynamicLink.getUri();
        // [END create_link_basic]

        Task<ShortDynamicLink> shortLinkTask = FirebaseDynamicLinks.getInstance().createDynamicLink()
                .setLongLink(Uri.parse(dynamicLinkUri.toString()))
                .buildShortDynamicLink()
                .addOnCompleteListener((Activity) mContext, new OnCompleteListener<ShortDynamicLink>() {
                    @Override
                    public void onComplete(@NonNull Task<ShortDynamicLink> task) {
                        if (task.isSuccessful()) {
                            // Short link created
                            Uri shortLink = task.getResult().getShortLink();
                            Uri flowchartLink = task.getResult().getPreviewLink();
                            //Log.d(TAG, shortLink + " short dynamic link");
                            Intent sendIntent = new Intent();
                            sendIntent.setAction(Intent.ACTION_SEND);
                            sendIntent.putExtra(Intent.EXTRA_TEXT, ayah_txt_uz + "\n(" + chaptername + ", " + verse_number + ")\n"+shortLink+"\n"+ mContext.getResources().getText(R.string.seeTranslations));
                            sendIntent.setType("text/plain");
                            mContext.startActivity(Intent.createChooser(sendIntent, mContext.getResources().getText(R.string.shareayah)));
                            Log.d(TAG, "manual link: " + ayah_txt_uz + "\n(" + chaptername + ", " + verse_number + ")\n"+ shortLink +"\n"+ mContext.getResources().getText(R.string.seeTranslations));
                        } else {
                            // Error
                            // ...
                            Intent sendIntent = new Intent();
                            sendIntent.setAction(Intent.ACTION_SEND);
                            sendIntent.putExtra(Intent.EXTRA_TEXT, ayah_txt_uz + "\n(" + chaptername + ", " + verse_number + ")\nhttps://goo.gl/sXBkNt\n"+ mContext.getResources().getText(R.string.seeTranslations));
                            sendIntent.setType("text/plain");
                            mContext.startActivity(Intent.createChooser(sendIntent, mContext.getResources().getText(R.string.shareayah)));
                            Log.d(TAG, "manual link: " + ayah_txt_uz + "\n(" + chaptername + ", " + verse_number + ")\nhttps://goo.gl/sXBkNt\n"+ mContext.getResources().getText(R.string.seeTranslations));
                        }
                    }
                });
    }


    private void takeAction(View view) {

        switch (view.getId()) {
            case R.id.f_sharebut:
                createDynamicLink_Basic();
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
        if (holder.arabic) {
            holder.arabic_text.setGravity(Gravity.END);
            holder.arabic_text.setText(ar_text);
            holder.arabic_ayah_number.setText(String.valueOf(numb));
            holder.arabic_ayah_number.setVisibility(View.VISIBLE);
            holder.arabic_text.setVisibility(View.VISIBLE);
            holder.ayah_number.setVisibility(View.GONE);
        }else{
            holder.arabic_ayah_number.setVisibility(View.GONE);
            holder.arabic_text.setVisibility(View.GONE);
            if(holder.languageIsSelected)
            {
                holder.ayah_number.setVisibility(View.VISIBLE);
            }else {
                //TODO
                //Display a message to the user to enable a language
                holder.ayah_number.setVisibility(View.GONE);
            }

        }

        if (holder.uzbek) {
            //holder.ayah_number.setVisibility(View.VISIBLE);
            holder.ayah_text_uz.setVisibility(View.VISIBLE);
            holder.ayah_text_uz.setText(Html.fromHtml(collapseBraces(uz_text)));
            holder.ayah_number.setText(String.valueOf(numb));
        }else{
            holder.ayah_text_uz.setVisibility(View.GONE);
        }
        if (holder.russian) {
            holder.ayah_text_ru.setVisibility(View.VISIBLE);
            holder.ayah_text_ru.setText(Html.fromHtml(collapseBraces(ru_text)));
            holder.ayah_number.setText(String.valueOf(numb));
        }else{
            holder.ayah_text_ru.setVisibility(View.GONE);
        }
        if (holder.english) {
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
