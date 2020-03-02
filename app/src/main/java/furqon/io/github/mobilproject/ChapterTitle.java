package furqon.io.github.mobilproject;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "SuraNames")
public class ChapterTitle {
    public int languageNo;
    public int orderNo;
    @ColumnInfo(name = "ChapterID")
    public int chapterId;
    @ColumnInfo(name="SuraName")
    public String title;
    @PrimaryKey
    public int id;
    public String surahtype;
    public int status;
}

