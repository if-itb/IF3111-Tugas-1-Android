package wxr.tugas1android;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class MainActivity extends ActionBarActivity {
    static final String ACTION_SCAN = "com.google.zxing.client.android.SCAN";


    public double LatVal = -6, LongVal = 100;
    public long theTime;
    boolean lock = true;
    public String time = "Undefined time";
    public String contents = "Undefined content";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button MapsB = (Button) findViewById(R.id.button);
        Button QRB = (Button) findViewById(R.id.button2);
        Button Compass = (Button) findViewById(R.id.button3);
        //lock = true;
        //new Task().execute(getApplicationContext());
        //while (lock){}

        MapsB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                /*
                JSONObject theData = getData("167.205.32.46/pbd/api/track?nim=13512039");
                try {
                    LatVal = theData.getDouble("lat");
                    LongVal = theData.getDouble("long");
                    theTime = theData.getLong("valid_until");
                    time = String.format("%02d:%02d:%02d", (TimeUnit.MILLISECONDS.toHours(theTime) + 7),
                            TimeUnit.MILLISECONDS.toMinutes(theTime) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(theTime)),
                            TimeUnit.MILLISECONDS.toSeconds(theTime) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(theTime)));
                }
                catch(JSONException e){}
                */


                Intent intent = new Intent(getApplicationContext(), MapsActivity.class);
                intent.putExtra("Long", LongVal);
                intent.putExtra("Lat", LatVal);
                intent.putExtra("Valid", theTime);
                intent.putExtra("ValidString", time);
                startActivity(intent);
            }
        });
        Compass.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent2 = new Intent(getApplicationContext(), compass.class);
                startActivity(intent2);
            }
        });

        QRB.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    //start the scanning activity from the com.google.zxing.client.android.SCAN intent
                    Intent intent = new Intent(ACTION_SCAN);
                    intent.putExtra("SCAN_MODE", "QR_CODE_MODE");
                    startActivityForResult(intent, 0);
                } catch (ActivityNotFoundException anfe) {
                    //on catch, show the download dialog
                    showDialog(MainActivity.this, "No Scanner Found", "Download a scanner code activity?", "Yes", "No").show();
                }
            }
        });

    }

    public class Task extends AsyncTask<Context, String, String> {
        private Context context;
        public JSONObject getData(String url) {
            JSONObject theData = new JSONObject();
            HttpClient client = new DefaultHttpClient();
            HttpGet request = new HttpGet(url);
            HttpResponse response;
            String result = "";
            try {
                response = client.execute(request);
                // Get the response
                BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent(), "UTF8"));

                String line = "";
                while ((line = rd.readLine()) != null) {
                    result += line;
                }
                Log.d("api/track", result);

                String resulte = result.substring(result.indexOf("{"), (result.lastIndexOf("}") + 1));
                Log.d("api/track", resulte);
                theData = new JSONObject(resulte);
            } catch (Exception e) {
            }

            //Toast T = new Toast(getApplicationContext());
            //T.setText(result);

            //Toast.makeText(getApplication().getApplicationContext(), result, Toast.LENGTH_LONG).show();
            lock = false;
            return theData;
        }

        @Override
        protected String doInBackground(Context... params) {
            context = params[0];
            JSONObject theData = getData("http://167.205.32.46/pbd/api/track?nim=13512039");
            try {
                LatVal = theData.getDouble("lat");
                LongVal = theData.getDouble("long");
                theTime = theData.getLong("valid_until");

                time = String.format("%02d:%02d:%02d", ((TimeUnit.SECONDS.toHours(theTime) + 7) % 24),
                        TimeUnit.SECONDS.toMinutes(theTime) % 60,
                        TimeUnit.SECONDS.toSeconds(theTime) % 60);
            }
            catch(JSONException e){}

            return new String();
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            Toast.makeText(getApplication().getApplicationContext(), "Finished loading from PBD Server", Toast.LENGTH_LONG).show();
        }
    }
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

    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (requestCode == 0) {
            if (resultCode == RESULT_OK) {
                //get the extras that are returned from the intent
                contents = intent.getStringExtra("SCAN_RESULT");
                String format = intent.getStringExtra("SCAN_RESULT_FORMAT");
                Toast toast = Toast.makeText(this, "Content:" + contents + " Format:" + format, Toast.LENGTH_LONG);
                toast.show();

                // send post_http
                new postTask().execute(getApplicationContext());

            }
        }
    }

    public class postTask extends  AsyncTask<Context, String, String> {

        public String restring = "Undefined restring";
        @Override
        protected String doInBackground(Context... params) {

            HttpClient client = new DefaultHttpClient();
            HttpPost post = new HttpPost("http://167.205.32.46/pbd/api/catch");
            HttpResponse response;
            post.addHeader("content-type", "application/json");
            try {
                restring = "";
                List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
                nameValuePairs.add(new BasicNameValuePair("nim", "13512039"));
                nameValuePairs.add(new BasicNameValuePair("token", contents));

                JSONObject jeson = new JSONObject();
                jeson.put("nim", "13512039");
                jeson.put("token", contents);

                post.setEntity(new StringEntity(jeson.toString()));
                response = client.execute(post);

                BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
                Log.d("contents", contents);
                String line = "";
                while ((line = rd.readLine()) != null) {
                    restring += line;
                }
                Log.d("api/push", restring);

            }
            catch(Exception E){Log.d("reqBody", "paaah");}
            //Toast.makeText(getApplication().getApplicationContext(), restring, Toast.LENGTH_LONG).show();


            return new String();
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            Toast.makeText(getApplication().getApplicationContext(), restring, Toast.LENGTH_LONG).show();
        }


    }


}
