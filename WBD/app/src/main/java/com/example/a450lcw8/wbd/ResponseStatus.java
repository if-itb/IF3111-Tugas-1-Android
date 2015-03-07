package com.example.a450lcw8.wbd;

import android.app.Activity;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;


public class ResponseStatus extends Activity {

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.activity_response_status);
        Bundle extras = getIntent().getExtras();
        String inputString = extras.getString("responOk");
        TextView view = (TextView) findViewById(R.id.respon);
        view.setText(inputString);
    }

}
