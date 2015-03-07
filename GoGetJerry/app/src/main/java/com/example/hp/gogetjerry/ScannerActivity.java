package com.example.hp.gogetjerry;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;


public class ScannerActivity extends Activity {

    static final String ACTION_SCAN = "com.google.zxing.client.android.SCAN";
    static final String url = "http://167.205.32.46/pbd/api/catch";
    String secret_token;
    TextView isCaught;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scanner);
        isCaught = (TextView) findViewById(R.id.isCaught);
        scanQR();
    }

    //product qr code mode
    public void scanQR(){
        try{
            //start scanning activity
            Intent intent = new Intent(ACTION_SCAN);
            intent.putExtra("SCAN_MODE", "PRODUCT_MODE");
            startActivityForResult(intent, 0);
        } catch (ActivityNotFoundException anfe){
            showDialog(ScannerActivity.this, "No Scanner Found", "Download!", "Yes", "No").show();
        }
    }

    //alert dialog for downloadDialog
    private static AlertDialog showDialog(final Activity act, CharSequence title, CharSequence message, CharSequence buttonYes, CharSequence buttonNo){
        AlertDialog.Builder downloadDialog = new AlertDialog.Builder(act);
        downloadDialog.setTitle(title);
        downloadDialog.setMessage(message);
        downloadDialog.setPositiveButton(buttonYes, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Uri uri = Uri.parse("market://search?q=pname:" + "com.google.zxing.client.android");
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                try{
                    act.startActivity(intent);
                } catch (ActivityNotFoundException anfe){

                }
            }
        });
        downloadDialog.setNegativeButton(buttonNo, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });
        return downloadDialog.show();
    }

    public void onActivityResult(int requestCode, int resultCode, Intent intent){
        if (requestCode == 0){
            if (resultCode == RESULT_OK){
                //get extras returned from intent
                secret_token = intent.getStringExtra("SCAN_RESULT");

                String format = intent.getStringExtra("SCAN_RESULT_FORMAT");
                Toast toast = Toast.makeText(this, "Content:" + secret_token + " Format:" + format, Toast.LENGTH_LONG);
                toast.show();
                new PostAPICatch().execute();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_scanner, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    class PostAPICatch extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... params) {
            String response = "";
            DefaultHttpClient client = new DefaultHttpClient();
            HttpResponse httpResponse = null;
            HttpPost httpPost = new HttpPost(url);
            try{
                List<BasicNameValuePair> Params = new ArrayList();
                Params.add(new BasicNameValuePair("nim", "13512080"));
                Params.add(new BasicNameValuePair("token", secret_token));
                httpPost.setEntity(new UrlEncodedFormEntity(Params));
                Log.d("Cek post parameter", httpPost.getParams().toString());
                httpResponse = client.execute(httpPost);
                InputStream content = httpResponse.getEntity().getContent();
                BufferedReader br = new BufferedReader(new InputStreamReader(content));
                String s = "";
                while((s = br.readLine()) != null){
                    response += s;
                }
            } catch (ClientProtocolException e){
                e.printStackTrace();
            } catch (IOException e){
                e.printStackTrace();
            }

            Log.d("Response ", response);
            return response;
        }

        @Override
        protected void onPostExecute(String s){
            super.onPostExecute(s);
            JSONObject jsonResponse;
            String responseStatus;

            String toBeChecked = "Success: 13512080 -> ";
            toBeChecked += secret_token;
            try {
                jsonResponse = new JSONObject(s);
                responseStatus = jsonResponse.getString("message");
                if (responseStatus.equals(toBeChecked)){
                    isCaught.setText("Jerry is caught! Well done!");
                } else {
                    isCaught.setText("Whoops, Jerry is not here. Go find Jerry!");
                }
                Log.d("Status response", responseStatus);
                Log.d("Check", toBeChecked);
            } catch (JSONException e){
                e.printStackTrace();
            }
        }
    }
}
