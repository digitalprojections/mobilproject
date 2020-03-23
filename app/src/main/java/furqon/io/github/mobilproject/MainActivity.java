package furqon.io.github.mobilproject;


import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Base64;
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
import androidx.lifecycle.LiveData;
import androidx.viewpager.widget.ViewPager;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.crashlytics.android.Crashlytics;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.measurement.module.Analytics;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.appindexing.Action;
import com.google.firebase.appindexing.FirebaseAppIndex;
import com.google.firebase.appindexing.FirebaseUserActions;
import com.google.firebase.appindexing.Indexable;
import com.google.firebase.appindexing.builders.Actions;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.dynamiclinks.DynamicLink;
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks;
import com.google.firebase.dynamiclinks.PendingDynamicLinkData;
import com.google.firebase.dynamiclinks.ShortDynamicLink;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.security.MessageDigest;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import io.fabric.sdk.android.Fabric;

public class MainActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    FirebaseUser currentUser;
    Button suralar_but;
    Button extra_btn;
    Button davomi_but;
    ImageView imageView;
    private Handler handler;
    // Try to use more data here. ANDROID_ID is a single point of attack.
    InterstitialAd mInterstitialAd;
    private LiveData<List<NewMessages>> newMessages;
    private FirebaseAnalytics mFirebaseAnalytics;
    private RequestQueue queue;
    private String shash;
    private static final int VALID = 0;
    private static final int INVALID = 1;
    private static final String TAG = "MAIN ACTIVITY";
    private static final int REQUEST_INVITE = 0;
    private static final String DEEP_LINK_URL = "https://furqon.page.link/ThB2";
    Uri deepLink;
    private sharedpref sharedPref;
    private boolean randomayahshown;
    private String token;
    private String currentSignature;
    private ArrayList<JSONObject> jsonArrayResponse;

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

        sharedPref = sharedpref.getInstance();
        sharedPref.init(getApplicationContext());

        // Initialize Firebase Auth
        //--------------------------------------------------------
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        //========================================================

        Fabric.with(this, new Crashlytics());
        Crashlytics.log("Activity created");
        if (!sharedPref.read(sharedPref.TOKEN, "").isEmpty()) {
            token = sharedPref.read(sharedPref.TOKEN, "");
            Log.d(TAG, "TOKEN RESTORED:" + token);

        } else {
            token = sharedPref.read(sharedPref.TOKEN, "");
            Log.d(TAG, "TOKEN MISSING, RENEW");

        }
        updateUI();
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

        MobileAds.initialize(this, getString(R.string.addmob_app_id));
        mInterstitialAd = new InterstitialAd(this);
        mInterstitialAd.setAdUnitId(getString(R.string.interstitial_fullpage));
        mInterstitialAd.loadAd(new AdRequest.Builder().build());

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

        if (sharedPref.contains(sharedPref.XATCHUP)) {
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
        deepLink = buildDeepLink(Uri.parse(DEEP_LINK_URL), 0);

        if (sharedPref.isFirstRun()) {
            Intent intent = new Intent(this, ScrollingActivity.class);
            startActivity(intent);

        } else {
            if (sharedPref.getDefaults("random_ayah_sw") && !randomayahshown) {
                ayahOfTheDay();
                randomayahshown = true;
            }
        }

//        Intent intent = new Intent(getApplicationContext(), AudioPlayerService.class);
//        intent.setAction(AudioPlayerService.ACTION_PLAY);
//        startService(intent);
        // ATTENTION: This was auto-generated to handle app links.
        Intent appLinkIntent = getIntent();
        String appLinkAction = appLinkIntent.getAction();
        Uri appLinkData = appLinkIntent.getData();


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
        }
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

    private void checkForDynamicLink() {
        Log.i(TAG, "LINK CHECKING");
        FirebaseDynamicLinks.getInstance()
                .getDynamicLink(getIntent())
                .addOnSuccessListener(this, new OnSuccessListener<PendingDynamicLinkData>() {
                    @Override
                    public void onSuccess(PendingDynamicLinkData pendingDynamicLinkData) {
                        // Get deep link from result (may be null if no link is found)
                        Uri deepLink = null;
                        if (pendingDynamicLinkData != null) {
                            deepLink = pendingDynamicLinkData.getLink();
                            String inviter_id = deepLink.getQueryParameter("user_id");
                            Log.i(TAG, "LINK FOUND " + inviter_id);
                            pendingDynamicLinkData = null;
                            Snackbar.make(findViewById(android.R.id.content),
                                    R.string.invitation_confirm, Snackbar.LENGTH_LONG).show();
                            //Send confirmation
                            if(sharedpref.getInstance().read(sharedpref.getInstance().INVITER, 0)==0)
                            {
                                sendConfirmationToServer(inviter_id);
                            }else{
                                //user is already invited
                            }
                        }
                        // Handle the deep link. For example, open the linked
                        // content, or apply promotional credit to the user's
                        // account.
                        // ...
                        // ...
                    }
                })
                .addOnFailureListener(this, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "getDynamicLink:onFailure", e);
                    }
                });
        //sendConfirmationToServer("b4sGS2mH92RIv8bTJnomGzH9IDp1");
    }

    private void sendConfirmationToServer(final String inviter_id) {
        //TODO send the token to  database.
        //Add to the user account token, app id, device id
        Log.i(TAG, "ATTEMPTING confirmation " + inviter_id);
        queue = Volley.newRequestQueue(this);
        String url = "https://inventivesolutionste.ipage.com/apijson.php";

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        //Log.i("APP SIGNATURE STORED?", response);
                        if (response.contains("confirmation")) {
                            Log.i(  TAG,"Invitation " + response);
                            sharedPref.AddCoins(getApplicationContext(), 50);

                            sharedPref.getInstance().write(sharedPref.getInstance().INVITER, 1);

                        }else{
                            Log.i(  TAG,"Invitation failed " + response);
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.i(TAG, "Signature send fail " + error.toString());
                Crashlytics.log(Log.ERROR, TAG, error.toString());
            }
        }) {
            protected Map<String, String> getParams() {
                Map<String, String> MyData = new HashMap<>();
                MyData.put("action", "confirm"); //Add the data you'd like to send to the server.
                MyData.put("appname", "furqon"); //Add the data you'd like to send to the server.
                MyData.put("user_id", currentUser.getUid()); //Add the data you'd like to send to the server.
                MyData.put("inviter_id", inviter_id);
                return MyData;
            }

        };
        queue.add(stringRequest);
    }



    @Override
    protected void onStart() {
        super.onStart();
        //checkForDynamicLink();
        // Check if user is signed in (non-null) and update UI accordingly.
        updateUI();

    }

    private void updateUI() {
        boolean isSignedIn = (currentUser != null);

        // Status text
        if (isSignedIn) {
            Log.i(TAG, "FIREBASE AUTH " + currentUser.getUid());
            sharedpref.getInstance().write(sharedPref.USERID, currentUser.getUid());

            checkAppSignature(this);
            //sendRegistrationToServer();
        } else {
            signInAnonymously();
        }

    }
    private void signInAnonymously() {
        //showProgressBar();
        // [START signin_anonymously]
        mAuth.signInAnonymously()
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInAnonymously:success");
                            currentUser = mAuth.getCurrentUser();
                            updateUI();
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInAnonymously:failure", task.getException());

                            Crashlytics.log(Log.ERROR, TAG, task.getException().toString());
                            Toast.makeText(MainActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                            currentUser = null;
                        }

                    }
                });
        // [END signin_anonymously]
        Log.e(TAG, "Signin ATTEMPT: " + mAuth);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (sharedPref.contains(sharedPref.XATCHUP)) {
            davomi_but.setVisibility(View.VISIBLE);
        }else{
            davomi_but.setVisibility(View.GONE);
        }
    }

    private void sendRegistrationToServer() {
        //TODO send the token to  database.
        //Add to the user account token, app id, device id
        //Log.i("ATTEMPTING TOKEN SEND", token);

        queue = Volley.newRequestQueue(this);
        String url = "https://inventivesolutionste.ipage.com/apijson.php";
        //String url = "http://127.0.0.1:1234/apijson/localhost_test.php";
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        //progressBar.setVisibility(View.INVISIBLE);
                        //textView.setText(response);
                        //handle response. ["{\"last_visit\":null,\"fcm_token\":null,\"id\":\"1\"}"]
                        //Log.d(TAG, "JSON raw " + response);
                        jsonArrayResponse = new ArrayList<>();
                        try {
                            JSONArray jsonArray = new JSONArray(response);
                            for (int i=0; i<jsonArray.length();i++)
                            {
                                JSONObject object = new JSONObject(jsonArray.getString(i));
                                jsonArrayResponse.add(object);
                            }


                                int sdate = jsonArrayResponse.get(0).getInt("last_visit");
                                Log.d(TAG, "JSON " + sdate);
                                if (sdate==0) {
                                    //too early or too late
                                    //String mes = new StringBuilder().append(getString(R.string.u_received)).append(String.valueOf(sdate)).append(getString(R.string._coins)).toString();
                                    //Toast.makeText(getApplicationContext(), mes, Toast.LENGTH_LONG).show();
                                }
                                else {
                                    sharedPref.AddCoins(getApplicationContext(), sdate);
                                    //String mes = new StringBuilder().append(getString(R.string.u_received)).append(String.valueOf(sdate)).append(getString(R.string._coins)).toString();
                                    Toast.makeText(getApplicationContext(), "+"+sdate, Toast.LENGTH_LONG).show();
                                }






                        } catch (JSONException e) {
                            e.printStackTrace();
                            //Log.i("error json", "tttttttttttttttt");
                            Crashlytics.log(Log.ERROR, TAG, e.getStackTrace().toString());
                        }
                        catch (IndexOutOfBoundsException iobx){
                            Log.d(TAG, "JSON " + iobx);
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.i(TAG, "TOKEN That did not work" + error.toString());
            }
        }) {
            protected Map<String, String> getParams() {
                Map<String, String> MyData = new HashMap<>();
                MyData.put("action", "store_token"); //Add the data you'd like to send to the server.
                MyData.put("appname", "furqon"); //Add the data you'd like to send to the server.
                MyData.put("token", token); //Add the data you'd like to send to the server.
                MyData.put("user_id", currentUser.getUid());
                MyData.put("app_id", currentSignature);
                return MyData;
            }

        };
        if(token!=null && currentUser!=null){
            queue.add(stringRequest);
            //Toast.makeText(this, "Initiation", Toast.LENGTH_SHORT).show();
        }else{
            Toast.makeText(this, "You are missing important credentials. Try to restart the app!", Toast.LENGTH_LONG).show();
        }


        checkForDynamicLink();
    }



    private int checkAppSignature(Context context) {
        try {

            PackageInfo packageInfo;
            packageInfo = context.getPackageManager()

                    .getPackageInfo(context.getPackageName(),

                            PackageManager.GET_SIGNATURES);


            for (Signature signature : packageInfo.signatures) {

                byte[] signatureBytes = signature.toByteArray();

                MessageDigest md = MessageDigest.getInstance("SHA");

                md.update(signature.toByteArray());


                currentSignature = Base64.encodeToString(md.digest(), Base64.DEFAULT);
                String model = Build.MODEL;
                String manufacturer = Build.MANUFACTURER;
                String bootloader = Build.BOOTLOADER;


                //checkSignatureOnServer(currentSignature);
                //Log.d("REMOVE_ME", "Include this string as a value for SIGNATURE:" + currentSignature + model + manufacturer + bootloader);

                //compare signatures

            }

        } catch (Exception e) {
            Crashlytics.log(Log.ERROR, TAG, e.getStackTrace().toString());

//assumes an issue in checking signature., but we let the caller decide on what to do.

        }
        sendRegistrationToServer();
        return INVALID;

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
        if (sharedPref.contains(sharedPref.XATCHUP)) {
            String xatchup = sharedPref.read(sharedPref.XATCHUP, "");
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
        Intent intent = new Intent(this, AyahOfTheDay.class);
        startActivity(intent);

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
