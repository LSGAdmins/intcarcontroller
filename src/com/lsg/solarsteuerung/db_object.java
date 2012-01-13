package com.lsg.solarsteuerung;

import android.content.Context;
import android.os.Build;
import android.util.Log;



public class db_object {
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

	public static void setTheme(boolean dialog, Context context) {
		int theme = android.R.style.Theme_Black;
		if(Build.VERSION.SDK_INT >= 11) {
			theme = 0x0103006b;  //-> android.R.Theme_Holo, needed, because build target is only 2.3.1
			if(dialog)
				theme = 0x0103006f;  //-> android.R.Theme_Holo_Dialog, needed, because build target is only 2.3.1
		} else {
			if(dialog) {
				Log.d(TAG, "else");
				theme = android.R.style.Theme_Dialog;
			}
		}
		Log.d(TAG, new Integer(theme).toString());
		context.setTheme(theme);
		Log.d("db_object.java", "setting theme");
	}
}