package com.kevinyudiutama.android.pbd;

import android.app.Activity;
import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.util.Log;

import com.kevinyudiutama.android.pbd.downloader.StringDownloader;

import org.json.JSONObject;

/**
 * Created by kevinyu on 3/1/15.
 */
public class DownloadLocationService extends IntentService {

    public static final String TAG = "DownloadLocationService";
    public static final String RESULT_RECEIVER_TAG = "com.kevinyudiutama.android.pbd.DownloadLocationService.receiver";
    public static final String RESULT_LONGITUDE = "com.kevinyudiutama.android.pbd.DownloadLocationService.longitude";
    public static final String RESULT_LATITUDE = "com.kevinyudiutama.android.pbd.DownloadLocationService.latitude";
    public static final String RESULT_VALID_UNTIL = "com.kevinyudiutama.android.pbd.DownloadLocationService.VALID_UNTIL";

    public DownloadLocationService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        StringDownloader locationDownloader  = new StringDownloader();
        ResultReceiver receiver = intent.getParcelableExtra(RESULT_RECEIVER_TAG);

        ConnectivityManager connMgr = (ConnectivityManager)
                this.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            try {
                Log.d("DEBUG","Sucess 0");
                locationDownloader.download("http://167.205.32.46/pbd/api/track?nim=13512010");
                String resultString = locationDownloader.getString();

                JSONObject resultObj = new JSONObject(resultString);
                double longitude = resultObj.getDouble("long");
                double latitude = resultObj.getDouble("lat");
                long validUntil = resultObj.getLong("valid_until");
                Log.d("DEBUG","Success1");

                Bundle resultBundle = new Bundle();
                resultBundle.putDouble(RESULT_LONGITUDE,longitude);
                resultBundle.putDouble(RESULT_LATITUDE,latitude);
                resultBundle.putLong(RESULT_VALID_UNTIL,validUntil);
                receiver.send(Activity.RESULT_OK,resultBundle);

                Log.d("DEBUG","DownloadLocationService:OnHandleIntent:Success");

            } catch (Exception e) {
                Log.d("DEBUG","DownloadLocationService:OnHandleIntent:Error fetching destination from server");
                e.printStackTrace();
            }
        } else {
            Log.d("DEBUG", "No network available");
        }

    }
}
