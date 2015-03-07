package com.tomjerry;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ParseException;
import android.net.Uri;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;


public class CatchJerryActivity extends ActionBarActivity {
 private String token;
 private TextView token_view;

 @Override
 public void onCreate(Bundle savedInstanceState) {
 super.onCreate(savedInstanceState);
 //set the main content layout of the Activity
 setContentView(R.layout.activity_catch_jerry);
 token_view = (TextView) findViewById(R.id.textView2);

 Intent intent = getIntent();

 token = intent.getStringExtra("respond");
 token_view.setText("");
 token_view.setText("'"  token  "'");
 }

 @Override
 public boolean onCreateOptionsMenu(Menu menu) {
 // Inflate the menu; this adds items to the action bar if it is present.
 getMenuInflater().inflate(R.menu.menu_locate, menu);
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
}