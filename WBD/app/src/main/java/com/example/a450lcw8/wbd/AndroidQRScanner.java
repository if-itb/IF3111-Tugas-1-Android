package com.example.a450lcw8.wbd;

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

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.BufferedHttpEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

/**
 * Created by A450LC W8 on 06/03/2015.
 */
public class AndroidQRScanner extends Activity {
    public static final String ACTION_SCAN = "com.google.zxing.client.android.SCAN";

    // constant to determine which sub-activity result
    private static final int REQUEST_CODE = 10;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //set the main content layout of the Activity
        setContentView(R.layout.activity_scanner);
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
            showDialog(AndroidQRScanner.this, "No Scanner Found", "Download a scanner code activity?", "Yes", "No").show();
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
                Toast toast = Toast.makeText(this, "Content:" + contents + " Format:" + format, Toast.LENGTH_LONG);
                toast.show();

                // create HttpClient
                HttpClient httpclient = new DefaultHttpClient();

                // make POST request to the given URL
                HttpPost httpPost = new HttpPost("http://167.205.32.46/pbd/api/catch");

                JSONObject jsonobj; // declared locally so that it destroys after serving its purpose
                jsonobj = new JSONObject();
                try {
                    // adding some keys
                    jsonobj.put("nim", "13512049");
                    jsonobj.put("token", contents);

                } catch (JSONException ex) {
                    ex.printStackTrace();
                }

                String json = "";

                //  convert JSONObject to JSON to String
                json = jsonobj.toString();

                StringEntity se = null;

                //  set json to StringEntity
                try {
                    se = new StringEntity(json);
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }

                //  set httpPost Entity
                httpPost.setEntity(se);
                httpPost.setHeader("Content-type", "application/json");

                //  Execute POST request to the given URL
                HttpResponse httpResponse = null;
                try {
                    httpResponse = httpclient.execute(httpPost);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                sendResponseMessage(httpResponse);

            }
        }
    }
    void sendResponseMessage(HttpResponse response){
        StatusLine status=response.getStatusLine();
        if (status.getStatusCode() == 200) {
            Log.d("1", "OK");
            Intent intent = new Intent(this, ResponseStatus.class);
            intent.putExtra("responOk", "Respon OK,\n" +
                    "penangkapan dilakukan dengan benar!");
            startActivity(intent);

        }
        else if(status.getStatusCode() == 400){
            Log.d("2", "MISSING PARAMETER");
            Intent intent = new Intent(this, ResponseStatus.class);
            intent.putExtra("responOk", "MISSING PARAMETER\n" +
                    "Ada kesalahan pada format penangkapan!");
            startActivity(intent);
        }else{
            try {
                HttpEntity entity=null;
                HttpEntity temp=response.getEntity();
                if (temp != null) {
                    entity=new BufferedHttpEntity(temp);
                }
                Log.d("3", "FORBIDDEN");
                Intent intent = new Intent(this, ResponseStatus.class);
                intent.putExtra("responOk", "FORBIDDEN\n" +
                        "Token yang ditangkap salah!");
                startActivity(intent);
            }
            catch (    IOException e) {
                Log.d("0", "Could not download data");
            }
        }
    }

}
