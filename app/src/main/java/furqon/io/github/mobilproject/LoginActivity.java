package furqon.io.github.mobilproject;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.crashlytics.android.Crashlytics;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks;
import com.google.firebase.dynamiclinks.PendingDynamicLinkData;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.security.MessageDigest;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import io.fabric.sdk.android.Fabric;

public class LoginActivity extends AppCompatActivity{

    static final String TAG = "LOGIN ACTIVITY";
    TextView welcome_txt;
    TextView username_txt;
    Button start_btn;
    Button no_ads_btn;
    SignInButton google_btn;
    Button privacy_btn;
    String token;
    FirebaseAuth mAuth;
    FirebaseUser currentUser;
    String currentPoints;
    private RewardAd mRewardedVideoAd;

    private static final int VALID = 0;
    private static final int INVALID = 1;
    private String shash;
    private static final int REQUEST_INVITE = 0;

    private String currentSignature;
    private ArrayList<JSONObject> jsonArrayResponse;
    private RequestQueue queue;
    private FirebaseRemoteConfig mFirebaseRemoteConfig;
    private SharedPreferences mSharedPref;
    private GoogleSignInClient mGoogleSignInClient;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater inflater = getMenuInflater();

        inflater.inflate(R.menu.menu_scrolling, menu);


        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.signin_item:
                signIn();
                return true;
            case R.id.action_settings:
                open_settings();
                return true;
            case R.id.noads_item:
                if(mSharedPref.contains(SharedPreferences.NOMOREADS)){
                    if(mSharedPref.read(SharedPreferences.NOMOREADS, false))
                        mRewardedVideoAd.NOMORE();
                }
                else{
                    mRewardedVideoAd.NOMORE();
                }
                return true;
//            case R.id.messages_i:
//                open_messages();
//                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, 1);
    }

    private void open_settings() {
        Intent intent;
        intent = new Intent(this, Settings.class);
        startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        Toolbar toolbar = findViewById(R.id.tool_bar);
        setSupportActionBar(toolbar);

        mRewardedVideoAd = new RewardAd(this);

        Fabric.with(this, new Crashlytics());
        mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance();


        mFirebaseRemoteConfig.setDefaultsAsync(R.xml.remote_config_defaults);

        FirebaseRemoteConfigSettings configSettings = new FirebaseRemoteConfigSettings.Builder()
                .setMinimumFetchIntervalInSeconds(3600)
                .build();
        mFirebaseRemoteConfig.setConfigSettingsAsync(configSettings);
        // Initialize Firebase Auth
        //--------------------------------------------------------
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
// [START config_signin]
        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        // [END config_signin]

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        //========================================================



        mSharedPref = SharedPreferences.getInstance();
        mSharedPref.init(getApplicationContext());
        //CheckToken();
        //updateUI(currentUser);
        //UI
        //===============================================
        welcome_txt = findViewById(R.id.welcome_textView);
        welcome_txt.setText(R.string.welcome_title);
        username_txt = findViewById(R.id.username_textView);
        username_txt.setText("");

        start_btn = findViewById(R.id.start_app_button);
        start_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mSharedPref.getDefaults("random_ayah_sw")) {
                    ayahOfTheDay();
                }else{
                    Intent intent = new Intent(getBaseContext(), MainActivity.class);
                    startActivity(intent);
                }

            }
        });
        privacy_btn = findViewById(R.id.privacy_policy_btn);
        privacy_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                Intent intent = new Intent(getApplicationContext(),
                        PrivacyPolicyActivity.class);
                startActivity(intent);
            }
        });

        if (mSharedPref.isFirstRun()) {
            Intent intent = new Intent(this, ScrollingActivity.class);
            startActivity(intent);

        } else {

            CheckInviterThanked();
        }




    }

    private void CheckToken() {
        if (!mSharedPref.read(mSharedPref.TOKEN, "").isEmpty()) {
            token = mSharedPref.read(mSharedPref.TOKEN, "");
            Log.d(TAG, "TOKEN RESTORED:" + token);

        } else {

            token = null;

            Log.d(TAG, "TOKEN MISSING, RENEW");

        }
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == 1) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                Log.d(TAG, "firebaseAuthWithGoogle:" + account.getId());

                firebaseAuthWithGoogle(account.getIdToken());
            } catch (ApiException e) {
                // Google Sign In failed, update UI appropriately
                Log.w(TAG, "Google sign in failed", e);
                // ...
            }
        }
    }
    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithCredential:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            updateUI(user);
                            mSharedPref.write(SharedPreferences.GOOGLE, true);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            //Snackbar.make(mBinding.mainLayout, "Authentication Failed.", Snackbar.LENGTH_SHORT).show();
                            updateUI(null);
                        }

                        // ...
                    }
                });
    }
    public void CheckRC() {
        Log.d(TAG, mFirebaseRemoteConfig.getString("rewardad_multiplier") + " reward multiplier");
        mFirebaseRemoteConfig.fetchAndActivate()
                .addOnCompleteListener(this, new OnCompleteListener<Boolean>() {
                    @Override
                    public void onComplete(@NonNull Task<Boolean> task) {
                        if (task.isSuccessful()) {
                            boolean updated = task.getResult();
                            Log.d(TAG, "Config params updated: " + updated);
                            //Toast.makeText(MainActivity.this, "Fetch and activate succeeded", Toast.LENGTH_SHORT).show();
                            mSharedPref.write(mSharedPref.SHAREWARD, (int) mFirebaseRemoteConfig.getLong("share_reward"));
                            mSharedPref.write(mSharedPref.PERSONAL_REWARD, (int) mFirebaseRemoteConfig.getLong("personal_reward"));
                            //sharedpref.getInstance().write(sharedpref.INITIAL_COINS, (int) mFirebaseRemoteConfig.getLong("initial_coins"));
                            //Log.d("COINS", mSharedPref.read(mSharedPref.INITIAL_COINS_USED, false) + " " + mSharedPref.read(mSharedPref.INITIAL_COINS, 0));
                        } else {
                            mSharedPref.write(mSharedPref.SHAREWARD, (int) mFirebaseRemoteConfig.getLong("share_reward"));
                            mSharedPref.write(mSharedPref.PERSONAL_REWARD, (int) mFirebaseRemoteConfig.getLong("personal_reward"));
                            Toast.makeText(LoginActivity.this, "Fetch failed",
                                    Toast.LENGTH_SHORT).show();
                        }
                        //CheckRC();
                    }
                });
    }

    private void signInAnonymously() {
        mSharedPref.write(mSharedPref.RANDOM_AYAH_SEEN, false);
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
                            AuthCredential credential = GoogleAuthProvider.getCredential( getString(R.string.default_web_client_id), null);
                            mAuth.getCurrentUser().linkWithCredential(credential)
                                    .addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
                                        @Override
                                        public void onComplete(@NonNull Task<AuthResult> task) {
                                            if (task.isSuccessful()) {
                                                Log.d(TAG, "linkWithCredential:success");
                                                FirebaseUser user = task.getResult().getUser();
                                                updateUI(user);
                                            } else {
                                                Log.w(TAG, "linkWithCredential:failure", task.getException());
                                                Toast.makeText(LoginActivity.this, "Authentication failed.",
                                                        Toast.LENGTH_SHORT).show();
                                                updateUI(currentUser);
                                            }

                                            // ...
                                        }
                                    });
                            //updateUI(currentUser);


                        } else {
                            // If sign in fails, display a message to the user.
                            Log.i(TAG, "signInAnonymously:failure", task.getException());

//                            Crashlytics.log(Log.ERROR, TAG, task.getException().toString());
//                            Toast.makeText(LoginActivity.this, "Authentication failed.",
//                                    Toast.LENGTH_SHORT).show();
                            currentUser = null;
                        }

                    }
                });
        // [END signin_anonymously]
        Log.e(TAG, "Signin ATTEMPT: " + mAuth);
    }

    private void updateUI(FirebaseUser currentUser) {
        boolean isSignedIn = (currentUser != null);
        currentPoints = String.valueOf(mSharedPref.read(mSharedPref.COINS, 0));

        // Status text
        if (isSignedIn) {
            Log.i(TAG, "SIGNED IN. FIREBASE AUTH " + currentUser.getUid() + " " +  currentUser.getEmail());
            sendRegistrationToServer();
            checkAppSignature(this);
            try{
                if (!Objects.requireNonNull(currentUser.getEmail()).isEmpty()) {
                    google_btn.setVisibility(View.GONE);
                    username_txt.setText(currentUser.getEmail());
                }
            }catch (NullPointerException x){

            }
        } else {
            if(isNetworkAvailable())
                signInAnonymously();
        }

    }

    private void sendRegistrationToServer() {
        //TODO send the token to  database.
        //Add to the user account token, app id, device id
        //Log.i("ATTEMPTING TOKEN SEND", token);

        queue = Volley.newRequestQueue(this);
        String url = mFirebaseRemoteConfig.getString("server_php") + "/apijson.php";
        //String url = "http://127.0.0.1:1234/apijson/localhost_test.php";
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        //progressBar.setVisibility(View.INVISIBLE);
                        //textView.setText(response);
                        //handle response. ["{\"last_visit\":null,\"fcm_token\":null,\"id\":\"1\"}"]
                        Log.d(TAG, "JSON raw " + response);
                        jsonArrayResponse = new ArrayList<>();
                        try {
                            JSONArray jsonArray = new JSONArray(response);
                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject object = new JSONObject(jsonArray.getString(i));
                                jsonArrayResponse.add(object);
                            }
                            int sdate = jsonArrayResponse.get(0).getInt("last_visit");
                            Log.d(TAG, "JSON " + sdate);
                            if (sdate == 0) {
                                //too early or too late
                                //String mes = new StringBuilder().append(getString(R.string.u_received)).append(String.valueOf(sdate)).append(getString(R.string._coins)).toString();
                                //Toast.makeText(getApplicationContext(), mes, Toast.LENGTH_LONG).show();
                            } else {
                                mSharedPref.AddCoins(getApplicationContext(), (int) mFirebaseRemoteConfig.getLong("first_visit_reward"));
                                //String mes = new StringBuilder().append(getString(R.string.u_received)).append(String.valueOf(sdate)).append(getString(R.string._coins)).toString();
                                //Toast.makeText(getApplicationContext(), "+" + sdate, Toast.LENGTH_LONG).show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            //Log.i("error json", "tttttttttttttttt");
                            Crashlytics.log(Log.ERROR, TAG, e.getStackTrace().toString());
                        } catch (IndexOutOfBoundsException iobx) {
                            Log.d(TAG, "JSON " + iobx);
                        }
                        CheckRC();
                        checkForDynamicLink();

                        //successfully saved creds. Mark the app launch to get rid of redundant requests
                        mSharedPref.write(mSharedPref.CREDS_ALREADY_SENT, true);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.i(TAG, "UserID and TOKEN failed to send" + error.toString());

            }
        }) {
            protected Map<String, String> getParams() {
                Map<String, String> MyData = new HashMap<>();
                MyData.put("action", "store_token"); //Add the data you'd like to send to the server.
                MyData.put("appname", "furqon"); //Add the data you'd like to send to the server.
                MyData.put("token", token); //Add the data you'd like to send to the server.
                MyData.put("user_id", currentUser.getUid());
                MyData.put("points", currentPoints);
                MyData.put("email", Objects.requireNonNull(currentUser.getEmail()));
                MyData.put("app_id", BuildConfig.VERSION_NAME);
                return MyData;
            }

        };
        if (token != null && currentUser != null) {
            queue.add(stringRequest);
            //Toast.makeText(this, "Initiation", Toast.LENGTH_SHORT).show();
            Log.d(TAG, "sending creds " + currentPoints);

        } else {
            Log.d(TAG, "missing important creds");
            Toast.makeText(this, "You are missing important credentials. Try to restart the app!", Toast.LENGTH_LONG).show();
            try{
                if(mSharedPref.contains(mSharedPref.TOKEN_ATTEMPTED)){
                    int attempt_count = mSharedPref.read(mSharedPref.TOKEN_ATTEMPT_COUNT, 0);
                    if(attempt_count<3){
                        attempt_count++;
                        mSharedPref.write(mSharedPref.TOKEN_ATTEMPT_COUNT, attempt_count);
                        Intent i = getBaseContext().getPackageManager().
                                getLaunchIntentForPackage(getBaseContext().getPackageName());
                        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(i);
                        finish();
                    }
                }else{
                    mSharedPref.write(mSharedPref.TOKEN_ATTEMPT_COUNT, 0);

                }
                if (!Objects.requireNonNull(currentUser.getEmail()).isEmpty()) {
                    google_btn.setVisibility(View.GONE);
                    username_txt.setText(currentUser.getEmail());
                }
            } catch (Exception e) {
                
            }

        }


    }
    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
    private void checkForDynamicLink() {
        Log.i(TAG, "Dynamic LINK CHECKING");
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
                            if (!inviter_id.equals(currentUser.getUid())) {
                                Log.i(TAG, "Dynamic LINK FOUND " + inviter_id);
                                pendingDynamicLinkData = null;

                                //Send confirmation
                                if (mSharedPref.read(SharedPreferences.INVITER, 0) == 0 && mSharedPref.read(SharedPreferences.INVITER_ID, "").isEmpty()) {
                                    //Initial thanking.
                                    //both inviter and its ID are empty
                                    //simply store if not logged in
                                    if(currentUser.getEmail().isEmpty()){
                                        mSharedPref.write(SharedPreferences.INVITER_ID, inviter_id);
                                    }
                                    else{
                                        mSharedPref.write(SharedPreferences.INVITER_ID, inviter_id);
                                        sendConfirmationToServer(inviter_id);
                                    }
                                } else if(mSharedPref.read(SharedPreferences.INVITER, 0) == 0 && !mSharedPref.read(SharedPreferences.INVITER_ID, "").isEmpty()){
                                    //retry thanking, if the same person is inviting.
                                    //user can not thank more than one inviter
                                    if(mSharedPref.read(SharedPreferences.INVITER_ID, "")==inviter_id)
                                        sendConfirmationToServer(inviter_id);
                                }
                            } else {
                                Log.i(TAG, "Can not use the dlink");

                            }

                        } else {

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
        //CheckRC();
    }

    private void CheckInviterThanked() {
        if (mSharedPref.read(mSharedPref.INVITER, 0) == 0) {
            String inviter_id = mSharedPref.read(mSharedPref.INVITER_ID, "");
            if (!inviter_id.isEmpty()) {
                if(!currentUser.getEmail().isEmpty()){
                    sendConfirmationToServer(inviter_id);
                }
                else{
                    Toast.makeText(getApplicationContext(), R.string.signin_request, Toast.LENGTH_LONG).show();
                }
            } else {
                Crashlytics.log(Log.ERROR, TAG, "INVITER ID WAS EMPTY");
            }
        } else {
            //user is already invited
        }
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


                checkSignatureOnServer(currentSignature);
                //Log.d("REMOVE_ME", "Include this string as a value for SIGNATURE:" + currentSignature + model + manufacturer + bootloader);

                //compare signatures

            }

        } catch (Exception e) {
            Crashlytics.log(Log.ERROR, TAG, e.getStackTrace().toString());

//assumes an issue in checking signature., but we let the caller decide on what to do.

        }
        //sendRegistrationToServer();
        return INVALID;

    }

    private void checkSignatureOnServer(final String currentSignature) {

        queue = Volley.newRequestQueue(this);
        String url = mFirebaseRemoteConfig.getString("server_php") + "/apijson.php";

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        //Log.i("APP SIGNATURE STORED?", response);
                        if (response.contains("OK")) {
                            Log.i(TAG, "signature " + response);
                            mSharedPref.write(SharedPreferences.SIGNATURE, response);
                        } else {
                            Log.i(TAG, "signature failed " + response);
                            //failed, save for future
                            Toast.makeText(getApplicationContext(), "Illegal access", Toast.LENGTH_SHORT).show();
                            mSharedPref.write(SharedPreferences.SIGNATURE, response);
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
                MyData.put("action", "check_signature"); //Add the data you'd like to send to the server.
                MyData.put("app_signature", currentSignature); //Add the data you'd like to send to the server.
                return MyData;
            }

        };
        queue.add(stringRequest);
    }

    private void sendConfirmationToServer(final String inviter_id) {
        //TODO send the token to  database.
        //Add to the user account token, app id, device id
        Log.i(TAG, "ATTEMPTING confirmation " + inviter_id);
        queue = Volley.newRequestQueue(this);
        String url = mFirebaseRemoteConfig.getString("server_php") + "/apijson.php";

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        //Log.i("APP SIGNATURE STORED?", response);
                        if (response.contains("confirmation")) {
                            Log.i(TAG, "Invitation " + response);
                            mSharedPref.AddCoins(getApplicationContext(), 200);
                            mSharedPref.write(mSharedPref.INVITER, 1);
                            Snackbar.make(findViewById(android.R.id.content),
                                    R.string.invitation_confirm, Snackbar.LENGTH_LONG).show();
                        } else {
                            Log.i(TAG, "Invitation failed " + response);
                            //failed, save for future

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
                MyData.put("points", currentPoints);
                MyData.put("email", Objects.requireNonNull(currentUser.getEmail())); //Add the data you'd like to send to the server.
                MyData.put("inviter_id", inviter_id);
                return MyData;
            }

        };
        queue.add(stringRequest);
    }

    public void ayahOfTheDay() {

        Intent intent = new Intent(this, AyahOfTheDay.class);
        startActivity(intent);
        mSharedPref.write(mSharedPref.RANDOM_AYAH_SEEN, true);

    }

    @Override
    protected void onResume() {
        super.onResume();


    }

    @Override
    protected void onStart() {
        super.onStart();
        CheckToken();
        updateUI(currentUser);
        Date c = Calendar.getInstance().getTime();
        System.out.println("Current time => " + c);


        Log.i(TAG, " " + c.getTime());
        if(mSharedPref.contains(SharedPreferences.PREVIOUS_SET)){
            String yesterday = mSharedPref.read(SharedPreferences.PREVIOUS_SET, "0");
            long prev_day = Long.parseLong(yesterday);
            long today = c.getTime();
            if(Math.abs(prev_day-today)>(1000*60*60*24)){
                Log.i(TAG, yesterday + " ONE DAY PASSED " + c.getTime());
                mSharedPref.write(SharedPreferences.NOMOREADS, false);
            }else{
                Log.i(TAG, yesterday + " LESS THAN A DAY " + c.getTime());
            }


        }
    }

}
