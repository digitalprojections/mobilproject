package furqon.io.github.mobilproject;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static furqon.io.github.mobilproject.R.color.colorArabic;
import static furqon.io.github.mobilproject.R.drawable.*;
import static furqon.io.github.mobilproject.Settings.*;

public class AyahListAdapter extends RecyclerView.Adapter<AyahListAdapter.AyahListViewHolder> {
    private Context mContext;
    private Cursor mCursor;
    private ArrayList<String> mArrayList;
    SharedPreferences sharedPreferences;

    private boolean sw_ar;
    private boolean sw_uz;
    private ViewGroup.LayoutParams lp; // Height of TextView
    private ViewGroup.LayoutParams lpmar; // Height of TextView
    private ViewGroup.LayoutParams lpartxt; // Height of TextView


    AyahListAdapter(Context context, Cursor cursor) {
        mContext = context;
        mCursor = cursor;
    }


    public class AyahListViewHolder extends RecyclerView.ViewHolder implements OnClickListener {
        TextView ayatext;
        TextView arabictext;
        TextView ayahnumber;
        TextView arabic_ayahnumber;
        TextView comment;

        LinearLayout linearLayout1;
        LinearLayout linearLayout2;


        AyahListViewHolder(@NonNull View itemView) {
            super(itemView);

            linearLayout1 = itemView.findViewById(R.id.uzbektranslation);
            linearLayout2 = itemView.findViewById(R.id.arabictranslation);

            linearLayout2.setGravity(Gravity.END);

            itemView.setOnClickListener(this);
            ayahnumber = new TextView(itemView.getContext());
            ayatext = new TextView(itemView.getContext());

            arabictext = new TextView(itemView.getContext());
            arabic_ayahnumber = new TextView(itemView.getContext());


            ((LinearLayout.LayoutParams) lpmar).setMargins(5, -15, 5, 5);
            ((LinearLayout.LayoutParams) lp).setMargins(15, 0, 5, 5);
            ((LinearLayout.LayoutParams) lpartxt).setMargins(128, 0, 5, 5);
            //lpmar.width = 32;
            ayahnumber.setTextSize(15);
            ayahnumber.setLayoutParams(lp);
            ayahnumber.setGravity(Gravity.CENTER);
            ayahnumber.setBackgroundResource(ayah_symbol32);
            ayahnumber.setVisibility(View.GONE);
            ayatext.setLayoutParams(lp);
            ayatext.setTextSize(18);
            ayatext.setPadding(0, 15, 0, 15);
            ayatext.setVisibility(View.GONE);
            arabictext.setLayoutParams(lpartxt);
            arabictext.setTextSize(28);
            arabictext.setGravity(Gravity.END);
            arabictext.setTextColor(Color.BLACK);
            arabictext.setShadowLayer(1.5f, 0, 0, Color.BLACK);
            arabictext.setVisibility(View.GONE);
            arabic_ayahnumber.setLayoutParams(lpmar);
            arabic_ayahnumber.setBackgroundResource(ayah_symbol32);

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
            linearLayout1.addView(ayahnumber);
            linearLayout1.addView(ayatext);
            linearLayout2.addView(arabictext);
            linearLayout2.addView(arabic_ayahnumber);
        }


        @Override
        public void onClick(View view) {
            int position = getAdapterPosition();
            Log.d("CLICK", ayatext.getText().toString());

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


        holder.arabic_ayahnumber.setVisibility(View.VISIBLE);
        holder.arabictext.setVisibility(View.VISIBLE);

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
