package furqon.io.github.mobilproject;

import android.app.Application;
import android.os.AsyncTask;

import androidx.lifecycle.LiveData;

import java.util.List;

public class TitleRepository {
    private ChapterTitleDAO mTitleDao;
    private LiveData<List<ChapterTitle>> mAllTitles;

    TitleRepository(Application application){
        ChapterTitleDatabase titleDatabase = ChapterTitleDatabase.getDatabase(application);
        mTitleDao = titleDatabase.titleDAO();
        mAllTitles = mTitleDao.getAllTitles();
    }

    LiveData<List<ChapterTitle>> getmAllTitles(){
        return mAllTitles;
    }
    public void insert(ChapterTitle title){
        new insertAsyncTask(mTitleDao).execute(title);
    }

    private class insertAsyncTask extends AsyncTask<ChapterTitle, Void, Void> {

        private ChapterTitleDAO mAsyncTitleDAO;

        public insertAsyncTask(ChapterTitleDAO mTitleDao) {
            mAsyncTitleDAO = mTitleDao;
        }

        @Override
        protected Void doInBackground(final ChapterTitle... chapterTitles) {
            mAsyncTitleDAO.insert(chapterTitles[0]);
            return null;
        }
    }
}
