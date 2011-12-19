package com.IndoorGPS;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import java.util.Date;
//import java.sql.Date;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import Jama.Matrix;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Paint;
import android.graphics.Point;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.Toast;

import com.IndoorGPS.Localization.APCSLocalization;
import com.IndoorGPS.LocalizerBasicClass.ConfigSettings;
import com.IndoorGPS.LocalizerBasicClass.GeometryFunc;
import com.IndoorGPS.LocalizerBasicClass.LocDB;
import com.IndoorGPS.LocalizerBasicClass.LocResult;
import com.IndoorGPS.LocalizerBasicClass.MapInfo;
import com.IndoorGPS.LocalizerBasicClass.ObtainFPDB;
import com.IndoorGPS.Navigation.NavigAnalysis;
import com.IndoorGPS.Navigation.PathRouting;
import com.IndoorGPS.Navigation.VoiceOutAudio;
import com.IndoorGPS.Navigation.VoiceOutAudio.VoiceInstr;
import com.IndoorGPS.Tracking.KFTracking;
import com.IndoorGPS.Tracking.MKFTracking;


/**
 * @MapActivity Perform Localization & Tracking & Navigation
 * */
public class MapActivity extends Activity implements OnInitListener {
	private final String MSG_TAG = "MapActivity ->";
	
	// Class Instance
	private String mapFilePath = null;
	private File mapFile;
	private String chooseMapFile;

	// dimension of the screen, in pixels
	private int screenWidth;
	private int screenHeight;

	// shared preference settings
	// public static boolean DEBUG = true;
	public static SharedPreferences sharedPrefs;
	private String appMode;
	private String testMode;
	private int APSampleCount;
	private int MAX_AP_NUM;
	private boolean is4OEnabled;
	private String Building;
	private String Floor;
	private int numAP;
	private boolean useDC = false;  
	
	private int actionCode = 0;
	
	// Map view and bitmap
	private Bitmap bm;
	private Bitmap latestbm;
	private GPSMapView gpsMapView;
	private DisplayMetrics dm;

	// WiFi scanning related
	private WifiManager wifiManager;
	private ReceiverWifi receiverWifi;
	private List<ScanResult> wifiList;
	private HashMap<String, List<Integer>> accumulatedScanResult;
	private HashMap<String, List<Integer>> observedRSS;
	
	/** map containing the average and variance */
	private HashMap<String, double[]> averageMap;
	private HashMap<String, Boolean> orientationStatus;

	// DBManager
	private DBManager dbManager;
	private String targetDBTable;

	// progress dialog
	private ProgressDialog progDialog;
	private double progIncrement;
	private int progSteps;
	
	// power manager
	protected PowerManager.WakeLock mWakeLock;

	// sensor service and listener
	private Sensor mSensor;
	private SensorManager mSensorManager;
	private Sensor mAccelerometer;
	
	// Localization
    public static double X_RATIO = 1.05; //0.70; // convert from PDA to HTC
    public static double Y_RATIO = 0.8851;// 0.53;
    
	// Tracking
    private int tracecount = 0;
    private int stepcount = 0;
    private boolean isTrackingOn = false;
    private boolean isWiFiScanDone = false;
    private int trackUpdateInterval = 1;
    private double initVx = 0;
    private double initVy = 0;
    private static Thread trackingThread = new Thread();
    private int STARTING_POINT;
    private int DEBUG_trackcount;
    private int TEST_NUM = 128;
    private int TEST_O = 0;
    private int headingDC = -1;
    private float[] DCValues;
    private boolean isDCworking = false;
      
    // Navigation
    private String userDestination = null;
    private boolean isNavigationRunning = false;
    private static Thread navigationThread = new Thread();
    private int routeCount = 1;
    private boolean firstloc4navig = false;
    
    private List<Point> mapSelectedPoints = new ArrayList<Point>();
    private String userStartingPt = null;
    private int VoiceInterval = 3; // for go straight cmd
     
    //private delegate void locUpdateNavigDelegate(int x, int y, string numst);
    //private delegate void pathNavigDelegate(Point[] pts);
    //private delegate void displaytxtDelegate(string msg, bool append);
    //private delegate void buttonDelegate();
    
    //for repeat voice commands
    private boolean isRepeatVoicePlaying = false;
    private boolean isNavigUpdateVoicePlaying = false;
    private boolean isVoiceCopying = false;
    private List<VoiceInstr> RepeatVoiceBuffer = new ArrayList<VoiceInstr>();
    private List<String> RepeatVoiceAdditionalBuffer = new ArrayList<String>();
    
	long repeatCmdCopyTime = new Date().getSeconds();
	private static final int REQ_TTS_STATUS_CHECK = 0;
	private static final int REQ_DESTINATION_CHECK = 1;
	private static TextToSpeech mTts;
	
	// Speech recognition
	private static final int VOICE_RECOGNITION_REQUEST_CODE = 3;
	private int level = 0;
	private int destID = 0;
	private List<Integer> deleteCount = new ArrayList<Integer>();        		

	// Camera Map
	private static final int REQ_MAP_OPTIONS = 1500;
	private static final int CAMERA_PIC_REQUEST = 2500;
	private static final int SD_CARD_REQUEST = 100;
	private String workingImagePath = null;
	private Uri outputFileUri;
	private Bitmap pic;
	private String[] before;
	private String[] after;
	private File DCIM = new File(Environment.getExternalStorageDirectory()+"/DCIM/100MEDIA");

	
	private final SensorEventListener mListener = new SensorEventListener() {
		public void onSensorChanged(SensorEvent event) {
			if (gpsMapView != null) {
				gpsMapView.setAzimuth(event.values);
				gpsMapView.invalidate();
			}
			DCValues = event.values;
		}
		public void onAccuracyChanged(Sensor sensor, int accuracy) {
		}
	};
	
		
	// *****************************************************************
	// onCreate
	//
	// *****************************************************************
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Utilities.initUtil(this);

		orientationStatus = new HashMap<String, Boolean>();
		orientationStatus.put(DBManager.DATABASE_TABLE_N, false);
		orientationStatus.put(DBManager.DATABASE_TABLE_E, false);
		orientationStatus.put(DBManager.DATABASE_TABLE_S, false);
		orientationStatus.put(DBManager.DATABASE_TABLE_W, false);

		progDialog = new ProgressDialog(this);
		progDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		progIncrement = 0;

		// get root dir
		File root = Environment.getExternalStorageDirectory();
		
		// get shared preferences
		sharedPrefs = PreferenceManager
				.getDefaultSharedPreferences(getBaseContext());
		updatePrefSettings();
	   
		if (testMode.equalsIgnoreCase("Demo")||testMode.equalsIgnoreCase("Debug"))
		{
			STARTING_POINT = 101;
			DEBUG_trackcount = STARTING_POINT;
		}
		
		// initiate a sensor
		mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
		mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);
		mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		
		// initiate WiFi manager
		wifiManager = (WifiManager) this.getSystemService(Context.WIFI_SERVICE);
		if (wifiManager.isWifiEnabled() != true) {
			enableWifi();
		}
		accumulatedScanResult = new HashMap<String, List<Integer>>();
		observedRSS = new HashMap<String, List<Integer>>();
		averageMap = new HashMap<String, double[]>();

		receiverWifi = new ReceiverWifi();
		registerReceiver(receiverWifi, new IntentFilter(
				WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));

		// open database
		dbManager = new DBManager(this);
		dbManager.open();
		targetDBTable = DBManager.DATABASE_TABLE;

		// query display metrics for MapView
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

		// check map file and load it
		chooseMapFile = String.format("%s%s", Building, Floor);
		if(chooseMapFile.equalsIgnoreCase("CNIB2")){
			mapFilePath = root + "/Maps/CNIB2.JPG";
		}
		else if (chooseMapFile.equalsIgnoreCase("Bahen4")){
			mapFilePath = root + "/Maps/Bahen4.jpg";
		}
		
		mapFile = new File(mapFilePath);
		// System.out.println("mapFilePath is --->" + mapFilePath);
		// System.out.println("mapFile exist --->" + mapFile.exists());

		if (!mapFile.exists()) {
			Toast.makeText(this, "map file not found.", Toast.LENGTH_SHORT)
					.show();
			return;
		}
		bm = BitmapFactory.decodeFile(mapFilePath);

		screenWidth = disp.getWidth();
		screenHeight = disp.getHeight();

		// paint for drawing points in localizer map view
		Paint paint = new Paint();
		paint.setColor(0xFFFFFF00);

		Paint paint2 = new Paint();
		paint2.setColor(0xFFFF00FF);
		
		// Check to see if a recognition activity is present        
		PackageManager pmg = getPackageManager();        
		List<ResolveInfo> activities = pmg.queryIntentActivities(new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH), 0);
		if (activities.size() != 0) {            
			Log.d(MSG_TAG, "Speech Recognizer Ready");
		} else{            
			Log.d(MSG_TAG, "Speech Recognizer not present"); 
		}
		
		// Check Text-To-Speech
		Intent checkIntent = new Intent();   
		checkIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);   
		startActivityForResult(checkIntent, REQ_TTS_STATUS_CHECK);

		// Load Database
		if (appMode.equalsIgnoreCase("Localizer mode")) {
			gpsMapView = new LocalizerMapView(this, bm, screenWidth,
					screenHeight, paint, true, true);
			
			// Load Database for Localization
			ConfigSettings.Initialization(root + "/IndoorLocResouce/Config.txt");
			boolean noErr = true;
			updatePrefSettings();
			try {
				noErr = LocDB.LoadDatabase(Building, Floor, true, true);
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			if (!noErr)
            {
				Utilities.displayMsgBox(MSG_TAG, "Missing Files, Load Database Failed");
            }
		}
		else if(appMode.equalsIgnoreCase("Tracking mode"))
		{
			gpsMapView = new LocalizerMapView(this, bm, screenWidth,
					screenHeight, paint2, true, true);
			ConfigSettings.Initialization(root + "/IndoorLocResouce/Config.txt");
			boolean noErr = true;
			updatePrefSettings();
			try {
				noErr = LocDB.LoadDatabase(Building, Floor, true, true);
				noErr = noErr & LocDB.LoadMapInfo();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			if (!noErr)
            {
                Utilities.displayMsgBox(MSG_TAG, "Missing Files, Load Database Failed");
            }
			
		}
		else if(appMode.equalsIgnoreCase("Navigation mode"))
		{
			gpsMapView = new LocalizerMapView(this, bm, screenWidth,
					screenHeight, paint2, true, true);
			ConfigSettings.Initialization(root + "/IndoorLocResouce/Config.txt");
			boolean noErr = true;
			updatePrefSettings();
			try {
				noErr = LocDB.LoadDatabase(Building, Floor, true, true);
				noErr = noErr & LocDB.LoadMapInfo();
				InitNavigationNListDestination(); 
				VoiceOutAudio.LocateAudioFiles();
			} catch (IOException e1) {
				e1.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			}			
			if (!noErr)
            {
                Utilities.displayMsgBox(MSG_TAG, "Missing Files, Load Database Failed");
            }
	     }
		else if(appMode.equalsIgnoreCase("Training mode"))
		{
			gpsMapView = new GPSMapView(this, bm, screenWidth, screenHeight,
					null, true, true);
		}

		setContentView(gpsMapView);	
	}	

	// *****************************************************************
	// 
	// For voice recognition (non-training mode)
	// For map options (training mode)
	// onKeyDown
	//
	// *****************************************************************
	
	// On Key Down to start voice recognition (repeat the voice instruction)
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub

		if(keyCode == KeyEvent.KEYCODE_DPAD_CENTER)
		{
			Log.d(MSG_TAG, "Key Press");

			if(appMode.equalsIgnoreCase("Training mode")){
				startCameraActivity();
			}else{
				/*try {
				RepeatVoiceCommand();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}*/
				startVoiceRecognitionActivity();
			}
			return true;
		}

		return super.onKeyDown(keyCode, event);
	}


	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
	    if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER && event.isTracking()
	            && !event.isCanceled()) {   
	        return true;
	    }
	    return super.onKeyUp(keyCode, event);
	}

	/*** Fire an intent to start the speech recognition activity.*/   
	private void startVoiceRecognitionActivity()
	{
		Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
		intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
		intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Speech Recognition ...");
		startActivityForResult(intent, VOICE_RECOGNITION_REQUEST_CODE);
		Log.d(MSG_TAG, "Start Recog Activity");		
	}
	
	/*** Fire an intent to start the camera activity.*/   
	private void startCameraActivity()
	{
		Intent intent = new Intent();
		intent.setClass(MapActivity.this, MapOptionsActivity.class);
		startActivityForResult(intent, REQ_MAP_OPTIONS);
		Log.d(MSG_TAG, "Start Camera Activity");		
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
		/****start navigation after choosing the destination ******/
		if(resultCode==RESULT_OK && requestCode == REQ_DESTINATION_CHECK)
		{
			userDestination = data.getStringExtra("Destination");
			try {
				Navigation();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		/****For speech recognition ******/
		if(requestCode == REQ_TTS_STATUS_CHECK) 
		{
			switch (resultCode) 
			{
				case TextToSpeech.Engine.CHECK_VOICE_DATA_PASS: 
					{
						mTts = new TextToSpeech(this, this);
						Log.v(MSG_TAG, "TTS Engine is installed!");
						break;
					}
				case TextToSpeech.Engine.CHECK_VOICE_DATA_BAD_DATA:  
				case TextToSpeech.Engine.CHECK_VOICE_DATA_MISSING_DATA:
				case TextToSpeech.Engine.CHECK_VOICE_DATA_MISSING_VOLUME:
					{
						Log.v(MSG_TAG, "Need language stuff:" + resultCode);   
						Intent dataIntent = new Intent();   
						dataIntent.setAction(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);   
						startActivity(dataIntent);  
					}
					break;
				case TextToSpeech.Engine.CHECK_VOICE_DATA_FAIL:  
				default:   
						Log.v(MSG_TAG, "Got a failure. TTS apparently not available");   
						break;   
			}
		}

		if(requestCode == VOICE_RECOGNITION_REQUEST_CODE && resultCode == RESULT_OK)
		{
			Log.d(MSG_TAG, "Check Request Code for Speech Recog");
			ArrayList<String> matches = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);            
			
			AnalyzeSpeech(matches);
		}
		
		if(resultCode==RESULT_OK && requestCode == REQ_MAP_OPTIONS)
		{
			String mapOptionType = data.getStringExtra("mapOptionType");
			if(mapOptionType.equalsIgnoreCase("loadmap")){
				
				Intent myIntent = new Intent(getApplicationContext(),SDCardActivity.class);
				startActivityForResult(myIntent, SD_CARD_REQUEST);
			}
			else if(mapOptionType.equalsIgnoreCase("cameramap")){
				
				before = DCIM.list(); // For deleting Double save
				Intent cameraIntent = new Intent("android.media.action.IMAGE_CAPTURE");
			    File filedir = new File(Environment.getExternalStorageDirectory()+"/Maps","tmp.jpg");
			    outputFileUri = Uri.fromFile(filedir);
			    cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri);
			    startActivityForResult(cameraIntent, CAMERA_PIC_REQUEST);	
			}	
		}
		
		if (resultCode == Activity.RESULT_OK && requestCode == CAMERA_PIC_REQUEST) {
           	// Display image received on the view
            Bundle b = data.getExtras(); 
            pic = (Bitmap) b.get("data");	
            
			latestbm = gpsMapView.changeView(pic, screenWidth, screenHeight);	//resize			
			gpsMapView.invalidate();
			
			handle_save();
			handle_doubleSave(); // For deleting the extra saved images by Android 2.2
        }
		
		if(resultCode == Activity.RESULT_OK && requestCode == SD_CARD_REQUEST){ 
    		String imagePath = data.getExtras().getString("returnKey1"); // Get the Image Absolute Path
    		File imageFile = new File(imagePath);
    		if(imageFile.exists()){
    			workingImagePath = imagePath; 
    			latestbm = BitmapFactory.decodeFile(imagePath);
    			latestbm = gpsMapView.changeView(latestbm, screenWidth, screenHeight);	//resize

    			gpsMapView.invalidate();
    		}
    	}
		super.onActivityResult(requestCode, resultCode, data);	
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
		if (appMode.equalsIgnoreCase("Localizer mode")) {
			inflater.inflate(R.menu.locmapmenu, menu);
		} 
		else if(appMode.equalsIgnoreCase("Tracking mode")) {
			inflater.inflate(R.menu.locmapmenu, menu);
		}
		else if(appMode.equalsIgnoreCase("Navigation mode")) {
			inflater.inflate(R.menu.locmapmenu, menu);
		}
		else if(appMode.equalsIgnoreCase("Training mode")){
			inflater.inflate(R.menu.mapmenu, menu);
		}

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
		case R.id.change_map:
			changeMap();
			return true;
		case R.id.search:
			searchLocation();
			return true;
		case R.id.map_info:
			displayMapInfo();
			return true;
		case R.id.collect_fp:
			handleFPCollection();
			return true;
		case R.id.localize:
            try 
            {
            	actionCode = 1;
            	APCSLocalization.SetUp(true, true, true, numAP);
            	localizeSelfPosition();
    		} 
            catch (IOException e) 
            {
    			e.printStackTrace();
    		}              
			return true;
		case R.id.tracking:
			actionCode = 2;
			APCSLocalization.SetUp(true, true, true, numAP);   
            KFTracking.Initialization(initVx, initVy, Utilities.intDouble(trackUpdateInterval), ConfigSettings.KALMANFILTER_DIAG_Q, ConfigSettings.KALMANFILTER_DIAG_R, ConfigSettings.KALMANFILTER_DIAG_INITP);
            MKFTracking.SetUseCompass(useDC, false);
			
            Tracking();
			return true;
		case R.id.trackstop:
			StopTrack();
			return true;
		case R.id.navigation:	
			actionCode = 3;
			APCSLocalization.SetUp(true, true, true, numAP);    
            KFTracking.Initialization(initVx, initVy, Utilities.intDouble(trackUpdateInterval), ConfigSettings.KALMANFILTER_DIAG_Q, ConfigSettings.KALMANFILTER_DIAG_R, ConfigSettings.KALMANFILTER_DIAG_INITP);
            MKFTracking.SetUseCompass(useDC, false);

			// Popup dialog to choose destination
			Intent intent = new Intent();
			intent.setClass(MapActivity.this, NavigationActivity.class);
			startActivityForResult(intent, REQ_DESTINATION_CHECK);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	
	// *********************************************************************
	// handleFPCollection
	//
	//
	// implements finger print collecting functionality
	// *********************************************************************
	private void handleFPCollection() {
		updatePrefSettings();

		AlertDialog.Builder alertBuilder = new AlertDialog.Builder(this);
		alertBuilder.setSingleChoiceItems(R.array.orientation_list, 4,
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						if (which == 0) {
							targetDBTable = (is4OEnabled == true) ? DBManager.DATABASE_TABLE_N
									: DBManager.DATABASE_TABLE;
						} else if (which == 1) {
							targetDBTable = (is4OEnabled == true) ? DBManager.DATABASE_TABLE_E
									: DBManager.DATABASE_TABLE;
						} else if (which == 2) {
							targetDBTable = (is4OEnabled == true) ? DBManager.DATABASE_TABLE_S
									: DBManager.DATABASE_TABLE;
						} else if (which == 3) {
							targetDBTable = (is4OEnabled == true) ? DBManager.DATABASE_TABLE_W
									: DBManager.DATABASE_TABLE;
						} else if (which == 4) {
							targetDBTable = DBManager.DATABASE_TABLE;
						}
					}
				});

		AlertDialog alert = alertBuilder.create();
		alert.setTitle("Choose your current orientation.");
		alert.setIcon(R.drawable.icon_compass);
		alert.setCancelable(true);
		alert.setCanceledOnTouchOutside(true);

		// OK button
		alert.setButton("OK", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				Log.d(MSG_TAG, "Starting scan with table " + targetDBTable);

				// setup a progress dialog and start scan
				progDialog.setMessage("Collecting RSS, please standby..");
				progDialog.show();
				progDialog.setProgress(0);
				
				progIncrement = 100 / APSampleCount;

				wifiManager.startScan();
			}
		});

		// Cancel button
		alert.setButton2("Cancel", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				// empty
			}
		});

		alert.show();
	}

	// *********************************************************************
	// handleLocalization
	//
	//
	// implements localization function
	// *********************************************************************
	private void localizeSelfPosition() throws IOException 
	{   
		updatePrefSettings();
		//observedRSS.clear();
		Log.d(MSG_TAG, "Start Localization");		
		
		// Take offline database as online reading to debug
		if(testMode.equalsIgnoreCase("Debug")||testMode.equalsIgnoreCase("Demo"))
		{
			int TestLocPoint = 1;
			//int TestLocPoint = LocDB.numRPs;
			String RSSfilename = String.format("%s/RSS_%s_%s_South.txt", ConfigSettings.CSSL_SUBDIR, LocDB.x00List.get(TestLocPoint - 1), LocDB.y00List.get(TestLocPoint - 1));
		
			progDialog.setMessage("Use offline reading for debug, Localize now, Please wait...");
			progDialog.show();		
			
			observedRSS = CollectOnlineRSS(RSSfilename);
			//observedRSS = LocDB.GetTestRSS_Y(APCSLocalization.WhichDB(headingDC).get(2), TestLocPoint-1);
			UpdateAction_Loc();
		}
		else
		{			
			progDialog.setMessage("Localize now, Please wait...");
			progDialog.show();
			progDialog.setProgress(10);
			
			wifiManager.startScan();
		}
	}


	// *********************************************************************
	// handleTracking
	//
	// Implement tracking function
	// *********************************************************************
	private void Tracking() 
	{
		gpsMapView.Reset();
		isTrackingOn = true;
		isWiFiScanDone = true;
		isNavigationRunning = false;
		trackingThread = new Thread(){
			@Override
			public void run() {
				// TODO Auto-generated method stub
				try{
					TrackingThreadJob();
				}catch (InterruptedException e){
					e.printStackTrace();
				}
				super.run();
			}
		};
		trackingThread.start();
	}
	
	/*** Tracking Functions ***/
	private void TrackingThreadJob() throws InterruptedException
	{   
		while (isTrackingOn)
	    {
	        //DEBUG_trackcount = 0;
	        UpdateSelfPosition();
	        Thread.sleep(100 * trackUpdateInterval);
	    }
	    trackingThread.stop();
	}


	private boolean UpdateSelfPosition() 
	{
		long t0 = new Date().getTime();
		numAP = new Integer(sharedPrefs.getString("Num_AP_Loc", "20"));
	    //>>> Can't let DC event work in parallel with collecting RSS   -  StartDC();
	    if (testMode.equalsIgnoreCase("Debug"))
	    {
	        if(DEBUG_trackcount < LocDB.numRPs)
	        {
	        	observedRSS = LocDB.GetTestRSS_Y(APCSLocalization.WhichDB(headingDC).get(TEST_O), DEBUG_trackcount);
	        	try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				DEBUG_trackcount++;
	        }
	        else
	        {
	        	if(isNavigationRunning)
	        	{
	        		isNavigationRunning = false;
	        		isTrackingOn = false;
	        		Speak("No more fingerprint, end of this test", TextToSpeech.QUEUE_ADD);
	        		return false;
	        	}
	        	else if(isTrackingOn)
	        	{
	        		isTrackingOn = false; 
	            	isNavigationRunning = false;
	            	return false;   
	        	}
	        }
	        return UpdateAction_Track(t0);
	    }      
	    else if (testMode.equalsIgnoreCase("Demo"))
	    {
	        if(DEBUG_trackcount < LocDB.numTests)
	        {
	        	observedRSS = LocDB.GetRSS_Y(DEBUG_trackcount);
	        	try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
	        	DEBUG_trackcount++;
	        }
	        else
	        {
	        	if(isNavigationRunning)
	        	{
	        		isNavigationRunning = false;
	        		isTrackingOn = false;
	        		Speak("End of this trace", TextToSpeech.QUEUE_ADD);
	        		return false;
	        	}
	        	else if(isTrackingOn)
	        	{
	        		isTrackingOn = false; 
	            	isNavigationRunning = false;
	            	DEBUG_trackcount = STARTING_POINT;
	            	return false;   
	        	}
	        }
	        return UpdateAction_Track(t0);
	    }    
	    else if (testMode.equalsIgnoreCase("Test"))
	    {   
	    	// Collect Online RSS readings.
	    	while(isWiFiScanDone)
	        {
	        	if (LocResult.ComputedPositionMeas.size() > 0)
	        	{            	
	        		APSampleCount = ConfigSettings.TRACKING_NUM_SAMPLE;
	        	}
	        	else 
	        	{
	        		APSampleCount = ConfigSettings.STARTPT_NUM_SAMPLE;
	        	}
	        	isWiFiScanDone = false;
	        	wifiManager.startScan(); 
	        	return true;
	        }
	    }
		return true;
	}
	
	// *********************************************************************
	// handleNavigation
	//
	// Implement navigation function
	// *********************************************************************
	private void Navigation() throws Exception 
	{	
		Log.d(MSG_TAG, "Destination is:" + userDestination);
		gpsMapView.Reset();
		LocResult.ResetPositionEst();
        MKFTracking.ResetMKF();
        
        isNavigationRunning = false;
        isTrackingOn = false;
        firstloc4navig = true;
        isWiFiScanDone = true;
        routeCount = 1;
        
        PathRouting.SetDestination(userDestination);    
        NavigAnalysis.SetVoiceInterval(VoiceInterval);
                
        if(testMode.equalsIgnoreCase("Debug")||testMode.equalsIgnoreCase("Demo"))
        {
        	DEBUG_trackcount = STARTING_POINT;
            boolean updateResult = UpdateSelfPosition();
           
        	UpdateAction_Navi(updateResult);
        }
        else
        {
        	UpdateSelfPosition();
        }
	}
	
	private void UpdateAction_Navi(boolean update)throws Exception
	{
		boolean updateResult = update;
		int count = 1;
		while (!updateResult && count < ConfigSettings.MAXDETECTRSSCOUNT)
        {
            count++;
            Log.d(MSG_TAG, "RSS READINGS INVALID");
            updateResult = UpdateSelfPosition();
        }
        if (!updateResult)
        {
        	Log.d(MSG_TAG, "Exceed Retry Max Count. Can't start navigation");
            return;
        }
        
        Point myPoint_D = new Point();
        Point myPoint_S = new Point();
        myPoint_D = PathRouting.DestinationLoc;
        myPoint_S = LocResult.EstimatedPositions.get(LocResult.EstimatedPositions.size()-1);
    	
        boolean findPath = Navigation_FindRoute(LocResult.EstimatedPositions.get(LocResult.EstimatedPositions.size()-1));
        if (!findPath)
        {
        	Log.d(MSG_TAG, "Can't find path");
        	return;
        }
        
        NavigAnalysis.AnalyzeRoutedPath(PathRouting.RoutedPath);
        
    	NavigAnalysis.SmoothUpdates();
        
    	DrawUpdatesOnMapImage(myPoint_S, myPoint_D);
    	
        isNavigationRunning = true;
        navigationThread = new Thread(){
			@Override
			public void run() {
				try {
					navigationThreadJob();
				} catch (InterruptedException e) {
					e.printStackTrace();
				} catch (Exception e) {
					e.printStackTrace();
				}
				super.run();
			}
		};
		navigationThread.start();
	}
	
    private void navigationThreadJob() throws Exception
    {
    	Log.d(MSG_TAG, "NavigationThreadJob Entry");
    	firstloc4navig = true;
    	
    	while (isNavigationRunning)
    	{       		
    		if (firstloc4navig)
    		{
    			CopyVoiceCommandForRepeat();
    			int duration = 1000;
                isNavigUpdateVoicePlaying = true;
                
                String Speech1 = VoiceOutAudio.PlayAudio(VoiceInstr.FollowVoiceInstr);
                Speak(Speech1, TextToSpeech.QUEUE_FLUSH);
                Thread.sleep(duration);
                
                Speak(PathRouting.DestinationPlace, TextToSpeech.QUEUE_ADD);
                duration = VoiceOutAudio.PlayDestinationAudio();
                Thread.sleep(duration);
                
                NavigAnalysis.PlayVoiceInstructions();
                isNavigUpdateVoicePlaying = false;
                firstloc4navig = false;                   
    		}
    		
            boolean updateResult = UpdateSelfPosition();
            if(testMode.equalsIgnoreCase("Debug")||testMode.equalsIgnoreCase("Demo"))
            {
            	UpdateAction_Navi_S2(updateResult);
            }
    	}
    }
    	
    private void UpdateAction_Navi_S2(boolean update) throws Exception
    {
    	boolean updateResult = update;
    	int localizeCount = 1;
            while (!updateResult && localizeCount < ConfigSettings.MAXDETECTRSSCOUNT)
            {
                if (localizeCount == 2)
                {
                    CopyVoiceCommandForRepeat(VoiceInstr.StopRelocalize);
                    int duration;
                    while (isRepeatVoicePlaying) { };
                    isNavigUpdateVoicePlaying = true;
                    String Speech6 = VoiceOutAudio.PlayAudio(VoiceInstr.StopRelocalize);
                    duration = VoiceOutAudio.VoiceDurationCheck(VoiceInstr.StopRelocalize);
                    Speak(Speech6, TextToSpeech.QUEUE_FLUSH);
                    Thread.sleep(duration);
                    isNavigUpdateVoicePlaying = false;
                }

                localizeCount++;
                updateResult = UpdateSelfPosition();
            }
            if (!updateResult)
            {
                Log.d(MSG_TAG, "Exceed Retry Max Count. Can't continue navigation");
            	return;
            }
            
            NavigAnalysis.SmoothUpdates();
            DrawSmoothedUpdatesOnMapImage();
            
            int checkCode = NavigAnalysis.AnalyzeCurrentUpdates();
            if (checkCode == 10)
            {
                while (isRepeatVoicePlaying) { };

                isNavigUpdateVoicePlaying = true;
                NavigAnalysis.PlayVoiceInstructions();
                isNavigUpdateVoicePlaying = false;

                isNavigationRunning = false;
                Log.d(MSG_TAG, "Reach Dest.");
                LocResult.ResetPositionEst();
                return;
            }
            
            if (checkCode == -10)
            {
                CopyVoiceCommandForRepeat(VoiceInstr.Rerouting);
                
                while (isRepeatVoicePlaying) { };
                int duration;
                isNavigUpdateVoicePlaying = true;
                String Speech7 = VoiceOutAudio.PlayAudio(VoiceInstr.Rerouting);
                duration = VoiceOutAudio.VoiceDurationCheck(VoiceInstr.Rerouting);
                Speak(Speech7, TextToSpeech.QUEUE_FLUSH);
                Thread.sleep(duration);
                isNavigUpdateVoicePlaying = false;
       
                Point currentLoc = LocResult.EstimatedPositions.get(LocResult.EstimatedPositions.size()-1);
                Point prevLoc = LocResult.EstimatedPositions.get(LocResult.EstimatedPositions.size() - 2);
                                    
                routeCount++;
                // use currentLoc to re-route the path; 
                boolean findPath = Navigation_FindRoute(currentLoc);
                if (!findPath)
                {
                    Log.d(MSG_TAG, "Can not find path");
                    return;
                }
                NavigAnalysis.AnalyzeRoutedPath(PathRouting.RoutedPath);                    
                // Re-route
                DrawUpdatesOnMapImage(LocResult.EstimatedPositions.get(LocResult.EstimatedPositions.size()-1), PathRouting.DestinationLoc);
                
                Log.d(MSG_TAG, "Re-Routing");
            }
            
            CopyVoiceCommandForRepeat();
            
            while (isRepeatVoicePlaying) { };

            isNavigUpdateVoicePlaying = true;
            boolean playvoice = NavigAnalysis.PlayVoiceInstructions();
            isNavigUpdateVoicePlaying = false;
            
            if (localizeCount > 1 && !playvoice)
            {
                CopyVoiceCommandForRepeat(VoiceInstr.GoStraight);
                while (isRepeatVoicePlaying) { };

                isNavigUpdateVoicePlaying = true;
                String Speech8 = VoiceOutAudio.PlayAudio(VoiceInstr.GoStraight);
                Speak(Speech8, TextToSpeech.QUEUE_FLUSH);
                isNavigUpdateVoicePlaying = false;
            }        
            Thread.sleep(100 * trackUpdateInterval);
    	}   	
    
    
    // Find Path for First Navigation
    private boolean Navigation_FindRoute(Point currentloc) throws Exception
    {
        boolean findPath = PathRouting.FindPath(currentloc);
        return findPath;
    }
    
    private void CopyVoiceCommandForRepeat()
    {
        while (isRepeatVoicePlaying || isNavigUpdateVoicePlaying) { /*wait*/}        

        List<VoiceInstr> temp = NavigAnalysis.GetVoiceBuffer();
        if (temp.size() > 0)
        {
            isVoiceCopying = true;

            RepeatVoiceBuffer.clear();
            RepeatVoiceBuffer.addAll(temp);
            RepeatVoiceAdditionalBuffer.clear();
            RepeatVoiceAdditionalBuffer.addAll(NavigAnalysis.GetVoiceAdditionalBuffer());
            
            isVoiceCopying = false;
            
    		Date t = new Date();
    		repeatCmdCopyTime = t.getSeconds();
        }
    }
    
    private void CopyVoiceCommandForRepeat(VoiceInstr cmd)
    {
        while (isRepeatVoicePlaying || isNavigUpdateVoicePlaying) { /*wait*/}        

        isVoiceCopying = true;

        RepeatVoiceBuffer.clear();
        RepeatVoiceBuffer.add(cmd);
        
        isVoiceCopying = false;

        Date t = new Date();
		repeatCmdCopyTime = t.getSeconds();
    }
    
    private void RepeatVoiceCommand() throws InterruptedException
    {
        //another instance of this function has already been called.
        if (isRepeatVoicePlaying)
        {
            return;
        }

        if (NavigAnalysis.ReachDestination())
        {
            NavigAnalysis.PlayVoiceInstructions();
            return;
        }

        if (isNavigationRunning)
        {               
            while (isVoiceCopying) { /*wait*/}

            if (RepeatVoiceBuffer.size() > 0)
            {
                isRepeatVoicePlaying = true;

                long ts = new Date().getSeconds();
                ts = ts - repeatCmdCopyTime;

                if (ts < ConfigSettings.REPEAT_CMD_LIFETIME_S)
                {
                    int durationtime = 1000;
                    while (isNavigUpdateVoicePlaying) { /*wait*/};

                    for (VoiceInstr cmd : RepeatVoiceBuffer)
                    {
                        Thread.sleep(durationtime);
                        
                        if (cmd == VoiceInstr.ADDITIONAL)
                        {
                            if (RepeatVoiceAdditionalBuffer.size() > 0)
                            {
                                String instr = RepeatVoiceAdditionalBuffer.get(0);
                                RepeatVoiceAdditionalBuffer.remove(0);
                                String speech9 = VoiceOutAudio.PlayAdditionalAudio(instr);
                                Speak(speech9, TextToSpeech.QUEUE_ADD);
                                durationtime = VoiceOutAudio.VoiceDurationAdditionalCheck(instr);
                            }
                        }
                        else
                        {
                             String speech9 = VoiceOutAudio.PlayAudio(cmd);
                             Speak(speech9, TextToSpeech.QUEUE_FLUSH);
                             durationtime = VoiceOutAudio.VoiceDurationCheck(cmd);
                        }
                    }
                }
                else
                {
                    //UtilitiesFunc.WriteLineInFile(ConfigSettings.NAVIGATION_FILENAME, ">Commands exceed lifetime. Not repeated. " + ConfigSettings.REPEAT_CMD_LIFETIME_S);
                }

                isRepeatVoicePlaying = false;
            }
            else
            {
               // UtilitiesFunc.WriteLineInFile(ConfigSettings.NAVIGATION_FILENAME, ">No commands needed to be repeated.");
            }
        }
    }
	
	private void InitNavigationNListDestination() throws Exception {
		// TODO Auto-generated method stub
        if (MapInfo.LoadMapInfoFiles(Building, Floor) == -1)
        {
        	Utilities.displayMsgBox(MSG_TAG, "MapInfo File Missing");
        }
        else
        {
            PathRouting.RoutingSetup(Building, Floor);
        }
	}
	
	// *********************************************************************
	// handleStop
	//
	// Stop Thread
	// *********************************************************************
	private void StopTrack() {
		// TODO Auto-generated method stub
		if(isTrackingOn)
		{
			isTrackingOn = false;
			trackingThread.stop();
			gpsMapView.Reset();
			actionCode = 0;
		}
		if(isNavigationRunning)
		{
			isNavigationRunning = false;
			navigationThread.stop();
			LocResult.ResetPositionEst();
			gpsMapView.Reset();
			actionCode = 0;
		}
	}

	// *********************************************************************
	// changeMap
	//
	//
	//
	// *********************************************************************
	private void changeMap() {
		startCameraActivity();
		//Toast.makeText(this, "not implemented yet", Toast.LENGTH_SHORT).show();
	}

	// *********************************************************************
	// searchLocation
	//
	//
	//
	// *********************************************************************
	private void searchLocation() {
	}

	// *********************************************************************
	// displayMapInfo
	//
	//
	//
	// *********************************************************************
	private void displayMapInfo() {
		String title = "Information";
		String msg = "height: " + screenHeight + "\nWidth: " + screenWidth;

		Utilities.displayMsgBox(title, msg);
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
		dbManager.open();
		registerReceiver(receiverWifi, new IntentFilter(
				WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
		super.onStart();
	}

	// *********************************************************
	// onPause
	//
	//
	// *********************************************************
	@Override
	public void onPause() {
		dbManager.close();
		if(mTts != null)  
		{
			mTts.stop();
		}
		super.onPause();
	}

	// *********************************************************
	// onResume
	//
	//
	// *********************************************************
	@Override
	public void onResume() {
		updatePrefSettings();
		dbManager.open();
		mSensorManager.registerListener(mListener, mSensor,
				SensorManager.SENSOR_DELAY_GAME);
		registerReceiver(receiverWifi, new IntentFilter(
				WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
		super.onResume();
	}

	// *********************************************************
	// onStop
	//
	//
	// *********************************************************
	@Override
	public void onStop() {
		unregisterReceiver(receiverWifi);
		mSensorManager.unregisterListener(mListener);
		dbManager.close();		
		super.onStop();
	}

	// *********************************************************
	// onDestroy
	//
	//
	// *********************************************************
	@Override
	public void onDestroy() {
		dbManager.close();
		mWakeLock.release();
		mTts.shutdown();   
		super.onDestroy();
	}

	// *********************************************************
	// ReceiverWifi
	//
	// @brief: broadcast receiver for WiFi scanning
	// puts results in ObtainFPDB class
	// *********************************************************
	class ReceiverWifi extends BroadcastReceiver {
		@Override
		public void onReceive(Context c, Intent intent) {
			Log.d(MSG_TAG, "onReceive entry");

			wifiList = wifiManager.getScanResults();
			// Collections.sort(wifiList, new SortByRss());
			if (wifiList == null) {
				Log.d("wifi receiver", "wifilist null");
				return;
			}

			if (appMode.equals("Training mode"))
			{
				if (APSampleCount > 0) 
				{
					accumulatedScanResult = ObtainFPDB.appendScanResults(wifiList, accumulatedScanResult);
					APSampleCount--;
					progDialog.incrementProgressBy((int) Math.round(progIncrement));

					if (APSampleCount == 0) 
					{
						progDialog.setProgress(100);

						int sampleCountSetting = new Integer(
								sharedPrefs.getString("sampleCount", "3"));

						ObtainFPDB.postProcessScanResults(
								accumulatedScanResult, sampleCountSetting,
								averageMap);

						ObtainFPDB.storeScanResults(targetDBTable,
								gpsMapView.getPinTip(), averageMap, dbManager);

						orientationStatus.put(targetDBTable, true);

						if (isCollectionComplete() == true
								|| is4OEnabled == false) {
							// ObtainFPDB.processRSSinDB(dbManager,
							// gpsMapView.getPinTip(), is4OEnabled);
							updatePrefSettings();
						}

						progDialog.dismiss();
						Utilities.closeFile();
					} 
					else 
					{
						wifiManager.startScan();
					}
				}
			} 
			else 
			{	
				long t0 = new Date().getTime();
				if(APSampleCount > 0)
				{
					observedRSS = ObtainFPDB.appendScanResults(wifiList, observedRSS);
					APSampleCount--;
					//progDialog.incrementProgressBy((int) Math.round(progIncrement));
					
					if(APSampleCount == 0)
					{
						isWiFiScanDone = true;
						int sampleCountSetting = new Integer(
								sharedPrefs.getString("sampleCount", "3"));
						
						ObtainFPDB.postProcessScanResults(
								observedRSS, sampleCountSetting,
								averageMap);
						progDialog.setProgress(20);
						if(observedRSS.isEmpty())
						{
							Utilities.displayMsgBox(MSG_TAG, "InValid RSS Reading");
							return;
						}
						try {
							PerformLocAction(t0);
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
					else
					{
						wifiManager.startScan();
					}
				}
			}
			Log.d(MSG_TAG, "onReceive exit and sampleCount " + APSampleCount);
		}
		
		// Perform Action at the Receiver Side, when testMode == test
		private void PerformLocAction(long t) throws Exception {
			// TODO Auto-generated method stub
			if(testMode.equalsIgnoreCase("test"))
			{
				if(actionCode == 1)
				{
					UpdateAction_Loc();
				}
				if(actionCode == 2)
				{
					UpdateAction_Track(t);
				}
				if(actionCode == 3)
				{
					if(firstloc4navig == true)
					{
						boolean updateResult = UpdateAction_Track(t);
						UpdateAction_Navi(updateResult);
					}
					else
					{
						boolean updateResult = UpdateAction_Track(t);
						UpdateAction_Navi_S2(updateResult);
					}
				}
				observedRSS.clear();
			}
		}
	}
	
	private void UpdateAction_Loc() {
		// TODO Auto-generated method stub
		boolean isValidOnlineY = APCSLocalization.SelfLocS0_CreateY(observedRSS);	
		
		if(isValidOnlineY)
		{
            APCSLocalization.SelfLocS1_APSelection();
            progDialog.setProgress(40);
            APCSLocalization.SelfLocS2_MatchCluster(-1);
            progDialog.setProgress(60);
            APCSLocalization.SelfLocS3_L1NormMin();
            progDialog.setProgress(80);
            Point estimatedPoint = APCSLocalization.SelfLocS4_GetEstLoc(false);
            progDialog.setProgress(100);
            progDialog.dismiss();
            
            Point estPoint = new Point();
            
            if(testMode.equalsIgnoreCase("Debug")||testMode.equalsIgnoreCase("Demo"))
            {
            	estPoint.x = (int) Math.round(estimatedPoint.x * X_RATIO);
            	estPoint.y = (int) Math.round(estimatedPoint.y * Y_RATIO);
            }
            else
            {
            	estPoint = estimatedPoint;
            }
            
            gpsMapView.setLocalize(true);
            gpsMapView.setPoint(estPoint);
		
            gpsMapView.postInvalidate();
		}
		else
		{
			Utilities.displayMsgBox(MSG_TAG, "Online RSS Reading is not valid");
			return;
		}
	}
	
	private boolean UpdateAction_Track(long t)
	{
		long t0 = t;
        //>>> Call the GetDC 5 times to obtain stable heading values
        for (int i = 0; i < ConfigSettings.NUM_DC_READINGS; i++)
        {
            GetDCReading();
        }
        boolean isValidOnlineY = APCSLocalization.SelfLocS0_CreateY(observedRSS);
        if(isValidOnlineY)
        {
        	LocResult.CompassHeadingAngle.add(headingDC);
        	APCSLocalization.SelfLocS1_APSelection();
        	progDialog.setProgress(40);
        	boolean foundCluster = MKFTracking.CoarseLocS2_ChooseRelevantFPs(LocResult.CompassHeadingAngle.get(LocResult.CompassHeadingAngle.size()-1));
        	if (!foundCluster)
        	{
        		Log.d(MSG_TAG, "Can not find common FP");
        		return false;
        	}
        	progDialog.setProgress(60);
        	APCSLocalization.SelfLocS3_L1NormMin();
        	progDialog.setProgress(80);
        	Point estimatedPoint = APCSLocalization.SelfLocS4_GetEstLoc(false);
        	LocResult.ComputedPositionMeas.add(estimatedPoint);
        	progDialog.setProgress(100);
            progDialog.dismiss();
            
        	int validate = 0;        	
        	MKFTracking.ComputeEstimate();
        	validate = MKFTracking.ValidateEstimation();
        	if (validate == 1)
        	{
        		Log.d(MSG_TAG, "Estimation is far from prev point; ignore this update");
        		return false;
        	}
        	else if (validate == 2)
        	{
        		Log.d(MSG_TAG, "Estimation is far from prev point (for 2 times)");
        	}
        	 
            double traveldistance = 0;
        	if (LocResult.EstimatedPositions.size() >= 2)
        	{
        		traveldistance = GeometryFunc.EuclideanDistanceInMeter(LocResult.EstimatedPositions.get(LocResult.EstimatedPositions.size() - 2), LocResult.EstimatedPositions.get(LocResult.EstimatedPositions.size()-1));
        	}
        	
        	DrawSmoothedUpdatesOnMapImage();

        	Log.d(MSG_TAG, DEBUG_trackcount + " " + "Comp Pos:" + LocResult.ComputedPositionMeas.get(LocResult.ComputedPositionMeas.size()-1).x + "," + LocResult.ComputedPositionMeas.get(LocResult.ComputedPositionMeas.size()-1).y);
        	Log.d(MSG_TAG, DEBUG_trackcount + " " + "Est Pos:" + LocResult.EstimatedPositions.get(LocResult.EstimatedPositions.size()-1).x + "," + LocResult.EstimatedPositions.get(LocResult.EstimatedPositions.size()-1).y);
        }
        else
        {
        	Log.d(MSG_TAG, "Not enough valid RSS readings are detected");
        	return false;
        }

        long t3 = new Date().getTime(); //millisecond
        
        LocResult.UpdatesInterval.add((double)((t3 - t0)/1000));//to second
        return true;      
	}

	// Get DC Reading
	private void GetDCReading() 
	{
		// Add DC reading function here
		if(testMode.equalsIgnoreCase("Debug")||testMode.equalsIgnoreCase("Demo"))
		{
			headingDC = -1;
		}
		else
		{
			headingDC = (int) DCValues[0];
		}
	}

	
	// *********************************************************
	// enableWifi
	// *********************************************************
	private void enableWifi() {
		this.wifiManager.setWifiEnabled(true);
		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			// nothing
		}
		Log.d(MSG_TAG, "wifi enabled~");
	}

	// *********************************************************
	// updatePrefSettings
	// *********************************************************
	private void updatePrefSettings() {
		MAX_AP_NUM = new Integer(sharedPrefs.getString("maxAPPref", "100"));
		APSampleCount = new Integer(sharedPrefs.getString("sampleCount", "3"));
		appMode = sharedPrefs.getString("app_mode_pref", "Training mode");
		testMode = sharedPrefs.getString("test_mode_pref", "Debug");
		is4OEnabled = sharedPrefs.getBoolean("4oTraining", false);
		Building = sharedPrefs.getString("building", "CNIB");
		Floor = sharedPrefs.getString("floor", "2");
		numAP = new Integer(sharedPrefs.getString("Num_AP_Loc", "20"));
	}

	// *********************************************************
	// isCollectionComplete
	//
	// @brief: check the status of RSS readings for all
	// orientations. return true if they are all ready
	// *********************************************************
	private boolean isCollectionComplete() {
		return (orientationStatus.get(DBManager.DATABASE_TABLE_N)
				&& orientationStatus.get(DBManager.DATABASE_TABLE_E)
				&& orientationStatus.get(DBManager.DATABASE_TABLE_S) && orientationStatus
				.get(DBManager.DATABASE_TABLE_W));
	}
	
	
	// This is only for debug purpose - Localization function
	public HashMap<String, List<Integer>> CollectOnlineRSS(String rSSfilename) throws NumberFormatException, IOException
	{
		HashMap<String, List<Integer>> observedRSS = new HashMap<String, List<Integer>>();
		int aPSampleCount = 50;
		Matrix tempOnlineRSS = new Matrix(aPSampleCount, LocDB.apMacIDList.size());
		progDialog.setProgress(10);
		
		BufferedReader sr = new BufferedReader(new FileReader(rSSfilename));
		String line;
        String[] splitstrings;
         
        int i = 0;
        int j = 0;
        while ((line = sr.readLine()) != null && i < aPSampleCount)
        {
            splitstrings = line.split(" ");      
            for (String s : splitstrings)
            {
                if (s.trim() != "")
                {
                	tempOnlineRSS.set(i, j, Integer.parseInt(s.trim()));
                    j++;
                }
            }
            i++;
            j = 0;
         }
         sr.close();

         int jj = 0;
         for(String mac : LocDB.apMacIDList)
         {
        	 observedRSS.put(mac, Utilities.intList(tempOnlineRSS.getMatrix(0, aPSampleCount - 1, jj, jj).getIntColumnPackedCopy()));
        	 jj++;
         }
         progDialog.setProgress(20);
		 return observedRSS;	
	}
	
    
    /////////////////////////////////////////////////////////////////////////////
    
    private void DrawSmoothedUpdatesOnMapImage()
    {
    	if(isTrackingOn)
    	{
    		gpsMapView.setTrack(true);
    		Point myPoint = new Point();
    		if(testMode.equalsIgnoreCase("Debug")||testMode.equalsIgnoreCase("Demo")) // Database from PDA, pixel is diff
    		{
    			myPoint.x = (int) Math.round(LocResult.EstimatedPositions.get(LocResult.EstimatedPositions.size()-1).x * MapActivity.X_RATIO);
    			myPoint.y = (int) Math.round(LocResult.EstimatedPositions.get(LocResult.EstimatedPositions.size()-1).y * MapActivity.Y_RATIO);
    		}
    		else
    		{
    			myPoint = LocResult.EstimatedPositions.get(LocResult.EstimatedPositions.size()-1);
    		}
    		gpsMapView.setPoint(myPoint);    	
    		gpsMapView.postInvalidate();
    	}
    	
    	if(isNavigationRunning)
    	{
    		if(!firstloc4navig)
    		{
    			Point est = new Point();
    			if(testMode.equalsIgnoreCase("Debug")||testMode.equalsIgnoreCase("Demo")) // Database from PDA, pixel is diff
        		{
    				est.x = (int) Math.round(LocResult.SmoothedNavigPositions.get(LocResult.SmoothedNavigPositions.size()-1).x * X_RATIO);
    				est.y = (int) Math.round(LocResult.SmoothedNavigPositions.get(LocResult.SmoothedNavigPositions.size()-1).y * Y_RATIO);
        		}
    			else
    			{
    				est = LocResult.SmoothedNavigPositions.get(LocResult.SmoothedNavigPositions.size()-1);
    			}
    			String numst = LocResult.SmoothedNavigPositions.size()+"";
    			gpsMapView.setNavigate(true);
    			gpsMapView.setFirstNavi(false);
    			gpsMapView.setPoint(est);
    			gpsMapView.setText(numst);
    			///gpsMapView.setText(DEBUG_trackcount + "");		
    			gpsMapView.postInvalidate();
    		}
    	}
    }
    
    private void DrawUpdatesOnMapImage(Point est, Point des)
    {
    	Point p1 = new Point();
    	Point p2 = new Point();
    	List<Point> pathPoints = new ArrayList<Point>();
    	
		if(testMode.equalsIgnoreCase("Debug")||testMode.equalsIgnoreCase("Demo")) // Database from PDA, pixel is diff
		{
			p1.x = (int) Math.round(est.x * X_RATIO);
			p1.y = (int) Math.round(est.y * Y_RATIO);
			p2.x = (int) Math.round(des.x * X_RATIO);
			p2.y = (int) Math.round(des.y * Y_RATIO);
			for(int i = 0 ; i < NavigAnalysis.NavigRoutedPath.size();i++)
			{
				Point temp = new Point();
				temp.x = (int) (NavigAnalysis.NavigRoutedPath.get(i).x * MapActivity.X_RATIO);
				temp.y = (int) (NavigAnalysis.NavigRoutedPath.get(i).y * MapActivity.Y_RATIO);
				pathPoints.add(i, temp);
			}	
		}
		else
		{
	    	p1 = est;
	    	p2 = des;
	    	pathPoints = NavigAnalysis.NavigRoutedPath;
		}
    	gpsMapView.setNavigate(true);
    	gpsMapView.setFirstNavi(true);
    	gpsMapView.setPoints(p1,p2);
    	gpsMapView.setRoutePoints(pathPoints);
    	gpsMapView.postInvalidate();
    }
	
    @Override
	public void onInit(int status) {
		// TODO Auto-generated method stub
    	mTts.setLanguage(Locale.US);
	}
    
    public static void Speak(String speech, int queueMode)
    {
    	switch (queueMode)
    	{
    		case TextToSpeech.QUEUE_FLUSH:
    			mTts.speak(speech, TextToSpeech.QUEUE_FLUSH, null);
    			break;
    		case TextToSpeech.QUEUE_ADD:
    			mTts.speak(speech, TextToSpeech.QUEUE_ADD, null);
    			break;
    	}	
    }

    
    private void AnalyzeSpeech(ArrayList<String> matches){
		// Speech recognition for localize
		if(matches.contains("localize")){
			try 
			{
				actionCode = 1;
				APCSLocalization.SetUp(true, true, true, numAP);
				localizeSelfPosition();
			} 
			catch (IOException e) 
			{
				e.printStackTrace();
			}  
		}

		// Speech recognition for tracing
		else if(matches.contains("tracking")){
			actionCode = 2;
			APCSLocalization.SetUp(true, true, true, numAP);   
			KFTracking.Initialization(initVx, initVy, Utilities.intDouble(trackUpdateInterval), ConfigSettings.KALMANFILTER_DIAG_Q, ConfigSettings.KALMANFILTER_DIAG_R, ConfigSettings.KALMANFILTER_DIAG_INITP);
			MKFTracking.SetUseCompass(useDC, false);

			Tracking();
		}

		// Speech recognition for stop
		else if(matches.contains("stop")){
			StopTrack();
		}

		// Speech recognition for navigation
		else if(matches.contains("navigate")){
			actionCode = 3;
			APCSLocalization.SetUp(true, true, true, numAP);    
			KFTracking.Initialization(initVx, initVy, Utilities.intDouble(trackUpdateInterval), ConfigSettings.KALMANFILTER_DIAG_Q, ConfigSettings.KALMANFILTER_DIAG_R, ConfigSettings.KALMANFILTER_DIAG_INITP);
			MKFTracking.SetUseCompass(useDC, false);

			String speech0 = "Please select your destination," +
			"You have choice of meetingroom 218C, storage room" +
			"fountain, elevator";
			Speak(speech0, TextToSpeech.QUEUE_FLUSH);
			level = 1;
		}
		/****For limited number of pre-designed destinations ******/
		else if(matches.contains("meeting room") && level == 1){
			String speech1 = "Your destination is meeting room 218C" +
			"is that correct?";
			Speak(speech1, TextToSpeech.QUEUE_ADD);
			level = 2;
			destID = 1;
		}
		else if(matches.contains("storage room") && level == 1){
			String speech1 = "Your destination is storage room" +
			"is that correct?";
			Speak(speech1, TextToSpeech.QUEUE_ADD);
			level = 2;
			destID = 2;
		}
		else if(matches.contains("fountain") && level == 1){
			String speech1 = "Your destination is fountain" +
			"is that correct?";
			Speak(speech1, TextToSpeech.QUEUE_ADD);
			level = 2;
			destID = 3;
		}
		else if(matches.contains("elevator") && level == 1){
			String speech1 = "Your destination is elevator" +
			"is that correct?";
			Speak(speech1, TextToSpeech.QUEUE_ADD);
			level = 2;
			destID = 4;
		}
		else if ((matches.contains("correct")||matches.contains("yes")) && level == 2){
			String speech3 = "correct";
			Speak(speech3, TextToSpeech.QUEUE_ADD);
			switch(destID){
			case 1: userDestination = "218C";
			destID = 0;
			break;
			case 2: userDestination = "StorageRoom";
			destID = 0;
			break;
			case 3: userDestination = "Fountain";
			destID = 0;
			break;
			case 4: userDestination = "Elevator";
			destID = 0;
			break;
			default: 
				String speech2 = "Please select your destination";
				Speak(speech2, TextToSpeech.QUEUE_FLUSH);
				level = 1;
				break;
			}
			try 
			{
				Navigation();
			} 
			catch (Exception e) {
				e.printStackTrace();
			}
		}
		else if (matches.contains("no") && level == 2){
			String speech2 = "Please re-select your destination";
			Speak(speech2, TextToSpeech.QUEUE_FLUSH);
			level = 1;
		}
		else
		{
			String speech2 = "it is not recognized, please say it again";
			Speak(speech2, TextToSpeech.QUEUE_ADD);
		}
	}

	// *********************************************************
	// selectAPs
	//
	// @brief: select APs according to RSS
	// *********************************************************
	private void selectAPs(List<ScanResult> resultList,
			List<ScanResult> new_resultList, int numAPs) {
		Collections.sort(resultList, new SortByRss());
		new_resultList.clear();

		for (int i = 0; i < numAPs; i++) {
			new_resultList.add(resultList.get(i));
		}
	}

	private void handle_save() {
    	// TODO Auto-generated method stub
    	final EditText input = new EditText(getApplicationContext());
    	AlertDialog.Builder alert=new AlertDialog.Builder(MapActivity.this);
    	
    	alert.setMessage("Please Enter Name of the Map");
    	alert.setView(input);
    	alert.setPositiveButton("Save", new DialogInterface.OnClickListener() {
    		public void onClick(DialogInterface dialog, int whichButton){
    			String value = input.getText().toString(); 
    			mapFile =new File( Environment.getExternalStorageDirectory()+"/Maps/"+value+".jpg");
    			
    			File f =new File( Environment.getExternalStorageDirectory()+"/Maps","tmp.jpg");
    			if(!f.renameTo(mapFile)){
    				Toast.makeText(getApplicationContext(), "could not save", Toast.LENGTH_SHORT).show();
    			}
    			//workingImagePath = mapFile.getAbsolutePath();  
    			//latestbm = BitmapFactory.decodeFile(workingImagePath);
    			//latestbm = gpsMapView.changeView(latestbm, screenWidth, screenHeight);	//resize
    			
    			//gpsMapView.invalidate();
    			
    		}});

    	alert.setNegativeButton("Discard", new DialogInterface.OnClickListener() {
    		public void onClick(DialogInterface dialog, int whichButton) {
    			// Remove the tmp file
    			File f = new File(Environment.getExternalStorageDirectory()+"/Maps","tmp.jpg");
    			f.delete();
    			workingImagePath = null;
    		}
    	});
    	alert.show(); 
    }
    
    private void handle_doubleSave(){
    	after = DCIM.list();
    	List<String> dummy = Arrays.asList(before);
    	for(int i=0;i<after.length;i++){
    		if(!dummy.contains(after[i])){
    			File toBeDeleted = new File(Environment.getExternalStorageDirectory()+"/DCIM/100MEDIA/"+after[i]);	
    			toBeDeleted.delete();
    			break;
    		}

    	}
    }
    
    @Override
	public void onBackPressed() {
		  Intent data = new Intent();
		setResult(Activity.RESULT_CANCELED,data);
		super.finish();
	
	}
	@Override
	public void finish(){
		  Intent data = new Intent();
		if(this.workingImagePath!=null){
			  data.putExtra("mapAbsolutePath", workingImagePath);
			  setResult(RESULT_OK,data);
		}
		else{
			Toast.makeText(getApplicationContext(), "No Change", Toast.LENGTH_SHORT).show();
			setResult(Activity.RESULT_CANCELED,data);
		}
		super.finish();
	}
	
	// *********************************************************
	// SortByRss
	//
	// override comparator to sort by RSSs
	//
	// *********************************************************
	class SortByRss implements Comparator<ScanResult> {
		public int compare(ScanResult s1, ScanResult s2) {
			if (s1.level < s2.level) {
				return 1;
			}
			if (s1.level == s2.level) {
				return 0;
			} else {
				return -1;
			}
		}
	}
	
}