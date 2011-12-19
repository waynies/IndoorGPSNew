package com.IndoorGPS;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.IndoorGPS.LocalizerBasicClass.ConfigSettings;
import com.IndoorGPS.LocalizerBasicClass.LocDB;
import com.IndoorGPS.LocalizerBasicClass.Orientation;
import com.google.android.maps.MapController;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Paint;
import android.graphics.Point;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.Toast;

public class MapFeatureActivity extends Activity{
	private final String MSG_TAG = "MapFeatureActivity ->";

	private int screenWidth;
	private int screenHeight;
	
	private String mapFilePath = null;
	private File mapFile;
	private String chooseMapFile;
	
	public static SharedPreferences sharedPrefs;
	private String Building;
	private String Floor;
	private String testMode;
	
	// power manager
	protected PowerManager.WakeLock mWakeLock;
	
	private Bitmap bm;
	private GPSMapView gpsMapView;
	private LocalizerMapView locMapView;
	private DisplayMetrics dm;
	private MapController myMapController;
	
	private List<Point> RefPtList = new ArrayList<Point>();
	private List<Integer> ClusterList = new ArrayList<Integer>();
	private List<Integer> cheadlist = new ArrayList<Integer>();
	
	private String fpFile;
	private HashMap<Orientation, String> clusterFile = new HashMap<Orientation, String>();

	private static final int REQ_CLUSTER_CHECK = 5;
	private static final int REQ_MAPFEATURE_CHECK = 10;
	private static final int SD_CARD_REQUEST = 1000;
	
	// Define map feature
	private String orientation;
	private int mapfeatureType = 0;
	private int nodenum = 0;
	private String LMPositionFile;
	private String LMPositionFile_o;
	private String MapFeaturesFile;
	private String TurnAreaFile;
	private String MapInfoFile;
	private static int BoundaryConst = 15;
	
	private static BufferedWriter bw1,bw3;

	private HashMap<Integer,HashMap<Point,String>> mapfeaturenodes = new HashMap<Integer,HashMap<Point,String>>();
	private List<Point> mfPtList = new ArrayList<Point>();
	private List<Point> turnPtList = new ArrayList<Point>();
	
	private final SensorEventListener mListener = new SensorEventListener() {
		public void onSensorChanged(SensorEvent event) {
			if (gpsMapView != null) {
				gpsMapView.setAzimuth(event.values);
				gpsMapView.invalidate();
			}
		}

		public void onAccuracyChanged(Sensor sensor, int accuracy) {
		}
	};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		
		Log.d(MSG_TAG, "onCreate");
		
		File root = Environment.getExternalStorageDirectory();
		sharedPrefs = PreferenceManager
		.getDefaultSharedPreferences(getBaseContext());
		
		updatePrefSettings();

		WindowManager w = getWindowManager();
		Display disp = w.getDefaultDisplay();
		dm = new DisplayMetrics();
		disp.getMetrics(dm);

		// set this map activity to full screen
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		
		// keep screen always on until this activity is destroyed
		PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
		mWakeLock = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, MSG_TAG);
		mWakeLock.acquire();
		
		chooseMapFile = String.format("%s%s", Building, Floor);
		if(chooseMapFile.equalsIgnoreCase("CNIB2")){
			//mapFilePath = "/mnt/sdcard/LOST.DIR/CNIB2.JPG";
			mapFilePath = root + "/Maps/CNIB2.JPG";
		}
		else if (chooseMapFile.equalsIgnoreCase("Bahen4")){
			//mapFilePath = "/mnt/sdcard/LOST.DIR/Bahen4.jpg";
			mapFilePath = root + "/Maps/Bahen4.jpg";
		}
		
		mapFile = new File(mapFilePath);
		
		if (!mapFile.exists()) {
			Toast.makeText(this, "map file not found.", Toast.LENGTH_SHORT)
					.show();
			return;
		}
		bm = BitmapFactory.decodeFile(mapFilePath);

		screenWidth = disp.getWidth();
		screenHeight = disp.getHeight();
		
		Paint paint = new Paint();
		paint.setColor(0xFFFFFF00);
		
		boolean noErr = true;
		try 
		{
			ConfigSettings.InitColor();
			ConfigSettings.Initialization(root + "/IndoorLocResouce/Config.txt");
			noErr = LocDB.LoadAdditionalDatabase(Building, Floor, true, true);
		} 
		catch (IOException e1) {
			//e1.printStackTrace();
		}
		if (!noErr)
        {
			Toast.makeText(this, "Missing Files. There is no database file for this map. Please collect FP first.", Toast.LENGTH_LONG).show();
			//Utilities.displayMsgBox(MSG_TAG, "Missing Files. There is no database file for this map. Please collect FP first.");
        }
		
		LMPositionFile_o = String.format("%s/%s%s_MLMPosition_o.txt", ConfigSettings.NAV_SUBDIR, Building, Floor);
		LMPositionFile = String.format("%s/%s%s_MLMPosition.txt", ConfigSettings.NAV_SUBDIR, Building, Floor);
		MapFeaturesFile = String.format("%s/%s%s_MMapFeatures.txt", ConfigSettings.NAV_SUBDIR, Building, Floor);
		TurnAreaFile = String.format("%s/%s%s_MTurnArea.txt", ConfigSettings.NAV_SUBDIR, Building, Floor);
		MapInfoFile = String.format("%s/MMapInfo_%s_%s.txt", ConfigSettings.CSSL_SUBDIR, Building, Floor);

		try {
			bw1 = new BufferedWriter(new FileWriter(LMPositionFile_o));
			bw3 = new BufferedWriter(new FileWriter(MapFeaturesFile));
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		nodenum = 0;
		
		mapfeaturenodes = new HashMap<Integer,HashMap<Point,String>>();
		mfPtList = new ArrayList<Point>();
		turnPtList = new ArrayList<Point>();
		
		gpsMapView = new FeatureMapView(this, bm, screenWidth,
				screenHeight, paint, true, true);

		
		setContentView(gpsMapView);
	}
	
	
	// *****************************************************************
	// onCreateOptionsMenu
	//
	//
	//
	// *****************************************************************
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		Log.d(MSG_TAG, "onCreateOptionsMenu entry");
		
		updatePrefSettings();

		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.mapfeaturemenu, menu);
		Log.d(MSG_TAG, "onCreateOptionsMenu exit");
		return true;		
	}
	
	// *****************************************************************
	// onOptionsItemSelected
	//
	//
	//
	// *****************************************************************
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle item selection
		switch (item.getItemId()) {
		case R.id.choosemapforfeature:
			
			Intent myIntent = new Intent(getApplicationContext(),SDCardActivity.class);
			startActivityForResult(myIntent, SD_CARD_REQUEST);
			
			return true;
		
		case R.id.cluster:
			
			// Popup dialog to choose destination
			if(LocDB.numRPs > 0){
				Intent intent = new Intent();
				intent.setClass(MapFeatureActivity.this, ChooseClusterInfo.class);
				startActivityForResult(intent, REQ_CLUSTER_CHECK);
			}else{
				Toast.makeText(this, "There is no cluster info for this map.", Toast.LENGTH_SHORT).show();
			}
			return true;
			
		case R.id.definemapfeature:
			
			// Popup dialog to choose map feature type
			Intent featureintent = new Intent();
			featureintent.setClass(MapFeatureActivity.this, DefineMapFeature.class);
			startActivityForResult(featureintent, REQ_MAPFEATURE_CHECK);
			
			return true;
		
		case R.id.writemapfeature:
			
			try {
				CreateTXTFiles(1);//LMPositionFile
				CreateTXTFiles(2);//TurnAreaFile
				CreateTXTFiles(3);//MapInfoFile
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return true;
			
		default:
			return super.onOptionsItemSelected(item);
		}
	}
	

	// *****************************************************************
	// onActivityResult
	//
	//
	//
	// *****************************************************************
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		if(resultCode==RESULT_OK && requestCode == REQ_CLUSTER_CHECK)
		{
			orientation = null;
			orientation = data.getStringExtra("orientation");
			
			DrawClusterInfo(orientation);
			
		}
		
		if(resultCode==RESULT_OK && requestCode == REQ_MAPFEATURE_CHECK){
			
			mapfeatureType = data.getIntExtra("mapfeatureType", mapfeatureType);
			
			try {
				
				HandleDefineMapFeature(mapfeatureType);

				gpsMapView.setDrawCluster(false);
				gpsMapView.setDrawMF(true);
				gpsMapView.setMFPoints(mfPtList);
				gpsMapView.invalidate();
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		if(resultCode == Activity.RESULT_OK && requestCode == SD_CARD_REQUEST){ 
    		String imagePath = data.getExtras().getString("returnKey1"); // Get the Image Absolute Path
    		File imageFile = new File(imagePath);
    		if(imageFile.exists()){
    			String newimagePath = imageFile.getAbsolutePath();
    			String building = newimagePath.substring(newimagePath.lastIndexOf("/")+1, newimagePath.lastIndexOf(".")-1);
    			String floor = newimagePath.substring(newimagePath.lastIndexOf(".")-1,newimagePath.lastIndexOf("."));
    			if(!newimagePath.equalsIgnoreCase(mapFilePath)){
    				mapFilePath = newimagePath;
    				boolean noErr = true;
    				try {
    					noErr = LocDB.LoadAdditionalDatabase(building, floor, true, true);
    				} catch (IOException e) {
    				}
    				if(!noErr){
    					LocDB.clusterIndexList.clear();
    					LocDB.clusterHeadList.clear();
    					LocDB.numRPs = 0;
    					LocDB.x00List.clear();
    					LocDB.y00List.clear();
    					Toast.makeText(this, "Map is changed, but there is no cluster info for this map.", Toast.LENGTH_SHORT).show();
    				}
    			}
    			
    			Bitmap latestbm = BitmapFactory.decodeFile(imagePath);
    			latestbm = gpsMapView.changeView(latestbm, screenWidth, screenHeight);	//resize
    			gpsMapView.setDrawCluster(false);
    			gpsMapView.invalidate();
    		}
    	}
	}

	private void HandleDefineMapFeature(int mapfeatureType) throws IOException{
		String nodename = null;
		HashMap<Point,String> node = new HashMap<Point,String>();
		
		
		switch (mapfeatureType){
		case 0:
			nodename = "_NODE_";
			
			Point currentpt = new Point();
			currentpt.x = gpsMapView.getPinTip().x;
			currentpt.y = gpsMapView.getPinTip().y;
			
			mfPtList.add(currentpt);
			node.put(currentpt,nodename);
			mapfeaturenodes.put(nodenum, node);
			
			AddLM(currentpt,nodename,nodenum);
			nodenum++;
			break;
		case 1:
			nodename = "DESCRIP";
			
			AlertDialog.Builder alert = new AlertDialog.Builder(this);
			alert.setTitle("Destination");
			alert.setMessage("Please name the destination point");
			
			// Set an EditText view to get user input 
			final EditText input = new EditText(this);
			alert.setView(input);
			
			alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {
					Editable value = input.getText();
					final String nm = "DESCRIP";
					
					try {
						Point currentpt1 = new Point();
						currentpt1.x = gpsMapView.getPinTip().x;
						currentpt1.y = gpsMapView.getPinTip().y;
						
						mfPtList.add(currentpt1);
						
						HashMap<Point,String> node = new HashMap<Point,String>();
						node.put(currentpt1,value.toString());
						mapfeaturenodes.put(nodenum, node);
						
						AddLM(gpsMapView.getPinTip(),value.toString(),nodenum);
						AddMF(gpsMapView.getPinTip(),value.toString(),nm);
						nodenum++;
						
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				  	
				  }
				});

				alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
				  public void onClick(DialogInterface dialog, int whichButton) {
				    // Canceled.
				  }
				});

				alert.show();

			break;
		case 2:
			nodename = "_TURN_";
			
			Point currentpt2 = new Point();
			currentpt2.x = gpsMapView.getPinTip().x;
			currentpt2.y = gpsMapView.getPinTip().y;

			mfPtList.add(currentpt2);
			turnPtList.add(currentpt2);
			
			node.put(currentpt2,nodename);
			mapfeaturenodes.put(nodenum, node);
			
			AddLM(currentpt2,nodename,nodenum);
			nodenum++;
			break;
		default:
			break;

		}
	}
	
	private void AddMF(Point pinloc,String destname,String nodename) throws IOException{
		String input = nodename + ":" + destname + "," + pinloc.x + "," + pinloc.y;
		bw3.append(input);
		bw3.newLine();
	}
	
	private void AddLM(Point pinloc, String nodename, int nodenum) throws IOException{
		String input = pinloc.x + "," + pinloc.y + "," + nodename + "," + nodenum;
		bw1.append(input);
		bw1.newLine();
	}
	
	private void CreateTXTFiles(int order) throws IOException{
		boolean isValid = CheckValidation(order);
		if(isValid){
			
			switch (order){	
			case 1:	
				//int scrollX = gpsMapView.scrollX;
				//int scrollY = gpsMapView.scrollY;
				//float level = gpsMapView.level;
				BufferedWriter bw = new BufferedWriter(new FileWriter(LMPositionFile));

				for(int i=0; i<mfPtList.size();i++){
					HashMap<Point,String> node = mapfeaturenodes.get(i);
					boolean firstneighbor = true;

					StringBuilder sb = new StringBuilder();
					String line = mfPtList.get(i).x + "," + mfPtList.get(i).y + "," + node.get(mfPtList.get(i)) + ",";
					sb.append(line);

					for(int j=0; j<mfPtList.size();j++){
						if(j!=i){
							if(((Math.abs(mfPtList.get(i).x - mfPtList.get(j).x) < 8) && (Math.abs(mfPtList.get(i).y - mfPtList.get(j).y) < 60))
									|| ((Math.abs(mfPtList.get(i).x - mfPtList.get(j).x) < 60) && (Math.abs(mfPtList.get(i).y - mfPtList.get(j).y) < 8))){
								if(firstneighbor){
									sb.append(j + "");
									firstneighbor = false;
								}else{
									sb.append("-" + j + "");
								}
							}
						}
					}

					if(firstneighbor){ // no neighbor
						if(i > 0 && i!= mfPtList.size()-1){
							sb.append(i-1 + "");
							sb.append("-" + (i+1) + "");
						}else if(i == 0){
							sb.append(i+1 + "");
						}else if(i == mfPtList.size()-1){
							sb.append(i-1 + "");
						}

						firstneighbor = false;
					}

					bw.append(sb.toString());
					bw.newLine();
					
				}
				bw.close();
				break;

			case 2:
				int mostdown = mostDown();
				int mostup = mostUp();
				int mostleft = mostLeft();
				int mostright = mostRight();
				
				BufferedWriter bw1 = new BufferedWriter(new FileWriter(TurnAreaFile));
				for(int i = 0; i < turnPtList.size(); i++){	
					int x = turnPtList.get(i).x;
					int y = turnPtList.get(i).y;
					
					Point[] bound = new Point[4];
					bound[0] = new Point(x-BoundaryConst,y-BoundaryConst);
					bound[1] = new Point(x+BoundaryConst,y-BoundaryConst);
					bound[2] = new Point(x+BoundaryConst,y+BoundaryConst);
					bound[3] = new Point(x-BoundaryConst,y+BoundaryConst);
					
					bw1.write(x + "," + y);
					bw1.newLine();
					
					for(int j = 0; j < 4; j++){
						if(checkTurn(x,y,j,mostdown,mostup,mostleft,mostright)){
							StringBuilder sb = new StringBuilder();
							switch (j){
							case 0:
								sb.append(0 + ":");
								// for turn
								sb.append(bound[0].x + "," + (bound[0].y + 5) + "," + bound[1].x + "," + (bound[1].y + 5) + ",");
								sb.append(bound[2].x + "," + (bound[2].y + 5) + "," + bound[3].x + "," + (bound[3].y + 5) + ":");
								
								// for advance turn
								sb.append(bound[0].x + "," + (bound[0].y + 15) + "," + bound[1].x + "," + (bound[1].y + 15) + ",");
								sb.append(bound[2].x + "," + (bound[2].y + 30) + "," + bound[3].x + "," + (bound[3].y + 30));
								
								bw1.append(sb.toString());
								bw1.newLine();
								break;
							case 1:
								sb.append(90 + ":");
								// for turn
								sb.append((bound[0].x-5)  + "," + bound[0].y + "," + (bound[1].x-5) + "," + bound[1].y + ",");
								sb.append((bound[2].x-5) + "," + bound[2].y + "," + (bound[3].x-5) + "," + bound[3].y + ":");
								
								// for advance turn
								sb.append((bound[0].x-30) + "," + bound[0].y + "," + (bound[1].x-15) + "," + bound[1].y + ",");
								sb.append((bound[2].x-15) + "," + bound[2].y + "," + (bound[3].x-30) + "," + bound[3].y);
								
								bw1.append(sb.toString());
								bw1.newLine();
								break;
							case 2:
								sb.append(180 + ":");
								// for turn
								sb.append(bound[0].x + "," + (bound[0].y - 5) + "," + bound[1].x + "," + (bound[1].y - 5) + ",");
								sb.append(bound[2].x + "," + (bound[2].y - 5) + "," + bound[3].x + "," + (bound[3].y - 5) + ":");
								
								// for advance turn
								sb.append(bound[0].x + "," + (bound[0].y - 30) + "," + bound[1].x + "," + (bound[1].y - 30) + ",");
								sb.append(bound[2].x + "," + (bound[2].y - 15) + "," + bound[3].x + "," + (bound[3].y - 15));
								
								bw1.append(sb.toString());
								bw1.newLine();
								break;
							case 3:
								sb.append(270 + ":");
								// for turn
								sb.append((bound[0].x+5)  + "," + bound[0].y + "," + (bound[1].x+5) + "," + bound[1].y + ",");
								sb.append((bound[2].x+5) + "," + bound[2].y + "," + (bound[3].x+5) + "," + bound[3].y + ":");
								
								// for advance turn
								sb.append((bound[0].x+15) + "," + bound[0].y + "," + (bound[1].x+30) + "," + bound[1].y + ",");
								sb.append((bound[2].x+30) + "," + bound[2].y + "," + (bound[3].x+15) + "," + bound[3].y);
								
								bw1.append(sb.toString());
								bw1.newLine();
								break;
							default:
								break;
							}
							
						}
					}
				}
				bw1.close();
				break;

			case 3:

				BufferedWriter bw2 = new BufferedWriter(new FileWriter(MapInfoFile));
				for(int i = 0; i < turnPtList.size(); i++){	
					String line = "TFP:" + findClosestRP(i);
					bw2.append(line);
					bw2.newLine();
				}
				bw2.close();

				break;

			default:
				break;
			}
		}
		
	}
	
	private boolean checkTurn(int x, int y, int j,int mostdown,int mostup,int mostleft,int mostright){
		boolean hasTurn = false;
		switch (j){
		case 0:
			if(y + BoundaryConst + 30 < mostdown){
				hasTurn = true;
			}
			break;
		case 1:
			if(x - BoundaryConst - 30 > mostleft){
				hasTurn = true;
			}
			break;	
		case 2:
			if(y - BoundaryConst - 30 > mostup){
				hasTurn = true;
			}
			break;
		case 3:
			if(x + BoundaryConst + 30 < mostright){
				hasTurn = true;
			}
			break;
		default:
			break;
		}
		return hasTurn;
	}
	
	private int mostDown(){
		int mostdown = 0;
		for(int i = 0; i<RefPtList.size();i++){
			if(RefPtList.get(i).y > mostdown){
				mostdown = RefPtList.get(i).y;
			}
		}
		return mostdown;
	}
	
	private int mostUp(){
		int mostup = 999;
		for(int i = 0; i<RefPtList.size();i++){
			if(RefPtList.get(i).y < mostup){
				mostup = RefPtList.get(i).y;
			}
		}
		return mostup;
	}
	
	private int mostLeft(){
		int mostleft = 999;
		for(int i = 0; i<RefPtList.size();i++){
			if(RefPtList.get(i).x < mostleft){
				mostleft = RefPtList.get(i).x;
			}
		}
		return mostleft;
	}
	
	private int mostRight(){
		int mostright = 0;
		for(int i = 0; i<RefPtList.size();i++){
			if(RefPtList.get(i).x > mostright){
				mostright = RefPtList.get(i).x;
			}
		}
		return mostright;
	}
	
	private int findClosestRP(int i){
		double closestRPtDis = 9999;
		int closestRPt = 0;
		for(int j=0; j<RefPtList.size();j++){			
			double dist = Math.sqrt(Math.pow(turnPtList.get(i).x - RefPtList.get(j).x,2)+Math.pow(turnPtList.get(i).y - RefPtList.get(j).y,2));
			if(dist < closestRPtDis){
				closestRPt = j;
				closestRPtDis = dist;
			}
		}
		return closestRPt;
	}
	
	private boolean CheckValidation(int order){
		if(order==1){
			return (mapfeaturenodes.size()>0 && Utilities.IsValidFilePath(LMPositionFile_o));
		}else if(order == 2){
			if(!Utilities.IsValidFilePath(TurnAreaFile)){
				File newFile = new File(TurnAreaFile);
			}
			
			RefPtList.clear();
			for(int i = 0; i < LocDB.numRPs; i++)
	        {
	        	Point p = new Point();
	        	p.x = LocDB.x00List.get(i);
	        	p.y = LocDB.y00List.get(i);
	        	RefPtList.add(p);
	        }
			
			return (mapfeaturenodes.size()>0 && RefPtList.size()>0) ;
			
		}else if(order==3){
			
			
			if(!Utilities.IsValidFilePath(MapInfoFile)){
				File newFile = new File(MapInfoFile);
			}
			
			
			return mapfeaturenodes.size()>0;
		}
		return false;
	}
	
	private boolean DrawClusterInfo(String o)
	{
		gpsMapView.Reset();
		
		LoadClusterInfo(o);
		
		List<Point> listPoint = new ArrayList<Point>();
		
        if(testMode.equalsIgnoreCase("Debug")||testMode.equalsIgnoreCase("Demo"))
        {
        	for(int i = 0; i < RefPtList.size(); i++)
        	{
        		Point p1 = new Point();
        		p1.x = (int) Math.round(RefPtList.get(i).x * MapActivity.X_RATIO);
        		p1.y = (int) Math.round(RefPtList.get(i).y * MapActivity.Y_RATIO);
        		listPoint.add(p1);
        	}
        }
        else
        {
        	listPoint = RefPtList;
        }
        
        List<Integer> FPColor = new ArrayList<Integer>();
       
        FPColor = ClusterList;
		
		for(int i = 0; i < cheadlist.size();i++)
		{
			for(int j = 0; j < ClusterList.size(); j++)
			{
				if(ClusterList.get(j) == cheadlist.get(i))
				{
					FPColor.set(j, i);
				}
			}
		}
		
		gpsMapView.setDrawMF(false);
		gpsMapView.setDrawCluster(true);
        gpsMapView.setRPoints(listPoint);	
        gpsMapView.setCIndex(FPColor);
        gpsMapView.invalidate();
        return true;
	}
	// *********************************************************
	// Load Cluster Info
	// *********************************************************
	private void LoadClusterInfo(String o)
	{
		RefPtList.clear();
		ClusterList.clear();
		cheadlist.clear();

		Orientation tag = null;
		
		if(o.equalsIgnoreCase("North")){
    	   tag = Orientation.North;
        }
        if(o.equalsIgnoreCase("South")){
    	   tag = Orientation.South;
        }
        if(o.equalsIgnoreCase("West")){
    	   tag = Orientation.West;
        }
        if(o.equalsIgnoreCase("East")){
    	   tag = Orientation.East;
        }
        
        for(int i = 0; i < LocDB.numRPs; i++)
        {
        	Point p = new Point();
        	p.x = LocDB.x00List.get(i);
        	p.y = LocDB.y00List.get(i);
        	RefPtList.add(p);
        }
        
       
       /*for(int i = 0; i <LocDB.clusterIndexList.get(tag).size(); i++)
        {
        	Integer clist = null;
        	clist = LocDB.clusterIndexList.get(tag).get(i);
        	ClusterList.add(clist);
        }*/
        
        ClusterList.addAll(LocDB.clusterIndexList.get(tag));
        cheadlist = Utilities.uniqueArray(ClusterList);
        
	}
		
	// *********************************************************
	// updatePrefSettings
	// *********************************************************
	private void updatePrefSettings() {
		Building = sharedPrefs.getString("building", "CNIB");
		Floor = sharedPrefs.getString("floor", "2");
		testMode = sharedPrefs.getString("test_mode_pref", "Debug");
	}
	
	// *********************************************************************
	// onTouchEvent
	//
	//
	// @brief: Invoke GPSMapView's gesture scanner to handle screen motion
	// events
	// *********************************************************************
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		return gpsMapView.getGestureScanner().onTouchEvent(event);
	}

	// *********************************************************
	// onStart
	//
	//
	// *********************************************************
	@Override
	public void onStart() {
		super.onStart();
	}

	// *********************************************************
	// onPause
	//
	//
	// *********************************************************
	@Override
	public void onPause() {
		super.onPause();
	}

	// *********************************************************
	// onResume
	//
	//
	// *********************************************************
	@Override
	public void onResume() {
		super.onResume();
	}

	// *********************************************************
	// onStop
	//
	//
	// *********************************************************
	@Override
	public void onStop() {	
		super.onStop();
		try {
			bw1.close();
			bw3.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		
	}

	// *********************************************************
	// onDestroy
	//
	//
	// *********************************************************
	@Override
	public void onDestroy() {
		mWakeLock.release();  
		super.onDestroy();
	}
}
