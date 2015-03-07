package com.example.rikysamuel.tomjerry;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.Menu;
import android.view.MenuItem;

import org.json.JSONException;
import org.json.JSONObject;
import android.app.Activity;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;


public class getLatLng extends Activity {
    // JSON object Node Names
    private static final String LATITUDE = "lat";
    private static final String LONGITUDE = "long";
    private static final String VALID_UNTIL = "valid_until";

    private boolean lock;
    private double latitude;
    private double longitude;
    private int valid_until;
    private HTTPClass httpget;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_activity_http);

        lock = true;
        httpget = new HTTPClass();
        new Task().execute(getApplicationContext());
        while(lock){
        }
        finish();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
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

    @Override
    public void finish(){
        Intent i = new Intent();
        i.putExtra("long",longitude);
        i.putExtra("lat", latitude);
        i.putExtra("val", valid_until);
        setResult(RESULT_OK,i);
        super.finish();
    }

    public class Task extends AsyncTask<Context, String, String> {

        private Context context;

        public void parse(String result) {
            try {
                JSONObject json = new JSONObject(result);
                latitude = json.getDouble(LATITUDE);
                longitude = json.getDouble(LONGITUDE);
                valid_until = json.getInt(VALID_UNTIL);

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        @Override
        protected void onPreExecute() {
            // TODO Auto-generated method stub
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(Context... params) {
            context = params[0];
            httpget.setUrl("http://167.205.32.46/pbd/api/track?nim=13512089");
            String result = httpget.doGet();
            parse(result);
            lock = false;

            return result;
        }
    }
}
