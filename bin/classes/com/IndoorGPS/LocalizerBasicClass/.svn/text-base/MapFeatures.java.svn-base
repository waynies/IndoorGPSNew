package com.IndoorGPS.LocalizerBasicClass;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import com.IndoorGPS.Utilities;

import android.graphics.Point;

public class MapFeatures {

		private static String mapFeatureFile = null;
        //private static string mapFeatureFileTemp = null;
        private static String landMarkPositionFile = null;

        private static String building = null;
        private static String floor = null;

        public static List<List<Point>> WallsList = new ArrayList<List<Point>>();
        public static List<List<Point>> BlocksList = new ArrayList<List<Point>>();
        public static HashMap<String, List<Point>> DoorsList = new HashMap<String, List<Point>> ();
        public static HashMap<String, List<Point>> AreasList = new HashMap<String, List<Point>>();
        public static List<Point> LMsList = new ArrayList<Point>();
        public static HashMap<String, Point> DestsList = new HashMap<String, Point>();
        public static HashMap<String, Point> APsList = new HashMap<String, Point>();


        public static void MFSetting(String mybuilding, String myfloor) throws NumberFormatException, IOException
        {
            if (! building.equalsIgnoreCase(mybuilding) || ! floor.equalsIgnoreCase(myfloor))
            {
                building = mybuilding;
                floor = myfloor;

                mapFeatureFile = ConfigSettings.NAV_SUBDIR + "/" + building + floor + "_MapFeatures.txt";
                //mapFeatureFileTemp = ConfigSettings.MAP_SUBDIR + "\\" + building + floor + "_MapFeaturesTemp.txt";
                landMarkPositionFile = ConfigSettings.NAV_SUBDIR + "/" + building + floor + "_LMPosition.txt";

                CreateOrLoadFiles();

            }
            //return 0;
        }

        public static void AddWall(List<Point> coordlist)
        {
            WallsList.add(new ArrayList<Point>(coordlist));

           //UtilitiesFunc.WriteLineInFile(mapFeatureFile, "WALL," + coordlist[0].X.ToString() + "," + coordlist[0].Y.ToString() + ","
           //                                                       + coordlist[1].X.ToString() + "," + coordlist[1].Y.ToString());
        }

        public static void AddBlock(List<Point> coordlist)
        {
            BlocksList.add(new ArrayList<Point>(coordlist));

            //UtilitiesFunc.WriteLineInFile(mapFeatureFile, "BLOCK," + coordlist[0].X.ToString() + "," + coordlist[0].Y.ToString() + ","
            //                                                      + coordlist[1].X.ToString() + "," + coordlist[1].Y.ToString());
        }

        public static void AddDoor(String doorname, List<Point> coordlist)
        {
            DoorsList.put(doorname, new ArrayList<Point>(coordlist));

            //UtilitiesFunc.WriteLineInFile(mapFeatureFile, "DOOR:" + doorname + ","
            //                                                      + coordlist[0].X.ToString() + "," + coordlist[0].Y.ToString() + ","
            //                                                      + coordlist[1].X.ToString() + "," + coordlist[1].Y.ToString());
        }

        public static void AddArea(String areaname, List<Point> coordlist)
        {
            DoorsList.put(areaname, new ArrayList<Point>(coordlist));

            String coordst = "";
            for (int i = 0; i < coordlist.size(); i++)
            {
                coordst += "," + coordlist.get(i).x + "," + coordlist.get(i).y;
            }
            //UtilitiesFunc.WriteLineInFile(mapFeatureFile, "AREA:" + areaname + coordst);
        }

        public static void AddLM(Point coord)
        {
            LMsList.add(coord);

            //UtilitiesFunc.WriteLineInFile(mapFeatureFile, "LM," + coord.X.ToString() + "," + coord.Y.ToString());
        }

        public static void AddDest(String name, Point coord)
        {
            DestsList.put(name, coord);

            //UtilitiesFunc.WriteLineInFile(mapFeatureFile, "DEST:" + name + "," + coord.X.ToString() + "," + coord.Y.ToString());
        }

        public static void AddAP(String name, Point coord)
        {
            APsList.put(name, coord);

            //UtilitiesFunc.WriteLineInFile(mapFeatureFile, "AP:" + name + "," + coord.X.ToString() + "," + coord.Y.ToString());
        }


        public static void RewriteMapfFiles() throws IOException
        {
            BufferedWriter sw = new BufferedWriter(new FileWriter(mapFeatureFile));
            BufferedWriter sw1 = new BufferedWriter(new FileWriter(landMarkPositionFile));

            for (List<Point> coordlist : WallsList)
            {
                sw.write("WALL");
                for (Point coord : coordlist)
                {
                    sw.write("," + coord.x + "," + coord.y);
                }
                sw.newLine();
            }

            for (List<Point> coordlist : BlocksList)
            {
                sw.write("BLOCK");
                for (Point coord : coordlist)
                {
                    sw.write("," + coord.x + "," + coord.y);
                }
                sw.newLine();
            }
            
            Iterator<String> keyIt = DoorsList.keySet().iterator();
    		while (keyIt.hasNext()) {
    			String kvpKey = keyIt.next();
    			List<Point> kvpValue = DoorsList.get(kvpKey);
           
                sw.write("DOOR:" + kvpKey);
                for (Point coord : kvpValue)
                {
                    sw.write("," + coord.x + "," + coord.y);
                }
                sw.newLine();
            }

    		Iterator<String> keyIt2 = AreasList.keySet().iterator();
    		while (keyIt2.hasNext()) {
    			String kvpKey = keyIt2.next();
    			List<Point> kvpValue = AreasList.get(kvpKey);
  
                sw.write("AREA:" + kvpKey);
                for (Point coord :  kvpValue)
                {
                    sw.write("," + coord.x + "," + coord.y);
                }
                sw.newLine();
            }

            for (int i = 0; i < LMsList.size(); i++)
            {
                sw.write("LM:" + i);
                sw.write("," + LMsList.get(i).x + "," + LMsList.get(i).y);
                sw.newLine();

                sw1.write(LMsList.get(i).x + "," + LMsList.get(i).y + ",_NODE_");
                sw1.newLine();

            }

            Iterator<String> keyIt3 = DestsList.keySet().iterator();
    		while (keyIt3.hasNext()) {
    			String kvpKey = keyIt3.next();
    			Point kvpValue = DestsList.get(kvpKey);
  
    			sw.write("DEST:" + kvpKey);
                sw.write("," + kvpValue.x + "," + kvpValue.y);
                sw.newLine();
                
                sw1.write(kvpValue.x + "," + kvpValue.y + "," + kvpKey);
                sw1.newLine();
            }

    		Iterator<String> keyIt4 = APsList.keySet().iterator();
    		while (keyIt4.hasNext()) {
    			String kvpKey = keyIt4.next();
    			Point kvpValue = APsList.get(kvpKey);
            
                sw.write("AP:" + kvpKey);
                sw.write("," + kvpValue.x + "," + kvpValue.y);
                sw.newLine();

                sw1.write(kvpValue.x + "," + kvpValue.y + "," + kvpKey);
                sw1.newLine();
            }
            sw.close();
            sw1.close();
        }

        /****************************************************************************************************************
        * Private Functions
         * @throws IOException 
         * @throws NumberFormatException 
        * *************************************************************************************************************/

        private static void CreateOrLoadFiles() throws NumberFormatException, IOException
        {
            if (Utilities.IsValidFilePath(mapFeatureFile))
            {
                LoadMapFeaturesFromFile();
                //File.Copy(mapFeatureFile, mapFeatureFileTemp, true);
            }
            else
            {
                //File.Create(mapFeatureFile).Close();
                //File.Create(mapFeatureFileTemp).Close();
            }

            if (Utilities.IsValidFilePath(landMarkPositionFile))
            {
                //File.Copy(landMarkPositionFile, landMarkPositionFile + "_" + DateTime.Now.ToString("ddMMHHmm"), true); 
            }
            else
            {
                //File.Create(landMarkPositionFile).Close();
            }
        }

        // load map feature from file
        // _Feature_:(_name_),x1,y1,x2,y2,...
        private static void LoadMapFeaturesFromFile() throws NumberFormatException, IOException
        {
            WallsList.clear();
            BlocksList.clear();
            DoorsList.clear();
            AreasList.clear();
            LMsList.clear();
            DestsList.clear();
            APsList.clear();

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
                
                if(feature.equalsIgnoreCase("WALL"))
                {
                    WallsList.add(new ArrayList<Point>(ptsList));
                }
                else if(feature.equalsIgnoreCase("BLOCK"))
                {
                    BlocksList.add(new ArrayList<Point>(ptsList));
                }
                else if(feature.equalsIgnoreCase("DOOR"))
                {
                    DoorsList.put(name, new ArrayList<Point>(ptsList));
                }
                else if(feature.equalsIgnoreCase("AREA"))
                {
                    AreasList.put(name, new ArrayList<Point>(ptsList));
                }
                else if(feature.equalsIgnoreCase("LM"))
                {
                    LMsList.add(ptsList.get(0));
                }
                else if(feature.equalsIgnoreCase("DEST"))
                {
                    DestsList.put(name, ptsList.get(0));
                }
                else if(feature.equalsIgnoreCase("AP"))
                {
                    APsList.put(name, ptsList.get(0));
                }
            }
            sr.close();

        }
}