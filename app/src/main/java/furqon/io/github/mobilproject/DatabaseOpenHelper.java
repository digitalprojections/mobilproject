package furqon.io.github.mobilproject;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;


public class DatabaseOpenHelper extends SQLiteOpenHelper{
    private static final String DB_NAME = "qurandb";
    private static final int DB_VERSION = 3;

    DatabaseOpenHelper(Context context) {

        super(context, DB_NAME, null, DB_VERSION);

    }


    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {

    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        Cursor c = sqLiteDatabase.rawQuery("ALTER TABLE quran ADD favourite INTEGER;", null);
        Log.i("COLUMN CREATED", String.valueOf(c));
    }


    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }
    //constructor


}
