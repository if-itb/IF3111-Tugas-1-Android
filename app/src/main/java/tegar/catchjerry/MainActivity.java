package tegar.catchjerry;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.CountDownTimer;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.support.v4.widget.DrawerLayout;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;


public class MainActivity extends ActionBarActivity
        implements NavigationDrawerFragment.NavigationDrawerCallbacks {

    static final String ACTION_SCAN = "com.google.zxing.client.android.SCAN";
    private String TAG = "debug";
    private String qRCodeString;
    private String latitude;
    private String longitude;
    private String valid_until;
    private String tokenQRCode;
    private boolean lock;
    private TextView longTextView;
    private TextView latTextView;
    private TextView validTextView;

    /**
     * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
     */

    public void scanQR(View v) {
        try {
            //start the scanning activity from the com.google.zxing.client.android.SCAN intent
            Intent intent = new Intent(ACTION_SCAN);
            intent.putExtra("SCAN_MODE", "QR_CODE_MODE");
            startActivityForResult(intent, 0);
        } catch (ActivityNotFoundException anfe) {
            //on catch, show the download dialog
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (requestCode == 0) {
            if (resultCode == RESULT_OK) {
                //get the extras that are returned from the intent
                String contents = intent.getStringExtra("SCAN_RESULT");
                String format = intent.getStringExtra("SCAN_RESULT_FORMAT");
                Toast toast = Toast.makeText(this, "Content:" + contents + " Format:" + format, Toast.LENGTH_LONG);
                toast.show();
                tokenQRCode = contents;
                PostTask postTask = new PostTask();
                postTask.execute(getApplicationContext());
            }
        }
    }

    public void showMap(View v)
    {
        LatLng jerryPosition = new LatLng(Double.valueOf(latitude), Double.valueOf(longitude));
        Bundle args = new Bundle();
        args.putParcelable("jerryPosition", jerryPosition);
        Intent myIntent = new Intent(MainActivity.this, ShowMapActivity.class);
        myIntent.putExtra("bundle",args);
        MainActivity.this.startActivity(myIntent);
    }

    public void showCompass(View v)
    {
        Intent myIntent = new Intent(MainActivity.this, CompassActivity.class);
        MainActivity.this.startActivity(myIntent);
    }

    private NavigationDrawerFragment mNavigationDrawerFragment;

    /**
     * Used to store the last screen title. For use in {@link #restoreActionBar()}.
     */
    private CharSequence mTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mNavigationDrawerFragment = (NavigationDrawerFragment)
                getSupportFragmentManager().findFragmentById(R.id.navigation_drawer);
        mTitle = getTitle();

        // Set up the drawer.
        mNavigationDrawerFragment.setUp(
                R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout));

        /*Get Long lang from server*/
        String result = "";
        GetTask getTask = new GetTask();
        getTask.execute(getApplicationContext());
        if (latitude!=null)
        {

        }
    }

    @Override
    public void onNavigationDrawerItemSelected(int position) {
        // update the main content by replacing fragments
        FragmentManager fragmentManager = getSupportFragmentManager();
        if (position==0)
        {
            MainFragment mainFragment = new MainFragment();
            fragmentManager.beginTransaction()
                    .replace(R.id.container, mainFragment.newInstance(position + 1))
                    .commit();
        }
        else if (position==1)
        {
            FirstFragment firstFragment = new FirstFragment();
            fragmentManager.beginTransaction()
                    .replace(R.id.container, firstFragment.newInstance(position + 1))
                    .commit();
        }
        else if (position==2)
        {
            SecondFragment secondFragment = new SecondFragment();
            fragmentManager.beginTransaction()
                    .replace(R.id.container, secondFragment.newInstance(position + 1))
                    .commit();
        }
        else if (position==3)
        {
            ThirdFragment thirdFragment = new ThirdFragment();
            fragmentManager.beginTransaction()
                    .replace(R.id.container, thirdFragment.newInstance(position + 1))
                    .commit();
        }
    }

    public void onSectionAttached(int number) {
        switch (number) {
            case 1:
                mTitle = getString(R.string.title_section1);
                break;
            case 2:
                mTitle = getString(R.string.title_section2);
                break;
            case 3:
                mTitle = getString(R.string.title_section3);
                break;
            case 4:
                mTitle = getString(R.string.title_section4);
                break;
        }
    }

    public void restoreActionBar() {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(mTitle);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!mNavigationDrawerFragment.isDrawerOpen()) {
            // Only show items in the action bar relevant to this screen
            // if the drawer is not showing. Otherwise, let the drawer
            // decide what to show in the action bar.
            getMenuInflater().inflate(R.menu.main, menu);
            restoreActionBar();
            return true;
        }
        return super.onCreateOptionsMenu(menu);
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

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main, container, false);
            return rootView;
        }

        @Override
        public void onAttach(Activity activity) {
            super.onAttach(activity);
            ((MainActivity) activity).onSectionAttached(
                    getArguments().getInt(ARG_SECTION_NUMBER));
        }
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public class MainFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        Button qrCode;
        private static final String ARG_SECTION_NUMBER = "section_number";
        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */

        public MainFragment newInstance(int sectionNumber) {
            MainFragment fragment = new MainFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        public MainFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_mainfragment, container, false);
            return rootView;
        }
        @Override
        public void onAttach(Activity activity) {
            super.onAttach(activity);
            ((MainActivity) activity).onSectionAttached(
                    getArguments().getInt(ARG_SECTION_NUMBER));
        }

    }

    public class FirstFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";
        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */

        public FirstFragment newInstance(int sectionNumber) {
            FirstFragment fragment = new FirstFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        public FirstFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_fragment1, container, false);
            return rootView;
        }
        @Override
        public void onAttach(Activity activity) {
            super.onAttach(activity);
            ((MainActivity) activity).onSectionAttached(
                    getArguments().getInt(ARG_SECTION_NUMBER));
        }

    }

    public class SecondFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public SecondFragment newInstance(int sectionNumber) {
            SecondFragment fragment = new SecondFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        public SecondFragment() {

        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_fragment2, container, false);
            latTextView=(TextView)rootView.findViewById(R.id.latitude);
            longTextView=(TextView)rootView.findViewById(R.id.longitude);
            validTextView=(TextView)rootView.findViewById(R.id.expired);
            latTextView.setText("Latitude = "+latitude);
            longTextView.setText("Longitude = " + longitude);
            Date date1 = new Date();
            Date date2 = new Date(Long.valueOf(valid_until)*1000);
            final int days = (int)(date2.getTime() - date1.getTime()) / (1000 * 60 * 60 * 24);
            long timeDiff = date2.getTime() - date1.getTime();
            new CountDownTimer(timeDiff, 1000){

                @Override
                public void onTick(long millisUntilFinished) {
                    String seconds = String.valueOf((millisUntilFinished/1000)%60);
                    String minutes = String.valueOf((millisUntilFinished/1000)/60%60);
                    String hours = String.valueOf((millisUntilFinished/1000)/60/60%24);
                    String dayString = String.valueOf(millisUntilFinished / (1000 * 60 * 60 * 24));
                    if(seconds.length() == 1) seconds = "0" + seconds;
                    if(minutes.length() == 1) minutes = "0" + minutes;
                    if(hours.length() == 1) hours = "0" + hours;
                    if(days <= 1) dayString = dayString + " day ";
                    else dayString = dayString + " day(s) ";
                    validTextView.setText("Time before Jerry run =  "+ dayString + hours + ":" + minutes + ":" + seconds);
//                    validTextView.setText("Time before Jerry run = "+valid_until);
                }

                @Override
                public void onFinish() {
                    GetTask getTask = new GetTask();
                    getTask.execute(getApplicationContext());
                }
            }.start();
            return rootView;
        }

        @Override
        public void onAttach(Activity activity) {
            super.onAttach(activity);
            ((MainActivity) activity).onSectionAttached(
                    getArguments().getInt(ARG_SECTION_NUMBER));
        }
    }

    public class ThirdFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public ThirdFragment newInstance(int sectionNumber) {
            ThirdFragment fragment = new ThirdFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        public ThirdFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_fragment3, container, false);
            return rootView;
        }

        @Override
        public void onAttach(Activity activity) {
            super.onAttach(activity);
            ((MainActivity) activity).onSectionAttached(
                    getArguments().getInt(ARG_SECTION_NUMBER));
        }
    }

    public class GetTask extends AsyncTask<Context, String, String> {

        private Context context;

        public void parse(String result) {
            JSONObject jsonObject = null;
            try {
                jsonObject = new JSONObject(result);
                latitude = jsonObject.getString("lat");
                longitude = jsonObject.getString("long");
                valid_until = jsonObject.getString("valid_until");
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        @Override
        protected void onPreExecute() {
            // TODO Auto-generated method stub
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(Context... params) {
            // TODO Auto-generated method stub
            context = params[0];

            HttpClient client = new DefaultHttpClient();
            HttpGet request = new HttpGet("http://167.205.32.46/pbd/api/track?nim=13512061");
            HttpResponse response;
            String result = "";
            try {
                response = client.execute(request);
                // Get the response
                BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));

                String line = "";
                while ((line = rd.readLine()) != null) {
                    result += line;
                }
                parse(result);
                Log.d("debug",result);
                lock = false;
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            return result;
        }

    }

    public class PostTask extends AsyncTask<Context, String, String> {

        private Context context;

        public void parse(String result) {
            JSONObject jsonObject = null;
            try {
                jsonObject = new JSONObject(result);
//                latitude = jsonObject.getString("lat");
//                longitude = jsonObject.getString("long");
//                valid_until = jsonObject.getString("valid_until");
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        @Override
        protected void onPreExecute() {
            // TODO Auto-generated method stub
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(Context... params) {
            HttpClient httpClient = new DefaultHttpClient(); //Use this instead
            String result = "";
            try {
                HttpPost request = new HttpPost("http://167.205.32.46/pbd/api/catch/");
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("nim","13512061");
                jsonObject.put("token",tokenQRCode);
//                StringEntity param =new StringEntity("details={\"nim\":\"13512061\",\"token\":\""+tokenQRCode+"\"} ");
                StringEntity param = new StringEntity(jsonObject.toString());
                request.addHeader("Content-type", "application/json");
                request.setEntity(param);
                Log.d("debug",jsonObject.toString());
                HttpResponse response = httpClient.execute(request);

                BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));

                String line = "";
                while ((line = rd.readLine()) != null) {
                    result += line;
                }
                parse(result);
                Log.d("debug",result);
                lock = false;

            }catch (Exception ex) {
                // handle exception here
            } finally {
                httpClient.getConnectionManager().shutdown();
                return result;
            }
        }

    }

}
