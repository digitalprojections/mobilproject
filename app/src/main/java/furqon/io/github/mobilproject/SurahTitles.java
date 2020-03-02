package furqon.io.github.mobilproject;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "surah_titles")
public class SurahTitles {
    public int languageNo;
    public int orderNo;
    @ColumnInfo(name = "ChapterID")
    public int chapterId;
    @ColumnInfo(name="SuraName")
    public String title;
    @ColumnInfo(name="arabic_title")
    public String artitle;
    @PrimaryKey
    public int id;
    public String surahtype;
    public int status;
}
