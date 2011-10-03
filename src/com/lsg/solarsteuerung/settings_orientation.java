package com.lsg.solarsteuerung;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.SeekBar;
import android.widget.TextView;

public class settings_orientation extends Activity {
	int MAX_IGNORE = 10;
	int MAX_SLOPE  = 10;
	int MIN_SLOPE  = 3;
	public static final String PREFERENCES = "preferencesDocument";
    TextView dead_angle_speedText;
    TextView slope_speedText;
    TextView dead_angle_steeringText;
    TextView slope_steeringText;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		/*getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
	            WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setRequestedOrientation(0x00000000);*/

		super.onCreate(savedInstanceState);
		setContentView(R.layout.settings_orientation);
		
		SharedPreferences settings = getSharedPreferences(PREFERENCES, 0);
		int dead_angle_speed_value = settings.getInt("dead_angle_speed", 5);
		
	    this.dead_angle_speedText  = (TextView) this.findViewById(R.id.dead_angle_speed);
		this.dead_angle_speedText.setText(" "
				+new Integer(dead_angle_speed_value).toString());
		SeekBar dead_angle_speed = (SeekBar)findViewById(R.id.dead_angle_speed_seek);
	    dead_angle_speed.setMax(MAX_IGNORE);
        dead_angle_speed.setProgress(dead_angle_speed_value);
	    dead_angle_speed.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
	    	@Override
	    	public void onProgressChanged(SeekBar arg0, int arg1, boolean arg2) {
	    		settings_orientation.this.dead_angle_speedText.setText(" "
	    				+new Integer(arg1).toString());
	    	      SharedPreferences settings = getSharedPreferences(PREFERENCES, 0);
	    	      SharedPreferences.Editor editor = settings.edit();
	    	      editor.putInt("dead_angle_speed", arg1);
	    	      editor.commit();
	    		}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
				// TODO Auto-generated method stub
			}

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				// TODO Auto-generated method stub				
			}
	    	});

		int slope_speed_value = settings.getInt("multiplicator_speed", 4)+3;
	    this.slope_speedText  = (TextView) this.findViewById(R.id.slope_speed);
		this.slope_speedText.setText(" "
				+new Float(slope_speed_value/10F).toString());
		SeekBar slope_speed = (SeekBar)findViewById(R.id.slope_speed_seek);
	    slope_speed.setMax(MAX_SLOPE - MIN_SLOPE);
	    slope_speed.setProgress(slope_speed_value-3);
	    slope_speed.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
	    	@Override
	    	public void onStopTrackingTouch(SeekBar arg0) {
	    		// TODO Auto-generated method stub
	    		}
	    	@Override
	    	public void onStartTrackingTouch(SeekBar arg0) {
	    		// TODO Auto-generated method stub
	    		}
	    	@Override
	    	public void onProgressChanged(SeekBar arg0, int arg1, boolean arg2) {
	    		settings_orientation.this.slope_speedText.setText(" "
	    				+new Float((arg1+3)/10F).toString());
	    	      SharedPreferences settings = getSharedPreferences(PREFERENCES, 0);
	    	      SharedPreferences.Editor editor = settings.edit();
	    	      editor.putInt("multiplicator_speed", arg1);
	    	      editor.commit();
	    		}
	    	});
	    //Lenkung
		int dead_angle_steering_value = settings.getInt("dead_angle_steering", 5);
	    this.dead_angle_steeringText  = (TextView) this.findViewById(R.id.dead_angle_steering);
		this.dead_angle_steeringText.setText(" "
				+new Integer(dead_angle_steering_value).toString());
		SeekBar dead_angle_steering = (SeekBar)findViewById(R.id.dead_angle_steering_seek);
	    dead_angle_steering.setMax(MAX_IGNORE);
        dead_angle_steering.setProgress(dead_angle_steering_value);
	    dead_angle_steering.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
	    	@Override
	    	public void onStopTrackingTouch(SeekBar arg0) {
	    		// TODO Auto-generated method stub
	    		}
	    	@Override
	    	public void onStartTrackingTouch(SeekBar arg0) {
	    		// TODO Auto-generated method stub
	    		}
	    	@Override
	    	public void onProgressChanged(SeekBar arg0, int arg1, boolean arg2) {
	    		settings_orientation.this.dead_angle_steeringText.setText(" "
	    				+new Integer(arg1).toString());
	    	      SharedPreferences settings = getSharedPreferences(PREFERENCES, 0);
	    	      SharedPreferences.Editor editor = settings.edit();
	    	      editor.putInt("dead_angle_steering", arg1);
	    	      editor.commit();
	    		}
	    	});

		int slope_steering_value = settings.getInt("multiplicator_steering", 4)+3;
	    this.slope_steeringText  = (TextView) this.findViewById(R.id.slope_steering);
		this.slope_steeringText.setText(" "
				+new Float(slope_steering_value/10F).toString());
		SeekBar slope_steering = (SeekBar)findViewById(R.id.slope_steering_seek);
	    slope_steering.setMax(MAX_SLOPE - MIN_SLOPE);
	    slope_steering.setProgress(slope_steering_value-3);
	    slope_steering.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
	    	@Override
	    	public void onStopTrackingTouch(SeekBar arg0) {
	    		// TODO Auto-generated method stub
	    		}
	    	@Override
	    	public void onStartTrackingTouch(SeekBar arg0) {
	    		// TODO Auto-generated method stub
	    		}
	    	@Override
	    	public void onProgressChanged(SeekBar arg0, int arg1, boolean arg2) {
	    		settings_orientation.this.slope_steeringText.setText(" "
	    				+new Float((arg1+3)/10F).toString());
	    	      SharedPreferences settings = getSharedPreferences(PREFERENCES, 0);
	    	      SharedPreferences.Editor editor = settings.edit();
	    	      editor.putInt("multiplicator_steering", arg1);
	    	      editor.commit();
	    		}
	    	});
	    }
	}