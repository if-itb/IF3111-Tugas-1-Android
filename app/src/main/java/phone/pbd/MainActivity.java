package phone.pbd;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;


public class MainActivity extends ActionBarActivity {
    private boolean lock;
    private String res = "";
    private Position position;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_main);
        lock = true;
        new HttpActivity().execute(getApplicationContext());
        Thread httpThread = new Thread(){
            @Override
            public void run() {
                try{
                    super.run();
                    while (lock) {

                    }
                } catch (Exception ex) {

                } finally {
                    System.out.println("res: " + res);
                    Intent i = new Intent(MainActivity.this, MapActivity.class);
                    i.putExtra("latitude", position.getLatitude());
                    i.putExtra("longitude", position.getLongitude());
                    startActivity(i);
                    finish();
//                    TextView tv = (TextView) findViewById(R.id.hello_world);
//                    tv.setText(res);
                }
            }
        };

        httpThread.start();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return false;
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

    public class HttpActivity extends AsyncTask<Context, Void, Void> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            ProgressDialog dialog = ProgressDialog.show(MainActivity.this, "Loading.. Wait..", "Retrieving data", true);
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
//            showDialog("Jerry found!");
        }

        @Override
        protected Void doInBackground(Context... params) {
            HttpGet httpGet = new HttpGet("http://167.205.32.46/pbd/api/track?nim=13512065");
            HttpClient client = new DefaultHttpClient();
            HttpResponse httpResponse;
            try {
                httpResponse = client.execute(httpGet);
                BufferedReader br = new BufferedReader(new InputStreamReader(httpResponse.getEntity().getContent()));
                String line = "";

                while ((line = br.readLine()) != null) {
                    res += line;
                }
                if (!res.equalsIgnoreCase("")) {
                    JSONObject object = new JSONObject(res);
                    double lat = object.getDouble("lat");
                    double longitude = object.getDouble("long");
                    long valUntil = object.getLong("valid_until");
                    position = new Position(lat, longitude, valUntil);
                }

            } catch (Exception ex) {
                ex.printStackTrace();
                res = ex.getMessage();
            }
            lock = false;
            return null;
        }
    }
}
