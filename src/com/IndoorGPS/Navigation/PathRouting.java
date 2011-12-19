package com.IndoorGPS.Navigation;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.IndoorGPS.Utilities;
import com.IndoorGPS.LocalizerBasicClass.ConfigSettings;
import com.IndoorGPS.LocalizerBasicClass.GeometryFunc;
import com.IndoorGPS.LocalizerBasicClass.MapInfo;
import com.IndoorGPS.MathAlgorithm.Dijkstra;

import android.graphics.Point;
import android.util.Log;

import Jama.Matrix;

public class PathRouting {
	
	private static String weightMatrixFile = null;
    
    private static Matrix pathWeight;
    private static Dijkstra dijkstraAlg;

    public static List<Point> RoutedPath = new ArrayList<Point>();
    public static List<Integer> RountedPathIndices;

    private static int destinationIndex = -1;

    public static String DestinationPlace = null;
    public static Point DestinationLoc;

    private static List<Integer> cloestNodes2SrcIndex = new ArrayList<Integer>();
    private static List<Double> cloestNodes2SrcWeight = new ArrayList<Double>();

    private static Point source;

    public static int RoutingSetup(String mybuilding, String myfloor) throws Exception
    {
        weightMatrixFile = String.format("%s/%s%s_weightMatrix.txt", ConfigSettings.NAV_SUBDIR, mybuilding, myfloor);
        GenerateWeightMatrix();

        dijkstraAlg = new Dijkstra(pathWeight);

        return 0;
    }
    
    public static Point SetDestination(String dest)
    {
        destinationIndex = MapInfo.Destinations.get(dest);
        DestinationPlace = dest;
        DestinationLoc = MapInfo.LandMarkCoordList.get(destinationIndex);
        return DestinationLoc;
    }
    
    public static boolean FindPath(Point mysrc) throws Exception
    {
        RoutedPath.clear();
        source = mysrc;

        int startIndex = MapInfo.LandMarkCoordList.indexOf(mysrc);
        
        if (startIndex != -1)
        {
            RountedPathIndices = dijkstraAlg.FindShortestPath(startIndex, destinationIndex);
        }
        else
        {
            FindCloestNodes2Source();
            RountedPathIndices = dijkstraAlg.FindShortestPath(cloestNodes2SrcIndex, cloestNodes2SrcWeight, destinationIndex);
        }

        if (RountedPathIndices.size() > 1)
        {
            ConvertPathNodeIndex2Coord();
            return true;
        }
        else
        {
            return false;
        }

    }

    /****************************************************************************************************************
     * Private Functions
     * @throws IOException 
     * *************************************************************************************************************/
    private static void GenerateWeightMatrix() throws IOException
    {
        pathWeight = new Matrix(MapInfo.LandMarkCoordList.size(), MapInfo.LandMarkCoordList.size(), ConfigSettings.INF);

        if (MapInfo.LandMarkConnectionList.size() == MapInfo.LandMarkCoordList.size())
        {
            for (int i = 0; i < MapInfo.LandMarkCoordList.size(); i++)
            {
                for (int j : MapInfo.LandMarkConnectionList.get(i))
                {
                    pathWeight.set(i, j, GeometryFunc.EuclideanDistanceInMeter(MapInfo.LandMarkCoordList.get(i), MapInfo.LandMarkCoordList.get(j)));
                }
            }

        }
        else
        {
            for (int i = 0; i < MapInfo.LandMarkCoordList.size(); i++)
            {
                for (int j = i; j < MapInfo.LandMarkCoordList.size(); j++)
                {
                    double weight;

                    // Both nodes are destination - no path between the two - INF
                    if (MapInfo.Destinations.values().contains(i) && MapInfo.Destinations.values().contains(j))
                    {
                        weight = ConfigSettings.INF;
                    }
                    // node i is destination
                    else if (MapInfo.Destinations.values().contains(i))
                    {
                        weight = CalculateWeightBetweenDestnNode(MapInfo.LandMarkCoordList.get(j), MapInfo.LandMarkCoordList.get(i));
                    }
                    // node j is destination
                    else if (MapInfo.Destinations.values().contains(j))
                    {
                        weight = CalculateWeightBetweenDestnNode(MapInfo.LandMarkCoordList.get(i), MapInfo.LandMarkCoordList.get(j));
                    }
                    // both nodes are not destination
                    else
                    {
                        weight = CalculateWeightBetween2Nodes(MapInfo.LandMarkCoordList.get(i), MapInfo.LandMarkCoordList.get(j));
                    }

                    pathWeight.set(i, j, weight);
                    pathWeight.set(j, i, weight);
                }
            }
        }
        // record the weightmatrix into a file
        BufferedWriter sw = new BufferedWriter(new FileWriter(weightMatrixFile));
     
        for (int i = 0; i < MapInfo.LandMarkCoordList.size(); i++)
        {
            for (int j = 0; j < MapInfo.LandMarkCoordList.size(); j++)
            {
                sw.write(pathWeight.get(i,j)+ "");
                sw.write(" ");
            }
            sw.newLine();
        }
        sw.close();
    }
    
    private static void ConvertPathNodeIndex2Coord()
    {
        for (int index : RountedPathIndices)
        {
            if (index < MapInfo.LandMarkCoordList.size())
            {
                RoutedPath.add(MapInfo.LandMarkCoordList.get(index));
            }
            // when index = count, it refers to the additional node - source besides the original landmark nodes.
            else
            {
                RoutedPath.add(source);
            }
        }
    }
    
    private static void FindCloestNodes2Source()
    {
        cloestNodes2SrcIndex.clear();
        cloestNodes2SrcWeight.clear();

        HashMap<Integer, Double> distances2src = new  HashMap<Integer, Double>();

        // find distances between the source and the landmark nodes
        for (int i = 0; i < MapInfo.LandMarkCoordList.size(); i++)
        {
            distances2src.put(i, GeometryFunc.EuclideanDistanceInMeter(source, MapInfo.LandMarkCoordList.get(i)));
        }

        // sorted the distance ascendingly
        //var sortedDist2SrcIndex = from k in distances2src.Keys orderby distances2src[k] ascending select k;
        HashMap<Integer, Double> sortedDist2SrcIndex = new HashMap<Integer, Double>();
        sortedDist2SrcIndex = Utilities.sortHashMap_A(distances2src);
        double minsrc2nodedist = ConfigSettings.SRC2NODE_DISTANCE_M;
        List<Integer> mapKeys = new ArrayList<Integer>(sortedDist2SrcIndex.keySet());
        
        // enlarge the min bound by 1 meter each time until it is bigger than the min distance 
        //Log.d(null, mapKeys.get(0).toString());
        while (distances2src.get(0) > minsrc2nodedist)
        {
            minsrc2nodedist += 1;
        }

        // include landmark nodes whose distance to source are smaller than min bound; then calculate the weight
        for (int index : mapKeys)
        {
            if (distances2src.get(index) < minsrc2nodedist)
            {
                cloestNodes2SrcIndex.add(index);
                cloestNodes2SrcWeight.add(CalculateWeightBetween2Nodes(source, MapInfo.LandMarkCoordList.get(index)));
            }
            else
            {
                break;
            }
        }
    }
    
    private static double CalculateWeightBetweenDestnNode(Point node, Point dest)
    {
        double distance = GeometryFunc.EuclideanDistanceInMeter(node, dest);

        // Connect the destincation node to the LM node if they are within the tol distance and they are vertical/horizontal
        if ((node.x - dest.x) == 0 && Math.abs(node.y - dest.y) < ConfigSettings.DEST2NODE_DISTANCE_M * ConfigSettings.METER_2_PIXEL_Y)
        {
            return distance;
        }
        else if ((node.y - dest.y) == 0 && Math.abs(node.x - dest.x) < ConfigSettings.DEST2NODE_DISTANCE_M*ConfigSettings.METER_2_PIXEL_X)
        {
            return distance;
        }
        else
        {
            return ConfigSettings.INF; 
        }
    }
    
    private static double CalculateWeightBetween2Nodes(Point node1, Point node2)
    {
        // Find euclidean distance between the two; convert into meter; 
        double distance = GeometryFunc.EuclideanDistanceInMeter(node1, node2);
        distance *= 5;
        if (distance > ConfigSettings.NODE2NODE_DISTANCE_M * 5)
        {
            distance = ConfigSettings.INF; //nodes too far away are not considered as neighbour
        }

        // Find the orientation between the two points
        double orientation;
        if (Math.abs(node1.x - node2.x) < 5)
        {
            orientation = 0; //along a vertical line
        }
        else if (Math.abs(node1.y - node2.y) < 5)
        {
            orientation = 5; // along a horizontal line
        }
        else
        {
            orientation = 100;
        }

        return distance + orientation;
    }

}
