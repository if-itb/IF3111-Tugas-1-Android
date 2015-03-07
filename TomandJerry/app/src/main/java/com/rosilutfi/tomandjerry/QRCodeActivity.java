package com.rosilutfi.tomandjerry;

/**
 * Created by Rosi on 07/03/2015.
 */
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.LatLng;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;


public class QRCodeActivity extends Activity {
    final String SERVER = "http://167.205.32.46/pbd";
    static final String ACTION_SCAN = "com.google.zxing.client.android.SCAN";
    private TextView codeResult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qrcode);
        codeResult = (TextView) findViewById(R.id.codeResult);
    }

    public void scanQR(View v) {
        try {
            //start the scanning activity from the com.google.zxing.client.android.SCAN intent
            Intent intent = new Intent(ACTION_SCAN);
            intent.putExtra("SCAN_MODE", "QR_CODE_MODE");
            startActivityForResult(intent,0);
        } catch (ActivityNotFoundException anfe) {
            //on catch, show the download dialog
            Toast.makeText(getApplicationContext(), "Activity not found", Toast.LENGTH_LONG).show();
        }
    }

    public void sendCodeToServer(View v){
        String text = codeResult.getText().toString();
        if (codeResult.getText().equals("")){
            Toast.makeText(getApplicationContext(),"Please do a qr scan first",Toast.LENGTH_SHORT).show();
        } else {
            try {
                JSONObject data = new JSONObject();
                data.put("nim", "13512014"); data.put("token",text);
                new SendCodeToServerTask().execute(data.toString());
            } catch (JSONException je){
                je.printStackTrace();
            }

        }
    }

    //on ActivityResult method
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (requestCode == 0) {
            if (resultCode == RESULT_OK) {
                //get the extras that are returned from the intent
                String contents = intent.getStringExtra("SCAN_RESULT");
                String format = intent.getStringExtra("SCAN_RESULT_FORMAT");
                //OKDialog(this,"Result",contents).show();
                codeResult.setText(contents);
            }
        }
    }

    private static AlertDialog OKDialog(final Activity act, CharSequence title, final CharSequence message) {
        AlertDialog.Builder okDialog = new AlertDialog.Builder(act);
        okDialog.setTitle(title);
        okDialog.setMessage("Send this token to server?" + message);
        okDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //new MainActivity.SendCodeToServerTask().execute(message.toString());
            }
        });
        okDialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //do nothing
            }
        });
        return okDialog.show();
    }

    private class SendCodeToServerTask extends AsyncTask<String,Void,String> {
        @Override
        protected String doInBackground(String... params) {
            String url = SERVER + "/api/catch";
            Log.d("test","sending "+params[0]+" to server");
            int responseCode = 0;
            try {
                URL obj = new URL(url);
                HttpURLConnection con = (HttpURLConnection) obj.openConnection();
                con.setRequestMethod("POST");
                con.setRequestProperty("Content-type", "application/json");

                OutputStreamWriter writer = new OutputStreamWriter(con.getOutputStream());
                writer.write(params[0]);
                writer.close();

                con.connect();
                responseCode = con.getResponseCode();
                Log.d("response code",Integer.toString(responseCode));
                InputStream is = con.getInputStream();
                BufferedReader r = new BufferedReader(new InputStreamReader(is,"UTF8"));
                StringBuffer sb = new StringBuffer();
                int chr;
                while ((chr = r.read()) != -1){
                    sb.append(((char) chr));
                }
                return sb.toString();
            } catch (MalformedURLException mue) {
                return null;
            } catch (IOException ioe) {
                return null;
            }
        }

        @Override
        protected void onPostExecute(String result) {
            try{
                JSONObject jo = new JSONObject(result);
                String message;
                if (jo.has("message")){
                    message = (String) jo.get("message");
                } else {
                    message = result;
                }
                Toast.makeText(getApplicationContext(),message,Toast.LENGTH_LONG).show();
            } catch (JSONException je){
                Toast.makeText(getApplicationContext(),je.getMessage(),Toast.LENGTH_LONG).show();
            }
        }
    }
}

