package furqon.io.github.mobilproject;
import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.regex.PatternSyntaxException;

public class sharedpref
{
    public static final String USERID = "USERID";
    private Context mContext;
    private static final String TRACKS = "TRACKS";
    private static SharedPreferences mSharedPref;
    final String COINS = "COINS";
    final String TOKEN = "TOKEN";
    final String UZSW = "UZSW";
    final String ARSW = "ARSW";
    final String RUSW = "RUSW";
    final String ENSW = "ENSW";
    final String XATCHUP = "xatchup";
    final String RANDOMAYAHSW = "SHOW_ONE_AYAH";
    private static final String FIRSTRUN = "firstrun";

    static String displayOrder = "displayOrder";

    private static boolean arsw;
    private static boolean uzsw;
    private static boolean rusw;
    private static boolean ensw;
    private static boolean random_ayah_sw;
    private static sharedpref singleton_sharedpref = null;
    private static SharedPreferences.Editor prefsEditor;

    public static sharedpref getInstance(){
        if(singleton_sharedpref==null){
            singleton_sharedpref = new sharedpref();
        }
        return singleton_sharedpref;
    }

    private sharedpref(){

    }

    public static void AddCoins(Context mContext, int coins) {
        int existingCoins = getInstance().read(getInstance().COINS, 0);
        int totalCoins = existingCoins + coins;
        getInstance().write(getInstance().COINS, totalCoins);
        Toast.makeText(mContext,"Ad triggered reward. Coins amount: " + totalCoins, Toast.LENGTH_LONG).show();
    }

    public void init(Context context)
    {
        mContext = context;
        if(mSharedPref == null) {

            mSharedPref = context.getSharedPreferences(context.getPackageName(), Activity.MODE_PRIVATE);
            prefsEditor = mSharedPref.edit();
            prefsEditor.apply();
        }
        setDefaults();
    }

    boolean getDefaults(String sw){
        boolean rv = false;

        switch (sw){
            case "uz":
                rv = uzsw;
                break;
            case "ar":
                rv = arsw;
                break;
            case "ru":
                rv = rusw;
                break;
            case "en":
                rv = ensw;
                break;
            case "random_ayah_sw":
                rv=random_ayah_sw;
                break;

        }

        return rv;
    }



    private void setDefaults(){
        if(!mSharedPref.contains(UZSW) || !mSharedPref.contains(RUSW) || !mSharedPref.contains(ENSW)){
            write(ARSW,true);
            write(UZSW,true);
            write(RUSW,true);
            write(ENSW,true);
            Log.i("SHAREDPREFS", "No translations selected");
        }
        else {
            arsw = read(ARSW, false);
            uzsw = read(UZSW, false);
            rusw = read(RUSW, false);
            ensw = read(ENSW, false);

        }

        if(!mSharedPref.contains(RANDOMAYAHSW)){
            write(RANDOMAYAHSW, true);
        }else {
            random_ayah_sw = read(RANDOMAYAHSW, true);
        }

        if(!mSharedPref.contains(TRACKS)){
            write(TRACKS, "");
        }
    }

    Boolean contains(String s){
        boolean rv = false;

        if(mSharedPref.contains(s)){
            rv = true;
        }

        return rv;
    }




    String read(String key, String defValue) {
        return mSharedPref.getString(key, defValue);
    }
    boolean read(String key, boolean defValue) {
        return mSharedPref.getBoolean(key, defValue);
    }
    Integer read(String key, int defValue) {
        return mSharedPref.getInt(key, defValue);
    }
    void write(String key, boolean value) {
        prefsEditor.putBoolean(key, value);
        prefsEditor.apply();
    }
    void write(String key, Integer value) {
        prefsEditor.putInt(key, value).apply();
    }
    void write(String key, String value) {
        prefsEditor.putString(key, value);
        prefsEditor.apply();
    }

    boolean isFirstRun() {
        return read(FIRSTRUN, true);
    }

    void setFirstRun(boolean firstRun) {
        write(FIRSTRUN, firstRun);
    }
}