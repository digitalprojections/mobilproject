package furqon.io.github.mobilproject;

import java.util.List;

public interface MyListener {
    void DownloadThis(String suraNumber);
    //void EnableThis(String suraNumber);
    void LoadTitlesFromServer();
    void insertTitle(ChapterTitle title);
    void MarkAsAwarded(int surah_id);
    void MarkAsDownloaded(int surah_id);


}
