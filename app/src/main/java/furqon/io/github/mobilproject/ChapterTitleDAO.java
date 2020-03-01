package furqon.io.github.mobilproject;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface ChapterTitleDAO {

    @Insert
    void insert(ChapterTitle title);

    @Query("DELETE FROM SuraNames")
    void deleteAll();

    @Query("SELECT * from SuraNames ORDER BY chapterId ASC")
    LiveData<List<ChapterTitle>> getAllTitles();
}
