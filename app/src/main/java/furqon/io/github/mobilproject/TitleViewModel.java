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
    public TitleViewModel(@NonNull Application application) {
        super(application);
        titleRepository = new TitleRepository(application);
        mAllTitles = titleRepository.getmAllTitles();
    }

    LiveData<List<ChapterTitle>> getAllTitles(){

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
