package com.example.afik.tomnjerry;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;


public class MainActivity extends Activity {
    private static final int REQUEST_QR = 4;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }


    public void onClickMaps(View view) {
        Intent i = new Intent(this, MapsActivity.class);
        startActivity(i);
    }


    public void onClickQR(View view) {
        Intent i = new Intent(this, QRCode.class);
        startActivityForResult(i,REQUEST_QR);
    }

}
