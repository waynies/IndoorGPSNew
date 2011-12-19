package com.IndoorGPS;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.TreeSet;

import com.IndoorGPS.LocalizerBasicClass.ConfigSettings;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Environment;
import android.util.Log;

public class Utilities
{
	private static final String TAG = "Utilities->";
	
	private static AlertDialog alertDialog;
	
	private static File scanResultFile;
	private static FileWriter resultWriter;
	private static BufferedWriter out;
	
    //**************************************************************
	//		initUtil
	//
	//@brief: initialise utilities
	//**************************************************************
	public static void initUtil(Context parentContext)
	{
		alertDialog = new AlertDialog.Builder(parentContext).create();
		
		// Initialise files
		try
		{
		    File root = Environment.getExternalStorageDirectory();
		    
		    if(root.canWrite() == true)
		    {
		        scanResultFile = new File(root, "result.txt");
		        resultWriter = new FileWriter(scanResultFile);
		        out = new BufferedWriter(resultWriter);
		    }
		    else
		    {
		    	Log.d("on create MapActivity", "unable to write to root");
		    }
		}
		catch(IOException e)
		{
		    Log.e(TAG, "Could not write file " + e.getMessage());
		}
	}
	
    //**************************************************************
	//		ReadRSSnWriteDB
	//
	//
	//**************************************************************
	public static void closeFile()
	{
		try {
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
    //**************************************************************
	//		ReadRSSnWriteDB
	//
	//
	//**************************************************************
	public static void writeFile(String input)
	{
		try {
			out.write(input);
			out.newLine();
		} catch (IOException e) {
			Log.e(TAG, "Could not write file " + e.getMessage());
		}
	}
	
    //**************************************************************
	//		displayMsgBox
	//
	//@brief: used to display message only. not to be used to handle
	//	user inputs
	//**************************************************************
	public static void displayMsgBox(String title, String msg)
	{	
		alertDialog.setTitle(title);
		alertDialog.setMessage(msg);
		
		alertDialog.setButton("OK", new DialogInterface.OnClickListener(){
			public void onClick(DialogInterface dialog, int which)
			{
				// empty
			}
		});

		alertDialog.setIcon(R.drawable.icon_info);
		alertDialog.show();
	}
	
    //**************************************************************
	//		Check if the file exists or not
	//
	//
	//**************************************************************
	public static boolean IsValidFilePath(String filepath){
		File myfile = new File(filepath);
		boolean fileexists = myfile.exists();
		//System.out.println(myfile + (myfile.exists()? " is found " : " is missing "));

		return (filepath != null && myfile.exists());
	}
	
	// @brief: take average for valide Online y, in the order of given macIDList
	public static HashMap<Integer, Double> RearrangeOnlineReading(HashMap<String, List<Integer>> observedRSS, List<String> macIDList)
	{
		//IsValidRSSReading = false;
		HashMap<Integer, Double> onliney = new HashMap<Integer, Double>();
		double avg = 0.0;
		Iterator<String> keyIt = macIDList.iterator();
		int j = 0;
		while(keyIt.hasNext()){			
			String name = keyIt.next();
			List<Integer> levelList = new ArrayList<Integer>();
			levelList = observedRSS.get(name);
			// calculate average
			//if(!levelList.isEmpty())
			if(levelList == null)
			{
				avg = ConfigSettings.RSS_DEFAULT_VALUE;
			}
			else
			{
				//IsValidRSSReading = true;
				double sum = 0;
				int tol = 0;
				for (Integer i : levelList) {
					if(i.intValue() != ConfigSettings.RSS_DEFAULT_VALUE)
					{
						sum += i.intValue();
						tol++;
					}
				}
				if(tol != 0){
					avg = sum / tol;
				}
				else{
					avg = ConfigSettings.RSS_DEFAULT_VALUE;
				}
			}			
			onliney.put(j, avg);
			j++;
		}
		
		return onliney;
		
	}
	
	public static boolean IsValidRSSReading = true;
	
	
	// Descending order
	public static HashMap<Integer, Double> sortHashMap(HashMap<Integer, Double> input)
	{
	    HashMap<Integer, Double> tempMap = new HashMap<Integer, Double>();
	    for (Integer wsState : input.keySet()){
	        tempMap.put(wsState,input.get(wsState));
	    }

	    List<Integer> mapKeys = new ArrayList<Integer>(tempMap.keySet());
	    List<Double> mapValues = new ArrayList<Double>(tempMap.values());
	    HashMap<Integer, Double> sortedMap = new LinkedHashMap<Integer, Double>();
	    TreeSet<Double> sortedSet = new TreeSet<Double>(mapValues);
	    Object[] sortedArray = sortedSet.toArray();
	    int size = sortedArray.length;
	    for (int i=size-1; i>=0; i--){
	        sortedMap.put(mapKeys.get(mapValues.indexOf(sortedArray[i])), 
	                      (Double)sortedArray[i]);
	    }
	    
	    return sortedMap;
	}
	
	// Ascending order
	public static HashMap<Integer, Double> sortHashMap_A(HashMap<Integer, Double> input)
	{
	    HashMap<Integer, Double> tempMap = new HashMap<Integer, Double>();
	    for (Integer wsState : input.keySet()){
	        tempMap.put(wsState,input.get(wsState));
	    }

	    List<Integer> mapKeys = new ArrayList<Integer>(tempMap.keySet());
	    List<Double> mapValues = new ArrayList<Double>(tempMap.values());
	    HashMap<Integer, Double> sortedMap = new LinkedHashMap<Integer, Double>();
	    TreeSet<Double> sortedSet = new TreeSet<Double>(mapValues);
	    Object[] sortedArray = sortedSet.toArray();
	    int size = sortedArray.length;
	    for (int i=0; i< size; i++){
	        sortedMap.put(mapKeys.get(mapValues.indexOf(sortedArray[i])), 
	                      (Double)sortedArray[i]);
	    }
	    
	    return sortedMap;
	}
	
	public static List<Integer> intList(int[] input){
		List<Integer> intList0 = new ArrayList<Integer>();  
		for(int i = 0; i < input.length; i++)
		{
			intList0.add(input[i]);
		}
		return intList0;
	}
	
	public static int[] Listint(List<Integer> input){
		int[] Listint0 = new int[input.size()];  
		for(int i = 0; i < input.size(); i++)
		{
			Listint0[i] = input.get(i);
		}
		return Listint0;
	}

	
	public static List<Double> doubleList(double[] input){
		List<Double> intList0 = new ArrayList<Double>();  
		for(int i = 0; i < input.length; i++)
		{
			intList0.add(input[i]);
		}
		return intList0;
	}
	
	
	public static Double doubleMax(double[] input){
		Double maxNum = input[0];
		for(int i = 1; i < input.length; i++)
		{
			if(input[i] > maxNum)
			{
				maxNum = input[i];
			}
		}
		return maxNum;
	}
	
	public static Double doubleMin(double[] input){
		Double minNum = input[0];
		for(int i = 1; i < input.length; i++)
		{
			if(input[i] < minNum)
			{
				minNum = input[i];
			}
		}
		return minNum;
	}

	public static List<Integer> uniqueArray(List<Integer> input) 
	{
		List<Integer> output = new ArrayList<Integer>();
		output.add(input.get(0));
		for( int i = 1; i < input.size(); i++)
		{
			boolean unique_i = true;
			for( int j = 0; j < i; j++)
			{
				if(input.get(i) == input.get(j))
				{
					unique_i = false;
					break;
				}
			}
			if(unique_i == true)
			{
				output.add(input.get(i));
			}
		}
		return output;
		
	}

	public static double intDouble(int x) {
		// TODO Auto-generated method stub
		return (x + 0.0);
	}

	public static double doubleAve(double[] rowPackedCopy) {
		// TODO Auto-generated method stub
		double ave = 0.0;
		for (int i = 0; i < rowPackedCopy.length; i++)
		{
			ave += rowPackedCopy[i];
		}
		ave = ave/rowPackedCopy.length;
		return ave;
	}


	public static int[] intersect(List<Integer> arg1, int[] arg2) {
		// TODO Auto-generated method stub
		List<Integer> common = new ArrayList<Integer>();
		for(int i = 0; i < arg2.length; i++){
			if(arg1.contains(arg2[i]))
			{
				common.add(arg2[i]);
			}
		}
		return Listint(common);
	}

	public static List<Integer> inListint(int i) {
		// TODO Auto-generated method stub
		List<Integer> a = new ArrayList<Integer>();
		a.add(i);
		return a;
	}
	
	public static List<Integer> sortKeys(List<Integer> input)
	{
		int [] input1 = Utilities.Listint(input);
		int j = input1.length;
		int temp;
		for(int i = 0; i < j; i++)
		{
			for(int k = 0; k < j; k++)
			{
				if(input1[k+1] < input1[k])
				{
					temp = input1[k];
					input1[k] = input1[k+1];
					input1[k+1] = temp;
				}
			}
			j--;
		}
		return Utilities.intList(input1);
	}
	
	public static List<Integer> BubbleSort(List<Integer> l) {
		List<Integer> sortedList = new ArrayList<Integer>();
		List<Integer> fullList = new ArrayList<Integer>(l);
		while (sortedList.size() < fullList.size()) {
			int lowest = l.get(0);
			for (int next : l) {
				if (lowest > next) {
					lowest = next;

					//debug code
					//System.out.println(highest);
				}
			}
			sortedList.add(lowest);

			//debug code
			//System.out.println(sortedList + "\n" + l);

			l.remove(l.indexOf(lowest));
		}

		//debug code
		//System.out.println(inOrder(sortedList));
		//System.out.println("Full List: " + fullList);

		return sortedList;
	}


	



}