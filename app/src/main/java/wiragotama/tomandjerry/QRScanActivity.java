package wiragotama.tomandjerry;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

/**
 * Created by wira gotama on 2/26/2015.
 */
public class QRScanActivity extends Activity {
    String msg = "Debug QR Scan - WIRAGOTAMA GANTENG - ";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan);
        //get request
        Log.d(msg, "The onCreate() event");
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(msg, "The onStart() event");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(msg, "The onResume() event");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(msg, "The onPause() event");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(msg, "The onStop() event");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(msg, "The onDestroy() event");
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void POST_REQUEST() {
        //kirim token ke server
    }


    public void changeToDirectionActivity(View view) {
        Intent i = new Intent(this, MainActivity.class);
        onPause();
        startActivity(i);
    }
}
