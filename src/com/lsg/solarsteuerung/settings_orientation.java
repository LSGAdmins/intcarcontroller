package com.lsg.solarsteuerung;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.SeekBar;
import android.widget.TextView;

public class settings_orientation extends Activity {
	int MAX_IGNORE;
	int MAX_SLOPE;
	int MIN_SLOPE;
	int   dead_angle_speed;
	int   dead_angle_steering;
	float multiplicator_speed;
	float multiplicator_steering;
	int multiplicator_speed_raw;
	int multiplicator_steering_raw;
	boolean reverse_pwm_speed;
	boolean reverse_pwm_steering;
	orientation_object options;
	public static final String PREFERENCES = "preferencesDocument";
    TextView dead_angle_speedText;
    TextView slope_speedText;
    TextView dead_angle_steeringText;
    TextView slope_steeringText;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
	            WindowManager.LayoutParams.FLAG_FULLSCREEN);
		requestWindowFeature(Window.FEATURE_NO_TITLE); //no title
        setRequestedOrientation(0x00000001);

		super.onCreate(savedInstanceState);
		setContentView(R.layout.settings_orientation);
		
		this.options = new orientation_object();
		this.options.getPrefs(getApplicationContext());
		this.dead_angle_speed           = this.options.dead_angle_speed;
		this.dead_angle_steering        = this.options.dead_angle_steering;
		this.multiplicator_speed        = this.options.multiplicator_speed;
		this.multiplicator_steering     = this.options.multiplicator_steering;
		this.multiplicator_speed_raw    = this.options.multiplicator_speed_raw;
		this.multiplicator_steering_raw = this.options.multiplicator_steering_raw;
		this.MAX_IGNORE                 = this.options.MAX_IGNORE;
		this.MAX_SLOPE                  = this.options.MAX_SLOPE;
		this.MIN_SLOPE                  = this.options.MIN_SLOPE;
		this.reverse_pwm_speed          = this.options.reverse_pwm_speed;
		this.reverse_pwm_steering       = this.options.reverse_pwm_steering;
		
	    this.dead_angle_speedText  = (TextView) this.findViewById(R.id.dead_angle_speed);
		this.dead_angle_speedText.setText(" "
				+new Integer(this.dead_angle_speed).toString());
		SeekBar dead_angle_speed_seek = (SeekBar)findViewById(R.id.dead_angle_speed_seek);
	    dead_angle_speed_seek.setMax(MAX_IGNORE);
        dead_angle_speed_seek.setProgress(this.dead_angle_speed);
	    dead_angle_speed_seek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
	    	@Override
	    	public void onProgressChanged(SeekBar arg0, int arg1, boolean arg2) {
	    		settings_orientation.this.dead_angle_speedText.setText(" "
	    				+new Integer(arg1).toString());
	    	      settings_orientation.this.dead_angle_speed = arg1;
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

	    this.slope_speedText  = (TextView) this.findViewById(R.id.slope_speed);
		this.slope_speedText.setText(" "
				+new Float(this.multiplicator_speed).toString());
		SeekBar slope_speed_seek = (SeekBar)findViewById(R.id.slope_speed_seek);
	    slope_speed_seek.setMax(MAX_SLOPE - MIN_SLOPE);
	    slope_speed_seek.setProgress(this.multiplicator_speed_raw);
	    slope_speed_seek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
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
	    	      settings_orientation.this.multiplicator_speed_raw = arg1;
	    		}
	    	});
	    //Lenkung
	    this.dead_angle_steeringText  = (TextView) this.findViewById(R.id.dead_angle_steering);
		this.dead_angle_steeringText.setText(" "
				+new Integer(this.dead_angle_steering).toString());
		SeekBar dead_angle_steering_seek = (SeekBar)findViewById(R.id.dead_angle_steering_seek);
	    dead_angle_steering_seek.setMax(MAX_IGNORE);
        dead_angle_steering_seek.setProgress(this.dead_angle_steering);
	    dead_angle_steering_seek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
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
	    	      settings_orientation.this.dead_angle_steering = arg1;
	    		}
	    	});
	    this.slope_steeringText  = (TextView) this.findViewById(R.id.slope_steering);
		this.slope_steeringText.setText(" "
				+new Float(this.multiplicator_steering).toString());
		SeekBar slope_steering_seek = (SeekBar)findViewById(R.id.slope_steering_seek);
	    slope_steering_seek.setMax(MAX_SLOPE - MIN_SLOPE);
	    slope_steering_seek.setProgress(this.multiplicator_steering_raw);
	    slope_steering_seek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
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
	    	      settings_orientation.this.multiplicator_steering_raw = arg1;
	    		}
	    	});
	    final CheckBox reverse_pwm_speed_check = (CheckBox) findViewById(R.id.reverse_pwm_speed);
	    reverse_pwm_speed_check.setChecked(this.reverse_pwm_speed);
	    reverse_pwm_speed_check.setOnClickListener(new View.OnClickListener() {
	        public void onClick(View v) {
	            // Perform action on clicks, depending on whether it's now checked
	            if (((CheckBox) v).isChecked()) {
	                settings_orientation.this.reverse_pwm_speed = true;
	            } else {
	                settings_orientation.this.reverse_pwm_speed = false;
	            }
	        }
	    });
	    final CheckBox reverse_pwm_steering_check = (CheckBox) findViewById(R.id.reverse_pwm_steering);
	    reverse_pwm_steering_check.setChecked(this.reverse_pwm_steering);
	    
	    reverse_pwm_steering_check.setOnClickListener(new View.OnClickListener() {
	        public void onClick(View v) {
	            // Perform action on clicks, depending on whether it's now checked
	            if (((CheckBox) v).isChecked()) {
	                settings_orientation.this.reverse_pwm_steering = true;
	            } else {
	                settings_orientation.this.reverse_pwm_steering = false;
	            }
	        }
	    });
	    }
	@Override
	public void onPause() {
		this.options.dead_angle_speed           = this.dead_angle_speed;
		this.options.dead_angle_steering        = this.dead_angle_steering;
		this.options.multiplicator_speed_raw    = this.multiplicator_speed_raw;
		this.options.multiplicator_steering_raw = this.multiplicator_steering_raw;
		this.options.reverse_pwm_speed          = this.reverse_pwm_speed;
		this.options.reverse_pwm_steering       = this.reverse_pwm_steering;
		this.options.writePrefs(getApplicationContext());
		super.onPause();
	}
	}