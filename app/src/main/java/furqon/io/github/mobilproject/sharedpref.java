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
    private Context mContext;
    private static final String TRACKS = "TRACKS";
    private static SharedPreferences mSharedPref;
    final String TOKEN = "TOKEN";
    final String UZSW = "UZSW";
    final String ARSW = "ARSW";
    final String RUSW = "RUSW";
    final String ENSW = "ENSW";
    final String XATCHUP = "xatchup";
    final String RANDOMAYAHSW = "SHOW_ONE_AYAH";
    private static final String FIRSTRUN = "firstrun";

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

    public ArrayList GetTrackStates(){
        String jstring = read(TRACKS, "");
        ArrayList strings = new ArrayList();
        if(!jstring.isEmpty()){
            strings = GetJson(jstring);
            Log.i("TRACK INFO", strings.size() + " size of array");
        }
        return strings;
    }
    public void SetTrackStates(ArrayList list){
        Toast.makeText(mContext, list.toString(), Toast.LENGTH_LONG).show();
        Log.i("LIST TO SAVE", list.toString());
    }

    private ArrayList GetJson(String jstring) {
        String[] strings;
        ArrayList json = new ArrayList();
        try{
            strings = jstring.split(":");

            for (int i=0; i<strings.length;i++) {
                json.add(strings[i].split(";"));
                json.set(json.size()-1, json.get(json.size()-1).toString().split(","));
            }
        }catch (PatternSyntaxException psx){
            Toast.makeText(mContext, psx.getMessage(), Toast.LENGTH_LONG).show();
        }
        return json;
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