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

    @Insert
    void insertText(ChapterText text);

    @Query("SELECT COUNT(*) FROM sura_names")
    int getCount();

    @Update
    void update(ChapterTitle title);
    @Update
    void updateText(ChapterText chapterText);

    @Query("SELECT * FROM sura_names ORDER BY chapter_id ASC")
    LiveData<List<ChapterTitle>> getAllTitlesQuranicOrder();

    @Query("SELECT * FROM sura_names ORDER BY order_no ASC")
    LiveData<List<ChapterTitle>> getAllTitlesByRevelationOrder();

    //@Query("SELECT * FROM quran_text WHERE sura_id = :surah_id AND language_id IN (:language_list) ORDER BY verse_id ASC")
   //Query("SELECT * FROM quran_text WHERE language_id LIKE 1 AND sura_id = :surah_id ORDER BY verse_id ASC")
    //id,sura_id,language_id,order_no,read_count,share_count,audio_progress
    @Query("SELECT q.id, q.sura_id, q.language_id, q.order_no, q.read_count, q.surah_type, q.share_count, q.audio_progress, q.favourite, q.verse_id, q.ayah_text as ar_text, (SELECT qe.ayah_text FROM quran_text qe WHERE qe.sura_id LIKE :surah_id AND qe.verse_id = q.verse_id AND qe.language_id = 59) as en_text, (SELECT qr.ayah_text FROM quran_text qr WHERE qr.sura_id LIKE :surah_id AND qr.verse_id = q.verse_id AND qr.language_id = 79) as ru_text, (SELECT qu.ayah_text FROM quran_text qu WHERE qu.sura_id LIKE :surah_id AND qu.verse_id = q.verse_id AND qu.language_id = 120) as uz_text FROM quran_text q WHERE q.sura_id LIKE :surah_id AND q.language_id LIKE 1 ORDER BY q.verse_id ASC")
    LiveData<List<AllTranslations>> getChapterText( String surah_id);

    @Query("SELECT q.sura_id, q.verse_id, q.favourite, q.language_id, q.order_no, q.read_count, q.surah_type, q.share_count, q.audio_progress, q.ayah_text as ar_text, (SELECT qe.ayah_text FROM quran_text qe WHERE qe.sura_id = q.sura_id AND qe.verse_id = q.verse_id AND qe.language_id = 59) as en_text, (SELECT qr.ayah_text FROM quran_text qr WHERE qr.sura_id = q.sura_id AND qr.verse_id = q.verse_id AND qr.language_id = 79) as ru_text, (SELECT qu.ayah_text FROM quran_text qu WHERE qu.sura_id  = q.sura_id AND qu.verse_id = q.verse_id AND qu.language_id = 120) as uz_text FROM quran_text q WHERE q.sura_id = q.sura_id AND q.language_id LIKE 1 and q.favourite LIKE 1 ORDER BY q.verse_id ASC")
    LiveData<List<FavouriteAyah>> getFavourites();


}
