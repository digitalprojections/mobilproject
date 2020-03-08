package furqon.io.github.mobilproject;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class AyahDetailsTable {
    @PrimaryKey(autoGenerate = true)
    public int id;
    public int read_count;
    public int share_count;
    public int audio_progress;
    public int verse_id;
    public int surah_id;

    public AyahDetailsTable() {
    }

    public void setId(int id) {
        this.id = id;
    }
}
