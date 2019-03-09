package furqon.io.github.mobilproject;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseAccess {
    private SQLiteOpenHelper openHelper;
    private SQLiteDatabase db;
    private static DatabaseAccess instance;
    private Cursor c = null;

    private DatabaseAccess(Context context){
        this.openHelper = new DatabaseOpenHelper(context);
    }

    static DatabaseAccess getInstance(Context context){
        if(instance==null){
            instance = new
                    DatabaseAccess(context);

        }
        return instance;
    }
    //open
    void open(){
        this.db = openHelper.getReadableDatabase();
    }
    public void close(){
        if(db!=null){
            this.db.close();
        }
    }
    Cursor getSuraTitles(){
        c= db.rawQuery("SELECT at.ChapterID, at.SuraName as 'arabic', sn.SuraName as 'uzbek'  FROM arabic_titles at inner join suranames sn on sn.chapterid = at.chapterid", new String[]{});

    return c;

    }
}
