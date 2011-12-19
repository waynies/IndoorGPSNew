package com.IndoorGPS.Tracking;

import android.graphics.Point;

import com.IndoorGPS.Utilities;
import com.IndoorGPS.LocalizerBasicClass.LocResult;
import com.IndoorGPS.MathAlgorithm.KalmanFilter;

import Jama.Matrix;

public class KFTracking {

	public static Matrix A;
    public static Matrix H;
    private static Matrix Q;
    private static Matrix R;
    private static Matrix initX;
    private static Matrix initP;
    private static KalmanFilter kalmanF;
    
    public static void Initialization(double vx, double vy, double duration, double q, double r, double l)
    {
        A = new Matrix(new double[][] { new double[] { 1, 0, duration, 0 }, new double[] { 0, 1, 0, duration }, new double[] { 0, 0, 1, 0 }, new double[] { 0, 0, 0, 1 } });
        H = new Matrix(new double[][] { new double[] { 1, 0, 0, 0 }, new double[] { 0, 1, 0, 0 } });
        Q = Matrix.identity(4, 4).times(q);
        R = Matrix.identity(2, 2).times(r);            

        initX = new Matrix(4, 1);
        initX.set(2, 0, vx);
        initX.set(3, 0, vy);

        initP = Matrix.identity(4, 4).times(l);

        //UtilitiesFunc.WriteLineInFile(ConfigSettings.LOGFILENAME, String.Format(">Kalman Filter Tracking: dt:{0} q:{1} r:{2}, l:{3}", duration, q, r, l));

        kalmanF = new KalmanFilter(A, H, Q, R, initX, initP);
    }
    
    public static void SetInitPos(Point initPos)
    {
        initX.set(0, 0, Utilities.intDouble(initPos.x));
        initX.set(1, 0, Utilities.intDouble(initPos.y));
        //UtilitiesFunc.WriteLineInFile(ConfigSettings.LOGFILENAME, String.Format("--InitX=[{0} {1} {2} {3}]'", initX.ColumnPackedCopy[0], initX.ColumnPackedCopy[1], initX.ColumnPackedCopy[2], initX.ColumnPackedCopy[3]));
        kalmanF.Reset(initX, initP);
    }

    public static void ResetWPrevComputedLoc()
    {
        initX.set(0, 0, Utilities.intDouble(LocResult.ComputedPositionMeas.get(LocResult.ComputedPositionMeas.size()-1).x));
        initX.set(1, 0, Utilities.intDouble(LocResult.ComputedPositionMeas.get(LocResult.ComputedPositionMeas.size()-1).y));

        //UtilitiesFunc.WriteLineInFile(ConfigSettings.LOGFILENAME, String.Format("-Reset-InitX=[{0} {1} {2} {3}]'", initX.ColumnPackedCopy[0], initX.ColumnPackedCopy[1], initX.ColumnPackedCopy[2], initX.ColumnPackedCopy[3]));
        kalmanF.Reset(initX, initP);
    }

    public static void ResetWPrevLoc()
    {
        initX.set(0, 0, Utilities.intDouble(LocResult.EstimatedPositions.get(LocResult.EstimatedPositions.size()-1).x));
        initX.set(1, 0, Utilities.intDouble(LocResult.EstimatedPositions.get(LocResult.EstimatedPositions.size()-1).y));

        //UtilitiesFunc.WriteLineInFile(ConfigSettings.LOGFILENAME, String.Format("-Reset-InitX=[{0} {1} {2} {3}]'", initX.ColumnPackedCopy[0], initX.ColumnPackedCopy[1], initX.ColumnPackedCopy[2], initX.ColumnPackedCopy[3]));
        kalmanF.Reset(initX, initP);
    }

    public static void ResetWPrevLoc(double vx, double vy)
    {
        initX.set(0, 0, Utilities.intDouble(LocResult.EstimatedPositions.get(LocResult.EstimatedPositions.size()-1).x));
        initX.set(1, 0, Utilities.intDouble(LocResult.EstimatedPositions.get(LocResult.EstimatedPositions.size()-1).y));
        initX.set(2, 0, vx);
        initX.set(3, 0, vy);

        //UtilitiesFunc.WriteLineInFile(ConfigSettings.LOGFILENAME, String.Format("-Reset-InitX=[{0} {1} {2} {3}]'", initX.ColumnPackedCopy[0], initX.ColumnPackedCopy[1], initX.ColumnPackedCopy[2], initX.ColumnPackedCopy[3]));
        kalmanF.Reset(initX, initP);
    }

    public static double[] GetPredXY()
    {
        return H.times(A.times(kalmanF.GetEstState())).getColumnPackedCopy();
    }
       
    public static Matrix UpdateKF(Point currentmeas)
    {
        Matrix z = new Matrix(2, 1);
        z.set(0, 0, currentmeas.x);
        z.set(1, 0, currentmeas.y);
        kalmanF.KFUpdate(z);

        Matrix estX = kalmanF.GetEstState();
        return estX;                
    }

    public static Point ComputeEstimate()
    {
        Point currentmeas = LocResult.ComputedPositionMeas.get(LocResult.ComputedPositionMeas.size()-1);
        Point estPt;

        if (LocResult.EstimatedPositions.size() == 0)
        {
            SetInitPos(currentmeas);
            estPt = currentmeas;
        }
        else
        {
            Matrix estX = UpdateKF(currentmeas);
            
            estPt = new Point((int)Math.round(estX.get(0, 0)), (int)Math.round(estX.get(1, 0)));
        }
        LocResult.EstimatedPositions.add(estPt);
        //UtilitiesFunc.WriteLineInFile(ConfigSettings.LOGFILENAME, string.Format("#{0}: \nMeas=({1},{2})\nEst=({3},{4})", LocResult.EstimatedPositions.Count, currentmeas.X, currentmeas.Y, estPt.X, estPt.Y));
        return estPt;
    }
    
}
