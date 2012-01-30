package com.lsg.solarsteuerung;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.UUID;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;
import android.widget.Toast;

public class BluetoothService extends Service {
	private final static String TAG = "BluetoothService";
	//stuff for the calculation
	private String PREFERENCES;
	private long id;
	public boolean device_control = false;
	int     dead_angle_speed;
	int     dead_angle_steering;
	float   multiplicator_speed;
	float   multiplicator_steering;
	int     MAX_IGNORE = 10;
	int     MAX_SLOPE  = 10;
	int     MIN_SLOPE  = 3;
	int     MAX_ZEROPOINT = 80;
	boolean reverse_pwm_speed;
	boolean reverse_pwm_steering;
	boolean no_clean = false;
	int     max_speed;
	int     min_speed;
	int     max_steering;
	int     min_steering;
	int     middle_speed;
	int     middle_steering;
	boolean fortyfive_angle;
	
	int speed_current;
	int steering_current;
	//stuff for service
	private Messenger replytoMessenger;
	//actions: human readable ints and act-Strings
	public static final String act                = "act";
	public static final String pitch              = "pitch";
	public static final String roll               = "roll";
	public static final String speed              = "speed";
	public static final String steering           = "steering";
	public static final String DEVICE_CONTROL_KEY = "device_control";
	public static final String capabilities       = "capabilities";
	public static final String connect_state      = "connect_state";
	
	public static final int    nop             = 0;
	public static final int    sendInitData    = 1;
	public static final int    sendOrientation = 2;
	public static final int    sendValues      = 3;
	public static final int    DEVICE_CONTROL  = 4;
	public static final int    getCapabilities = 5;
	public static final int    connected       = 6;
	public static final int    connect         = 7;
	//register / unregister / exit
	public static final int    register        = 100;
	public static final int    unregister      = 101;
	public static final int    exit            = 103;
	//device capabilities
	public static final int    orientation_sensor = 1;
	private ArrayList<Integer> caps = new ArrayList<Integer>();
	//notification
	private NotificationManager mNotificationManager;
	private boolean is_notified = false;
	private String device_name;
	private String BTDevice_mac;
	//bluetooth
	public static final int BT_OFF           = 0;
	public static final int BT_CONNECTED     = 1;
	public static final int BT_NOT_CONNECTED = 2;
	private BluetoothAdapter BTAdapter;
	private int BTState;
	private int BTConnectionState;
	private BroadcastReceiver receiver;
	
	public void setId(long _id) {
		this.id = _id;
		this.PREFERENCES = new Long(id).toString();
		getPrefs();
	}
	
	public void getPrefs() {
		SharedPreferences settings      = getSharedPreferences(PREFERENCES, 0);
		this.dead_angle_speed           = Integer.parseInt(settings.getString("dead_angle_speed", "5"));
		this.dead_angle_steering        = Integer.parseInt(settings.getString("dead_angle_steering", "5"));
		this.multiplicator_speed        = Float.valueOf(settings.getString("speed_slope", "1.0").trim()).floatValue();
		this.multiplicator_steering     = Float.valueOf(settings.getString("steering_slope", "1.0").trim()).floatValue();
		this.reverse_pwm_speed          = settings.getBoolean("invert_speed", false);
		this.reverse_pwm_steering       = settings.getBoolean("invert_steering", false);
		this.max_speed                  = Integer.parseInt(settings.getString("max_speed", "200"));
		this.min_speed                  = Integer.parseInt(settings.getString("min_speed", "100"));
		this.max_steering               = Integer.parseInt(settings.getString("max_steering", "200"));
		this.min_steering               = Integer.parseInt(settings.getString("min_steering", "100"));
		this.fortyfive_angle            = settings.getBoolean("fortyfive_angle", false);
		
		this.middle_speed               = (int)((this.max_speed-this.min_speed)/2)+this.min_speed;
		this.middle_steering            = (int)((this.max_steering-this.min_steering)/2)+this.min_steering;
		speed_current    = middle_speed;
		steering_current = middle_steering;
	}
	public int[] getValues (float roll, float pitch) {
		if(this.fortyfive_angle) {
			roll -= 40;
			roll *= 2;
		}
		if(pitch < this.dead_angle_steering && pitch > -this.dead_angle_steering) {
			pitch = 0; //dead angle
		}
		else {
			if(pitch > 0)
				pitch -= this.dead_angle_steering; //remove dead angle
			else
				pitch += this.dead_angle_steering; //remove dead angle
		}
		//same again for speed
		if(roll < this.dead_angle_speed && roll > -this.dead_angle_speed) {
			roll = 0;
		}
		else {
			if(roll > 0)
				roll -= this.dead_angle_speed;
			else
				roll += this.dead_angle_speed;
		}
		int speed = (int)(roll * ((this.max_speed-this.min_speed)/80)*this.multiplicator_speed);
		int steering = (int)(pitch * ((this.max_steering-this.min_steering)/80)*this.multiplicator_steering);
		
		if(this.reverse_pwm_speed)
			speed *= -1;
		if(this.reverse_pwm_steering)
			steering *= -1;
		
		speed    += this.middle_speed;
		steering += this.middle_steering;
		//set limits: this.min_speed; this.max_speed
		if(speed > this.max_speed)
			speed = this.max_speed;
		if(speed < this.min_speed)
			speed = this.min_speed;
		if(steering < this.min_steering)
			steering = this.min_steering;
		if(steering > this.max_steering)
			steering = this.max_steering;
		//int returnval[];
		if(!(device_control)) {
			speed_current    = middle_speed;
			steering_current = middle_steering;
			return(new int[] {middle_speed, middle_steering});
		}
		else {
			speed_current    = speed;
			steering_current = steering;
			return(new int[] {speed, steering});
		}
	}
    @Override
    public void onCreate() {
        mNotificationManager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
        BTAdapter            = BluetoothAdapter.getDefaultAdapter();
    }
    
    @Override
    public void onDestroy() {
    	unregisterReceiver(receiver);
        stopNotification();
    }
    @Override
    public IBinder onBind(Intent intent) {
    	//messenger to get messages
        return mMessenger.getBinder();
    }
    public void sendBTState() {
		try {
			Message remsg = new Message();
			Bundle data2 = new Bundle();
			data2.putInt(act, BluetoothService.connected);
			data2.putInt(connect_state, BTConnectionState);
			remsg.setData(data2);
			replytoMessenger.send(remsg);
		} catch(Exception e) {}
    }
    
	private void notify_running() {
		if(is_notified)
			mNotificationManager.cancel((int) id);
		sendBTState();
		String infotext;
		switch(BTConnectionState) {
		case BT_CONNECTED:
			infotext = getString(R.string.connected);
			break;
		case BT_NOT_CONNECTED:
			infotext = getString(R.string.not_connected);
			break;
			default:
				infotext = getString(R.string.bt_disabled);
				}
		
		//notification to jump back (especially when bluetooth connection is established)
		String ns = Context.NOTIFICATION_SERVICE;
		mNotificationManager = (NotificationManager) getSystemService(ns);
		//values
		int icon = R.drawable.solarsteuerung;
		CharSequence tickerText = device_name + " " + getText(R.string.running)+": " + infotext;
		long when = System.currentTimeMillis();
		CharSequence contentTitle = getText(R.string.app_name);
		CharSequence contentText = device_name + " " + getText(R.string.running);
		Intent notificationIntent = new Intent(this, Orientation.class);
		notificationIntent.putExtra(HelperClass.DB_DEVICE_NAME, device_name);
		notificationIntent.putExtra(HelperClass.DB_ROWID, id);
		notificationIntent.putExtra(HelperClass.DB_DEVICE_MAC, BTDevice_mac);
		notificationIntent.putExtra("is_notified", true);
		PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);	
		
		if(Build.VERSION.SDK_INT < 11) {
			// the next two lines initialize the Notification, using the configurations above
			Notification notification = new Notification(icon, tickerText, when);
			notification.setLatestEventInfo(this, contentTitle, contentText, contentIntent);
			notification.flags = Notification.FLAG_ONGOING_EVENT;
			notification.defaults |= Notification.DEFAULT_SOUND;
			mNotificationManager.notify((int) id, notification);
			}
		if(Build.VERSION.SDK_INT >= 11) {
			Notification.Builder notification_builder = new Notification.Builder(this);
			notification_builder.setContentText(contentText);
			notification_builder.setContentTitle(contentTitle);
			notification_builder.setContentIntent(contentIntent);
			notification_builder.setContentInfo(infotext);
			notification_builder.setTicker(tickerText);
			notification_builder.setWhen(when);
			notification_builder.setSmallIcon(icon);
			notification_builder.setOngoing(true);
			Notification notification = notification_builder.getNotification();
			mNotificationManager.notify((int) id, notification);
			}
		is_notified = true;
	}
	private void stopNotification() {
		if(is_notified) {
			int _id = (int) id;
			mNotificationManager.cancel(_id);
			/*if(mConnected != null)
				mConnected.cancel();*/
		}
		is_notified = false;
	}
	//communication
	class IncomingHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
        	Bundle data = msg.getData();
            switch (data.getInt("act", nop)) {
            case sendOrientation:
            	float roll_val  = data.getFloat(roll);
            	float pitch_val = data.getFloat(pitch);
            	int [] values = getValues(roll_val, pitch_val);
    			try {
    				//Message -> send data to service
    				Message msgback = new Message();
    				//give id & device name
    				Bundle info = new Bundle();
    				info.putInt(speed,    values[0]);
    				info.putInt(steering, values[1]);
    				info.putInt(BluetoothService.act, BluetoothService.sendValues);
    				msgback.setData(info);
    				replytoMessenger.send(msgback);
    			} catch (RemoteException e) {
    				//the activity did some bullshit
    			}
            	break;
            case DEVICE_CONTROL:
            	device_control = data.getBoolean(DEVICE_CONTROL_KEY);
            	Log.d("device_control", new Boolean(device_control).toString());
            	getPrefs(); //update preferences
            	break;
            case nop:
            	default:
            		if(msg.what == register) {
            			replytoMessenger = msg.replyTo;
            		}
            		if(msg.what == unregister)
            			device_control = false;
            		if(msg.what == exit) {
            			stopNotification();//seems that stopself() is not enough
            			stopSelf();
            		}
            		if(msg.what == getCapabilities) {
            			sendCaps();
            		}
            		if(msg.what == connected)
            			sendBTState();
            		if(msg.what == connect)
            			connectBT();
                    super.handleMessage(msg);
            }
        }
    }
	public void sendCaps() {
		int i           = 0;
		int caps_send[];
		if(caps.size() > 0) {
			caps_send = new int[caps.size()];
			while(i < caps.size()) {
				caps_send[i] = caps.get(i);
				Log.d("capi", new Integer(caps.get(i)).toString());
				i++;
				}
		}
		else {
			caps_send = new int[1];
			caps_send[0] = R.string.sensor;
		}
		int [] device_capabilities = caps_send;
		Bundle info = new Bundle();
		info.putInt(act, getCapabilities);
		info.putIntArray(capabilities, device_capabilities);
		Message msgback = new Message();
		msgback.setData(info);
		try{
			replytoMessenger.send(msgback);
		} catch(RemoteException e) {}
	}
	final Messenger mMessenger = new Messenger(new IncomingHandler());
	ConnectThread mConn;
	ConnectedThread mConnected;
	private BluetoothAdapter mBT;
	public void enableBT() {
		mBT.enable();
	}
	public void setBTConnState() {
		int previous_state = BTConnectionState;
		switch(BTState) {
		case BluetoothAdapter.STATE_CONNECTED:
			BTConnectionState = BT_CONNECTED;
			break;
		case BluetoothAdapter.STATE_CONNECTING:
		case BluetoothAdapter.STATE_DISCONNECTED:
		case BluetoothAdapter.STATE_DISCONNECTING:
		case BluetoothAdapter.STATE_ON:
		case BluetoothAdapter.STATE_TURNING_ON:
			BTConnectionState = BT_NOT_CONNECTED;
			break;
			default:
				BTConnectionState = BT_OFF;
				}
		if(!BTAdapter.isEnabled()) //BT is set to STATE_DISCONNECTED -> need this
			BTConnectionState = BT_OFF;
		/*if(BTAdapter.isEnabled() && BTConnectionState == BT_OFF) //happens on cm 7.2, don't know why...
			BTConnectionState = BT_NOT_CONNECTED;*/
		if(previous_state != BTConnectionState) {
			notify_running();
			Log.d("BTState", new Integer(BTConnectionState).toString());
		}
		Log.d("BT", new Integer(BTState).toString());
	}
	@Override
	  public int onStartCommand(Intent intent, int flags, int startId) {
		IntentFilter filter = new IntentFilter(); //intent filter
		filter.addAction(BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED); //add connection to device state to filter
		filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED); //add BT state to filter
		receiver = (new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				Bundle extras = intent.getExtras();
				if(intent.getAction() == BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED) {
					BTState = extras.getInt(BluetoothAdapter.ACTION_STATE_CHANGED);
				}
				if(intent.getAction() == BluetoothAdapter.ACTION_STATE_CHANGED) {
					BTState = extras.getInt(BluetoothAdapter.ACTION_STATE_CHANGED);
					}
				setBTConnState();
			}
			});
		registerReceiver(receiver, filter);
		try { //Nullpointerexception is sometimes raised by following line, don't know why
			Bundle extras = intent.getExtras();
			if(extras.getLong(HelperClass.DB_ROWID) != id) { //device with another id? -> init; also place where bluetooth is inited
				setId(extras.getLong(HelperClass.DB_ROWID));
				device_name  = extras.getString(HelperClass.DB_DEVICE_NAME);
				
				BTDevice_mac = extras.getString(HelperClass.DB_DEVICE_MAC);
				if(BTDevice_mac == null || BTDevice_mac.length() != 17)
					Toast.makeText(this, getString(R.string.no_valid_bt_device), Toast.LENGTH_LONG).show();
				Log.d(TAG, "MAC: " + BTDevice_mac);
				Log.d(TAG, "Device Name: " + device_name);
				//stop BT Threads if running
				if(mConn != null) {
					mConn.cancel();
					mConn = null;
				}
				if(mConnected != null) {
					mConnected.cancel();
					mConnected = null;
				}
				if(BTAdapter == null)
					stopSelf();
		        // Start the thread to connect with the given device
				try {
					connectBT();
				} catch(Exception e) { Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show(); }
				BTState = BTAdapter.getState();
				setBTConnState();
				notify_running();
				}
		} catch(Exception e) {
			//Can't Log Exception because Exception doesn't have a Message :D
		}
		return START_STICKY;
	  }
	
	public void connectBT() {
		Log.d("intcar", "connectBT");
		BluetoothDevice BTDevice = BTAdapter.getRemoteDevice(BTDevice_mac);
        mConn = new ConnectThread(BTDevice);
        mConn.start();
	}
	
	//thread to connect - hope this works
	private class ConnectThread extends Thread {
	    private final BluetoothSocket mmSocket;
	 
	    public ConnectThread(BluetoothDevice device) {
	    	Log.d("connectthread", "constructor");
	        // Use a temporary object that is later assigned to mmSocket,
	        // because mmSocket is final
	        BluetoothSocket tmp = null;
	 
	        // Get a BluetoothSocket to connect with the given BluetoothDevice
	        try {
	            tmp = device.createRfcommSocketToServiceRecord(UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"));
	        } catch (IOException e) { Log.d("connect", e.getMessage()); }
	        mmSocket = tmp;
	    }
	 
	    public void run() {
	    	Log.d("connectthread", "run");
	        // Cancel discovery because it will slow down the connection
	        BTAdapter.cancelDiscovery();
	 
	        try {
	            // Connect the device through the socket. This will block
	            // until it succeeds or throws an exception
	            mmSocket.connect();
	        } catch (IOException connectException) {
	        	Log.d("except", connectException.getMessage());
	        	connectException.printStackTrace();
	            // Unable to connect; close the socket and get out
	            try {
	                mmSocket.close();
	            } catch (IOException closeException) { }
	            return;
	        }
	 
	        // Do work to manage the connection (in a separate thread)
	        Log.d("tag", "starting connect");
	        mConnected = new ConnectedThread(mmSocket);
	        mConnected.start();
	    }
	    /** Will cancel an in-progress connection, and close the socket */
	    public void cancel() {
	        try {
	            mmSocket.close();
	        } catch (IOException e) { }
	    }
	}
	//thread that is called when BT Device is connected
	private class ConnectedThread extends Thread {
	    private final BluetoothSocket mmSocket;
	    private final InputStream mmInStream;
	    private final OutputStream mmOutStream;
	 
	    public ConnectedThread(BluetoothSocket socket) {
	        mmSocket = socket;
	        InputStream tmpIn = null;
	        OutputStream tmpOut = null;
	        // Get the input and output streams, using temp objects because
	        // member streams are final
	        try {
	            tmpIn = socket.getInputStream();
	            tmpOut = socket.getOutputStream();
	        } catch (IOException e) {}
	 
	        mmInStream = tmpIn;
	        mmOutStream = tmpOut;
	        notify_running();
	    }
	 
	    public void run() {
	        while (true) {
	            try {
	            	//read one byte
	            	int data = mmInStream.read();
	                switch(data) {
	                case 48: //send capabilities
	                	read_capabilities();
	                	break;
	                case 49:
	                	send_values();
	                	break;
	                	default:
	                		Log.d("unhandled byte", new Integer(data).toString());
	                }
	            } catch (IOException e) {
	            	Log.d("IOException", e.getMessage());
	            	//TODO notify_running(BT_NO_CONNECT);
	            	connectBT();
	                break;
	            }
	        }
	    }
	    public void send_values() {
	    	byte[] data = new byte[8];
	    	data[0] = (byte) '\n';
	    	data[1] = (byte) '\r';
	    	data[2] = (byte) 101;
	    	data[3] = (byte) (speed_current-100);
	    	data[4] = (byte) 102;
	    	data[5] = (byte) (steering_current-100);
	    	data[6] = (byte) '\n';
	    	data[7] = (byte) '\r';
	    	this.write(data);
	    }
	    public void read_capabilities() {
	    	byte[] send = new byte[1];
	    	send[0] = (byte) 123;
	    	this.write(send);
	    	int data = 0;
	    	while(data != 27) {
	    		try {
	    			data = mmInStream.read();
	    			byte[] data_send = new byte[1];
	    			data_send[0] = (byte) data;
	    			this.write(data_send);
	    			int string = 0;
	    			boolean init = false;
	    			switch(data) {
	    			case 97:
	    				string = R.string.sensor;
	    				init = true;
	    				break;
	    			case 115:
	    				string = R.string.draw;
	    				init = true;
	    				break;
	    			case 101:
	    				caps.clear();
	    				break;
	    				default:
	    	    			Log.d("cap", new Integer(data).toString());
	    					break;
	    			}
	    			if(init && !caps.contains(string)) {
	    				caps.add(string);
	    				
	    				sendCaps();
	    			}
	    		} catch(IOException e) {}
	    	}
	    	send[0] = (byte) 125;
	    	this.write(send);	
	    }
	    /* Call this from the main Activity to send data to the remote device */
	    public void write(byte[] bytes) {
	        try {
	            mmOutStream.write(bytes);
	        } catch (IOException e) { }
	    }
	 
	    /* Call this from the main Activity to shutdown the connection */
	    public void cancel() {
	        try {
	            mmSocket.close();
	        } catch (IOException e) { }
	    }
	}
}