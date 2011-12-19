package com.IndoorGPS.Tracking;

import java.util.ArrayList;
import java.util.List;

import Jama.Matrix;
import android.graphics.Point;

import com.IndoorGPS.Utilities;
import com.IndoorGPS.Localization.APCSLocalization;
import com.IndoorGPS.LocalizerBasicClass.ConfigSettings;
import com.IndoorGPS.LocalizerBasicClass.GeometryFunc;
import com.IndoorGPS.LocalizerBasicClass.LocDB;
import com.IndoorGPS.LocalizerBasicClass.LocResult;

public class MKFTracking {

    private static List<Integer> chosenFPs = new ArrayList<Integer>();
    private static boolean isTurningPtPrev = false;
    private static boolean isTurningPt = false;
    private static int turningPtIndex = 0;
    private static int chosenTurningAngle = 1000;

    private static boolean useCommonFP = false;
    private static int commonFPNotFoundCount = 0;
    private static int wrongEstimationCount = 0;

    private static boolean useDCheading = false;
    private static boolean useEstimate = false;

    //Additional code to support map-assisted tracking including to choose FPs for fine localization

    //use previous estimated location to predict current possible area and hence select the potential candidates FPS

    public static void SetUseCompass(boolean useDC, boolean useEst)
    {
        useDCheading = useDC;
        useEstimate = useEst;
    }

    public static void ResetMKF()
    {
        isTurningPt = false;
        isTurningPtPrev = false;
        wrongEstimationCount = 0;
        commonFPNotFoundCount = 0;
        
    }

    public static boolean CoarseLocS2_ChooseRelevantFPs(int headingangle)
    {
        chosenFPs.clear();

        if (LocResult.ComputedPositionMeas.size() == 0)
        {
            //UtilitiesFunc.WriteLineInFile(ConfigSettings.LOGFILENAME, "** First Est - use cluster for coarse localization");
            APCSLocalization.SelfLocS2_MatchCluster(headingangle);
            useCommonFP = true;
            return true;
        }

        Point prevEstPt = LocResult.EstimatedPositions.get(LocResult.EstimatedPositions.size()-1);

        // check if previous is a turning pt
        if (!isTurningPtPrev)
        {
            for (int ts = 0; ts < LocDB.turningXYBoundarySet.size(); ts++)
            {
                       	
            	if (LocDB.turningXYBoundarySet.get(ts)[0] < prevEstPt.x && prevEstPt.x < LocDB.turningXYBoundarySet.get(ts)[1] &&
                    LocDB.turningXYBoundarySet.get(ts)[2] < prevEstPt.y && prevEstPt.y < LocDB.turningXYBoundarySet.get(ts)[3])
                {
                    isTurningPt = true;
                    turningPtIndex = ts;
                    LocResult.DetectedTurningPoints.add(LocResult.EstimatedPositions.size() - 1);
                    //UtilitiesFunc.WriteLineInFile(ConfigSettings.LOGFILENAME, String.Format("** Est {0} is a turning pt; (FP {1})", LocResult.EstimatedPositions.Count - 1, LocDB.turningFPSet[turningPtIndex]));
                }
            }
        }

        // choose fps pt based on liner model (even for turning points)
        //if (!isTurningPt)
        //{
            
            //UtilitiesFunc.WriteLineInFile(ConfigSettings.LOGFILENAME, "** Use linear model to predict/choose FPs");

            // double[] predXY = KFTracking.GetPredXY();                    
            //UtilitiesFunc.WriteLineInFile(ConfigSettings.LOGFILENAME, "** Use linear model to predict/choose FPs");

            for (int fp = 0; fp < LocDB.numRPs; fp++)
            {
                //double distance = Math.Sqrt(Math.Pow((predXY[0] - LocDB.refptsList[fp] / 100000) / ConfigSettings.METER_2_PIXEL_X, 2) +
                //                            Math.Pow((predXY[1] - LocDB.refptsList[fp] % 100000) / ConfigSettings.METER_2_PIXEL_Y, 2));
                //if (distance < ConfigSettings.COARSEDIST_R_M)
                //{                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                         
                //    chosenFPs.Add(fp);
                //}

                double distancePrev;

                distancePrev = Math.sqrt(Math.pow((LocResult.EstimatedPositions.get(LocResult.EstimatedPositions.size()-1).x - LocDB.x00List.get(fp)) / ConfigSettings.METER_2_PIXEL_X, 2) +
                               Math.pow((LocResult.EstimatedPositions.get(LocResult.EstimatedPositions.size()-1).y - LocDB.y00List.get(fp)) / ConfigSettings.METER_2_PIXEL_Y, 2));


                if (distancePrev < ConfigSettings.COARSEDIST_R_M)
                {
                    chosenFPs.add(fp);
                }
            }
        //}
        //else
        //{
        //    //// --- Turning pt ---


            ////   --- For Valid Heading angle
            //if (useDCheading && headingangle > 0)
            //{
            //    int absdiff;
            //    int absdiffC = 200;
            //    boolean firstangle = true;
            //    foreach (int dirangle in LocDB.turningFPMembersSet[turningPtIndex].Keys)
            //    {
            //        if (firstangle)
            //        {
            //            chosenTurningAngle = dirangle;
            //            firstangle = false;
            //        }
            //        else
            //        {
            //            absdiffC = Math.Abs(headingangle - chosenTurningAngle);
            //            if (absdiffC > 180)
            //            {
            //                absdiffC = 360 - absdiffC;
            //            }

            //            absdiff = Math.Abs(headingangle - dirangle);
            //            if (absdiff > 180)
            //            {
            //                absdiff = 360 - absdiff;
            //            }

            //            if (absdiff < absdiffC)
            //            {
            //                chosenTurningAngle = dirangle;
            //            }
            //        }
            //    }
            //    UtilitiesFunc.WriteLineInFile(ConfigSettings.LOGFILENAME, String.Format("** Turning heading angle: {0}; choose dir-{1} for members selection", headingangle, chosenTurningAngle));
            //    chosenFPs = LocDB.turningFPMembersSet[turningPtIndex][chosenTurningAngle].ToList();
            //}
            //else
            //{
            //    // --- For Invalid Heading angle

            //    foreach (int dirangle in LocDB.turningFPMembersSet[turningPtIndex].Keys)
            //    {
            //        chosenFPs.AddRange(LocDB.turningFPMembersSet[turningPtIndex][dirangle].ToList());
            //    }
            //    UtilitiesFunc.WriteLineInFile(ConfigSettings.LOGFILENAME, string.Format("** Turning heading angle is invalid/Not used; choose all the possible members selection at that turning"));
            //}
        //}

        //StreamWriter sw = File.AppendText(ConfigSettings.LOGFILENAME);
        //sw.Write("Choosen FPs = ");
        //foreach (int ff in chosenFPs)
        //{
        //    sw.Write("{0} ", ff);
        //}
        //sw.WriteLine();
        //sw.Close();

        useCommonFP = APCSLocalization.SelfLocS2t_CreatePsi(headingangle, chosenFPs);

        if (useCommonFP)
        {
            commonFPNotFoundCount = 0;
            return true;
        }
        else
        {
            commonFPNotFoundCount++;
            if (commonFPNotFoundCount == ConfigSettings.NO_COMMON_FP_COUNT)
            {
               // UtilitiesFunc.WriteLineInFile(ConfigSettings.LOGFILENAME, string.Format("** Use RSS match cluster method. (no common FP count exceed {0})", commonFPNotFoundCount.ToString()));

                commonFPNotFoundCount = 0;
                return true;
            }
            else
            {
                //UtilitiesFunc.WriteLineInFile(ConfigSettings.LOGFILENAME, "** skip this RSS online readings as no common FPs found - " + commonFPNotFoundCount.ToString());
                return false;
            }
        }
    }

    public static int ValidateEstimation()
    {            
        if (LocResult.EstimatedPositions.size() < 2)
        {
            wrongEstimationCount = 0;
            return 0;
        }

        double traveldistance = GeometryFunc.EuclideanDistanceInMeter(LocResult.EstimatedPositions.get(LocResult.EstimatedPositions.size() - 2), LocResult.EstimatedPositions.get(LocResult.EstimatedPositions.size()-1));
        if (traveldistance > ConfigSettings.MAX_DISTANCE_BTW_UPDATES_M)
        {
            wrongEstimationCount++;
        }
        else
        {
            wrongEstimationCount = 0;
            return 0;
        }

        if (wrongEstimationCount == ConfigSettings.EXCEED_MAX_DIST_BTW_UPDATE_COUNT)
        {
            //UtilitiesFunc.WriteLineInFile(ConfigSettings.LOGFILENAME, string.Format(">>> Estimation is far from prev point (Exceed {0} times); Reset Kalman Filter", ConfigSettings.EXCEED_MAX_DIST_BTW_UPDATE_COUNT));
            KFTracking.ResetWPrevComputedLoc();
            
            LocResult.EstimatedPositions.set(LocResult.EstimatedPositions.size() - 1, LocResult.ComputedPositionMeas.get(LocResult.ComputedPositionMeas.size()-1));
            wrongEstimationCount = 0;
            return 2;
        }
        else if (wrongEstimationCount != 0)
        {
            //UtilitiesFunc.WriteLineInFile(ConfigSettings.LOGFILENAME, ">>> Estimation is far from prev point; ignore this update");
            LocResult.ComputedPositionMeas.remove(LocResult.ComputedPositionMeas.size() - 1);
            LocResult.EstimatedPositions.remove(LocResult.EstimatedPositions.size() - 1);
            LocResult.CompassHeadingAngle.remove(LocResult.CompassHeadingAngle.size() - 1);
            return 1;
        }
        return 0;
        
    }
    

    public static Point ComputeEstimate()
    {
        Point currentmeas = LocResult.ComputedPositionMeas.get(LocResult.ComputedPositionMeas.size()-1);
        Point estPt;
        Matrix estX;

        if (LocResult.EstimatedPositions.size() == 0)
        {
            //UtilitiesFunc.WriteLineInFile(ConfigSettings.LOGFILENAME, "** Use currentmeas as initX for KF"); 
            KFTracking.SetInitPos(currentmeas);
            estPt = currentmeas;
            
        }
        else
        {
            if (!isTurningPt)
            {
                isTurningPtPrev = false;
                estX = KFTracking.UpdateKF(currentmeas);
                estPt = new Point((int)Math.round(estX.get(0, 0)), (int)Math.round(estX.get(1, 0)));
            }
            else
            {
                // reset KF as it's at turning pt
                isTurningPtPrev = true;
                isTurningPt = false;

                double vx = ConfigSettings.METER_2_PIXEL_X * ConfigSettings.DEFAULT_TURNING_STEPSIZE_M * Math.sin(chosenTurningAngle / 180 * Math.PI);
                double vy = -1 * ConfigSettings.METER_2_PIXEL_Y * ConfigSettings.DEFAULT_TURNING_STEPSIZE_M * Math.cos(chosenTurningAngle / 180 * Math.PI);
                
                //UtilitiesFunc.WriteLineInFile(ConfigSettings.LOGFILENAME, string.Format("** Reset KF with heading:{0} Stepsize:{1}", chosenTurningAngle, ConfigSettings.DEFAULT_TURNING_STEPSIZE_M)); 
                KFTracking.ResetWPrevLoc(vx, vy);

                estX = KFTracking.UpdateKF(currentmeas);
                estPt = new Point((int)Math.round(estX.get(0, 0)), (int)Math.round(estX.get(1, 0)));
            }
            //UtilitiesFunc.WriteLineInFile(ConfigSettings.LOGFILENAME, string.Format("#{0} estimated state = [{1} {2} {3} {4}]'", LocResult.EstimatedPositions.Count+1, estX.ColumnPackedCopy[0], estX.ColumnPackedCopy[1], estX.ColumnPackedCopy[2], estX.ColumnPackedCopy[3]));
        }
        LocResult.EstimatedPositions.add(estPt);
        //UtilitiesFunc.WriteLineInFile(ConfigSettings.LOGFILENAME, string.Format("#{0}: \nMeas=({1},{2})\nEst=({3},{4})", LocResult.EstimatedPositions.Count, currentmeas.X, currentmeas.Y, estPt.X, estPt.Y));
        return estPt;
    }
	
}
