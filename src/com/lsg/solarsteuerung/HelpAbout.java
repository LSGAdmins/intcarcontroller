package com.lsg.solarsteuerung;

import android.app.Activity;
import android.os.Bundle;

public class HelpAbout extends Activity {
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		db_object.setTheme(false, this);
		Bundle data = getIntent().getExtras();
		String type = data.getString(db_object.helpabout);
		if(type.equals(db_object.help)) {
			setContentView(R.layout.help);
			setTitle(getString(R.string.help));
		}
		else {
			setContentView(R.layout.about);
			setTitle(getString(R.string.about));
		}
	}
}
