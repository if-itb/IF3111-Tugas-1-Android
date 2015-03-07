package id.ac.itb.pbd.pbdtugas1;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;

public class MainActivity extends Activity implements SensorEventListener {

	static private final String ACTION_SCAN = "com.google.zxing.client.android.SCAN",
			NIM = "13512021",
			TRACK_URL = "http://167.205.32.46/pbd/api/track?nim=" + NIM,
			LOG_TAG = "KlongkgungMainActivity";
	static private int SCAN_CODE = 0;

	private GoogleMap mMap;

	private ImageView mPointer;
	private SensorManager mSensorManager;
	private Sensor mAccelerometer;
	private Sensor mMagnetometer;

	private float[] mLastAccelerometer = new float[3];
	private float[] mLastMagnetometer = new float[3];
	private boolean mLastAccelerometerSet = false;
	private boolean mLastMagnetometerSet = false;

	private float[] mR = new float[9];
	private float[] mOrientation = new float[3];
	private float mCurrentDegree = 0f;

	private CountDownTimer countDownTimer;

	private Marker jerry;
	private double latitude, longitude;
	private long validUntil;
	private boolean jerryLocated;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		// setup compass
		mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
		mAccelerometer = mSensorManager
				.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		mMagnetometer = mSensorManager
				.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
		mPointer = (ImageView) findViewById(R.id.pointer);
	}

	protected void onResume() {
		super.onResume();

		setUpMap();
		if (!jerryLocated) {
			Log.i(LOG_TAG, "getJerry from onResume");
			getJerry(null);
		}

		mSensorManager.registerListener(this, mAccelerometer,
				SensorManager.SENSOR_DELAY_UI);
		mSensorManager.registerListener(this, mMagnetometer,
				SensorManager.SENSOR_DELAY_UI);
	}

	protected void onPause() {
		super.onPause();
		mSensorManager.unregisterListener(this, mAccelerometer);
		mSensorManager.unregisterListener(this, mMagnetometer);
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent intent) {
		if (requestCode == SCAN_CODE) {
			if (resultCode == RESULT_OK) {
				// get the extras that are returned from the intent
				String contents = intent.getStringExtra("SCAN_RESULT");

				new ValidateTask(getApplicationContext())
						.execute(NIM, contents);
			}
		}
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		if (event.sensor == mAccelerometer) {
			System.arraycopy(event.values, 0, mLastAccelerometer, 0,
					event.values.length);
			mLastAccelerometerSet = true;
		} else if (event.sensor == mMagnetometer) {
			System.arraycopy(event.values, 0, mLastMagnetometer, 0,
					event.values.length);
			mLastMagnetometerSet = true;
		}

		if (mLastAccelerometerSet && mLastMagnetometerSet) {
			SensorManager.getRotationMatrix(mR, null, mLastAccelerometer,
					mLastMagnetometer);
			SensorManager.getOrientation(mR, mOrientation);

			float azimuthInRadians = mOrientation[0];
			float azimuthInDegress = (float) (Math.toDegrees(azimuthInRadians) + 360) % 360;

			RotateAnimation ra = new RotateAnimation(mCurrentDegree,
					-azimuthInDegress, Animation.RELATIVE_TO_SELF, 0.5f,
					Animation.RELATIVE_TO_SELF, 0.5f);

			ra.setDuration(250);
			ra.setFillAfter(true);

			mPointer.startAnimation(ra);

			mCurrentDegree = -azimuthInDegress;
		}
	}

	/**
	 * This is where we can add markers or lines, add listeners or move the
	 * camera. In this case, we just add a marker near Africa.
	 * <p>
	 * This should only be called once and when we are sure that {@link #mMap}
	 * is not null.
	 */
	public void setUpMap() {
		if (mMap == null) {
			mMap = ((MapFragment) getFragmentManager().findFragmentById(
					R.id.map)).getMap();
			// Check if we were successful in obtaining the map.
			if (mMap != null) {
				mMap.setMyLocationEnabled(true);
				setUpMap();
			}

		} else {
			if (jerry != null) {
				jerry.remove();
			}

			LatLng latLng = new LatLng(latitude, longitude);
			jerry = mMap.addMarker(new MarkerOptions().position(latLng).title(
					"Jerry"));
			mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
			mMap.animateCamera(CameraUpdateFactory.zoomTo(15));
		}
	}

	public void getJerry(View v) {
		new RequestTask(this).execute(TRACK_URL);
	}

	public void setTimer() {

		long time = 1000L * validUntil - System.currentTimeMillis();
		if (countDownTimer != null)
			countDownTimer.cancel();

		countDownTimer = new CountDownTimer(time, 1000) {

			@Override
			public void onTick(long millisUntilFinished) {
			}

			@Override
			public void onFinish() {
				Log.i(LOG_TAG, "getJerry from setTimerIfNeeded");
				getJerry(null);
			}
		};
		countDownTimer.start();
		Log.i(LOG_TAG, "Timer set " + time / 1000L + " seconds remaining");

	}

	public void scanQR(View v) {
		try {
			// start the scanning activity from the
			// com.google.zxing.client.android.SCAN intent
			Intent intent = new Intent(ACTION_SCAN);
			intent.putExtra("SCAN_MODE", "QR_CODE_MODE");
			startActivityForResult(intent, SCAN_CODE);

		} catch (ActivityNotFoundException anfe) {
			// on catch, show the download dialog
			showDialog(this, "No Scanner Found",
					"Download a scanner code activity?", "Yes", "No").show();
		}
	}

	public void setLatitude(double val) {
		latitude = val;
	}

	public void setLongitude(double val) {
		longitude = val;
	}

	public void setJerryLocated(boolean val) {
		jerryLocated = val;
	}

	public void setValidUntil(long val) {
		validUntil = val - 7 * 3600;
	}

	private static AlertDialog showDialog(final Activity act,
			CharSequence title, CharSequence message, CharSequence buttonYes,
			CharSequence buttonNo) {

		AlertDialog.Builder downloadDialog = new AlertDialog.Builder(act);

		downloadDialog.setTitle(title);
		downloadDialog.setMessage(message);

		downloadDialog.setPositiveButton(buttonYes,

		new DialogInterface.OnClickListener() {

			public void onClick(DialogInterface dialogInterface, int i) {
				Uri uri = Uri.parse("market://search?q=pname:"
						+ "com.google.zxing.client.android");
				Intent intent = new Intent(Intent.ACTION_VIEW, uri);

				try {
					act.startActivity(intent);
				} catch (ActivityNotFoundException anfe) {
				}
			}
		});

		downloadDialog.setNegativeButton(buttonNo,

		new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialogInterface, int i) {
			}
		});

		return downloadDialog.show();
	}
}