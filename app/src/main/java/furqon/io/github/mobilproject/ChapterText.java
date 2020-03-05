package furqon.io.github.mobilproject;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity (tableName = "quran_text")
public class ChapterText {

//    "SELECT quz.DatabaseID, quz.SuraID, quz.VerseID, \n" +
//            "qar.AyahText as 'arab', \n" +
//            "quz.AyahText as 'uzb', \n" +
//            "qru.AyahText as 'ru', \n" +
//            "qen.AyahText as 'en', \n" +
//            "(SELECT fav from favourites WHERE sura_id=quz.SuraID and ayah_id=quz.VerseID) as fav, \n" +
//            "sn.SuraName  \n" +
//            "from quran quz\n" +
//            "JOIN quran qar\n" +
//            "ON qar.SuraID = quz.SuraID\n" +
//            "JOIN quran qru\n" +
//            "ON qru.SuraID=qar.SuraID\n" +
//            "JOIN quran qen\n" +
//            "ON qen.SuraID=qru.SuraID\n" +
//            "JOIN SuraNames sn \n" +
//            "ON sn.ChapterID = quz.SuraID\n" +
//            "where \n" +
//            "quz.VerseID=qar.VerseID\n" +
//            "and qru.VerseID=qar.VerseID\n" +
//            "and qen.VerseID=qru.VerseID\n" +
//            "and quz.DatabaseID=120\n" +
//            "and qar.DatabaseID=1\n" +
//            "and qru.DatabaseID=79\n" +
//            "and qen.DatabaseID=59\n" +
//            "and quz.SuraID = "

    @PrimaryKey(autoGenerate = true)
    public int id;
    public int sura_id;
    public int verse_id;
    @ColumnInfo(defaultValue = "0")
    public int favourite;
    public int language_id;
    public String ayah_text;
    public String comments_text;
    public int read_count;

    public int share_count;

    public int audio_progress;



    public ChapterText(int sura_id, int verse_id, int favourite, int language_id, String ayah_text) {
        this.sura_id = sura_id;
        this.verse_id = verse_id;
        this.favourite = favourite;
        this.language_id = language_id;
        this.ayah_text = ayah_text;
        this.read_count = 0;
        this.share_count = 0;
        this.audio_progress = 0;
    }
}
