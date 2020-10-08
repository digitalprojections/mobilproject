package furqon.io.github.mobilproject;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "sura_names", indices = {@Index(value = {"chapter_id","language_no"}, unique = true)})
public class AyahRange {
    public AyahRange(int language_no, int order_no, int chapter_id, String uzbek, String arabic, String surah_type) {
        this.language_no = language_no;
        this.chapter_id = chapter_id;
        this.arabic = arabic;
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
    public int chapter_id;
    public String arabic;
    public int d_progress;
    @ColumnInfo(defaultValue = "1")
    public String status;
}

