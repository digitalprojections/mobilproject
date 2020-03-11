package furqon.io.github.mobilproject;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import java.util.List;

public class TitleViewModel extends AndroidViewModel {
    private TitleRepository titleRepository;
    private LiveData<List<ChapterTitleTable>> mAllTitles;
    private LiveData<List<AllTranslations>> mChapterText;
    private LiveData<List<FavouriteAyah>> mFavourites;
    private LiveData<List<RandomSurah>> randomSurah;
    private LiveData<List<MessageTable>> liveMessages;

    private sharedpref sharedPref;

    public TitleViewModel(@NonNull Application application) {
        super(application);
        sharedPref = sharedpref.getInstance();
        titleRepository = new TitleRepository(application);
    }
    LiveData<List<FavouriteAyah>> getFavourites(){
        mFavourites = titleRepository.getFavourites();
        return mFavourites;
    }

    LiveData<List<AllTranslations>> getChapterText(String surah_id){
        mChapterText = titleRepository.getChapterText(surah_id);
        return mChapterText;
    }

    LiveData<List<ChapterTitleTable>> getAllTitles(){
        if(sharedPref.read(sharedPref.displayOrder, 0)==0){
            mAllTitles = titleRepository.getAllTitlesByQuranicOrder();
        }else{
            mAllTitles = titleRepository.getAllTitlesByRevelationOrder();
        }
        return mAllTitles;
    }
    LiveData<List<RandomSurah>> getRandomSurah(){
        randomSurah = titleRepository.getAvailableSurahIDs();
        return randomSurah;
    }
    LiveData<List<MessageTable>> getMessages(){
        liveMessages = titleRepository.getMessages();
        return liveMessages;
    }

    public void insert(ChapterTitleTable title){
        Log.d("TITLE insert", title.uzbek);
        titleRepository.insert(title);
    }
    public void insertText(ChapterTextTable text){
        //Log.d("TITLE insert", text);
        titleRepository.insertText(text);
    }

    public void update(ChapterTitleTable title){

        titleRepository.update(title);
    }
    public void updateText(ChapterTextTable text){

        titleRepository.updateText(text);
    }

    public void deleteAll(){

        titleRepository.deteteAll();
    }
    public void markAllAsRead(){
        titleRepository.markAllAsRead();
    }

    public void deleteMessage(MessageTable message){
        titleRepository.deleteMessage(message);
    }
}
