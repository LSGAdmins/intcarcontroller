package com.lsg.solarsteuerung;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

public class SettingsOrientation extends PreferenceActivity {
	private long id;
	private String device_name;
	private String PREFERENCES;
	@Override
    protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            
            HelperClass.setTheme(false, this);
            
            //get extras (id of device, used in preference file)
            Bundle extras = getIntent().getExtras();
    		if (extras != null) {
    		    id          = extras.getLong(HelperClass.DB_ROWID);
    		    PREFERENCES = new Long(id).toString();
    		    device_name = extras.getString(HelperClass.DB_DEVICE_NAME);
    		    setTitle(getTitle()+" » "+device_name);
    		}
    		
    		//set preferencefile to id of device to get different settings for other devices
    		//http://idlesun.wordpress.com/2011/04/08/how-to-make-preferenceactivity-use-non-default-sharedpreferences/
            PreferenceManager prefMgr = getPreferenceManager();
            prefMgr.setSharedPreferencesName(PREFERENCES);
            prefMgr.setSharedPreferencesMode(MODE_PRIVATE);

            addPreferencesFromResource(R.xml.preferences_orientation);
    }
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.settings_orientation, menu);
	    return true;
	}
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    // Handle item selection
	    switch (item.getItemId()) {
	    case R.id.reset_values:
	    	PreferenceManager prefMgr = getPreferenceManager();
	    	SharedPreferences shPrefs = prefMgr.getSharedPreferences();
	    	SharedPreferences.Editor edit = shPrefs.edit();
	    	edit.clear();
	    	edit.commit();
	    	Intent intent = new Intent(this, Orientation.class);
	    	intent.putExtra(HelperClass.DB_ROWID, id);
	    	intent.putExtra(HelperClass.DB_DEVICE_NAME, device_name);
	    	startActivity(intent);
	        return true;
	    default:
	        return super.onOptionsItemSelected(item);
	    }
	}
}