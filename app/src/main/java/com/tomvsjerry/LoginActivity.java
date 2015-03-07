package com.tomvsjerry;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;


public class LoginActivity extends ActionBarActivity {
    Button btnLogin;
    EditText etNim;

    int devCounter;
    boolean devMode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        devCounter = 0;
        devMode = false;

        registerViews();
    }

    private void registerViews()
    {
        btnLogin = (Button) findViewById(R.id.btnLogin);
        etNim = (EditText) findViewById(R.id.etNim);

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(LoginActivity.this.etNim.getText().length() == 0)
                {
                    devCounter++;
                    if (devCounter == 10)
                    {
                        devMode = true;
                        Toast.makeText(LoginActivity.this, "Developer mode enabled", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    intent.putExtra("nim", LoginActivity.this.etNim.getText().toString());
                    intent.putExtra("devMode", LoginActivity.this.devMode);
                    startActivity(intent);
                }
            }
        });
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_login, menu);
        return true;
    }
}
