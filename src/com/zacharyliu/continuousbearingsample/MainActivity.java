package com.zacharyliu.continuousbearingsample;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.zacharyliu.continuousbearingsample.CompassHeading.CompassHeadingListener;
import com.zacharyliu.continuousbearingsample.GpsBearing.GpsBearingListener;
import com.zacharyliu.continuousbearingsample.StepDetector.StepDetectorListener;

public class MainActivity extends Activity {

	private final String TAG = "MainActivity";
	private final double STEP_LENGTH_METERS = 0.8; // http://www.wolframalpha.com/input/?i=step+length+in+meters
	private final double EARTH_RADIUS_KILOMETERS = 6371;
	private double mHeading = 0.0;
	private double mBearing = 0.0;
	private boolean isCalibrating;
	private final int HISTORY_COUNT = 30;
	private Queue<Double> history = new LinkedList<Double>();
	private double historyAvg = 0.0;
	private List<ICustomSensor> sensors = new ArrayList<ICustomSensor>();
	private double corrected;
	private GoogleMap mMap;
	private Marker mMarker;
	private LatLng currentLoc;
	private ColorDrawable startColor = new ColorDrawable(Color.WHITE);
	private ColorDrawable blinkColor = new ColorDrawable(Color.YELLOW);

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		ToggleButton toggle = (ToggleButton) findViewById(R.id.calibrationToggle);
		isCalibrating = toggle.isChecked();
		toggle.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (((ToggleButton) v).isChecked()) {
					isCalibrating = true;
				} else {
					isCalibrating = false;
				}
			}
		});

		sensors.add(new CompassHeading(this, new CompassHeadingListener() {
			@Override
			public void onHeadingUpdate(double heading) {
				mHeading = heading;
				onDirectionUpdate();
			}
		}));
		sensors.add(new GpsBearing(this, new GpsBearingListener() {
			@Override
			public void onBearingUpdate(double bearing) {
				mBearing = bearing;
				onDirectionUpdate();
			}

			@Override
			public void onLocationUpdate(LatLng loc) {
				if (isCalibrating)
					onNewLocation(loc);
			}
		}));
		sensors.add(new StepDetector(this, new StepDetectorListener() {
			@Override
			public void onStep() {
				MainActivity.this.onStep();
			}
		}));

		currentLoc = new LatLng(40.468184, -74.445385);
		mMap = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();
		mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLoc, 19));
		mMarker = mMap.addMarker(new MarkerOptions().position(currentLoc));
		mMap.setMyLocationEnabled(true);
	}

	@Override
	protected void onResume() {
		super.onResume();
		for (ICustomSensor sensor : sensors) {
			sensor.resume();
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		for (ICustomSensor sensor : sensors) {
			sensor.pause();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	private void onDirectionUpdate() {
		if (isCalibrating) {
			double diff = mBearing - mHeading;
			history.add(diff);
			while (history.size() > HISTORY_COUNT) {
				history.remove();
			}
			double sum = 0.0;
			for (double item : history) {
				sum += item;
			}
			historyAvg = sum / history.size();
		}

		corrected = mHeading + historyAvg;
		if (corrected > 360)
			corrected -= 360;
		if (corrected < 0)
			corrected += 360;

		display(R.id.heading, mHeading);
		display(R.id.bearing, mBearing);
		display(R.id.factor, historyAvg);
		display(R.id.corrected, corrected);
	}

	private void onStep() {
		Log.d(TAG, "step");
		AnimationDrawable ani = new AnimationDrawable();
		ani.addFrame(blinkColor, 50);
		ani.addFrame(startColor, 50);
		ani.setOneShot(true);
		findViewById(R.id.main).setBackground(ani);
		ani.run();
		
		if (currentLoc == null || isCalibrating)
			return;

		// Calculate new location
		// Formula: http://www.movable-type.co.uk/scripts/latlong.html#destPoint
		// (angles in radians)
		double lat1 = Math.toRadians(currentLoc.latitude); // starting latitude
		double lon1 = Math.toRadians(currentLoc.longitude); // starting longitude
		double brng = Math.toRadians(corrected); // bearing
		double d = STEP_LENGTH_METERS / 1000; // distance traveled
		double R = EARTH_RADIUS_KILOMETERS; // radius of Earth
		double lat2 = Math.asin(Math.sin(lat1) * Math.cos(d / R)
				+ Math.cos(lat1) * Math.sin(d / R) * Math.cos(brng));
		double lon2 = lon1
				+ Math.atan2(Math.sin(brng) * Math.sin(d / R) * Math.cos(lat1),
						Math.cos(d / R) - Math.sin(lat1) * Math.sin(lat2));
		lat2 = Math.toDegrees(lat2);
		lon2 = Math.toDegrees(lon2);
		
		Log.d(TAG, "delta lat: " + Double.toString(lat2-lat1) + ", delta lon: " + Double.toString(lon2-lon1)); 
		
		LatLng newLoc = new LatLng(lat2, lon2);
		onNewLocation(newLoc);
	}

	private void display(int resId, double value) {
		((TextView) findViewById(resId)).setText(Double.toString(value));
	}

	private void onNewLocation(LatLng loc) {
		currentLoc = loc;
		mMarker.setPosition(loc);
		mMap.animateCamera(CameraUpdateFactory.newLatLng(loc));
	}

}
