package com.example.rikysamuel.tomjerry;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

public class qrCode extends Activity {

    static final String ACTION_SCAN = "com.google.zxing.client.android.SCAN";

    private String contents;
    private String response;
    private Boolean lock;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //set the main content layout of the Activity
        setContentView(R.layout.activity_qr_code);
        scanQR();
    }

    //product qr code mode
    public void scanQR() {
        try {
            //start the scanning activity from the com.google.zxing.client.android.SCAN intent
            Intent intent = new Intent(ACTION_SCAN);
            intent.putExtra("SCAN_MODE", "QR_CODE_MODE");
            startActivityForResult(intent, 0);
        } catch (ActivityNotFoundException anfe) {
            //on catch, show the download dialog
            showDialog(qrCode.this, "No Scanner Found", "Download a scanner code activity?", "Yes", "No").show();
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
        if (requestCode == 0 && resultCode == RESULT_OK) {
            //get the extras that are returned from the intent
            contents = intent.getStringExtra("SCAN_RESULT");
            lock = true;
            new PostTask().execute(getApplicationContext());
            Toast.makeText(this, " Sending Content: " + contents + " to the server.", Toast.LENGTH_LONG).show();
            while(lock){
            }
            TextView t = (TextView) findViewById(R.id.hello);
            t.setText(response);
        }
        finish();
    }

    @Override
    public void finish(){
        Intent i = new Intent();
        i.putExtra("response", response);
        setResult(RESULT_OK,i);
        super.finish();
    }

    public class PostTask extends AsyncTask<Context, String, String> {

        private Context context;

        @Override
        protected void onPreExecute() {
            // TODO Auto-generated method stub
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(Context... params) {
            context = params[0];

            HTTPClass htc = new HTTPClass();
            htc.setUrl("http://167.205.32.46/pbd/api/catch");
            htc.setContents(contents);
            response = htc.doPost();
            lock = false;

            return "Success";
        }
    }
}