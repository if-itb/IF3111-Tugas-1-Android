package com.kevhnmay94.assignments.tomandjerry; /**
 * Created by Asus on 06/03/2015.
 */

import android.content.Context;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.android.volley.Request;

public class RequestSingleton {
    private static RequestSingleton instance;
    private RequestQueue request;
    private static Context mCtx;
    private RequestSingleton(Context context){
        mCtx = context;
        request = getRequestQueue();
    }

    public static synchronized RequestSingleton getInstance(Context context) {
        if (instance == null) {
            instance = new RequestSingleton(context);
        }
        return instance;
    }

    public RequestQueue getRequestQueue() {
        if (request == null) {
            // getApplicationContext() is key, it keeps you from leaking the
            // Activity or BroadcastReceiver if someone passes one in.
            request = Volley.newRequestQueue(mCtx.getApplicationContext());
        }
        return request;
    }

    public <T> void addToRequestQueue(Request<T> req) {
            getRequestQueue().add(req);
    }

}
