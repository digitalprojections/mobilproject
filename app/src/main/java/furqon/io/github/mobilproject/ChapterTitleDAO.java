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
    LiveData<List<ChapterTitle>> getAllTitlesQuranicOrder();

    @Query("SELECT * FROM sura_names ORDER BY order_no ASC")
    LiveData<List<ChapterTitle>> getAllTitlesByRevelationOrder();

    //@Query("SELECT * FROM quran_text WHERE sura_id = :surah_id AND language_id IN (:language_list) ORDER BY verse_id ASC")
    @Query("SELECT * FROM quran_text WHERE language_id IN (:language_list) AND sura_id = :surah_id ORDER BY verse_id ASC")
    LiveData<List<ChapterText>> getChapterText( String surah_id, List<String> language_list);
}
