package furqon.io.github.mobilproject;

import java.util.List;

public interface MyListener {
    void DownloadThis(String suraNumber);
    void EnableThis(String suraNumber);
    void LoadTitlesFromServer();

    void DisableRefreshButton();


}
