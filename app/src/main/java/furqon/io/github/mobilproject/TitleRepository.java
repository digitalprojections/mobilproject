package furqon.io.github.mobilproject;

import androidx.lifecycle.LiveData;

import java.util.List;

public class TitleRepository {
    private ChapterTitleDAO mTitleDao;
    private LiveData<List<ChapterTitle>> mAllTitles;
}
