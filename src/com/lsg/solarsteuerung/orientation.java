package com.lsg.solarsteuerung;

import java.util.ArrayList;

import android.app.ActionBar;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.gesture.Gesture;
import android.gesture.GestureLibraries;
import android.gesture.GestureLibrary;
import android.gesture.GestureOverlayView;
import android.gesture.GestureOverlayView.OnGesturePerformedListener;
import android.gesture.Prediction;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.os.RemoteException;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.ToggleButton;

public class orientation extends Activity implements SensorEventListener, OnGesturePerformedListener {

	private boolean service_bound;
	Messenger mService = null;
	
	class IncomingHandler extends Handler {
	    @Override
	    public void handleMessage(Message msg) {
	    	Bundle info = msg.getData();
	        switch (info.getInt(orientation_object.act)) {
	        case orientation_object.sendValues:
				orientation.this.speed.setText(new Integer(info.getInt(orientation_object.speed)).toString());
				orientation.this.steering.setText(new Integer(info.getInt(orientation_object.steering)).toString());
	        	break;
	            default:
	            	Log.d(db_object.TAG, "unhandled data");
	                super.handleMessage(msg);
	        }
	    }
	}
	
	final Messenger mMessenger = new Messenger(new IncomingHandler());
	private ServiceConnection mConnection = new ServiceConnection() {
	    public void onServiceConnected(ComponentName className,
	            IBinder service) {
	        mService = new Messenger(service);
	        try {
	        	Message msg = new Message();
	        	msg = Message.obtain(null, orientation_object.register);
	        	msg.replyTo = mMessenger;
	        	mService.send(msg);
	        } catch(RemoteException e) {
	        	
	        }
	    }

	    public void onServiceDisconnected(ComponentName className) {
	    	//service disconnected
	        mService = null;
	    }
	};
	
	private long id; //id of device
	private String device_name; //name of device
	private WakeLock wakelock;
	private boolean screen_on = true;
	private final String WAKELOCK = "WAKELOCK";
	
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
	
	ToggleButton device_control;
	
	//gestures
	private GestureLibrary gestLib;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		if(Build.VERSION.SDK_INT < 11)
			requestWindowFeature(Window.FEATURE_NO_TITLE); //no title
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
	            WindowManager.LayoutParams.FLAG_FULLSCREEN); //fullscreen
        setRequestedOrientation(0x00000000); //landscape orientation
        
        db_object.setTheme(false, this);
        
		super.onCreate(savedInstanceState);

		GestureOverlayView gestureOverlayView = new GestureOverlayView(this);
		View inflate = getLayoutInflater().inflate(R.layout.orientation, null);
		gestureOverlayView.addView(inflate);
		gestureOverlayView.addOnGesturePerformedListener(this);
		gestureOverlayView.setGestureColor(Color.TRANSPARENT);
		gestureOverlayView.setUncertainGestureColor(Color.TRANSPARENT);
		gestLib = GestureLibraries.fromRawResource(this, R.raw.gestures);
		gestLib.load();
		setContentView(gestureOverlayView);
		
        //get extras (id of device, used in preference file)
        Bundle extras = getIntent().getExtras(); 
		if (extras != null) {
		    id = extras.getLong(db_object.DB_ROWID);
		    device_name = extras.getString(db_object.DB_DEVICE_NAME);
		    TextView car_label = (TextView) findViewById(R.id.car_label);
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
        doBindService();
        
        device_control = (ToggleButton) findViewById(R.id.device_control);
        device_control.setChecked(false);
        device_control.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
            	pauseService(device_control.isChecked());
            }
        });
        
        final ToggleButton screen_on_button = (ToggleButton) findViewById(R.id.screen_lock_control);
        screen_on_button.setChecked(true);
        screen_on_button.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                //set screen on
            	screen_on = screen_on_button.isChecked();
            	setScreen(screen_on_button.isChecked());
            }
        });
		PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        wakelock = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, WAKELOCK);
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
			//Message -> send data to service
			Bundle info = new Bundle();
			info.putFloat(orientation_object.pitch, pitch);
			info.putFloat(orientation_object.roll,  roll);
			info.putInt(orientation_object.act,     orientation_object.sendOrientation);
			sendData(info);
			//the textviews with speed values are filled in the reply of the above message
			}
		if(event.sensor.getType() == Sensor.TYPE_PROXIMITY && !android.os.Build.MODEL.equals("google_sdk")) {
			if(event.values[0] == 1.0F) {
				//no proximity
				}
			else {
				//proximity
				}
			}
		}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		//don't know what this call is for, so just log it :D
		Log.i("Orientation Sensor accuracy has changed", new Integer(accuracy).toString());
	}

	@Override
	protected void onResume() {
		//start service recording
		pauseService(device_control.isChecked());
		super.onResume();
        //register sensorlistener
		 mSensorManager.registerListener(this, orientation_sensor, SensorManager.SENSOR_DELAY_NORMAL);
		 mSensorManager.registerListener(this, proximity_sensor,   SensorManager.SENSOR_DELAY_NORMAL);
		 //screen wakelock
		 setScreen(screen_on);
	}

	@Override
	protected void onPause() {
		//pause the service
		pauseService(false);
		//let device control screen
		setScreen(false);
		//unregister listener
		mSensorManager.unregisterListener(this);
		super.onStop();
	}
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.orientation, menu);
	    return true;
	}
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Intent homeIntent = new Intent(this, Solarsteuerung.class);
	    // Handle item selection
	    switch (item.getItemId()) {
        case android.R.id.home:
            // app icon in action bar clicked; change mode
        	change_mode(null);
            return true;
	    case R.id.orientation_home:
	        startActivity(homeIntent);
	        return true;
	    case R.id.stop_service:
    		try {
    			Message msg = Message.obtain(null, orientation_object.exit);
    			mService.send(msg);
    			} catch (RemoteException e) {
    				//what the heck is going on with the service???
    				}
	    	//stopService(new Intent(this, orientation_object.class));
	        startActivity(homeIntent);
	        return true;
	    case R.id.orientation_settings:
			settings();
	    	return true;
	    default:
	        return super.onOptionsItemSelected(item);
	    }
	}
	@Override
	protected void onDestroy() {
	    super.onDestroy();
	    if (service_bound) {
	    	if (mService != null) {
	    		try {
	    			Message msg = Message.obtain(null, orientation_object.unregister);
	    			mService.send(msg);
	    			} catch (RemoteException e) {
	    				//what the heck is going on with the service???
	    				}
	    		}
	    	}
	    unbindService(mConnection);
	    service_bound = false;
	    }
	public void settings()  {
		//intent for settings activity
		Intent settings = new Intent(orientation.this, settings_orientation.class);
		settings.putExtra(db_object.DB_ROWID, id);
		settings.putExtra(db_object.DB_DEVICE_NAME, device_name);
		startActivity(settings);
	}
	public void change_mode(View view) {
        Intent intent = new Intent(this, device_options.class);
        intent.putExtra(db_object.DB_DEVICE_NAME, device_name);
        intent.putExtra(db_object.DB_ROWID, id);
        startActivity(intent);
	}
	private void pauseService(boolean device_control_val) {
		//Message -> (de)activate control of device
		Bundle info = new Bundle();
		info.putBoolean(orientation_object.DEVICE_CONTROL_KEY, device_control_val);
		info.putInt(orientation_object.act,                    orientation_object.DEVICE_CONTROL);
		sendData(info);
		}
	void doBindService() {
		Intent serviceIntent = new Intent(this, orientation_object.class);
		/*
		 * startService is needed to call onStartCommand of Service
		 * that is needed to return START_STICKY
		 * which makes the Service don't stop even if the calling activity is killed
		 * (back button)
		 * hope that my Explanation is right :D */
		serviceIntent.putExtra(db_object.DB_ROWID, id);
		serviceIntent.putExtra(db_object.DB_DEVICE_NAME, device_name);
		serviceIntent.putExtra(orientation_object.act, orientation_object.sendInitData);
		startService(serviceIntent);
		if(bindService(serviceIntent, mConnection, Context.BIND_AUTO_CREATE))
			service_bound = true;
		}
	private void setScreen(boolean on) {
		if(screen_on && !wakelock.isHeld()) {
			wakelock.acquire();
		}
		else {
			if(wakelock.isHeld())
				wakelock.release();
		}
		}
	private void sendData(Bundle data) {
		try {
			//Message -> (de)activate control of device
			Message msg = new Message();
			msg.setData(data);
			orientation.this.mService.send(msg);
			} catch (Exception e) {
				//the service did some bullshit
				}
		}

	@Override
	public void onGesturePerformed(GestureOverlayView overlay, Gesture gesture) {
		ArrayList<Prediction> predictions = gestLib.recognize(gesture);
		for (Prediction prediction : predictions) {
			if (prediction.score > 1.0) {
				if(prediction.name.equals("right")) {
					Intent intent = new Intent(this, draw.class);
					startActivity(intent);
					}
				if(prediction.name.equals("left")) {
					settings();
					}
				}
			}
		}
	}