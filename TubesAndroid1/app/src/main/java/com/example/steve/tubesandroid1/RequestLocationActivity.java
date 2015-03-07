package com.example.steve.tubesandroid1;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.hardware.SensorManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;


public class RequestLocationActivity extends Activity {
    /**
     * Atribut kelas
     */
    private TextView textLatitude;             // Text yang menandakan posisi latitude Jerry
    private TextView textLongitude;            // Text yang menandakan posisi longitude Jerry
    private TextView textStatusTangkap;        // Text yang menandakan status penangkapan Jerry sudah tertangkap atau belum
    private TextView textToken;                // Text yang menampilkan token hasil QR scanner
    private Button buttonQRScan;               // Button yang ada pada layout.xml berfungsi untuk melakukan scan QR code
    private Button buttonLacakJerry;           // Button yang ada pada layout.xml berfungsi melacak posisi Jerry saat ini
    private Button buttonCatchJerry;           // Button yang ada pada layout.xml berfungsi melakukan request penangkapan Jerry
    private String[] Koordinat;                // Koordinat Jerry : Koordinat[0] = latitude, Koordinat[1] = longitude
    private String tokenJerry = "";            // String untuk menyimpan hasil scanner QR code

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_request_location);

        // Bind view dengan TextView atribut
        textLatitude = (TextView) findViewById(R.id.textLatitude);
        textLongitude = (TextView) findViewById(R.id.textLongitude);
        textStatusTangkap = (TextView) findViewById(R.id.statusPenangkapanJerry);
        textToken = (TextView) findViewById(R.id.tokenQRCode);

        // Bind view dengan Button atribut
        buttonQRScan = (Button) findViewById(R.id.scanCode);
        buttonLacakJerry = (Button) findViewById(R.id.lacakJerry);
        buttonCatchJerry = (Button) findViewById(R.id.catchJerry);

        // Setting konfigurasi awal view TextView dan Button
        textLatitude.setText("");
        textLongitude.setText("");
        textStatusTangkap.setText("Jerry Kabur");
        textToken.setText("");
        buttonQRScan.setEnabled(true);
        buttonLacakJerry.setEnabled(true);
        buttonCatchJerry.setEnabled(false);

        // Inisialisasi array of koordinat Jerry
        Koordinat = new String[2];
    }

    /**
     * Kelas asynchronous untuk menghandle request yang ditujukan pada UI thread
     *
     */
    private class sendTokenJerry extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... endpoint_url) {
            // Buat objek http
            HttpClient httpClient = new DefaultHttpClient();
            HttpPost httpPost = new HttpPost(endpoint_url[0]);

            // Buat objek json dengan nim dan token sebagai elemen
            JSONObject jsonPostRequest = new JSONObject();
            try {
                jsonPostRequest.accumulate("nim",13512035);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            try {
                jsonPostRequest.accumulate("token",tokenJerry);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            // Konversi json object yand dibuat ke string lalu dikonversi lagi ke bentuk stringEntity
            String postRequest = jsonPostRequest.toString();
            StringEntity finalRequest = null;
            try {
                finalRequest = new StringEntity(postRequest);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }

            // Setting post header dengan parameter key value pair yang sudah dibuat sebelumnya
            httpPost.setEntity(finalRequest);

            // Lakukan post request dengan parameter request body, dapatkan status request lalu return
            String Status = "";
            try {
                HttpResponse response = httpClient.execute(httpPost);
                int StatusCode = response.getStatusLine().getStatusCode();
                Status = Integer.toString(StatusCode);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return Status;
        }

        @Override
        protected void onPostExecute(String result) {
            if (result.equals("200")) {
                textStatusTangkap.setText("Jerry Tertangkap :D");
            }
            else {
                textStatusTangkap.setText("Yah Jerry Kabur Lagi :(");
            }

            if (!textToken.getText().equals("") && !textLatitude.getText().equals("")
                    && !textLongitude.getText().equals("")) {
                // Aktifkan tombol
                buttonCatchJerry.setEnabled(true);
            }
        }
    }

    /**
     * Kelas asynchronous untuk menghandle request yang ditujukan pada UI thread
     *
     */
    private class requestLngLatJerry extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... endpoint_url) {
            // Buat objek http dan url
            URL endpoint = null;
            try {
                endpoint = new URL(endpoint_url[0]);
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
            HttpURLConnection askEndpoint = null;
            try {
                askEndpoint = (HttpURLConnection) endpoint.openConnection();
            } catch (IOException e) {
                e.printStackTrace();
            }

            // Baca response dari request endpoint dalam bentuk buffer stream
            BufferedReader reader = null;
            try {
                assert askEndpoint != null;
                reader = new BufferedReader(new InputStreamReader(askEndpoint.getInputStream()));
            } catch (IOException e) {
                e.printStackTrace();
            }

            // Ubah stream buffer dari response ke string
            String response = "";
            try {
                response = reader.readLine();
            } catch (IOException e) {
                e.printStackTrace();
            }

            // Tutup koneksi endpoint
            try {
                reader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return response;
        }

        private void getKoordinatJerry (String response) {
            JSONObject konversiResult = null;
            try {
                konversiResult = new JSONObject(response);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            try {
                Koordinat[0] = konversiResult.getString("lat");     // Latitude Jerry
            } catch (JSONException e) {
                e.printStackTrace();
            }
            try {
                Koordinat[1] = konversiResult.getString("long");    // Longitude Jerry
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        @Override
        protected void onPostExecute(String result) {
            getKoordinatJerry(result);
            textLatitude.setText(Koordinat[0]);
            textLongitude.setText(Koordinat[1]);

            if (!textToken.getText().equals("") && !textLatitude.getText().equals("")
                    && !textLongitude.getText().equals("")) {
                // Aktifkan tombol
                buttonCatchJerry.setEnabled(true);
            }
        }
    }

    /**
     * Method untuk melakukan action scan QR code saat tombol diklik
     * @param view widget button
     */
    public void onClickButton2(View view) {
        Intent intentQRCode = new Intent("com.google.zxing.client.android.SCAN");
        intentQRCode.putExtra("SCAN_MODE", "QR_CODE_MODE");
        startActivityForResult(intentQRCode, 0);
    }

    /**
     * Method menghandle kembalian hasil activity scan QR code
     * @param requestCode
     * @param resultCode
     * @param intent
     */
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (requestCode == 0) {
            if (resultCode == RESULT_OK) {
                String contents = intent.getStringExtra("SCAN_RESULT");
                textToken.setText(contents);
                tokenJerry = contents;
            }
        }
    }

    /**
     * Method untuk melakukan konfirmasi ke endpoint apakah posisi Jerry sudah tepat saat ini
     * @param view
     */
    public void onClickButton3(View view) {
        // Post token dan nim
        sendTokenJerry taskCatch = new sendTokenJerry();
        taskCatch.execute(new String[] {"http://167.205.32.46/pbd/api/catch"});
    }

    /**
     * Method untuk menampilkan posisi Jerry saat tombol diklik
     * @param view widget button
     */
    public void onClickButton(View view) {
        // Request lokasi
        requestLngLatJerry taskRequest = new requestLngLatJerry();
        taskRequest.execute(new String[] {"http://167.205.32.46/pbd/api/track?nim=13512035"});

        // Start intent google map
        Intent googleMapActivity = new Intent(this, mapGoogleJerry.class);
        /*googleMapActivity.putExtra("latitude", Koordinat[0]);
        googleMapActivity.putExtra("longitude", Koordinat[1]);*/
        lokasiJerry.latitudeJerry = Koordinat[0];
        lokasiJerry.longitudeJerry = Koordinat[1];
        this.startActivity(googleMapActivity);
    }
}
