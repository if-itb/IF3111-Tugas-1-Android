package tony.tom_and_jerry;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
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


public class ScanActivity extends Activity {

    static final String ACTION_SCAN = "com.google.zxing.client.android.SCAN";
    TextView secretTokenView,responseStatusView;
    Button postButton;

    String secretTokenString;
    String responseStatusString;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan);
        secretTokenView = (TextView) findViewById(R.id.secretToken);
        responseStatusView = (TextView) findViewById(R.id.responseStatus);
        postButton = (Button) findViewById(R.id.postButton);

        try {
            // start the scanning activity from the com.google.zxing.client.android.SCAN.intent
            Intent intent = new Intent(ACTION_SCAN);
            intent.putExtra("SCAN_MODE","QR_CODE_MODE");
            startActivityForResult(intent,0);
        } catch (ActivityNotFoundException anfe) {
            // on catch, show the download dialog
            showDialog(ScanActivity.this, "No Scanner Found", "Download a scanner code activity?", "Yes", "No").show();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    // alert dialog for downloadDialogx
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
        downloadDialog.setNegativeButton(buttonNo,new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        return downloadDialog.show();
    }

    // onActivityResult Method
    public void onActivityResult (int requestCode, int resultCode, Intent intent) {
        if (requestCode == 0) {
            if (resultCode == RESULT_OK) {
                String contents = intent.getStringExtra("SCAN_RESULT");
                String format = intent.getStringExtra("SCAN_RESULT_FORMAT");

                secretTokenString = contents;

                secretTokenView.setText("Secret Token: "+secretTokenString);
                postButton.setVisibility(View.VISIBLE);

                Toast toast = Toast.makeText(this, "Content:" + contents + " Format:" + format, Toast.LENGTH_LONG);

                toast.show();
            }
        }
    }

    class tokenSender extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... params) {
            Intent intent = new Intent(ACTION_SCAN);
            HttpClient httpClient = new DefaultHttpClient();
            String responseString = "";
            HttpResponse httpResponse = null;
            HttpPost httpPost = new HttpPost("http://167.205.32.46/pbd/api/catch");
            try {
                List<BasicNameValuePair> Parameters = new ArrayList();

                Parameters.add(new BasicNameValuePair("nim","13512018"));
                Parameters.add(new BasicNameValuePair("token", secretTokenString));
                httpPost.setEntity(new UrlEncodedFormEntity(Parameters));

                httpResponse = httpClient.execute(httpPost);
                InputStream content = httpResponse.getEntity().getContent();
                BufferedReader buffer = new BufferedReader(new InputStreamReader(content));
                String s = "";
                while ((s = buffer.readLine())!= null) {
                    responseString += s;
                }
            } catch (ClientProtocolException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return responseString;
        }


        @Override
        protected void onPostExecute(String res) {
            super.onPostExecute(res);

            try {
                JSONObject object = new JSONObject(res);
                String msg = object.getString("message");
                int statusCode = object.getInt("code");

                responseStatusView.setText("Response Status: " + statusCode + "\nMessage: " + msg);
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
    }

    public void postButtonClick(View view) {
        new tokenSender().execute();
        postButton.setVisibility(View.INVISIBLE);
    }
}
