package com.tomvsjerry;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;


public class DeveloperActivity extends ActionBarActivity implements SpikeListener{

    private Spike spike;
    private double lan, lon;
    private String nim;

    private EditText etNim, etToken, etLan, etLon;
    private TextView txtExpired;
    private Button btnAsk, btnSend;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_developer);

        spike = new Spike(this, this);

        etNim = (EditText) findViewById(R.id.etDevNim);
        etToken = (EditText) findViewById(R.id.etDevToken);
        etLan = (EditText) findViewById(R.id.etJerryLat);
        etLon = (EditText) findViewById(R.id.etJerryLon);
        txtExpired = (TextView) findViewById(R.id.txtDevExpired);

        Bundle bundle = getIntent().getExtras();
        lan = bundle.getDouble("lat");
        lon = bundle.getDouble("lon");
        nim = bundle.getString("nim");

        etNim.setText(nim);
        etLan.setText(String.valueOf(lan));
        etLon.setText(String.valueOf(lon));
        txtExpired.setText(bundle.getString("expired"));

        setButtonListener();
    }

    private void setButtonListener()
    {
        btnAsk = (Button) findViewById(R.id.btnDevAskSpike);
        btnSend = (Button) findViewById(R.id.btnDevSend);

        btnAsk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                spike.askLocation(DeveloperActivity.this.etNim.getText().toString());
            }
        });

        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                spike.catchRequest(DeveloperActivity.this.etNim.getText().toString(),
                        DeveloperActivity.this.etToken.getText().toString());
            }
        });
    }


    @Override
    public void onAnswer() {
        etLan.setText(String.valueOf(spike.getLatitude()));
        etLon.setText(String.valueOf(spike.getLongitude()));
    }
}
