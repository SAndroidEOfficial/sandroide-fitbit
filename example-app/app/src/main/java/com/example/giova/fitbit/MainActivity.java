package com.example.giova.fitbit;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import it.unibs.sandroide.lib.activities.SandroideBaseActivity;
import it.unibs.sandroide.lib.item.restapi.OnApiCallListener;
import it.unibs.sandroide.lib.item.restapi.RestAPI;

public class MainActivity  extends SandroideBaseActivity {
    protected static final String TAG = "MainActivity";
    RestAPI fitbitAPI;

    Button btnFitbitLogin, btnFitbitSteps, btnIntradayHeartRate;
    TextView tvResult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        try {
            // Here we initialize the fitbit API
            // You need to create a Fitbit developer account on dev.fitbit.com, and generate a new app, to get a valid your oauth2_client_id
            fitbitAPI = new RestAPI(this,"api_fitbit_v1",new JSONObject()   // this is the name of the sandroide json file containing the Fitbit api specification
                    .put("oauth2_clientid", "228CLH")                   // this is your fitbit app OAuth 2.0 Client ID, generated on dev.fitbit.com
                    .put("redirect_uri", URLEncoder.encode("https://www.example.com/index.html?","UTF-8"))  // this must match with the url entered for your fitbit app on dev.fitbit.com
                    .put("expires_in", 604800)
                    .put("scope", "activity%20heartrate%20location%20nutrition%20profile%20settings%20sleep%20social%20weight")); // this is the list of permissions request from this android app

        } catch (JSONException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        tvResult = (TextView) findViewById(R.id.tvApiResult);
        btnIntradayHeartRate = (Button) findViewById(R.id.buttonHeartRate);
        btnFitbitSteps = (Button) findViewById(R.id.buttonSteps);

        // Here we create a button to start the authentication workflow, but this is also called automatically
        // when calling other API calls and response is "401 Unauthorized user"
        btnFitbitLogin = (Button) findViewById(R.id.buttonLogin);


        // first way: provide the listener once ...
        fitbitAPI.setOnApiCallListener("heartrate_intraday",new OnApiCallListener() {
            @Override
            public void onResult(int statusCode, String body) {
                try {
                    JSONObject responseObject = new JSONObject(body);
                    JSONArray dataset = responseObject.getJSONObject("activities-heart-intraday").getJSONArray("dataset");
                    String str="";
                    for (int i =0; i<dataset.length(); i++) {
                        JSONObject row = dataset.getJSONObject(i);
                        if (row.getInt("value")>100) {
                            str += String.format("High Heart Rate! was %d at %s\n",row.getInt("value"),row.get("time"));
                        }
                    }
                    printLine(str);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });

        btnIntradayHeartRate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    // first way: ... then execute the API call later, also multiple times with different parameters.
                    fitbitAPI.runApiCall("heartrate_intraday", new JSONObject()
                            .put("user-id", "-")
                            .put("date", "2017-02-20")
                            .put("end-date", "2017-02-20")
                            .put("detail-level", "1min"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });


        // second way: execute the API call with paramters, and specify inline, the callback to be executed on success
        btnFitbitSteps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    fitbitAPI.runApiCall("activity_intraday_time_series", new JSONObject()
                            .put("user-id", "-")
                            .put("resource-path", "activities/steps")
                            .put("date", "2017-02-20")
                            .put("end-date", "2017-02-20")
                            .put("detail-level", "1min")
                            .put("start-time", "00:00")
                            .put("end-time", "23:59"),new OnApiCallListener() {
                        @Override
                        public void onResult(int statusCode, String body) {
                            try {
                                JSONObject responseObject = new JSONObject(body);
                                JSONArray dataset = responseObject.getJSONObject("activities-steps-intraday").getJSONArray("dataset");
                                String str="";
                                for (int i =0; i<dataset.length(); i++) {
                                    JSONObject row = dataset.getJSONObject(i);
                                    if (row.getInt("value")>1) {
                                        str += String.format("Some steps! was %d at %s\n",row.getInt("value"),row.get("time"));
                                    }
                                }
                                printLine(str);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    });
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });


        btnFitbitLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fitbitAPI.authenticate();
            }
        });
    }

    // listener is called, when the api call succeeds
    protected void printLine(String result) {
        tvResult.setText(result);
        Log.i("MainActivityFitbitApi",result);
    }

}
