package tools.tiger.gascap;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

import io.oauth.OAuth;
import io.oauth.OAuthCallback;
import io.oauth.OAuthData;
import io.oauth.OAuthRequest;

import tools.tiger.gascap.app.GasApi;
import tools.tiger.gascap.app.GasClient;
import tools.tiger.gascap.app.JsonGasRequest;


public class Home extends Activity implements OAuthCallback {

    Button facebookButton;
    Button googleButton;
    TextView facebookText;
    TextView googleText;
    private String email;
    private String urlEmail;
    private String urlKeyHash;
    private boolean loggedIn = false;
    private String apiToken = "";
    private String apiVehicle = "";

    public final static String API_TOKEN = "tools.tiger.gascap.API_TOKEN";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        apiToken = intent.getStringExtra(Home.API_TOKEN);
        urlKeyHash = GasApi.getUrlHash(this, "tools.tiger.gascap");
        JSONObject user = GasApi.getJSONObjectFileContent(getString(R.string.oauth_user_cache));
        Log.d("user result", user.toString());

        try {
            if (user.has("email")) {
                email = user.getString("email");
                loggedIn = true;
            } else {
                JSONArray emails = user.getJSONArray("emails");
                JSONObject emailObj = emails.getJSONObject(0);
                try {
                    email = emailObj.getString("email");
                    loggedIn = true;
                } catch (JSONException e) {
                    Log.d("email", e.getMessage());
                }
            }
        } catch (JSONException e) {
            Log.d("email", e.getMessage());
        }

        if (loggedIn == true) {
            try {
                urlEmail = URLEncoder.encode(email, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }

            JSONObject token = GasApi.getJSONObjectFileContent(getString(R.string.api_token_cache));
            Log.d("token result", token.toString());

            try {
                apiToken = token.getString("key");
                Log.d("apiToken", apiToken);
            } catch (JSONException e) {
                Log.d("apiToken", e.getMessage());
            }

            JSONArray vehicles = GasApi.getJSONArrayFileContent(getString(R.string.api_vehicle_cache));
            Log.d("vehicle result", vehicles.toString());

            try {
                JSONObject vehicle = vehicles.getJSONObject(0);
                apiVehicle = vehicle.getString("year") + ", " + vehicle.getString("make") + " - " + vehicle.getString("name");
                Log.d("apiVehicle", apiVehicle);
            } catch (JSONException e) {
                Log.d("apiVehicle", e.getMessage());
            }

            if (apiToken == null) {
                final RequestQueue queue = GasClient.getRequestQueue();
                String resource = "token?hashkey=" + urlKeyHash + "&email=" + urlEmail;
                Response.Listener<JSONObject> listener = getTokenListener();
                JsonGasRequest tokenReq = GasApi.request(resource, apiToken, listener);
                queue.add(tokenReq);
            }

            if (apiVehicle == "") {
                Intent vehicleIntent = new Intent(this, Vehicle.class);
                vehicleIntent.putExtra(API_TOKEN, apiToken);
                startActivity(vehicleIntent);
            }

            this.setContentView(R.layout.activity_home);
            Button logFillButton = (Button) findViewById(R.id.log_fill_button);
            final Intent logFillIntent = new Intent(this, LogFill.class);
            logFillButton.setOnClickListener(new View.OnClickListener() { // Listen the on click event
                @Override
                public void onClick(View v)
                {
                    logFillIntent.putExtra(API_TOKEN, apiToken);
                    startActivity(logFillIntent);
                }
            });

        }

        else if (loggedIn == false){
            this.setContentView(R.layout.activity_login);
            final OAuth o = new OAuth(this);
            o.initialize("4_wlfPF3Bc7If-Dz6uNLz-8b2ow"); // Initialize the oauth key
            facebookButton = (Button) findViewById(R.id.facebook);
            googleButton = (Button) findViewById(R.id.google);
            facebookText = (TextView) findViewById(R.id.facebookText);
            googleText = (TextView) findViewById(R.id.googleText);

            facebookButton.setOnClickListener(new View.OnClickListener() { // Listen the on click event
                @Override
                public void onClick(View v)
                {
                    o.popup("facebook", Home.this); // Launch the pop up with the right provider & callback
                }
            });

            googleButton.setOnClickListener(new View.OnClickListener() { // Listen the on click event
                @Override
                public void onClick(View v)
                {
                    o.popup("google", Home.this); // Launch the pop up with the right provider & callback
                }
            });
        }
    }

    private void doVehicleRequest(String apiToken) {
        final RequestQueue queue = GasClient.getRequestQueue();
        String resource = "gas/vehicles/";
        Response.Listener<JSONArray> listener = this.getVehicleListener();
        JsonGasRequest vehicleReq = GasApi.request(Request.Method.GET, resource, apiToken, listener);
        queue.add(vehicleReq);
    }

    private Response.Listener<JSONObject> getTokenListener() {
        Response.Listener<JSONObject> listener = new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                String apiToken = "";
                Log.d("token response", response.toString());
                try {
                    apiToken = response.getString("key");
                } catch (Exception e) {
                    Log.d("getTokenListener", "Token failure: apiToken=\"" + apiToken + "\"");
                }
                GasApi.setJSONObjectFileContent(getString(R.string.api_token_cache), response);
                doVehicleRequest(apiToken);
            }
        };
        return listener;
    }

    private Response.Listener<JSONArray> getVehicleListener() {
        Response.Listener<JSONArray> listener = new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                try {
                    Log.d("vehicle response", response.toString());
                    GasApi.setJSONArrayFileContent(getString(R.string.api_vehicle_cache), response);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        return listener;
    }

    public void onFinished(OAuthData data) {
        final TextView textview = data.provider.equals("google") ? googleText : facebookText;
        if (!data.status.equals("success")) {
            textview.setTextColor(Color.parseColor("#FF0000"));
            textview.setText("error, " + data.error);
        }

        textview.setText("loading...");
        textview.setTextColor(Color.parseColor("#00FF00"));

        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().permitAll().build());

        data.http(data.provider.equals("facebook") ? "/me" : "/plus/v1/people/me", new OAuthRequest(data) {
            private URL url;
            private URLConnection con;

            @Override
            public void onSetURL(String _url) {
                try {
                    url = new URL(_url);
                    con = url.openConnection();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onSetHeader(String header, String value) {
                con.addRequestProperty(header, value);
            }

            @Override
            public void onReady() {
                try {
                    OAuthData myData = getOAuthData();
                    con.setRequestProperty("Authorization",
                            "Bearer " +myData.token);
                    BufferedReader r = new BufferedReader(new InputStreamReader(con.getInputStream()));
                    StringBuilder total = new StringBuilder();
                    String line;
                    while ((line = r.readLine()) != null) {
                        total.append(line);
                    }
                    JSONObject result = new JSONObject(total.toString());
                    GasApi.setJSONObjectFileContent(getString(R.string.oauth_user_cache), result);

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onError(String message) {
                textview.setText("error: " + message);
            }
        });

        Intent intent = getIntent();
        finish();
        startActivity(intent);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void doLogFill(View view) {
        //do stuff
    }
}
