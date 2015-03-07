package id.ac.itb.pbd.pbdtugas1;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

public class ValidateTask extends AsyncTask<String, String, String> {
	private static final String LOG_TAG = "KlongkgungValidateTask";
	private static final String POST_ENDPOINT = "http://167.205.32.46/pbd/api/catch";

	private Context main_context;

	public ValidateTask(Context ctx) {
		main_context = ctx;
	}

	@Override
	protected String doInBackground(String... args) {
		JSONObject to_send = new JSONObject();
		try {
			to_send.put("nim", args[0]);
			to_send.put("token", args[1]);
		} catch (JSONException e1) {
			Log.wtf(LOG_TAG, e1.getMessage());
		}

		HttpClient httpclient = new DefaultHttpClient();
		HttpResponse response;
		String responseString = null;
		try {
			HttpPost post_con = new HttpPost(POST_ENDPOINT);
			post_con.setHeader("Content-type", "application/json");
			post_con.setEntity(new StringEntity(to_send.toString()));

			response = httpclient.execute(post_con);
			StatusLine statusLine = response.getStatusLine();

			int statusCode = statusLine.getStatusCode();
			if (statusCode == HttpStatus.SC_OK
					|| statusCode == HttpStatus.SC_BAD_REQUEST
					|| statusCode == HttpStatus.SC_FORBIDDEN) {
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

		JSONObject jObject;
		try {
			jObject = new JSONObject(result);
			Toast.makeText(
					main_context,
					"Code: " + jObject.getString("code") + "\nMessage: "
							+ jObject.getString("message"), Toast.LENGTH_LONG)
					.show();
			Log.i(LOG_TAG, result);

		} catch (JSONException e) {
			Log.wtf(LOG_TAG, e.getMessage());
		}
	}
}