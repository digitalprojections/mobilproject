package furqon.io.github.mobilproject;

import android.content.Context;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class HTTPRequestHandler {
    private FirebaseRemoteConfig mFirebaseRemoteConfig;
    private Context mContext;
    private RequestQueue queue;
    private ArrayList<JSONObject> jsonArrayResponse;
    private static HTTPRequestHandler INSTANCE;
    private TitleViewModel tvm;

    public HTTPRequestHandler(Context context, TitleViewModel titleViewModel){
        mContext = context;
        tvm = titleViewModel;
        mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance();
        FirebaseRemoteConfigSettings configSettings = new FirebaseRemoteConfigSettings.Builder()
                .setMinimumFetchIntervalInSeconds(3600)
                .build();
        mFirebaseRemoteConfig.setConfigSettingsAsync(configSettings);
    }

    public void httpRequest() {
        RequestQueue queue = Volley.newRequestQueue(mContext);
        String url = mFirebaseRemoteConfig.getString("server_php") + "/ajax_quran.php";

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        //progressBar.setVisibility(View.INVISIBLE);
                        // Convert String to json object
                        jsonArrayResponse = new ArrayList<JSONObject>();

                        try {
                            JSONArray jsonArray = new JSONArray(response);
                            for (int i=0; i<jsonArray.length();i++)
                            {
                                JSONObject object = new JSONObject(jsonArray.getString(i));
                                jsonArrayResponse.add(object);
                            }

                            //PASS to SPINNER
                            //load auction names and available lot/bid count
                            populateAuctionList(jsonArrayResponse);

                        } catch (JSONException e) {
                            e.printStackTrace();
                            Log.i("error json", "tttttttttttttttt");
                        }

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                //progressBar.setVisibility(View.INVISIBLE);
            }
        }) {
            protected Map<String, String> getParams() {
                Map<String, String> MyData = new HashMap<String, String>();
                MyData.put("action", "names_as_objects"); //Add the data you'd like to send to the server.
                MyData.put("language_id", "1");
                //https://inventivesolutionste.ipage.com/ajax_quran.php
                //POST
                //action:names_as_objects
                //language_id:1
                return MyData;
            }

        };

        queue.add(stringRequest);
        //progressBar.setVisibility(View.VISIBLE);
    }
    private void populateAuctionList(ArrayList<JSONObject> auclist){

//        ArrayAdapter<String> adapter = new ArrayAdapter<String>(, android.R.layout.simple_spinner_item, auclist);
//        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        //spinner.setAdapter(adapter);
        ChapterTitleTable title;
        MyListener myListener;
        SuraNameList nameList = (SuraNameList) mContext;
        myListener =  (MyListener) nameList;



        for (JSONObject i:auclist
             ) {

            try{
                Log.d("JSONOBJECT", i.getString("arabic"));
                int language_no = i.getInt("language_no");
                int order_no = i.getInt("order_no");
                int chapter_id = i.getInt("chapter_id");
                String surah_type = i.getString("surah_type");
                String uzbek = i.getString("uzbek");
                String arabic = i.getString("arabic");

                title = new ChapterTitleTable(language_no, order_no, chapter_id, uzbek, arabic, surah_type);
                tvm.insert(title);


            }catch (Exception sx){

            }
        }




    }
}
