package com.lsg.solarsteuerung;

import android.app.Activity;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

public class orientation extends Activity implements SensorEventListener {
	private SensorManager sensorManager;
	TextView x;
	TextView y;
	TextView z;
	TextView steering;
	TextView speed;
/** Called when the activity is first created. */

	@Override
	public void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
	            WindowManager.LayoutParams.FLAG_FULLSCREEN);
	  
		super.onCreate(savedInstanceState);
		setContentView(R.layout.orientation);
	    this.x        = (TextView) this.findViewById(R.id.orient_x_value);
	    this.y        = (TextView) this.findViewById(R.id.orient_y_value);
	    this.z        = (TextView) this.findViewById(R.id.orient_z_value);
	    this.speed    = (TextView) this.findViewById(R.id.speed_value);
	    this.steering = (TextView) this.findViewById(R.id.steering_value);

		sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
		/*sensorManager.registerListener(this,
				sensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION),
				SensorManager.SENSOR_DELAY_NORMAL);*/
	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		if (event.sensor.getType() == Sensor.TYPE_ORIENTATION) {
			float[] values = event.values;
			// Movement
			float azimuth = values[0];
			float pitch = values[1];
			float roll = values[2];
			
			int speed = (int)(roll * 1.6F);
			int steering = (int)(pitch * 1.6F);
			if(speed > 100) {
				speed = 100;
			}
			if(steering > 100) {
				speed = 100;
			}
			orientation.this.x.setText(new Float(azimuth).toString());
			orientation.this.y.setText(new Float(pitch).toString());
			orientation.this.z.setText(new Float(roll).toString());
			orientation.this.speed.setText(new Integer(speed).toString());
			orientation.this.steering.setText(new Integer(steering).toString());
            //Toast.makeText(getApplicationContext(), "Azimuth: " + new Float(azimuth).toString() +"Pitch " + new Float(pitch).toString() +"roll: " + new Float(roll).toString(), Toast.LENGTH_SHORT).show();
		}

	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// TODO Auto-generated method stub

	}

	@Override
	protected void onResume() {
		super.onResume();
		// register this class as a listener for the orientation and
		// accelerometer sensors
		sensorManager.registerListener(this,
				sensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION),
				SensorManager.SENSOR_DELAY_NORMAL);
	}

	@Override
	protected void onPause() {
		// unregister listener
		sensorManager.unregisterListener(this);
		super.onStop();
	}
}