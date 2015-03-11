package com.example.tomandjerry;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;


public class MainActivity extends Activity {
	
	Button bCompass, bGPS, bQR;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		bCompass = (Button) findViewById(R.id.bCompass);
		bGPS = (Button) findViewById(R.id.bGPS);
		bQR = (Button) findViewById(R.id.bQR);
		
		bCompass.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				
				Intent openCompass = new Intent("com.example.tomandjerry.COMPASS");
				startActivity(openCompass);

			}
		});
		bGPS.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				
				Intent openGPS = new Intent("com.example.tomandjerry.GPS");
				startActivity(openGPS);
				
			}
		});
		bQR.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				
				Intent openQR = new Intent("com.example.tomandjerry.QR");
				startActivity(openQR);
				
			}
		});
	}
}
