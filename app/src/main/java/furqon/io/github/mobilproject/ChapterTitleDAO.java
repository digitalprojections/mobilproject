package furqon.io.github.mobilproject;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface ChapterTitleDAO {

    @Query("DELETE FROM suranames")
    void deleteAll();

//    @Query("SELECT arabic_titles.ChapterID, " +
//            "arabic_titles.SuraName as arabic, " +
//            "SuraNames.SuraName as uzbek " +
//            "FROM arabic_titles, SuraNames " +
//            "WHERE " +
//            "SuraNames.ChapterID = arabic_titles.ChapterID")
    @Query("SELECT ChapterID, " +
            "SuraName as arabic, " +
            "SuraName as uzbek " +
            "FROM SuraNames ")
    public LiveData<List<SurahTitles>> getAllTitles();
}
