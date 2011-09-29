package com.lsg.solarsteuerung;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.View.OnTouchListener;
import android.widget.Toast;

public class draw extends Activity {
	private SensorManager mSensorManager;
	private ShakeEventListener mSensorListener;
    DrawView drawView;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setRequestedOrientation(0x00000001);
    	mSensorListener = new ShakeEventListener();
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mSensorManager.registerListener(mSensorListener,
        		mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
        		SensorManager.SENSOR_DELAY_UI);
        drawView = new DrawView(this);
        setContentView(drawView);
        drawView.requestFocus();
    }
    @Override
    protected void onResume() {
        super.onResume();
        mSensorManager.registerListener(mSensorListener,
            mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
            SensorManager.SENSOR_DELAY_UI);
      }

      @Override
      protected void onStop() {
        mSensorManager.unregisterListener(mSensorListener);
        super.onStop();
      }

  	@Override
  	public boolean onCreateOptionsMenu(Menu menu) {
  	    menu.add(0, 0, 0, R.string.send);
  	    Toast.makeText(getApplicationContext(), "Coming soon...", Toast.LENGTH_SHORT).show();
  	    return true;
  	}
  	@Override
  	public boolean onOptionsItemSelected(MenuItem item) {
  	    // Handle item selection
  	    switch (item.getItemId()) {
  	    case 0:
  	    	Toast.makeText(getApplicationContext(), "asdfsadf", Toast.LENGTH_LONG).show();
  	    	//startActivity(new Intent(this, sensors.class));
  	        return true;
  	    default:
  	        return super.onOptionsItemSelected(item);
  	    }
  	}

    public class DrawView extends View implements OnTouchListener {
    	private ArrayList<lines> _lines = new ArrayList<lines>();
        Paint paint = new Paint();
        boolean temporary = false;
        private int number = 0;
        float x_start;
        float y_start;
        float x_end;
        float y_end;

        public DrawView(Context context) {
            super(context);
            setFocusable(true);
            setFocusableInTouchMode(true);

            this.setOnTouchListener(this);

            paint.setColor(Color.WHITE);
            //paint.setAntiAlias(true);
        }

        @Override
        public void onDraw(Canvas canvas) {
        	  mSensorListener.setOnShakeListener(new ShakeEventListener.OnShakeListener() {

                  public void onShake() {
                    _lines.clear();
                    number = 0;
                    temporary = false;
                    invalidate();
                    //Toast.makeText(getApplicationContext(), "shake", Toast.LENGTH_SHORT).show();
                  }
                });
        	  for (lines line : _lines) {  
        		    //canvas.drawPoint(graphic.x, graphic.y, mPaint);  
        		    canvas.drawLine(line.x_start, line.y_start, line.x_end, line.y_end, paint); 
        		  }
        	  if(this.temporary)
        	  {
        		  canvas.drawLine(this.x_start, this.y_start, this.x_end, this.y_end, paint); 
        	  }
        }
        public boolean onTouch(View view, MotionEvent event) {
            // if(event.getAction() != MotionEvent.ACTION_DOWN)
            // return super.onTouchEvent(event);
            if(event.getAction() == MotionEvent.ACTION_DOWN){
            	if( this.number == 0) {
            		this.x_start = event.getX();
            		this.y_start = event.getY();
            		}
            	else{
            		//this.x_start = this.x_end;
            		//this.y_start = this.x_end;
                    /*Toast.makeText(getApplicationContext(), new Float(this.x_start).toString() +" "+ new Float(this.x_end).toString(), Toast.LENGTH_LONG).show();/*
            		this.x_end = event.getX();
            		this.y_end = event.getY();*/
            	}
              }else if(event.getAction() == MotionEvent.ACTION_MOVE){  
                this.temporary = true;
                this.x_end = event.getX();
                this.y_end = event.getY();
                invalidate();
              }else if(event.getAction() == MotionEvent.ACTION_UP){  
                //path.lineTo(event.getX(), event.getY());
                lines line = new lines();
                line.order = this.number;
                line.x_start = this.x_start;
                line.x_end = event.getX();
                line.y_start = this.y_start;
                line.y_end = event.getY();
                _lines.add(line);
                this.x_start = event.getX();
                this.y_start = event.getY();
                //Toast.makeText(getApplicationContext(), new Float(this.x_start).toString() +" "+ new Float(this.x_end).toString(), Toast.LENGTH_LONG).show();
                this.number++;
                this.temporary = false;
                //Toast.makeText(getApplicationContext(), new Integer(this.number).toString(), Toast.LENGTH_LONG).show();
                invalidate();
              }  
              return true;  
            }
        }
    public class lines {
    	int order;
    	float x_start;
    	float y_start;
    	float x_end;
    	float y_end;
    	}
    }