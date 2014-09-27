package tools.tiger.gascap.app;

import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;


/**
 * Created by jgreathouse on 9/24/2014.
 */
public class JsonGasRequest extends Request {

    /** Charset for request. */
    private static final String PROTOCOL_CHARSET = "utf-8";
    /** Content type for request. */
    private static final String PROTOCOL_CONTENT_TYPE =
            String.format("application/json; charset=%s", PROTOCOL_CHARSET);

    private Listener listener;
    private Map<String, String> params;
    private String mRequestBody;
    private String apiToken;

    public JsonGasRequest(String url, String apiToken, Map<String, String> params,
                         Listener reponseListener, ErrorListener errorListener) {
        super(Method.GET, url, errorListener);
        this.listener = reponseListener;
        this.params = params;
        this.apiToken = apiToken;
    }

    public JsonGasRequest(int method, String url, String apiToken, Map<String, String> params,
                         Listener reponseListener, ErrorListener errorListener) {
        super(method, url, errorListener);
        this.listener = reponseListener;
        this.params = params;
        this.apiToken = apiToken;
    }

    protected Map<String, String> getParams()
            throws com.android.volley.AuthFailureError {
        return params;
    };

    @Override
    public String getBodyContentType() {
        return PROTOCOL_CONTENT_TYPE;
    }

    @Override
    public byte[] getBody() {
        try {
            mRequestBody = requestParamsToJSON().toString();
            VolleyLog.d(mRequestBody.toString());
            return mRequestBody.getBytes(PROTOCOL_CHARSET);
        } catch (UnsupportedEncodingException uee) {
            VolleyLog.wtf("Unsupported Encoding while trying to get the bytes of %s using %s",
                    mRequestBody, PROTOCOL_CHARSET);
            return null;
        }
    }

    @Override
    public Map<String, String> getHeaders() throws AuthFailureError {
        Map<String, String> headers = new HashMap<String, String>();
        if (apiToken != null) {
            headers.put("Authorization", "Token " + apiToken);
        }
        headers.put("Content-Type", getBodyContentType());
        VolleyLog.d(headers.toString());
        return headers;
    }

    @Override
    protected Response parseNetworkResponse(NetworkResponse response) {
        try {
            String jsonString = new String(response.data,
                    HttpHeaderParser.parseCharset(response.headers));
            try {
                return Response.success(new JSONObject(jsonString),
                        HttpHeaderParser.parseCacheHeaders(response));
            } finally {
                try {
                    return Response.success(new JSONArray(jsonString),
                            HttpHeaderParser.parseCacheHeaders(response));
                } catch (JSONException e) {
                    Log.e("parseNetworkResponse", "Failed to parse as JSON: " + jsonString);
                }
            }

        } catch (UnsupportedEncodingException e) {
            return Response.error(new ParseError(e));
        } catch (JSONException je) {
            return Response.error(new ParseError(je));
        }
    }

    @Override
    protected void deliverResponse(Object response) {
        listener.onResponse(response);
    }

    private JSONObject requestParamsToJSON() {
        JSONObject jsonObj = new JSONObject();
        for (Map.Entry<String,String> entry : params.entrySet()) {
            String v = entry.getValue();
            try {
                jsonObj.put(entry.getKey(), v);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return jsonObj;
    }
}
