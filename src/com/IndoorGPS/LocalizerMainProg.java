/*
 * LocalizerMainProg
 * 
 * @brief: entry point of this application: IndoorGPS.
 * 
 * */
package com.IndoorGPS;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

//*********************************************************
//		Main Program Entrance
//
//
//*********************************************************
public class LocalizerMainProg extends Activity
{
	// DEBUG TAG
	private static final String MSG_TAG = "IndoorGPS -> LocalizerMainProg";
	// Class Instance
	private Button mapButton;
	private Button searchButton;
	private Button favouritesButton;
	private Button settingsButton;
	private Button databaseButton;
	private Button mapfeatureButton;
    //*********************************************************
    //		onCreate
    //
    //
    //*********************************************************
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
    	Log.d(MSG_TAG, "onCreateEntry");
    	//File myDir=new File("/mnt/sdcard/IndoorLocResouce/Database/TestTrace"); 
    	//myDir.mkdirs(); 

        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
		// Map button
		mapButton = (Button) findViewById(R.id.MapButton);
		mapButton.setOnClickListener(new OnClickListener(){
			@Override			
			public void onClick(View v) {
				final Intent mapIndent = new Intent(LocalizerMainProg.this, MapActivity.class);
				startActivityForResult(mapIndent, 0);
			}
		});
		
		// settings button
		settingsButton = (Button) findViewById(R.id.SettingsButton);
		settingsButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				final Intent settings = new Intent(LocalizerMainProg.this, SettingsActivity.class);
				startActivity(settings);
			}
		});
		
		// favourite button
		favouritesButton = (Button) findViewById(R.id.FavoritesButton);
		favouritesButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				final Intent settings = new Intent(LocalizerMainProg.this, FavouritesActivity.class);
				startActivity(settings);
			}
		});
		
		// search button
		searchButton = (Button) findViewById(R.id.SearchButton);
		searchButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				final Intent search = new Intent(LocalizerMainProg.this, SearchActivity.class);
				startActivity(search);
			}
		});
		
		// database button
		databaseButton = (Button) findViewById(R.id.DatabaseButton);
		databaseButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				final Intent database = new Intent(LocalizerMainProg.this, DatabaseActivity.class);
				startActivity(database);
			}
		});
		
		// mapfeature button
		mapfeatureButton = (Button) findViewById(R.id.MapFeatureButton);
		mapfeatureButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				final Intent mapfeature = new Intent(LocalizerMainProg.this, MapFeatureActivity.class);
				startActivity(mapfeature);
			}
		});
        Log.d(MSG_TAG, "OnCreate Exit\n");
    }
    
    //*********************************************************
    //		onPause
    //
    //
    //*********************************************************
    @Override
	public void onPause()
    {
    	//unregisterReceiver(receiverWifi);
    	super.onPause();
    }
    
    //*********************************************************
    //		onResume
    //
    //
    //*********************************************************
    @Override
	public void onResume()
    {
    	//registerReceiver(receiverWifi, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
    	super.onResume();
    }
}