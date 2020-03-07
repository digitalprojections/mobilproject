package furqon.io.github.mobilproject;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity (tableName = "quran_text")
public class ChapterTextTable {
    @PrimaryKey(autoGenerate = true)
    public int id;
    public int sura_id;
    public int verse_id;
    public int favourite;
    public int language_id;
    public int order_no;

    public String ayah_text;
    public String comments_text;
//    public String en_text;
//    public String ru_text;
//    public String uz_text;
//    public String ar_text;
    public String surah_type;
    public int read_count;
    public int share_count;
    public int audio_progress;

    public ChapterTextTable(int sura_id, int verse_id, int favourite, int language_id, int order_no, String ayah_text, String comments_text, String surah_type, int read_count, int share_count, int audio_progress) {
        this.sura_id = sura_id;
        this.verse_id = verse_id;
        this.favourite = favourite;
        this.language_id = language_id;
        this.order_no = order_no;
        this.ayah_text = ayah_text;
        this.comments_text = comments_text;
        this.surah_type = surah_type;
        this.read_count = read_count;
        this.share_count = share_count;
        this.audio_progress = audio_progress;
    }

    public void setId(int id) {
        this.id = id;
    }




}
