package furqon.io.github.mobilproject;

import android.app.Application;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import androidx.lifecycle.LiveData;

import java.util.List;
import java.util.Map;

public class TitleRepository {
    private ChapterTitleDAO mTitleDao;
    private LiveData<List<ChapterTitleTable>> mAllTitles;
    private LiveData<List<AllTranslations>> mChapterText;
    private LiveData<List<FavouriteAyah>> mFavourites;
    private LiveData<List<RandomSurah>> randomSurah;
    private LiveData<List<MessageTable>> liveMessages;
    private LiveData<List<NewMessages>> liveUnreadMessages;
    private Context context;

    //MyListener myListener = (MyListener) new SuraNameList();
    TitleRepository(Application application){
        ChapterTitleDatabase titleDatabase = ChapterTitleDatabase.getDatabase(application);
        mTitleDao = titleDatabase.titleDAO();

        context = application;

        //new readAsyncTask(mTitleDao).execute();
    }
    LiveData<List<NewMessages>> getUnreadMessages(){
        liveUnreadMessages = mTitleDao.getUnreadMessages();
        return liveUnreadMessages;
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
    LiveData<List<MessageTable>> getMessages(){
        liveMessages = mTitleDao.getMessages();
        return liveMessages;
    }

    LiveData<ChapterTitleTable> getTitle(String suraNomer){

        return mTitleDao.getTitle(suraNomer);
    }

    public void insert(ChapterTitleTable title){

        new insertAsyncTask(mTitleDao).execute(title);
    }
    public void insertText(ChapterTextTable text){

        new insertTextAsyncTask(mTitleDao).execute(text);
    }

    public void updateTitleAsRewarded(String suraNomer){
        new updateTitleRewardedAsyncTask(mTitleDao).execute(suraNomer);
    }
    public void updateTitleAsDownloaded(String suraNomer){
        new updateTitleDownloadedAsyncTask(mTitleDao).execute(suraNomer);
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
    public void markAllAsRead(){
        new markAsReadAsync().execute();
    }

    public void deleteMessage(MessageTable message){
        new deleteMessageAsyncTask(mTitleDao).execute(message);
    }

    public void deteteSurah(int x) {
        new deleteSurahAsyncTask(mTitleDao).execute(x);
    }
    private class deleteSurahAsyncTask extends AsyncTask<Integer, Void, Void>{
        private ChapterTitleDAO mAsyncTitleDAO;
        public deleteSurahAsyncTask(ChapterTitleDAO dao){
            mAsyncTitleDAO = dao;
        }


        @Override
        protected Void doInBackground(Integer... integers) {
            mAsyncTitleDAO.deleteSurah(integers[0]);
            return null;
        }
    }

    private class deleteMessageAsyncTask extends AsyncTask<MessageTable, Void, Void>{
        private ChapterTitleDAO mAsyncTitleDAO;
        public deleteMessageAsyncTask(ChapterTitleDAO dao){
            mAsyncTitleDAO = dao;
        }

        @Override
        protected Void doInBackground(MessageTable... messageTables) {
            mAsyncTitleDAO.deleteMessage(messageTables[0]);
            return null;
        }
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
    private class updateTitleDownloadedAsyncTask extends AsyncTask<String, Void, Void> {
        private ChapterTitleDAO mAsyncTitleDAO;
        public updateTitleDownloadedAsyncTask(ChapterTitleDAO mTitleDao) {
            mAsyncTitleDAO = mTitleDao;
        }
        @Override
        protected Void doInBackground(String... suraNomer) {
            mAsyncTitleDAO.updateTitleAsDownloaded(suraNomer[0]);
            return null;
        }
    }

    private class updateTitleRewardedAsyncTask extends AsyncTask<String, Void, Void> {
        private ChapterTitleDAO mAsyncTitleDAO;
        public updateTitleRewardedAsyncTask(ChapterTitleDAO mTitleDao) {
            mAsyncTitleDAO = mTitleDao;
        }
        @Override
        protected Void doInBackground(String... suraNomer) {
            mAsyncTitleDAO.updateTitleAsRewarded(suraNomer[0]);
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
    private class markAsReadAsync extends AsyncTask<MessageTable, Void, Void> {
        private ChapterTitleDAO mAsyncTitleDAO;
        public markAsReadAsync() {
            mAsyncTitleDAO = mTitleDao;
        }

        @Override
        protected Void doInBackground(MessageTable... messages) {
            mAsyncTitleDAO.markAllAsRead();
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

//            int rows = mAsyncTitleDAO.getCount();
//            if(rows!=114){
//                //myListener.LoadTitlesFromServer();
//            }
            return null;
        }
    }

}
