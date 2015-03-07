package com.tomjerry;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.net.Uri;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.content.Intent;
import android.view.View;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends ActionBarActivity {
 //QR Code Scanner
 static final String ACTION_SCAN = "com.google.zxing.client.android.SCAN";
 private boolean lock;
 private String responseText = null;

 @Override
 protected void onCreate(Bundle savedInstanceState) {
 super.onCreate(savedInstanceState);
 setContentView(R.layout.activity_main);
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
 /*if (id == R.id.action_settings) {
 return true;
 }*/

 return super.onOptionsItemSelected(item);
 }

 public void toAskSpike(View view){
 Intent intent = new Intent(this, LocateActivity.class);
 startActivity(intent);
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
 showDialog(MainActivity.this, "No Scanner Found", "Download a scanner code activity?", "Yes", "No").show();
 }
 }

 //alert dialog for downloadDialog
 private static AlertDialog showDialog(final Activity act, CharSequence title, CharSequence message, CharSequence buttonYes, CharSequence buttonNo) {
 AlertDialog.Builder downloadDialog = new AlertDialog.Builder(act);
 downloadDialog.setTitle(title);
 downloadDialog.setMessage(message);
 downloadDialog.setPositiveButton(buttonYes, new DialogInterface.OnClickListener() {
 public void onClick(DialogInterface dialogInterface, int i) {
 Uri uri = Uri.parse("market://search?q=pname:"  "com.google.zxing.client.android");
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
 final String token = intent.getStringExtra("SCAN_RESULT");
 lock = true;
 Thread postThread = new Thread() {
 @Override
 public void run() {
 try {
 super.run();
 try {
 HttpClient client = new DefaultHttpClient();
 HttpPost post = new HttpPost("http://167.205.32.46/pbd/api/catch");
 post.setHeader("Content-type","application/json");

 List postParameters = new ArrayList<NameValuePair>();
 postParameters.add(new BasicNameValuePair("token", token));
 postParameters.add(new BasicNameValuePair("nim", "13512006"));

 post.setEntity(new UrlEncodedFormEntity(postParameters));

 JSONObject jsonObj = new JSONObject();
 StringEntity entity = null;
 jsonObj.put("nim", "13512006");
 jsonObj.put("token", token);
 entity = new StringEntity(jsonObj.toString(), HTTP.UTF_8);

 entity.setContentType("application/json");
 post.setEntity(entity);

 String result = "";
 HttpResponse response = client.execute(post);

 int status = response.getStatusLine().getStatusCode();
 if(status == 200) {
 result = response.getStatusLine().getReasonPhrase();
 } else if(status == 400) {
 result = response.getStatusLine().getReasonPhrase();
 } else if(status == 403) {
 result = response.getStatusLine().getReasonPhrase();
 } else {
 result = response.getStatusLine().getReasonPhrase();
 }
 responseText = result;
 lock = false;
 } catch (Exception e) {
 }
 } catch (Exception e) {

 } finally {

 }
 }
 };
 postThread.start();
 while(lock) {}
 Intent intentt = new Intent(this, CatchJerryActivity.class);
 intentt.putExtra("respond",responseText);
 startActivity(intentt);
 }
 }
 }
}