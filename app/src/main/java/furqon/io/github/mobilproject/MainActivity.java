package furqon.io.github.mobilproject;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.crashlytics.android.Crashlytics;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.appindexing.Action;
import com.google.firebase.appindexing.FirebaseUserActions;
import com.google.firebase.appindexing.builders.Actions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.dynamiclinks.DynamicLink;
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks;
import com.google.firebase.dynamiclinks.ShortDynamicLink;
import com.google.firebase.functions.FirebaseFunctions;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;

import java.io.File;
import java.util.Locale;

import io.fabric.sdk.android.Fabric;

public class MainActivity extends AppCompatActivity {
    Uri deepLink;
    Button suralar_but;
    Button extra_btn;
    Button davomi_but;
    ImageView imageView;
    private Handler handler;
    // Try to use more data here. ANDROID_ID is a single point of attack.
    //InterstitialAd mInterstitialAd;
    private FirebaseAnalytics mFirebaseAnalytics;
    private static final String TAG = "MAIN ACTIVITY";
    private static final String DEEP_LINK_URL = "https://furqon.page.link/ThB2";
    private sharedpref mSharedPref;
    private boolean randomayahshown;
    private FirebaseRemoteConfig mFirebaseRemoteConfig;
    private FirebaseFunctions mFunctions;
    private FirebaseAuth mAuth;
    FirebaseUser currentUser;
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();

        inflater.inflate(R.menu.options_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.invite_i:
                shareDeepLink();
                return true;
            case R.id.settings_i:
                open_settings();
                return true;
            case R.id.favourites_i:
                open_favourites();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }


    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.tool_bar);
        setSupportActionBar(toolbar);

        mSharedPref = sharedpref.getInstance();


        mFunctions = FirebaseFunctions.getInstance();
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();

        Fabric.with(this, new Crashlytics());
        Crashlytics.log("Activity created");


        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

        MobileAds.initialize(this, getString(R.string.addmob_app_id));

        handler = new Handler();

        suralar_but = findViewById(R.id.suralar);
        extra_btn = findViewById(R.id.extra_button);
        davomi_but = findViewById(R.id.davomi);

        imageView = findViewById(R.id.imageView);
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ayahOfTheDay();
            }
        });

        //day_but = findViewById(R.id.ayahoftheday);
        suralar_but.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openSuraNames(view);
            }
        });
        extra_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                open_extraActivities();
            }
        });
        davomi_but.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                continueReading();
            }
        });

        if (mSharedPref.contains(mSharedPref.XATCHUP)) {
            davomi_but.setVisibility(View.VISIBLE);
        } else {
            davomi_but.setVisibility(View.GONE);
        }

        //String deviceId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);

        try {
            File dir = this.getCacheDir();
            deleteDir(dir);
        } catch (Exception e) {
            e.printStackTrace();
            Crashlytics.log(Log.ERROR, TAG, "NPE caught");
            Crashlytics.logException(e);
        }
// Create a deep link and display it in the UI
        if(deepLink==null){
            deepLink = buildDeepLink(Uri.parse(DEEP_LINK_URL), 0);
        }

//        Intent intent = new Intent(getApplicationContext(), AudioPlayerService.class);
//        intent.setAction(AudioPlayerService.ACTION_PLAY);
//        startService(intent);
        // ATTENTION: This was auto-generated to handle app links.
        Intent appLinkIntent = getIntent();
        String appLinkAction = appLinkIntent.getAction();
        Uri appLinkData = appLinkIntent.getData();

        Log.i(TAG, Locale.getDefault().getDisplayLanguage());
    }


    private void open_extraActivities() {
        Intent intent;
        intent = new Intent(this, ExtraActivity.class);
        startActivity(intent);
    }

    private void open_settings() {
        Intent intent;
        intent = new Intent(this, Settings.class);
        startActivity(intent);
    }
    private void shareDeepLink() {
        createShortLink();
    }
    private void open_favourites() {
        Intent intent;
        intent = new Intent(this, Favourites.class);
        startActivity(intent);
    }
    public Uri buildDeepLink(Uri dl, int version) {
        String uriPrefix = "furqon.page.link";

        if(currentUser!=null){
            Log.i(TAG, "CURRENT USER ID " + currentUser.getUid());
            //userId=currentUser.getUid()
            if(!mSharedPref.read(sharedpref.CREDS_ALREADY_SENT, false)){
                mSharedPref.write(sharedpref.USERID, currentUser.getUid());
                //checkAppSignature(this);
            }

        }
//        else{
//            signInAnonymously();
//        }
        // Set dynamic link parameters:
        //  * URI prefix (required)
        //  * Android Parameters (required)
        //  * Deep link
        // [START build_dynamic_link]


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
//                            try{
//
//                            }catch (ApiException apix){
//
//                            }
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
                                Log.i(TAG, "LINK ERROR" + task.getResult().toString());
                                Crashlytics.log(Log.ERROR, TAG, task.getResult().toString());
                            }
                        }
                    });
        }
        // [END create_short_link]
    }



    @Override
    protected void onStart() {
        super.onStart();
        //checkForDynamicLink();
        // Check if user is signed in (non-null) and update UI accordingly.
        //updateUI();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mSharedPref.contains(mSharedPref.XATCHUP)) {
            davomi_but.setVisibility(View.VISIBLE);
        }else{
            davomi_but.setVisibility(View.GONE);
        }
    }





//
//    private void sendSignatureToServer(final String sign) {
//        //TODO send the token to  database.
//        //Add to the user account token, app id, device id
//        Log.i(TAG, "ATTEMPTING SIGNATURE " + sign);
//        queue = Volley.newRequestQueue(this);
//        String url = "https://inventivesolutionste.ipage.com/apijson.php";
//        //String url = "http://localhost/apijson/apijson.php";
//
//        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
//                new Response.Listener<String>() {
//                    @Override
//                    public void onResponse(String response) {
//                        Log.i("APP SIGNATURE STORED?", response);
//                        if (response.contains("app signature recorded")) {
//                            Log.i("APP SIGNATURE STORED", response);
//                            sharedPref.write("appsignature", sign);
//                        }
//                    }
//                }, new Response.ErrorListener() {
//            @Override
//            public void onErrorResponse(VolleyError error) {
//                Log.i("Signature send fail ", error.toString());
//            }
//        }) {
//            protected Map<String, String> getParams() {
//                Map<String, String> MyData = new HashMap<>();
//                MyData.put("action", "set_shash"); //Add the data you'd like to send to the server.
//                MyData.put("appname", "furqon"); //Add the data you'd like to send to the server.
//                MyData.put("shash", sign); //Add the data you'd like to send to the server.
//                return MyData;
//            }
//
//        };
//        queue.add(stringRequest);
//    }

    public static boolean deleteDir(File dir) {
        if (dir != null && dir.isDirectory()) {
            String[] children = dir.list();
            for (String child : children) {
                boolean success = deleteDir(new File(dir, child));
                if (!success) {
                    return false;
                }
            }
            return dir.delete();
        } else if (dir != null && dir.isFile()) {
            return dir.delete();
        } else {
            return false;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //mInterstitialAd.show();
    }

    //TODO ClassCastException fixed???
    private void continueReading() {
        if (mSharedPref.contains(mSharedPref.XATCHUP)) {
            String xatchup = mSharedPref.read(mSharedPref.XATCHUP, "");
            if (xatchup.length() > 0) {
                Log.i("XATCHUP", xatchup);
                Intent intent;
                Context context = this;
                intent = new Intent(context, AyahList.class);
                intent.putExtra("SURANAME", xatchup);
                context.startActivity(intent);
            } else {
                Toast.makeText(getBaseContext(), getString(R.string.no_bookmarks), Toast.LENGTH_LONG).show();
            }
        } else {
            Toast.makeText(getBaseContext(), getString(R.string.no_bookmarks), Toast.LENGTH_LONG).show();
        }

    }

    public void openSuraNames(View view) {

        Intent intent = new Intent(this, SuraNameList.class);
        startActivity(intent);

        Bundle bundle = new Bundle();
        String id = "1";
        String name = "Surlar button";
        bundle.putString(FirebaseAnalytics.Param.ITEM_ID, id);
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, name);
        bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "activity");
        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
    }

    public void ayahOfTheDay() {
        if(!mSharedPref.read(sharedpref.RANDOM_AYAH_SEEN, false)){
            Intent intent = new Intent(this, AyahOfTheDay.class);
            startActivity(intent);

            mSharedPref.write(sharedpref.RANDOM_AYAH_SEEN, true);
        }


    }



    private void displayResult(final String result) {
        handler.post(new Runnable() {
            public void run() {
                //statusText.setText(result);
                setProgressBarIndeterminateVisibility(false);


            }
        });
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (intent != null) {

            String data = intent.getStringExtra("data");


        }
    }

    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    public Action getIndexApiAction() {
        return Actions.newView("Main", "https://furqon.page.link/deeplink");
    }

    @Override
    public void onStop() {

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        FirebaseUserActions.getInstance().end(getIndexApiAction());
        super.onStop();
    }
}
