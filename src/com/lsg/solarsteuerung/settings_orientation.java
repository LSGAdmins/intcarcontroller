package com.lsg.solarsteuerung;

import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;

public class settings_orientation extends PreferenceActivity {
	private long id;
	private String device_name;
	private String PREFERENCES;
	@Override
    protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            
            //get extras (id of device, used in preference file)
            Bundle extras = getIntent().getExtras();
    		if (extras != null) {
    		    id          = extras.getLong(db_object.DB_ROWID);
    		    PREFERENCES = new Long(id).toString();
    		    device_name = extras.getString(db_object.DB_DEVICE_NAME);
    		    setTitle(getTitle()+" » "+device_name);
    		}
    		
    		//set preferencefile to id of device to get different settings for other devices
    		//http://idlesun.wordpress.com/2011/04/08/how-to-make-preferenceactivity-use-non-default-sharedpreferences/
            PreferenceManager prefMgr = getPreferenceManager();
            prefMgr.setSharedPreferencesName(PREFERENCES);
            prefMgr.setSharedPreferencesMode(MODE_PRIVATE);

            addPreferencesFromResource(R.xml.preferences_orientation);
    }
}