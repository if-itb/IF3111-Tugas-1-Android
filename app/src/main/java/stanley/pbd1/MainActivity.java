package stanley.pbd1;

import android.accounts.NetworkErrorException;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HTTP;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;


public class MainActivity extends ActionBarActivity implements View.OnClickListener {

    static final String ACTION_SCAN = "com.google.zxing.client.android.SCAN";
    Button track;
    Button gsbutton;

    JSONObject deliver = new JSONObject();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        track = (Button) findViewById(R.id.button);
        track.setOnClickListener(this);
        gsbutton = (Button)findViewById(R.id.button2);
        gsbutton.setOnClickListener(this);
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

    public void onClick(View v){
        switch(v.getId())
        {
            case R.id.button:
                button1click();
                break;
            case R.id.button2:
                button2click();
                break;
        }
    }

    private void button1click() {
        startActivity(new Intent("stanley.pbd1.Tracker"));
    }

    private void button2click() {
        startActivity(new Intent("stanley.pbd1.CompassActivity"));
    }

    public void help(View v) {
        AlertDialog.Builder helpdialog = new AlertDialog.Builder(this);
        helpdialog.setTitle("Instructions");
        helpdialog.setMessage("Find Jerry then catch him !!");
        helpdialog.setNeutralButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialogInterface, int i) {
            }
        });
        AlertDialog help = helpdialog.create();
        help.show();
    }
    public void scanQR(View v) {
        try {
            Intent intent = new Intent(ACTION_SCAN);
            intent.putExtra("SCAN_MODE", "QR_CODE_MODE");
            startActivityForResult(intent, 0);
        } catch (ActivityNotFoundException e) {
            showDialog(MainActivity.this, "No Scanner Found", "Download a scanner code activity?", "Yes", "No").show();
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
                    AlertDialog.Builder errordialog = new AlertDialog.Builder(act);
                    errordialog.setMessage("Activity not found");
                    errordialog.setNeutralButton("Ok", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialogInterface, int i) {
                        }
                    });
                }
            }
        });
        downloadDialog.setNegativeButton(buttonNo, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialogInterface, int i) {
            }
        });
        return downloadDialog.show();
    }

    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (requestCode == 0) {
            if (resultCode == RESULT_OK) {
                //get the extras that are returned from the intent
                String contents = intent.getStringExtra("SCAN_RESULT");
                String format = intent.getStringExtra("SCAN_RESULT_FORMAT");

                Toast toast = Toast.makeText(this, "Content:" + contents + " Format:" + format, Toast.LENGTH_SHORT);
                toast.show();
                try {
                    deliver.accumulate("nim", "13512086");
                    deliver.accumulate("token",contents);
                } catch (JSONException je) {
                    Toast jsonerror = Toast.makeText(this, "Error : " + je.getMessage(), Toast.LENGTH_LONG);
                    jsonerror.show();
                }
                new AsyncSend().execute("http://167.205.32.46/pbd/api/catch");
            }
        }
    }

    public class AsyncSend extends AsyncTask<String, Void, Integer> {
        @Override
        protected Integer doInBackground(String... urls) {
            try {
                String url = urls[0];
                String token = deliver.toString();
                HttpParams httpParams = new BasicHttpParams();
                HttpClient client = new DefaultHttpClient(httpParams);
                HttpPost postserver = new HttpPost(url);
                postserver.setHeader("Content-Type","application/json");
                postserver.setHeader("Accept","application/json");
                StringEntity entity = new StringEntity(token, HTTP.UTF_8);
                entity.setContentType("application/json");
                postserver.setEntity(entity);
                HttpResponse response = client.execute(postserver);
                int stats=response.getStatusLine().getStatusCode();
                return stats;
            }
            catch (Exception e){
                e.printStackTrace();
            }
            return 0;
        }


        @Override
        protected void onPostExecute(Integer stats) {

         //   Toast.makeText(getApplicationContext(),stats.toString(),Toast.LENGTH_LONG).show();
            Toast result;
            switch(stats){
                case 200 :
                    result = Toast.makeText(getApplicationContext(),"Jerry Caught !",Toast.LENGTH_SHORT);
                    result.show();
                    break;
                case 400:
                    result = Toast.makeText(getApplicationContext(),"Missing Parameter",Toast.LENGTH_SHORT);
                    result.show();
                    break;
                case 403:
                    result = Toast.makeText(getApplicationContext(),"Forbidden",Toast.LENGTH_SHORT);
                    result.show();
                    break;
                default:
                    result = Toast.makeText(getApplicationContext(),"Wrong Results",Toast.LENGTH_SHORT);
                    result.show();
            }
        }



    }
}
