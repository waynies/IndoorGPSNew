/*
 * Obtain FPDB class
 * */
package com.IndoorGPS.LocalizerBasicClass;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import Jama.Matrix;
import android.graphics.Point;
import android.net.wifi.ScanResult;
import android.util.Log;

import com.IndoorGPS.DBManager;
import com.IndoorGPS.Utilities;
import com.IndoorGPS.MathAlgorithm.APCluster;

public class ObtainFPDB {
	private static final String TAG = "ObtainFPDB -> ";
	private static final int RSS_DEFAULT_VALUE = -110;

	private static double[][] Psi;
	private static boolean isValidPsi = false;
	private static List<String> apMacIDList = new ArrayList<String>();

	// Finger prints
	public static boolean needClustering = false;
	public static double clusterPreference;
	public static List<Point> refPtList = new ArrayList<Point>();
	public static List<Integer> clusterList = new ArrayList<Integer>();
	public static List<Integer> clusterHeadList = new ArrayList<Integer>();

    
	// **************************************************************
	// appendScanResults
	//
	// @brief: accumulate scan results for further use
	// **************************************************************
	public static HashMap<String, List<Integer>> appendScanResults(List<ScanResult> inputList,
			HashMap<String, List<Integer>> accumulatedMap) 
	{	
			for (int i = 0; i < inputList.size(); i++) 
			{
				String name = inputList.get(i).BSSID;
				int level = inputList.get(i).level;

				if (accumulatedMap.containsKey(name)) {
					// found an existing key
					List<Integer> list = accumulatedMap.get(name);

					if (list != null) {
						list.add(new Integer(level));
					} else {
						Log.d(TAG, "error: list is null");
					}
				} 
				else 
				{
					// add new key
					List<Integer> list = new ArrayList<Integer>();
					list.add(new Integer(level));
					accumulatedMap.put(name, list);
				}
			}
			return accumulatedMap;
	}

	// **************************************************************
	// postProcessScanResults
	//
	// @brief: fill in blank spaces in the list, take the average and variance
	// of
	// all samples (condense accumulatedMap data to outputMap)
	// **************************************************************
	public static void postProcessScanResults(
			HashMap<String, List<Integer>> accumulatedMap, int sampleCount,
			HashMap<String, double[]> outputMap) {
		double[] avgVarPair = { 0.0, 0.0 };

		Iterator<String> keyIt = accumulatedMap.keySet().iterator();
		while (keyIt.hasNext()) {
			String name = keyIt.next();
			String valueString = name + "\t";
			List<Integer> levelList = accumulatedMap.get(name);

			// fill in empty slots with -110 dBm
			for (int i = levelList.size(); i < sampleCount; i++) {
				levelList.add(new Integer(RSS_DEFAULT_VALUE));
			}

			// calculate average
			double sum = 0;
			for (Integer i : levelList) {
				sum += i.intValue();
			}
			avgVarPair[0] = sum / levelList.size();

			// calculate variance
			sum = 0;
			for (Integer i : levelList) {
				sum += Math.pow(i.intValue() - (int) avgVarPair[0], 2);
			}
			avgVarPair[1] = sum / levelList.size();
            double[] newAveVarPair = {avgVarPair[0],avgVarPair[1]};
			outputMap.put(name, newAveVarPair);

			// write to file for debugging
			for (Integer i : levelList) {
				valueString += i + "\t";
			}

			valueString += "Average " + (int) avgVarPair[0] + "\tVariance "
					+ (int) avgVarPair[1] + "\n";
			Log.d(TAG, valueString);
			Utilities.writeFile(valueString);
		}
	}


	// **************************************************************
	// storeScanResults
	//
	// @brief: stores list to database
	// **************************************************************
	public static void storeScanResults(String table, Point position,
			HashMap<String, double[]> inputList, DBManager dbManager) {
		// write averageMap to database
		// selectAPs(wifiList, accumulatedWifiList, MAX_AP_NUM);
		dbManager.insertRows(table, inputList, // from list
				position.x, // x coordinate on map
				position.y, // y coordinate on map
				0 // iteration number
				);
	}

	// **************************************************************
	// isExistingPoint
	//
	//
	// **************************************************************
	public static boolean isExistingPoint(int ptx, int pty) {
		return false;
	}

	// **************************************************************
	// ProcessRSSinDB
	//
	// @brief: called when scanning is finished in function
	// Calculate the average RSS values from 4 orientations
	// as the overall rss for the AP. if 4o not enabled, do nothing
	// **************************************************************
	public static void processRSSinDB(DBManager dbManager, Point currentPt,
			boolean is4O) {
		// Add current point to reference point list and cluster list
		refPtList.add(currentPt);
		clusterList.add(-1);

		needClustering = true;

		// write (x, y, -1) to refptfile
		// UtilitiesFunc.WriteLineInFile(refptFile, currentX.ToString() + "," +
		// currentY.ToString() + ",-1");

		// Initialise a column of Psi (the last column in the psi matrix)
		if (apMacIDList.size() > 0) {
			for (int i = 0; i < apMacIDList.size(); i++) {
				Psi[i][refPtList.size() - 1] = RSS_DEFAULT_VALUE;
			}
		}

		// get a list of MAC for a specific ref point from database
		// refptNode = xmldbDoc.DocumentElement["building-" + building]["floor-"
		// + floor]["refpt-" + XYpt.ToString()];

		// Update the overall RSS in database for current point, Do nothing if
		// 4O is false
		if (is4O) {
			HashMap<String, Double> RSSVector_N = new HashMap<String, Double>();
			HashMap<String, Double> RSSVector_E = new HashMap<String, Double>();
			HashMap<String, Double> RSSVector_S = new HashMap<String, Double>();
			HashMap<String, Double> RSSVector_W = new HashMap<String, Double>();
			HashMap<String, Double> consolidatedRSSVector = new HashMap<String, Double>();

			RSSVector_N = dbManager.getRSSList(DBManager.DATABASE_TABLE_N,
					currentPt);
			RSSVector_E = dbManager.getRSSList(DBManager.DATABASE_TABLE_E,
					currentPt);
			RSSVector_S = dbManager.getRSSList(DBManager.DATABASE_TABLE_S,
					currentPt);
			RSSVector_W = dbManager.getRSSList(DBManager.DATABASE_TABLE_W,
					currentPt);

			// extract a list of MAC from all four orientations
			extractMac(consolidatedRSSVector, RSSVector_N);
			extractMac(consolidatedRSSVector, RSSVector_E);
			extractMac(consolidatedRSSVector, RSSVector_S);
			extractMac(consolidatedRSSVector, RSSVector_W);

			Iterator<String> it = consolidatedRSSVector.keySet().iterator();

			while (it.hasNext()) {
				String name = it.next().toString();
				double newVal = 0.0;

				// north
				if (RSSVector_N.containsKey(name)) {
					newVal = RSSVector_N.get(name)
							+ consolidatedRSSVector.get(name);
				} else {
					newVal = RSS_DEFAULT_VALUE
							+ consolidatedRSSVector.get(name);
				}
				consolidatedRSSVector.put(name, newVal);

				// east
				if (RSSVector_E.containsKey(name)) {
					newVal = RSSVector_N.get(name)
							+ consolidatedRSSVector.get(name);
				} else {
					newVal = RSS_DEFAULT_VALUE
							+ consolidatedRSSVector.get(name);
				}
				consolidatedRSSVector.put(name, newVal);

				// south
				if (RSSVector_S.containsKey(name)) {
					newVal = RSSVector_N.get(name)
							+ consolidatedRSSVector.get(name);
				} else {
					newVal = RSS_DEFAULT_VALUE
							+ consolidatedRSSVector.get(name);
				}
				consolidatedRSSVector.put(name, newVal);

				// west
				if (RSSVector_W.containsKey(name)) {
					newVal = RSSVector_N.get(name)
							+ consolidatedRSSVector.get(name);
				} else {
					newVal = RSS_DEFAULT_VALUE
							+ consolidatedRSSVector.get(name);
				}
				consolidatedRSSVector.put(name, newVal);

				// take the average
				newVal = consolidatedRSSVector.get(name) / 4;
				consolidatedRSSVector.put(name, newVal);
			}

			// store the consolidated map to DATABASE_TABLE without orientations
			dbManager.insertRows(DBManager.DATABASE_TABLE,
					consolidatedRSSVector, currentPt.x, currentPt.y, 0, 0);
		}

		/*
		 * apnode.SetAttribute("rss", average.ToString());
		 * 
		 * // change the psi if(apMacIDList.Count > 0) { if
		 * (apMacIDList.Contains(apnode.Name) == true) {
		 * Psi[apMacIDList.IndexOf(apnode.Name)][RefPtsList.Count - 1] =
		 * average; isValidPsi = true; } }
		 */
	}

	// **************************************************************
	// generateCluster
	//
	//
	// **************************************************************
	public static int generateCluster() {
		// // No need to do clustering
		/*
		 * if (needClustering == false) { return 1; }
		 */

		needClustering = false;

		if (apMacIDList.size() == 0) {
			return -1;
		} else if (isValidPsi == false) {
			return -2;
		}

		// Psi is already created.

		// Generate similarity metric for Psi
		List<Double> ss = new ArrayList<Double>();
		Matrix s = new Matrix(refPtList.size() * (refPtList.size() - 1), 3);
		int jj = 0;
		for (int i = 0; i < refPtList.size(); i++) {
			for (int k = 0; k < refPtList.size(); k++) {
				if (k != i) {
					s.set(jj, 0, i);
					s.set(jj, 1, k);

					double sum = 0;
					for (int m = 0; m < apMacIDList.size(); m++) {
						sum -= Math.pow(Psi[m][i] - Psi[m][k], 2);
					}
					s.set(jj, 2, sum);
					ss.add(sum);

					jj++;
				}
			}
		}

		// Get median similarity
		double pp;
		// ss.Sort();
		Collections.sort(ss);

		if (ss.size() % 2 == 1) {
			pp = ss.get(ss.size() / 2);
		} else {
			pp = (ss.get(ss.size() / 2 - 1) + ss.get(ss.size() / 2)) / 2;
		}

		clusterPreference = pp;
		Matrix p = new Matrix(refPtList.size(), 1,
				ConfigSettings.CLUSTER_PREF_FRAC * pp);

		// Run APCluster
		APCluster apCluster = new APCluster(s, p);

		clusterList.clear();
		for (int ii = 0; ii < refPtList.size(); ii++) {
			clusterList.add(apCluster.c[ii]);
		}

		clusterHeadList.clear();
		for (int ii = 0; ii < apCluster.Iexemplars.length; ii++) {
			clusterHeadList.add(apCluster.Iexemplars[ii]);
		}

		// Add cluster info to refptfile and xmldb
		/*
		 * StreamWriter sw = File.CreateText(refptFile); for (int ii = 0; ii <
		 * refPtList.size(); ii++) { int x = refPtList[ii] / 100000; int y =
		 * refPtList[ii] % 100000; sw.WriteLine(x + "," + y + "," +
		 * apCluster.idx[ii].ToString());
		 * 
		 * if (ConfigSettings.LOAD_FP_XMLDB) {
		 * xmldbDoc.DocumentElement["building-" + building]["floor-" +
		 * floor]["refpt-" +
		 * RefPtsList[ii].ToString()].SetAttribute("clustergroup",
		 * apCluster.idx[ii].ToString()); } } sw.Close();
		 * 
		 * if (ConfigSettings.LOAD_FP_XMLDB) { xmldbDoc.Save(xmldbFile); }
		 */

		return 0;
	}

	// **************************************************************
	// CreateClusterPsiFile
	//
	//
	// **************************************************************
	public static void CreateClusterPsiFile() {

	}

	// **************************************************************
	// CreateNonClusterPsiFile
	//
	//
	// **************************************************************
	public static void CreateNonClusterPsiFile() {
	}

	// **************************************************************
	// CreateNonClusterMultiplePsiFile
	//
	//
	// **************************************************************
	public static void CreateNonClusterMultiplePsiFile() {
	}

	// **************************************************************
	// CreateClusterMultiplePsiFile
	//
	//
	// **************************************************************
	public static void CreateClusterMultiplePsiFile() {
	}

	// **************************************************************
	// extractMac
	//
	//
	// **************************************************************
	private static void extractMac(HashMap<String, Double> targetMap,
			HashMap<String, Double> fromMap) {
		Iterator<String> it = fromMap.keySet().iterator();

		// merge
		while (it.hasNext()) {
			String macID = it.next().toString();

			if (!targetMap.containsKey(macID)) {
				// MAC ID already exists in the target map
				targetMap.put(macID, (double) 0);
			}
		}
	}
}