package com.lsg.solarsteuerung;

import android.app.ActionBar;
import android.app.ActionBar.OnNavigationListener;
import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.widget.ArrayAdapter;
import android.widget.SpinnerAdapter;



public class HelperClass {
	public static final String DB_NAME               = "intcar";
	public static final String DB_TABLE              = "devices";
	public static final String DB_DEVICE_NAME        = "device_name";
	public static final String DB_DEVICE_DESCRIPTION = "device_description";
	public static final String DB_STANDARD_DEVICE    = "standard_device";
	public static final String DB_DEVICE_MAC         = "mac";
	public static final String DB_BTDEVICE_NAME      = "btdevice_name";
	public static final String DB_ROWID              = "_id";
	public static final String TAG                   = "intcar";

	public static final String helpabout             = "helpabout";
	public static final String help                  = "help";
	public static final String about                 = "about";
	public static final String caller                = "caller";
	public static final String known_id              = "known_id";
	public static final String send_BT               = "send_BT";
	public static final String new_device            = "new_device";

	public static void setTheme(boolean dialog, Context context) {
		int theme = android.R.style.Theme_Black;
		if(Build.VERSION.SDK_INT >= 11) {
			theme = 0x0103006b;  //-> android.R.Theme_Holo, needed, because build target is only 2.3.1
			if(dialog)
				theme = 0x0103006f;  //-> android.R.Theme_Holo_Dialog, needed, because build target is only 2.3.1
		} else {
			if(dialog) {
				theme = android.R.style.Theme_Dialog;
			}
		}
		context.setTheme(theme);
	}
	
	public static void makeNavigation(int[] capabilities, Activity act) {
        if(Build.VERSION.SDK_INT >= 11) {
        ArrayAdapter<String> spinnerArray = new ArrayAdapter<String>(act, android.R.layout.simple_spinner_dropdown_item);
        int i = 0;
        while(i < capabilities.length) {
        	spinnerArray.add(act.getString(capabilities[i]));
        	i++;
        }
        SpinnerAdapter mSpinnerAdapter = spinnerArray;
        ActionBar.OnNavigationListener mOnNavigationListener = new OnNavigationListener() {
        	  @Override
        	  public boolean onNavigationItemSelected(int position, long itemId) {
        	    //Log.d(new Integer(position).toString(), getString(context.caps[position]));
        	    // TODO make navigation
        	    return true;
        	  }
        	};
        	ActionBar actionBar = act.getActionBar();
        	actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
        	actionBar.setDisplayHomeAsUpEnabled(true);
        	actionBar.setDisplayShowTitleEnabled(false);
        	actionBar.setListNavigationCallbacks(mSpinnerAdapter, mOnNavigationListener);
        }
	}
	public static void displayUp(Activity act) {
		if(Build.VERSION.SDK_INT >= 11) {
			ActionBar actionBar = act.getActionBar();
			actionBar.setDisplayHomeAsUpEnabled(true);
		}
	}
}