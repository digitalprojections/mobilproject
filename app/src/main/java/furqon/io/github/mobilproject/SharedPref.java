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
    public static final String ARABSW = "ARABSW";
    public static final String RUSW = "RUSW";
    public static final String ENSW = "ENSW";
    public static final String XATCHUP = "XATCHUP";

    public static boolean arsw;
    public static boolean uzsw;
    public static boolean rusw;
    public static boolean ensw;

    private SharedPref()
    {
    }



    public static void init(Context context)
    {

        if(mSharedPref == null)
            mSharedPref = context.getSharedPreferences(context.getPackageName(), Activity.MODE_PRIVATE);

        setDefaults();
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
        if(!mSharedPref.contains(UZSW)){
            write(ARABSW,true);
            write(UZSW,false);
            write(RUSW,false);
            write(ENSW,false);
            Log.i("SHAREDPREFS", "ok");
        }
        else {
            arsw = read(ARABSW, false);
            uzsw = read(UZSW, false);
            rusw = read(RUSW, false);
            ensw = read(ENSW, false);
        }
    }

    public static String read(String key, String defValue) {
        return mSharedPref.getString(key, defValue);
    }

    public static void write(String key, String value) {
        SharedPreferences.Editor prefsEditor = mSharedPref.edit();
        prefsEditor.putString(key, value);
        prefsEditor.apply();
    }


    public static boolean read(String key, boolean defValue) {
        return mSharedPref.getBoolean(key, defValue);
    }

    public static void write(String key, boolean value) {
        SharedPreferences.Editor prefsEditor = mSharedPref.edit();
        prefsEditor.putBoolean(key, value);
        prefsEditor.apply();
    }

    public static Integer read(String key, int defValue) {
        return mSharedPref.getInt(key, defValue);
    }

    public static void write(String key, Integer value) {
        SharedPreferences.Editor prefsEditor = mSharedPref.edit();
        prefsEditor.putInt(key, value).apply();
    }
}