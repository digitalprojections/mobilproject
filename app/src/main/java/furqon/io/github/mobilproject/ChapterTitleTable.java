package furqon.io.github.mobilproject;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "sura_names", indices = {@Index(value = {"chapter_id","language_no"}, unique = true)})
public class ChapterTitleTable {
    public ChapterTitleTable(int language_no, int order_no, int chapter_id, String uzbek, String arabic, String surah_type) {
        this.language_no = language_no;
        this.order_no = order_no;
        this.chapter_id = chapter_id;
        this.uzbek = uzbek;
        this.arabic = arabic;
        this.surah_type = surah_type;
        this.status = "1";
        this.d_progress = 0;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @PrimaryKey(autoGenerate = true)
    public int id;
    public int language_no;
    public int order_no;
    public int chapter_id;
    public String uzbek;
    public String arabic;
    public String surah_type;

    public int d_progress;

    @ColumnInfo(defaultValue = "1")
    public String status;
}

