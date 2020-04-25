package furqon.io.github.mobilproject;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
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
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks;
import com.google.firebase.dynamiclinks.PendingDynamicLinkData;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import io.fabric.sdk.android.Fabric;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LOGIN ACTIVITY";
    TextView welcome_txt;
    Button start_btn;
    private String token;
    private FirebaseAuth mAuth;
    FirebaseUser currentUser;

    private static final int VALID = 0;
    private static final int INVALID = 1;
    private String shash;
    private static final int REQUEST_INVITE = 0;

    private String currentSignature;
    private ArrayList<JSONObject> jsonArrayResponse;
    private RequestQueue queue;
    private FirebaseRemoteConfig mFirebaseRemoteConfig;
    private SharedPreferences mSharedPref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

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
        //========================================================
        mSharedPref = SharedPreferences.getInstance();
        mSharedPref.init(getApplicationContext());
        if (!mSharedPref.read(mSharedPref.TOKEN, "").isEmpty()) {
            token = mSharedPref.read(mSharedPref.TOKEN, "");
            Log.d(TAG, "TOKEN RESTORED:" + token);

        } else {

            token = null;

            Log.d(TAG, "TOKEN MISSING, RENEW");

        }


        updateUI();
        if (mSharedPref.isFirstRun()) {
            Intent intent = new Intent(this, ScrollingActivity.class);
            startActivity(intent);

        } else {

            CheckInviterThanked();
        }
        //UI
        //===============================================
        welcome_txt = findViewById(R.id.welcome_textView);
        welcome_txt.setText(R.string.welcome_title);

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
                            updateUI();


                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInAnonymously:failure", task.getException());

                            Crashlytics.log(Log.ERROR, TAG, task.getException().toString());
                            Toast.makeText(LoginActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                            currentUser = null;
                        }

                    }
                });
        // [END signin_anonymously]
        Log.e(TAG, "Signin ATTEMPT: " + mAuth);
    }

    private void updateUI() {
        boolean isSignedIn = (currentUser != null);

        // Status text
        if (isSignedIn) {
            Log.i(TAG, "SIGNED IN. FIREBASE AUTH " + currentUser.getUid());
            sendRegistrationToServer();
            //checkAppSignature(this);
        } else {
            signInAnonymously();
        }

    }

    private void sendRegistrationToServer() {
        //TODO send the token to  database.
        //Add to the user account token, app id, device id
        //Log.i("ATTEMPTING TOKEN SEND", token);

        queue = Volley.newRequestQueue(this);
        String url = mFirebaseRemoteConfig.getString("server_link") + "/apijson.php";
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
                MyData.put("app_id", BuildConfig.VERSION_NAME);
                return MyData;
            }

        };
        if (token != null && currentUser != null) {
            queue.add(stringRequest);
            //Toast.makeText(this, "Initiation", Toast.LENGTH_SHORT).show();
            Log.d(TAG, "sending creds");

        } else {
            Log.d(TAG, "missing important creds");
            Toast.makeText(this, "You are missing important credentials. Try to restart the app!", Toast.LENGTH_LONG).show();
        }


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
                                Snackbar.make(findViewById(android.R.id.content),
                                        R.string.invitation_confirm, Snackbar.LENGTH_LONG).show();
                                //Send confirmation
                                if (mSharedPref.read(mSharedPref.INVITER, 0) == 0) {
                                    sendConfirmationToServer(inviter_id);
                                } else {
                                    //user is already invited
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
                sendConfirmationToServer(inviter_id);
            } else {
                Crashlytics.log(Log.ERROR, TAG, "INVITER ID WAS EMPTY");
            }
        } else {
            //user is already invited
        }
    }

    //    private int checkAppSignature(Context context) {
//        try {
//
//            PackageInfo packageInfo;
//            packageInfo = context.getPackageManager()
//
//                    .getPackageInfo(context.getPackageName(),
//
//                            PackageManager.GET_SIGNATURES);
//
//
//            for (Signature signature : packageInfo.signatures) {
//
//                byte[] signatureBytes = signature.toByteArray();
//
//                MessageDigest md = MessageDigest.getInstance("SHA");
//
//                md.update(signature.toByteArray());
//
//
//                currentSignature = Base64.encodeToString(md.digest(), Base64.DEFAULT);
//                String model = Build.MODEL;
//                String manufacturer = Build.MANUFACTURER;
//                String bootloader = Build.BOOTLOADER;
//
//
//                //checkSignatureOnServer(currentSignature);
//                //Log.d("REMOVE_ME", "Include this string as a value for SIGNATURE:" + currentSignature + model + manufacturer + bootloader);
//
//                //compare signatures
//
//            }
//
//        } catch (Exception e) {
//            Crashlytics.log(Log.ERROR, TAG, e.getStackTrace().toString());
//
////assumes an issue in checking signature., but we let the caller decide on what to do.
//
//        }
//        sendRegistrationToServer();
//        return INVALID;
//
//    }
    private void sendConfirmationToServer(final String inviter_id) {
        //TODO send the token to  database.
        //Add to the user account token, app id, device id
        Log.i(TAG, "ATTEMPTING confirmation " + inviter_id);
        queue = Volley.newRequestQueue(this);
        String url = mFirebaseRemoteConfig.getString("server_link") + "/apijson.php";

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        //Log.i("APP SIGNATURE STORED?", response);
                        if (response.contains("confirmation")) {
                            Log.i(TAG, "Invitation " + response);
                            mSharedPref.AddCoins(getApplicationContext(), 200);
                            mSharedPref.write(mSharedPref.INVITER, 1);

                        } else {
                            Log.i(TAG, "Invitation failed " + response);
                            //failed, save for future
                            mSharedPref.write(mSharedPref.INVITER_ID, inviter_id);
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

    public void ayahOfTheDay() {

        Intent intent = new Intent(this, AyahOfTheDay.class);
        startActivity(intent);
        mSharedPref.write(mSharedPref.RANDOM_AYAH_SEEN, true);

    }
}
