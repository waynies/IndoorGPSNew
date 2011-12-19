/**
 * 
 */
package com.IndoorGPS.Localization;

import java.io.File;
import java.io.OutputStreamWriter;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import Jama.Matrix;
import android.graphics.Point;
import android.util.Log;

import com.IndoorGPS.MapActivity;
import com.IndoorGPS.Utilities;
import com.IndoorGPS.LocalizerBasicClass.*;
import com.IndoorGPS.MathAlgorithm.CSMath;

/** static class providing localization functions for online phase
 *
 */
public class APCSLocalization {
    // Algorithm Settings
    private static boolean useCluster;
    private static boolean use4OrientationDB;
    private static boolean use4OforFineLoc;

    private static boolean excludeInvalidRSS4CoarseLoc;
    private static boolean excludeInvalidRSS4FineLoc;

    private static boolean useRandomMatrix;
    private static int m_strongestAPs;
    private static int nn_randomMatrix;

    // data variables
    private static HashMap<Integer, Double> y = new HashMap<Integer, Double>();
    private static Matrix y_strongAPs;
    
    // matched cluster head
    private static HashMap<Orientation, double[]> similarityMetric = new HashMap<Orientation, double[]>();
    private static double maxS, minS;

    private static int[] APSelectionRowIndex;
    private static List<Integer> APSelectionRowIndexCoaLoc = new ArrayList<Integer>();
    private static List<Integer> APSelectionRowIndexFineLoc = new ArrayList<Integer>();

    private static List<Orientation> chosenDBOrientation = new ArrayList<Orientation>();
    private static HashMap<Orientation, int[]> matchedIndexInClusterHeadList = new HashMap<Orientation, int[]>();
    private static HashMap<Orientation, int[]> matchedClusterMemberIndexList = new HashMap<Orientation, int[]>();

    // CS l1-norm min required matrix
    private static Matrix R, Q, T, Psi_FineLoc, Phi;
    private static Matrix theta_hat;
    
    private static List<Integer> x00thetaIndex_FineLoc = new ArrayList<Integer>();
    private static List<Integer> y00thetaIndex_FineLoc = new ArrayList<Integer>();


    // Result
    private static Point calLoc;

    /*
     * Set up the localization system
     * @param usec		Use Cluster
     * @param use4O		Use 4 orientation database
     * @param use4Ofl	Use all 4 orientation for fine localization
     * @param userand	Use random AP selection matrix
     * @param mAP		Number of APs to be used
     * @param nRM		Number of rows for the random AP selection matrix
     * 
     */
   
     public static void SetUp(boolean usec, boolean use4O, boolean use4Ofl, int mAP)
     {

         useCluster = usec;
         use4OrientationDB = use4O;
         use4OforFineLoc = use4Ofl;

         excludeInvalidRSS4CoarseLoc = ConfigSettings.EXCLUDE_INVALIDRSS_COALOC;
         excludeInvalidRSS4FineLoc = ConfigSettings.EXCLUDE_INVALIDRSS_FINELOC;

         chosenDBOrientation.clear();
         if (use4OrientationDB)
         {
             if (use4OforFineLoc)
             {
                 chosenDBOrientation.add(Orientation.North);
                 chosenDBOrientation.add(Orientation.East);
                 chosenDBOrientation.add(Orientation.South);
                 chosenDBOrientation.add(Orientation.West);

                 //UtilitiesFunc.WriteLineInFile(ConfigSettings.LOGFILENAME, ">>Use NESW DB");
             }
             else
             {
                 // DB is chosen on the fly depend on compass reading; only one orientation is chosen at a time
                // UtilitiesFunc.WriteLineInFile(ConfigSettings.LOGFILENAME, ">>Choice of DB decided by headingDB");
             }
         }
         else
         {
             chosenDBOrientation.add(Orientation.None);
             //UtilitiesFunc.WriteLineInFile(ConfigSettings.LOGFILENAME, ">>Use NoneDB");
         }

         m_strongestAPs = mAP;

         if (m_strongestAPs > LocDB.numAPs)
         {
             m_strongestAPs = 0;
         }

         //UtilitiesFunc.WriteLineInFile(ConfigSettings.LOGFILENAME, string.Format(">>Loc Settings: useC:{0} use4ODB:{1} use4ODC:{2} useRand:{3} #APs:{4} RN:{5} excludeFine:{6} excludeCoa:{7} clusterscheme:{8}", useCluster, use4OrientationDB, use4OforFineLoc, useRandomMatrix, m_strongestAPs, nn_randomMatrix, excludeInvalidRSS4FineLoc, excludeInvalidRSS4CoarseLoc, ConfigSettings.CLUSTER_MATCH_METRIC_CHOICE));
     }

     // Start navi, first time m_strongestAPs = mAP;
     public static void SetUpAPs(int mAP)
     {
    	 m_strongestAPs = mAP;

    	 if (m_strongestAPs > LocDB.numAPs)
    	 {
    		 m_strongestAPs = 0;
    	 }
     }

     /** 
      * Use the heading angle to determine which database should be chosen (when use4OforFineLoc is false)
      * @param headingangle		Heading Angle
      * 
      */
    /**
     public static List<Orientation> WhichDB(int headingangle)
     {
         ObtainDesiredDB(headingangle); 
         return chosenDBOrientation;
     }
 **/   

     /*
      * Create vector Y from collected RSS
      * @param observedRSS		Observed RSS reading
      * @return	isValidRSS		if RSS is valid
      * 
      */  
    
     public static boolean SelfLocS0_CreateY(HashMap<String, List<Integer>> observedRSS)
     {   
         y = Utilities.RearrangeOnlineReading(observedRSS, LocDB.validMacIDList);
         //System.out.println(y);
         if (y.size() >= ConfigSettings.MIN_NUM_VALID_ONLINE)
         {
        	 return true;
         }
         else
         {
        	 return false;
         }
     }    
     
     /**
      * AP Selection 
      * 
      */
    public static void SelfLocS1_APSelection()
    {
    	// No AP selection procedure is run.
        if (m_strongestAPs == 0)
        {
            APSelectionRowIndex = new int[y.size()];
            for (int i = 0; i < y.size(); i++)
            {
                APSelectionRowIndex[i] = i;
            }
            //UtilitiesFunc.WriteLineInFile(ConfigSettings.LOGFILENAME, "*Select all APs for loc");
        }
        else
        {             
        	HashMap<Integer, Double> sorted_y = new HashMap<Integer, Double>();
        	sorted_y = Utilities.sortHashMap(y);
        	List<Integer> mapKeys = new ArrayList<Integer>(sorted_y.keySet());
        	if(m_strongestAPs > mapKeys.size())
        	{
        		m_strongestAPs = mapKeys.size();
        	}
        	//System.out.println(m_strongestAPs);
        	//Log.d("APs:", m_strongestAPs + "");
        	
            APSelectionRowIndex = new int[m_strongestAPs]; 
            for (int i = 0; i < m_strongestAPs; i++)
            {
                APSelectionRowIndex[i] = (int) mapKeys.get(i);
            }
            
            //Array.Sort(APSelectionRowIndex);
            //StreamWriter sw = File.AppendText(ConfigSettings.LOGFILENAME);
            //sw.Write("APselectionRowIndex =");
            //for (int i = 0; i < m_strongestAPs; i++)
            //{
            //    sw.Write("{0} ", APSelectionRowIndex[i]);
            //}
            //sw.WriteLine();
            //sw.Close();
        }

        //StreamWriter ssw = File.AppendText(ConfigSettings.LOGFILENAME);

        APSelectionRowIndexCoaLoc.clear();
        APSelectionRowIndexFineLoc.clear();

        if (excludeInvalidRSS4CoarseLoc)
        {
        	for(Integer rowindex : APSelectionRowIndex)
            {
                if (y.get(rowindex) != ConfigSettings.RSS_DEFAULT_VALUE)
                {
                    APSelectionRowIndexCoaLoc.add(rowindex);
                }
            }
        }
        else
        {
        	APSelectionRowIndexCoaLoc = Utilities.intList(APSelectionRowIndex);
        }
        //ssw.Write("APSelectionRowIndexCoaLoc =");
        //for (int i = 0; i < APSelectionRowIndexCoaLoc.Count; i++)
        //{
        //    ssw.Write("{0} ", APSelectionRowIndexCoaLoc[i]);
        //}
        //ssw.WriteLine();
        

        if (excludeInvalidRSS4FineLoc)
        {
            for(Integer rowindex : APSelectionRowIndex)
            {
                if (y.get(rowindex) != ConfigSettings.RSS_DEFAULT_VALUE)
                {
                    APSelectionRowIndexFineLoc.add(rowindex);
                }
            }
        }
        else
        {
            APSelectionRowIndexFineLoc = Utilities.intList(APSelectionRowIndex);
        }
        //ssw.Write("APSelectionRowIndexCoaLoc =");
        //for (int i = 0; i < APSelectionRowIndexFineLoc.Count; i++)
        //{
        //    ssw.Write("{0} ", APSelectionRowIndexFineLoc[i]);
        //}
        //ssw.WriteLine();
        //ssw.Close();
    	
    }   
   
     /*
      * Compare similariy between online reading and cluster averages to choose clusters for fine localization
      * @param headingangle heading angle
      * 
      */ 
     public static void SelfLocS2_MatchCluster(int headingangle)
     {
         ObtainDesiredDB(headingangle);

         if (useCluster == true)
         {
             FindMatchedCluster();
             CreatePSI_cluster();                
         }
         else
         {
             // no clustering needed
             x00thetaIndex_FineLoc.clear();
             y00thetaIndex_FineLoc.clear();

             if (chosenDBOrientation.size() == 1)
             {
                 Psi_FineLoc = LocDB.Psi.get(chosenDBOrientation).getMatrix(Utilities.Listint(APSelectionRowIndexFineLoc), 0, LocDB.numRPs - 1);
                 
                 x00thetaIndex_FineLoc = new ArrayList<Integer>(LocDB.x00List);
                 y00thetaIndex_FineLoc = new ArrayList<Integer>(LocDB.y00List);

             }
             else
             {
                 int numrow = 0;
                 int numcol = 0;
                                     
                 numrow = APSelectionRowIndexFineLoc.size();
                 numcol = chosenDBOrientation.size() * LocDB.numRPs;
                 Psi_FineLoc = new Matrix(numrow, numcol);

                 int oldI = 0;
                 
                 for (Orientation dir : chosenDBOrientation)
                 {
                     Psi_FineLoc.setMatrix(0, numrow - 1, oldI, oldI + LocDB.numRPs - 1, LocDB.Psi.get(dir).getMatrix(Utilities.Listint(APSelectionRowIndexFineLoc), 0, LocDB.numRPs - 1));
                     
                     oldI += LocDB.numRPs;

                     x00thetaIndex_FineLoc.addAll(LocDB.x00List);
                     y00thetaIndex_FineLoc.addAll(LocDB.y00List);
                 }
                 

             }
         }

         y_strongAPs = new Matrix(APSelectionRowIndexFineLoc.size(), 1);
         for (int i = 0; i < APSelectionRowIndexFineLoc.size(); i++)
         {
             y_strongAPs.set(i, 0, y.get(APSelectionRowIndexFineLoc.get(i)));
         }
         ComputeQTfromPsi(Psi_FineLoc);
     }

     /*
      * Find common FP between the chosenFPsList and the RSS similariy matching scheme for the fine localization
      * @param headingangle		Heading Angle
      * @param chosenFPsList	chosen FP list
      * @return if use common FP
      */

     public static boolean SelfLocS2t_CreatePsi(int headingangle, List<Integer> chosenFPsList)
     {
         boolean useCommonFP = false;

         ObtainDesiredDB(headingangle);

         if (chosenFPsList.size() > 0)
         {
             FindMatchedCluster();

             HashMap<Orientation, Matrix> psi_temp = new HashMap<Orientation, Matrix>();
             x00thetaIndex_FineLoc.clear();
             y00thetaIndex_FineLoc.clear();
             int numrow = APSelectionRowIndexFineLoc.size();
             int totalnumcol = 0;
                             
             for (Orientation dir : chosenDBOrientation)
             {
                 //UtilitiesFunc.WriteLineInFile(ConfigSettings.LOGFILENAME, "* Dir=" + dir.ToString());
                                   

                 // --- Check similariy

                 double chosenFPsSimMetric = 0;
                 for (int api : APSelectionRowIndexCoaLoc)
                 {
                     double avgPsi = Utilities.doubleAve(LocDB.Psi.get(dir).getMatrix(api, api, Utilities.Listint(chosenFPsList)).getRowPackedCopy());
                     
                     chosenFPsSimMetric -= Math.pow(avgPsi - y.get(api), 2);
                 }
                 //UtilitiesFunc.WriteLineInFile(ConfigSettings.LOGFILENAME, String.Format("** Similarity of chosenFPslist={0}; max cluster sim ={1}", chosenFPsSimMetric, similarityMetric[dir].Max()));

                 // --- Find common FPs
                 int[] commonFPsIndex = Utilities.intersect(chosenFPsList, matchedClusterMemberIndexList.get(dir));
                 if (commonFPsIndex.length > 0)
                 {
                     useCommonFP = true;

                     //StreamWriter sw = File.AppendText(ConfigSettings.LOGFILENAME);
                     //sw.Write("** Find Common FPs range: ");
                     //for (int c : commonFPsIndex)
                     //{   
                     //    sw.Write("{0}, ", c);
                     //}
                     //sw.WriteLine();
                     //sw.Close();
                 }


                 // --- Create Psi
                 totalnumcol += commonFPsIndex.length;
                 psi_temp.put(dir, LocDB.Psi.get(dir).getMatrix(Utilities.Listint(APSelectionRowIndexFineLoc), commonFPsIndex));
                 for (int fp : commonFPsIndex)
                 {
                     x00thetaIndex_FineLoc.add(LocDB.x00List.get(fp));
                     y00thetaIndex_FineLoc.add(LocDB.y00List.get(fp));
                 }

             }
         
         
             if (!useCommonFP)
             {
                 psi_temp.clear();
                 x00thetaIndex_FineLoc.clear();
                 y00thetaIndex_FineLoc.clear();
                 totalnumcol = 0;

                 for (Orientation dir : chosenDBOrientation)
                 {
                     //UtilitiesFunc.WriteLineInFile(ConfigSettings.LOGFILENAME, "** No common fps found; ");

                     totalnumcol += matchedClusterMemberIndexList.get(dir).length;
                     psi_temp.put(dir, LocDB.Psi.get(dir).getMatrix(Utilities.Listint(APSelectionRowIndexFineLoc), matchedClusterMemberIndexList.get(dir)));

                     //int[] combineIndexList = matchedClusterMemberIndexList[dir].Union(iiIndex[dir]).ToArray();

                     //totalnumcol += combineIndexList.Length;

                     //psi_temp[dir] = LocDB.Psi[dir].GetMatrix(APSelectionRowIndexFineLoc.ToArray(), combineIndexList);
                     //foreach (int fp in combineIndexList)
                     for (int fp : matchedClusterMemberIndexList.get(dir))
                     {
                         x00thetaIndex_FineLoc.add(LocDB.x00List.get(fp));
                         y00thetaIndex_FineLoc.add(LocDB.y00List.get(fp));
                     }
                 }
             }

             int oldI = 0;
             Psi_FineLoc = new Matrix(numrow, totalnumcol);
             for (Orientation dir : chosenDBOrientation)
             {
                 Psi_FineLoc.setMatrix(0, numrow - 1, oldI, oldI + psi_temp.get(dir).getColumnDimension() - 1, psi_temp.get(dir));
                 oldI += psi_temp.get(dir).getColumnDimension();
             }
         }
         else
         {
             // --- No FPs are chosen; use RSS directly
             //UtilitiesFunc.WriteLineInFile(ConfigSettings.LOGFILENAME, "** num of chosen fps = 0; use match cluster method");
             FindMatchedCluster();
             CreatePSI_cluster();
         }

         y_strongAPs = new Matrix(APSelectionRowIndexFineLoc.size(), 1);
         for (int i = 0; i < APSelectionRowIndexFineLoc.size(); i++)
         {
             y_strongAPs.set(i, 0, y.get(APSelectionRowIndexFineLoc.get(i)));
         }

         ComputeQTfromPsi(Psi_FineLoc);

         return useCommonFP;
     }


     
     /*
      * Run L1 norm minimization to find theta hat
      * 
      */

     public static void SelfLocS3_L1NormMin()
     {
         Matrix z;
         //if (useRandomMatrix)
         //{
         //    z = T.Multiply(Phi.Multiply(y_strongAPs));
         //}
         //else
         //{
         //    z = T.Multiply(y_strongAPs);
         //}
         z = T.times(y_strongAPs);
         Matrix xx0 = Q.transpose().times(z);
         theta_hat = CSMath.L1eq_pd(xx0, Q, z);
         
         //StreamWriter sw = File.AppendText(ConfigSettings.LOGFILENAME);
         //sw.Write("Theta_hat=[");
         //for (double tth : theta_hat.ColumnPackedCopy)
         //{
         //    sw.Write("{0} ", tth);
         //}
         //sw.WriteLine("]'");
         //sw.Close();
     }
     
     
     /*
      * Interpret theta hat into actual location estimate
      * @param useOne
      * @return location
      */

     public static Point SelfLocS4_GetEstLoc(boolean useOne)
     {            
         InterpretThetaHat2XY(useOne);
         //UtilitiesFunc.WriteLineInFile(ConfigSettings.LOGFILENAME, "Estimated :(" + calLoc.X.ToString() + "," + calLoc.Y.ToString() + ")");
         return calLoc;
     }
     
     /*
      * Find similiarity between y and clusters in db
      * 
      */
    
     public static void CalculateSimiliarityMetric()
     {
         maxS = -99999999; minS = 0;

         // Compute similarities and find its max/min
         similarityMetric.clear();

         y_strongAPs = new Matrix(APSelectionRowIndexCoaLoc.size(), 1);
         for (int i = 0; i < APSelectionRowIndexCoaLoc.size(); i++)
         {
             y_strongAPs.set(i, 0, y.get(APSelectionRowIndexCoaLoc.get(i)));
         }

         for (Orientation dir : chosenDBOrientation)
         {
        	 double[] similarityMetricValues = new double[LocDB.clusterHeadList.get(dir).size()];

             for (int i = 0; i < LocDB.clusterHeadList.get(dir).size(); i++)
             {
            	 similarityMetricValues[i] = 0.0;
                 double totalwt = 0;
                 double wt = 0;

                 for (int j = 0; j < y_strongAPs.getRowDimension(); j++)
                 {
                     switch (ConfigSettings.CLUSTER_MATCH_METRIC_CHOICE)
                     {
                         case 2:
                             if (LocDB.clusterVarRSS.get(dir).get(APSelectionRowIndexCoaLoc.get(j), i) > 0)
                             {
                                 wt = 1 / LocDB.clusterVarRSS.get(dir).get(APSelectionRowIndexCoaLoc.get(j), i);
                             }
                             else
                             {
                                 wt = 0;
                             }
                             totalwt += wt;
                             
                             similarityMetricValues[i] -= wt * Math.pow(LocDB.clusterAvgRSS.get(dir).get(APSelectionRowIndexCoaLoc.get(j), i) - y_strongAPs.get(j, 0), 2);
                             break;
                         case 1:
                        	 similarityMetricValues[i] -= Math.pow(LocDB.clusterAvgRSS.get(dir).get(APSelectionRowIndexCoaLoc.get(j), i) - y_strongAPs.get(j, 0), 2);
                             break;
                         default:
                        	 List<Integer> keys = new ArrayList<Integer>(LocDB.clusterHeadList.get(dir).keySet());
                        	 similarityMetricValues[i] -= Math.pow(LocDB.Psi.get(dir).get(APSelectionRowIndexCoaLoc.get(j), keys.get(i)) - y_strongAPs.get(j, 0), 2);
                             break;
                     }
                 }

                 if (ConfigSettings.CLUSTER_MATCH_METRIC_CHOICE == 2)
                 {
                	 similarityMetricValues[i] = similarityMetricValues[i] / totalwt;
                 }
             }

             maxS = Math.max(maxS, Utilities.doubleMax(similarityMetricValues));
             minS = Math.min(minS, Utilities.doubleMin(similarityMetricValues));
             
             similarityMetric.put(dir, similarityMetricValues);
         }
     }
 
        
     private static void ObtainDesiredDB(int headingangle)
     {
         if (use4OrientationDB && !use4OforFineLoc)
         {
             chosenDBOrientation.clear();

             if (headingangle > 45 && headingangle <= 135)
             {
                 chosenDBOrientation.add(Orientation.East);
             }
             else if (headingangle > 135 && headingangle <= 225)
             {
                 chosenDBOrientation.add(Orientation.South);
             }
             else if (headingangle > 225 && headingangle <= 315)
             {
                 chosenDBOrientation.add(Orientation.West);
             }
             else
             {
                 // heading < 45 || heading > 315 || heading == -1 (invalid value)
                 chosenDBOrientation.add(Orientation.North);
             }

             //UtilitiesFunc.WriteLineInFile(ConfigSettings.LOGFILENAME, "DB: " + chosenDBOrientation.Last().ToString("G"));
         }
     }

     
     private static void FindMatchedCluster()
     {
         CalculateSimiliarityMetric();

         HashMap<Integer, Double> matchedClusterS = new HashMap<Integer, Double>();
         HashMap<Integer, Double> sortedCluster = new HashMap<Integer, Double>();
         for (Orientation dir : chosenDBOrientation)
         {
             matchedClusterS.clear();
             sortedCluster.clear();
             double[] similarityMetricValues = similarityMetric.get(dir);
             List<Integer> keys = new ArrayList<Integer>(LocDB.clusterHeadList.get(dir).keySet());
             keys = Utilities.BubbleSort(keys);
             for (int i = 0; i < LocDB.clusterHeadList.get(dir).size(); i++)
             {
                 if (similarityMetricValues[i] - minS > ConfigSettings.CLUSTER_MATHCHED_THRESHOLD_PERC* (maxS - minS))
                 {
                     matchedClusterS.put(keys.get(i), similarityMetricValues[i]);
                 }
             }

             int size = matchedClusterS.size();
             if (size > ConfigSettings.MAX_MATCHED_NUM_CLUSTERS)
             {
                 size = ConfigSettings.MAX_MATCHED_NUM_CLUSTERS;
             }

             matchedIndexInClusterHeadList.put(dir, new int[size]);
             sortedCluster = Utilities.sortHashMap(matchedClusterS);
             List<Integer> mapKeys = new ArrayList<Integer>();
             mapKeys.addAll(sortedCluster.keySet()) ;
            
             matchedIndexInClusterHeadList.put(dir, Utilities.Listint(mapKeys.subList(0, size)));

             List<Integer> matchedIndexList = new ArrayList<Integer>();

             //StreamWriter sww = File.AppendText(ConfigSettings.LOGFILENAME);
             //sww.WriteLine("** Chosen cluster member index ({0}): ", dir);

             for (int k : matchedIndexInClusterHeadList.get(dir))
             {
             //    sww.Write("  Cluster Head <{0}>: ", k);
             //    foreach (int m in LocDB.clusterHeadList[dir][k])
             //    {
             //        sww.Write("{0}, ", m);
             //    }
                 matchedIndexList.addAll(LocDB.clusterHeadList.get(dir).get(k));
             }
             matchedClusterMemberIndexList.put(dir, Utilities.Listint(matchedIndexList));

             //sww.Close();

             //int ii;
             //int[] iis = new int[matchedIndexInClusterHeadList[dir].Length];
             //int[] iie = new int[matchedIndexInClusterHeadList[dir].Length];
             //int iisize = 0;

             //for (int i = 0; i < matchedIndexInClusterHeadList[dir].Length; i++)
             //{
             //    ii = matchedIndexInClusterHeadList[dir][i];
             //    iis[i] = LocDB.clusterHeadList[dir][ii];
             //    if (ii + 1 < LocDB.clusterHeadList[dir].Count)
             //    {
             //        iie[i] = LocDB.clusterHeadList[dir][ii + 1] - 1;
             //    }
             //    else
             //    {
             //        iie[i] = LocDB.numRPs - 1;
             //    }
             //    iisize += iie[i] - iis[i] + 1;
             //}

             //matchedClusterMemberIndexList[dir] = new int[iisize];

             //int iii = 0;

             //StreamWriter sww = File.AppendText(ConfigSettings.LOGFILENAME);
             //sww.WriteLine("** Chosen cluster member index ({0}): ", dir);
             //for (int m = 0; m < matchedIndexInClusterHeadList[dir].Length; m++)
             //{
             //    for (int p = iis[m]; p <= iie[m]; p++)
             //    {
             //        matchedClusterMemberIndexList[dir][iii] = p;
             //        sww.Write("{0} ({1}), ", LocDB.refptsList.IndexOf(LocDB.x00List[dir][p] * 100000 + LocDB.y00List[dir][p]), p);
             //        iii++;
             //    }

             //}
             //sww.WriteLine(" - # clusters = {0}", matchedIndexInClusterHeadList[dir].Length);
             //sww.Close();
             //}

             //StreamWriter sw = File.AppendText(ConfigSettings.LOGFILENAME);
             //sw.Write("Matched cluster heads: ");

             //foreach (Orientation dir in chosenDBOrientation)
             //{
             //    for (int i = 0; i < matchedIndexInClusterHeadList[dir].Length; i++)
             //    {
             //        int x = LocDB.x00List[dir][LocDB.clusterHeadList[dir][matchedIndexInClusterHeadList[dir][i]]];
             //        int y = LocDB.y00List[dir][LocDB.clusterHeadList[dir][matchedIndexInClusterHeadList[dir][i]]];
             //        sw.Write("{0} ({1}, {2}) {3},  ", LocDB.refptsList.IndexOf(x * 100000 + y) + 1, x, y, LocDB.clusterHeadList[dir][matchedIndexInClusterHeadList[dir][i]]);
             //    }
             //}
             //sw.WriteLine();
             //sw.Close();
         }
     }
     
     /// <summary>
     /// 
     /// </summary>
     private static void CreatePSI_cluster()
     {
         HashMap<Orientation, Matrix> psi_temp = new HashMap<Orientation, Matrix>();
         x00thetaIndex_FineLoc.clear();
         y00thetaIndex_FineLoc.clear();

         int numrow = APSelectionRowIndexFineLoc.size();
         int totalnumcol = 0;
         for (Orientation dir : chosenDBOrientation)
         {
             int numcol = matchedClusterMemberIndexList.get(dir).length;
             totalnumcol += numcol;
             psi_temp.put(dir, LocDB.Psi.get(dir).getMatrix(Utilities.Listint(APSelectionRowIndexFineLoc),matchedClusterMemberIndexList.get(dir)));
             
             for (int index : matchedClusterMemberIndexList.get(dir))
             {
                 x00thetaIndex_FineLoc.add(LocDB.x00List.get(index));
                 y00thetaIndex_FineLoc.add(LocDB.y00List.get(index));
             }
         }

         int oldI = 0;
         Psi_FineLoc = new Matrix(numrow, totalnumcol);
         for (Orientation dir : chosenDBOrientation)
         {
             Psi_FineLoc.setMatrix(0, numrow - 1, oldI, oldI + psi_temp.get(dir).getColumnDimension() - 1, psi_temp.get(dir));
             oldI += psi_temp.get(dir).getColumnDimension();
         }
     }

     /// <summary>
     /// Compute Q and T from PSI
     /// R = PSI or PHI*PSI
     /// Q = orth(R_t)_t
     /// T = Q pserinv(R)
     /// </summary>
     /// <param name="mypsi"></param>
     private static void ComputeQTfromPsi(Matrix mypsi)
     {
         //if (useRandomMatrix)
         //{
         //    Phi = Matrix.Random(nn_randomMatrix, mypsi.RowDimension);
         //    R = Phi.Multiply(mypsi);
         //}
         //else
         //{
         //    R = mypsi.Copy();
         //}
         R = mypsi.copy();
         Q = CSMath.Orthogonalization(R.transpose()).transpose();
         T = Q.times(CSMath.Pseudoinverse(R));
     }

     /// <summary>
     /// 
     /// </summary>
     /// <returns></returns>
     private static void InterpretThetaHat2XY(boolean useOne)
     {
         double[] xxarray = theta_hat.getColumnPackedCopy();
         List<Double> xxarrayList = new ArrayList<Double>();
         xxarrayList = Utilities.doubleList(xxarray);
         //List<Double> xxarray = Utilities.theta_hat.getColumnPackedCopy();
         // only choose the highest entry 
         if (useOne)
         {
             double max = Utilities.doubleMax(xxarray);
             int index = xxarrayList.indexOf(max);
             	
             calLoc = new Point(x00thetaIndex_FineLoc.get(index), y00thetaIndex_FineLoc.get(index));

         }
         // consider only higher entries as well (normal operation)
         else
         {
             double threshold = ConfigSettings.THETA_HAT_THRESHOLD_PERCENTAGE * Utilities.doubleMax(xxarray);

             double ax = 0;
             double ay = 0;
             double totalwt = 0;

             //StreamWriter sw = File.AppendText(ConfigSettings.LOGFILENAME);
             //sw.Write("Chosen indices from theta_h: ");
             for (int i = 0; i < xxarray.length; i++)
             {
                 if (xxarray[i] >= threshold)
                 {
                     //sw.Write("{0} ", i);
                     ax += xxarray[i] * x00thetaIndex_FineLoc.get(i);
                     ay += xxarray[i] * y00thetaIndex_FineLoc.get(i);
                     totalwt += xxarray[i];
                 }
             }
             //sw.WriteLine();
             //sw.Close();
             ax = ax / totalwt;
             ay = ay / totalwt;

             calLoc = new Point((int)Math.round(ax), (int)Math.round(ay));
         }
     }
     

     /// <summary>
     /// Use the heading angle to determine which database should be chosen (when use4OforFineLoc is false)
     /// </summary>
     /// <param name="headingangle"></param>
     /// <returns></returns>
     public static List<Orientation> WhichDB(int headingangle)
     {
         ObtainDesiredDB(headingangle); 
         return chosenDBOrientation;
     }

 
}
