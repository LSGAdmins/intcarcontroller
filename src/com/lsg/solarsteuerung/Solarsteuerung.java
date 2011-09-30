package com.lsg.solarsteuerung;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

public class Solarsteuerung extends ListActivity{
	@Override
	public void onCreate(Bundle savedInstanceState) {
	  super.onCreate(savedInstanceState);
	  String[] options = {this.getString(R.string.sensor),
			  this.getString(R.string.draw)};
	  setListAdapter(new ArrayAdapter<String>(this, R.layout.list_item, options));

	  ListView lv = getListView();
	  lv.setTextFilterEnabled(true);

	  lv.setOnItemClickListener(new OnItemClickListener() {
	    public void onItemClick(AdapterView<?> parent, View view,
	        int position, long id) {
	      // When clicked, show a toast with the TextView text
	    	if(((TextView) view).getText() == Solarsteuerung.this.getString(R.string.draw))
	    	{
	    		Intent intent = new Intent(Solarsteuerung.this, draw.class);
	    		startActivity(intent);
	    	}
	    	if(((TextView) view).getText() == Solarsteuerung.this.getString(R.string.sensor))
	    	{
	  	      Toast.makeText(getApplicationContext(), ((TextView) view).getText(),
	  		          Toast.LENGTH_SHORT).show();
	    		Intent intent = new Intent(Solarsteuerung.this, orientation.class);
	    		startActivity(intent);
	    	}
	    }
	  });
	}
	}