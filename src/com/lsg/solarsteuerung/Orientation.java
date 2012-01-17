package com.lsg.solarsteuerung;

import java.util.ArrayList;

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
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ToggleButton;

public class Orientation extends Activity implements SensorEventListener, OnGesturePerformedListener {

	private boolean service_bound;
	Messenger mService = null;
	
	class IncomingHandler extends Handler {
	    @Override
	    public void handleMessage(Message msg) {
	    	Bundle info = msg.getData();
	        switch (info.getInt(BluetoothService.act)) {
	        case BluetoothService.sendValues:
				Orientation.this.speed.setText(new Integer(info.getInt(BluetoothService.speed)).toString());
				Orientation.this.steering.setText(new Integer(info.getInt(BluetoothService.steering)).toString());
	        	break;
	            default:
	            	Log.d(HelperClass.TAG, "unhandled data");
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
	        	msg = Message.obtain(null, BluetoothService.register);
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
        
        HelperClass.setTheme(false, this);
        
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
		    id = extras.getLong(HelperClass.DB_ROWID);
		    device_name = extras.getString(HelperClass.DB_DEVICE_NAME);
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
        //on android with no actionbar display textview to switch mode
        if(Build.VERSION.SDK_INT < 11) {
        	Button change_mode = new Button(this);
        	change_mode.setText(getString(R.string.change_mode));
        	change_mode.setLayoutParams((new ViewGroup.LayoutParams(-1, -2)));
        	change_mode.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					change_mode(v);
				}
			});
        	LinearLayout container = (LinearLayout) findViewById(R.id.change_mode_container);
        	container.addView(change_mode);
        }
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
			Orientation.this.x.setText(new Float(azimuth).toString()+"°");
			Orientation.this.y.setText(new Float(pitch).toString()+"°");
			Orientation.this.z.setText(new Float(roll).toString()+"°");
			//Message -> send data to service
			Bundle info = new Bundle();
			info.putFloat(BluetoothService.pitch, pitch);
			info.putFloat(BluetoothService.roll,  roll);
			info.putInt(BluetoothService.act,     BluetoothService.sendOrientation);
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
		homeIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
	    // Handle item selection
	    switch (item.getItemId()) {
        case android.R.id.home:
            // app icon in action bar clicked; change mode
        	change_mode(null);
            return true;
	    case R.id.orientation_home:
	        startActivity(homeIntent);
	        finish();
	        return true;
	    case R.id.stop_service:
    		try {
    			Message msg = Message.obtain(null, BluetoothService.exit);
    			mService.send(msg);
    			} catch (RemoteException e) {
    				//what the heck is going on with the service???
    				}
	    	//stopService(new Intent(this, orientation_object.class));
	        startActivity(homeIntent);
	        finish();
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
	    			Message msg = Message.obtain(null, BluetoothService.unregister);
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
		Intent settings = new Intent(Orientation.this, SettingsOrientation.class);
		settings.putExtra(HelperClass.DB_ROWID, id);
		settings.putExtra(HelperClass.DB_DEVICE_NAME, device_name);
		startActivity(settings);
	}
	public void change_mode(View view) {
        Intent intent = new Intent(this, DeviceOptions.class);
        intent.putExtra(HelperClass.DB_DEVICE_NAME, device_name);
        intent.putExtra(HelperClass.DB_ROWID, id);
        intent.putExtra(HelperClass.caller, R.string.sensor);
        startActivity(intent);
	}
	private void pauseService(boolean device_control_val) {
		//Message -> (de)activate control of device
		Bundle info = new Bundle();
		info.putBoolean(BluetoothService.DEVICE_CONTROL_KEY, device_control_val);
		info.putInt(BluetoothService.act,                    BluetoothService.DEVICE_CONTROL);
		sendData(info);
		}
	void doBindService() {
		Intent serviceIntent = new Intent(this, BluetoothService.class);
		/*
		 * startService is needed to call onStartCommand of Service
		 * that is needed to return START_STICKY
		 * which makes the Service don't stop even if the calling activity is killed
		 * (back button)
		 * hope that my Explanation is right :D */
		serviceIntent.putExtra(HelperClass.DB_ROWID, id);
		serviceIntent.putExtra(HelperClass.DB_DEVICE_NAME, device_name);
		serviceIntent.putExtra(BluetoothService.act, BluetoothService.sendInitData);
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
			Orientation.this.mService.send(msg);
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
					Intent intent = new Intent(this, Draw.class);
					startActivity(intent);
					}
				if(prediction.name.equals("left")) {
					settings();
					}
				}
			}
		}
	}