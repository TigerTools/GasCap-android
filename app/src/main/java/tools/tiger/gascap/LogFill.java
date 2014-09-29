package tools.tiger.gascap;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.android.volley.Response;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import tools.tiger.gascap.app.GasApi;


public class LogFill extends Activity {

    public final static String API_TOKEN = "tools.tiger.gascap.API_TOKEN";
    public String apiToken;
    public JSONArray vehicles;
    public JSONArray locations;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        apiToken = intent.getStringExtra(Home.API_TOKEN);
        setContentView(R.layout.activity_log_fill);
        Response.Listener vehiclesListener = getVehiclesListener();
        GasApi.getVehiclesRequest(apiToken, vehiclesListener);
    }

    private Response.Listener<JSONArray> getVehiclesListener() {
        Response.Listener<JSONArray> listener = new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                vehicles = response;
                populateVehiclesSpinner();
            }
        };
        return listener;
    }

    private void populateVehiclesSpinner() {
        ArrayList<String> vehicleNames = new ArrayList<String>();
        for (int i = 0; i < vehicles.length(); i++) {
            try {
                JSONObject vehicle = vehicles.getJSONObject(i);
                String name = vehicle.optString("year") + ", " + vehicle.optString("make") + " - " + vehicle.optString("name");
                vehicleNames.add(name);
            } catch (JSONException e) {
                Log.d("LogFill.populateVehicleSpinner", e.getMessage());
            }

        }
        Spinner vehicleSpinner = (Spinner)findViewById(R.id.vehicle);
        vehicleSpinner.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, vehicleNames));
    }

    public void addLocation() {
        Intent locationIntent = new Intent(this, Home.class);
        locationIntent.putExtra(API_TOKEN, apiToken);
        startActivity(locationIntent);
    }

    public void addVehicle() {
        Intent vehicleIntent = new Intent(this, Vehicle.class);
        vehicleIntent.putExtra(API_TOKEN, apiToken);
        startActivity(vehicleIntent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.log_fill, menu);
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
