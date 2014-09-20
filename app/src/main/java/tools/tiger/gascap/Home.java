package tools.tiger.gascap;

import android.app.Activity;
import android.content.pm.PackageInfo;
import android.content.pm.Signature;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import org.json.JSONArray;

import org.json.JSONException;
import org.json.JSONObject;
import android.os.StrictMode;
import android.graphics.Color;
import android.widget.Button;
import android.widget.TextView;
import io.oauth.*;


public class Home extends Activity implements OAuthCallback {

    Button facebookButton;
    Button googleButton;
    TextView facebookText;
    TextView googleText;
    boolean loggedIn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            PackageInfo info = getPackageManager().getPackageInfo("tools.tiger.gascap", PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                Log.d("KeyHash:", Base64.encodeToString(md.digest(), Base64.DEFAULT));
            }
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        loggedIn = false;
        /* Check login status */
        if (loggedIn == true){
            this.setContentView(R.layout.activity_home);
        }

        else if (loggedIn == false){
            this.setContentView(R.layout.activity_login);
        }

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

    public void onFinished(OAuthData data) {
        final TextView textview = data.provider.equals("google") ? googleText : facebookText;
        if (!data.status.equals("success")) {
            textview.setTextColor(Color.parseColor("#FF0000"));
            textview.setText("error, " + data.error);
        }

        // You can access the tokens through data.token and data.secret

        textview.setText("loading...");
        textview.setTextColor(Color.parseColor("#00FF00"));

        // Let's skip the NetworkOnMainThreadException for the purpose of this sample.
        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().permitAll().build());

        // To make an authenticated request, you can implement OAuthRequest with your prefered way.
        // Here, we use an URLConnection (HttpURLConnection) but you can use any library.
        data.http(data.provider.equals("facebook") ? "/me" : "/plus/v1/people/me", new OAuthRequest(data) {
            private URL url;
            private URLConnection con;
            private String email;

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
                    if (result.has("email")) {
                        email = result.getString("email");
                    } else {
                        JSONArray emails = result.getJSONArray("emails");
                        JSONObject emailObj = emails.getJSONObject(0);
                        try {
                            email = emailObj.getString("value");
                        } catch (JSONException e) {
                            Log.d("obj:", emailObj.toString());
                        }
                    }
                    textview.setText("hello, " + email);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onError(String message) {
                textview.setText("error: " + message);
            }
        });

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
