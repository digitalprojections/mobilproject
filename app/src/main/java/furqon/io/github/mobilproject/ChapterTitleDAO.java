package furqon.io.github.mobilproject;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface ChapterTitleDAO {

    @Query("DELETE FROM sura_names")
    void deleteAll();

    @Query("UPDATE messages SET message_read=1 WHERE message_read != 1")
    void markAllAsRead();

    @Delete
    void deleteMessage(MessageTable messageTable);

    @Query("DELETE FROM quran_text WHERE sura_id like :x")
    void deleteSurah(Integer x);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(ChapterTitleTable title);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertText(ChapterTextTable text);

    @Insert
    void insertMessage(MessageTable text);

    @Query("SELECT COUNT(*) FROM sura_names")
    int getCount();

    @Update
    void update(ChapterTitleTable title);

    @Query("UPDATE sura_names SET status=2 WHERE chapter_id LIKE:suraNomer")
    void updateTitleAsRewarded(String suraNomer);
    @Query("UPDATE sura_names SET status=3 WHERE chapter_id LIKE:suraNomer")
    void updateTitleAsDownloaded(String suraNomer);

    @Query("SELECT * FROM sura_names WHERE chapter_id LIKE:suraNomer")
    LiveData<ChapterTitleTable> getTitle(String suraNomer);

    @Update
    void updateText(ChapterTextTable chapterText);

    @Query("SELECT * FROM sura_names WHERE language_no LIKE:ln ORDER BY chapter_id ASC")
    LiveData<List<ChapterTitleTable>> getAllTitlesByLanguage(String ln);

    @Query("SELECT * FROM sura_names ORDER BY chapter_id ASC")
    LiveData<List<ChapterTitleTable>> getAllTitlesQuranicOrder();

    @Query("SELECT * FROM sura_names ORDER BY order_no ASC")
    LiveData<List<ChapterTitleTable>> getAllTitlesByRevelationOrder();

    //@Query("SELECT * FROM quran_text WHERE sura_id = :surah_id AND language_id IN (:language_list) ORDER BY verse_id ASC")
   //Query("SELECT * FROM quran_text WHERE language_id LIKE 1 AND sura_id = :surah_id ORDER BY verse_id ASC")
    //id,sura_id,language_id,order_no,read_count,share_count,audio_progress
    @Query("SELECT q.id, q.sura_id, q.language_id, q.order_no, q.read_count, q.surah_type, q.share_count, q.audio_progress, q.favourite, q.verse_id, q.ayah_text as ar_text, (SELECT qe.ayah_text FROM quran_text qe WHERE qe.sura_id LIKE :surah_id AND qe.verse_id = q.verse_id AND qe.language_id = 59) as en_text, (SELECT qr.ayah_text FROM quran_text qr WHERE qr.sura_id LIKE :surah_id AND qr.verse_id = q.verse_id AND qr.language_id = 79) as ru_text, (SELECT qu.ayah_text FROM quran_text qu WHERE qu.sura_id LIKE :surah_id AND qu.verse_id = q.verse_id AND qu.language_id = 120) as uz_text FROM quran_text q WHERE q.sura_id LIKE :surah_id AND q.language_id LIKE 1 ORDER BY q.verse_id ASC")
    LiveData<List<AllTranslations>> getChapterText( String surah_id);

    @Query("SELECT q.id, q.sura_id, q.verse_id, q.favourite, q.order_no, q.read_count, q.surah_type, q.share_count, q.audio_progress, qe.ayah_text as en_text, qr.ayah_text as ru_text, qu.ayah_text as uz_text,  q.ayah_text as ar_text FROM quran_text q JOIN quran_text qe ON qe.sura_id = q.sura_id AND qe.verse_id = q.verse_id JOIN quran_text qr ON qr.sura_id = q.sura_id AND qr.verse_id = q.verse_id JOIN quran_text qu ON qu.sura_id=q.sura_id AND qu.verse_id = q.verse_id WHERE q.favourite LIKE 1 AND q.language_id = 1 AND qr.language_id = 79 AND qe.language_id = 59 AND qu.language_id = 120 ORDER BY q.sura_id, q.verse_id")
    LiveData<List<FavouriteAyah>> getFavourites();

   @Query("SELECT sura_id FROM quran_text WHERE 1 GROUP BY sura_id")
   LiveData<List<RandomSurah>> getAvailableSurahIDs();

   @Query("SELECT * FROM messages WHERE 1 order by id DESC")
   LiveData<List<MessageTable>> getMessages();

   @Query("SELECT message_title as mTitle, message_read as unread FROM messages WHERE message_read = 0")
    LiveData<List<NewMessages>> getUnreadMessages();

   @Query("SELECT id, verse_id, sura_id, language_id, share_count, ayah_text as ar_text, audio_progress FROM quran_text WHERE language_id=1 AND sura_id =:suraid AND verse_id BETWEEN :start AND :end ORDER BY verse_id ASC")
    LiveData<List<AyahRange>> getAyahRange(String suraid, String start, String end);

    @Query("UPDATE quran_text SET audio_progress=:audioProgress WHERE language_id=1 AND sura_id=:suraNumber AND verse_id=:ayahNumber")
    void update(int suraNumber, int ayahNumber, int audioProgress);
}
