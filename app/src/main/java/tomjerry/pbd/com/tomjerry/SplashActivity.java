package tomjerry.pbd.com.tomjerry;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;


public class SplashActivity extends ActionBarActivity {

    private double latitude;
    private double longitude;
    private long valid_until;
    private boolean lock;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_splash);
        lock = true;

        // Mengeksekusi AsyncTask untuk mengambil koordinat dan batas waktu Jerry
        new Task().execute(getApplicationContext());

        // Menjalankan thread untuk menunggu AsyncTask selesai melakukan request
        Thread welcomeThread = new Thread() {
            @Override
            public void run() {
            try {
                super.run();
                while(lock) {
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                Intent i = new Intent(SplashActivity.this,
                        MainActivity.class);
                i.putExtra("latitude",latitude);
                i.putExtra("longitude",longitude);
                i.putExtra("valid_until",valid_until);
                startActivity(i);
                finish();
            }
            }
        };
        welcomeThread.start();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_splash, menu);
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

    public class Task extends AsyncTask<Context, Void, Void> {
        @Override
        protected Void doInBackground(Context... params) {
            Context context = params[0];
            System.out.println(context.toString());

            HttpClient client = new DefaultHttpClient();
            HttpGet request = new HttpGet("http://167.205.32.46/pbd/api/track?nim=13512025");
            HttpResponse response;
            String result = "";
            try {
                // Mengeksekusi Http Get
                response = client.execute(request);

                // Mengambil isi dari response Http Get
                BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));

                String line;
                while ((line = rd.readLine()) != null) {
                    result += line;
                }
            } catch (Exception e) {
                // Jika hasil gagal didapat, set semua menjadi 0
                e.printStackTrace();
                latitude = 0;
                longitude = 0;
                valid_until = 0;
            }
            if(!result.equalsIgnoreCase("")) {
                try {
                    JSONObject object = new JSONObject(result);
                    latitude =  object.getDouble("lat");
                    longitude = object.getDouble("long");
                    valid_until = object.getLong("valid_until");
                } catch(Exception e) {
                    e.printStackTrace();
                }
            }
            lock = false;
            return null;
        }
    }
}
