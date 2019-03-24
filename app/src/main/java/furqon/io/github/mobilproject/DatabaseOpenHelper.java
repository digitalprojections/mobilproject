package furqon.io.github.mobilproject;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.readystatesoftware.sqliteasset.SQLiteAssetHelper;


public class DatabaseOpenHelper extends SQLiteAssetHelper {
    private static final String DB_NAME = "qurandb";
    private static final int DB_VERSION = 3;

    DatabaseOpenHelper(Context context) {

        super(context, DB_NAME, null, DB_VERSION);

    }



    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        Cursor c = sqLiteDatabase.rawQuery("ALTER TABLE quran ADD favourite INTEGER;", null);
        Log.i("COLUMN CREATED", String.valueOf(c));
    }




}
