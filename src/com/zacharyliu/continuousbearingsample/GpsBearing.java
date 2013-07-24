package com.zacharyliu.continuousbearingsample;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

import com.google.android.gms.maps.model.LatLng;

public class GpsBearing implements ICustomSensor {

	private LocationManager mLocationManager;
	private GpsBearingListener mListener;

	public GpsBearing(Context context, GpsBearingListener listener) {
		mLocationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
		mListener = listener;
	}
	
	public interface GpsBearingListener {
		public void onBearingUpdate(double bearing);
		public void onLocationUpdate(LatLng loc);
	}
	
	private LocationListener mLocationListener = new LocationListener() {
		private float bearing;

		@Override
		public void onLocationChanged(Location location) {
			bearing = location.getBearing();
			mListener.onBearingUpdate(bearing);
			
			LatLng loc = new LatLng(location.getLatitude(), location.getLongitude());
			mListener.onLocationUpdate(loc);
		}

		@Override
		public void onProviderDisabled(String provider) {}

		@Override
		public void onProviderEnabled(String provider) {}

		@Override
		public void onStatusChanged(String provider, int status, Bundle extras) {}
	};
	
	public void resume() {
		mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, mLocationListener);
	}
	
	public void pause() {
		mLocationManager.removeUpdates(mLocationListener);
	}
	
}
