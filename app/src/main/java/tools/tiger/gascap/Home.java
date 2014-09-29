package tools.tiger.gascap;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import com.android.volley.RequestQueue;
import com.android.volley.Response;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import tools.tiger.gascap.app.AccountUtils;
import tools.tiger.gascap.app.GasApi;
import tools.tiger.gascap.app.GasClient;
import tools.tiger.gascap.app.JsonGasRequest;

public class Home extends Activity {

    public final static String API_TOKEN = "tools.tiger.gascap.API_TOKEN";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        String apiToken;
        String email;
        String urlKeyHash;
        String apiVehicle;

        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_home);
        Intent intent = getIntent();
        apiToken = intent.getStringExtra(Home.API_TOKEN);
        urlKeyHash = GasApi.getUrlHash(this, "tools.tiger.gascap");
        email = AccountUtils.getAccountName(this);

        if (email != null) {
            JSONObject gasToken = GasApi.getJSONObjectFileContent(getString(R.string.api_token_cache));
            Log.d("gasToken result", gasToken.toString());

            try {
                apiToken = gasToken.getString("key");
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
                String urlEmail = "";
                try {
                    urlEmail = URLEncoder.encode(email, "UTF-8");
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                final RequestQueue queue = GasClient.getRequestQueue();
                String resource = "gasToken?hashkey=" + urlKeyHash + "&email=" + urlEmail;
                Response.Listener<JSONObject> listener = getTokenListener();
                JsonGasRequest tokenReq = GasApi.request(resource, apiToken, listener);
                queue.add(tokenReq);
            } else {
                final String token = apiToken;
                Button logFillButton = (Button) findViewById(R.id.log_fill_button);
                final Intent logFillIntent = new Intent(this, LogFill.class);
                logFillButton.setOnClickListener(new View.OnClickListener() { // Listen the on click event
                    @Override
                    public void onClick(View v) {
                        logFillIntent.putExtra(API_TOKEN, token);
                        startActivity(logFillIntent);
                    }
                });
            }
        } else {
            Intent googleAuthIntent = new Intent(this, GoogleAuth.class);
            startActivity(googleAuthIntent);
        }
    }

    private void doVehicle(String apiToken) {
        Intent vehicleIntent = new Intent(this, Vehicle.class);
        vehicleIntent.putExtra(API_TOKEN, apiToken);
        startActivity(vehicleIntent);
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
                doVehicle(apiToken);
            }
        };
        return listener;
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
}
