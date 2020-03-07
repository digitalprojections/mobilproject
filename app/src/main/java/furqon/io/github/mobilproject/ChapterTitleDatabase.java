package furqon.io.github.mobilproject;

import android.content.Context;
import android.os.AsyncTask;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;

@Database(entities = {ChapterTitleTable.class, ChapterTextTable.class}, version =4, exportSchema = false)
public abstract class ChapterTitleDatabase extends RoomDatabase {

    public abstract ChapterTitleDAO titleDAO();
    private static ChapterTitleDatabase INSTANCE;

    public static ChapterTitleDatabase getDatabase(final Context context) {

        synchronized (ChapterTitleDatabase.class) {
            if (INSTANCE == null) {
                // Create database here
                INSTANCE = Room.databaseBuilder(context,
                        ChapterTitleDatabase.class, "quran00_db")
                        //.createFromAsset("databases/qurandb")
                        .fallbackToDestructiveMigration()
                        .addCallback(callback)
                        .build();
            }


        }
        return INSTANCE;
    }

    private static RoomDatabase.Callback callback = new RoomDatabase.Callback(){
        @Override
        public void onCreate(@NonNull SupportSQLiteDatabase db) {
            super.onCreate(db);
            new PopulateDBAsync(INSTANCE).execute();
        }
    };

    public static class PopulateDBAsync extends AsyncTask<Void, Void, Void>{
        private ChapterTitleDAO titleDAO;

        public PopulateDBAsync(ChapterTitleDatabase db){

            titleDAO = db.titleDAO();
        }

        @Override
        protected Void doInBackground(Void... voids) {
            //titleDAO.insert(new ChapterTitle(10, 1, 2, "uzbektitle", "arabictitle", "makka"));


            return null;
        }
    }
}
