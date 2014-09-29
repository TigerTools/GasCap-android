package tools.tiger.gascap.app;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.util.Base64;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateFactory;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.HashMap;

import tools.tiger.gascap.R;


/**
 * Created by jgreathouse on 9/25/2014.
 */
public class GasApi {

    private static String storagePath;
    private static String endpoint;

    private GasApi() {
        Context context = GasApp.getAppContext();
        storagePath = GasApp.getAppContext().getFilesDir().getAbsolutePath();
        endpoint = context.getString(R.string.tigertools_api_protocol) + context.getString(R.string.tigertools_api_endppoint) + ":" + context.getString(R.string.tigertools_api_port) + "/";
        context.getFilesDir().mkdirs();
    }

    static {
        Context context = GasApp.getAppContext();
        storagePath = GasApp.getAppContext().getFilesDir().getAbsolutePath();
        endpoint = context.getString(R.string.tigertools_api_protocol) + context.getString(R.string.tigertools_api_endppoint) + ":" + context.getString(R.string.tigertools_api_port) + "/";
        context.getFilesDir().mkdirs();
    }

    public static JsonGasRequest request(Integer method, final String resource, final String apiToken, Response.Listener listener, final HashMap<String, String> params) {

        JsonGasRequest gasRequest = new JsonGasRequest(method,
                endpoint + resource,
                apiToken,
                params,
                listener,
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("GasApi.request", error.toString());
                        Log.d("GasApi.request", "endpoint + resource=\"" + endpoint + resource + "\"");
                        Log.d("GasApi.request", "apiToken=\"" + apiToken + "\"");
                        Log.d("GasApi.request", "params=\"" + params.toString() + "\"");
                    }
                }
        );
        return gasRequest;
    }

    public static JsonGasRequest request(Integer method, String resource, String apiToken, Response.Listener listener) {
        HashMap<String, String> params = new HashMap<String, String>();
        return GasApi.request(method, resource, apiToken, listener, params);
    }

    public static JsonGasRequest request(String resource, String apiToken, Response.Listener listener) {
        HashMap<String, String> params = new HashMap<String, String>();
        return GasApi.request(Request.Method.GET, resource, apiToken, listener, params);
    }

    public static void getVehiclesRequest(Integer method, String apiToken, Response.Listener listener, HashMap<String, String> params) {
        final RequestQueue queue = GasClient.getRequestQueue();
        String resource = "gas/vehicles/";
        JsonGasRequest vehicleReq = GasApi.request(method, resource, apiToken, listener, params);
        queue.add(vehicleReq);
    }

    public static void getVehiclesRequest(Integer method, String apiToken, Response.Listener listener) {
        HashMap<String, String> params = new HashMap<String, String>();
        getVehiclesRequest(method, apiToken, listener, params);
    }

    public static void getVehiclesRequest(String apiToken, Response.Listener listener) {
        Integer method = Request.Method.GET;
        getVehiclesRequest(method, apiToken, listener);
    }

    public static void getVehicleByIdRequest(Integer vehicleId, String apiToken, Response.Listener listener) {
        Integer method = Request.Method.GET;
        final RequestQueue queue = GasClient.getRequestQueue();
        String resource = "gas/vehicles/" + vehicleId.toString() + "/";
        JsonGasRequest vehicleReq = GasApi.request(method, resource, apiToken, listener);
        queue.add(vehicleReq);
    }

    public static void saveVehicleRequest(String apiToken, Response.Listener listener, HashMap<String, String> params) {
        Integer method = Request.Method.POST;
        getVehiclesRequest(method, apiToken, listener, params);
    }

    public static Response.Listener<JSONObject> JSONObjectListener() {
        Response.Listener listener = new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Log.d(this.getClass().getName(), response.toString());
            }
        };
        return listener;
    }

    public static Response.Listener<JSONArray> JSONArrayListener() {
        Response.Listener listener = new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                Log.d(this.getClass().getName(), response.toString());
            }
        };
        return listener;
    }

    public static String getHash(Activity activity, String appId) {
        String keyHash = "";
        try {
            PackageInfo info = activity.getPackageManager().getPackageInfo(appId, PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                keyHash = Base64.encodeToString(md.digest(), Base64.DEFAULT);
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        Log.d("keyHash:", keyHash);
        return keyHash;
    }

    public static String getUrlHash(Activity activity, String appId) {
        String urlKeyHash = "";
        String keyHash = GasApi.getHash(activity, appId);
        try {
            urlKeyHash = URLEncoder.encode(keyHash.trim(), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        Log.d("urlKeyHash:", urlKeyHash);
        return urlKeyHash;
    }

    public static File getFile(String fileName) {
        return getFile(fileName, false);
    }

    public static File getFile(String fileName, boolean delete) {
        File apiFile = new File(storagePath, fileName);
        if (!apiFile.exists()) {
            try {
                apiFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else if (delete && apiFile.exists()) {
            apiFile.delete();
            try {
                apiFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return apiFile;
    }

    public static String setFileContent(String fileName, String content) {
        getFile(fileName, true);
        FileOutputStream outputStream;
        try {
            outputStream = GasApp.getAppContext().openFileOutput(fileName, Context.MODE_PRIVATE);
            outputStream.write(content.getBytes());
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return content;
    }

    public static JSONObject setJSONObjectFileContent(String fileName, JSONObject obj) {
        try {
            String content = setFileContent(fileName, obj.toString());
            obj = new JSONObject(content);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return obj;
    }

    public static JSONArray setJSONArrayFileContent(String fileName, JSONArray arr) {
        try {
            String content = setFileContent(fileName, arr.toString());
            arr = new JSONArray(content);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return arr;
    }

    public static String getFileContent(String fileName) {
        String content = "";
        byte[] dataInBytes = new byte[0];
        File vehicleFile = getFile(fileName);
        try {
            DataInputStream dis =
                    new DataInputStream (
                            new FileInputStream(vehicleFile));

            dataInBytes = new byte[dis.available()];
            dis.readFully(dataInBytes);
            dis.close();

            content = new String(dataInBytes, 0, dataInBytes.length);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return content;
    }

    public static JSONObject getJSONObjectFileContent(String fileName) {
        JSONObject obj = new JSONObject();
        String content = GasApi.getFileContent(fileName);
        try {
            obj = new JSONObject(content);
        } catch (Throwable t) {
            Log.e("getJSONObjectFileContent", "Could not parse JSON");
        }
        return obj;
    }

    public static JSONArray getJSONArrayFileContent(String fileName) {
        JSONArray arr = new JSONArray();
        String content = GasApi.getFileContent(fileName);
        try {
            arr = new JSONArray(content);
        } catch (Throwable t) {
            Log.e("getJSONArrayFileContent", "Could not parse JSON");
        }
        return arr;
    }
}
