package id.ac.itb.pbd.pbdtugas1;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.AsyncTask;
import android.util.Log;

public class RequestTask extends AsyncTask<String, String, String> {
	private static final String LOG_TAG = "KlongkgungRequestTask";
	
	MainActivity main_window;
	
	public RequestTask(MainActivity ref) {
		main_window = ref;
	}

	@Override
	protected String doInBackground(String... uri) {
		HttpClient httpclient = new DefaultHttpClient();
		HttpResponse response;
		String responseString = null;
		try {
			response = httpclient.execute(new HttpGet(uri[0]));
			StatusLine statusLine = response.getStatusLine();
			
			if (statusLine.getStatusCode() == HttpStatus.SC_OK) {
				ByteArrayOutputStream out = new ByteArrayOutputStream();
				response.getEntity().writeTo(out);
				responseString = out.toString();
				out.close();
				
			} else {
				// Closes the connection.
				response.getEntity().getContent().close();
				throw new IOException(statusLine.getReasonPhrase());
			}
			
		} catch (ClientProtocolException e) {
			Log.wtf(LOG_TAG, e.getMessage());
		} catch (IOException e) {
			Log.wtf(LOG_TAG, e.getMessage());
		}
		
		return responseString;
	}

	@Override
	protected void onPostExecute(String result) {
		super.onPostExecute(result);
		// Do anything with response..
		try {
			JSONObject info_jerry = new JSONObject(result);
			main_window.setLatitude(info_jerry.getDouble("lat"));
			main_window.setLongitude(info_jerry.getDouble("long"));
			main_window.setValidUntil(info_jerry.getLong("valid_until"));
			main_window.setJerryLocated(true);
			main_window.setUpMap();
			main_window.setTimer();
			
		} catch (JSONException e) {
			Log.wtf(LOG_TAG, e);
		}
	}
}
