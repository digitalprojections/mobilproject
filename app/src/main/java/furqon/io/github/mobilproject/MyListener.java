package furqon.io.github.mobilproject;

public interface MyListener {
    void DownloadThis(String suraNumber);
    //void EnableThis(String suraNumber);
    void LoadTitlesFromServer();
    void insertTitle(ChapterTitleTable title);
    void MarkAsAwarded(int surah_id);
    void MarkAsDownloaded(int surah_id);
    void MarkAsDownloading(int surah_id);


}
