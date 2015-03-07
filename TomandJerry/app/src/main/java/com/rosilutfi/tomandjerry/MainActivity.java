package com.rosilutfi.tomandjerry;

/**
 * Created by Rosi on 07/03/2015.
 */
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.LatLng;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;

public class MainActivity extends Activity {


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //set the main content layout of the Activity
        setContentView(R.layout.activity_main);
    }

    //product qr code mode
    public void scanQR(View v) {
        try {
            Intent intent = new Intent(this,QRCodeActivity.class);
            startActivity(intent);
            //start the scanning activity from the com.google.zxing.client.android.SCAN intent
            //Intent intent = new Intent(ACTION_SCAN);
            //intent.putExtra("SCAN_MODE", "QR_CODE_MODE");
            //startActivityForResult(intent,0);
        } catch (ActivityNotFoundException anfe) {
            //on catch, show the download dialog
            Toast.makeText(getApplicationContext(), "Activity not found", Toast.LENGTH_LONG).show();
        }
    }

    public void compass(View v){
        try {
            Intent intent = new Intent(this,CompassActivity.class);
            startActivity(intent);
        } catch (ActivityNotFoundException anfe) {
            Toast.makeText(getApplicationContext(),"Activity not found",Toast.LENGTH_LONG).show();
        }

    }

    public void map(View v){
        try {
            if (isNetworkAvailable()){
                Intent intent = new Intent(this,MapActivity.class);
                startActivity(intent);
            } else {
                Toast.makeText(getApplicationContext(),"Please check your internet connection",Toast.LENGTH_LONG).show();
            }
        } catch (ActivityNotFoundException anfe){
            Toast.makeText(getApplicationContext(),"Activity not found",Toast.LENGTH_LONG).show();
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

    //on ActivityResult method
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (requestCode == 0) {
            if (resultCode == RESULT_OK) {
                //get the extras that are returned from the intent
                String contents = intent.getStringExtra("SCAN_RESULT");
                String format = intent.getStringExtra("SCAN_RESULT_FORMAT");
                OKDialog(this,"Result",contents).show();
            }
        }
    }

    public boolean isNetworkAvailable() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo ni = cm.getActiveNetworkInfo();
        if (ni == null) {
            // There are no active networks.
            return false;
        } else
            return true;
    }



}
