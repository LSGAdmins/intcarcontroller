package com.lsg.solarsteuerung;

import java.util.Date;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;

public class BluetoothService extends Service {
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
	
	public static final int    nop             = 0;
	public static final int    sendInitData    = 1;
	public static final int    sendOrientation = 2;
	public static final int    sendValues      = 3;
	public static final int    DEVICE_CONTROL  = 4;
	public static final int    getCapabilities = 5;
	//register / unregister / exit
	public static final int    register        = 100;
	public static final int    unregister      = 101;
	public static final int    exit            = 103;
	//device capabilities
	public static final int    orientation_sensor = 1;
	//notification
	private NotificationManager mNotificationManager;
	private boolean is_notified = false;
	private String device_name = null;
	
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
	}
	public int[] getValues (float roll, float pitch) {
		if(this.fortyfive_angle)
			roll -= 40;
			roll *= 2;
		if(pitch < this.dead_angle_steering && pitch > -this.dead_angle_steering) {
			pitch = 0; //dead angle
		}
		else {
			if(pitch > 0)
				pitch -= this.dead_angle_steering; //remove dead angle
			else
				pitch += this.dead_angle_steering; //remove dead angle
		}
		//same again for steering
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
			return(new int[] {middle_speed, middle_steering});
		}
		else {
			return(new int[] {speed, steering});
		}
	}
    @Override
    public void onCreate() {
        mNotificationManager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
    }
    
    @Override
    public void onDestroy() {
        stopNotification();
    }
    @Override
    public IBinder onBind(Intent intent) {
    	//messenger to get messages
        return mMessenger.getBinder();
    }
    
	private void notify_running() {
		if(!is_notified) {
			//orientation.this.mNotificationManager.cancelAll();
			//notification to jump back (especially when bluetooth connection is established)
			String ns = Context.NOTIFICATION_SERVICE;
			mNotificationManager = (NotificationManager) getSystemService(ns);
			//values
			int icon = R.drawable.solarsteuerung;
			CharSequence tickerText = device_name + " " + getText(R.string.running);
			long when = System.currentTimeMillis();
			CharSequence contentTitle = getText(R.string.app_name);
			CharSequence contentText = device_name + " " + getText(R.string.running);
			
			Intent notificationIntent = new Intent(this, Orientation.class);
			notificationIntent.putExtra(HelperClass.DB_DEVICE_NAME, device_name);
			notificationIntent.putExtra(HelperClass.DB_ROWID, id);
			notificationIntent.putExtra(HelperClass.DB_ROWID, id);
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
				
				Date date = new Date(when);
				notification_builder.setContentInfo(getString(R.string.since) + " " + date.getHours() + ":" + date.getMinutes());
				notification_builder.setTicker(tickerText);
				notification_builder.setWhen(when);
				notification_builder.setSmallIcon(icon);
				notification_builder.setOngoing(true);
				Notification notification = notification_builder.getNotification();
				Log.d("asdf", "notify");
				mNotificationManager.notify((int) id, notification);
			}
		}
		is_notified = true;
	}
	private void stopNotification() {
		if(is_notified) {
			int _id = (int) id;
			mNotificationManager.cancel(_id);
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
            	getPrefs(); //update preferences, e.g. if last activity was preferences NOTE this is not the optimum, i think this call is done much to often
            	break;
            case nop:
            	default:
            		if(msg.what == register)
            			replytoMessenger = msg.replyTo;
            		if(msg.what == unregister)
            			device_control = false;
            		if(msg.what == exit) {
            			stopNotification();//seems that stopself() is not enough
            			stopSelf();
            		}
            		if(msg.what == getCapabilities) {
            			try {
            				int [] device_capabilities = {R.string.sensor}; //this int [] should contain all the actions supportet by the device -> bluetooth
            				Bundle info = new Bundle();
            				info.putInt(act, getCapabilities);
            				info.putIntArray(capabilities, device_capabilities);
            				Message msgback = new Message();
            				msgback.setData(info);
            				replytoMessenger.send(msgback);
            			} catch(Exception e) {}
            		}
                    super.handleMessage(msg);
            }
        }
    }
	final Messenger mMessenger = new Messenger(new IncomingHandler());
	@Override
	  public int onStartCommand(Intent intent, int flags, int startId) {
		try { //Nullpointerexception is sometimes raised by following line, don't know why
			Bundle extras = intent.getExtras(); //hope that this Bundle one day also contains bluetooth device name
			if(extras.getLong(HelperClass.DB_ROWID) != id) { //device with another id? -> init; also place where bluetooth should be inited
				stopNotification();
				setId(extras.getLong(HelperClass.DB_ROWID));
				device_name = extras.getString(HelperClass.DB_DEVICE_NAME);
				notify_running();
				}
			if(!is_notified)
				notify_running();
		} catch(Exception e) {
			//Can't Log Exception because Exception doesn't have a Message :D
		}
		return START_STICKY;
	  }
}