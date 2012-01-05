package com.lsg.solarsteuerung;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;


public class device_general_settings extends Activity {
	private long id;
	private boolean known_id = false;
	private SQLiteDatabase myDB;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.device_general_settings);
		Bundle extras = getIntent().getExtras(); 
		if (extras != null) {
		    id = extras.getLong(db_object.DB_ROWID);
		    known_id = true;
		    //Toast.makeText(getApplicationContext(), new Long(id).toString(), Toast.LENGTH_LONG).show();
			myDB = this.openOrCreateDatabase(db_object.DB_NAME, MODE_PRIVATE, null);
		    Cursor result = myDB.query(db_object.DB_TABLE, new String [] {db_object.DB_DEVICE_NAME, db_object.DB_DEVICE_DESCRIPTION}, db_object.DB_ROWID+" = ?",
		    		new String [] {new Long(id).toString()}, null, null, null);
		    result.moveToFirst();
		    startManagingCursor(result);
		    String device_name = result.getString(result.getColumnIndex(db_object.DB_DEVICE_NAME));
		    String device_desc = result.getString(result.getColumnIndex(db_object.DB_DEVICE_DESCRIPTION));
		    result.close();
		    myDB.close();
		    setTitle(this.getText(R.string.edit) + " » " + device_name);
		    EditText device_name_edit = (EditText) findViewById(R.id.device_name_edittext);
		    EditText device_desc_edit = (EditText) findViewById(R.id.device_description_edittext);
		    device_name_edit.setText(device_name);
		    device_desc_edit.setText(device_desc);
		}
		else
			setTitle(this.getText(R.string.new_device));
	}
	public void save(View v) {
		EditText edittext_device_name = (EditText) findViewById(R.id.device_name_edittext);
		EditText edittext_device_desc = (EditText) findViewById(R.id.device_description_edittext);
		String device_name = edittext_device_name.getText().toString();
		String device_desc = edittext_device_desc.getText().toString();
		myDB = this.openOrCreateDatabase(db_object.DB_NAME, MODE_PRIVATE, null);
		ContentValues values = new ContentValues();
		values.put(db_object.DB_DEVICE_NAME, device_name);
		values.put(db_object.DB_DEVICE_DESCRIPTION, device_desc);
		if(known_id)
			myDB.update(db_object.DB_TABLE, values, db_object.DB_ROWID+" = ?", new String [] {new Long(id).toString()});
		else
			myDB.insert(db_object.DB_TABLE, null, values);
		myDB.close();
		Intent main_activity = new Intent(this, Solarsteuerung.class);
		startActivity(main_activity);
	}
}