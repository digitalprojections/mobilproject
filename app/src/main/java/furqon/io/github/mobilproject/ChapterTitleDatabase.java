package furqon.io.github.mobilproject;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.google.firebase.messaging.RemoteMessage;

import java.sql.Timestamp;
import java.util.Map;

@Database(entities = {ChapterTitleTable.class, ChapterTextTable.class, AyahDetailsTable.class, SajdaAyahTable.class, MessageTable.class},
        version =2,
        exportSchema = false)
public abstract class ChapterTitleDatabase extends RoomDatabase {

    public abstract ChapterTitleDAO titleDAO();
    private static ChapterTitleDatabase INSTANCE;

    public static ChapterTitleDatabase getDatabase(final Context context) {

        synchronized (ChapterTitleDatabase.class) {
            if (INSTANCE == null) {
                // Create database here
                INSTANCE = Room.databaseBuilder(context,
                        ChapterTitleDatabase.class, "quran_dynamic")
                        .createFromAsset("databases/quran_dynamic")
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

    public static void SaveMessage(RemoteMessage s){

        long ts = System.currentTimeMillis()/1000;
        MessageTable mt = new MessageTable(s.getNotification().getTitle(), s.getNotification().getBody(), String.valueOf(ts));
        new InsertMessageAsyncTask(INSTANCE).execute(mt);
    }

    private static class InsertMessageAsyncTask extends AsyncTask<MessageTable, Void, Void> {

        private ChapterTitleDAO mAsyncTitleDAO;

        public InsertMessageAsyncTask(ChapterTitleDatabase database) {

            mAsyncTitleDAO = database.titleDAO();
        }

        @Override
        protected Void doInBackground(final MessageTable... messageTables) {
            mAsyncTitleDAO.insertMessage(messageTables[0]);
            Log.e("IN DATABASE", messageTables[0].message_body);
            return null;
        }
    }


    public static class PopulateDBAsync extends AsyncTask<Void, Void, Void>{
        ChapterTitleDAO titleDAO;
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
