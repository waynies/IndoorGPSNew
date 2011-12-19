package com.IndoorGPS.Navigation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import android.graphics.Point;
import android.speech.tts.TextToSpeech;

import com.IndoorGPS.MapActivity;
import com.IndoorGPS.LocalizerBasicClass.ConfigSettings;
import com.IndoorGPS.LocalizerBasicClass.GeometryFunc;
import com.IndoorGPS.LocalizerBasicClass.LineSegment;
import com.IndoorGPS.LocalizerBasicClass.LocResult;
import com.IndoorGPS.LocalizerBasicClass.MapInfo;
import com.IndoorGPS.Navigation.VoiceOutAudio.VoiceInstr;

public class NavigAnalysis {
	
    private static List<VoiceInstr> VoiceInstBuffer = new ArrayList<VoiceInstr>();
    private static List<VoiceInstr> VoiceInfoBuffer = new ArrayList<VoiceInstr>();
    private static List<String> VoiceAdditionalBuffer = new ArrayList<String>();

    private static List<LineSegment> routedPathSegments = new ArrayList<LineSegment>();
    private static List<HashMap<Double, String>> routedPathSegmentsMapFeature = new ArrayList<HashMap<Double, String>>();
    private static List<Integer> routedPathTurningPtDBAngle = new ArrayList<Integer>();
   
    private static int currentLSIndex;
    private static double prevLSu, currentLSu;
    private static int currentPathDir;

    private static boolean atTurningArea;
    private static int turnCode;

    private static boolean WarningAdvanceTurn;
    private static boolean WarningTurn;
    private static HashMap<Double, Integer> WarningGiven = new HashMap<Double, Integer>();

    private static int offRouteCount;

    private static boolean reachDestination = false;
    private static int destinationTurnCode;

    private static int noInstrCount = 0;
    private static int maxNoInstrCount = 2;

    private static int smoothedWindowMinBoundIndex = 0;
    private static int dirIndex = 0;

    private static int consecutiveBackwardCount = 0;

    public static List<Point> NavigRoutedPath = new ArrayList<Point>();

    public static String InstrCode = "";

    private static VoiceInstr destinationSide = VoiceInstr.InYourFront;

    public static void SetVoiceInterval(int voiceCount)
    {
        maxNoInstrCount = voiceCount;
    }
    
    public static void AnalyzeRoutedPath(List<Point> routedPath)
    {
    	routedPathSegments.clear();
        NavigRoutedPath.clear();
        
        int prevSIndex = 0;
        NavigRoutedPath.add(routedPath.get(prevSIndex));
        for (int i =1; i < routedPath.size(); i++)
        {
            if (MapInfo.TurningLMCoordList.contains(routedPath.get(i)))
            {
                if (routedPathSegments.size() > 0)
                {
                    int currentpathangle = GeometryFunc.ComputeAngle(routedPath.get(prevSIndex), routedPath.get(i), true);

                    int angledirr = Math.abs(currentpathangle - routedPathSegments.get(routedPathSegments.size()-1).DirAngle);
                    if (angledirr > 180)
                    {
                        angledirr = 360 - angledirr;
                    }

                    if (currentpathangle >= 0 && angledirr > 45)
                    {
                        routedPathSegments.add(new LineSegment(routedPath.get(prevSIndex), routedPath.get(i)));
                        prevSIndex = i;

                        NavigRoutedPath.add(routedPath.get(i));
                    }
                    else
                    {
                        LineSegment newls = new LineSegment(routedPathSegments.get(routedPathSegments.size()-1).PtS, routedPath.get(i));
                        routedPathSegments.remove(routedPathSegments.size() - 1);
                        routedPathSegments.add(newls);
                        prevSIndex = i;

                        NavigRoutedPath.add(routedPath.get(i));
                    }
                }
                else
                {
                    if (GeometryFunc.ComputeAngle(routedPath.get(prevSIndex), routedPath.get(i), true) >= 0)
                    {
                        routedPathSegments.add(new LineSegment(routedPath.get(prevSIndex), routedPath.get(i)));
                        prevSIndex = i;

                        NavigRoutedPath.add(routedPath.get(i));
                    }
                }
            }
        }
        if (prevSIndex < routedPath.size() - 2)
        {
            int secondlast = routedPath.size() - 2;
            if (prevSIndex == 0)
            {
                routedPathSegments.add(new LineSegment(routedPath.get(prevSIndex), routedPath.get(secondlast)));

                NavigRoutedPath.add(routedPath.get(secondlast));
                NavigRoutedPath.add(routedPath.get(routedPath.size()-1));
            }
            else
            {
                int currentpathangle = GeometryFunc.ComputeAngle(routedPath.get(prevSIndex), routedPath.get(secondlast), true);

                int angledirr = Math.abs(currentpathangle - routedPathSegments.get(routedPathSegments.size()-1).DirAngle);
                if (angledirr > 180)
                {
                    angledirr = 360 - angledirr;
                }

                if (currentpathangle >= 0 && angledirr > 45)
                {
                    routedPathSegments.add(new LineSegment(routedPath.get(prevSIndex), routedPath.get(secondlast)));

                    NavigRoutedPath.add(routedPath.get(secondlast));
                    NavigRoutedPath.add(routedPath.get(routedPath.size()-1));
                }
                else
                {
                    LineSegment newls = new LineSegment(routedPathSegments.get(routedPathSegments.size()-1).PtS, routedPath.get(secondlast));
                    routedPathSegments.remove(routedPathSegments.size() - 1);
                    routedPathSegments.add(newls);

                    NavigRoutedPath.add(routedPath.get(secondlast));
                    NavigRoutedPath.add(routedPath.get(routedPath.size()-1));
                }
            }
        }
        else
        {
            NavigRoutedPath.add(routedPath.get(routedPath.size()-1));
        }

        int destination2lastpathangle = GeometryFunc.ComputeAngle(routedPathSegments.get(routedPathSegments.size()-1).PtE, routedPath.get(routedPath.size()-1), false);
        turnCode = FindTurnSide(routedPathSegments.get(routedPathSegments.size()-1).DirAngle, destination2lastpathangle);

        if (turnCode == 1)
        {
            destinationSide = VoiceInstr.OnYourRight;
        }
        else if (turnCode == 2)
        {
            destinationSide = VoiceInstr.OnYourLeft;
        }
        else
        {
            destinationSide = VoiceInstr.InYourFront;
        }


        routedPathSegmentsMapFeature.clear();
        routedPathTurningPtDBAngle.clear();
        for (int i = 0; i < routedPathSegments.size(); i++)
        {
            // === Identify the map feature associate with the line segments (now - check if go through any doors; also any descriptions)

            HashMap<Double, String> mapfeature = new HashMap<Double, String>();
            Iterator<String> keyIt =  MapInfo.DoorsList.keySet().iterator();
            while (keyIt.hasNext())
            {
            	String doorName = keyIt.next();
    			Point[] doorValue = MapInfo.DoorsList.get(doorName);
            	double pathU = 0.0;
            	double doorU = 0.0; 
            	
            	int intersectResult = routedPathSegments.get(i).IsIntersectWithLineSegment(doorValue[0], doorValue[1], pathU, doorU);
            	Double[] out_u = routedPathSegments.get(i).Out_U1_U2(doorValue[0], doorValue[1], pathU, doorU);
            	pathU = out_u[0];
            	doorU = out_u[1];
            	if (intersectResult == 1)
                {
                    mapfeature.put(pathU, "DOOR_" + doorName);
                }
            	
            }

            routedPathSegmentsMapFeature.add(mapfeature);

            Iterator<String> keyDescrip =  MapInfo.DescripList.keySet().iterator();
            while (keyDescrip.hasNext())
            {
                String descripName = keyDescrip.next();
                Point descripValue = MapInfo.DescripList.get(descripName);
                if (descripName == PathRouting.DestinationPlace)
                {
                    continue;
                }

                double descripU = 0.0;
                double distance = routedPathSegments.get(i).FindShortestDist2Point(descripValue, descripU);
                descripU = routedPathSegments.get(i).Out_U(descripValue, descripU);
                if (distance < ConfigSettings.MAX_DESCRIPTION_TO_PATH_M && -0.1 <= descripU && descripU <= 1.1)
                {
                    mapfeature.put(descripU, "DESCRIP_" + descripName);

                    int angle = GeometryFunc.ComputeAngle(routedPathSegments.get(i).GetPointFromEqn(descripU), descripValue, false);
                    turnCode = FindTurnSide(routedPathSegments.get(i).DirAngle, angle);
                    if (turnCode == 1)
                    {
                        mapfeature.put(descripU, mapfeature.get(descripU) + "_R");
                       
                    }
                    else
                    {
                    	mapfeature.put(descripU, mapfeature.get(descripU) + "_L");
                    }

                    VoiceOutAudio.LocateAdditionalAudioFile(mapfeature.get(descripU));
                }
            }

            // === Get the turning point and the turning area definition depend on the direction where the path approach the turning point
            if (i < routedPathSegments.size() - 1)
            {
                Point turnPt = routedPathSegments.get(i).PtE;
                int pathdir = routedPathSegments.get(i).DirAngle;

                int absdiff, absdiffC;
                boolean firstangle = true;
                int chosenTurningAngle = 0;
                for (int defineddir : MapInfo.TurningLMAreaList.get(turnPt).keySet())
                {
                    if (firstangle)
                    {
                        chosenTurningAngle = defineddir;
                        firstangle = false;
                    }
                    else
                    {
                        absdiffC = Math.abs(pathdir - chosenTurningAngle);
                        if (absdiffC > 180)
                        {
                            absdiffC = 360 - absdiffC;
                        }

                        absdiff = Math.abs(pathdir - defineddir);
                        if (absdiff > 180)
                        {
                            absdiff = 360 - absdiff;
                        }

                        if (absdiff < absdiffC)
                        {
                            chosenTurningAngle = defineddir;
                        }
                    }
                }
                routedPathTurningPtDBAngle.add(chosenTurningAngle);
            }
        }

/*        // === Log down the path info.
        UtilitiesFunc.WriteLineInFile(ConfigSettings.NAVIGATION_FILENAME, "---Line Segment:");
        for (int i = 0; i < routedPathSegments.Count; i++)
        {
            UtilitiesFunc.WriteLineInFile(ConfigSettings.NAVIGATION_FILENAME, " #" + i.ToString() + " - " + routedPathSegments[i].Print());
            foreach (KeyValuePair<double, string> mapf in routedPathSegmentsMapFeature[i])
            {
                UtilitiesFunc.WriteLineInFile(ConfigSettings.NAVIGATION_FILENAME, string.Format(" > {0} - {1}", mapf.Key, mapf.Value));
            }

            if (i < routedPathSegments.Count - 1)
            {
                UtilitiesFunc.WriteLineInFile(ConfigSettings.NAVIGATION_FILENAME, string.Format(" > At turning pt (the end pt), use DB angle={0}", routedPathTurningPtDBAngle[i]));
            }
        }
        UtilitiesFunc.WriteLineInFile(ConfigSettings.NAVIGATION_FILENAME, string.Format(" Destination:({0},{1}), Side:{2}", routedPath.Last().X, routedPath.Last().Y, destinationSide));
        UtilitiesFunc.WriteLineInFile(ConfigSettings.NAVIGATION_FILENAME, "----------------:");
*/
        // === Obtain the first voice instruction when navigation start
        VoiceInstBuffer.clear();
        VoiceInfoBuffer.clear();
        VoiceAdditionalBuffer.clear();

        VoiceInfoBuffer.add(VoiceInstr.Please);
        VoiceInfoBuffer.add(VoiceInstr.WalkStraightFor);
        VoiceInfoBuffer.add(GetAnnouncementDistance(routedPathSegments.get(0).Distance));

        if (routedPathSegments.size() > 1)
        {
            VoiceInstr turnInstr = GetAnnouncementTurn(routedPathSegments.get(0).DirAngle, routedPathSegments.get(1).DirAngle);
            if (turnInstr != null)
            {
                VoiceInfoBuffer.add(VoiceInstr.And);
                VoiceInfoBuffer.add((VoiceInstr)turnInstr);
            }                
        }

        // === Initialization
        currentLSIndex = 0;
        prevLSu = 0;
        currentPathDir = routedPathSegments.get(0).DirAngle;

        atTurningArea = false;
        WarningAdvanceTurn = false;
        WarningTurn = false;
        WarningGiven.clear();

        offRouteCount = 0;

        reachDestination = false;
        destinationTurnCode = -1;

        consecutiveBackwardCount = 0;

        smoothedWindowMinBoundIndex = LocResult.EstimatedPositions.size() - 1;

    }
    
    /// <summary>
    /// Obtain the latest ConfigSettings.SMOOTH_TRACE_WINDOW_SIZE updates to smooth out the current location using moving average.
    /// </summary>
    /// <returns></returns>
    public static void SmoothUpdates()
    {
        Point smoothedPt = new Point();

        if (ConfigSettings.USE_SMOOTH_AVERAGE == 1)
        {
            double x = 0;
            double y = 0;

            int windowsize = ConfigSettings.SMOOTH_TRACE_WINDOW_SIZE;
            int numestimates = LocResult.EstimatedPositions.size() - smoothedWindowMinBoundIndex;

            if (numestimates < windowsize)
            {
                windowsize = numestimates;
            }

            double totalwt = windowsize * (windowsize + 1) / 2;
            for (int wt = windowsize; wt > 0; wt--)
            {
                int i = LocResult.EstimatedPositions.size() - windowsize - 1 + wt;
                x += wt * (LocResult.EstimatedPositions.get(i).x);
                y += wt * (LocResult.EstimatedPositions.get(i).y);
            }               

            smoothedPt = new Point((int)Math.round(x / totalwt), (int)Math.round(y / totalwt));
            //UtilitiesFunc.WriteLineInFile(ConfigSettings.NAVIGATION_FILENAME, string.Format(" > Smoothed from [{0},{1}] w/ linear wt", LocResult.EstimatedPositions.Count - windowsize, LocResult.EstimatedPositions.Count - 1));

        }
        else if (ConfigSettings.USE_SMOOTH_AVERAGE == 2)
        {
            double x = 0;
            double y = 0;

            int windowsize = ConfigSettings.SMOOTH_TRACE_WINDOW_SIZE;
            int numestimates = LocResult.EstimatedPositions.size() - smoothedWindowMinBoundIndex;

            if (numestimates < windowsize)
            {
                windowsize = numestimates;
            }                            

            //-- normal average
            for (int i = numestimates - windowsize; i < numestimates; i++)
            {
                x += (LocResult.EstimatedPositions.get(i).x);
                y += (LocResult.EstimatedPositions.get(i).y);
            }
            double totalwt = windowsize;

            smoothedPt = new Point((int)Math.round(x / totalwt), (int)Math.round(y / totalwt));
            //UtilitiesFunc.WriteLineInFile(ConfigSettings.NAVIGATION_FILENAME, string.Format(" > Smoothed from [{0},{1}] avg", LocResult.EstimatedPositions.Count - windowsize, LocResult.EstimatedPositions.Count - 1));

        }
        else if (ConfigSettings.USE_SMOOTH_AVERAGE == 0)
        {
            smoothedPt = LocResult.EstimatedPositions.get(LocResult.EstimatedPositions.size()-1);
            //UtilitiesFunc.WriteLineInFile(ConfigSettings.NAVIGATION_FILENAME, string.Format(" > Smoothed obtained directly from kalman output"));
        }

        if (LocResult.SmoothedNavigPositions.size() < LocResult.EstimatedPositions.size())
        {
            LocResult.SmoothedNavigPositions.add(smoothedPt);
        }
        else
        {
            LocResult.SmoothedNavigPositions.set(LocResult.EstimatedPositions.size() - 1, smoothedPt);
        }
        //UtilitiesFunc.WriteLineInFile(ConfigSettings.NAVIGATION_FILENAME, string.Format("#{0}: Smoothed:({1},{2})", LocResult.SmoothedNavigPositions.Count, smoothedPt.X, smoothedPt.Y));

        int angle;
        int numsmoothpt = LocResult.SmoothedNavigPositions.size();
        if (numsmoothpt == 2)
        {
            angle = GeometryFunc.ComputeAngle(LocResult.SmoothedNavigPositions.get(0), LocResult.SmoothedNavigPositions.get(1), true);
            LocResult.InterpretedUserDirections.add(angle);
            dirIndex = 0;
        }
        else if (numsmoothpt > 2)
        {
            angle = GeometryFunc.ComputeAngle(LocResult.SmoothedNavigPositions.get(dirIndex), LocResult.SmoothedNavigPositions.get(LocResult.SmoothedNavigPositions.size()-1), true);
            if (angle >= 0)
            {
                LocResult.InterpretedUserDirections.add(angle);
                dirIndex = numsmoothpt - 1;
            }
            else
            {
                LocResult.InterpretedUserDirections.add(LocResult.InterpretedUserDirections.get(LocResult.InterpretedUserDirections.size()-1));
            }
        }
        if (numsmoothpt >= 2)
        {
            angle = GeometryFunc.ComputeAngle(LocResult.SmoothedNavigPositions.get(LocResult.SmoothedNavigPositions.size() - 2), LocResult.SmoothedNavigPositions.get(LocResult.SmoothedNavigPositions.size()-1), true);

            //UtilitiesFunc.WriteLineInFile(ConfigSettings.NAVIGATION_FILENAME, string.Format(" angle={0}/{1}", angle, LocResult.InterpretedUserDirections.Last()));

            if (angle >= 0)
            {
                int anglediff = Math.abs(LocResult.InterpretedUserDirections.get(LocResult.InterpretedUserDirections.size()-1) - routedPathSegments.get(currentLSIndex).DirAngle);
                if (anglediff > 180)
                {
                    anglediff = 360 - anglediff;
                }

                if (anglediff > 90)
                {
                    consecutiveBackwardCount++;
                }
                else
                {
                    consecutiveBackwardCount = 0;
                }
            }

            if (consecutiveBackwardCount >= ConfigSettings.OFFPATH_DIR_COUNT)
            {
                //UtilitiesFunc.WriteLineInFile(ConfigSettings.NAVIGATION_FILENAME, string.Format(">> Walking backward for consecutively {0} times; WRONG Direction", ConfigSettings.OFFPATH_DIR_COUNT));
                VoiceInstBuffer.add(VoiceInstr.TurnBackward);
                InstrCode += "B ";
            }
        }
    }
    
    
    /// <summary>
    /// Find if smoothed pt follows route, needs turn/advance turn and through door warning.
    /// <returns>10 - reach destination
    ///           0 - no voice
    ///           5 - have voice
    ///           -10 - needreroute
    ///           -1 - offpath</returns>
    /// </summary>
    public static int AnalyzeCurrentUpdates()
    {
        //VoiceInstBuffer.Clear();
        //VoiceInfoBuffer.Clear();
        
        // === 1 === Get the smoothed position

        Point smoothedPt = LocResult.SmoothedNavigPositions.get(LocResult.SmoothedNavigPositions.size()-1);            


        // === 2 === Check if update is close to destination

        reachDestination = CheckIfReachDestination(smoothedPt);
        if (reachDestination)
        {
            InstrCode += "RD ";
            return 10;
        }

        // === 3 === Check if update snaps to the routed line segment
        double uu = 0.0;
        double distance;
        distance = routedPathSegments.get(currentLSIndex).FindShortestDist2Point(smoothedPt, uu);
        //UtilitiesFunc.WriteLineInFile(ConfigSettings.NAVIGATION_FILENAME, string.Format("-LS:{0} D:{1} u:{2}", currentLSIndex, distance, uu));
        uu = routedPathSegments.get(currentLSIndex).Out_U(smoothedPt, uu);
       
        boolean isClose2Path = false;
        if (-0.1 <= uu && uu <= 1.1)
        {
            isClose2Path = true;
        }
        else
        {
            if (currentLSIndex == 0)
            {
                if (uu < 0 && GeometryFunc.EuclideanDistanceInMeter(smoothedPt, routedPathSegments.get(currentLSIndex).PtS) < ConfigSettings.MAX_CLOSET_DISTANCE_TO_PATH_M)
                {
                    isClose2Path = true;
                }
            }
            else
            {
                isClose2Path = false;
            }
        }
        
        
        if (distance < ConfigSettings.MAX_CLOSET_DISTANCE_TO_PATH_M && isClose2Path)
        {
            // updates is one this line segment currentLSIndex
            offRouteCount = 0;
            currentLSu = uu;
            //UtilitiesFunc.WriteLineInFile(ConfigSettings.NAVIGATION_FILENAME, string.Format("> Stay at current line segment #{0}, uu={1}, distance={2}", currentLSIndex, currentLSu, distance));

            if (atTurningArea || (uu > 0.7 && currentLSIndex + 1 < routedPathSegments.size()))
            {
                double distance1;
                double uu1 = 0.0;
                distance1 = routedPathSegments.get(currentLSIndex + 1).FindShortestDist2Point(smoothedPt, uu1);
                uu1 = routedPathSegments.get(currentLSIndex + 1).Out_U(smoothedPt, uu1);
                if (distance1 < ConfigSettings.MAX_CLOSET_DISTANCE_TO_PATH_M && -0.1 <= uu1 && uu1 <= 1.1 &&
                    distance1 < distance)
                {
                    InstrCode += "AP ";

                    VoiceInfoBuffer.add(VoiceInstr.Please);
                    VoiceInfoBuffer.add(VoiceInstr.Stop);
                    VoiceInfoBuffer.add(VoiceInstr.AfterTurnFacing);
                    VoiceInfoBuffer.add(GetAnnouncementFacingDirection(routedPathSegments.get(currentLSIndex + 1).DirAngle));
                    VoiceInfoBuffer.add(VoiceInstr.YourPathInfoIs);
                    VoiceInfoBuffer.add(VoiceInstr.WalkStraightFor);
                    VoiceInfoBuffer.add(GetAnnouncementDistance(smoothedPt, routedPathSegments.get(currentLSIndex + 1).PtE));

                    if (currentLSIndex + 1 < routedPathSegments.size() - 1)
                    {
                        VoiceInstr turnInstr = GetAnnouncementTurn(routedPathSegments.get(currentLSIndex + 1).DirAngle, routedPathSegments.get(currentLSIndex + 2).DirAngle);
                        if (turnInstr != null)
                        {
                            VoiceInfoBuffer.add(VoiceInstr.And);
                            VoiceInfoBuffer.add((VoiceInstr)turnInstr);
                        }
                    }
                    else
                    {
                        VoiceInfoBuffer.add(VoiceInstr.And);
                        VoiceInfoBuffer.add(VoiceInstr.YouWillArriveAtDestination);
                    }
                    VoiceInfoBuffer.add(VoiceInstr.Please);
                    VoiceInfoBuffer.add(VoiceInstr.GoStraight);

                    smoothedWindowMinBoundIndex = LocResult.EstimatedPositions.size() - 1;

                    offRouteCount = 0;
                    currentPathDir = routedPathSegments.get(currentLSIndex + 1).DirAngle;
                    currentLSIndex = currentLSIndex + 1;
                    prevLSu = uu1;
                    currentLSu = uu1;
                    WarningAdvanceTurn = false;
                    WarningTurn = false;
                    WarningGiven.clear();
                    atTurningArea = false;
                    consecutiveBackwardCount = 0;
                    //UtilitiesFunc.WriteLineInFile(ConfigSettings.NAVIGATION_FILENAME, string.Format("> move to next line segment #{0}, uu={1}/{2}, distance={3}/{4} atturn:{5}", currentLSIndex, currentLSu, uu, distance1, distance, atTurningArea));

                }

            }
        }
        else
        {
            int ii = currentLSIndex;
            while (!(distance < ConfigSettings.MAX_CLOSET_DISTANCE_TO_PATH_M && -0.1 <= uu && uu <= 1.1) && ii < routedPathSegments.size() - 1)
            {
                ii++;
                distance = routedPathSegments.get(ii).FindShortestDist2Point(smoothedPt, uu);   
                uu = routedPathSegments.get(ii).Out_U(smoothedPt, uu);  
            }

            if (distance < ConfigSettings.MAX_CLOSET_DISTANCE_TO_PATH_M && -0.1 <= uu && uu <= 1.1)
            {
                if (atTurningArea)
                {
                    InstrCode += "AP ";

                    VoiceInfoBuffer.add(VoiceInstr.Please);
                    VoiceInfoBuffer.add(VoiceInstr.Stop);
                    VoiceInfoBuffer.add(VoiceInstr.AfterTurnFacing);
                    VoiceInfoBuffer.add(GetAnnouncementFacingDirection(routedPathSegments.get(ii).DirAngle));
                    VoiceInfoBuffer.add(VoiceInstr.YourPathInfoIs);
                    VoiceInfoBuffer.add(VoiceInstr.WalkStraightFor);
                    VoiceInfoBuffer.add(GetAnnouncementDistance(smoothedPt, routedPathSegments.get(ii).PtE));

                    if (ii < routedPathSegments.size() - 1)
                    {
                        VoiceInstr turnInstr = GetAnnouncementTurn(routedPathSegments.get(ii).DirAngle, routedPathSegments.get(ii + 1).DirAngle);
                        if (turnInstr != null)
                        {
                            VoiceInfoBuffer.add(VoiceInstr.And);
                            VoiceInfoBuffer.add((VoiceInstr)turnInstr);
                        }
                    }
                    else
                    {
                        VoiceInfoBuffer.add(VoiceInstr.And);
                        VoiceInfoBuffer.add(VoiceInstr.YouWillArriveAtDestination);
                    }
                    VoiceInfoBuffer.add(VoiceInstr.Please);
                    VoiceInfoBuffer.add(VoiceInstr.GoStraight);
                }
                smoothedWindowMinBoundIndex = LocResult.EstimatedPositions.size() - 1;

                offRouteCount = 0;
                currentPathDir = routedPathSegments.get(ii).DirAngle;
                currentLSIndex = ii;
                prevLSu = uu;
                currentLSu = uu;
                WarningAdvanceTurn = false;
                WarningTurn = false;
                WarningGiven.clear();
                atTurningArea = false;
                consecutiveBackwardCount = 0;
                //UtilitiesFunc.WriteLineInFile(ConfigSettings.NAVIGATION_FILENAME, string.Format("> Switch to line segment #{0}, uu={1}, distance={2}", currentLSIndex, currentLSu, distance));
            }
            else
            {
                offRouteCount++;
                //UtilitiesFunc.WriteLineInFile(ConfigSettings.NAVIGATION_FILENAME, "> OFFPATH; no voice are generated");

                if (offRouteCount < ConfigSettings.OFFPATH_COUNTER_TOL)
                {
                    return -1;
                }
                else
                {
                    return -10;
                }
            }
        }
        
        
        // === 4 === Check if warning to go thru door is needed on the current line segment
        Iterator<Double> keymapf = routedPathSegmentsMapFeature.get(currentLSIndex).keySet().iterator();
		while (keymapf.hasNext()) {
            Double mapfName = keymapf.next();
            String mapfValue = routedPathSegmentsMapFeature.get(currentLSIndex).get(mapfName);
            double tolerance;
            if (mapfValue.contains("DOOR"))
            {
                tolerance = ConfigSettings.WARNING_THRU_DOOR_M;
            }
            else if (mapfValue.contains("DESCRIP"))
            {
                tolerance = ConfigSettings.WARNING_DESCRIPTION_M;
            }
            else
            {
                continue;
            }

            double distancef = routedPathSegments.get(currentLSIndex).DistanceBTW2PtsOnLS(currentLSu, mapfName);
            //UtilitiesFunc.WriteLineInFile(ConfigSettings.NAVIGATION_FILENAME, String.Format("<C> {0}, dist={1}, t={2}", mapf.Value, distancef, tolerance));

            if (distancef < tolerance)
            //|| GeometryFunc.EuclideanDistanceInMeter(smoothedPt, routedPathSegments[currentLSIndex].GetPointFromEqn(mapf.Key)) < ConfigSettings.tolerance)
            {
                if (WarningGiven.containsKey(mapfName))
                {
                    WarningGiven.put(mapfName, WarningGiven.get(mapfName) + 1);
                }
                else
                {
                	WarningGiven.put(mapfName, 1);
                }

                //UtilitiesFunc.WriteLineInFile(ConfigSettings.NAVIGATION_FILENAME, "waring check:" + WarningGiven[mapf.Key]);
                if (WarningGiven.get(mapfName) % ConfigSettings.WARNING_REPEAT_COUNT == 1)
                {
                    if (VoiceInstBuffer.size() > 0)
                    {
                        VoiceInstBuffer.add(VoiceInstr.And);
                    }


                    if (mapfValue.contains("Door"))
                    {
                        InstrCode += "D ";
                        VoiceInstBuffer.add(VoiceInstr.MoveThruDoor);

                        Point doorpt = routedPathSegments.get(currentLSIndex).GetPointFromEqn(mapfName);
                        //UtilitiesFunc.WriteLineInFile(ConfigSettings.NAVIGATION_FILENAME, string.Format("> Move Thru Door Warning - ({0},{1}) {2}", doorpt.X, doorpt.Y, mapf.Value));
                    }
                    else if (mapfValue.contains("DESCRIP"))
                    {
                        InstrCode += "F ";
                        VoiceInstBuffer.add(VoiceInstr.ADDITIONAL);
                        VoiceAdditionalBuffer.add(mapfValue);

                        Point descpt = routedPathSegments.get(currentLSIndex).GetPointFromEqn(mapfName);
                        //UtilitiesFunc.WriteLineInFile(ConfigSettings.NAVIGATION_FILENAME, string.Format("> Description Warning - ({0},{1}) {2}", descpt.X, descpt.Y, mapf.Value));
                    }
                }
            }
        }

        // if the routed path requires to turn
        if (currentLSIndex < routedPathSegments.size() - 1)
        {
            Point turnPt = routedPathSegments.get(currentLSIndex).PtE;
            int chosenTurningAngle = routedPathTurningPtDBAngle.get(currentLSIndex);

            //UtilitiesFunc.WriteLineInFile(ConfigSettings.NAVIGATION_FILENAME, string.Format("<C> turnpt:{0},{1}, angle", turnPt.X, turnPt.Y, chosenTurningAngle));
            Point[] boundary;

            // === 5 === Check if warning to turn
            boundary = MapInfo.TurningLMAreaList.get(turnPt).get(chosenTurningAngle).turnBoundary;
            if (!WarningTurn)
            {
                //UtilitiesFunc.WriteLineInFile(ConfigSettings.NAVIGATION_FILENAME, "<C> Check turn");
                if (GeometryFunc.IsPtInsideNonRotatedBox(smoothedPt, boundary[0], boundary[1], boundary[2], boundary[3], 0))
                {
                    WarningTurn = true;
                    turnCode = FindTurnSide(routedPathSegments.get(currentLSIndex).DirAngle, routedPathSegments.get(currentLSIndex + 1).DirAngle);
                    if (VoiceInstBuffer.size() > 0)
                    {
                        VoiceInstBuffer.add(VoiceInstr.And);
                    }

                    if (turnCode == 1)
                    {
                        InstrCode += "TR ";
                        VoiceInstBuffer.add(VoiceInstr.TurnRight);
                    }
                    else if (turnCode == 2)
                    {
                        InstrCode += "TL ";
                        VoiceInstBuffer.add(VoiceInstr.TurnLeft);
                    }
                    //UtilitiesFunc.WriteLineInFile(ConfigSettings.NAVIGATION_FILENAME, string.Format(" > Warning - turn (map)- code{0}", turnCode));

                    if (!atTurningArea)
                    {
                        atTurningArea = true;
                        smoothedWindowMinBoundIndex = LocResult.EstimatedPositions.size() - 1;
                        //UtilitiesFunc.WriteLineInFile(ConfigSettings.NAVIGATION_FILENAME, string.Format(" >> Window size min bound index = {0}", smoothedWindowMinBoundIndex));
                    }

                }
                else if (routedPathSegments.get(currentLSIndex).DistanceBTW2PtsOnLS(currentLSu, 1) < ConfigSettings.WARNING_TURN_M)
                {
                    WarningTurn = true;
                    turnCode = FindTurnSide(routedPathSegments.get(currentLSIndex).DirAngle, routedPathSegments.get(currentLSIndex + 1).DirAngle);
                    if (VoiceInstBuffer.size() > 0)
                    {
                        VoiceInstBuffer.add(VoiceInstr.And);
                    }

                    if (turnCode == 1)
                    {
                        InstrCode += "TR ";
                        VoiceInstBuffer.add(VoiceInstr.TurnRight);
                    }
                    else if (turnCode == 2)
                    {
                        InstrCode += "TL ";
                        VoiceInstBuffer.add(VoiceInstr.TurnLeft);
                    }
                    //UtilitiesFunc.WriteLineInFile(ConfigSettings.NAVIGATION_FILENAME, string.Format(" > Warning - turn (u)- code{0}", turnCode));

                    if (!atTurningArea)
                    {
                        atTurningArea = true;
                        smoothedWindowMinBoundIndex = LocResult.EstimatedPositions.size() - 1;
                        //UtilitiesFunc.WriteLineInFile(ConfigSettings.NAVIGATION_FILENAME, string.Format(" >> Window size min bound index = {0}", smoothedWindowMinBoundIndex));
                    }

                }
            }

            // === 6 === Check if warning to advance turn
            boundary = MapInfo.TurningLMAreaList.get(turnPt).get(chosenTurningAngle).advanceTurnBoundary;
            if (!WarningAdvanceTurn && !WarningTurn)
            {
                //UtilitiesFunc.WriteLineInFile(ConfigSettings.NAVIGATION_FILENAME, "<C> Check advance turn");
                if (GeometryFunc.IsPtInsideNonRotatedBox(smoothedPt, boundary[0], boundary[1], boundary[2], boundary[3], 0))
                {
                    WarningAdvanceTurn = true;
                    turnCode = FindTurnSide(routedPathSegments.get(currentLSIndex).DirAngle, routedPathSegments.get(currentLSIndex + 1).DirAngle);
                    if (VoiceInstBuffer.size() > 0)
                    {
                        VoiceInstBuffer.add(VoiceInstr.And);
                    }
                    if (turnCode == 1)
                    {
                        InstrCode += "ATR ";
                        VoiceInstBuffer.add(VoiceInstr.AdvanceTurnRight);
                    }
                    else if (turnCode == 2)
                    {
                        InstrCode += "ATL ";
                        VoiceInstBuffer.add(VoiceInstr.AdvanceTurnLeft);
                    }
                    //UtilitiesFunc.WriteLineInFile(ConfigSettings.NAVIGATION_FILENAME, string.Format(" > Warning - advance turn (map) - code{0}", turnCode));

                    if (!atTurningArea)
                    {
                        atTurningArea = true;
                        smoothedWindowMinBoundIndex = LocResult.EstimatedPositions.size() - 1;
                        //UtilitiesFunc.WriteLineInFile(ConfigSettings.NAVIGATION_FILENAME, string.Format(" >> Window size min bound index = {0}", smoothedWindowMinBoundIndex));
                    }

                }
                else if (routedPathSegments.get(currentLSIndex).DistanceBTW2PtsOnLS(currentLSu, 1) < ConfigSettings.WARNING_ADVANCE_TURN_M)
                {
                    WarningAdvanceTurn = true;
                    turnCode = FindTurnSide(routedPathSegments.get(currentLSIndex).DirAngle, routedPathSegments.get(currentLSIndex + 1).DirAngle);
                    if (VoiceInstBuffer.size() > 0)
                    {
                        VoiceInstBuffer.add(VoiceInstr.And);
                    }
                    if (turnCode == 1)
                    {
                        InstrCode += "ATR ";
                        VoiceInstBuffer.add(VoiceInstr.AdvanceTurnRight);
                    }
                    else if (turnCode == 2)
                    {
                        InstrCode += "ATL ";
                        VoiceInstBuffer.add(VoiceInstr.AdvanceTurnLeft);
                    } 
                    //UtilitiesFunc.WriteLineInFile(ConfigSettings.NAVIGATION_FILENAME, string.Format(" > Warning - advance turn (u) - code{0}", turnCode));

                    if (!atTurningArea)
                    {
                        atTurningArea = true;
                        smoothedWindowMinBoundIndex = LocResult.EstimatedPositions.size() - 1;
                        //UtilitiesFunc.WriteLineInFile(ConfigSettings.NAVIGATION_FILENAME, string.Format(" >> Window size min bound index = {0}", smoothedWindowMinBoundIndex));
                    }
                }
            }


            // === 7 === Check if warning to go thru door is needed on the next line segment
            if (atTurningArea)
            {
            	Iterator<Double> mapf = routedPathSegmentsMapFeature.get(currentLSIndex+1).keySet().iterator();
                while(mapf.hasNext()){
            	//UtilitiesFunc.WriteLineInFile(ConfigSettings.NAVIGATION_FILENAME, "<C> check next line segment door");
                	Double mapfName = mapf.next();
                	String mapfValue = routedPathSegmentsMapFeature.get(currentLSIndex+1).get(mapfName);
                    
                    if (mapfValue.contains("DOOR"))
                    {
                        if (GeometryFunc.EuclideanDistanceInMeter(smoothedPt, routedPathSegments.get(currentLSIndex + 1).GetPointFromEqn(mapfName)) < ConfigSettings.WARNING_THRU_DOOR_M)
                        {
                            if (WarningGiven.containsKey(mapfName + 1))
                            {
                                WarningGiven.put(mapfName + 1, WarningGiven.get(mapfName + 1) + 1);
                            }
                            else
                            {
                                WarningGiven.put(mapfName + 1, 1);
                            }
                            //UtilitiesFunc.WriteLineInFile(ConfigSettings.NAVIGATION_FILENAME, "Door waring check:" + WarningGiven[mapf.Key + 1]);
                            if (WarningGiven.get(mapfName + 1) % ConfigSettings.WARNING_REPEAT_COUNT == 1)
                            {

                                if (VoiceInstBuffer.size() > 0)
                                {
                                    VoiceInstBuffer.add(VoiceInstr.And);
                                }

                                InstrCode += "D ";
                                VoiceInstBuffer.add(VoiceInstr.MoveThruDoor);

                                Point doorpt = routedPathSegments.get(currentLSIndex + 1).GetPointFromEqn(mapfName);
                                //UtilitiesFunc.WriteLineInFile(ConfigSettings.NAVIGATION_FILENAME, string.Format("> Move Thru Door Warning - ({0},{1}) {2}", doorpt.X, doorpt.Y, mapf.Value));
                            }
                        }
                    }
                }
            }
        }

              

        if (VoiceInstBuffer.size() > 0 || VoiceInfoBuffer.size() > 0)
        {
            noInstrCount = 0;
        }
        else
        {
            noInstrCount++;
        }

        if (!atTurningArea && noInstrCount >= maxNoInstrCount)
        {
            InstrCode += "S ";
            VoiceInstBuffer.add(VoiceInstr.GoStraight);
            noInstrCount = 0;
        }

        return VoiceInstBuffer.size() > 0 || VoiceInfoBuffer.size() > 0 ? 5 : 0;
    }
    
    public static boolean ReachDestination()
    {
        return routedPathSegments.size() > 0 && reachDestination;
    }

    public static List<VoiceInstr> GetVoiceBuffer()
    {
        List<VoiceInstr> cmdlist = new ArrayList<VoiceInstr>();

        if (VoiceInstBuffer.size() > 0 && !VoiceInstBuffer.contains(VoiceInstr.GoStraight))
        {
            cmdlist.addAll(VoiceInstBuffer);
        }

        if (VoiceInfoBuffer.size() > 0)
        {
            cmdlist.addAll(VoiceInfoBuffer);
        }
        return cmdlist;
    }

    public static List<String> GetVoiceAdditionalBuffer()
    {
        List<String> addlist = new ArrayList<String>(VoiceAdditionalBuffer);
        return addlist;
    }

    ///// <summary>
    ///// Play the voice instruction stored in the VoiceInstBuffer
    ///// </summary>
    ///// 
    
    /// <summary>
    /// Play the voice instruction stored in the VoiceInstBuffer
    /// </summary>
    /// <returns>true if it plays voice</returns>
    public static boolean PlayVoiceInstructions() throws InterruptedException
    {
        boolean playvoice = false;
        InstrCode = "";
        int durationtime;

        if (reachDestination)
        {
/*            if (VoiceOutAudio.LocateDestinationAudioFile(PathRouting.DestinationPlace))
            {
                durationtime = VoiceOutAudio.PlayAudio(VoiceInstr.YouHaveArrived);
                Thread.sleep(durationtime);

                durationtime = VoiceOutAudio.PlayDestinationAudio();
                Thread.sleep(durationtime);

                VoiceOutAudio.PlayAudio(destinationSide);
            }
            else
            {
                durationtime = VoiceOutAudio.PlayAudio(VoiceInstr.YouAreHere);
                Thread.sleep(durationtime); 
                
                VoiceOutAudio.PlayAudio(destinationSide);
            }*/
            
            String speech1 = VoiceOutAudio.PlayAudio(VoiceInstr.YouHaveArrived);
            MapActivity.Speak(speech1,TextToSpeech.QUEUE_FLUSH);
            
            String speech2 = PathRouting.DestinationPlace;
            MapActivity.Speak(speech2,TextToSpeech.QUEUE_ADD);
            
            String speech3 = VoiceOutAudio.PlayAudio(destinationSide);
            MapActivity.Speak(speech3,TextToSpeech.QUEUE_ADD);
            
            return true;
        }

        boolean firstcommand = true;
        durationtime = ConfigSettings.DEFAULTVOICEDURATION;

        if (VoiceInstBuffer.size() > 0)
        {
            playvoice = true;

            //UtilitiesFunc.WriteLineInFile(ConfigSettings.NAVIGATION_FILENAME, "==Play instrs:");
            for (VoiceInstr cmd : VoiceInstBuffer)
            {
            	int queueMode = TextToSpeech.QUEUE_ADD;
                if (!firstcommand)
                {
                    Thread.sleep(durationtime);
                	//queueMode = TextToSpeech.QUEUE_ADD;
                }

                firstcommand = false;
                if (cmd == VoiceInstr.ADDITIONAL)
                {
                    String instr = VoiceAdditionalBuffer.get(0);
                    VoiceAdditionalBuffer.remove(0);
                    //UtilitiesFunc.WriteLineInFile(ConfigSettings.NAVIGATION_FILENAME, " =" + instr);
                    String speech4 = VoiceOutAudio.PlayAdditionalAudio(instr);
                    MapActivity.Speak(speech4, queueMode);
                    durationtime = VoiceOutAudio.VoiceDurationAdditionalCheck(instr);
                }
                else
                {
                    //UtilitiesFunc.WriteLineInFile(ConfigSettings.NAVIGATION_FILENAME, " =" + cmd.ToString());
                	String speech4 = VoiceOutAudio.PlayAudio(cmd);
                    MapActivity.Speak(speech4,queueMode);
                	durationtime = VoiceOutAudio.VoiceDurationCheck(cmd);
                }
            }
            Thread.sleep(durationtime);
            VoiceInstBuffer.clear();
        }

        if (VoiceInfoBuffer.size() > 0)
        {
            playvoice = true;
            
            //UtilitiesFunc.WriteLineInFile(ConfigSettings.NAVIGATION_FILENAME, "==Play info instrs:");
            for (VoiceInstr cmd : VoiceInfoBuffer)
            {
            	int queueMode = TextToSpeech.QUEUE_ADD;
                if (!firstcommand)
                {
                    //queueMode = TextToSpeech.QUEUE_ADD;
                	Thread.sleep(durationtime);
                }
                firstcommand = false;

                //UtilitiesFunc.WriteLineInFile(ConfigSettings.NAVIGATION_FILENAME, " =" + cmd.ToString());   
                String speech5 =  VoiceOutAudio.PlayAudio(cmd);
                MapActivity.Speak(speech5, queueMode);
                durationtime = VoiceOutAudio.VoiceDurationCheck(cmd);
            }
            Thread.sleep(durationtime);
            VoiceInfoBuffer.clear();
        }

        return playvoice;
    }
    
    /****************************************************************************************************************
     * Private Functions
     * *************************************************************************************************************/

     private static boolean CheckIfReachDestination(Point smoothedUpdate)
     {
         if (GeometryFunc.EuclideanDistanceInMeter(smoothedUpdate, PathRouting.DestinationLoc) < ConfigSettings.DESTINATION_TOL_M)
         {
             //UtilitiesFunc.WriteLineInFile(ConfigSettings.NAVIGATION_FILENAME, ">> Reach Destination");
             destinationTurnCode = FindTurnSide(currentPathDir, GeometryFunc.ComputeAngle(smoothedUpdate, PathRouting.DestinationLoc, false));
             return true;
         }
         else if (GeometryFunc.EuclideanDistanceInMeter(smoothedUpdate, routedPathSegments.get(routedPathSegments.size()-1).PtE) < ConfigSettings.DESTINATION_TOL_M)
         {
             //UtilitiesFunc.WriteLineInFile(ConfigSettings.NAVIGATION_FILENAME, ">> Reach Destination on path");
             destinationTurnCode = FindTurnSide(currentPathDir, GeometryFunc.ComputeAngle(smoothedUpdate, PathRouting.DestinationLoc, false));
             return true;
         }
         else
         {
             return false;
         }
     }                
             
         
     private static VoiceInstr GetAnnouncementTurn(int currentdir, int nextdir)
     {
         int turn = FindTurnSide(currentdir, nextdir);
         if (turn == 1)
         {
             return VoiceInstr.TurnRight;
         }
         else if (turn == 2)
         {
             return VoiceInstr.TurnLeft;
         }
         else
         {
             return null;
         }
     }
     /// <summary>
     /// Determine which side to turn given the current direction and the next path direction
     /// </summary>
     /// <param name="currentdir"></param>
     /// <param name="nextdir"></param>
     /// <returns>0 - walk straight; 1 - turn right; 2 - turn left</returns>
     private static int FindTurnSide(int currentdir, int nextdir)
     {
         // Find the angle difference; in the same range: [0,360)
         int angleDiff = nextdir - currentdir;
         if (angleDiff < 0)
         {
             angleDiff = angleDiff + 360;
         }

         if (45 <= angleDiff && angleDiff < 180)
         {
             // turn right
             return 1;
         }
         else if (180 < angleDiff && angleDiff <= 270 + 45)
         {
             // turn left
             return 2;
         }
         else
         {
             // walk straight 
             return 0;
         }
     }

     private static VoiceInstr GetAnnouncementFacingDirection(int angle)
     {
         if (45 < angle && angle <= 135)
         {
             return VoiceInstr.East;
         }
         else if (135 < angle && angle <= 225)
         {
             return VoiceInstr.South;
         }
         else if (225 < angle && angle <= 315)
         {
             return VoiceInstr.West;
         }
         else
         {
             return VoiceInstr.North;
         }
     }
     /// <summary>
     /// Return the VoiceInstruction "x meters", where x is determined by the distance between pt1 and pt2
     /// </summary>
     /// <param name="pt1"></param>
     /// <param name="pt2"></param>
     /// <returns></returns>
     private static VoiceInstr GetAnnouncementDistance(Point pt1, Point pt2)
     {
         int distance_m = (int)Math.round(GeometryFunc.EuclideanDistanceInMeter(pt1, pt2));
         return Distance2Voice(distance_m);            
     }

     /// <summary>
     /// Return the VoiceInstruction "x meters", where x is given
     /// </summary>
     /// <param name="distance_m"></param>
     /// <returns></returns>
     private static VoiceInstr GetAnnouncementDistance(double distance_m)
     {
         return Distance2Voice((int)Math.round(distance_m));
     }

     /// <summary>
     /// Pick up which instruction should be obtained given the integer-parsed distance
     /// </summary>
     /// <param name="distance_m"></param>
     /// <returns></returns>
     private static VoiceInstr Distance2Voice(int distance_m)
     {
         switch (distance_m)
         {
             case 0:
             case 1:
                 return VoiceInstr.M1;
             case 2:
                 return VoiceInstr.M2;
             case 3:
                 return VoiceInstr.M3;
             case 4:
                 return VoiceInstr.M4;
             case 5:
                 return VoiceInstr.M5;
             case 6:
                 return VoiceInstr.M6;
             case 7:
                 return VoiceInstr.M7;
             case 8:
                 return VoiceInstr.M8;
             case 9:
                 return VoiceInstr.M9;
             case 10:
             case 11:
             case 12:
                 return VoiceInstr.M10;
             case 13:
             case 14:
             case 15:
             case 16:
             case 17:
                 return VoiceInstr.M15;
             case 18:
             case 19:
             case 20:
             case 21:
             case 22:
                 return VoiceInstr.M20;
             case 23:
             case 24:
             case 25:
             default:
                 return VoiceInstr.M25;
         }
     }
}
    
