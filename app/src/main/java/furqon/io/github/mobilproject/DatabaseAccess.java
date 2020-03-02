package furqon.io.github.mobilproject;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.os.CountDownTimer;
import android.provider.BaseColumns;
import android.util.Log;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;

import java.io.IOException;
import java.util.logging.Handler;

public class DatabaseAccess {
    private DatabaseOpenHelper openHelper;
    private SQLiteDatabase db;
    private static DatabaseAccess instance;
    private Cursor c = null;
    private Context mContext;

    private DatabaseAccess(Context context) {

        this.openHelper = new DatabaseOpenHelper(context);
        mContext = context;
    }

    public static class FavouriteManager implements BaseColumns {
        public static final String TABLE_NAME = "quran";
        public static final String TABLE_FAV = "favourites";
        public static final String COLUMN_REMARKS = "remarks";
        public static final String COLUMN_VERSEID = "ayah_id";
        public static final String COLUMN_SURAID = "sura_id";
        public static final String COLUMN_FAV = "fav";
        public static final String COLUMN_SHARE_COUNT = "share_count";
        public static final String COLUMN_CATEGORY = "category_id";
        public static final String COLUMN_LANGUAGE = "language_id";
        public static final String COLUMN_X1 = "additional1";
        public static final String COLUMN_X2 = "additional2";
        public static final String COLUMN_X3 = "additional3";
        public static final String COLUMN_X4 = "additional4";


    }

    static DatabaseAccess getInstance(Context context) {
        if (instance == null) {
            instance = new
                    DatabaseAccess(context);

        }
        return instance;
    }

    //open
    void open() {

            db = openHelper.getWritableDatabase();
        }


    public void close() {
        if (db != null) {
            db.close();
        }
    }

    public boolean isOpen() {
        if (db != null) {
            return db.isOpen();
        } else {
            return false;
        }

    }

    Cursor getSuraTitles() {
        try {
            c = db.rawQuery("SELECT at.ChapterID, at.SuraName as arabic, sn.SuraName as uzbek FROM arabic_titles at inner join suranames sn on sn.chapterid = at.chapterid", new String[]{});
        } catch (SQLiteException e) {
            Crashlytics.logException(e);
        }
        return c;

    }

    Cursor loadFavourites() {

        try {
            c = db.rawQuery("SELECT quz.DatabaseID, quz.SuraID, quz.VerseID, \n" +
                    "qar.AyahText as 'arab', \n" +
                    "quz.AyahText as 'uzb', \n" +
                    "qru.AyahText as 'ru', \n" +
                    "qen.AyahText as 'en', \n" +
                    "fv.fav, \n" +
                    "sn.SuraName  \n" +
                    "from quran quz\n" +
                    "JOIN quran qar\n" +
                    "ON qar.SuraID = quz.SuraID\n" +
                    "JOIN quran qru\n" +
                    "ON qru.SuraID=qar.SuraID\n" +
                    "JOIN quran qen\n" +
                    "ON qen.SuraID=qru.SuraID\n" +
                    "JOIN SuraNames sn \n" +
                    "ON sn.ChapterID = quz.SuraID\n" +
                    "JOIN favourites fv\n" +
                    "ON fv.sura_id = quz.SuraID\n" +
                    "where \n" +
                    "quz.VerseID=qar.VerseID\n" +
                    "and qru.VerseID=qar.VerseID\n" +
                    "and qen.VerseID=qru.VerseID\n" +
                    "and quz.DatabaseID=120\n" +
                    "and qar.DatabaseID=1\n" +
                    "and qru.DatabaseID=79\n" +
                    "and qen.DatabaseID=59\n" +
                    "and quz.VerseID = fv.ayah_id\n" +
                    "and fv.fav = '1'", new String[]{});
            Log.i("TABLE COLUMN", String.valueOf(c));
        } catch (SQLiteException e) {
            Crashlytics.logException(e);
        }

        return c;

    }


    Cursor getRandomAyah(int suraid, int ayahno) {
        Log.d("DATABASE", suraid + " " + ayahno);
        try {
            c = db.rawQuery("SELECT uzbek.SuraID,uzbek.VerseID, sn.SuraName, arabic.AyahText as 'arab', uzbek.AyahText as 'uzb', russian.AyahText as 'ru', english.AyahText as 'en', (SELECT fv.fav FROM favourites fv WHERE fv.sura_id=arabic.SuraID and fv.ayah_id=arabic.VerseID) as 'fav'\n" +
                    "\tFROM quran uzbek\n" +
                    "\tJOIN quran arabic\n" +
                    "\tON arabic.SuraID = uzbek.SuraID\n" +
                    "\tJOIN quran russian\n" +
                    "\tON russian.SuraID = arabic.SuraID\n" +
                    "\tJOIN quran english\n" +
                    "\tON english.SuraID = uzbek.SuraID\n" +
                    "JOIN SuraNames sn\n" +
                    "\tON sn.ChapterID = arabic.SuraID" +
                    "\twhere \n" +
                    "\tuzbek.databaseid = 120\n" +
                    "\tand uzbek.VerseID = arabic.VerseID\n" +
                    "\tand arabic.DatabaseID = 1\n" +
                    "\tand russian.VerseID = arabic.VerseID\n" +
                    "\tand russian.DatabaseID = 79\n" +
                    "\tand english.VerseID = arabic.VerseID\n" +
                    "\tand english.DatabaseID = 59\n" +
                    "\tand uzbek.SuraID =" + suraid +
                    "\tand uzbek.VerseID =" + ayahno, new String[]{});
        } catch (SQLiteException e) {
            Crashlytics.logException(e);
        }
        return c;
    }

    Cursor getSuraText(String sn) {
        try {
            c = db.rawQuery("SELECT quz.DatabaseID, quz.SuraID, quz.VerseID, \n" +
                    "qar.AyahText as 'arab', \n" +
                    "quz.AyahText as 'uzb', \n" +
                    "qru.AyahText as 'ru', \n" +
                    "qen.AyahText as 'en', \n" +
                    "(SELECT fav from favourites WHERE sura_id=quz.SuraID and ayah_id=quz.VerseID) as fav, \n" +
                    "sn.SuraName  \n" +
                    "from quran quz\n" +
                    "JOIN quran qar\n" +
                    "ON qar.SuraID = quz.SuraID\n" +
                    "JOIN quran qru\n" +
                    "ON qru.SuraID=qar.SuraID\n" +
                    "JOIN quran qen\n" +
                    "ON qen.SuraID=qru.SuraID\n" +
                    "JOIN SuraNames sn \n" +
                    "ON sn.ChapterID = quz.SuraID\n" +
                    "where \n" +
                    "quz.VerseID=qar.VerseID\n" +
                    "and qru.VerseID=qar.VerseID\n" +
                    "and qen.VerseID=qru.VerseID\n" +
                    "and quz.DatabaseID=120\n" +
                    "and qar.DatabaseID=1\n" +
                    "and qru.DatabaseID=79\n" +
                    "and qen.DatabaseID=59\n" +
                    "and quz.SuraID = " + sn, new String[]{});
        } catch (SQLiteException e) {
            Log.i("TABLE ERROR", String.valueOf(c));
            c = null;
            try {
                // clearing app data
                showToast();
                new CountDownTimer(3000, 1000) {
                    public void onFinish() {
                        // When timer is finished
                        // Execute your code here
                        Runtime runtime = Runtime.getRuntime();
                        try {
                            runtime.exec("pm clear furqon.io.github.mobilproject");
                        } catch (IOException e1) {
                            e1.printStackTrace();
                        }
                    }

                    public void onTick(long millisUntilFinished) {
                        // millisUntilFinished    The amount of time until finished.
                    }
                }.start();


            } catch (Exception e1) {
                e1.printStackTrace();
            }

        }


        //
        return c;

    }
Cursor searchText(String st) {
        try {
            if(db.isOpen()) {
                c = db.rawQuery("SELECT sn.SuraName, quz.SuraID, quz.VerseID, \n" +
                        "qar.AyahText as 'arab', \n" +
                        "quz.AyahText as 'uzb', \n" +
                        "qru.AyahText as 'ru', \n" +
                        "qen.AyahText as 'en',\n" +
                        "(SELECT fav FROM favourites WHERE sura_id = qall.SuraID and ayah_id=qall.VerseID) as 'fav'\n" +
                        "from quran quz\n" +
                        "JOIN quran qar\n" +
                        "ON qar.SuraID = quz.SuraID\n" +
                        "JOIN quran qru\n" +
                        "ON qru.SuraID=qar.SuraID\n" +
                        "JOIN quran qen\n" +
                        "ON qen.SuraID=qru.SuraID\n" +
                        "JOIN quran qall\n" +
                        "ON qall.SuraID = qen.SuraID\n" +
                        "JOIN SuraNames sn \n" +
                        "ON sn.ChapterID = quz.SuraID\n" +
                        "where \t\n" +
                        "\tquz.VerseID=qar.VerseID\n" +
                        "\tand qru.VerseID=qar.VerseID\n" +
                        "\tand qen.VerseID=qru.VerseID\n" +
                        "\tand qall.VerseID=qen.VerseID\n" +
                        "\tand quz.DatabaseID=120\n" +
                        "\tand qar.DatabaseID=1\n" +
                        "\tand qru.DatabaseID=79\n" +
                        "\tand qen.DatabaseID=59\n" +
                        "\tand qall.AyahText LIKE '%" + st + "%'", new String[]{});
            }
        } catch (SQLiteException e) {
            Crashlytics.log("TABLE ERROR" + String.valueOf(c));
            c = null;
            try {
                // clearing app data
                showToast();
                new CountDownTimer(3000, 1000) {
                    public void onFinish() {
                        // When timer is finished
                        // Execute your code here
                        Runtime runtime = Runtime.getRuntime();
                        try {
                            runtime.exec("pm clear furqon.io.github.mobilproject");
                        } catch (IOException e1) {
                            e1.printStackTrace();
                        }
                    }

                    public void onTick(long millisUntilFinished) {
                        // millisUntilFinished    The amount of time until finished.
                    }
                }.start();


            } catch (Exception e1) {
                e1.printStackTrace();
                Crashlytics.log("ERROR" + String.valueOf(e1));
            }

        }


        //
        return c;

    }

    private void showToast() {
        Toast.makeText(mContext, R.string.app_shuts_down_now, Toast.LENGTH_LONG).show();
    }

    Cursor getVersion() {
        Cursor v = db.rawQuery("PRAGMA user_version", null);
        Log.i("DATABASE VERSION", String.valueOf(v));
        return v;
    }

    public void removeFromFavs(String suraid, String ayahno, String fav) {
// New value for one column

        ContentValues values = new ContentValues();
        values.put(FavouriteManager.COLUMN_FAV, fav);
        String[] vals = {suraid, ayahno};

        long count = db.updateWithOnConflict(FavouriteManager.TABLE_FAV, values, "sura_id=? and ayah_id=?", vals, SQLiteDatabase.CONFLICT_REPLACE);
        if (count < 0) {


        }

        Log.i("UPDATE ", "database updated? " + suraid + " " + ayahno + " " + fav);

    }

    public void removeFromFavs(int suraid, int ayahno, String fav) {
// New value for one column

        ContentValues values = new ContentValues();
        values.put(FavouriteManager.COLUMN_FAV, fav);
        String[] vals = {String.valueOf(suraid), String.valueOf(ayahno)};

        long count = db.updateWithOnConflict(FavouriteManager.TABLE_FAV, values, "sura_id=? and ayah_id=?", vals, SQLiteDatabase.CONFLICT_REPLACE);
        if (count < 0) {


        }

        Log.i("UPDATE ", "database updated? " + suraid + " " + ayahno + " " + fav);

    }

    public void saveToFavs(String suraid, String ayahno, String fav) {
// New value for one column

        ContentValues values = new ContentValues();
        values.put(FavouriteManager.COLUMN_FAV, fav);
        values.put(FavouriteManager.COLUMN_SURAID, suraid);
        values.put(FavouriteManager.COLUMN_VERSEID, ayahno);

        long count = db.insertOrThrow(FavouriteManager.TABLE_FAV, null, values);
        if (count < 0) {


        }

        Log.i("UPDATE ", "database updated? " + suraid + " " + ayahno + " " + fav);



    }

    public void saveToFavs(int suraid, int ayahno, String fav) {
// New value for one column

        ContentValues values = new ContentValues();
        values.put(FavouriteManager.COLUMN_FAV, fav);
        values.put(FavouriteManager.COLUMN_SURAID, suraid);
        values.put(FavouriteManager.COLUMN_VERSEID, ayahno);

        long count = db.insertOrThrow(FavouriteManager.TABLE_FAV, null, values);
        if (count < 0) {


        }

        Log.i("UPDATE ", "database updated? " + suraid + " " + ayahno + " " + fav);

    }


}
