package com.IndoorGPS.LocalizerBasicClass;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.IndoorGPS.Utilities;

import android.graphics.Point;

public class MapInfo
{
    private static String landMarkPositionFile = null;
    private static String turningAreaFile = null;
    private static String mapFeatureFile = null;
    private static String testRouteFile = null;

    private static String building = null;
    private static String floor = null;

    public static HashMap<String, Point[]> DoorsList = new HashMap<String, Point[]>();
    public static HashMap<String, Point> DescripList = new HashMap<String, Point>();

    public static List<Point> LandMarkCoordList = new ArrayList<Point>();
    public static List<Point> TurningLMCoordList = new ArrayList<Point>();
    public static HashMap<String, Integer> Destinations = new HashMap<String, Integer>();
    public static HashMap<String, Integer> StartingPoints = new HashMap<String, Integer>();
    public static HashMap<Integer, List<Integer>> LandMarkConnectionList = new HashMap<Integer, List<Integer>>();
    public static HashMap<String, String> TestRouteList = new HashMap<String, String>();
    public static HashMap<Point, HashMap<Integer, TurnMapArea>> TurningLMAreaList = new HashMap<Point, HashMap<Integer, TurnMapArea>>();

    public static String missingFiles = null;

    public static int LoadMapInfoFiles(String mybuilding, String myfloor) throws Exception
    {
        //if (building != mybuilding || floor != myfloor)
        //{
            building = mybuilding;
            floor = myfloor;

            landMarkPositionFile = String.format("%s/%s%s_LMPosition.txt", ConfigSettings.NAV_SUBDIR, building, floor);
            turningAreaFile = String.format("%s/%s%s_TurnArea.txt", ConfigSettings.NAV_SUBDIR, building, floor);
            mapFeatureFile = String.format("%s/%s%s_MapFeatures.txt", ConfigSettings.NAV_SUBDIR, building, floor);
            testRouteFile = String.format("%s/%s%s_TestRoute.txt", ConfigSettings.NAV_SUBDIR, building, floor); 
            
            if (Utilities.IsValidFilePath(landMarkPositionFile) == true)
            {
                LoadLandmarkListFromFile();
            }
            else
            {
                missingFiles = landMarkPositionFile;
                return -1;
            }

            if (Utilities.IsValidFilePath(testRouteFile))
            {
                LoadTestRouteFromFile();
            }
            else
            {
                //missingFiles = testRouteFile;
                //return -1;
            }

            if (Utilities.IsValidFilePath(turningAreaFile))
            {
                LoadTurnAreaFromFile();
            }
            else
            {
                missingFiles = turningAreaFile;
                return -1;
            }

            if (Utilities.IsValidFilePath(mapFeatureFile))
            {
                LoadMapFeaturesFromFile();
            }
            else
            {
                missingFiles = mapFeatureFile;
                return -1;
            }
            return 1;
        //}
        //return 0;
    }
    
    
    /****************************************************************************************************************
     * Private Functions
     * @throws IOException 
     * @throws NumberFormatException 
     * *************************************************************************************************************/

    //landmarkpostionfile: xxx,yyy,_NODE_/_TURN_/RM_XXXX
    private static void LoadLandmarkListFromFile() throws NumberFormatException, IOException
    {
        LandMarkCoordList.clear();
        Destinations.clear();
        TurningLMCoordList.clear();
        StartingPoints.clear();

        //DestinationVoiceFile.Clear();

        String line;
        String[] splitstrings,ccindexlist;
        int x, y, index;

        String descrip;
        BufferedReader sr = new BufferedReader(new FileReader(landMarkPositionFile));
       
        while ((line = sr.readLine()) != null)
        {
            splitstrings = line.split(",");
            x = Integer.parseInt(splitstrings[0]);
            y = Integer.parseInt(splitstrings[1]);
            descrip = splitstrings[2];

            LandMarkCoordList.add(new Point(x, y));

            // descrip are the destination name, unless with the tag _NODE_
            if (descrip.equalsIgnoreCase("_TURN_"))
            {
                TurningLMCoordList.add(new Point(x, y));
            }
            else if (descrip.startsWith("_S_"))
            {
                String startpt = descrip.substring(3);
                StartingPoints.put(startpt, LandMarkCoordList.size() - 1);
            }
            else if (!descrip.equalsIgnoreCase("_NODE_") )
            {
                Destinations.put(descrip, LandMarkCoordList.size() - 1);
            }
            
            if (splitstrings.length == 4)
            {
                index = LandMarkCoordList.size() - 1;
                LandMarkConnectionList.put(index, new ArrayList<Integer>());

                ccindexlist = splitstrings[3].split("-");

                for (String ssindex : ccindexlist)
                {
                    LandMarkConnectionList.get(index).add(Integer.parseInt(ssindex));
                }
            }
        }
        sr.close();
    }
	
    private static void LoadTestRouteFromFile() throws IOException
    {
        TestRouteList.clear();

        String line;
        String[] splintedstrings;

        BufferedReader sr = new BufferedReader(new FileReader(testRouteFile));
        
        while ((line = sr.readLine()) != null)
        {
            splintedstrings = line.split(":");

            if (StartingPoints.containsKey(splintedstrings[1]) && Destinations.containsKey(splintedstrings[2]))
            {
                TestRouteList.put(splintedstrings[0], splintedstrings[1] + ":" + splintedstrings[2]);
            }
        }
        sr.close();
    }
    
    private static void LoadTurnAreaFromFile() throws Exception, IOException
    {
        TurningLMAreaList.clear();

        String line;
        String[] ss1;
        int x, y;

        Point turnLM = new Point();

        int dir;
        TurnMapArea tma;

        boolean isTurnLMDefinedInLM = false;

        BufferedReader sr = new BufferedReader(new FileReader(turningAreaFile));

        while ((line = sr.readLine()) != null)
        {
            if (!line.contains(":"))
            {
                ss1 = line.split(",");
                x = Integer.parseInt(ss1[0]);
                y = Integer.parseInt(ss1[1]);
                turnLM = new Point(x, y);

                if (TurningLMCoordList.contains(turnLM))
                {
                    isTurnLMDefinedInLM = true;

                    TurningLMAreaList.put(turnLM, new HashMap<Integer, TurnMapArea>());
                }
                else
                {
                    isTurnLMDefinedInLM = false;
                }
            }
            else
            {
                if (!isTurnLMDefinedInLM)
                {
                    continue;
                }

                ss1 = line.split(":");

                dir = Integer.parseInt(ss1[0]);
                tma = new TurnMapArea(ss1[1].split(","), ss1[2].split(","));

                TurningLMAreaList.get(turnLM).put(dir, tma);
            }
        }            
    }
    
    private static void LoadMapFeaturesFromFile() throws IOException
    {
        DoorsList.clear();
        DescripList.clear();

        String line, feature, name;
        String[] splitstrings, splittitle;
        List<Point> ptsList = new ArrayList<Point>();

        BufferedReader sr = new BufferedReader(new FileReader(mapFeatureFile));

        while ((line = sr.readLine()) != null)
        {
            splitstrings = line.split(",");

            ptsList.clear();
            for (int i = 1; i < splitstrings.length - 1; i += 2)
            {
                int x = Integer.parseInt(splitstrings[i]);
                int y = Integer.parseInt(splitstrings[i + 1]);
                ptsList.add(new Point(x, y));
            }

            if (splitstrings[0].contains(":"))
            {
                splittitle = splitstrings[0].split(":");
                feature = splittitle[0];
                name = splittitle[1];
            }
            else
            {
                feature = splitstrings[0];
                name = "";
            }
            if (feature.equalsIgnoreCase("DOOR"))
            {                   
            	DoorsList.put(name, (Point[]) ptsList.toArray());
            }
            else if (feature.equalsIgnoreCase("DESCRIP"))
            {
                DescripList.put(name, ptsList.get(0));
            }
        }
        sr.close();

    }



}