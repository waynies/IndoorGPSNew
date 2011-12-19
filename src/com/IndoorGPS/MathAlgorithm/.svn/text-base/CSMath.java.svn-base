package com.IndoorGPS.MathAlgorithm;

import java.util.ArrayList;
import java.util.List;

import com.IndoorGPS.Utilities;

import Jama.Matrix;
import Jama.SingularValueDecomposition;

public class CSMath{
	
	/* Orthogonalization: return an orthonormal basis Q for the range of A.
     *                    Q'*Q = I 
     *                    Taken from Matlab code */
    public static Matrix Orthogonalization(Matrix A)
    {
        SingularValueDecomposition svd = A.svd();
        Matrix U = svd.getU();
        double[] s = svd.getSingularValues();
        int m = A.getRowDimension();
        int n = A.getColumnDimension();
        double tol = Math.max(m, n) * Utilities.doubleMax(s) * Math.pow(2, -52);

        int r = 0;
        for (double i : s)
        {
            if (i > tol)
            {
                r++;
            }
        }
        return U.getMatrix(0, m - 1, 0, r - 1);

    }
    
    /* Pseudoinverse: return a matrix X of the same dimensions as A' so that A*X*A = A,
     *                X*A*X = X and A*X and X*A are Hermitian.
     *                Taken from Matlab code */
    public static Matrix Pseudoinverse(Matrix A)
    {
        Matrix X;

        int m = A.getRowDimension();
        int n = A.getColumnDimension();

        double tol = Math.max(m, n) * Math.pow(10, -12);

        if (n > m)
        {
            X = Pseudoinverse(A.transpose()).transpose();
        }
        else
        {
            SingularValueDecomposition svd = A.svd();
            Matrix U = svd.getU();
            Matrix V = svd.getV();
            
            double[] s = svd.getSingularValues();

            int r = 0;
            for (double i : s)
            {
                if (i > tol)
                {
                    r++;
                }
            }

            if (r == 0)
            {
                X = new Matrix(m, n);
            }
            else
            {
                Matrix S = svd.getS().getMatrix(0, r - 1, 0, r - 1).inverse();
                X = V.getMatrix(0, V.getRowDimension() - 1, 0, r - 1).times(S).times(U.getMatrix(0, U.getRowDimension() - 1, 0, r - 1).transpose());
            }
        }
        return X;
    }
    
    
    /* L1eq_pd: Solve Optimization problem:
     *                 min_x ||x||_1   s.t. Ax=b
     *                 
     *          Recast as linear program
     *                 min_{x,u} sum(u)   s.t. -u <= x <= u, Ax=b
     *          and use primal-dual interior point method 
     *          
     *          xp = L1eq_pd(x0, A,b)
     *          x0 - N x 1 vector, initial vector
     *          A  - K x N matrix
     *          b  - K x 1 vector of obseravtions 
     *          Taken from Matlab code */
    public static Matrix L1eq_pd(Matrix x0, Matrix A, Matrix b)
    {
        double pdTol = Math.pow(10, -3);
        double pdMaxIter = 50;

        int N = x0.getRowDimension();

        double alpha = 0.01;
        double beta = 0.5;
        double mu = 10;


        Matrix gradf0 = new Matrix(2 * N, 1, 0);
        gradf0.setMatrix(N, 2 * N - 1, 0, 0, new Matrix(N, 1, 1));

        Matrix x = x0.copy();

        Matrix absx0 = new Matrix(N, 1);
        for (int i = 0; i < N; i++)
        {
            absx0.set(i, 0, Math.abs(x0.get(i, 0)));
        }
        double maxabsx0 = Utilities.doubleMax(absx0.getColumnPackedCopy());

        Matrix u = absx0.times(0.95).plus(new Matrix(N, 1, 0.10 * maxabsx0));

        Matrix fu1 = x.minus(u);
        Matrix fu2 = x.times(-1).minus(u);

        Matrix lamu1 = fu1.arrayLeftDivide(new Matrix(N, 1, -1));
        Matrix lamu2 = fu2.arrayLeftDivide(new Matrix(N, 1, -1));

        Matrix v = A.times(-1).times(lamu1.minus(lamu2));
        Matrix Atv = A.transpose().times(v);
        Matrix rpri = A.times(x).minus(b);

        double sdg = (fu1.transpose().times(lamu1).plus((fu2.transpose().times(lamu2)))).times(-1).get(0, 0);
        double tau = mu * 2 * N / sdg;

        Matrix rcent = new Matrix(2 * N, 1);
        rcent.setMatrix(0, N - 1, 0, 0, lamu1.times(-1).arrayTimes(fu1));
        rcent.setMatrix(N, 2 * N - 1, 0, 0, lamu2.times(-1).arrayTimes(fu2));
        rcent.minusEquals(new Matrix(2 * N, 1, 1 / tau));

        Matrix rdual = new Matrix(2 * N, 1);
        rdual.setMatrix(0, N - 1, 0, 0, gradf0.getMatrix(0, N - 1, 0, 0).plus(lamu1).minus(lamu2).plus(Atv));
        rdual.setMatrix(N, 2 * N - 1, 0, 0, gradf0.getMatrix(N, 2 * N - 1, 0, 0).minus(lamu1).minus(lamu2));

        Matrix rr = new Matrix(4 * N + b.getRowDimension(), 1);
        rr.setMatrix(0, 2 * N - 1, 0, 0, rdual);
        rr.setMatrix(2 * N, 4 * N - 1, 0, 0, rcent);
        rr.setMatrix(4 * N, 4 * N + b.getRowDimension() - 1, 0, 0, rpri);

        double resnorm = rr.norm2();

        int pditer = 0;

        boolean done = (sdg < pdTol) || (pditer >= pdMaxIter);

        Matrix w1, w2, w3, sig1, sig2, sigx, H11p, w1p, dv, dx, Adx, Atdv;
        Matrix du, dlamu1, dlamu2, ss, up, vp, Atvp, lamu1p, lamu2p, fu1p, fu2p, rdp, rcp, rpp;
        Matrix xp = x.copy();

        double s;
        int[] indp, indn;

        double hcond;

        int backiter;

        while (!done)
        {
            pditer++;

            w1 = (fu1.arrayLeftDivide(new Matrix(N, 1, -1)).plus(fu2.arrayLeftDivide(new Matrix(N, 1, 1)))).times(-1 / tau).minus(Atv);
            w2 = (fu1.arrayLeftDivide(new Matrix(N, 1, 1)).plus(fu2.arrayLeftDivide(new Matrix(N, 1, 1)))).times(-1 / tau).minus(new Matrix(N, 1, 1));
            w3 = rpri.copy().times(-1);

            sig1 = fu1.arrayLeftDivide(lamu1).times(-1).minus(fu2.arrayLeftDivide(lamu2));
            sig2 = fu1.arrayLeftDivide(lamu1).minus(fu2.arrayLeftDivide(lamu2));
            sigx = sig1.minus(sig2.arrayTimes(sig2).arrayRightDivide(sig1));

            H11p = A.times(-1).times(Diag(sigx.arrayLeftDivide(new Matrix(N, 1, 1)))).times(A.transpose());
            w1p = w3.minus(A.times(sigx.arrayLeftDivide(w1).minus(w2.arrayTimes(sig2).arrayRightDivide(sigx.arrayTimes(sig1)))));

            hcond = 1 / H11p.cond();

            // Matrix ill-conditioned. Returning previous iterate
            if (hcond < Math.pow(10, -14))
            {
                xp = x.copy();
                return xp;
            }

            dv = H11p.solve(w1p);
            dx = (w1.minus(w2.arrayTimes(sig2).arrayRightDivide(sig1)).minus(A.transpose().times(dv))).arrayRightDivide(sigx);
            Adx = A.times(dx);
            Atdv = A.transpose().times(dv);

            du = sig1.arrayLeftDivide(w2.minus(sig2.arrayTimes(dx)));

            dlamu1 = lamu1.arrayRightDivide(fu1).arrayTimes(du.minus(dx)).minus(lamu1).minus(fu1.arrayLeftDivide(new Matrix(N, 1, 1)).times(1 / tau));
            dlamu2 = lamu2.arrayRightDivide(fu2).arrayTimes(du.plus(dx)).minus(lamu2).minus(fu2.arrayLeftDivide(new Matrix(N, 1, 1)).times(1 / tau));

            // make sure that the step is feasible: keep lamu1, lamu2 > 0, fu1,fu2 <0
            indp = FindElement(dlamu1, 1);
            indn = FindElement(dlamu2, 1);

            ss = new Matrix(1 + indp.length + indn.length, 1, 1);
            ss.setMatrix(1, indp.length, 0, 0, lamu1.getMatrix(indp, 0, 0).arrayRightDivide(dlamu1.getMatrix(indp, 0, 0)).times(-1));
            ss.setMatrix(indp.length + 1, indp.length + indn.length, 0, 0, lamu2.getMatrix(indn, 0, 0).arrayRightDivide(dlamu2.getMatrix(indn, 0, 0)).times(-1));

            s = Utilities.doubleMin(ss.getColumnPackedCopy());

            indp = FindElement(dx.minus(du), 2);
            indn = FindElement(dx.times(-1).minus(du), 2);

            //ss.dispose();
            ss = new Matrix(1 + indp.length + indn.length, 1, s);
            ss.setMatrix(1, indp.length, 0, 0, fu1.getMatrix(indp, 0, 0).arrayRightDivide(dx.getMatrix(indp, 0, 0).minus(du.getMatrix(indp, 0, 0))).times(-1));
            ss.setMatrix(indp.length + 1, indp.length + indn.length, 0, 0, fu2.getMatrix(indn, 0, 0).arrayRightDivide(dx.getMatrix(indn, 0, 0).times(-1).minus(du.getMatrix(indn, 0, 0))).times(-1));

            s = 0.99 * Utilities.doubleMin(ss.getColumnPackedCopy());

            // backtracking line search
            backiter = 0;
            xp = x.plus(dx.times(s));
            up = u.plus(du.times(s));
            vp = v.plus(dv.times(s));
            Atvp = Atv.plus(Atdv.times(s));
            lamu1p = lamu1.plus(dlamu1.times(s));
            lamu2p = lamu2.plus(dlamu2.times(s));
            fu1p = xp.minus(up);
            fu2p = xp.times(-1).minus(up);

            rdp = new Matrix(2 * N, 1);
            rdp.setMatrix(0, N - 1, 0, 0, gradf0.getMatrix(0, N - 1, 0, 0).plus(lamu1p).minus(lamu2p).plus(Atvp));
            rdp.setMatrix(N, 2 * N - 1, 0, 0, gradf0.getMatrix(N, 2 * N - 1, 0, 0).minus(lamu1p).minus(lamu2p));

            rcp = new Matrix(2 * N, 1);
            rcp.setMatrix(0, N - 1, 0, 0, lamu1p.times(-1).arrayTimes(fu1p));
            rcp.setMatrix(N, 2 * N - 1, 0, 0, lamu2p.times(-1).arrayTimes(fu2p));
            rcp.minusEquals(new Matrix(2 * N, 1, 1 / tau));

            rpp = rpri.plus(Adx.times(s));

            rr = new Matrix(4 * N + b.getRowDimension(), 1);
            rr.setMatrix(0, 2 * N - 1, 0, 0, rdp);
            rr.setMatrix(2 * N, 4 * N - 1, 0, 0, rcp);
            rr.setMatrix(4 * N, 4 * N + b.getRowDimension() - 1, 0, 0, rpp);


            while (rr.norm2() > (1 - alpha * s) * resnorm)
            {
                s = beta * s;

                xp = x.plus(dx.times(s));
                up = u.plus(du.times(s));
                vp = v.plus(dv.times(s));
                Atvp = Atv.plus(Atdv.times(s));
                lamu1p = lamu1.plus(dlamu1.times(s));
                lamu2p = lamu2.plus(dlamu2.times(s));
                fu1p = xp.minus(up);
                fu2p = xp.times(-1).minus(up);

                rdp = new Matrix(2 * N, 1);
                rdp.setMatrix(0, N - 1, 0, 0, gradf0.getMatrix(0, N - 1, 0, 0).plus(lamu1p).minus(lamu2p).plus(Atvp));
                rdp.setMatrix(N, 2 * N - 1, 0, 0, gradf0.getMatrix(N, 2 * N - 1, 0, 0).minus(lamu1p).minus(lamu2p));

                rcp = new Matrix(2 * N, 1);
                rcp.setMatrix(0, N - 1, 0, 0, lamu1p.times(-1).arrayTimes(fu1p));
                rcp.setMatrix(N, 2 * N - 1, 0, 0, lamu2p.times(-1).arrayTimes(fu2p));
                rcp.minusEquals(new Matrix(2 * N, 1, 1 / tau));

                rpp = rpri.plus(Adx.times(s));

                rr = new Matrix(4 * N + b.getRowDimension(), 1);
                rr.setMatrix(0, 2 * N - 1, 0, 0, rdp);
                rr.setMatrix(2 * N, 4 * N - 1, 0, 0, rcp);
                rr.setMatrix(4 * N, 4 * N + b.getRowDimension() - 1, 0, 0, rpp);

                backiter++;

                //Stuck backtracking, returning last iterate
                if (backiter > 32)
                {
                    xp = x.copy();
                    return xp;
                }
            }

            // next iteration
            x = xp.copy();
            u = up.copy();
            v = vp.copy();
            Atv = Atvp.copy();
            lamu1 = lamu1p.copy();
            lamu2 = lamu2p.copy();
            fu1 = fu1p.copy();
            fu2 = fu2p.copy();

            // surrogate duality gap
            sdg = (fu1.transpose().times(lamu1).plus((fu2.transpose().times(lamu2)))).times(-1).get(0, 0);
            tau = mu * 2 * N / sdg;

            rpri = rpp.copy();

            rcent.setMatrix(0, N - 1, 0, 0, lamu1.times(-1).arrayTimes(fu1));
            rcent.setMatrix(N, 2 * N - 1, 0, 0, lamu2.times(-1).arrayTimes(fu2));
            rcent.minusEquals(new Matrix(2 * N, 1, 1 / tau));

            rdual.setMatrix(0, N - 1, 0, 0, gradf0.getMatrix(0, N - 1, 0, 0).plus(lamu1).minus(lamu2).plus(Atv));
            rdual.setMatrix(N, 2 * N - 1, 0, 0, gradf0.getMatrix(N, 2 * N - 1, 0, 0).minus(lamu1).minus(lamu2));

            rr.setMatrix(0, 2 * N - 1, 0, 0, rdual);
            rr.setMatrix(2 * N, 4 * N - 1, 0, 0, rcent);
            rr.setMatrix(4 * N, 4 * N + b.getRowDimension() - 1, 0, 0, rpri);

            resnorm = rr.norm2();

            done = (sdg < pdTol) || (pditer >= pdMaxIter);
        }

        return xp;
    }
    
    /* Diag: Convert column vector into diagional matrix */
    public static Matrix Diag(Matrix B)
    {
        Matrix X = new Matrix(B.getRowDimension(), B.getRowDimension(), 0);

        for (int i = 0; i < B.getRowDimension(); i++)
        {
            X.set(i, i, B.get(i, 0));
        }
        return X;

    }
    
    /* FindElement: return the indices of a column vector that satisfies the condition*/
    public static int[] FindElement(Matrix B, int condition)
    {
        //ArrayList indexlist = new ArrayList();
        List<Integer> indexlist = new ArrayList<Integer>();
        for (int i = 0; i < B.getRowDimension(); i++)
        {
            switch (condition)
            {
                case 1:
                    if (B.get(i, 0) < 0)
                    {
                        indexlist.add(i);
                    }
                    break;
                case 2:
                    if (B.get(i, 0) > 0)
                    {
                        indexlist.add(i);
                    }
                    break;
            }
        }

        int[] indices = new int[indexlist.size()];
        indices = Utilities.Listint(indexlist);
        
        indexlist.clear();

        return indices;
    }


}