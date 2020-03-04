package furqon.io.github.mobilproject;

import android.app.Application;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import androidx.lifecycle.LiveData;

import java.util.List;

public class TitleRepository {
    private ChapterTitleDAO mTitleDao;
    private LiveData<List<ChapterTitle>> mAllTitles;
    private Context context;
    HTTPRequestHandler requestHandler;
    MyListener myListener = (MyListener) new SuraNameList();
    TitleRepository(Application application){
        ChapterTitleDatabase titleDatabase = ChapterTitleDatabase.getDatabase(application);
        mTitleDao = titleDatabase.titleDAO();
        mAllTitles = mTitleDao.getAllTitles();
        Log.i("TITLES FOUND:", "--------------------------------- new rows");

        context = application;

        new readAsyncTask(mTitleDao).execute();
    }

    LiveData<List<ChapterTitle>> getmAllTitles(){
        return mAllTitles;
    }
    public void insert(ChapterTitle title){

        new insertAsyncTask(mTitleDao).execute(title);
    }
    public void update(ChapterTitle title){

        new updateAsyncTask(mTitleDao).execute(title);
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
    private class updateAsyncTask extends AsyncTask<ChapterTitle, Void, Void> {
        private ChapterTitleDAO mAsyncTitleDAO;
        public updateAsyncTask(ChapterTitleDAO mTitleDao) {
            mAsyncTitleDAO = mTitleDao;
        }

        @Override
        protected Void doInBackground(ChapterTitle... chapterTitles) {
            mAsyncTitleDAO.update(chapterTitles[0]);
            return null;
        }
    }
    private class readAsyncTask extends AsyncTask<ChapterTitle, Void, Void> {
        private ChapterTitleDAO mAsyncTitleDAO;
        public readAsyncTask(ChapterTitleDAO mTitleDao) {
            mAsyncTitleDAO = mTitleDao;
        }

        @Override
        protected Void doInBackground(ChapterTitle... chapterTitles) {

            int rows = mAsyncTitleDAO.getCount();
            if(rows!=114){
                Log.i("TITLES MISSING", "--------------------------------- rows availabe " + rows);

                myListener.LoadTitlesFromServer();
            }
            return null;
        }
    }

}
