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
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Map;

@Database(entities = {ChapterTitleTable.class, ChapterTextTable.class, AyahDetailsTable.class, SajdaAyahTable.class, MessageTable.class},
        version =7,
        exportSchema = false)
public abstract class ChapterTitleDatabase extends RoomDatabase {

    public abstract ChapterTitleDAO titleDAO();
    private static ChapterTitleDatabase INSTANCE;
    private static Timestamp timestamp;

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

    //Message data sent directly to the app by token
    public static void SaveMessage(RemoteMessage s){
        MessageTable mt;
        try{
            //String time = DateFormat.getDateTimeInstance().format(new Date(0));
//            String time;
//            Date currentTime = Calendar.getInstance().getTime();
//            Log.e("IN DATABASE", currentTime.toString());
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            String currentDateandTime = sdf.format(new Date());
            mt = new MessageTable(s.getNotification().getTitle(), s.getNotification().getBody(), currentDateandTime);
            new InsertMessageAsyncTask(INSTANCE).execute(mt);
        }catch (Exception x){
            mt = new MessageTable(s.getNotification().getTitle(), s.getNotification().getBody(), "");
            new InsertMessageAsyncTask(INSTANCE).execute(mt);
        }
    }
    public static void SaveMessage(Map<String, String> s){
        MessageTable mt;
            //String time = DateFormat.getDateTimeInstance().format(new Date(0));
//            String time;
//            Date currentTime = Calendar.getInstance().getTime();
//            Log.e("IN DATABASE", currentTime.toString());
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            String currentDateandTime = sdf.format(new Date());
            mt = new MessageTable(s.get("title"), s.get("body"), currentDateandTime);
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
