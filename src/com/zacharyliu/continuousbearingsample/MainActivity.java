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
	private GoogleMap mMap;
	private Marker mMarker;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		mMap = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();
		mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(40.468184, -74.445385), 19));
		mMarker = mMap.addMarker(new MarkerOptions().position(new LatLng(40.468184, -74.445385)));
		mMap.setMyLocationEnabled(true);
	}
	
	ServiceConnection mConnection = new ServiceConnection() {
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			((StepNavigationBinder) service).getService().register(MainActivity.this);
		}
		@Override
		public void onServiceDisconnected(ComponentName name) {}
	};
	
	@Override
	public void onLocationUpdate(double latitude, double longitude) {
		onNewLocation(new LatLng(latitude, longitude));
	}
	
	@Override
	protected void onStart() {
		super.onStart();
		Log.d(TAG, "Connecting to service");
		Intent intent = new Intent(this, StepNavigationService.class);
		bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
	};
	
	@Override
	protected void onStop() {
		super.onDestroy();
		Log.d(TAG, "Disconnecting from service");
		unbindService(mConnection);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	private void onNewLocation(LatLng loc) {
		Log.d(TAG, "Got new location");
		mMarker.setPosition(loc);
		mMap.animateCamera(CameraUpdateFactory.newLatLng(loc));
	}
}
