package furqon.io.github.mobilproject;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import java.util.List;

public class TitleViewModel extends AndroidViewModel {
    private TitleRepository titleRepository;
    private LiveData<List<ChapterTitle>> mAllTitles;
    private LiveData<List<AllTranslations>> mChapterText;
    private LiveData<List<FavouriteAyah>> mFavourites;
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

    LiveData<List<ChapterTitle>> getAllTitles(){
        if(sharedPref.read(sharedPref.displayOrder, 0)==0){
            mAllTitles = titleRepository.getAllTitlesByQuranicOrder();
        }else{
            mAllTitles = titleRepository.getAllTitlesByRevelationOrder();
        }
        return mAllTitles;
    }

    public void insert(ChapterTitle title){
        Log.d("TITLE insert", title.uzbek);
        titleRepository.insert(title);
    }
    public void insertText(ChapterText text){
        //Log.d("TITLE insert", text);
        titleRepository.insertText(text);
    }

    public void update(ChapterTitle title){
        Log.d("TITLE UPDATING", "UUUUUUUUUU " + title.status);
        titleRepository.update(title);
    }

    public void deleteAll(){

        titleRepository.deteteAll();
    }
}
