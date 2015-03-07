package com.example.kevinhuang.tomandjerry2;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;


public class MainScreen extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final Button btnAskSpike = (Button)findViewById(R.id.btnaskspike);
        btnAskSpike.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent catchintent = new Intent(view.getContext(),AskSpike.class);
                startActivity(catchintent);
            }
        });
        final Button btnhowtoplay = (Button)findViewById(R.id.btnhowto);
        btnhowtoplay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent catchintent = new Intent(view.getContext(),Howtoplay.class);
                startActivity(catchintent);
            }
        });
        final Button btnabout = (Button)findViewById(R.id.btnabout);
        btnabout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent catchintent = new Intent(view.getContext(),report.class);
                startActivity(catchintent);
            }
        });
        final Button btnCatchJerry = (Button)findViewById(R.id.btncatchjerry);
        btnCatchJerry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent catchintent = new Intent(view.getContext(),catchjerry.class);
                startActivity(catchintent);
            }
        });
    }
}
