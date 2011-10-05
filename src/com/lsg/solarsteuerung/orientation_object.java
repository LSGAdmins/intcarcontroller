package com.lsg.solarsteuerung;

import android.content.Context;
import android.content.SharedPreferences;

public class orientation_object {
	public static final String PREFERENCES = "preferencesDocument";
	int   dead_angle_speed;
	int   dead_angle_steering;
	float multiplicator_speed;
	float multiplicator_steering;
	int   multiplicator_speed_raw;
	int   multiplicator_steering_raw;
	int   MAX_IGNORE = 10;
	int   MAX_SLOPE  = 10;
	int   MIN_SLOPE  = 3;
	boolean reverse_pwm_speed;
	boolean reverse_pwm_steering;
	public void getPrefs(Context context) {
		SharedPreferences settings      = context.getSharedPreferences(PREFERENCES, 0);
		this.dead_angle_speed           = settings.getInt("dead_angle_speed", 5);
		this.dead_angle_steering        = settings.getInt("dead_angle_steering", 5);
		this.multiplicator_speed        = (settings.getInt("multiplicator_speed", 4)+3)/10F;
		this.multiplicator_steering     = (settings.getInt("multiplicator_steering", 4)+3)/10F;
		this.multiplicator_speed_raw    = settings.getInt("multiplicator_speed", 4);
		this.multiplicator_steering_raw = settings.getInt("multiplicator_steering", 4);
		this.reverse_pwm_speed           = settings.getBoolean("reverse_pwm_speed", false);
		this.reverse_pwm_steering        = settings.getBoolean("reverse_pwm_steering", false);
	}
	public void writePrefs(Context context) {
		SharedPreferences settings   = context.getSharedPreferences(PREFERENCES, 0);
	    SharedPreferences.Editor editor = settings.edit();
	    editor.putInt("multiplicator_steering", this.multiplicator_steering_raw);
	    editor.putInt("multiplicator_speed",    this.multiplicator_speed_raw);
	    editor.putInt("dead_angle_steering",    this.dead_angle_steering);
	    editor.putInt("dead_angle_speed",       this.dead_angle_speed);
	    editor.putBoolean("reverse_pwm_speed", this.reverse_pwm_speed);
	    editor.putBoolean("reverse_pwm_steering", this.reverse_pwm_steering);
	    editor.commit();
	}
	public int[] getPWM (float roll, float pitch) {
		if(pitch < this.dead_angle_steering && pitch > -this.dead_angle_steering) {
			pitch = 0; //dead angle
		}
		else {
			if(pitch > 0)
			{
				pitch -= this.dead_angle_steering; //remove dead angle
			}
			else {
				pitch += this.dead_angle_steering; //remove dead angle
			}
		}
		//same again for steering
		if(roll < this.dead_angle_speed && roll > -this.dead_angle_speed) {
			roll = 0;
		}
		else {
			if(roll > 0) {
				roll -= this.dead_angle_speed;
			}
			else {
				roll += this.dead_angle_speed;
			}
		}
		int speed = (int)(roll * this.multiplicator_speed + 150);
		int steering = (int)(pitch * this.multiplicator_steering + 150);
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
		}
		if(this.reverse_pwm_speed) {
			if(speed > 150) {
				int tmp = speed - 150;
				speed = 150 - tmp;
			}
			else if(speed < 150) {
				int tmp = 150 - speed;
				speed = 150 + tmp;
			}
		}
		if(this.reverse_pwm_steering) {
			if(steering > 150) {
				int tmp = steering - 150;
				steering = 150 - tmp;
			}
			else if(steering < 150) {
				int tmp = 150 - steering;
				steering = 150 + tmp;
			}
		}
		int returnvalue[] = {speed, steering};
		return returnvalue;
	}
}