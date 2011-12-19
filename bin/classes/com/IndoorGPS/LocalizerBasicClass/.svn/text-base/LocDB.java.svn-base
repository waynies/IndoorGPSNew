package com.IndoorGPS.LocalizerBasicClass;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import android.util.Log;

import com.IndoorGPS.Utilities;

import Jama.Matrix;

public class LocDB {
	
    private static String building = null;
    private static String floor = null;

    private static boolean useCluster = true;
    private static boolean load4OrientationDB = true;

    // Database files
    private static String macIDListFile = null;
    private static String excludeMacIDListFile = null;

    private static String fpFile = null;
    private static String mapInfoFile = null;
    
    private static HashMap<Orientation, String> psiAPDataFile = new HashMap<Orientation,String>();
    private static HashMap<Orientation, String> clusterFile = new HashMap<Orientation, String>();
    private static HashMap<Orientation, String> clusterAvgFile = new HashMap<Orientation, String>();
    private static HashMap<Orientation, String> clusterVarFile = new HashMap<Orientation, String>();
            
    private static String testDataFile = new String();
    
    // Actual Database
    public static List<String> apMacIDList = new ArrayList<String>();
    public static List<Integer> validMacIndexList = new ArrayList<Integer>();
    public static List<String> validMacIDList = new ArrayList<String>();

    public static List<Integer> x00List = new ArrayList<Integer>();
    public static List<Integer> y00List = new ArrayList<Integer>();

    public static HashMap<Orientation, Matrix> Psi = new HashMap<Orientation, Matrix>();
    public static HashMap<Orientation, Matrix> clusterAvgRSS = new HashMap<Orientation, Matrix>();
    public static HashMap<Orientation, Matrix> clusterVarRSS = new HashMap<Orientation, Matrix>();

    public static HashMap<Orientation, List<Integer>> clusterIndexList = new HashMap<Orientation, List<Integer>>();
    public static HashMap<Orientation, HashMap<Integer, List<Integer>>> clusterHeadList = new HashMap<Orientation, HashMap<Integer, List<Integer>>>(); 
    
    public static int numAPs;
    public static int numRPs;
    
    //public static int NUM_DEMO = 12; //12  
    
    public static int numTests = 128; //77
    
    public static Matrix PsiTest;

    public static List<Integer> turningFPSet = new ArrayList<Integer>();
    public static List<Integer[]> turningXYBoundarySet = new ArrayList<Integer[]>();
    public static List<HashMap<Integer,Integer[]>> turningFPMembersSet = new ArrayList<HashMap<Integer,Integer[]>>();

    // Error Msg
    private static List<String> missingFiles = new ArrayList<String>();

    public static boolean LoadAPMACList(String mybuilding, String myfloor) throws IOException
    {
        missingFiles.clear();

        macIDListFile = String.format("\\{0}\\MAC_ID_{1}_{2}.txt", ConfigSettings.CSSL_SUBDIR, mybuilding, myfloor);
        excludeMacIDListFile = String.format("\\{0}\\EXCLUDE_MAC_ID_{1}_{2}.txt", ConfigSettings.CSSL_SUBDIR, mybuilding, myfloor);
        LoadMACListFromFile();
       
        return missingFiles.size() == 0 ? true : false;
    }
    
    public static boolean LoadDatabase(String mybuilding, String myfloor, boolean myusecluster, boolean myload4OrientationDB) throws IOException
    {
        missingFiles.clear();
        boolean needreload = false;

        //if(building != mybuilding || floor != myfloor)
        
        	building = mybuilding;
        	floor = myfloor;
        	
        	// Set filenames according to building/floor
            macIDListFile = String.format("%s/MAC_ID_%s_%s.txt", ConfigSettings.CSSL_SUBDIR, building, floor);
            //macIDListFile = "/mnt/sdcard/IndoorLocResouce/Database/CS_SelfLoc/MAC_ID_CNIB2.txt";
            excludeMacIDListFile = String.format("%s/EXCLUDE_MAC_ID_%s_%s.txt", ConfigSettings.CSSL_SUBDIR, mybuilding, myfloor);

            fpFile = String.format("%s/FP_%s_%s.txt", ConfigSettings.CSSL_SUBDIR, building, floor);
            mapInfoFile = String.format("%s/MapInfo_%s_%s.txt", ConfigSettings.CSSL_SUBDIR, building, floor);
            psiAPDataFile.put(DB.DBTag_N, String.format("%s/Psi_APFP_%s_%s.txt", ConfigSettings.CSSL_SUBDIR, building, floor));
            clusterFile.put(DB.DBTag_N, String.format("%s/ClusterIndex_%s_%s.txt", ConfigSettings.CSSL_SUBDIR, building, floor));
            clusterAvgFile.put(DB.DBTag_N, String.format("%s/ClusterAvg_%s_%s.txt", ConfigSettings.CSSL_SUBDIR, building, floor));
            clusterVarFile.put(DB.DBTag_N, String.format("%s/ClusterVar_%s_%s.txt", ConfigSettings.CSSL_SUBDIR, building, floor));
            
            //testDataFile = String.format("%s/trace0_onlineRSS_1008091245_RSS.txt", ConfigSettings.TEST_SUBDIR);
            testDataFile = String.format("%s/Test1.txt", ConfigSettings.TEST_SUBDIR);

            for (Orientation tag : DB.DBTag_4O)
            {
            	psiAPDataFile.put(tag, String.format("%s/Psi_APFP_%s_%s_%s.txt", ConfigSettings.CSSL_SUBDIR, tag.toString(), building, floor));
                clusterFile.put(tag, String.format("%s/ClusterIndex_%s_%s_%s.txt", ConfigSettings.CSSL_SUBDIR, tag.toString(), building, floor));
                clusterAvgFile.put(tag, String.format("%s/ClusterAvg_%s_%s_%s.txt", ConfigSettings.CSSL_SUBDIR, tag.toString(), building, floor));
                clusterVarFile.put(tag, String.format("%s/ClusterVar_%s_%s_%s.txt", ConfigSettings.CSSL_SUBDIR, tag.toString(), building, floor));

            }
            
            LoadMACListFromFile();
            LoadFPsListFromFile();

            needreload = true;
        

        if (needreload || load4OrientationDB != myload4OrientationDB || useCluster != myusecluster)
        {
            load4OrientationDB = myload4OrientationDB;
            useCluster = myusecluster;

            if (load4OrientationDB)
            {
                for (Orientation tag : DB.DBTag_4O)
                {                        
                    LoadPsiFromFile(tag, apMacIDList.size(), numRPs);
                    
                    if (useCluster)
                    {
                        LoadClusterIndexFromFile(tag);
                        LoadClusterAvgVarFile(tag, apMacIDList.size(), clusterHeadList.get(tag).size(), true);
                        LoadClusterAvgVarFile(tag, apMacIDList.size(), clusterHeadList.get(tag).size(), false);
                    }
                }
            }
            else
            {                    
                LoadPsiFromFile(DB.DBTag_N, apMacIDList.size(), numRPs);

                if (useCluster)
                {
                    LoadClusterIndexFromFile(DB.DBTag_N);
                    LoadClusterAvgVarFile(DB.DBTag_N, apMacIDList.size(), clusterHeadList.get(DB.DBTag_N).size(), true);
                    LoadClusterAvgVarFile(DB.DBTag_N, apMacIDList.size(), clusterHeadList.get(DB.DBTag_N).size(), false);
                }
            }
        }
        
        LoadPsiTestFromFile(apMacIDList.size(), numTests);
        
        return missingFiles.size() == 0 ? true : false;
        //UtilitiesFunc.WriteLineInFile(ConfigSettings.LOGFILENAME, string.Format("Load DB: numAPs={0}; numRPs={0}", numAPs, numRPs));	
    }
    
    public static boolean LoadAdditionalDatabase(String mybuilding, String myfloor, boolean myusecluster, boolean myload4OrientationDB) throws IOException
    {
        missingFiles.clear();
        boolean needreload = false;

        //if(building != mybuilding || floor != myfloor)
        //{
        	building = mybuilding;
        	floor = myfloor;
        	
        	fpFile = String.format("%s/FP_%s_%s.txt", ConfigSettings.CSSL_SUBDIR, building, floor);
            clusterFile.put(DB.DBTag_N, String.format("%s/ClusterIndex_%s_%s.txt", ConfigSettings.CSSL_SUBDIR, building, floor));
            
            for (Orientation tag : DB.DBTag_4O)
            {
                clusterFile.put(tag, String.format("%s/ClusterIndex_%s_%s_%s.txt", ConfigSettings.CSSL_SUBDIR, tag.toString(), building, floor));
            }
            
            LoadFPsListFromFile();

            needreload = true;
        //}

        if (needreload || load4OrientationDB != myload4OrientationDB || useCluster != myusecluster)
        {
            load4OrientationDB = myload4OrientationDB;
            useCluster = myusecluster;

            if (load4OrientationDB)
            {
                for (Orientation tag : DB.DBTag_4O)
                {                        
                	if (useCluster)
                    {
                        LoadClusterIndexFromFile(tag);
                    }
                }
            }
            else
            {                    
                if (useCluster)
                {
                    LoadClusterIndexFromFile(DB.DBTag_N);
                 }
            }
        }
        return missingFiles.size() == 0 ? true : false;
        //UtilitiesFunc.WriteLineInFile(ConfigSettings.LOGFILENAME, string.Format("Load DB: numAPs={0}; numRPs={0}", numAPs, numRPs));	
    }
    
    
    public static boolean LoadMapInfo() throws IOException
    {
        if (Utilities.IsValidFilePath(mapInfoFile))
        {
            turningFPSet.clear();
            

            String line;
            String[] splitstrings, s1, s2;

            BufferedReader sr = new BufferedReader(new FileReader(mapInfoFile));
            while ((line = sr.readLine()) != null)
            {
                splitstrings = line.split(":");
                if (splitstrings[0].matches("TFP"))
                {
                    turningFPSet.add(Integer.parseInt(splitstrings[1]));
                   
                    HashMap<Integer, Integer[]> dirMembers = new HashMap<Integer, Integer[]>();
                    for (int i = 2; i < splitstrings.length; i++)
                    {
                       
                        s1 = splitstrings[i].split("=");
                        s2 = s1[1].split(",");
                        
                        int dir = Integer.parseInt(s1[0]);

                        //dirMembers.put(dir, new int[s2.length]);
                        Integer[] s22 = new Integer[s2.length];
                        for (int j = 0; j < s2.length; j++)
                        {
                           s22[j] = Integer.parseInt(s2[j]);
                        }
                        dirMembers.put(dir, s22);
                    }
                    turningFPMembersSet.add(dirMembers);
                }
            }
            sr.close();

            DefineTurningPtXYBoundary();
            
        }
        else
        {
            missingFiles.add(mapInfoFile);                
        }
        return missingFiles.size() == 0 ? true : false;
    }
    
    public static HashMap<String, List<Integer>> GetTestRSS_Y(Orientation tag, int col)
    {
    	HashMap<String, List<Integer>> apRSSDict = new HashMap<String, List<Integer>>();
    	List<Integer> rss_temp = new ArrayList<Integer>();
        for (int i = 0; i < validMacIDList.size(); i++)
        {
        	rss_temp.clear();
            String mac = validMacIDList.get(i);
            apRSSDict.put(mac, new ArrayList<Integer>());
            //System.out.println((int) Psi.get(tag).get(i, col));
            //rss_temp = (int) Psi.get(tag).get(i, col);
            apRSSDict.put(mac, Utilities.inListint((int) Psi.get(tag).get(i, col)));
        }
        return apRSSDict;
    }
    
    
    public static HashMap<String, List<Integer>> GetRSS_Y(int col)
    {
    	HashMap<String, List<Integer>> apRSSDict = new HashMap<String, List<Integer>>();
    	List<Integer> rss_temp = new ArrayList<Integer>();
        for (int i = 0; i < validMacIDList.size(); i++)
        {
        	rss_temp.clear();
            String mac = validMacIDList.get(i);
            apRSSDict.put(mac, new ArrayList<Integer>());
            //System.out.println((int) Psi.get(tag).get(i, col));
            //rss_temp = (int) Psi.get(tag).get(i, col);
            apRSSDict.put(mac, Utilities.inListint((int) PsiTest.get(i, col)));
        }
        return apRSSDict;
    }
    
    //////// Private Functions
    private static void LoadMACListFromFile() throws IOException
    {
    	if (Utilities.IsValidFilePath(macIDListFile))
        {
            apMacIDList.clear();
            validMacIDList.clear();
            validMacIndexList.clear();

            String line;
            BufferedReader sr = new BufferedReader(new FileReader(macIDListFile));
            while ((line = sr.readLine()) != null)
            {
                apMacIDList.add(line.trim());
                validMacIDList.add(line.trim());
            }
            sr.close();


            for (int i = 0; i < apMacIDList.size(); i++)
            {
                validMacIndexList.add(i);
            }

            if (Utilities.IsValidFilePath(excludeMacIDListFile))
            {                    
                validMacIDList.clear();
            	sr = new BufferedReader(new FileReader(excludeMacIDListFile));
                //Utilities.WriteLineInFile(ConfigSettings.LOGFILENAME, ">>> Remove MAC");
            	while ((line = sr.readLine()) != null)
                {
                    int apindex = apMacIDList.indexOf(line.trim());
                   
                    if (apindex >= 0)
                    {
                        //UtilitiesFunc.WriteLineInFile(ConfigSettings.LOGFILENAME, " >>" + apindex.ToString());
                        validMacIndexList.remove(apindex);
                    }
                }
                sr.close();

                validMacIDList.clear();
                for (int api : validMacIndexList)
                {
                    validMacIDList.add(apMacIDList.get(api));
                }

            }

            numAPs = validMacIDList.size();
        }
        else
        {
            missingFiles.add(macIDListFile);
        }
    	

    }
    
    /// <summary>
    /// Load ordered refpts coordinate into database
    /// </summary>
    private static void LoadFPsListFromFile() throws NumberFormatException, IOException
    {
    	if (Utilities.IsValidFilePath(fpFile))
        {
            x00List.clear();
            y00List.clear();

            String line;
            String[] splitstrings;
            int x, y;

            BufferedReader sr = new BufferedReader(new FileReader(fpFile));

            while ((line = sr.readLine()) != null)
            {
                splitstrings = line.split(",");
                x = Integer.parseInt(splitstrings[0]);
                y = Integer.parseInt(splitstrings[1]);

                x00List.add(x);
                y00List.add(y);                   
            }
            sr.close();

            numRPs = x00List.size();                                
        }
        else
        {
            missingFiles.add(fpFile);
        }
    	
    }
    
    /// <summary>
    /// Load the psi matrix from file. Each line is a row; each column entry is separted by a space
    /// </summary>
    /// <param name="psifilename"></param>
    /// <returns></returns>
    private static void LoadPsiFromFile(Orientation tag, int rownum, int colnum) throws IOException
    {
        String psifilename = psiAPDataFile.get(tag);
        Matrix tempPsi = new Matrix(rownum, colnum);

        if (Utilities.IsValidFilePath(psifilename))
        {
            BufferedReader sr = new BufferedReader(new FileReader(psifilename));
            String line;
            String[] splitstrings;
            
            int i = 0;
            int j = 0;

            while ((line = sr.readLine()) != null)
            {
                splitstrings = line.split(" ");
                for (String s : splitstrings)
                {
                    if (s.trim() != "")
                    {
                        tempPsi.set(i, j, Double.parseDouble(s.trim()));
                        j++;
                    }
                }

                i++;
                j = 0;
            }
            sr.close();


            // IF THERE EXISTS APS TO BE EXCLUDED
            if (numAPs < apMacIDList.size())
            {

                Psi.put(tag, tempPsi.getMatrix(Utilities.Listint(validMacIndexList), 0, colnum-1));
            }
            else
            {
                Psi.put(tag, tempPsi.copy());
            }
            Matrix psi_temp = Psi.get(tag);
            System.out.println("Psi loaded");
            //UtilitiesFunc.WriteLineInFile(ConfigSettings.LOGFILENAME, string.Format(">>> Load PSI {0} - {1}x{2}", tag, Psi[tag].RowDimension, Psi[tag].ColumnDimension));
        }
        else
        {
            missingFiles.add(psifilename);
        }
    }
    
    
    private static void LoadPsiTestFromFile(int rownum, int colnum) throws IOException
    {
        String psitestfilename = testDataFile;
        Matrix tempPsi = new Matrix(rownum, colnum);

        if (Utilities.IsValidFilePath(psitestfilename))
        {
            BufferedReader sr = new BufferedReader(new FileReader(psitestfilename));
            String line;
            String[] splitstrings;
            
            int i = 0;
            int j = 0;

            while ((line = sr.readLine()) != null)
            {
                splitstrings = line.split(" ");
                for (String s : splitstrings)
                {
                    if (s.trim() != "")
                    {
                        tempPsi.set(i, j, Double.parseDouble(s.trim()));
                        j++;
                    }
                }

                i++;
                j = 0;
            }
            sr.close();


            // IF THERE EXISTS APS TO BE EXCLUDED
            if (numAPs < apMacIDList.size())
            {

                PsiTest = tempPsi.getMatrix(Utilities.Listint(validMacIndexList), 0, colnum-1).copy();
            }
            else
            {
                PsiTest = tempPsi.copy();
            }
            
            System.out.println("Psi Test loaded");
            //UtilitiesFunc.WriteLineInFile(ConfigSettings.LOGFILENAME, string.Format(">>> Load PSI {0} - {1}x{2}", tag, Psi[tag].RowDimension, Psi[tag].ColumnDimension));
        }
        else
        {
            missingFiles.add(psitestfilename);
        }
    }
    
    /// <summary>
    /// Load the cluster head index into a list from file: North
    /// </summary>
    private static void LoadClusterIndexFromFile(Orientation tag) throws IOException
    {
        String file = clusterFile.get(tag);

        if (Utilities.IsValidFilePath(file))
        {
            if (clusterHeadList.containsKey(tag))
            {
                clusterIndexList.get(tag).clear();
                clusterHeadList.get(tag).clear();
            }
            else
            {
                clusterIndexList.put(tag, new ArrayList<Integer>());
                clusterHeadList.put(tag, new HashMap<Integer, List<Integer>>());
            }

            String line;
            
            BufferedReader sr = new BufferedReader(new FileReader(file));

            while ((line = sr.readLine()) != null)
            {
                clusterIndexList.get(tag).add(Integer.parseInt(line.trim()));                    
            }
            sr.close();

            
            List<Integer> cheadlist = Utilities.uniqueArray(clusterIndexList.get(tag));
            for (int chead : cheadlist)
            {
            	clusterHeadList.get(tag).put(chead, new ArrayList<Integer>());
                for (int i = 0; i < clusterIndexList.get(tag).size(); i++)
                {
                    if (clusterIndexList.get(tag).get(i) == chead)
                    {
                        clusterHeadList.get(tag).get(chead).add(i);
                    }
                }
            }
            //Log.d("LocDB", clusterHeadList.get(tag).keySet().toString());
            //List<Integer> keys = new ArrayList<Integer>(LocDB.clusterHeadList.get(tag).keySet());
            //keys = Utilities.BubbleSort(keys);
            //Log.d("LocDB", keys.toString());
        }
        else
        {
            missingFiles.add(file);
        }
        //System.out.println("clusterIndexList Loaded");
    }
    
    /// <summary>
    /// Load the psi matrix from file. Each line is a row; each column entry is separted by a space
    /// </summary>
    /// <param name="psifilename"></param>
    /// <returns></returns>
    private static void LoadClusterAvgVarFile(Orientation tag, int rownum, int colnum, boolean isAvg) throws IOException
    {
        String filename;
        Matrix tempMatrix = new Matrix(rownum, colnum);

        if (isAvg)
        {
            filename = clusterAvgFile.get(tag);
        }
        else
        {
            filename = clusterVarFile.get(tag);
        }
        
        if (Utilities.IsValidFilePath(filename))
        {
            BufferedReader sr = new BufferedReader(new FileReader(filename));
            String line;
            String[] splitstrings;

            int i = 0;
            int j = 0;

            while ((line = sr.readLine()) != null)
            {
                splitstrings = line.split(" ");
                for (String s : splitstrings)
                {
                    if (s.trim() != "")
                    {
                        tempMatrix.set(i, j, Double.parseDouble(s.trim()));
                        j++;
                    }
                }

                i++;
                j = 0;
            }
            sr.close();

            // IF THERE EXISTS APS TO BE EXCLUDED
            if (numAPs < apMacIDList.size())
            {
                if (isAvg)
                {
                    clusterAvgRSS.put(tag, tempMatrix.getMatrix(Utilities.Listint(validMacIndexList), 0, colnum - 1));
                }
                else
                {
                    clusterVarRSS.put(tag, tempMatrix.getMatrix(Utilities.Listint(validMacIndexList), 0, colnum - 1));
                }                    
            }
            else
            {
                if (isAvg)
                {
                    clusterAvgRSS.put(tag, tempMatrix.copy());
                }
                else
                {
                    clusterVarRSS.put(tag, tempMatrix.copy());
                }
            }

            //if (isAvg)
            //{
            //    UtilitiesFunc.WriteLineInFile(ConfigSettings.LOGFILENAME, string.Format(">>> Load clusterAvgRSS {0} - {1}x{2}", tag, clusterAvgRSS[tag].RowDimension, clusterAvgRSS[tag].ColumnDimension));
            //}
            //else
            //{
            //    UtilitiesFunc.WriteLineInFile(ConfigSettings.LOGFILENAME, string.Format(">>> Load clusterVarRSS {0} - {1}x{2}", tag, clusterVarRSS[tag].RowDimension, clusterVarRSS[tag].ColumnDimension));
            //}
        }
        else
        {
            missingFiles.add(filename);
        }
    }
    
    private static void DefineTurningPtXYBoundary()
    {
        turningXYBoundarySet.clear();

        for (int turningFP : turningFPSet)
        {
            Integer[] minmaxXY = new Integer[4];
            minmaxXY[0] = (int)Math.round(x00List.get(turningFP) - ConfigSettings.TURNING_TOL_M * ConfigSettings.METER_2_PIXEL_X);
            minmaxXY[1] = (int)Math.round(x00List.get(turningFP) + ConfigSettings.TURNING_TOL_M * ConfigSettings.METER_2_PIXEL_X);
            minmaxXY[2] = (int)Math.round(y00List.get(turningFP) - ConfigSettings.TURNING_TOL_M * ConfigSettings.METER_2_PIXEL_Y);
            minmaxXY[3] = (int)Math.round(y00List.get(turningFP) + ConfigSettings.TURNING_TOL_M * ConfigSettings.METER_2_PIXEL_Y);
            turningXYBoundarySet.add(minmaxXY);
        }
    }

}
