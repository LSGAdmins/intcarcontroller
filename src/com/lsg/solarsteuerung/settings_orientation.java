package com.lsg.solarsteuerung;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.SeekBar;
import android.widget.TextView;

public class settings_orientation extends Activity {
	orientation_object options;
    TextView dead_angle_speedText;
    TextView slope_speedText;
    TextView dead_angle_steeringText;
    TextView slope_steeringText;
    TextView zeropointText;
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
		
	    this.dead_angle_speedText  = (TextView) this.findViewById(R.id.dead_angle_speed);
		this.dead_angle_speedText.setText(" "
				+new Integer(options.dead_angle_speed).toString());
		SeekBar dead_angle_speed_seek = (SeekBar)findViewById(R.id.dead_angle_speed_seek);
	    dead_angle_speed_seek.setMax(options.MAX_IGNORE);
        dead_angle_speed_seek.setProgress(options.dead_angle_speed);
	    dead_angle_speed_seek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
	    	@Override
	    	public void onProgressChanged(SeekBar arg0, int arg1, boolean arg2) {
	    		settings_orientation.this.dead_angle_speedText.setText(" "
	    				+new Integer(arg1).toString());
	    	      settings_orientation.this.options.dead_angle_speed = arg1;
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
				+new Float(options.multiplicator_speed).toString());
		SeekBar slope_speed_seek = (SeekBar)findViewById(R.id.slope_speed_seek);
	    slope_speed_seek.setMax(options.MAX_SLOPE - options.MIN_SLOPE);
	    slope_speed_seek.setProgress(options.multiplicator_speed_raw);
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
	    	      settings_orientation.this.options.multiplicator_speed_raw = arg1;
	    		}
	    	});
	    //Lenkung
	    this.dead_angle_steeringText  = (TextView) this.findViewById(R.id.dead_angle_steering);
		this.dead_angle_steeringText.setText(" "
				+new Integer(options.dead_angle_steering).toString());
		SeekBar dead_angle_steering_seek = (SeekBar)findViewById(R.id.dead_angle_steering_seek);
	    dead_angle_steering_seek.setMax(options.MAX_IGNORE);
        dead_angle_steering_seek.setProgress(options.dead_angle_steering);
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
	    	      settings_orientation.this.options.dead_angle_steering = arg1;
	    		}
	    	});
	    this.slope_steeringText  = (TextView) this.findViewById(R.id.slope_steering);
		this.slope_steeringText.setText(" "
				+new Float(options.multiplicator_steering).toString());
		SeekBar slope_steering_seek = (SeekBar)findViewById(R.id.slope_steering_seek);
	    slope_steering_seek.setMax(options.MAX_SLOPE - options.MIN_SLOPE);
	    slope_steering_seek.setProgress(options.multiplicator_steering_raw);
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
	    	      settings_orientation.this.options.multiplicator_steering_raw = arg1;
	    		}
	    	});
	    final CheckBox reverse_pwm_speed_check = (CheckBox) findViewById(R.id.reverse_pwm_speed);
	    reverse_pwm_speed_check.setChecked(options.reverse_pwm_speed);
	    reverse_pwm_speed_check.setOnClickListener(new View.OnClickListener() {
	        public void onClick(View v) {
	            // Perform action on clicks, depending on whether it's now checked
	            if (((CheckBox) v).isChecked()) {
	                settings_orientation.this.options.reverse_pwm_speed = true;
	            } else {
	                settings_orientation.this.options.reverse_pwm_speed = false;
	            }
	        }
	    });
	    final CheckBox reverse_pwm_steering_check = (CheckBox) findViewById(R.id.reverse_pwm_steering);
	    reverse_pwm_steering_check.setChecked(options.reverse_pwm_steering);
	    
	    reverse_pwm_steering_check.setOnClickListener(new View.OnClickListener() {
	        public void onClick(View v) {
	            // Perform action on clicks, depending on whether it's now checked
	            if (((CheckBox) v).isChecked()) {
	                settings_orientation.this.options.reverse_pwm_steering = true;
	            } else {
	                settings_orientation.this.options.reverse_pwm_steering = false;
	            }
	        }
	    });
	    //zeropoint
	    this.zeropointText  = (TextView) this.findViewById(R.id.zeropoint);
		this.zeropointText.setText(" "
				+new Integer(options.zeropoint).toString());
		SeekBar zeropoint_seek = (SeekBar)findViewById(R.id.zeropoint_seek);
	    zeropoint_seek.setMax(options.MAX_ZEROPOINT);
        zeropoint_seek.setProgress(options.zeropoint_raw);
	    zeropoint_seek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
	    	@Override
	    	public void onProgressChanged(SeekBar arg0, int arg1, boolean arg2) {
	    		settings_orientation.this.zeropointText.setText(" "
	    				+new Integer(arg1-40).toString());
	    	      settings_orientation.this.options.zeropoint_raw = arg1;
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
	    }
	@Override
	public void onPause() {
		this.options.writePrefs(getApplicationContext());
		super.onPause();
		}
  	@Override
  	public boolean onCreateOptionsMenu(Menu menu) {
  	    menu.add(0, 0, 0, R.string.delete_preferences);
  	    return true;
  	    }
  	@Override
  	public boolean onOptionsItemSelected(MenuItem item) {
  	    // Handle item selection
  	    switch (item.getItemId()) {
  	    case 0:
  	    	settings_orientation.this.options.clearValues(getApplicationContext());
  	    	Intent settings = new Intent(settings_orientation.this, orientation.class);
  			startActivity(settings);
  	        return true;
  	    default:
  	        return super.onOptionsItemSelected(item);
  	        }
  	    }
	}