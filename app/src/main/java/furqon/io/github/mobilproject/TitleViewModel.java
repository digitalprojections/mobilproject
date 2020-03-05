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
    private LiveData<List<ChapterText>> mChapterText;
    private sharedpref sharedPref;

    public TitleViewModel(@NonNull Application application) {
        super(application);
        sharedPref = sharedpref.getInstance();
        titleRepository = new TitleRepository(application);
    }

    LiveData<List<ChapterText>> getChapterText(String surah_id, List<String> language_list){
        mChapterText = titleRepository.getChapterText(surah_id, language_list);
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
    public void update(ChapterTitle title){
        Log.d("TITLE UPDATING", "UUUUUUUUUU " + title.status);
        titleRepository.update(title);
    }

    public void deleteAll(){

        titleRepository.deteteAll();
    }
}
