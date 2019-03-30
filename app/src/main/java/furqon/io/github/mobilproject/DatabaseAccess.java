package furqon.io.github.mobilproject;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import android.util.Log;

import java.util.ArrayList;

public class DatabaseAccess {
    private DatabaseOpenHelper openHelper;
    private SQLiteDatabase db;
    private static DatabaseAccess instance;
    private Cursor c = null;

    private DatabaseAccess(Context context) {
        this.openHelper = new DatabaseOpenHelper(context);
    }

    public static class FavouriteManager implements BaseColumns {
        public static final String TABLE_NAME = "quran";
        public static final String COLUMN_AYAHTEXT = "AyahText";
        public static final String COLUMN_VERSEID = "VerseID";
        public static final String COLUMN_SURAID = "SuraID";
        public static final String COLUMN_FAV = "favourite";




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
        this.db = openHelper.getWritableDatabase();
    }


    public void close() {
        if (db != null) {
            this.db.close();
        }
    }
    public boolean isOpen()
    {
        if(db!=null){
            return db.isOpen();
        }
        else {
         return false;
        }

    }

    Cursor getSuraTitles() {
        try {
            c = db.rawQuery("SELECT at.ChapterID, at.SuraName as 'arabic', sn.SuraName as 'uzbek'  FROM arabic_titles at inner join suranames sn on sn.chapterid = at.chapterid", new String[]{});
        } catch (SQLiteException e) {

        }
        return c;

    }

    Cursor loadFavourites() {

        try {
            c = db.rawQuery("SELECT q1.AyahText, q2.AyahText as 'uztext', q1.VerseID, q1.SuraID, q1.favourite, sn.SuraName FROM quran q1\n" +
                    "INNER JOIN quran q2\n" +
                    "ON q1.SuraID=q2.SuraID\n" +
                    "INNER JOIN SuraNames sn \n" +
                    "ON sn.ChapterID=q1.SuraID\n" +
                    "WHERE q1.DatabaseID = 1\n" +
                    "and q2.favourite = 1\n" +
                    "and q1.VerseID=q2.VerseID\n" +
                    "and q2.DatabaseID =120 " , new String[]{});
            Log.i("TABLE COLUMN", c.toString());
        } catch (SQLiteException e) {

        }

        return c;

    }
    Cursor loadRandom() {
        try {
            c = db.rawQuery("SELECT q1.AyahText, q2.AyahText as 'uztext', q1.VerseID, q1.SuraID, q1.favourite, sn.SuraName FROM quran q1\n" +
                    "INNER JOIN quran q2\n" +
                    "ON q1.SuraID=q2.SuraID\n" +
                    "INNER JOIN SuraNames sn \n" +
                    "ON sn.ChapterID=q1.SuraID\n" +
                    "WHERE q1.DatabaseID = 1\n" +
                    "and q2.daily = 1\n" +
                    "and q1.VerseID=q2.VerseID\n" +
                    "and q2.DatabaseID=120", new String[]{});
            Log.i("TABLE COLUMN", c.toString());
        } catch (SQLiteException e) {

        }

        return c;

    }

    Cursor getSuraText(String sn) {
        c = db.rawQuery("SELECT quz.DatabaseID, quz.SuraID, quz.VerseID, qar.AyahText as 'arab', quz.AyahText as 'uzb', qru.AyahText as 'ru', qen.AyahText as 'en' from quran quz\n" +
                "JOIN quran qar\n" +
                "ON qar.SuraID = quz.SuraID\n" +
                "JOIN quran qru\n" +
                "ON qru.SuraID=qar.SuraID\n" +
                "JOIN quran qen\n" +
                "ON qen.SuraID=qru.SuraID\n" +
                "where \n" +
                "quz.VerseID=qar.VerseID\n" +
                "and qru.VerseID=qar.VerseID\n" +
                "and qen.VerseID=qru.VerseID\n" +
                "and quz.DatabaseID=120\n" +
                "and qar.DatabaseID=1\n" +
                "and qru.DatabaseID=79\n" +
                "and qen.DatabaseID=59\n" +
                "and quz.SuraID = " + sn, new String[]{});
        //Log.i("TABLE COLUMN", db.rawQuery("PRAGMA table_info()"));
        return c;

    }

    Cursor getVersion() {
        Cursor v = db.rawQuery("PRAGMA user_version", null);
        Log.i("DATABASE VERSION", String.valueOf(v));
        return v;
    }

    public void saveToFavs(String suraid, String ayahno, String fav) {
// New value for one column

        ContentValues values = new ContentValues();
        values.put(FavouriteManager.COLUMN_FAV, fav);

// Which row to update, based on the title
        String selection = FavouriteManager.COLUMN_SURAID + "=? and " + FavouriteManager.COLUMN_VERSEID + "=?";
        String[] selectionArgs = {suraid, ayahno};

        int count = db.update(
                FavouriteManager.TABLE_NAME,
                values,
                selection,
                selectionArgs);

        Log.i("UPDATE ", "database updated? " + suraid + " " + ayahno + " " + fav);

    }

}
