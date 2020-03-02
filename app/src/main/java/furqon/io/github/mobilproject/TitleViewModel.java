package furqon.io.github.mobilproject;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import java.util.List;

public class TitleViewModel extends AndroidViewModel {
    private TitleRepository titleRepository;
    private LiveData<List<SurahTitles>> mAllTitles;
    public TitleViewModel(@NonNull Application application) {
        super(application);
        titleRepository = new TitleRepository(application);
        mAllTitles = titleRepository.getmAllTitles();
    }

    LiveData<List<SurahTitles>> getAllTitles(){return mAllTitles;}

    public void insert(ChapterTitle title){titleRepository.insert(title);}
}
