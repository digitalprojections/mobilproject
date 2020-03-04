package furqon.io.github.mobilproject;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface ChapterTitleDAO {

    @Query("DELETE FROM sura_names")
    void deleteAll();

    @Insert
    void insert(ChapterTitle title);

    @Query("SELECT COUNT(*) FROM sura_names")
    int getCount();

    @Update
    void update(ChapterTitle title);

    @Query("SELECT * FROM sura_names ORDER BY chapter_id ASC")
    LiveData<List<ChapterTitle>> getAllTitles();
}
