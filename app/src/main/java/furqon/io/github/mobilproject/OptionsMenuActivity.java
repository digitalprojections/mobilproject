package furqon.io.github.mobilproject;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.dynamiclinks.DynamicLink;
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks;
import com.google.firebase.dynamiclinks.ShortDynamicLink;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;

public class OptionsMenuActivity extends AppCompatActivity {

    private static final String TAG = "OPTIONS MENU";
    private FirebaseRemoteConfig mFirebaseRemoteConfig;
    private SharedPreferences mSharedPref;
    private FirebaseAuth mAuth;
    FirebaseUser currentUser;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_options_menu);

        mSharedPref = SharedPreferences.getInstance();
        mSharedPref.init(getApplicationContext());
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();

        mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance();
        FirebaseRemoteConfigSettings configSettings = new FirebaseRemoteConfigSettings.Builder()
                .setMinimumFetchIntervalInSeconds(3600)
                .build();
        mFirebaseRemoteConfig.setConfigSettingsAsync(configSettings);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();

        inflater.inflate(R.menu.options_menu, menu);
        Log.i(TAG, "menu ");
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.about_i:
                open_about();
                return true;
            case R.id.search_i:
                open_search();
                return true;
            case R.id.youtube_i:
                open_youtube();
                return true;
            case R.id.invite_i:
                shareDeepLink();
                return true;
            case R.id.settings_i:
                open_settings();
                return true;
            case R.id.favourites_i:
                open_favourites();
                return true;
//            case R.id.messages_i:
//                open_messages();
//                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }
    private void open_settings() {
        Log.d(TAG, "SETTINGS ITEM");
        Intent intent;
        intent = new Intent(this, Settings.class);
        startActivity(intent);
    }
    private void open_favourites() {
        Intent intent;
        intent = new Intent(this, Favourites.class);
        startActivity(intent);
    }

    private void open_youtube() {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(mFirebaseRemoteConfig.getString("youtube_video")));
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        }
    }
    private void open_search() {
        Intent intent;
        intent = new Intent(this, SearchActivity.class);
        startActivity(intent);
    }
    private void open_chatroom() {
        Toast.makeText(getApplicationContext(), R.string.coming_soon, Toast.LENGTH_SHORT).show();
    }

    private void open_earn_coins() {
        //Toast.makeText(getApplicationContext(), R.string.coming_soon, Toast.LENGTH_SHORT).show();
        Intent intent;
        intent = new Intent(this, EarnCoinsActivity.class);
        startActivity(intent);
    }
    private void open_about() {
        Intent intent = new Intent(this, AboutActivity.class);
        //intent.setData(Uri.parse(mFirebaseRemoteConfig.getString("youtube_video")));
        startActivity(intent);
    }
    private void shareDeepLink() {
        createShortLink();
    }
    public Uri buildDeepLink(Uri dl, int version) {
        String uriPrefix = "furqon.page.link";

        if(currentUser!=null){
            Log.i(TAG, "CURRENT USER ID " + currentUser.getUid());
            //userId=currentUser.getUid()
            if (mSharedPref != null && !mSharedPref.read(mSharedPref.CREDS_ALREADY_SENT, false)) {
                mSharedPref.write(mSharedPref.USERID, currentUser.getUid());
                //checkAppSignature(this);
            }

        }

        DynamicLink.Builder builder = FirebaseDynamicLinks.getInstance()
                .createDynamicLink()
                .setDomainUriPrefix(uriPrefix)
                .setAndroidParameters(new DynamicLink.AndroidParameters.Builder()
                        .setMinimumVersion(version)
                        .build())
                .setLink(dl);

        // Build the dynamic link
        DynamicLink link = builder.buildDynamicLink();
        // [END build_dynamic_link]


        // Return the dynamic link as a URI
        return link.getUri();
    }

    public void createShortLink() {
        // [START create_short_link]
        if(currentUser!=null){
            String val = "https://quran-kareem.web.app/?user_id="+currentUser.getUid();
            Task<ShortDynamicLink> shortLinkTask = FirebaseDynamicLinks.getInstance().createDynamicLink()
                    //.setLink(Uri.parse("https://furqon.page.link/ThB2"))
                    .setLink(Uri.parse(val))
                    .setDomainUriPrefix("https://furqon.page.link")
                    .setAndroidParameters(
                            new DynamicLink.AndroidParameters.Builder("furqon.io.github.mobilproject")
                                    .setMinimumVersion(0)
                                    .build())

                    // Set parameters
                    // ...
                    .buildShortDynamicLink()
                    .addOnCompleteListener(this, new OnCompleteListener<ShortDynamicLink>() {
                        @Override
                        public void onComplete(@NonNull Task<ShortDynamicLink> task) {

                            if (task.isSuccessful()) {
                                // Short link created

                                Uri shortLink = task.getResult().getShortLink();
                                Uri flowchartLink = task.getResult().getPreviewLink();
                                Log.i(TAG, "SHORT LINK " + shortLink.getPath());
                                Intent intent = new Intent(Intent.ACTION_SEND);
                                intent.setType("text/plain");
                                intent.putExtra(Intent.EXTRA_SUBJECT, R.string.quran_kareem_title);
                                intent.putExtra(Intent.EXTRA_TEXT, shortLink.toString());
                                startActivity(intent);
                                //Log.i("SHARE", deepLink.getPath());

                            } else {
                                // Error
                                // ...

                            }


                        }
                    });
        }
        // [END create_short_link]
    }

}
