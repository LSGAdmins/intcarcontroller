package com.lsg.solarsteuerung;

import android.content.Context;
import android.content.SharedPreferences;

public class orientation_object {
	public String PREFERENCES = "preferencesDocument";
	private long id = 0;
	public boolean device_control = false;
	public boolean screen_on      = true;
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
	public void setId(long _id) {
		this.id = _id;
		this.PREFERENCES = new Long(id).toString();
	}
	public void getPrefs(Context context) {
		SharedPreferences settings      = context.getSharedPreferences(PREFERENCES, 0);
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
	/*public void writeStandardPrefs(Context context) {
			SharedPreferences settings   = context.getSharedPreferences(PREFERENCES, 0);
			SharedPreferences.Editor editor = settings.edit();
			editor.putString("multiplicator_steering",   "1.0");
			editor.putString("multiplicator_speed",      "1.0");
			editor.putString("dead_angle_steering",      "5");
			editor.putString("dead_angle_speed",         "5");
			editor.putBoolean("reverse_pwm_speed",    false);
			editor.putBoolean("reverse_pwm_steering", false);
			editor.putBoolean("fortyfive_angle",      false);
			editor.putString("max_speed",                "200");
			editor.putString("min_speed",                "100");
			editor.putString("max_steering",             "200");
			editor.putString("min_steering",             "100");
			editor.commit();
	}*/
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
		int speed = (int)(roll * ((this.max_speed-this.min_speed)/80)*this.multiplicator_speed);//this.multiplicator_speed);
		int steering = (int)(pitch * ((this.max_steering-this.min_steering)/80)*this.multiplicator_steering);//this.multiplicator_steering);
		
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
	public void clearValues(Context context) {
		SharedPreferences settings   = context.getSharedPreferences(PREFERENCES, 0);
	    SharedPreferences.Editor editor = settings.edit();
	    editor.clear();
	    editor.commit();
	    this.no_clean = true;
	}
}