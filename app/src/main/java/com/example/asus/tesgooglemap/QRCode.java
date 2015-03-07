package com.example.asus.tesgooglemap;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

/**
 * Created by asus on 3/7/2015.
 */
public class QRCode extends Activity {
    static final String ACTION_SCAN = "com.google.zxing.client.android.SCAN";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //set the main content layout of the Activity
        setContentView(R.layout.qr_code);
    }

    //product qr code mode
    public void scanQR(View v) {
        try {
            //start the scanning activity from the com.google.zxing.client.android.SCAN intent
            Intent intent = new Intent(ACTION_SCAN);
            intent.putExtra("SCAN_MODE", "QR_CODE_MODE");
            startActivityForResult(intent, 0);
        } catch (ActivityNotFoundException anfe) {
            //on catch, show the download dialog
            showDialog(QRCode.this, "No Scanner Found", "Download a scanner code activity?", "Yes", "No").show();
        }
    }

    //alert dialog for downloadDialog
    private static AlertDialog showDialog(final Activity act, CharSequence title, CharSequence message, CharSequence buttonYes, CharSequence buttonNo) {
        AlertDialog.Builder downloadDialog = new AlertDialog.Builder(act);
        downloadDialog.setTitle(title);
        downloadDialog.setMessage(message);
        downloadDialog.setPositiveButton(buttonYes, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialogInterface, int i) {
                Uri uri = Uri.parse("market://search?q=pname:" + "com.google.zxing.client.android");
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                try {
                    act.startActivity(intent);
                } catch (ActivityNotFoundException anfe) {

                }
            }
        });
        downloadDialog.setNegativeButton(buttonNo, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialogInterface, int i) {
            }
        });
        return downloadDialog.show();
    }

    //on ActivityResult method
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (requestCode == 0) {
            if (resultCode == RESULT_OK) {
                //get the extras that are returned from the intent
                String contents = intent.getStringExtra("SCAN_RESULT");
                String format = intent.getStringExtra("SCAN_RESULT_FORMAT");
//                Toast toast = Toast.makeText(this, "Content:" + contents + " Format:" + format, Toast.LENGTH_LONG);
//                toast.show();

                // 1. create HttpClient
                HttpClient httpclient = new DefaultHttpClient();

                // 2. make POST request to the given URL
                HttpPost httpPost = new HttpPost("http://167.205.32.46/pbd/api/catch");

                String json = "";

                // 3. build jsonObject
                JSONObject jsonObject = new JSONObject();
                try {
                    jsonObject.put("nim", "13512051");
                    jsonObject.put("token", contents);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                // 4. convert JSONObject to JSON to String
                json = jsonObject.toString();

                // ** Alternative way to convert Person object to JSON string usin Jackson Lib
                // ObjectMapper mapper = new ObjectMapper();
                // json = mapper.writeValueAsString(person);

                // 5. set json to StringEntity
                StringEntity se = null;

                try {
                    se = new StringEntity(json);
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }

                // 6. set httpPost Entity
                httpPost.setEntity(se);

                // 7. Set some headers to inform server about the type of the content
                httpPost.setHeader("Content-type", "application/json");

                // 8. Execute POST request to the given URL
                HttpResponse httpResponse = null;
                try {
                    httpResponse = httpclient.execute(httpPost);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                receiveHttpResponse(httpResponse);
            }

        }
    }

    public void receiveHttpResponse(HttpResponse hr) {
        if(hr.getStatusLine().getStatusCode() == 200) {
            Log.d("1", "OK");
            Toast toast = Toast.makeText(this, "Great! Jerry was caught", Toast.LENGTH_LONG);
            toast.show();
        } else if(hr.getStatusLine().getStatusCode() == 400) {
            Log.d("2", "MISSING PARAMETER");
            Toast toast = Toast.makeText(this, "Oops! Your tools not complete", Toast.LENGTH_LONG);
            toast.show();
        } else {
            Log.d("3", "FORBIDDEN");
            Toast toast = Toast.makeText(this, "Wait, your tools are wrong", Toast.LENGTH_LONG);
            toast.show();
        }
        finish();
    }
}
