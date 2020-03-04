package furqon.io.github.mobilproject;

import android.app.Application;
import android.content.Context;
import android.util.Log;
import android.widget.ArrayAdapter;

import androidx.lifecycle.ViewModelProviders;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class HTTPRequestHandler {
    private Context mContext;
    private RequestQueue queue;
    private ArrayList<JSONObject> jsonArrayResponse;
    private static HTTPRequestHandler INSTANCE;

    public HTTPRequestHandler(Context context){
        mContext = context;

    }

    public void httpRequest() {
        RequestQueue queue = Volley.newRequestQueue(mContext);
        String url = "https://inventivesolutionste.ipage.com/ajax_quran.php";

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
        for (JSONObject i:auclist
             ) {

            try{
                Log.d("JSONOBJECT", i.getString("arabic"));
            }catch (Exception sx){

            }
        }

//        MyListener myListener;
//        if(mContext instanceof MyListener){
//            myListener = (MyListener) mContext;
//            myListener.LoadTitlesFromServer();
//        }



    }
}
