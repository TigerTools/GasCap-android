package tools.tiger.gascap;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonArrayRequest;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import tools.tiger.gascap.app.GasApi;
import tools.tiger.gascap.app.GasApp;
import tools.tiger.gascap.app.GasClient;
import tools.tiger.gascap.app.JsonGasRequest;


public class Vehicle extends Activity {
    private Integer vehicleId = 0;
    private String apiToken;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        apiToken = intent.getStringExtra(Home.API_TOKEN);
        doVehicleRequest(apiToken);
        if (vehicleId == 0) {
            setContentView(R.layout.activity_vehicle);

            Calendar c = Calendar.getInstance();
            int currentYear = c.get(Calendar.YEAR);
            ArrayList<String> options=new ArrayList<String>();
            for(int i=1950; i<=(currentYear + 1); i++){
                options.add(Integer.toString(i));
            }
            Collections.reverse(options);
            Spinner vehicleYear = (Spinner)findViewById(R.id.vehicleYear);
            ArrayAdapter adapter = new ArrayAdapter(this, android.R.layout.simple_spinner_item, options);
            vehicleYear.setAdapter(adapter);

        } else {
            setContentView(R.layout.activity_vehicle_edit);
        }
    }

    private void doVehicleRequest(String apiToken) {
        final RequestQueue queue = GasClient.getRequestQueue();
        String resource = "gas/vehicles/";
        Response.Listener<JSONArray> listener = this.getVehicleListener();
        JsonGasRequest vehicleReq = GasApi.request(Request.Method.GET, resource, apiToken, listener);
        queue.add(vehicleReq);
    }

    private Response.Listener<JSONArray> getVehicleListener() {
        final Intent intent = new Intent(this, Home.class);
        Response.Listener<JSONArray> listener = new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                try {
                    Log.d("getVehicleListener", response.toString());
                    if (response.length() > 0) {
                        GasApi.setJSONArrayFileContent(getString(R.string.api_vehicle_cache), response);
                        intent.putExtra(Home.API_TOKEN, apiToken);
                        startActivity(intent);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        return listener;
    }

    private Response.Listener<JSONObject> getVehicleSaveListener() {
        Response.Listener<JSONObject> listener = new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    Log.d("getVehicleSaveListener", response.toString());
                    doVehicleRequest(apiToken);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        return listener;
    }

    /** Called when the user clicks the Send button */
    public void saveVehicle(final View view) {
        final RequestQueue queue = GasClient.getRequestQueue();
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("name", ((EditText) findViewById(R.id.vehicleName)).getText().toString());
        params.put("make", ((EditText) findViewById(R.id.vehicleMake)).getText().toString());
        params.put("year", ((Spinner)findViewById(R.id.vehicleYear)).getSelectedItem().toString());
        String resource = "gas/vehicles/";
        Response.Listener<JSONObject> listener = this.getVehicleSaveListener();
        JsonGasRequest vehicleReq = GasApi.request(Request.Method.POST, resource, apiToken, listener, params);
        queue.add(vehicleReq);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.vehicle, menu);
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
