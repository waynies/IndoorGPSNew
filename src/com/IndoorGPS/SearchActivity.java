package com.IndoorGPS;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class SearchActivity extends Activity
{
	// Debug message tag
	private String MSG_TAG = "IndoorGPS -> SearchActivity";
	
	//database manager
	private DBManager mDbManager;
	// WifiManager
	private WifiManager wifiManager;
	
	// Wifi scan event broadcast receiver
	ReceiverWifi receiverWifi;
	
	// A list of wifi AP results
	List<ScanResult> wifiList;
	List<ScanResult> updated_wifiList = new ArrayList<ScanResult>();
	
	// GUI stuff
	TextView mainText;
	EditText numSamples;
	EditText xcoord;
	EditText ycoord;
	EditText numAPs;
	StringBuilder sb;
	
	//count of number of samples
	private int APSampleCount = 0;
	
	//default number of samples
	private final int DEFAULT_NUM_SAMPLES = 3;
	
	//default location coordinates
	private final int X_CO = 1; 
	private final int Y_CO = 1;
	
	//default number of access points
	private final int DEFAULT_NUM_APS = 10;
	private final int MAX_NUM_APS = 50;
    
	// SensorManager related declarations
	//private SensorManager sensorManager;
	//private List<Sensor> sensorList; 
	
	boolean acceleroSupported;
	boolean DCSupported;
	
    @Override
	public void onCreate(Bundle savedInstanceState)
    {
        Log.d(MSG_TAG, "onCreate Entry\n");
        
    	super.onCreate(savedInstanceState);
        setContentView(R.layout.wifilistview);
        
        mainText = (TextView)findViewById(R.id.mainText);
        mainText.setMovementMethod(new ScrollingMovementMethod());
		numSamples = (EditText) findViewById(R.id.numSamples);
		numSamples.setText(Integer.toString(DEFAULT_NUM_SAMPLES));
		
		xcoord = (EditText) findViewById(R.id.xcoord);
		ycoord = (EditText) findViewById(R.id.ycoord);
		xcoord.setText(Integer.toString(X_CO));
		ycoord.setText(Integer.toString(Y_CO));
		
		numAPs = (EditText) findViewById(R.id.numAP);
		numAPs.setText(Integer.toString(DEFAULT_NUM_APS));
		
		Button collectButton = (Button) findViewById(R.id.collect);
	
		//open database
        mDbManager = new DBManager(this);
        mDbManager.open();
        
        // initiate Wifi
        wifiManager = (WifiManager) this.getSystemService(Context.WIFI_SERVICE);
        if(this.wifiManager.isWifiEnabled() != true)
        {
        	enableWifi();
        }
        
        receiverWifi = new ReceiverWifi();
        registerReceiver(receiverWifi, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
        wifiManager.startScan();
        mainText.setText("\nScanning....\n");
        
		collectButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v)
			{
				// TODO Auto-generated method stub
				APSampleCount = Integer.valueOf(numSamples.getText().toString());
			}
		});
        Log.d(MSG_TAG, "onCreate exit\n");
	}
    
    //*********************************************************
    //		onStart
    //
    //
    //*********************************************************
    @Override
	public void onStart()
    {
    	registerReceiver(receiverWifi, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
    	super.onStart();
    }
    
    //*********************************************************
    //		onPause
    //
    //
    //*********************************************************
    @Override
	public void onPause()
    {
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
    	registerReceiver(receiverWifi, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
    	super.onResume();
    }
    
    //*********************************************************
    //		onStop
    //
    //
    //*********************************************************
    @Override
	public void onStop()
    {
    	unregisterReceiver(receiverWifi);
    	super.onStop();
    }
    
    
    //*********************************************************
    //		disableWifi
    //
    //
    //*********************************************************
    public void disableWifi()
    {
    	this.wifiManager.setWifiEnabled(false);
    	// wait for interface-shutdown
    	try{
    		Thread.sleep(5000);
    	} catch (InterruptedException e)
    	{
    		// nothing
    	}
    	Log.d(MSG_TAG, "wifi disabled~");
    }
    
    //*********************************************************
    //		enableWifi
    //
    //
    //*********************************************************
    public void enableWifi()
    {
    	this.wifiManager.setWifiEnabled(true);
    	try
    	{
    		Thread.sleep(5000);
    	} catch(InterruptedException e)
    	{
    		// nothing
    	}
    	Log.d(MSG_TAG, "wifi enabled~");
    }
    
    //*********************************************************
    //		ReceiverWifi
    //
    //	@brief: broadcast receiver for wifi scanning
    //
    //*********************************************************
    class ReceiverWifi extends BroadcastReceiver
    {
    	@Override
		public void onReceive(Context c, Intent intent)
    	{
    		Log.d(MSG_TAG, "onReceive entry");
    		sb = new StringBuilder();
    		wifiList = wifiManager.getScanResults();
    		Collections.sort(wifiList, new SortByRss());
    		
    		if (APSampleCount == 0)
    		{
    			for(int i = 0; i < wifiList.size(); i++)
    			{
    				sb.append(new Integer(i+1).toString() + ".");
    				sb.append(wifiList.get(i).BSSID + " : " + wifiList.get(i).level);
    				sb.append("\n\n");
    			}
    			mainText.setText(sb);
    			
    		}   		
    		else
    		{
		        selectAPs(wifiList, updated_wifiList, Integer.valueOf(numAPs.getText().toString()));
		        /*mDbManager.insertRows(
		        		DATABASE_TABLE,
		        		updated_wifiList,
		        		Integer.valueOf(xcoord.getText().toString()), 
		        		Integer.valueOf(ycoord.getText().toString()),
		        		Integer.valueOf(numSamples.getText().toString()) - APSampleCount + 1,
		        		0);
		        */
		        APSampleCount--;
    		}    		
    		wifiManager.startScan();
    		Log.d(MSG_TAG, "onReceive exit" + "AP count is " + APSampleCount);
    	}
    }
    
    //select APs according to RSS
    public void selectAPs(List<ScanResult> resultList, List<ScanResult> new_resultList, int numAPs)
    {
    	Collections.sort(resultList, new SortByRss());
    	new_resultList.clear();
    	
    	for(int i=0; i<numAPs; i++)
    	{
    		new_resultList.add(resultList.get(i));
    	}
    	
    }
    
    //override comparator to sort by RSSs
    class SortByRss implements Comparator<ScanResult>
    {
        public int compare(ScanResult s1, ScanResult s2) 
        {
            if (s1.level < s2.level)
            {
            	return 1;
            }
            if (s1.level == s2.level)
            {
            	return 0;
            }
            else
            {
            	return -1;
            }
        }
    }    
}