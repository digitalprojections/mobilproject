package furqon.io.github.mobilproject;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {ChapterTitle.class}, version = 1, exportSchema = false)
public abstract class ChapterTitleDatabase extends RoomDatabase {

    public abstract ChapterTitleDAO titleDAO();
    private static ChapterTitleDatabase INSTANCE;

    public static ChapterTitleDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (ChapterTitleDatabase.class) {
                if (INSTANCE == null) {
                    // Create database here
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            ChapterTitleDatabase.class, "qurandb")
                            // Wipes and rebuilds instead of migrating
                            // if no Migration object.
                            // Migration is not part of this practical.
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}
