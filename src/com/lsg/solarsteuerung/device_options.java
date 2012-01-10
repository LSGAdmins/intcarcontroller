package com.lsg.solarsteuerung;

import android.app.ListActivity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class device_options extends ListActivity{
	//messenger for getting device options
	Messenger mService = null;
	private boolean service_bound;
	
	class IncomingHandler extends Handler {
	    @Override
	    public void handleMessage(Message msg) {
	    	Bundle info = msg.getData();
	        switch (info.getInt(orientation_object.act, orientation_object.nop)) {
	        case orientation_object.getCapabilities:
	        	int [] capabilities = info.getIntArray(orientation_object.capabilities);
	        	Log.d("asdf", new Integer(capabilities[0]).toString());
	        	int length = capabilities.length;
	        	String[] options = new String[length];
	        	int counter = 0;
	        	while(counter < length) {
	        		options[counter] = getString(capabilities[counter]);
	        		counter++;
	        	}
	    	  device_options.this.setListAdapter(new ArrayAdapter<String>(device_options.this, R.layout.list_item, options));
	        	break;
	            default:
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
	        	msg = Message.obtain(null, orientation_object.register);
	        	msg.replyTo = mMessenger;
	        	mService.send(msg);
	        	msg = Message.obtain(null, orientation_object.getCapabilities);
	        	mService.send(msg);
	        } catch(RemoteException e) {
	        	
	        }
	    }

	    public void onServiceDisconnected(ComponentName className) {
	    	//service disconnected
	        mService = null;
	    }
	};
	

	void doBindService() {
		Intent serviceIntent = new Intent(this, orientation_object.class);
		/*
		 * startService is needed to call onStartCommand of Service
		 * that is needed to return START_STICKY
		 * which makes the Service don't stop even if the calling activity is killed
		 * (back button)
		 * hope that my Explanation is right :D */
		serviceIntent.putExtra(db_object.DB_ROWID, id);
		serviceIntent.putExtra(db_object.DB_DEVICE_NAME, device_name);
		serviceIntent.putExtra(orientation_object.act, orientation_object.sendInitData);
		startService(serviceIntent);
		if(bindService(serviceIntent, mConnection, Context.BIND_AUTO_CREATE))
			service_bound = true;
		}
	
	public long id;
	private String device_name;
	@Override
	public void onCreate(Bundle savedInstanceState) {
	  super.onCreate(savedInstanceState);
	  db_object.setTheme(true, this);
	  Bundle extras = getIntent().getExtras(); 
		if (extras != null) {
		    id = extras.getLong(db_object.DB_ROWID);
		    device_name = extras.getString(db_object.DB_DEVICE_NAME);
		    setTitle(device_name);
		}
		  
	  doBindService();
	      
	  String[] options = {this.getString(R.string.loading)};
	  setListAdapter(new ArrayAdapter<String>(this, R.layout.list_item, options));

	  ListView lv = getListView();
	  lv.setTextFilterEnabled(true);

	  lv.setOnItemClickListener(new OnItemClickListener() {
	    public void onItemClick(AdapterView<?> parent, View view,
	        int position, long id) {
	    	if(((TextView) view).getText() == device_options.this.getString(R.string.draw))
	    	{
	    		add_opts(new Intent(device_options.this, draw.class));
	    	}
	    	if(((TextView) view).getText() == device_options.this.getString(R.string.sensor))
	    	{
	    		add_opts(new Intent(device_options.this, orientation.class));
	    	}
	    }
	  });
	}
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.device_options, menu);
	    return true;
	}
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    // Handle item selection
	    switch (item.getItemId()) {
	    case R.id.device_options_edit:
	        Intent intent = new Intent(this, device_general_settings.class);
	        intent.putExtra(db_object.DB_ROWID, id);
	        intent.putExtra(db_object.DB_DEVICE_NAME, device_name);
	        startActivity(intent);
	        return true;
	    default:
	        return super.onOptionsItemSelected(item);
	    }
	}
	public void add_opts(Intent intent) {
		intent.putExtra(db_object.DB_ROWID, device_options.this.id);
		intent.putExtra(db_object.DB_DEVICE_NAME, device_name);
		startActivity(intent);
	}
	@Override
	protected void onDestroy() {
	    super.onDestroy();
	    if (service_bound) {
	    	if (mService != null) {
	    		try {
	    			Message msg = Message.obtain(null, orientation_object.unregister);
	    			mService.send(msg);
	    			} catch (RemoteException e) {
	    				//what the heck is going on with the service???
	    				}
	    		}
	    	}
	    unbindService(mConnection);
	    service_bound = false;
	}
}