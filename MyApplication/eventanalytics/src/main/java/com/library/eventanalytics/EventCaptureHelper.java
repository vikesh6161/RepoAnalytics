/**
 @author Vikesh <vikesh6161@gmail.com>
 */

package com.library.eventanalytics;

import android.content.Intent;
import android.content.BroadcastReceiver;
import android.content.IntentFilter;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import android.net.NetworkInfo;
import android.net.ConnectivityManager;
import android.content.SharedPreferences;
import com.android.volley.Request;
import java.io.UnsupportedEncodingException;
import com.android.volley.AuthFailureError;
import java.util.HashMap;
import java.util.Map;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.Response;
import android.util.Log;
import com.google.gson.GsonBuilder;
import com.android.volley.toolbox.Volley;
import android.content.Context;

import androidx.core.content.ContextCompat;

import com.android.volley.RequestQueue;

public class EventCaptureHelper
{
    private static EventCaptureHelper helperInstance;
    private static boolean isInitialized;
    private static String mKey;
    private static String mUrl;
    private static RequestQueue mRequestQueue;
    private static String TAG;
    private static StringBuffer offlinejson;
    private static Context mContext;
    
    public static synchronized EventCaptureHelper getInstance(final Context context) {
        if (!EventCaptureHelper.isInitialized) {
            return null;
        }
        EventCaptureHelper.mContext = context.getApplicationContext();
        if (EventCaptureHelper.helperInstance == null) {
            EventCaptureHelper.helperInstance = new EventCaptureHelper();
        }
        return EventCaptureHelper.helperInstance;
    }
    
    public static boolean init(final String key) {
        return EventCaptureHelper.mKey.equals(key) && (EventCaptureHelper.isInitialized = true);
    }
    
    public void sendEvent(final Object jsonObject) {
        if (EventCaptureHelper.mContext == null) {
            return;
        }
        this.registerNetworkReceiver(EventCaptureHelper.mContext);
        if (jsonObject == null) {
            return;
        }
        if (EventCaptureHelper.mRequestQueue == null) {
            EventCaptureHelper.mRequestQueue = Volley.newRequestQueue(EventCaptureHelper.mContext);
        }
        final String valString = new GsonBuilder().create().toJson(jsonObject);
        Log.d(EventCaptureHelper.TAG, "JSON : " + valString);
        Log.d(EventCaptureHelper.TAG, "url : " + EventCaptureHelper.mUrl);
        final StringRequest jsonObjectRequest = new StringRequest(1, EventCaptureHelper.mUrl, new Response.Listener<String>() {
            public void onResponse(final String response) {
                VolleyLog.d("Success response", new Object[] { "response.toString()" });
            }
        }, new Response.ErrorListener() {
            public void onErrorResponse(final VolleyError error) {
                if (EventCaptureHelper.mContext == null) {
                    return;
                }
                if (!EventCaptureHelper.this.isNetworkAvailable(EventCaptureHelper.mContext)) {
                    if (EventCaptureHelper.offlinejson == null && getOfflineJson() == null) {
                        EventCaptureHelper.offlinejson = new StringBuffer(valString);
                    }
                    else if (EventCaptureHelper.offlinejson == null) {
                        EventCaptureHelper.offlinejson = new StringBuffer(getOfflineJson());
                        EventCaptureHelper.offlinejson.append(',').append(valString);
                    }
                    else {
                        EventCaptureHelper.offlinejson.append(',').append(valString);
                    }
                }
                VolleyLog.d("error response", new Object[] { "response.toString()" });
                error.printStackTrace();
            }
        }) {
            protected Map<String, String> getParams() throws AuthFailureError {
                final HashMap<String, String> headers = new HashMap<String, String>();
                return headers;
            }
            
            public byte[] getBody() throws AuthFailureError {
                try {
                    return (byte[])((valString == null) ? null : valString.getBytes("utf-8"));
                }
                catch (UnsupportedEncodingException ex) {
                    VolleyLog.d(EventCaptureHelper.TAG, new Object[] { "Unsupported exception : " + ex.getMessage() });
                    return null;
                }
            }
            
            public String getBodyContentType() {
                return "application/json";
            }
        };
        EventCaptureHelper.mRequestQueue.add((Request)jsonObjectRequest);
    }
    
    private void saveOffline(final String str) {
        if (null == str) {
            return;
        }
        if (EventCaptureHelper.mContext == null) {
            return;
        }
        final SharedPreferences sharedPref = EventCaptureHelper.mContext.getSharedPreferences("pref", 0);
        final SharedPreferences.Editor editor = sharedPref.edit();
        Log.d("test saving offline", str);
        editor.putString("Offline Json", str);
        editor.commit();
    }
    
    private static void removeOfflineJson() {
        if (EventCaptureHelper.mContext == null) {
            return;
        }
        final SharedPreferences sharedPref = EventCaptureHelper.mContext.getSharedPreferences("pref", 0);
        final SharedPreferences.Editor editor = sharedPref.edit();
        editor.remove("Offline Json");
        editor.commit();
    }
    
    private static String getOfflineJson() {
        if (EventCaptureHelper.mContext == null) {
            return null;
        }
        final SharedPreferences sharedPref = EventCaptureHelper.mContext.getSharedPreferences("pref", 0);
        final String str = sharedPref.getString("Offline Json", (String)null);
        return str;
    }
    
    public void setUrl(final String url) {
        EventCaptureHelper.mUrl = url;
    }
    
    private boolean isNetworkAvailable(final Context context) {
        final int permissionCheck = ContextCompat.checkSelfPermission(context, "android.permission.ACCESS_NETWORK_STATE");
        if (permissionCheck != 0) {
            return true;
        }
        if (null == context) {
            return true;
        }
        final ConnectivityManager connectivityManager = (ConnectivityManager)context.getSystemService("connectivity");
        final NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
    
    public static JsonElement getJsonElement(final Object obj) {
        return new Gson().toJsonTree(obj);
    }
    
    private void registerNetworkReceiver(final Context context) {
        final IntentFilter filter = new IntentFilter();
        filter.addAction("SOME_ACTION");
        filter.addAction("SOME_OTHER_ACTION");
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
        final ConnectionChangeReceiver listener = new ConnectionChangeReceiver();
        EventCaptureHelper.mContext.registerReceiver((BroadcastReceiver)listener, intentFilter);
    }
    
    private static void sendOffline() {
        if (null == EventCaptureHelper.offlinejson && getOfflineJson() == null) {
            return;
        }
        if (EventCaptureHelper.offlinejson == null) {
            final String str = "[" + getOfflineJson() + "]";
            sendEvent(str);
        }
        else {
            final String str = "[" + (Object)EventCaptureHelper.offlinejson + "]";
            sendEvent(str);
        }
        EventCaptureHelper.offlinejson = null;
        removeOfflineJson();
    }
    
    private static void sendEvent(final String jsonObject) {
        if (EventCaptureHelper.mContext == null) {
            return;
        }
        if (jsonObject == null) {
            return;
        }
        if (EventCaptureHelper.mRequestQueue == null) {
            EventCaptureHelper.mRequestQueue = Volley.newRequestQueue(EventCaptureHelper.mContext);
        }
        Log.d(EventCaptureHelper.TAG, "JSON : " + jsonObject);
        final StringRequest jsonObjectRequest = new StringRequest(1, EventCaptureHelper.mUrl, new Response.Listener<String>() {
            public void onResponse(final String response) {
                VolleyLog.d("Success response", new Object[] { "response.toString()" });
            }
        }, new Response.ErrorListener() {
            public void onErrorResponse(final VolleyError error) {
                if (EventCaptureHelper.mContext == null) {
                    return;
                }
                VolleyLog.d("error response", new Object[] { "response.toString()" });
                error.printStackTrace();
            }
        }) {
            protected Map<String, String> getParams() throws AuthFailureError {
                final HashMap<String, String> headers = new HashMap<String, String>();
                return headers;
            }
            
            public byte[] getBody() throws AuthFailureError {
                try {
                    return (byte[])((jsonObject == null) ? null : jsonObject.getBytes("utf-8"));
                }
                catch (UnsupportedEncodingException ex) {
                    VolleyLog.d(EventCaptureHelper.TAG, new Object[] { "Unsupported exception : " + ex.getMessage() });
                    return null;
                }
            }
            
            public String getBodyContentType() {
                return "application/json";
            }
        };
        EventCaptureHelper.mRequestQueue.add((Request)jsonObjectRequest);
    }
    
    @Override
    protected void finalize() throws Throwable {
        this.saveOffline(EventCaptureHelper.offlinejson.toString());
        super.finalize();
    }
    
    static {
        EventCaptureHelper.mKey = "xyz";
        EventCaptureHelper.mUrl = "http://dataout.recosenselabs.com/webhooks";
        EventCaptureHelper.TAG = "EventCaptureHelper";
    }
    
    public static class ConnectionChangeReceiver extends BroadcastReceiver
    {
        public void onReceive(final Context context, final Intent intent) {
            final ConnectivityManager connectivityManager = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
            final NetworkInfo activeNetInfo = connectivityManager.getActiveNetworkInfo();
            if (activeNetInfo != null) {
                if (activeNetInfo.isConnected()) {
                    sendOffline();
                }
            }
        }
    }
}
