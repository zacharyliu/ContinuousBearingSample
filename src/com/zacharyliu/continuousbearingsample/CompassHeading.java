package com.zacharyliu.continuousbearingsample;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

public class CompassHeading implements ICustomSensor {
	
	private SensorManager mSensorManager;
	private Sensor accelerometer;
	private Sensor magnetometer;
	private CompassHeadingListener mListener;

	public CompassHeading(Context context, CompassHeadingListener listener) {
		mSensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
		accelerometer = mSensorManager
				.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		magnetometer = mSensorManager
				.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
		mListener = listener;
	}
	
	public interface CompassHeadingListener {
		public void onHeadingUpdate(double heading);
	}
	
	private SensorEventListener mSensorEventListener = new SensorEventListener() {
		private float[] accelReadings;
		private float[] magnetReadings;
		private double azimuth;
		private boolean azimuthReady;

		@Override
		public void onAccuracyChanged(Sensor sensor, int accuracy) {}

		@Override
		public void onSensorChanged(SensorEvent event) {
			switch (event.sensor.getType()) {
				case Sensor.TYPE_ACCELEROMETER:
					accelReadings = event.values.clone();
					break;
				case Sensor.TYPE_MAGNETIC_FIELD:
					magnetReadings = event.values.clone();
					break;
			}
			if (accelReadings != null && magnetReadings != null) {
				float[] R = new float[9];
				float[] I = new float[9];
				boolean success = SensorManager.getRotationMatrix(R, I, accelReadings, magnetReadings);
				if (success) {
					float[] values = new float[3];
					SensorManager.getOrientation(R, values);
					azimuth = Math.toDegrees(values[0]);
					if (azimuth < 0) {
						azimuth += 360.0;
					}
					if (!azimuthReady) azimuthReady = true;
					mListener.onHeadingUpdate(azimuth);
				}
			}
		}
	};
	
	public void resume() {
		mSensorManager.registerListener(mSensorEventListener, accelerometer,
				SensorManager.SENSOR_DELAY_UI);
		mSensorManager.registerListener(mSensorEventListener, magnetometer,
				SensorManager.SENSOR_DELAY_UI);
	}
	
	public void pause() {
		mSensorManager.unregisterListener(mSensorEventListener);
	}
}
