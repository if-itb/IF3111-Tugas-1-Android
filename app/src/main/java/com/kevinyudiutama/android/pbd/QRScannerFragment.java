package com.kevinyudiutama.android.pbd;


import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Toast;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONObject;


/**
 * A simple {@link Fragment} subclass.
 */
public class QRScannerFragment extends Fragment {

    static final String ACTION_SCAN = "com.google.zxing.client.android.SCAN";
    private ImageButton mQrScanButton;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v =  inflater.inflate(R.layout.fragment_qrscanner, container, false);

        mQrScanButton = (ImageButton) v.findViewById(R.id.scan_button);
        mQrScanButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                scanQR(v);
            }
        });
        return v;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    //product qr code mode
    public void scanQR(View v) {
        try {
            Intent intent = new Intent(ACTION_SCAN);
            intent.putExtra("SCAN_MODE", "QR_CODE_MODE");
            startActivityForResult(intent, 0);
        } catch (ActivityNotFoundException anfe) {
            showDialog(getActivity(), "No Scanner Found",
                    "Download a scanner code activity?", "Yes", "No")
                    .show();
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
            if (resultCode == Activity.RESULT_OK) {
                String contents = intent.getStringExtra("SCAN_RESULT");
                String format = intent.getStringExtra("SCAN_RESULT_FORMAT");
                new PostTokenTask().execute(contents);
            }
        }
    }

    private class PostTokenTask extends AsyncTask<String,Void,String> {
        @Override
        protected String doInBackground(String... params) {
            try {
                HttpClient httpClient = new DefaultHttpClient();

                HttpPost httpPost = new HttpPost("http://167.205.32.46/pbd/api/catch");

                JSONObject httpBodyObj = new JSONObject();
                httpBodyObj.put("nim","13512010");
                httpBodyObj.put("token",params[0]);

                httpPost.setEntity(new StringEntity(httpBodyObj.toString()));
                httpPost.setHeader("Content-type","application/json");

                HttpResponse response = httpClient.execute(httpPost);

                if (response.getStatusLine().getStatusCode()==200) {
                    return "Status Code : 200, Message succesfully send";
                }
                else {
                    return "Status Code : "+response.getStatusLine().getStatusCode()+ ", Your token is invalid";
                }
            }
            catch (Exception e) {
                return "There is an error when sending the token";
            }
        }

        @Override
        protected void onPostExecute(String message) {
            Toast.makeText(getActivity(),message,Toast.LENGTH_SHORT).show();
        }
    }

}
