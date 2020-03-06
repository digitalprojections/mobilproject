package furqon.io.github.mobilproject;

import androidx.room.Ignore;

public class AllTranslations{
    public int id;
    public int sura_id;
    public int verse_id;
    public int favourite;
    public int language_id;
    public int order_no;

    @Ignore
    public String ayah_text;
    @Ignore
    public String comments_text;
    public String surah_type;
    public int read_count;

    public String en_text;
    public String ru_text;
    public String uz_text;
    public String ar_text;

    public int share_count;

    public int audio_progress;
}