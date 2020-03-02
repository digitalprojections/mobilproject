package furqon.io.github.mobilproject;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface ChapterTitleDAO {

    @Insert
    void insert(SurahTitles title);

    @Query("DELETE FROM surah_titles")
    void deleteAll();

    @Query("SELECT arabic_titles.chapterId, arabic_titles.SuraName as arabic, SuraNames.SuraName as uzbek FROM arabic_titles inner join SuraNames on SuraNames.ChapterID = arabic_titles.chapterid")
    LiveData<List<SurahTitles>> getAllTitles();
}
