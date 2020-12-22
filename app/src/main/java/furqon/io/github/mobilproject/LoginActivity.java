package furqon.io.github.mobilproject;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
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
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;


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
                        mRewardedVideoAd.NOMORE(this);
                }
                else{
                    mRewardedVideoAd.NOMORE(this);
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
            if (BuildConfig.BUILD_TYPE.equals("debug"))
                Log.d(TAG, "TOKEN RESTORED:" + token);

        } else {

            token = null;

            if (BuildConfig.BUILD_TYPE.equals("debug"))
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
                if (BuildConfig.BUILD_TYPE.equals("debug"))
                    Log.d(TAG, "firebaseAuthWithGoogle:" + account.getId());

                firebaseAuthWithGoogle(account.getIdToken());
            } catch (ApiException e) {
                // Google Sign In failed, update UI appropriately
                if (BuildConfig.BUILD_TYPE.equals("debug"))
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
                            if (BuildConfig.BUILD_TYPE.equals("debug"))
                                Log.d(TAG, "signInWithCredential:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            updateUI(user);
                            mSharedPref.write(SharedPreferences.GOOGLE, true);
                        } else {
                            // If sign in fails, display a message to the user.
                            if (BuildConfig.BUILD_TYPE.equals("debug"))
                                Log.w(TAG, "signInWithCredential:failure", task.getException());
                            //Snackbar.make(mBinding.mainLayout, "Authentication Failed.", Snackbar.LENGTH_SHORT).show();
                            updateUI(null);
                        }

                        // ...
                    }
                });
    }
    public void CheckRC() {
        if (BuildConfig.BUILD_TYPE.equals("debug"))
            Log.d(TAG, mFirebaseRemoteConfig.getString("rewardad_multiplier") + " reward multiplier");
        mFirebaseRemoteConfig.fetchAndActivate()
                .addOnCompleteListener(this, new OnCompleteListener<Boolean>() {
                    @Override
                    public void onComplete(@NonNull Task<Boolean> task) {
                        if (task.isSuccessful()) {
                            boolean updated = task.getResult();
                            if (BuildConfig.BUILD_TYPE.equals("debug"))
                                Log.d(TAG, "Config params updated: " + updated);
                            //Toast.makeText(MainActivity.this, "Fetch and activate succeeded", Toast.LENGTH_SHORT).show();
                            mSharedPref.write(SharedPreferences.SHAREWARD, (int) mFirebaseRemoteConfig.getLong("share_reward"));
                            mSharedPref.write(SharedPreferences.PERSONAL_REWARD, (int) mFirebaseRemoteConfig.getLong("personal_reward"));
                            //sharedpref.getInstance().write(sharedpref.INITIAL_COINS, (int) mFirebaseRemoteConfig.getLong("initial_coins"));

                        } else {
                            mSharedPref.write(SharedPreferences.SHAREWARD, (int) mFirebaseRemoteConfig.getLong("share_reward"));
                            mSharedPref.write(SharedPreferences.PERSONAL_REWARD, (int) mFirebaseRemoteConfig.getLong("personal_reward"));
                            Toast.makeText(LoginActivity.this, "Fetch failed",
                                    Toast.LENGTH_SHORT).show();
                        }
                        //CheckRC();
                    }
                });
    }

    private void signInAnonymously() {
        mSharedPref.write(SharedPreferences.RANDOM_AYAH_SEEN, false);
        //showProgressBar();
        // [START signin_anonymously]
        mAuth.signInAnonymously()
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            if (BuildConfig.BUILD_TYPE.equals("debug"))
                                Log.d(TAG, "signInAnonymously:success");

                            currentUser = mAuth.getCurrentUser();
                            AuthCredential credential = GoogleAuthProvider.getCredential( getString(R.string.default_web_client_id), null);
                            Objects.requireNonNull(mAuth.getCurrentUser()).linkWithCredential(credential)
                                    .addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
                                        @Override
                                        public void onComplete(@NonNull Task<AuthResult> task) {
                                            if (task.isSuccessful()) {
                                                if (BuildConfig.BUILD_TYPE.equals("debug"))
                                                    Log.d(TAG, "linkWithCredential:success");
                                                FirebaseUser user = task.getResult().getUser();
                                                updateUI(user);
                                            } else {
                                                if (BuildConfig.BUILD_TYPE.equals("debug"))
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
                            currentUser = null;
                        }

                    }
                });
    }

    private void updateUI(FirebaseUser currentUser) {
        boolean isSignedIn = (currentUser != null);
        currentPoints = String.valueOf(mSharedPref.read(mSharedPref.COINS, 0));

        // Status text
        if (isSignedIn) {
            if (BuildConfig.BUILD_TYPE.equals("debug"))
                Log.i(TAG, "SIGNED IN. FIREBASE AUTH " + currentUser.getUid() + " " +  currentUser.getEmail());
            sendRegistrationToServer();
            checkAppSignature(this);
            try{
                if (!Objects.requireNonNull(currentUser.getEmail()).isEmpty()) {
                    google_btn.setVisibility(View.GONE);
                    username_txt.setText(currentUser.getEmail());
                }
            }catch (NullPointerException ignored){

            }
        } else {
            if(isNetworkAvailable())
                signInAnonymously();
        }

    }

    private void sendRegistrationToServer() {
        queue = Volley.newRequestQueue(this);
        String url = mFirebaseRemoteConfig.getString("server_php") + "/apijson.php";
        //String url = "http://127.0.0.1:1234/apijson/localhost_test.php";
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        if (BuildConfig.BUILD_TYPE.equals("debug"))
                            Log.d(TAG, "JSON raw " + response);
                        jsonArrayResponse = new ArrayList<>();
                        try {
                            JSONArray jsonArray = new JSONArray(response);
                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject object = new JSONObject(jsonArray.getString(i));
                                jsonArrayResponse.add(object);
                            }
                            int sdate = jsonArrayResponse.get(0).getInt("last_visit");
                            if (BuildConfig.BUILD_TYPE.equals("debug"))
                                Log.d(TAG, "JSON " + sdate);
                            if (sdate == 0) {
                                //too early or too late
                                //String mes = new StringBuilder().append(getString(R.string.u_received)).append(String.valueOf(sdate)).append(getString(R.string._coins)).toString();
                                //Toast.makeText(getApplicationContext(), mes, Toast.LENGTH_LONG).show();
                            } else {
                                SharedPreferences.AddCoins(getApplicationContext(), (int) mFirebaseRemoteConfig.getLong("first_visit_reward"));
                                //String mes = new StringBuilder().append(getString(R.string.u_received)).append(String.valueOf(sdate)).append(getString(R.string._coins)).toString();
                                //Toast.makeText(getApplicationContext(), "+" + sdate, Toast.LENGTH_LONG).show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();

                        } catch (IndexOutOfBoundsException iobx) {
                            if (BuildConfig.BUILD_TYPE.equals("debug"))
                                Log.d(TAG, "JSON " + iobx);
                        }
                        CheckRC();
                        //checkForDynamicLink();

                        //successfully saved creds. Mark the app launch to get rid of redundant requests
                        mSharedPref.write(SharedPreferences.CREDS_ALREADY_SENT, true);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (BuildConfig.BUILD_TYPE.equals("debug"))
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
            if (BuildConfig.BUILD_TYPE.equals("debug"))
                Log.d(TAG, "sending creds " + currentPoints);

        } else {
            if (BuildConfig.BUILD_TYPE.equals("debug"))
                Log.d(TAG, "missing important creds");
            Toast.makeText(this, "You are missing important credentials. Try to restart the app!", Toast.LENGTH_LONG).show();
            try{
                if(mSharedPref.contains(SharedPreferences.TOKEN_ATTEMPTED)){
                    int attempt_count = mSharedPref.read(SharedPreferences.TOKEN_ATTEMPT_COUNT, 0);
                    if(attempt_count<3){
                        attempt_count++;
                        mSharedPref.write(SharedPreferences.TOKEN_ATTEMPT_COUNT, attempt_count);
                        Intent i = getBaseContext().getPackageManager().
                                getLaunchIntentForPackage(getBaseContext().getPackageName());
                        assert i != null;
                        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(i);
                        finish();
                    }
                }else{
                    mSharedPref.write(SharedPreferences.TOKEN_ATTEMPT_COUNT, 0);

                }
                if (!Objects.requireNonNull(currentUser.getEmail()).isEmpty()) {
                    google_btn.setVisibility(View.GONE);
                    username_txt.setText(currentUser.getEmail());
                }
            } catch (Exception ignored) {
                
            }

        }


    }
    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }


    private void CheckInviterThanked() {
        if (mSharedPref.read(SharedPreferences.INVITER, 0) == 0) {
            String inviter_id = mSharedPref.read(SharedPreferences.INVITER_ID, "");
            if (!inviter_id.isEmpty()) {
                if(!Objects.requireNonNull(currentUser.getEmail()).isEmpty()){
                    sendConfirmationToServer(inviter_id);
                }
                else{
                    Toast.makeText(getApplicationContext(), R.string.signin_request, Toast.LENGTH_LONG).show();
                }
            }  //Crashlytics.log(Log.ERROR, TAG, "INVITER ID WAS EMPTY");

        }  //user is already invited

    }

    private void checkAppSignature(Context context) {
        try {

            PackageInfo packageInfo;
            packageInfo = context.getPackageManager()

                    .getPackageInfo(context.getPackageName(),

                            PackageManager.GET_SIGNATURES);


            for (Signature signature : packageInfo.signatures) {

                byte[] signatureBytes = signature.toByteArray();

                MessageDigest md = MessageDigest.getInstance("SHA");

                md.update(signature.toByteArray());


                String currentSignature = Base64.encodeToString(md.digest(), Base64.DEFAULT);
                String model = Build.MODEL;
                String manufacturer = Build.MANUFACTURER;
                String bootloader = Build.BOOTLOADER;


                checkSignatureOnServer(currentSignature);
            }

        } catch (Exception e) {
            //Crashlytics.log(Log.ERROR, TAG, e.getStackTrace().toString());

//assumes an issue in checking signature., but we let the caller decide on what to do.

        }
        //sendRegistrationToServer();

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
                            //Log.i(TAG, "signature " + response);
                            mSharedPref.write(SharedPreferences.SIGNATURE, response);
                        } else {
                            //Log.i(TAG, "signature failed " + response);
                            //failed, save for future
                            Toast.makeText(getApplicationContext(), "Illegal access", Toast.LENGTH_SHORT).show();
                            mSharedPref.write(SharedPreferences.SIGNATURE, response);
                        }

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

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
        if (BuildConfig.BUILD_TYPE.equals("debug"))
            Log.i(TAG, "ATTEMPTING confirmation " + inviter_id);
        queue = Volley.newRequestQueue(this);
        String url = mFirebaseRemoteConfig.getString("server_php") + "/apijson.php";

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        //Log.i("APP SIGNATURE STORED?", response);
                        if (response.contains("confirmation")) {
                            if (BuildConfig.BUILD_TYPE.equals("debug"))
                                Log.i(TAG, "Invitation " + response);
                            SharedPreferences.AddCoins(getApplicationContext(), 200);
                            mSharedPref.write(SharedPreferences.INVITER, 1);
                            Snackbar.make(findViewById(android.R.id.content),
                                    R.string.invitation_confirm, Snackbar.LENGTH_LONG).show();
                        } else {
                            if (BuildConfig.BUILD_TYPE.equals("debug"))
                                Log.i(TAG, "Invitation failed " + response);
                            //failed, save for future

                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (BuildConfig.BUILD_TYPE.equals("debug"))
                    Log.i(TAG, "Signature send fail " + error.toString());
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
        mSharedPref.write(SharedPreferences.RANDOM_AYAH_SEEN, true);

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


        //Crashlytics.log(Log.ERROR, TAG, error.toString());Log.i(TAG, " " + c.getTime());
        if(mSharedPref.contains(SharedPreferences.PREVIOUS_SET)){
            String yesterday = mSharedPref.read(SharedPreferences.PREVIOUS_SET, "0");
            long prev_day = Long.parseLong(yesterday);
            long today = c.getTime();
            if(Math.abs(prev_day-today)>(1000*60*60*24)){
                if (BuildConfig.BUILD_TYPE.equals("debug"))
                    Log.i(TAG, yesterday + " ONE DAY PASSED " + c.getTime());
                mSharedPref.write(SharedPreferences.NOMOREADS, false);
            }else{
                if (BuildConfig.BUILD_TYPE.equals("debug"))
                    Log.i(TAG, yesterday + " LESS THAN A DAY " + c.getTime());
            }


        }
    }

}
