package com.example.tomandjerry;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

public class QR extends Activity {

	static final String ACTION_SCAN = "com.google.zxing.client.android.SCAN";
	public final Context context = this;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//set the main content layout of the Activity
		setContentView(R.layout.qr);
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
			showDialog(QR.this, "No Scanner Found", "Download a scanner code activity?", "Yes", "No").show();
		}
	}

	//alert dialog for downloadDialog
	private AlertDialog showDialog(final Activity act, CharSequence title, CharSequence message, CharSequence buttonYes, CharSequence buttonNo) {
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
				sendJson("13512056",contents);
				toast.show();
			}
		}
	}
	
	protected void sendJson(final String nim, final String token) {
        Thread t = new Thread() {

            public void run() {
                Looper.prepare(); //For Preparing Message Pool for the child Thread
                HttpClient client = new DefaultHttpClient();
                HttpConnectionParams.setConnectionTimeout(client.getParams(), 10000); //Timeout Limit
                HttpResponse response;
                JSONObject json = new JSONObject();

                try {
                    HttpPost post = new HttpPost("http://167.205.32.46/pbd/api/catch");
                    json.put("nim", nim);
                    json.put("token", token);
                    StringEntity se = new StringEntity( json.toString());  
                    se.setContentType(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));
                    post.setEntity(se);
                    response = client.execute(post);

                    /*Checking response */
                    if(response!=null){
                        String responseServer = EntityUtils.toString(response.getEntity());
                        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
                        alertDialogBuilder.setTitle("Server Message");
                        alertDialogBuilder.setMessage(serverResultToString(responseServer));
                        AlertDialog alertDialog = alertDialogBuilder.create();
                        alertDialog.show();
                    }

                } catch(Exception e) {
                    e.printStackTrace();
                    Log.e("Error", "Cannot Estabilish Connection");
                }

                Looper.loop(); //Loop in the message queue
            }
        };

        t.start();      
    }
	
	public String serverResultToString(String message){
		String result = "";
		if(message.contains(":200")){
			result = "Status: 200 OK";
		} else if (message.contains(":400")){
			result = "Status: 400 MISSING PARAMETER";
		} else if (message.contains(":403")){
			result = "Status: 403 FORBIDDEN";
		}
		return result;
	}
}
