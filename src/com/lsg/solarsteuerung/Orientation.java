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
	private boolean connected;
	Messenger mService = null;
	private int[] caps;
	
	class IncomingHandler extends Handler {
	    @Override
	    public void handleMessage(Message msg) {
	    	Bundle info = msg.getData();
	        switch (info.getInt(BluetoothService.act)) {
	        case BluetoothService.sendValues:
				Orientation.this.speed.setText(new Integer(info.getInt(BluetoothService.speed)).toString());
				Orientation.this.steering.setText(new Integer(info.getInt(BluetoothService.steering)).toString());
	        	break;
	        case BluetoothService.getCapabilities:
	        	int [] capabilities = info.getIntArray(BluetoothService.capabilities);
	        	caps = capabilities;
	        	if(Build.VERSION.SDK_INT >= 11)
	        		HelperClass.makeNavigation(capabilities, Orientation.this);
	        	break;
	        case BluetoothService.connected:
	        	connected = info.getBoolean(BluetoothService.connect_state);
	        	TextView conn = (TextView) findViewById(R.id.connected);
	        	if(connected) {
	        		conn.setText(R.string.connected);
	        		conn.setTextColor(getResources().getColor(R.color.darkgreen));
	        	}
	        	else {
	        		conn.setText(getString(R.string.not_connected));
	        		conn.setTextColor(getResources().getColor(R.color.darkred));
	        	}
	        	Log.d("connected", new Boolean(connected).toString());
	        	break;
	            default:
	            	Log.d("asdf", "unhandled data");
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
	        	msg = Message.obtain(null, BluetoothService.getCapabilities, null);
	        	mService.send(msg );
	        	msg = Message.obtain(null, BluetoothService.connected, null);
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
	private String BTDevice_mac;
	private WakeLock wakelock;
	private boolean screen_on = true;
	private final String WAKELOCK = "WAKELOCK";
	
	//the sensormangager
	private SensorManager mSensorManager;
	//sensors for honeycomb and above
    private Sensor mag_sensor;
    private Sensor acc_sensor;
    //sensor for gingerbread etc
    private Sensor orient_sensor;
	//some textviews to change content later
	TextView x;
	TextView y;
	TextView z;
	TextView steering;
	TextView speed;
	
	ToggleButton device_control;
	
	public String rad;
	
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
		
		rad = getString(R.string.rad);
		
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
		    BTDevice_mac = extras.getString(HelperClass.DB_DEVICE_MAC);
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
	    if(Build.VERSION.SDK_INT >= 11) {
	    	acc_sensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
	    	mag_sensor  = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
	    	}
	    else
	    	orient_sensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);
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
	private float[] acc_values;
	private float[] mag_values;
	private boolean mag_ready = false;
	private boolean acc_ready = false;
	@Override
	public void onSensorChanged(SensorEvent event) {
		//these two sensor are for ics devices
		switch (event.sensor.getType()) {
	    case Sensor.TYPE_MAGNETIC_FIELD:
	        mag_values = event.values.clone();
	        mag_ready = true;
	        break;
	    case Sensor.TYPE_ACCELEROMETER:
	        acc_values = event.values.clone();
	        acc_ready = true;
	    }   

	    if (mag_values != null && acc_values != null && mag_ready && acc_ready) {
	        mag_ready = false;
	        acc_ready = false;

	        float[] R = new float[16];
	        float[] I = new float[16];

	        SensorManager.getRotationMatrix(R, I, this.acc_values, this.mag_values);

	        float[] orientation = new float[3];
	        SensorManager.getOrientation(R, orientation);
			float azimuth = orientation[0]; //degree to north
			float pitch = orientation[1];
			float roll = orientation[2];
			//set values in text views
			Orientation.this.x.setText(new Float(azimuth).toString() + " " + rad);
			Orientation.this.y.setText(new Float(pitch).toString()   + " " + rad);
			Orientation.this.z.setText(new Float(roll).toString()    + " " + rad);
			//Message -> send data to service
			Bundle info = new Bundle();
			info.putFloat(BluetoothService.pitch, (pitch*(180/3.14F)));
			info.putFloat(BluetoothService.roll,  (roll*(180/3.14F)));
			info.putInt(BluetoothService.act,     BluetoothService.sendOrientation);
			sendData(info);
			//the textviews with speed values are filled in the reply of the above message
	        }
	    //put this again in code to have better speed on gingerbread (cyonagenmod 7.2)
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
		}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		//don't know what this call is for, so just log it :D
		Log.i("Orientation Sensor accuracy has changed", new Integer(accuracy).toString());
	}

	@Override
	protected void onResume() {
		super.onResume();
		//start service recording
		pauseService(device_control.isChecked());
        //register sensorlistener
		if(Build.VERSION.SDK_INT >= 11) {
			mSensorManager.registerListener(this, acc_sensor, SensorManager.SENSOR_DELAY_NORMAL);
			mSensorManager.registerListener(this, mag_sensor, SensorManager.SENSOR_DELAY_NORMAL);
			}
		else
			mSensorManager.registerListener(this, orient_sensor, SensorManager.SENSOR_DELAY_NORMAL);
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
	    if(Build.VERSION.SDK_INT < 11) {
	    	menu.add(0, android.R.id.home, Menu.NONE, "Home").setIcon(android.R.drawable.ic_menu_revert);
	    }
	    return true;
	}
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
	    if(connected) {
	    	menu.removeItem(R.id.connect_BT);
	    }
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
        	startActivity(homeIntent);
        	finish();
            return true;
	    case R.id.connect_BT:
	        Message msg_en = new Message();
	        msg_en = Message.obtain(null, BluetoothService.connect, null);
	        try {
	        	mService.send(msg_en);
	        } catch(RemoteException e) {}
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
		serviceIntent.putExtra(HelperClass.DB_DEVICE_MAC, BTDevice_mac);
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