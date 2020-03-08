package furqon.io.github.mobilproject;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class SajdaAyahTable {
    @PrimaryKey(autoGenerate = true)
    public int id;
    public Boolean recommended;
    public Boolean obligatory;
    public int number;
    public int surah_id;
    public int verse_id;
    public int juz;
    public int manzil;
    public int page;
    public int ruku;
    public int hizbQuarter;



    public String name;
    public int englishName;
    public int englishNameTranslation;
    public int revelationType;

    public SajdaAyahTable() {
    }

    public void setId(int id) {
        this.id = id;
    }
}
