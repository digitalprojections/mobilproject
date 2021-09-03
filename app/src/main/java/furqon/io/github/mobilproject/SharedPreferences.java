package furqon.io.github.mobilproject;
import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.widget.Toast;

public class SharedPreferences
{
    public static final String GOOGLE = "GOOGLE";
    public static final String RESTART_ATTEMPTED = "RESTARTED";
    public static final String NOMOREADS = "NOMOREADS";
    public static final String PREVIOUS_SET = "PREVIOUSSET";
    public static final String TOKEN_ATTEMPTED = "TOKEN_ATTEMPTED";
    public static final String TOKEN_ATTEMPT_COUNT = "token_attempts";
    public static final String PREFERRED_REPEAT_COUNT = "PREFERREDREPEAT";
    public static final String TRANSLATION_SELECTED = "TRANSLATION_SELECTED";
    static final String PLAYMODE = "PLAYMODE";
    static final String SIGNATURE = "SIGNATURE";
    static final String FONTSIZE = "FONTSIZE";
    static final String RECITATIONSTYLE = "RECITATIONSTYLE";
    static final String RECITER = "RECITER";
    static final String FONT = "FONT";
    static final String USERID = "USERID";
    static final String INVITER = "INVITER";
    static final String INVITER_ID = "INVITER_ID";
    static final String SHAREWARD = "SHAREREWARD";
    static final String CREDS_ALREADY_SENT = "CREDSENT";
    static final String RANDOM_AYAH_SEEN = "SEEN";
    static final String PERSONAL_REWARD = "PERSONALREWARD";
    static final String SELECTED_AUDIO_LANGUAGE = "SELECTED_AUDIO";
    static final String SELECTED_MEMORIZING_SURAH = "MEMORIZING_SURAH";
    private static final String TRACKS = "TRACKS";
    private static android.content.SharedPreferences mSharedPref;
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

    private static boolean random_ayah_sw;
    private static SharedPreferences singleton_sharedpref = null;
    private static android.content.SharedPreferences.Editor prefsEditor;

    public static SharedPreferences getInstance(){
        if(singleton_sharedpref==null){
            singleton_sharedpref = new SharedPreferences();
        }
        return singleton_sharedpref;
    }

    private SharedPreferences(){

    }

    static void AddCoins(Context mContext, int coins) {
        int existingCoins = getInstance().read(getInstance().COINS, 0);
        int totalCoins = existingCoins + coins;
        getInstance().write(getInstance().COINS, totalCoins);
        Toast.makeText(mContext, "+" + coins, Toast.LENGTH_LONG).show();
    }

    public static void NoMoreAds(boolean b) {
        getInstance().write(NOMOREADS, b);
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
        boolean rv = read(sw, false);
/*        switch (sw){
            case "uz":
                rv = read();
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
*/
        return rv;
    }



    private void setDefaults(){
        if(isFirstRun()){
            write(ARSW,true);
            write(UZSW,false);
            write(RUSW,false);
            write(ENSW,false);
            Log.i("SHAREDPREFS", "No translations selected");
            setFirstRun();
        }



        if(!mSharedPref.contains(RANDOMAYAHSW)){
            write(RANDOMAYAHSW, true);
        }else {
            random_ayah_sw = read(RANDOMAYAHSW, false);
        }

        if(!mSharedPref.contains(TRACKS)){
            write(TRACKS, "");
        }
    }

    Boolean contains(String s){
        boolean rv = false;

        if (mSharedPref != null && mSharedPref.contains(s)) {
            rv = true;
        }

        return rv;
    }

    boolean read(String key, boolean defValue) {
        return mSharedPref.getBoolean(key, defValue);
    }
    String read(String key, String defValue) {
        return mSharedPref.getString(key, defValue);
    }
    Integer read(String key, int defValue) {
        return mSharedPref.getInt(key, defValue);
    }

    void write(String key, boolean value){
        prefsEditor.putBoolean(key, value).apply();
    }
    void write(String key, Integer value) {prefsEditor.putInt(key, value).apply();}
    void write(String key, String value) {
        prefsEditor.putString(key, value).apply();
    }

    boolean isFirstRun() {
        return read(FIRSTRUN, true);
    }
    void setFirstRun() {
        write(FIRSTRUN, false);
    }
}