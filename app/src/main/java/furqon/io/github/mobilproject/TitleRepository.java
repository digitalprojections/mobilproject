package furqon.io.github.mobilproject;

import android.app.Application;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import androidx.lifecycle.LiveData;

import java.util.List;

public class TitleRepository {
    private ChapterTitleDAO mTitleDao;
    private LiveData<List<ChapterTitleTable>> mAllTitles;
    private LiveData<List<AllTranslations>> mChapterText;
    private LiveData<List<FavouriteAyah>> mFavourites;
    private LiveData<List<RandomSurah>> randomSurah;
    private Context context;

    MyListener myListener = (MyListener) new SuraNameList();
    TitleRepository(Application application){
        ChapterTitleDatabase titleDatabase = ChapterTitleDatabase.getDatabase(application);
        mTitleDao = titleDatabase.titleDAO();

        context = application;

        new readAsyncTask(mTitleDao).execute();
    }
    LiveData<List<ChapterTitleTable>> getAllTitlesByQuranicOrder(){
        mAllTitles = mTitleDao.getAllTitlesQuranicOrder();
        return mAllTitles;
    }
    LiveData<List<ChapterTitleTable>> getAllTitlesByRevelationOrder(){
        mAllTitles = mTitleDao.getAllTitlesByRevelationOrder();
        return mAllTitles;
    }
    LiveData<List<AllTranslations>> getChapterText(String surah_id) {
        mChapterText = mTitleDao.getChapterText(surah_id);
        return mChapterText;
    }
    LiveData<List<FavouriteAyah>> getFavourites() {
        mFavourites = mTitleDao.getFavourites();
        return mFavourites;
    }

    LiveData<List<RandomSurah>> getAvailableSurahIDs(){
        randomSurah = mTitleDao.getAvailableSurahIDs();
        return randomSurah;
    }

    public void insert(ChapterTitleTable title){

        new insertAsyncTask(mTitleDao).execute(title);
    }
    public void insertText(ChapterTextTable text){

        new insertTextAsyncTask(mTitleDao).execute(text);
    }

    public void update(ChapterTitleTable title){

        new updateAsyncTask(mTitleDao).execute(title);
    }
    public void updateText(ChapterTextTable text){
        new updateTextAsyncTask(mTitleDao).execute(text);
    }
    public void deteteAll(){

        new deleteAsyncTask().execute();
    }


    private class insertAsyncTask extends AsyncTask<ChapterTitleTable, Void, Void> {

        private ChapterTitleDAO mAsyncTitleDAO;

        public insertAsyncTask(ChapterTitleDAO mTitleDao) {

            mAsyncTitleDAO = mTitleDao;
        }

        @Override
        protected Void doInBackground(final ChapterTitleTable... chapterTitles) {
            mAsyncTitleDAO.insert(chapterTitles[0]);
            return null;
        }
    }
    private class insertTextAsyncTask extends AsyncTask<ChapterTextTable, Void, Void> {

        private ChapterTitleDAO mAsyncTitleDAO;

        public insertTextAsyncTask(ChapterTitleDAO mTitleDao) {
            mAsyncTitleDAO = mTitleDao;
        }

        @Override
        protected Void doInBackground(final ChapterTextTable... chapterTexts) {
            mAsyncTitleDAO.insertText(chapterTexts[0]);
            return null;
        }
    }
    private class updateAsyncTask extends AsyncTask<ChapterTitleTable, Void, Void> {
        private ChapterTitleDAO mAsyncTitleDAO;
        public updateAsyncTask(ChapterTitleDAO mTitleDao) {
            mAsyncTitleDAO = mTitleDao;
        }

        @Override
        protected Void doInBackground(ChapterTitleTable... chapterTitles) {
            mAsyncTitleDAO.update(chapterTitles[0]);
            return null;
        }
    }
    private class updateTextAsyncTask extends AsyncTask<ChapterTextTable, Void, Void> {
        private ChapterTitleDAO mAsyncTitleDAO;
        public updateTextAsyncTask(ChapterTitleDAO mTitleDao) {

            mAsyncTitleDAO = mTitleDao;
        }

        @Override
        protected Void doInBackground(ChapterTextTable... chapterTexts) {
            mAsyncTitleDAO.updateText(chapterTexts[0]);
            return null;
        }
    }
    private class deleteAsyncTask extends AsyncTask<ChapterTitleTable, Void, Void> {
        private ChapterTitleDAO mAsyncTitleDAO;
        public deleteAsyncTask() {
            mAsyncTitleDAO = mTitleDao;
        }

        @Override
        protected Void doInBackground(ChapterTitleTable... chapterTitles) {
            mAsyncTitleDAO.deleteAll();
            return null;
        }
    }
    private class readAsyncTask extends AsyncTask<ChapterTitleTable, Void, Void> {
        private ChapterTitleDAO mAsyncTitleDAO;
        public readAsyncTask(ChapterTitleDAO mTitleDao) {
            mAsyncTitleDAO = mTitleDao;
        }

        @Override
        protected Void doInBackground(ChapterTitleTable... chapterTitles) {

            int rows = mAsyncTitleDAO.getCount();
            if(rows!=114){
                Log.i("TITLES MISSING", "--------------------------------- rows availabe " + rows);

                myListener.LoadTitlesFromServer();
            }
            return null;
        }
    }

}
