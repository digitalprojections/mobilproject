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
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.dynamiclinks.DynamicLink;
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks;
import com.google.firebase.dynamiclinks.PendingDynamicLinkData;

import java.io.File;
import java.security.MessageDigest;
import java.util.HashMap;
import java.util.Map;

import hotchemi.android.rate.AppRate;
import hotchemi.android.rate.OnClickButtonListener;
import io.fabric.sdk.android.Fabric;

public class MainActivity extends AppCompatActivity {



    Button suralar_but;
    Button davomi_but;
    Button favourite_but;
    Button search_but;
    Button rate_but;

    ImageView imageView;

    private Handler handler;

    // Try to use more data here. ANDROID_ID is a single point of attack.
    InterstitialAd mInterstitialAd;


    private FirebaseAnalytics mFirebaseAnalytics;
    private RequestQueue queue;
    private String shash;
    private static final int VALID = 0;
    private static final int INVALID = 1;
    private static final String TAG = "MAIN ACTIVITY";
    private static final int REQUEST_INVITE = 0;
    private static final String DEEP_LINK_URL = "https://furqon.page.link/deeplink";
    Uri deepLink;
    private sharedpref sharedPref;
    private ViewPager viewPager;
    private boolean randomayahshown;



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
                shareDeepLink(deepLink.toString());
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


        Fabric.with(this, new Crashlytics());
        Crashlytics.log("Activity created");
        if (!sharedPref.read(sharedPref.TOKEN, "").isEmpty()) {
            String token = sharedPref.read(sharedPref.TOKEN, "");
            Log.d("TOKEN", "TOKEN RESTORED:" + token);
            sendRegistrationToServer(token);
        } else {
            String token = sharedPref.read(sharedPref.TOKEN, "");
            Log.d("TOKEN", "TOKEN MISSING? " + token);

        }

        checkAppSignature(this);
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

        MobileAds.initialize(this, getString(R.string.addmob_app_id));
        mInterstitialAd = new InterstitialAd(this);
        mInterstitialAd.setAdUnitId(getString(R.string.interstitial_fullpage));
        mInterstitialAd.loadAd(new AdRequest.Builder().build());



        handler = new Handler();

        suralar_but = findViewById(R.id.suralar);
        davomi_but = findViewById(R.id.davomi);
        favourite_but = findViewById(R.id.favouritebut);
        search_but = findViewById(R.id.searchbtn);
        rate_but = findViewById(R.id.ratebtn);

        imageView = findViewById(R.id.imageView);

        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ayahOfTheDay();
            }
        });
        favourite_but.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                open_favourites();
            }
        });
        search_but.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                open_search();
            }
        });
        rate_but.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Rateus();
            }
        });
        //day_but = findViewById(R.id.ayahoftheday);
        suralar_but.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openSuraNames(view);
            }
        });
        davomi_but.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                continueReading();
            }
        });


        //String deviceId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);


        try {
            File dir = this.getCacheDir();
            deleteDir(dir);
        } catch (Exception e) {
            e.printStackTrace();
            Crashlytics.log(Log.ERROR, TAG, "NPE caught");
            Crashlytics.logException(e);
        }

        //TODO login?
        //TODO point system
        //TODO share for points
        //TODO usage points
        //TODO unlock downloads

        // Create a deep link and display it in the UI
        deepLink = buildDeepLink(Uri.parse(DEEP_LINK_URL), 0);


        FirebaseDynamicLinks.getInstance()
                .getDynamicLink(getIntent())
                .addOnSuccessListener(this, new OnSuccessListener<PendingDynamicLinkData>() {
                    @Override
                    public void onSuccess(PendingDynamicLinkData pendingDynamicLinkData) {
                        // Get deep link from result (may be null if no link is found)
                        Uri deepLink = null;
                        if (pendingDynamicLinkData != null) {
                            deepLink = pendingDynamicLinkData.getLink();
                            Log.i(TAG, "LINK FOUND " + deepLink);
                            Snackbar.make(findViewById(android.R.id.content),
                                    "Found deep link!", Snackbar.LENGTH_LONG).show();
                        }


                        // Handle the deep link. For example, open the linked
                        // content, or apply promotional credit to the user's
                        // account.
                        // ...

                        // ...
                        Log.d(TAG, "getDynamicLink:SUCCESS " + deepLink);
                    }
                })
                .addOnFailureListener(this, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "getDynamicLink:onFailure", e);
                    }
                });

        AppRate.with(this)
                .setInstallDays(0) // default 10, 0 means install day.
                .setLaunchTimes(3) // default 10
                .setRemindInterval(2) // default 1
                .setShowLaterButton(true) // default true
                .setDebug(false) // default false
                .setOnClickButtonListener(new OnClickButtonListener() { // callback listener.
                    @Override
                    public void onClickButton(int which) {
                        Log.d(MainActivity.class.getName(), Integer.toString(which));
                    }
                })
                .monitor();


        // Show a dialog if meets conditions
        AppRate.showRateDialogIfMeetsConditions(this);
        if (sharedPref.isFirstRun()) {
            Intent intent = new Intent(this, ScrollingActivity.class);
            startActivity(intent);

        }else{

            if (sharedPref.getDefaults("random_ayah_sw") && !randomayahshown) {
                ayahOfTheDay();
                randomayahshown = true;
            }


        }


    }

    private void Rateus() {
        AppRate.with(this).showRateDialog(this);
    }

    private void open_settings() {
        Intent intent;
        intent = new Intent(this, furqon.io.github.mobilproject.Settings.class);
        startActivity(intent);
    }

    public Uri buildDeepLink(@NonNull Uri deepLink, int minVersion) {
        String uriPrefix = "furqon.page.link";

        // Set dynamic link parameters:
        //  * URI prefix (required)
        //  * Android Parameters (required)
        //  * Deep link
        // [START build_dynamic_link]
        DynamicLink.Builder builder = FirebaseDynamicLinks.getInstance()
                .createDynamicLink()
                .setDomainUriPrefix(uriPrefix)
                .setAndroidParameters(new DynamicLink.AndroidParameters.Builder()
                        .setMinimumVersion(minVersion)
                        .build())
                .setLink(deepLink);

        // Build the dynamic link
        DynamicLink link = builder.buildDynamicLink();
        // [END build_dynamic_link]

        // Return the dynamic link as a URI
        return link.getUri();
    }

    private void shareDeepLink(String deepLink) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_SUBJECT, "Firebase Deep Link");
        intent.putExtra(Intent.EXTRA_TEXT, "QURAN KAREEM Android " + deepLink);

        startActivity(intent);
    }

    @Override
    protected void onStart() {
        super.onStart();

    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    private void sendRegistrationToServer(final String token) {
        //TODO send the token to  database.
        //Add to the user account token, app id, device id
        Log.i("ATTEMPTING TOKEN SEND", token);

        queue = Volley.newRequestQueue(this);
        String url = "https://inventivesolutionste.ipage.com/ajphp_sbs.php";

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        //progressBar.setVisibility(View.INVISIBLE);
                        //textView.setText(response);
                        Log.i("TOKEN STORED", response);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.i("That did not work! ", error.toString());
            }
        }) {
            protected Map<String, String> getParams() {
                Map<String, String> MyData = new HashMap<>();
                MyData.put("action", "set_token"); //Add the data you'd like to send to the server.
                MyData.put("appname", ""); //Add the data you'd like to send to the server.
                MyData.put("username", ""); //Change to variable
                MyData.put("token", token); //Add the data you'd like to send to the server.
                return MyData;
            }

        };
        queue.add(stringRequest);
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

                final String currentSignature = Base64.encodeToString(md.digest(), Base64.DEFAULT);
                String model = Build.MODEL;
                String manufacturer = Build.MANUFACTURER;
                String bootloader = Build.BOOTLOADER;

                sendSignatureToServer(currentSignature);
                //checkSignatureOnServer(currentSignature);
                //Log.d("REMOVE_ME", "Include this string as a value for SIGNATURE:" + currentSignature + model + manufacturer + bootloader);

                //compare signatures
            }

        } catch (Exception e) {

//assumes an issue in checking signature., but we let the caller decide on what to do.

        }

        return INVALID;

    }

    private void sendSignatureToServer(final String sign) {
        //TODO send the token to  database.
        //Add to the user account token, app id, device id
        Log.i("ATTEMPTING SIGNATURE", sign);
        queue = Volley.newRequestQueue(this);
        String url = "https://inventivesolutionste.ipage.com/ajphp_sbs.php";

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.i("APP SIGNATURE STORED", response);
                        if (response.contains("app signature recorded")) {

                            sharedPref.write("appsignature", sign);
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.i("That did not work! ", error.toString());
            }
        }) {
            protected Map<String, String> getParams() {
                Map<String, String> MyData = new HashMap<>();
                MyData.put("action", "set_shash"); //Add the data you'd like to send to the server.
                MyData.put("appname", ""); //Add the data you'd like to send to the server.
                MyData.put("username", ""); //Change to variable
                MyData.put("password", ""); //Change to variable
                MyData.put("shash", sign); //Add the data you'd like to send to the server.
                return MyData;
            }

        };
        queue.add(stringRequest);
    }

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

    private void open_favourites() {
        Intent intent;
        intent = new Intent(this, furqon.io.github.mobilproject.Favourites.class);
        startActivity(intent);
    }

    private void open_search() {
        Intent intent;
        intent = new Intent(this, furqon.io.github.mobilproject.Search.class);
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
}
