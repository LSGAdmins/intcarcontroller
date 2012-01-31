package com.lsg.solarsteuerung;

import android.app.ActionBar;
import android.app.Activity;
import android.app.ActionBar.OnNavigationListener;
import android.os.Build;
import android.widget.ArrayAdapter;
import android.widget.SpinnerAdapter;

public class HoneyCombAbove {
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
