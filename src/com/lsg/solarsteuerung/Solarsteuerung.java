package com.lsg.solarsteuerung;


import android.app.AlertDialog;
import android.app.ListActivity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

public class Solarsteuerung extends ListActivity {
	private static final String TAG = "Solarsteuerung";
	public SQLiteDatabase myDB = null;
	long id;
	private SimpleCursorAdapter device_adapter;
	private Cursor c;

	/** Called when the activity is first created. */
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		myDB = this.openOrCreateDatabase(db_object.DB_NAME, MODE_PRIVATE, null);
		testDB();
        View add_device = LayoutInflater.from(getBaseContext()).inflate(R.layout.add_device, null);
        getListView().addFooterView(add_device);
		try {
		updateCursor();
		device_adapter = new SimpleCursorAdapter(this,
				R.layout.list_item_devices,
				c,
				new String[] { db_object.DB_DEVICE_NAME, db_object.DB_DEVICE_DESCRIPTION },
				new int[] { R.id.device_name, R.id.device_description });
		this.setListAdapter(device_adapter);
		registerForContextMenu(getListView()); } catch (Exception e) {Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();}
		//test

		}
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
	                                ContextMenuInfo menuInfo) {
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) menuInfo;
		if(info.id != -1) { //no contextmenu for new device option!
			TextView title_text_view = (TextView) findViewById(R.id.device_name);
			String title = new StringBuffer(title_text_view.getText()).toString();
			menu.setHeaderTitle(title);
			menu.add(Menu.NONE, 0, 0, this.getString(R.string.delete));
			menu.add(Menu.NONE, 1, 1, this.getString(R.string.edit));
		}
		else {
			Toast.makeText(getApplicationContext(), this.getString(R.string.nocontextmenu), Toast.LENGTH_SHORT).show();
		}
	}
	@Override
	public boolean onContextItemSelected(MenuItem item) {
	  AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();
	  id = info.id;
	  int menuItemIndex = item.getItemId(); //das ist die nummer der ausgewählten option, wenn mehr als eine verfügbar ist
	  if(menuItemIndex == 0) {
		  DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
			    @Override
			    public void onClick(DialogInterface dialog, int which) {
			        switch (which){
			        case DialogInterface.BUTTON_POSITIVE:
			        	Solarsteuerung.this.myDB.execSQL("DELETE FROM " + db_object.DB_TABLE + " WHERE " + db_object.DB_ROWID + "='" + (new Long(id).toString()) + "'");
			        	updateCursor();
			        	device_adapter.changeCursor(c);
			            break;
			        }
			    }
			};
		  AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setMessage(this.getString(R.string.really_delete_device))
			.setPositiveButton(this.getString(R.string.yes), dialogClickListener)
			.setNegativeButton(this.getString(R.string.no), dialogClickListener).show();
		  //Toast.makeText(getApplicationContext(), new Long(info.id).toString(), Toast.LENGTH_LONG).show();
	  }
	  if(menuItemIndex == 1) {
		  Intent intent = new Intent(Solarsteuerung.this, device_general_settings.class);
		  intent.putExtra(db_object.DB_ROWID, id); // übergebe aktuelle ID
		  startActivity(intent);
	  }
	  return true;
	}
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		if(id != -1) {
			Cursor o = (Cursor) this.getListAdapter().getItem(position);
			//Toast.makeText(getApplicationContext(), o.getString(0), Toast.LENGTH_LONG).show();
			Intent intent = new Intent(Solarsteuerung.this, device_options.class);
			intent.putExtra(db_object.DB_ROWID, o.getLong(0)); // übergebe aktuelle ID
			Cursor result = myDB.query(db_object.DB_TABLE, new String [] {db_object.DB_DEVICE_NAME}, db_object.DB_ROWID+" = ?",
					new String [] {new Long(id).toString()}, null, null, null);
			result.moveToFirst();
			startManagingCursor(result);
			String device_name = result.getString(result.getColumnIndex(db_object.DB_DEVICE_NAME));
			result.close();
			intent.putExtra(db_object.DB_DEVICE_NAME, device_name);
			startActivity(intent);
		}
		else {
			//Toast.makeText(getApplicationContext(), "new device", Toast.LENGTH_LONG).show();
			Intent intent = new Intent(Solarsteuerung.this, device_general_settings.class);
			startActivity(intent);
		}
	}
	public void updateCursor() {
		c = myDB.rawQuery("SELECT " + db_object.DB_ROWID + ", " + db_object.DB_DEVICE_NAME + ", " + db_object.DB_DEVICE_DESCRIPTION + " FROM "
        		+ db_object.DB_TABLE + ";", null);
		startManagingCursor(c);
	}
	public void testDB() {
    	try {
    		myDB.execSQL("CREATE TABLE IF NOT EXISTS " + db_object.DB_TABLE
    				+ " (" + db_object.DB_ROWID + " integer primary key autoincrement, "
    				+ db_object.DB_DEVICE_NAME + " text not null, "
    				+ db_object.DB_STANDARD_DEVICE + " text not null, "
    				+ db_object.DB_DEVICE_DESCRIPTION + " text not null) "
    				+";");
        } catch (Exception e) { Log.d(TAG, e.getMessage());} finally {
             /*if (myDB != null)
                  myDB.close();*/
        }
	}
	@Override
	public void onDestroy() {
		super.onStop();
		myDB.close();
	}
}