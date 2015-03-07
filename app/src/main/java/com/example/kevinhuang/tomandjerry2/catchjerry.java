package com.example.kevinhuang.tomandjerry2;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
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
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;


public class catchjerry extends ActionBarActivity {

    static final String ACTION_SCAN = "com.google.zxing.client.android.SCAN";
    private EditText edtnim;
    private EditText edttoken;
    private TextView txtmessage;
    private TextView txtcode;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_catchjerry);
        txtmessage = (TextView)findViewById(R.id.txtmessage);
        txtcode = (TextView)findViewById(R.id.txtcode);
        final Button btnscanqr = (Button)findViewById(R.id.btnscan);
        btnscanqr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                scanQR(view);
            }
        });
        final Button btnverify = (Button)findViewById(R.id.btnverify);
        btnverify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new postCode().execute();
            }
        });
        edtnim = (EditText)findViewById(R.id.edtnim);
        edttoken = (EditText)findViewById(R.id.edttoken);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_catchjerry, menu);
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
            //product qr code mode
    public void scanQR(View v) {
        try {
            //start the scanning activity from the com.google.zxing.client.android.SCAN intent
            Intent intent = new Intent(ACTION_SCAN);
            intent.putExtra("SCAN_MODE", "QR_CODE_MODE");
            startActivityForResult(intent, 0);
        } catch (ActivityNotFoundException anfe) {
            //on catch, show the download dialog
            showDialog(catchjerry.this, "No Scanner Found", "Download a scanner code activity?", "Yes", "No").show();
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
                edttoken.setText(contents);

            }
        }
    }
    public void postData() {
        // Create a new HttpClient and Post Header
        if(edttoken.getText().toString().equalsIgnoreCase("token") && edtnim.getText().toString().equalsIgnoreCase("nim")){
            Context context = getApplicationContext();
            CharSequence text = "All fields must not empty !";
            int duration = Toast.LENGTH_SHORT;
            Toast toast = Toast.makeText(context, text, duration);
            toast.show();
            txtmessage.setText("Field emtpy");
        }
        else {
            HttpClient httpClient = new DefaultHttpClient(); //Use this instead
            String result = "";
            txtmessage.setText(edtnim.getText().toString());
            try {
                    HttpPost request = new HttpPost("http://167.205.32.46/pbd/api/catch");
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("nim","13512096");
                    jsonObject.put("token",edttoken.getText().toString());
                    StringEntity param = new StringEntity(jsonObject.toString());
                    request.addHeader("Content-type", "application/json");
                    request.setEntity(param);
                    Log.d("debug", jsonObject.toString());
                    HttpResponse response = httpClient.execute(request);
                    BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
                    String line = "";
                    while ((line = rd.readLine()) != null) {
                            result += line;
                    }
                    txtmessage.setText(result);
                }
            catch (Exception ex) {

            } finally {
                httpClient.getConnectionManager().shutdown();
            }
        }
    }
    private class postCode extends AsyncTask<Void, Void, Void> {
        public ProgressDialog pDialog;
        String response = null;
        private String url = "http://167.205.32.46/pbd/api/catch";
        @Override
        protected void onPreExecute(){
            super.onPreExecute();
        }
        @Override
        protected Void doInBackground(Void... params) {
            List<NameValuePair> list = new ArrayList<NameValuePair>();
            list.add(new BasicNameValuePair("nim",edtnim.getText().toString()));
            list.add(new BasicNameValuePair("token",edttoken.getText().toString()));
            DefaultHttpClient httpClient = new DefaultHttpClient();
            HttpEntity httpentity = null;
            HttpResponse httpresponse = null;
            HttpPost httpost = new HttpPost(url);
            String json = "";
            JSONObject jsonobject = new JSONObject();
            for(int i=0;i<list.size();i++){
                try {
                    jsonobject.accumulate(list.get(i).getName(),list.get(i).getValue());
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
            json = jsonobject.toString();
            StringEntity se = null;
            try {
                se = new StringEntity(json);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            httpost.setEntity(se);
            try {
                httpresponse = httpClient.execute(httpost);
            } catch (IOException e) {
                e.printStackTrace();
            }
            httpentity = httpresponse.getEntity();
            try {
                response = EntityUtils.toString(httpentity);
                response = response.substring(3,response.length());
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                String hasil = new String(response.getBytes("ISO-8859-1"),"utf-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            return null;
        }
        @Override
        protected void onPostExecute(Void result){
            Toast.makeText(getBaseContext(),"Catching Jerry ...",Toast.LENGTH_LONG).show();
            //txtmessage.setText(response);
            JSONObject reader = null;
            try {
                reader = new JSONObject(response);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            try {
                txtmessage.setText("Message : " + reader.getString("message"));
            } catch (JSONException e) {
                e.printStackTrace();
            }
            try {
                txtcode.setText("Code :" + reader.getString("code"));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}
