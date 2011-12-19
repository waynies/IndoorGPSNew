package com.IndoorGPS.LocalizerBasicClass;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.List;

import android.R.bool;
import android.graphics.Color;
import android.os.Environment;

//*******************************************************************************
//          class ConfigSettings
//
//*******************************************************************************
public class ConfigSettings
{
	public static enum LocAlg
	{
	    AP_CS_Normal,
	    CS_Normal,
	    AP_CS_4O,
	    CS_4O,
	    AP_CS_4O_DC,
	    CS_4O_DC
	}
	
    //public static Device DEVICE;

    public static String RSC_DIR;

    public static String BUILDING;
    public static String FLOOR;
    
    public static String DB_SUBDIR;
    public static String CSSL_SUBDIR;
    public static String STL_SUBDIR;
    public static String TEST_SUBDIR;
    
    public static String NAV_SUBDIR;
    public static String VI_SUBDIR;
    public static String MAP_SUBDIR;

    public static String AP_FILENAME;
    public static String ESTLOC_PDA_FILENAME;
    public static String ESTLOC_TRACK_FILENAME;
    public static String RAWDATA_FILENAME;
    public static String XML_DATABASE_FILENAME;
    public static String NAVIGATION_FILENAME = null;
    public static String ACC_FILENAME;
    public static String LOGFILENAME = null;
    public static String TEMPSETUPFILENAME;
    public static String NAVLOGFILENAME;
    
    public static int FP_COLOR_STD;
    public static ArrayList<Integer> FP_COLOR_LIST = new ArrayList<Integer>();
    public static HashMap<LocAlg, Color> SELFLOCALG_COLOR_LIST;
    public static Color SELFLOC_WTLOC_COLOR;
    public static Color NAVIGATION_TARGET_COLOR;
    public static List<Color> NAVIGATION_LOC_COLOR;
    public static Color TRACK_MEA_COLOR;
    public static Color TRACK_EST_COLOR;
    public static Color NAVIG_SMOOTH_COLOR;
    public static Color MAP_PTR;
    public static Dictionary<String, Color> MAP_FEATURE_COLOR;
    
    public static double HISTOGRAM_ZERO; 
    public static int MAXDETECTRSSCOUNT;

    public static int RSS_DEFAULT_VALUE;
    public static int MAX_MATCHED_NUM_CLUSTERS;
    public static double THETA_HAT_THRESHOLD_PERCENTAGE;
    public static double CLUSTER_MATHCHED_THRESHOLD_PERC;

    public static int CSLOC_NUM_M;

    public static int MAX_FP_NUM;
    public static int MAX_NO_SAMPLE_TXT;
    public static int HISTOGRAM_INCREMENT;
    
    public static double INF;
    public static double NODE2NODE_DISTANCE_M;
    public static double SRC2NODE_DISTANCE_M;
    public static double NODEMEMBER_DISTANCE_M;
    public static double DEST2NODE_DISTANCE_M;
    public static int OFFPATH_COUNTER_TOL;

    public static double MIN_SEPARATION_M;
    public static double DESTINATION_TOL_M;

    public static int DEFAULTVOICEDURATION;

    public static String STARTTIMESTAMP;

    public static double CLUSTER_PREF_FRAC;


    public static int UPDATELOC_MAXDIST;
    public static double FIL1_ALPHA_NORMAL;
    public static double FIL1_ALPHA_IGNORED;

    public static double USER_STEPS_SIZE_M;


    public static int DCHEADING_NORTH_OFFSET;

    public static int RSS_SAMPLES_INTERVAL;

    public static int CLUSTER_MATCH_METRIC_CHOICE;       


    // NEW ADDITION TO MKF-TRACKING

    public static double METER_2_PIXEL_X;
    public static double METER_2_PIXEL_Y; 

    public static boolean EXCLUDE_INVALIDRSS_COALOC;
    public static boolean EXCLUDE_INVALIDRSS_FINELOC;
    public static int TRACKING_NUM_SAMPLE;
    public static int STARTPT_NUM_SAMPLE;

    public static double KALMANFILTER_DT;
    public static double KALMANFILTER_DIAG_INITP;
    public static double KALMANFILTER_DIAG_R;
    public static double KALMANFILTER_DIAG_Q;

    public static double TURNING_TOL_M;
    public static double COARSEDIST_R_M;

    public static int NUM_AP_USED;
    public static double DEFAULT_TURNING_STEPSIZE_M;

    public static int MIN_NUM_VALID_ONLINE;

    public static boolean DISPLAY_MEAS;

    public static int NUM_DC_READINGS;

    public static double MAX_DISTANCE_BTW_UPDATES_M;


    // NAVIGATION
    public static double MAX_CLOSET_DISTANCE_TO_PATH_M;
    public static int SMOOTH_TRACE_WINDOW_SIZE;
    public static double WARNING_THRU_DOOR_M;
    public static double WARNING_ADVANCE_TURN_M;
    public static double WARNING_TURN_M;
    public static double WARNING_DESCRIPTION_M;
    public static double MAX_DESCRIPTION_TO_PATH_M;

    public static int WARNING_REPEAT_COUNT;

    public static int USE_SMOOTH_AVERAGE;

    public static int OFFPATH_DIR_COUNT;

    public static int NO_COMMON_FP_COUNT;
    public static int EXCEED_MAX_DIST_BTW_UPDATE_COUNT;

    public static int NUM_ROW_RANDOMMAT_FINE_LOC;

    public static boolean LOAD_FP_XMLDB;

    public static int REPEAT_CMD_LIFETIME_S;

    //*********************************************************
    //		Initialisation
    //
    //
    //*********************************************************
    public static void Initialization(String filename)
    {
        //STARTTIMESTAMP = "yyMMddHHmm";
        
        Date dt = new Date();
        int year = dt.getYear();
        int month = dt.getMonth();
        int day = dt.getDay();
		String curTime = year + "_" + month + "_" + day;	
		STARTTIMESTAMP = curTime;
		
        InitFromCFGFile(filename);
        //InitColor();

        // Define corresponding directories and files from RSC_DIR
        DB_SUBDIR = RSC_DIR + "/Database";
        CSSL_SUBDIR = RSC_DIR + "/Database/Loc_SelfLoc";
        TEST_SUBDIR = RSC_DIR + "/Database/TestTrace";
        
        NAV_SUBDIR = RSC_DIR + "/Database/Navigation";
        VI_SUBDIR = RSC_DIR + "/VoiceInstr";
        MAP_SUBDIR = RSC_DIR + "/Maps";
        STL_SUBDIR = RSC_DIR + "/SubjectTestLog";

        AP_FILENAME = RSC_DIR + "/Info/detectedAPs_" + STARTTIMESTAMP + ".txt";
        RAWDATA_FILENAME = RSC_DIR + "/Info/RawData_" + STARTTIMESTAMP + ".txt";
        ESTLOC_PDA_FILENAME = RSC_DIR + "/Info/EstimatedSelfLoc_" + STARTTIMESTAMP + ".txt";
        ESTLOC_TRACK_FILENAME = RSC_DIR + "/Info/Tracking_" + STARTTIMESTAMP + ".txt";
        XML_DATABASE_FILENAME = RSC_DIR + "/Database/Database.xml";
        NAVLOGFILENAME = RSC_DIR + "/Info/Navigation_" + STARTTIMESTAMP + ".txt";
        ACC_FILENAME = RSC_DIR + "/Info/AccTimeSample_" + STARTTIMESTAMP + ".txt";
        TEMPSETUPFILENAME = RSC_DIR + "/Info/TempNavigSetup_" + STARTTIMESTAMP + ".txt";

        // Fingerprinting parameters
        RSS_DEFAULT_VALUE = -110;

        MAX_FP_NUM = 300;
        MAX_NO_SAMPLE_TXT = 20;
        HISTOGRAM_INCREMENT = 5;
        HISTOGRAM_ZERO = 0.001;

        // Routing Parameters
        INF = 999999;
    }

    //*********************************************************
    //		InitFromCFGFile
    //
    //
    //*********************************************************
    public static void InitFromCFGFile(String filename)
    {
    	// not implemented yet
    	File root = Environment.getExternalStorageDirectory();
    	
    	ConfigFileHandler cfh = new ConfigFileHandler(filename);
    	cfh.ParseSettingFile();
    	
    	//RSC_DIR = cfh.IsDefined("RSC_DIR") ? cfh.GetValueString("RSC_DIR"): "/mnt/sdcard/IndoorLocResouce";
    	RSC_DIR = cfh.IsDefined("RSC_DIR") ? cfh.GetValueString("RSC_DIR"): root + "/IndoorLocResouce";
    	BUILDING = cfh.IsDefined("BUILDING")? cfh.GetValueString("BUILDING"): "CNIB";
    	FLOOR = cfh.IsDefined("FLOOR")? cfh.GetValueString("FLOOR"): "2";
    	
    	// CS Localization parameters
    	MAX_MATCHED_NUM_CLUSTERS = cfh.IsDefined("MAX_MATCHED_NUM_CLUSTERS")? cfh.GetValueInt("MAX_MATCHED_NUM_CLUSTERS"): 2;
        THETA_HAT_THRESHOLD_PERCENTAGE = cfh.IsDefined("THETA_HAT_THRESHOLD_PERCENTAGE") ? cfh.GetValueDouble("THETA_HAT_THRESHOLD_PERCENTAGE") : 0.8;
        CLUSTER_MATHCHED_THRESHOLD_PERC = cfh.IsDefined("CLUSTER_MATHCHED_THRESHOLD_PERC") ? cfh.GetValueDouble("CLUSTER_MATHCHED_THRESHOLD_PERC") : 0.99;

        // Routing Parameters
        DEST2NODE_DISTANCE_M = cfh.IsDefined("DEST2NODE_DISTANCE_M") ? cfh.GetValueDouble("DEST2NODE_DISTANCE_M") : 3;
        NODE2NODE_DISTANCE_M = cfh.IsDefined("NODE2NODE_DISTANCE_M") ? cfh.GetValueDouble("NODE2NODE_DISTANCE_M") : 4;
        SRC2NODE_DISTANCE_M = cfh.IsDefined("SRC2NODE_DISTANCE_M") ? cfh.GetValueDouble("SRC2NODE_DISTANCE_M") : 2;
        OFFPATH_COUNTER_TOL = cfh.IsDefined("OFFPATH_COUNTER_TOL") ? cfh.GetValueInt("OFFPATH_COUNTER_TOL") : 3;

        // Loc Update Parameters
        MAXDETECTRSSCOUNT = cfh.IsDefined("MAXDETECTRSSCOUNT") ? cfh.GetValueInt("MAXDETECTRSSCOUNT") : 10;

        // Navigation Parameters
        MIN_SEPARATION_M = cfh.IsDefined("MIN_SEPARATION_M") ? cfh.GetValueDouble("MIN_SEPARATION_M") : 1;
        DESTINATION_TOL_M = cfh.IsDefined("DESTINATION_TOL_M") ? cfh.GetValueDouble("DESTINATION_TOL_M") : 2.5;
        DEFAULTVOICEDURATION = cfh.IsDefined("DEFAULTVOICEDURATION") ? cfh.GetValueInt("DEFAULTVOICEDURATION") : 1000;


        CLUSTER_PREF_FRAC = cfh.IsDefined("CLUSTER_PREF_FRAC") ? cfh.GetValueDouble("CLUSTER_PREF_FRAC") : 1.0;
        UPDATELOC_MAXDIST = cfh.IsDefined("UPDATELOC_MAXDIST") ? cfh.GetValueInt("UPDATELOC_MAXDIST") : 100;

        FIL1_ALPHA_NORMAL = cfh.IsDefined("FIL1_ALPHA_NORMAL") ? cfh.GetValueDouble("FIL1_ALPHA_NORMAL") : 0.4;
        FIL1_ALPHA_IGNORED = cfh.IsDefined("FIL1_ALPHA_IGNORED") ? cfh.GetValueDouble("FIL1_ALPHA_IGNORED") : 0.9;

        USER_STEPS_SIZE_M = cfh.IsDefined("USER_STEPS_SIZE_M") ? cfh.GetValueDouble("USER_STEPS_SIZE_M") : 0.6;


        DCHEADING_NORTH_OFFSET = cfh.IsDefined("DCHEADING_NORTH_OFFSET") ? cfh.GetValueInt("DCHEADING_NORTH_OFFSET") : 10;

        RSS_SAMPLES_INTERVAL = cfh.IsDefined("RSS_SAMPLES_INTERVAL") ? cfh.GetValueInt("RSS_SAMPLES_INTERVAL") : 1;

        CLUSTER_MATCH_METRIC_CHOICE = cfh.IsDefined("CLUSTER_MATCH_METRIC_CHOICE") ? cfh.GetValueInt("CLUSTER_MATCH_METRIC_CHOICE") : 2;

        // NEW ADDITION TO MKF-TRACKING
        METER_2_PIXEL_X = cfh.IsDefined("METER_2_PIXEL_X") ? cfh.GetValueDouble("METER_2_PIXEL_X") : 16;
        METER_2_PIXEL_Y = cfh.IsDefined("METER_2_PIXEL_Y") ? cfh.GetValueDouble("METER_2_PIXEL_Y") : 20;

        EXCLUDE_INVALIDRSS_FINELOC = cfh.IsDefined("EXCLUDE_INVALIDRSS_FINELOC") ? cfh.GetValueBool("EXCLUDE_INVALIDRSS_FINELOC") : true;
        EXCLUDE_INVALIDRSS_COALOC = cfh.IsDefined("EXCLUDE_INVALIDRSS_COALOC") ? cfh.GetValueBool("EXCLUDE_INVALIDRSS_COALOC") : true;

        KALMANFILTER_DT = cfh.IsDefined("KALMANFILTER_DT") ? cfh.GetValueDouble("KALMANFILTER_DT") : 1;
        KALMANFILTER_DIAG_INITP = cfh.IsDefined("KALMANFILTER_DIAG_INITP") ? cfh.GetValueDouble("KALMANFILTER_DIAG_INITP") : 100;
        KALMANFILTER_DIAG_Q = cfh.IsDefined("KALMANFILTER_DIAG_Q") ? cfh.GetValueDouble("KALMANFILTER_DIAG_Q") : 1;
        KALMANFILTER_DIAG_R = cfh.IsDefined("KALMANFILTER_DIAG_R") ? cfh.GetValueDouble("KALMANFILTER_DIAG_R") : 10;

        TRACKING_NUM_SAMPLE = cfh.IsDefined("TRACKING_NUM_SAMPLE") ? cfh.GetValueInt("TRACKING_NUM_SAMPLE") : 1;
        STARTPT_NUM_SAMPLE = cfh.IsDefined("STARTPT_NUM_SAMPLE") ? cfh.GetValueInt("STARTPT_NUM_SAMPLE") : 3;

        TURNING_TOL_M = cfh.IsDefined("TURNING_TOL_M") ? cfh.GetValueDouble("TURNING_TOL_M") : 1.5;
        COARSEDIST_R_M = cfh.IsDefined("COARSEDIST_R_M") ? cfh.GetValueDouble("COARSEDIST_R_M") : 2.5;

        NUM_AP_USED = cfh.IsDefined("NUM_AP_USED") ? cfh.GetValueInt("NUM_AP_USED") : 10;

        DEFAULT_TURNING_STEPSIZE_M = cfh.IsDefined("DEFAULT_TURNING_STEPSIZE_M") ? cfh.GetValueDouble("DEFAULT_TURNING_STEPSIZE_M") : 1;

        MIN_NUM_VALID_ONLINE = cfh.IsDefined("MIN_NUM_VALID_ONLINE") ? cfh.GetValueInt("MIN_NUM_VALID_ONLINE") : 8;

        DISPLAY_MEAS = cfh.IsDefined("DISPLAY_MEAS") ? cfh.GetValueBool("DISPLAY_MEAS") : false;

        NUM_DC_READINGS = cfh.IsDefined("NUM_DC_READINGS") ? cfh.GetValueInt("NUM_DC_READINGS") : 5;

        MAX_DISTANCE_BTW_UPDATES_M = cfh.IsDefined("MAX_DISTANCE_BTW_UPDATES_M") ? cfh.GetValueDouble("MAX_DISTANCE_BTW_UPDATES_M") : 1.5;
        
        MAX_CLOSET_DISTANCE_TO_PATH_M = cfh.IsDefined("MAX_CLOSET_DISTANCE_TO_PATH_M") ? cfh.GetValueDouble("MAX_CLOSET_DISTANCE_TO_PATH_M") : 1.6;
        SMOOTH_TRACE_WINDOW_SIZE = cfh.IsDefined("SMOOTH_TRACE_WINDOW_SIZE") ? cfh.GetValueInt("SMOOTH_TRACE_WINDOW_SIZE") : 5;

        WARNING_THRU_DOOR_M = cfh.IsDefined("WARNING_THRU_DOOR_M") ? cfh.GetValueDouble("WARNING_THRU_DOOR_M") : 2;
        WARNING_ADVANCE_TURN_M = cfh.IsDefined("WARNING_ADVANCE_TURN_M") ? cfh.GetValueDouble("WARNING_ADVANCE_TURN_M") : 5;
        WARNING_TURN_M = cfh.IsDefined("WARNING_TURN_M") ? cfh.GetValueDouble("WARNING_TURN_M") : 2;

        USE_SMOOTH_AVERAGE = cfh.IsDefined("USE_SMOOTH_AVERAGE") ? cfh.GetValueInt("USE_SMOOTH_AVERAGE") : 0;

        OFFPATH_DIR_COUNT = cfh.IsDefined("OFFPATH_DIR_COUNT") ? cfh.GetValueInt("OFFPATH_DIR_COUNT") : 3;

        WARNING_DESCRIPTION_M = cfh.IsDefined("WARNING_DESCRIPTION_M") ? cfh.GetValueDouble("WARNING_DESCRIPTION_M") : 2;
        MAX_DESCRIPTION_TO_PATH_M = cfh.IsDefined("MAX_DESCRIPTION_TO_PATH_M") ? cfh.GetValueDouble("MAX_DESCRIPTION_TO_PATH_M") : 2.5;

        WARNING_REPEAT_COUNT = cfh.IsDefined("WARNING_REPEAT_COUNT") ? cfh.GetValueInt("WARNING_REPEAT_COUNT") : 3;
        NO_COMMON_FP_COUNT = cfh.IsDefined("NO_COMMON_FP_COUNT") ? cfh.GetValueInt("NO_COMMON_FP_COUNT") : 3;
        EXCEED_MAX_DIST_BTW_UPDATE_COUNT = cfh.IsDefined("EXCEED_MAX_DIST_BTW_UPDATE_COUNT") ? cfh.GetValueInt("EXCEED_MAX_DIST_BTW_UPDATE_COUNT") : 2;

        NUM_ROW_RANDOMMAT_FINE_LOC = cfh.IsDefined("NUM_ROW_RANDOMMAT_FINE_LOC") ? cfh.GetValueInt("NUM_ROW_RANDOMMAT_FINE_LOC") : 6;


        LOAD_FP_XMLDB = cfh.IsDefined("LOAD_FP_XMLDB") ? cfh.GetValueBool("LOAD_FP_XMLDB") : false;

        REPEAT_CMD_LIFETIME_S = cfh.IsDefined("REPEAT_CMD_LIFETIME_S") ? cfh.GetValueInt("REPEAT_CMD_LIFETIME_S") : 6;

        cfh.Dispose();    	
    }

    //*********************************************************
    //		InitColor
    //
    //
    //*********************************************************
    public static void InitColor()
    {
    	// TODO: implement colors
    	// Not Finished
    	//List<Color> FP_COLOR_LIST = new List<Color>();
        FP_COLOR_STD = Color.BLUE;

        FP_COLOR_LIST = new ArrayList<Integer>();
        FP_COLOR_LIST.add(Color.CYAN);
        FP_COLOR_LIST.add(Color.GRAY);
        FP_COLOR_LIST.add(Color.GREEN);
        FP_COLOR_LIST.add(Color.LTGRAY);
        FP_COLOR_LIST.add(Color.MAGENTA);
        FP_COLOR_LIST.add(Color.RED);
        FP_COLOR_LIST.add(Color.TRANSPARENT);
        FP_COLOR_LIST.add(Color.YELLOW);
        FP_COLOR_LIST.add(Color.CYAN);
        FP_COLOR_LIST.add(Color.GRAY);
        FP_COLOR_LIST.add(Color.GREEN);
        FP_COLOR_LIST.add(Color.LTGRAY);
        FP_COLOR_LIST.add(Color.MAGENTA);
        FP_COLOR_LIST.add(Color.RED);
        FP_COLOR_LIST.add(Color.TRANSPARENT);
        FP_COLOR_LIST.add(Color.YELLOW);
          
    }
       
}