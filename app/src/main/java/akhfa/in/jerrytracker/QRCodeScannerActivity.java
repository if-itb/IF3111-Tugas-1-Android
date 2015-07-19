package akhfa.in.jerrytracker;

import android.app.Activity;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.zxing.Result;

import org.json.JSONException;
import org.json.JSONObject;

import me.dm7.barcodescanner.zxing.ZXingScannerView;


public class QRCodeScannerActivity extends Activity implements ZXingScannerView.ResultHandler {

    private ZXingScannerView mScannerView;
    private Laporan jerry;
    private static final String url= "http://167.205.32.46/pbd/api/catch";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mScannerView = new ZXingScannerView(this);
        setContentView(mScannerView);
    }

    @Override
    public void onResume() {
        super.onResume();
        mScannerView.setResultHandler(this); // Register ourselves as a handler for scan results.
        mScannerView.startCamera();          // Start camera on resume
    }

    @Override
    public void onPause() {
        super.onPause();
        mScannerView.stopCamera();           // Stop camera on pause
    }

    public void scanQR(View v)
    {
        setContentView(mScannerView);                // Set the scanner view as the content view
    }
    @Override
    public void handleResult(Result rawResult) {
        // Do something with the result here
        Log.v("TAG", rawResult.getText()); // Prints scan results
        Log.v("TAG", rawResult.getBarcodeFormat().toString()); // Prints the scan format (qrcode, pdf417 etc.)
        Toast toast = Toast.makeText(this, "Content:" + rawResult.getText() + " Format:" + rawResult.getBarcodeFormat().toString(), Toast.LENGTH_LONG);
        toast.show();
        mScannerView.stopCamera();
        jerry = new Laporan();
        jerry.setNim("13513601");
        jerry.setToken(rawResult.getText());

        new LaporanAsyncTask().execute();
        this.finish();
    }

    private class LaporanAsyncTask extends AsyncTask<String, Void, String> {
        String result = "";
        @Override
        protected String doInBackground(String... urls) {
            JSONObject laporan = new JSONObject();
            try {
                laporan.accumulate("nim", jerry.getNIM());
                laporan.accumulate("token", jerry.getToken());
                JSONParser jsonparser = new JSONParser();
                result = jsonparser.sendJsonToUrl(url, laporan);
                runOnUiThread(new Runnable() {
                    public void run() {
                        Toast.makeText(getApplicationContext(), result, Toast.LENGTH_SHORT).show();
                    }
                });
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return "";
        }
        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(String result) {
            Toast.makeText(getBaseContext(), "Data Sent!", Toast.LENGTH_LONG).show();
        }
    }
}
