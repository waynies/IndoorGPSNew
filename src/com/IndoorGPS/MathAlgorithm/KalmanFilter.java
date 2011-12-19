package com.IndoorGPS.MathAlgorithm;

import java.util.ArrayList;
import java.util.List;

import Jama.Matrix;

public class KalmanFilter {
    // Kalman filter model with no user input
    // x(k) = A(k) * x(k-1) + n(k)
    // z(k) = H(k) * x(k) + v(k)
    //
    // n(k) ~ N(0,Q(k))
    // v(k) ~ N(0,R(k))

    private Matrix A;
    private Matrix H;
    private Matrix Q;
    private Matrix R;
    private Matrix initX;
    private Matrix initP;

    public List<Matrix> X;
    public List<Matrix> P;
    public List<Matrix> E;

    public int k;

    public KalmanFilter(Matrix myA, Matrix myH, Matrix myQ, Matrix myR, Matrix myInitX, Matrix myInitP)
    {
        this.A = myA;
        this.H = myH;
        this.Q = myQ;
        this.R = myR;

        this.k = 0;            
        this.initX = myInitX;
        this.initP = myInitP;

        this.X = new ArrayList<Matrix>();
        this.P = new ArrayList<Matrix>();
        this.E = new ArrayList<Matrix>();
        this.X.add(this.initX);
        this.P.add(this.initP);            
    }

    public void KFUpdate(Matrix z)
    {
        ComputeUpdate(z, this.Q, this.R);
    }

    public void KFUpdateQR(Matrix z, Matrix myQ, Matrix myR)
    {
        ComputeUpdate(z, myQ, myR);
    }

    public Matrix GetEstState()
    {
        return X.get(k);
    }

    public void Reset(Matrix myInitX, Matrix myInitP)
    {
        X.clear();
        P.clear();

        this.k = 0;
        this.initX = myInitX;
        this.initP = myInitP;
        this.X.add(this.initX);
        this.P.add(this.initP);
    }

    private void ComputeUpdate(Matrix z, Matrix myQ, Matrix myR)
    {
        this.k++;

        // Prediction
        Matrix Xpred = this.A.times(X.get(k-1));
        Matrix Ppred = this.A.times(P.get(k-1)).times(this.A.transpose()).plus(myQ);

        Matrix e = z.minus(this.H.times(Xpred));
        Matrix S = H.times(Ppred).times(this.H.transpose()).plus(myR);
        Matrix K = Ppred.times(this.H.transpose()).times(S.inverse());

        // Update
        Matrix Xnew = Xpred.plus(K.times(e));
        Matrix Pnew = Matrix.identity(K.getRowDimension(), K.getRowDimension()).minus(K.times(this.H)).times(Ppred);

        X.add(Xnew);
        P.add(Pnew);
        E.add(e); 
    }
}