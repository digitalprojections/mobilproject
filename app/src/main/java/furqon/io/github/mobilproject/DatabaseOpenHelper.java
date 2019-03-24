package furqon.io.github.mobilproject;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Locale;


public class DatabaseOpenHelper extends SQLiteOpenHelper{
    private static final String DB_PATH = "/data/data/furqon.io.github.mobilproject/databases/";
    private static final String DB_NAME = "qurandb";
    private static final int DB_VERSION = 3;
    public SQLiteDatabase db;
    private Context mContext;

    public DatabaseOpenHelper(Context context) {

        super(context, DB_NAME, null, DB_VERSION);
        this.mContext = context;
    }


    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {

    }
    public void createDatabase() {
        createDB();
    }

    private void createDB() {
        boolean dbExist = DBExists();

        if(!dbExist) {
            this.getReadableDatabase();
            copyDBFromResource();
        }
    }

    private boolean DBExists() {
        SQLiteDatabase db = null;

        try {
            String databasePath = DB_PATH + DB_NAME;
            db = SQLiteDatabase.openDatabase(databasePath, null, SQLiteDatabase.OPEN_READWRITE);
            db.setLocale(Locale.getDefault());
            db.setVersion(DB_VERSION);
        } catch (SQLiteException e) {
            Log.e("SqlHelper", "database not found");
        }

        if (db != null) {
            db.close();
        }
        return db != null ? true : false;
    }
    private void copyDBFromResource() {
        InputStream inputStream = null;
        OutputStream outStream = null;
        String dbFilePath = DB_PATH + DB_NAME;

        try {
            inputStream = mContext.getAssets().open(DB_NAME);
            outStream = new FileOutputStream(dbFilePath);

            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) > 0) {
                outStream.write(buffer, 0, length);
            }

            outStream.flush();
            outStream.close();
            inputStream.close();

        } catch (IOException e) {
            throw new Error("Problem copying database from resource file.");
        }

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
