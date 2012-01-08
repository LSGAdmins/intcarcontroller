package com.lsg.solarsteuerung;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class device_options extends ListActivity{
	public long id;
	private String device_name;
	@Override
	public void onCreate(Bundle savedInstanceState) {
	  super.onCreate(savedInstanceState);
	  db_object.setTheme(true, this);
	  Bundle extras = getIntent().getExtras(); 
		if (extras != null) {
		    id = extras.getLong(db_object.DB_ROWID);
		    device_name = extras.getString(db_object.DB_DEVICE_NAME);
		    setTitle(device_name);
		}
	  String[] options = {this.getString(R.string.sensor),
			  this.getString(R.string.draw)};
	  setListAdapter(new ArrayAdapter<String>(this, R.layout.list_item, options));

	  ListView lv = getListView();
	  lv.setTextFilterEnabled(true);

	  lv.setOnItemClickListener(new OnItemClickListener() {
	    public void onItemClick(AdapterView<?> parent, View view,
	        int position, long id) {
	    	if(((TextView) view).getText() == device_options.this.getString(R.string.draw))
	    	{
	    		add_opts(new Intent(device_options.this, draw.class));
	    	}
	    	if(((TextView) view).getText() == device_options.this.getString(R.string.sensor))
	    	{
	    		add_opts(new Intent(device_options.this, orientation.class));
	    	}
	    }
	  });
	}
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.device_options, menu);
	    return true;
	}
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    // Handle item selection
	    switch (item.getItemId()) {
	    case R.id.device_options_edit:
	        Intent intent = new Intent(this, device_general_settings.class);
	        intent.putExtra(db_object.DB_ROWID, id);
	        intent.putExtra(db_object.DB_DEVICE_NAME, device_name);
	        startActivity(intent);
	        return true;
	    default:
	        return super.onOptionsItemSelected(item);
	    }
	}
	public void add_opts(Intent intent) {
		intent.putExtra(db_object.DB_ROWID, device_options.this.id);
		intent.putExtra(db_object.DB_DEVICE_NAME, device_name);
		startActivity(intent);
	}
}