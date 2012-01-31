package com.lsg.solarsteuerung;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;


public class DeviceGeneralSettings extends Activity {
	private long id;
	private boolean known_id = false;
	private SQLiteDatabase myDB;
    //private CharSequence[] items;
	private String device_name;
	private String device_desc;
	private String device_mac;
	private String device_BT_name;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		HelperClass.setTheme(false, this);
		//ActionBar
		if(Build.VERSION.SDK_INT >= 11) {
		    HoneyCombAbove.displayUp(this);
		}
		setContentView(R.layout.device_general_settings);
		Bundle extras = getIntent().getExtras(); 
		if (extras != null) {
			if(extras.getBoolean(HelperClass.known_id, false)) {
				id = extras.getLong(HelperClass.DB_ROWID);
				known_id = true;
				myDB = this.openOrCreateDatabase(HelperClass.DB_NAME, MODE_PRIVATE, null);
				Cursor result = myDB.query(HelperClass.DB_TABLE, new String [] {HelperClass.DB_DEVICE_NAME, HelperClass.DB_DEVICE_DESCRIPTION}, HelperClass.DB_ROWID+" = ?",
						new String [] {new Long(id).toString()}, null, null, null);
				result.moveToFirst();
				startManagingCursor(result);
				device_name = result.getString(result.getColumnIndex(HelperClass.DB_DEVICE_NAME));
				device_desc = result.getString(result.getColumnIndex(HelperClass.DB_DEVICE_DESCRIPTION));
				result.close();
				setTitle(this.getText(R.string.edit) + " » " + device_name);
				EditText device_name_edit = (EditText) findViewById(R.id.device_name_edittext);
				EditText device_desc_edit = (EditText) findViewById(R.id.device_description_edittext);
				device_name_edit.setText(device_name);
				device_desc_edit.setText(device_desc);
			}
			else {
				if(extras.getBoolean(HelperClass.new_device, false)) {
					EditText device_name_edit = (EditText) findViewById(R.id.device_name_edittext);
					EditText device_desc_edit = (EditText) findViewById(R.id.device_description_edittext);
					device_name_edit.setText(extras.getString(HelperClass.DB_DEVICE_NAME));
					device_desc_edit.setText(extras.getString(HelperClass.DB_DEVICE_DESCRIPTION));
					}
				}
			if(extras.getBoolean(HelperClass.send_BT, false)) {
				device_mac      = extras.getString(HelperClass.DB_DEVICE_MAC);
				device_BT_name  = extras.getString(HelperClass.DB_BTDEVICE_NAME);
				TextView mac    = (TextView) findViewById(R.id.device_mac);
				TextView BTName = (TextView) findViewById(R.id.device_BT_name);
				mac.setText(device_mac);
				BTName.setText(device_BT_name);
			}
			else {
				if(known_id) {
					Cursor btresult = myDB.query(HelperClass.DB_TABLE, new String [] {HelperClass.DB_BTDEVICE_NAME, HelperClass.DB_DEVICE_MAC}, HelperClass.DB_ROWID + " = ?",
							new String [] {new Long(id).toString()}, null, null, null);
					btresult.moveToFirst();
					startManagingCursor(btresult);
					String tempmac = btresult.getString(btresult.getColumnIndex(HelperClass.DB_DEVICE_MAC));
					if(tempmac != "null" && tempmac.length() == 17) { //seems that we have a valid mac address!!!
						device_mac    = tempmac;
						device_BT_name = btresult.getString(btresult.getColumnIndex(HelperClass.DB_BTDEVICE_NAME));
						TextView mac    = (TextView) findViewById(R.id.device_mac);
						TextView BTName = (TextView) findViewById(R.id.device_BT_name);
						mac.setText(device_mac);
						BTName.setText(device_BT_name);
						Log.d("asdf", new Integer(myDB.getVersion()).toString());
					}
					btresult.close();
					}
				}
			}
		if(known_id && myDB.isOpen())
			myDB.close();
		else
			setTitle(this.getText(R.string.new_device));
	}
	public void select_device(View v) {
		Intent select_device = new Intent(this, SelectBTDevice.class);
		if(known_id) {
			save_to_db();
			select_device.putExtra(HelperClass.known_id, true);
			select_device.putExtra(HelperClass.DB_ROWID, id);
			select_device.putExtra(HelperClass.DB_DEVICE_NAME, device_name);
		}
		else {
			read_values();
			select_device.putExtra(HelperClass.known_id, false);
			select_device.putExtra(HelperClass.DB_DEVICE_NAME,        device_name);
			select_device.putExtra(HelperClass.DB_DEVICE_DESCRIPTION, device_desc);
		}
		startActivity(select_device);
		finish();
	}
	public void save(View v) {
		save_to_db();
		Intent main_activity = new Intent(this, Solarsteuerung.class);
		startActivity(main_activity);
		finish();
	}
	public void read_values() {
		EditText edittext_device_name = (EditText) findViewById(R.id.device_name_edittext);
		EditText edittext_device_desc = (EditText) findViewById(R.id.device_description_edittext);
		device_name = edittext_device_name.getText().toString();
		device_desc = edittext_device_desc.getText().toString();
	}
	public void save_to_db() {
		read_values();
		myDB = this.openOrCreateDatabase(HelperClass.DB_NAME, MODE_PRIVATE, null);
		ContentValues values = new ContentValues();
		values.put(HelperClass.DB_DEVICE_NAME, device_name);
		values.put(HelperClass.DB_DEVICE_DESCRIPTION, device_desc);
		values.put(HelperClass.DB_STANDARD_DEVICE, "false");
		if(device_mac == null) {
			device_mac     = "null";
			device_BT_name = "null";
		}
		values.put(HelperClass.DB_BTDEVICE_NAME, device_BT_name);
		values.put(HelperClass.DB_DEVICE_MAC, device_mac);
		if(known_id)
			myDB.update(HelperClass.DB_TABLE, values, HelperClass.DB_ROWID+" = ?", new String [] {new Long(id).toString()});
		else
			myDB.insert(HelperClass.DB_TABLE, null, values);
		myDB.close();
	}

	 @Override
	 public boolean onKeyDown(int keyCode, KeyEvent event) {
		 if ((keyCode == KeyEvent.KEYCODE_BACK)) {
			 dismiss_dialog();
			 return true;
			 }
		 else
			 return super.onKeyDown(keyCode, event);
		 }
	 @Override
	 public boolean onOptionsItemSelected(MenuItem item) {
	     switch (item.getItemId()) {
	         case android.R.id.home:
	             dismiss_dialog();
	             return true;
	         default:
	             return super.onOptionsItemSelected(item);
	     }
	 }
	 public void dismiss_dialog() {
		 DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
			 @Override
			 public void onClick(DialogInterface dialog, int which) {
			        switch (which){
			        case DialogInterface.BUTTON_POSITIVE:
			        	finish();
			            break;
			        }
			    }
			};
		  AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setMessage(this.getString(R.string.dismiss_changes))
			.setPositiveButton(this.getString(R.string.yes), dialogClickListener)
			.setNegativeButton(this.getString(R.string.no), dialogClickListener).show();
	 }
}