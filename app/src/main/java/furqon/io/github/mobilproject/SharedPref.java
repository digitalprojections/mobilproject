package furqon.io.github.mobilproject;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

public class SharedPref
{
    private static SharedPreferences mSharedPref;
    public static final String TOKEN = "TOKEN";
    public static final String UZSW = "UZSW";
    public static final String ARSW = "ARSW";
    public static final String RUSW = "RUSW";
    public static final String ENSW = "ENSW";
    public static final String XATCHUP = "xatchup";
    public static final String FIRSTRUN = "firstrun";

    public static boolean arsw;
    public static boolean uzsw;
    public static boolean rusw;
    public static boolean ensw;



    private SharedPref()
    {
    }



    public static void init(Context context)
    {

        if(mSharedPref == null) {
            mSharedPref = context.getSharedPreferences(context.getPackageName(), Activity.MODE_PRIVATE);

        }
        setDefaults();
    }
    public static boolean firstRun(){
        boolean retval =true;
        if (mSharedPref.getBoolean(FIRSTRUN, true)) {
            // Do first run stuff here then set 'firstrun' as false
            // using the following line to edit/commit prefs
            mSharedPref.edit().putBoolean(FIRSTRUN, false).commit();
        }else{
            retval = true;
        }
        return retval;
    }

    public static boolean getDefaults(String sw){
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

        }

        return rv;
    }

    private static void setDefaults(){
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

    }

    public static Boolean contains(String s){
        Boolean rv = false;

        if(mSharedPref.contains(s)){
            rv = true;
        }

        return rv;
    }



    public static void write(String key, String value) {
        SharedPreferences.Editor prefsEditor = mSharedPref.edit();
        prefsEditor.putString(key, value);
        prefsEditor.apply();
    }

    public static String read(String key, String defValue) {
        return mSharedPref.getString(key, defValue);
    }
    public static boolean read(String key, boolean defValue) {
        return mSharedPref.getBoolean(key, defValue);
    }
    public static Integer read(String key, int defValue) {
        return mSharedPref.getInt(key, defValue);
    }
    public static void write(String key, boolean value) {
        SharedPreferences.Editor prefsEditor = mSharedPref.edit();
        prefsEditor.putBoolean(key, value);
        prefsEditor.apply();
    }



    public static void write(String key, Integer value) {
        SharedPreferences.Editor prefsEditor = mSharedPref.edit();
        prefsEditor.putInt(key, value).apply();
    }
}