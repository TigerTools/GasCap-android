package tools.tiger.gascap;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.Signature;
import android.graphics.Color;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.android.volley.Request.Method;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import io.oauth.OAuth;
import io.oauth.OAuthCallback;
import io.oauth.OAuthData;
import io.oauth.OAuthRequest;
import tools.tiger.gascap.app.App_Gas;
import tools.tiger.gascap.app.GasClient;


public class Home extends Activity implements OAuthCallback {

    Button facebookButton;
    Button googleButton;
    TextView facebookText;
    TextView googleText;
    private String keyHash;
    private String urlKeyHash;
    private String storagePath;
    private String email;
    private String token;
    private String urlEmail;
    private boolean loggedIn = false;
    private String apiToken = "";
    private String apiVehicle = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        String user_content = null;
        String token_content = null;
        String vehicle_content = null;
        byte[] datainBytes = new byte[0];
        super.onCreate(savedInstanceState);

        storagePath = App_Gas.getAppContext().getFilesDir().getAbsolutePath();
        App_Gas.getAppContext().getFilesDir().mkdirs();

        try {
            PackageInfo info = getPackageManager().getPackageInfo("tools.tiger.gascap", PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                keyHash = Base64.encodeToString(md.digest(), Base64.DEFAULT);
                try {
                    urlKeyHash = URLEncoder.encode(keyHash.trim(), "UTF-8");
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                Log.d("keyHash:", keyHash);
                Log.d("urlKeyHash:", urlKeyHash);
            }
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        File oauthFile = new File(storagePath, getString(R.string.oauth_user_cache));
        if (!oauthFile.exists()) {
            try {
                oauthFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try {
            DataInputStream dis =
                    new DataInputStream (
                            new FileInputStream(oauthFile));

            datainBytes = new byte[dis.available()];
            dis.readFully(datainBytes);
            dis.close();
            user_content = new String(datainBytes, 0, datainBytes.length);
        } catch (Exception e) {
            e.printStackTrace();
        }

        Log.d("user_content", user_content);
        if (datainBytes.length > 0) {
            try {
                JSONObject result = new JSONObject(user_content);
                if (result.has("email")) {
                    email = result.getString("email");
                    Log.d("email", email);
                    loggedIn = true;
                } else {
                    JSONArray emails = result.getJSONArray("emails");
                    JSONObject emailObj = emails.getJSONObject(0);
                    try {
                        email = emailObj.getString("value");
                        Log.d("email", email);
                        loggedIn = true;
                    } catch (JSONException e) {
                        Log.d("obj:", emailObj.toString());
                    }
                }

            } catch (Throwable t) {
                Log.e("Home", "Could not parse user_content JSON: \"");
            }
        }

        /* Check login status */
        if (loggedIn == true) {
            try {
                urlEmail = URLEncoder.encode(email, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            datainBytes = new byte[0];
            File tokenFile = new File(storagePath, getString(R.string.api_token_cache));
            if (!tokenFile.exists()) {
                try {
                    tokenFile.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            try {
                DataInputStream dis =
                        new DataInputStream (
                                new FileInputStream(tokenFile));

                datainBytes = new byte[dis.available()];
                dis.readFully(datainBytes);
                dis.close();

                token_content = new String(datainBytes, 0, datainBytes.length);
            } catch (Exception e) {
                e.printStackTrace();
            }

            if (datainBytes.length > 0) {
                try {
                    JSONObject result = new JSONObject(token_content);
                    if (result.has("key")) {
                        apiToken = result.getString("key");
                        Log.d("apiToken", apiToken);
                    }
                } catch (Throwable t) {
                    Log.e("Home", "Could not parse Api Token JSON: \"");
                }
            }

            datainBytes = new byte[0];
            File vehicleFile = new File(storagePath, getString(R.string.api_vehicle_cache));
            if (!tokenFile.exists()) {
                try {
                    vehicleFile.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            try {
                DataInputStream dis =
                        new DataInputStream (
                                new FileInputStream(vehicleFile));

                datainBytes = new byte[dis.available()];
                dis.readFully(datainBytes);
                dis.close();

                vehicle_content = new String(datainBytes, 0, datainBytes.length);
            } catch (Exception e) {
                e.printStackTrace();
            }

            if (datainBytes.length > 0) {
                try {
                    JSONObject result = new JSONObject(vehicle_content);
                    if (result.has("name")) {
                        apiVehicle = result.getString("year") + ", " + result.getString("make") + " - " + result.getString("name");
                        Log.d("apiVehicle", apiVehicle);
                    }
                } catch (Throwable t) {
                    Log.e("Home", "Could not parse Api Vehicle JSON: \"");
                }
            }


            if (apiToken == "") {
                // Instantiate the RequestQueue.
                RequestQueue queue = GasClient.getRequestQueue();
                String url = getString(R.string.tigertools_api_protocol)
                             + getString(R.string.tigertools_api_endppoint)
                             + ":" + getString(R.string.tigertools_api_port)
                             + "/token?hashkey=" + urlKeyHash
                             + "&email=" +  urlEmail;

                JsonObjectRequest tokenReq = new JsonObjectRequest(Method.GET,
                        url,
                        null,
                        new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {
                                Log.d("raw_token_resp", response.toString());
                                try {
                                    if (response.has("key")) {
                                        token = response.getString("key");
                                        Log.d("token", token);
                                    }

                                    File file = new File(storagePath, getString(R.string.api_token_cache));
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
                                        outputStream = openFileOutput(getString(R.string.api_token_cache), Context.MODE_PRIVATE);
                                        outputStream.write(string.getBytes());
                                        outputStream.close();
                                        Intent intent = getIntent();
                                        finish();
                                        startActivity(intent);
                                    } catch (Exception e) {
                                        e.printStackTrace();
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
                queue.add(tokenReq);

            } else if (apiVehicle == "") {

            } else {
                this.setContentView(R.layout.activity_home);
            }
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
                        Log.d("email", email);
                    } else {
                        JSONArray emails = result.getJSONArray("emails");
                        JSONObject emailObj = emails.getJSONObject(0);
                        try {
                            email = emailObj.getString("value");
                            Log.d("email", email);
                        } catch (JSONException e) {
                            Log.d("obj:", emailObj.toString());
                        }
                    }

                    File file = new File(storagePath, getString(R.string.oauth_user_cache));
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
                    String string = result.toString();
                    FileOutputStream outputStream;

                    try {
                        outputStream = openFileOutput(getString(R.string.oauth_user_cache), Context.MODE_PRIVATE);
                        outputStream.write(string.getBytes());
                        outputStream.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

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
