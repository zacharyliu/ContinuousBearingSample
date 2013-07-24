package com.zacharyliu.continuousbearingsample;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
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
import com.zacharyliu.stepnavigation.StepNavigationService;
import com.zacharyliu.stepnavigation.StepNavigationService.StepNavigationBinder;
import com.zacharyliu.stepnavigation.StepNavigationService.StepNavigationListener;

public class MainActivity extends Activity implements StepNavigationListener {

	private final String TAG = "MainActivity";
	private boolean isCalibrating;
	private GoogleMap mMap;
	private Marker mMarker;
	private LatLng currentLoc;

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

		currentLoc = new LatLng(40.468184, -74.445385);
		mMap = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();
		mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLoc, 19));
		mMarker = mMap.addMarker(new MarkerOptions().position(currentLoc));
		mMap.setMyLocationEnabled(true);
	}
	
	ServiceConnection mConnection = new ServiceConnection() {
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			Log.d(TAG, "Service connected");
			StepNavigationService stepService = ((StepNavigationBinder) service).getService();
			stepService.register(MainActivity.this);
		}
		@Override
		public void onServiceDisconnected(ComponentName name) {}
	};

	@Override
	protected void onStart() {
		super.onStart();
		Log.d(TAG, "Connecting to service");
		Intent intent = new Intent(this, StepNavigationService.class);
		bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		unbindService(mConnection);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	private void display(int resId, double value) {
		((TextView) findViewById(resId)).setText(Double.toString(value));
	}

	private void onNewLocation(LatLng loc) {
		Log.d(TAG, "Got new location");
		currentLoc = loc;
		mMarker.setPosition(loc);
		mMap.animateCamera(CameraUpdateFactory.newLatLng(loc));
	}

	@Override
	public void onLocationUpdate(double latitude, double longitude) {
		onNewLocation(new LatLng(latitude, longitude));
	}

}
