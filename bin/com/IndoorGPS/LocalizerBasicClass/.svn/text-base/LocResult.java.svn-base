package com.IndoorGPS.LocalizerBasicClass;

import java.util.ArrayList;
import java.util.List;

import android.graphics.Point;

public class LocResult {
	
    public static List<Point> SmoothedNavigPositions = new ArrayList<Point>();
    public static List<Point> EstimatedPositions = new ArrayList<Point>();
    public static List<Integer> InterpretedUserDirections = new ArrayList<Integer>();
    
    public static ArrayList<Point> ComputedPositionMeas = new ArrayList<Point>();
    public static List<Point> ActualPositions = new ArrayList<Point>();
    
    public static List<Double> UpdatesInterval = new ArrayList<Double>();
    
    public static List<Integer> CompassHeadingAngle = new ArrayList<Integer>();
    
    public static List<Double> DistErrorList = new ArrayList<Double>();

    public static List<Integer> DetectedTurningPoints = new ArrayList<Integer>();

    public static void ResetPositionEst()
    {
        EstimatedPositions.clear();
        ComputedPositionMeas.clear();
        UpdatesInterval.clear();
        CompassHeadingAngle.clear();
        ActualPositions.clear();
        DetectedTurningPoints.clear();
        SmoothedNavigPositions.clear();
        InterpretedUserDirections.clear();
    }

}
