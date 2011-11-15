package com.lsg.solarsteuerung;

import android.os.Bundle;
import android.preference.PreferenceActivity;

public class settings_orientation extends PreferenceActivity {
	@Override
    protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.preferences_orientation);
    }
}