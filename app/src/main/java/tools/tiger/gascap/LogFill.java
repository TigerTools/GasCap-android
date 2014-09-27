package tools.tiger.gascap;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;


public class LogFill extends Activity {

    public final static String API_TOKEN = "tools.tiger.gascap.API_TOKEN";
    public String apiToken;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        apiToken = intent.getStringExtra(Home.API_TOKEN);
        setContentView(R.layout.activity_log_fill);
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
