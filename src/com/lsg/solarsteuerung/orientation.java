package com.lsg.solarsteuerung;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

public class orientation extends Activity implements SensorEventListener {

	orientation_object options = new orientation_object();
	
	/*int   dead_angle_speed;
	int   dead_angle_steering;
	float multiplicator_speed;
	float multiplicator_steering;*/
	private PowerManager pm;
	WakeLock wakelock;
	//the sensormangager
	private SensorManager sensorManager;
	//some textviews to change content later
	TextView x;
	TextView y;
	TextView z;
	TextView steering;
	TextView speed;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_NO_TITLE); //no title
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
	            WindowManager.LayoutParams.FLAG_FULLSCREEN); //fullscreen
        setRequestedOrientation(0x00000000); //landscape
	  
		super.onCreate(savedInstanceState);
		setContentView(R.layout.orientation);
		// NOTE: some methods are called in onResume to get the newest data
		//connect textviews
	    this.x        = (TextView) this.findViewById(R.id.orient_x_value);
	    this.y        = (TextView) this.findViewById(R.id.orient_y_value);
	    this.z        = (TextView) this.findViewById(R.id.orient_z_value);
	    this.speed    = (TextView) this.findViewById(R.id.speed_value);
	    this.steering = (TextView) this.findViewById(R.id.steering_value);
        //connect sensormanager
		sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
		//listener is registered in onResume()
	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		if (event.sensor.getType() == Sensor.TYPE_ORIENTATION) {
			float[] values = event.values;
			// Movement
			float azimuth = values[0]; //degree to north
			float pitch = values[1];
			float roll = values[2];
			//set values in text views
			orientation.this.x.setText(new Float(azimuth).toString());
			orientation.this.y.setText(new Float(pitch).toString());
			orientation.this.z.setText(new Float(roll).toString());

			/*if(pitch < orientation.this.dead_angle_steering && pitch > -orientation.this.dead_angle_steering) {
				pitch = 0; //dead angle
			}
			else {
				if(pitch > 0)
				{
					pitch -= orientation.this.dead_angle_steering; //remove dead angle
				}
				else {
					pitch += orientation.this.dead_angle_steering; //remove dead angle
				}
			}
			//same again for steering
			if(roll < orientation.this.dead_angle_speed && roll > -orientation.this.dead_angle_speed) {
				roll = 0;
			}
			else {
				if(roll > 0) {
					roll -= orientation.this.dead_angle_speed;
				}
				else {
					roll += orientation.this.dead_angle_speed;
				}
			}
			int speed = (int)(roll * orientation.this.multiplicator_speed + 150);
			int steering = (int)(pitch * orientation.this.multiplicator_steering + 150);
			//set limits: 100 - 200
			if(speed > 200) {
				speed = 200;
			}
			if(speed < 100) {
				speed = 100;
			}
			if(steering < 100) {
				steering = 100;
			}
			if(steering > 200) {
				steering = 200;
			}*/
			int pwm[] = orientation.this.options.getPWM(roll, pitch);
			//echo speed
			orientation.this.speed.setText(new Integer(pwm[0]).toString());
			orientation.this.steering.setText(new Integer(pwm[1]).toString());
		}

	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// TODO Auto-generated method stub
        //don`t need this
	}

	@Override
	protected void onResume() {
		//register this class as a listener for the orientation sensor
		sensorManager.registerListener(this,
				sensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION),
				SensorManager.SENSOR_DELAY_NORMAL);
		//get options
		options.getPrefs(getApplicationContext());
		/*this.dead_angle_speed       = options.dead_angle_speed;
		this.dead_angle_steering    = options.dead_angle_steering;
		this.multiplicator_speed    = options.multiplicator_speed;
		this.multiplicator_steering = options.multiplicator_steering;*/ 
		super.onResume();
		this.pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        this.wakelock = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, "My Tag");
        this.wakelock.acquire();
	}

	@Override
	protected void onPause() {
		//unregister listener
		this.wakelock.release();
		sensorManager.unregisterListener(this);
		super.onStop();
	}
	public void settings_click(View view)  {
		//intent for settings activity
		Intent settings = new Intent(orientation.this, settings_orientation.class);
		startActivity(settings);
	}
}