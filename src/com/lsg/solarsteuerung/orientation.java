package com.lsg.solarsteuerung;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

public class orientation extends Activity implements SensorEventListener {

	private orientation_object options = new orientation_object();
	private long id; //id of device
	private String device_name; //name of device
	private PowerManager pm;
	private WakeLock wakelock;
	private boolean screen_on = true;
	private final String WAKELOCK = "WAKELOCK";
	private NotificationManager mNotificationManager;
	//private static final int NOTIFICATION_ID = 97;
	private boolean proximity = false;
	private boolean is_notified = false;
	
	//the sensormangager
	private SensorManager mSensorManager;
    private Sensor orientation_sensor;
    private Sensor proximity_sensor;
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
        setRequestedOrientation(0x00000000); //landscape orientation
        
		super.onCreate(savedInstanceState);
		setContentView(R.layout.orientation);
		
        //get extras (id of device, used in preference file)
        Bundle extras = getIntent().getExtras(); 
		if (extras != null) {
		    id = extras.getLong(db_object.DB_ROWID);
		    options.setId(id);
		    device_name = extras.getString(db_object.DB_DEVICE_NAME);
		    TextView car_label = (TextView) findViewById(R.id.car_label);
		    try {
		    	is_notified = extras.getBoolean("is_notified", false);
		    	Log.d(db_object.TAG, new Boolean(is_notified).toString());
		    }
		    catch(Exception e) {
		    	Log.d(db_object.TAG, getString(R.string.no_notification));
		    }
		    car_label.setText(device_name);
		    setTitle(device_name); //useless
		}
		// NOTE some methods are called in onResume to get the newest data
		//connect textviews
	    this.x        = (TextView) this.findViewById(R.id.orient_x_value);
	    this.y        = (TextView) this.findViewById(R.id.orient_y_value);
	    this.z        = (TextView) this.findViewById(R.id.orient_z_value);
	    this.speed    = (TextView) this.findViewById(R.id.speed_value);
	    this.steering = (TextView) this.findViewById(R.id.steering_value);
	    //sensors

	    mSensorManager     = (SensorManager)getSystemService(SENSOR_SERVICE);
        orientation_sensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);
        proximity_sensor  = mSensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
        final ToggleButton device_control = (ToggleButton) findViewById(R.id.device_control);
        device_control.setChecked(is_notified);
        device_control.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                // Perform action on clicks
                if (device_control.isChecked()) {
                	options.device_control = true;
                	notificate();
                } else {
        	    	stopNotification();
                    options.device_control = false;
                }
            }
        });
        final ToggleButton screen_on_button = (ToggleButton) findViewById(R.id.screen_lock_control);
        screen_on_button.setChecked(true);
        screen_on_button.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                // Perform action on clicks
                if (screen_on_button.isChecked()) {
                	screen_on = true;
        			orientation.this.wakelock = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, WAKELOCK);
        			orientation.this.wakelock.acquire();
                } else {
                    screen_on = false;
            		orientation.this.wakelock.release();
                }
            }
        });
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
			orientation.this.x.setText(new Float(azimuth).toString()+"°");
			orientation.this.y.setText(new Float(pitch).toString()+"°");
			orientation.this.z.setText(new Float(roll).toString()+"°");
			int pwm[] = orientation.this.options.getValues(roll, pitch);
			//echo speed
			orientation.this.speed.setText(new Integer(pwm[0]).toString());
			orientation.this.steering.setText(new Integer(pwm[1]).toString());
			}
		if(event.sensor.getType() == Sensor.TYPE_PROXIMITY) {
			if(event.values[0] > 0.9F) {
				Toast.makeText(getApplicationContext(), getString(R.string.thanks), Toast.LENGTH_SHORT).show();
				proximity = true;
			}
			else {
				if(proximity)
					Toast.makeText(getApplicationContext(), getString(R.string.goaway), Toast.LENGTH_SHORT).show();
				//Toast.makeText(getApplicationContext(), getString(R.string.thanks), Toast.LENGTH_SHORT).show();
			}
		}
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		//don't know what this call is for, so just log it :D
		Log.i("Orientation Sensor accuracy has changed", new Integer(accuracy).toString());
		// TODO Auto-generated method stub
	}

	@Override
	protected void onResume() {

		super.onResume();
        //register sensorlistener
		 mSensorManager.registerListener(this, orientation_sensor, SensorManager.SENSOR_DELAY_NORMAL);
		 mSensorManager.registerListener(this, proximity_sensor,   SensorManager.SENSOR_DELAY_NORMAL);
		//get options
		options.getPrefs(getApplicationContext());
		
		this.pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
		if(screen_on) {
			this.wakelock = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, WAKELOCK);
			this.wakelock.acquire();
		}
	}

	@Override
	protected void onPause() {
		//unregister listener
		if(wakelock.isHeld())
			this.wakelock.release();
		mSensorManager.unregisterListener(this);
		super.onStop();
	}
	public void settings_click(View view)  {
		//intent for settings activity
		Intent settings = new Intent(orientation.this, settings_orientation.class);
		settings.putExtra(db_object.DB_ROWID, id);
		settings.putExtra(db_object.DB_DEVICE_NAME, device_name);
		startActivity(settings);
	}
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.orientation, menu);
	    return true;
	}
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    // Handle item selection
	    switch (item.getItemId()) {
	    case R.id.orientation_home:
	    	stopNotification();
	        Intent intent = new Intent(this, Solarsteuerung.class);
	        startActivity(intent);
	        return true;
	    case R.id.orientation_settings:
			Intent settings = new Intent(orientation.this, settings_orientation.class);
			settings.putExtra(db_object.DB_ROWID, id);
			settings.putExtra(db_object.DB_DEVICE_NAME, device_name);
			startActivity(settings);
	    	return true;
	    default:
	        return super.onOptionsItemSelected(item);
	    }
	}
	private void notificate() {
		if(!is_notified) {
			//orientation.this.mNotificationManager.cancelAll();
			//notification to jump back (especially when bluetooth connection is established)
			String ns = Context.NOTIFICATION_SERVICE;
			mNotificationManager = (NotificationManager) getSystemService(ns);
			//notification to jump back (especially when bluetooth connection is established)
			int icon = R.drawable.solarsteuerung;        // icon from resources
			CharSequence tickerText = getText(R.string.app_name) + ": " + device_name + " " + getText(R.string.running);
			long when = System.currentTimeMillis();         // notification time
			Context context = getApplicationContext();      // application Context
			CharSequence contentTitle = getText(R.string.app_name);  // message title
			CharSequence contentText = device_name + " " + getText(R.string.running);      // message text
			
			Intent notificationIntent = new Intent(this, orientation.class);
			notificationIntent.putExtra(db_object.DB_DEVICE_NAME, device_name);
			notificationIntent.putExtra(db_object.DB_ROWID, id);
			notificationIntent.putExtra(db_object.DB_ROWID, id);
			notificationIntent.putExtra("is_notified", true);
			PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
			
			// the next two lines initialize the Notification, using the configurations above
			Notification notification = new Notification(icon, tickerText, when);
			notification.setLatestEventInfo(context, contentTitle, contentText, contentIntent);
			notification.flags = Notification.FLAG_ONGOING_EVENT;
			mNotificationManager.notify((int) id, notification);
		}
		Log.d(db_object.TAG, new Boolean(is_notified).toString());
		is_notified = true;
	}
	private void stopNotification() {
		Log.d(db_object.TAG, "stop");
		//try {
		if(is_notified) {
			//orientation.this.mNotificationManager.cancelAll();
			int _id = (int) id;
			mNotificationManager.cancel(_id);
		}
		//} catch(Exception e) {Log.e(db_object.TAG, e.getMessage() + "adsf");}
		is_notified = false;
	}
}