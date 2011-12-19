package com.IndoorGPS.MathAlgorithm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.IndoorGPS.Utilities;

import Jama.Matrix;

public class Dijkstra
{
    private static int INF = 999999;
    private static int UNDEFINED = -1;

    private Matrix originalPathWeightMatrix, pathWeightMatrix;
    private int numVertices;

    public int[] Previous;
    public HashMap<Integer, Double> Dist;

    public List<Integer> Path;

    public Dijkstra(Matrix weightMatrix) throws Exception
    {
        originalPathWeightMatrix = weightMatrix.copy();
        numVertices = originalPathWeightMatrix.getColumnDimension();

        if (numVertices != originalPathWeightMatrix.getRowDimension())
        {
            throw new Exception("Parameter must be a square matrix");
        }

        Dist = new HashMap<Integer, Double>();
        Path = new ArrayList<Integer>();
        Previous = new int[numVertices + 1];
    }

    public List<Integer> FindShortestPath(List<Integer> srcNeighbours, List<Double> srcNeightboursWt, int target) throws Exception
    {
    	
        if (!IsInRange(srcNeighbours) || !IsInRange(target))
        {
            throw new Exception("Parameters must be within 0 - numVertices. (one of the nodes in the graph)");
        }

        numVertices = originalPathWeightMatrix.getColumnDimension() + 1;
        pathWeightMatrix = new Matrix(numVertices, numVertices, INF);
        pathWeightMatrix.setMatrix(0, numVertices - 2, 0, numVertices - 2, originalPathWeightMatrix);

        for (int i = 0; i < srcNeighbours.size(); i++)
        {
            pathWeightMatrix.set(numVertices - 1, srcNeighbours.get(i), srcNeightboursWt.get(i));
            pathWeightMatrix.set(srcNeighbours.get(i), numVertices - 1, srcNeightboursWt.get(i));
        }

        RunAlgorithm(numVertices - 1, target);
        ShortestPath(numVertices - 1, target);

        return Path;
    }


	public List<Integer> FindShortestPath(int source, int target) throws Exception
    {
        if (!IsInRange(source) || !IsInRange(target))
        {
            throw new Exception("Parameters must be within 0 - numVertices. (one of the nodes in the graph)");
        }

        pathWeightMatrix = originalPathWeightMatrix.copy();
        numVertices = originalPathWeightMatrix.getColumnDimension();

        RunAlgorithm(source, target);
        ShortestPath(source, target);

        return Path;
    }

    private void RunAlgorithm(int source, int target){
        //initialization

        Dist.clear();

        for (int i = 0; i < numVertices; i++)
        {
            Dist.put(i, (double) INF);
            Previous[i] = UNDEFINED;
        }

        Dist.put(source, 0.0);

        List<Integer> Q_unvistedNodeSet = new ArrayList<Integer>();
        for (int i = 0; i < numVertices; i++)
        {
            Q_unvistedNodeSet.add(i);
        }

        //for each node in set Q
        while (Q_unvistedNodeSet.size() > 0)
        {
            HashMap<Integer, Double> sortedDist = new HashMap<Integer, Double>();
            sortedDist = Utilities.sortHashMap_A(Dist);
            List<Integer> mapKeys = new ArrayList<Integer>(sortedDist.keySet());
            
            //var sortedDist = from k in Dist.Keys orderby Dist[k] ascending select k;
            
            //find the vertex (u) in set Q that has the smallest distance
            int j = 0;
            while (!Q_unvistedNodeSet.contains(mapKeys.get(j)))
            {
                j++;
            }

            int u = mapKeys.get(j);

            // source is not assessible; no path is found
            if (Dist.get(u) >= INF)
            {
                break;
            }

            Q_unvistedNodeSet.remove(Q_unvistedNodeSet.indexOf(u));

            if (u == target)
            {
            	System.out.println(Previous.toString());
                break;
            }

            //for (int v = 0; v < NumVertices; i++)
            for (int v : Q_unvistedNodeSet)
            {
                // v is not a neighbour of u
                if (pathWeightMatrix.get(u, v) < 0)
                {
                    continue;
                }
                else
                {
                    double alt = Dist.get(u) + pathWeightMatrix.get(u, v);
                    if (alt < Dist.get(v))
                    {
                        Dist.put(v, alt);
                        Previous[v] = u;
                    }
                }
            }
        }
        System.out.println(Previous.toString());
    }

    private void ShortestPath(int source, int target)
    {
        Path = new ArrayList<Integer>();
        int u = target;
        Path.add(u);
        while (Previous[u] != UNDEFINED)
        {
        	Path.add(0, Previous[u]);
            u = Previous[u];
        }
    }

    private boolean IsInRange(int index)
    {
        return (index >= 0 && index < numVertices);
    }
    

    private boolean IsInRange(List<Integer> input) {
		// TODO Auto-generated method stub
    	boolean output = true;
    	for(int i = 0; i < input.size(); i++)
    	{
    		if(input.get(i) < 0 || input.get(i) > numVertices)
    		{	
    			output = false;
    			break;
    		}
    	}
		return output;
	}
}