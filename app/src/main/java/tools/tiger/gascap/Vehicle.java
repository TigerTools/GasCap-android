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

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

import tools.tiger.gascap.app.App_Gas;
import tools.tiger.gascap.app.GasClient;


public class Vehicle extends Activity {

    private Integer vehicleId = 0;
    private String storagePath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        storagePath = App_Gas.getAppContext().getFilesDir().getAbsolutePath();
        App_Gas.getAppContext().getFilesDir().mkdirs();

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

    /** Called when the user clicks the Send button */
    public void saveVehicle(View view) {
        boolean vehicleSaved = false;
        Intent intent = new Intent(this, Home.class);
        EditText vehicleName = (EditText) findViewById(R.id.vehicleName);
        EditText vehicleMake = (EditText) findViewById(R.id.vehicleMake);
        Spinner vehicleYear = (Spinner)findViewById(R.id.vehicleYear);
        String name = vehicleName.getText().toString();
        String make = vehicleMake.getText().toString();
        String year = vehicleYear.getSelectedItem().toString();

        RequestQueue queue = GasClient.getRequestQueue();
        String url = getString(R.string.tigertools_api_protocol)
                + getString(R.string.tigertools_api_endppoint)
                + ":" + getString(R.string.tigertools_api_port)
                + "/gas/vehicles/";

        JsonObjectRequest vehicleReq = new JsonObjectRequest(Request.Method.POST,
                url,
                null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        Log.d("raw_vehicle_resp", response.toString());
                        try {
                            if (response.length() > 0) {
                                Log.d("file", getString(R.string.api_vehicle_cache));
                                File file = new File(storagePath, getString(R.string.api_vehicle_cache));
                                if (!file.exists()) {
                                    try {
                                        file.createNewFile();
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                } else {
                                    file.delete();
                                    file.createNewFile();
                                }
                                String string = response.toString();
                                FileOutputStream outputStream;

                                try {
                                    outputStream = openFileOutput(getString(R.string.api_vehicle_cache), Context.MODE_PRIVATE);
                                    outputStream.write(string.getBytes());
                                    outputStream.close();
                                    Intent intent = getIntent();
                                    finish();
                                    startActivity(intent);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d("token", "Remote Call Failed");
                    }
                });
        if (vehicleSaved) {
            startActivity(intent);
        }
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