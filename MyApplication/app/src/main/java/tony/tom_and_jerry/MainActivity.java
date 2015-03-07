package tony.tom_and_jerry;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Typeface fontType = Typeface.createFromAsset(getAssets(), "fonts/chunkyness.ttf");
        TextView titleView = (TextView)findViewById(R.id.title);
        titleView.setTypeface(fontType);
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    public void onClickTitle(View view) {
        Intent intent = new Intent(this,MapActivity.class);
        if (intent != null) {
            startActivity(intent);
        }
    }
}
