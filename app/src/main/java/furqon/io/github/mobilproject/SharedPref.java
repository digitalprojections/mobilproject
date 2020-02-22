package furqon.io.github.mobilproject;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

public class SharedPref
{
    private static SharedPreferences mSharedPref;
    final String TOKEN = "TOKEN";
    final String UZSW = "UZSW";
    final String ARSW = "ARSW";
    final String RUSW = "RUSW";
    final String ENSW = "ENSW";
    final String XATCHUP = "xatchup";
    private static final String FIRSTRUN = "firstrun";

    private static boolean arsw;
    private static boolean uzsw;
    private static boolean rusw;
    private static boolean ensw;
    private static SharedPref singleton_sharedpref = null;
    private static SharedPreferences.Editor prefsEditor;

    public static SharedPref getInstance(){
        if(singleton_sharedpref==null){
            singleton_sharedpref = new SharedPref();
        }
        return singleton_sharedpref;
    }

    private SharedPref(){

    }

    public void init(Context context)
    {

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

    }

    Boolean contains(String s){
        boolean rv = false;

        if(mSharedPref.contains(s)){
            rv = true;
        }

        return rv;
    }



    void write(String key, String value) {

        prefsEditor.putString(key, value);
        prefsEditor.apply();
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

    boolean isFirstRun() {
        return read(FIRSTRUN, true);
    }

    void setFirstRun(boolean firstRun) {
        write(FIRSTRUN, firstRun);
    }
}