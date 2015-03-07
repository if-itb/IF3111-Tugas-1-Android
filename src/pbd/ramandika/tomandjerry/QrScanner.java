package pbd.ramandika.tomandjerry;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public class QrScanner extends Activity {
	/** Called when the activity is first created. */

	static final String ACTION_SCAN = "com.google.zxing.client.android.SCAN";
	private ProgressDialog pDialog;
	private static String url="http://167.205.32.46/pbd/api/catch";
	private String token_QR=null;
	private String message;
	private String responseServer;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.qr_scanner);
		try {
			Intent intent = new Intent(ACTION_SCAN);
			intent.putExtra("SCAN_MODE", "QR_CODE_MODE");
			startActivityForResult(intent, 0);
			
		} catch (ActivityNotFoundException anfe) {
			showDialog(QrScanner.this, "No Scanner Found", "Download a scanner code activity?", "Yes", "No").show();
		}
		
	}
	
	private class postQR extends AsyncTask<Void,Void,Void>{
    	@Override
    	protected void onPreExecute(){
    		super.onPreExecute();
    		pDialog=new ProgressDialog(QrScanner.this);
    		pDialog.setMessage("Uploading QR to Server....");
    		pDialog.setCancelable(false);
    		pDialog.show();
    	}
    	@Override
    	protected Void doInBackground(Void... arg0){
    		ServiceHandler service=new ServiceHandler();
    		List<NameValuePair> params= new ArrayList<NameValuePair>();
    		params.add(new BasicNameValuePair("nim", "13512078"));
    		params.add(new BasicNameValuePair("token", token_QR));
    		try{
    			JSONObject jsonObj=new JSONObject(service.makeServiceCall(url,ServiceHandler.POST,params));
    			message=jsonObj.getString("message");
    			responseServer=jsonObj.getString("code");
    			if(responseServer.equals("200")) responseServer="QR Code berhasil diterima (200)";
    			else if(responseServer.equals("400")) responseServer="Terdapat Parameter yang hilang(400)";
    			else if(responseServer.equals("403")) responseServer="Ada Parameter yang Salah(403)";
    			else responseServer="Undefined Error";
    		}catch(JSONException e){
    			e.printStackTrace();
    		}
    		
    		if(responseServer==null)
    			Log.e("postQR","Can't Connect to Server");
    		return null;
    	}
    	@Override
    	protected void onPostExecute(Void result){
    		super.onPostExecute(result);if(pDialog.isShowing()) pDialog.dismiss();
    		TextView serverResponse=(TextView) findViewById(R.id.status);
    		serverResponse.setText("Value Send=" +message+
    				"\n\n\nRespon Server="+ responseServer);
    	}
	}
	
	public void scanQR(View v) {
		try {
			Intent intent = new Intent(ACTION_SCAN);
			intent.putExtra("SCAN_MODE", "QR_CODE_MODE");
			startActivityForResult(intent, 0);
		} catch (ActivityNotFoundException anfe) {
			showDialog(QrScanner.this, "No Scanner Found", "Download a scanner code activity?", "Yes", "No").show();
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

	public void onActivityResult(int requestCode, int resultCode, Intent intent) {
		if (requestCode == 0) {
			if (resultCode == RESULT_OK) {
				String contents = intent.getStringExtra("SCAN_RESULT");
				String format = intent.getStringExtra("SCAN_RESULT_FORMAT");
				token_QR=contents;
				Toast toast = Toast.makeText(this, "Content:" + contents + " Format:" + format, Toast.LENGTH_LONG);
				toast.show();
				new postQR().execute();
			}
		}
	}
}