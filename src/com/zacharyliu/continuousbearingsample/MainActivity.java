package com.zacharyliu.continuousbearingsample;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
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
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.zacharyliu.stepnavigation.StepNavigationService;
import com.zacharyliu.stepnavigation.StepNavigationService.StepNavigationBinder;
import com.zacharyliu.stepnavigation.StepNavigationService.StepNavigationMultiListener;

public class MainActivity extends Activity implements StepNavigationMultiListener {
	private final String TAG = "MainActivity";
	private GoogleMap mMap;
	private Marker mMarker;
	private Polyline mLine;
	private List<LatLng> locations = new ArrayList<LatLng>();
	private StepNavigationService mService;
//	private CSVWriter writer;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		mMap = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();
		mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(40.468184, -74.445385), 19));
		mMap.setMyLocationEnabled(true);
		
//		String filename = Environment.getExternalStorageDirectory().getAbsolutePath() + "/CompassHeading" + Long.toString(System.currentTimeMillis()) + ".csv";
//		Toast.makeText(this, "Logging to: " + filename, Toast.LENGTH_SHORT).show();
//		try {
//			writer = new CSVWriter(new FileWriter(filename));
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
		
		Log.d(TAG, "Connecting to service");
		Intent intent = new Intent(this, StepNavigationService.class);
		bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
	}
	
	ServiceConnection mConnection = new ServiceConnection() {
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			mService = ((StepNavigationBinder) service).getService();
			mService.register(MainActivity.this, StepNavigationService.TYPE_LOCATION);
			mService.register(new StepNavigationMultiListener() {
				@Override
				public void onSensorChanged(int type, double[] values) {
//					writer.writeNext(new String[] {Long.toString(System.currentTimeMillis()), "step"});
				}
			}, StepNavigationService.TYPE_STEP);
		}
		@Override
		public void onServiceDisconnected(ComponentName name) {}
	};
	
	@Override
	public void onSensorChanged(int type, double[] values) {
		if (type == StepNavigationService.TYPE_LOCATION)
			newLocation(new LatLng(values[0], values[1]));
	}
	
	@Override
	protected void onDestroy() {
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

	private void newLocation(LatLng loc) {
		Log.d(TAG, "Got new location");
		if (mMarker == null) {
			mMarker = mMap.addMarker(new MarkerOptions().position(loc));
		} else {
			mMarker.setPosition(loc);
		}
		
		locations.add(loc);
		
		if (mLine == null) {
			mLine = mMap.addPolyline(new PolylineOptions().color(Color.RED).addAll(locations));
		} else {
			mLine.setPoints(locations);
		}
		
		mMap.animateCamera(CameraUpdateFactory.newLatLng(loc));
	}
}
