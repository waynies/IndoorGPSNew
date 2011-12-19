package com.IndoorGPS.MathAlgorithm;

import java.util.ArrayList;
import java.util.List;

import Jama.Matrix;

public class APCluster
{
    public int[] idx;
    public double netsim;
    public double dpsim;
    public double expref;

    public int[] Iexemplars;
    public int[] c;
    public int N;
    public int K;

    private static int maxIts = 1000;
    private static int convIts = 100;
    private static double lam = 0.9;

    private static double realmax = 1.7977 * Math.pow(10, 308);
    private static double realmin = 2.2251 * Math.pow(10, -308);
    private static double eps = 2.2204 * Math.pow(10, -16);

	//*****************************************************************
	//		Constructor
	//
	//*****************************************************************
    public APCluster(Matrix s, Matrix p, int maxits, int convits, double llam)
    {
        if (maxits > 0)
        {
            maxIts = maxits;
        }

        if (convits > 0)
        {
            convIts = convits;
        }

        if (llam >= 0.5 && llam < 1)
        {
            lam = llam;
        }

        new APCluster(s, p);
    }

	//*****************************************************************
	//		Constructor
	//
	//*****************************************************************
    public APCluster(Matrix s, Matrix p)
    {
        N = p.getRowDimension();

        boolean unconverged;

        Matrix S;

        // Construct similarity matrix
        if (s.getColumnDimension() == 3 && s.getRowDimension() != 3)
        {
            S = new Matrix(N, N, -realmax);
            
            for (int j = 0; j < s.getRowDimension(); j++)
            {
                S.set((int)s.get(j, 0), (int)s.get(j, 1), s.get(j, 2));
            }
        }
        else
        {
            S = s.copy();
        }

        boolean symmetric = false;
            
        //S.Equals(S.Transpose());
        if ( S.minus(S.transpose()).norm1() < 0.01 )
        {
            symmetric = true;
        }

        // Add small amount of noise to the input similarities to avoid degenerate solutions
        S.plusEquals(S.times(eps).plus(new Matrix(N, N, realmin * 100)).arrayTimes(Matrix.random(N, N)));

        // Place preferences on diagonal of S
        for (int a = 0; a < N; a++)
        {
            S.set(a, a, p.get(a, 0));
        }

        double[] dS = Diag(S);
        Matrix A = new Matrix(N, N, 0);
        Matrix R = new Matrix(N, N, 0);

        // Execute parallel affinity propagation updates
        Matrix e = new Matrix(N, convIts, 0);
        boolean dn = false;
        int i = 0;

        Matrix ST;

        if (symmetric == true)
        {
            ST = S.copy();
        }
        else
        {
            ST = S.transpose();
        }

        while (dn == false)
        {
            i++;

            // Compute responsibilities
            A = A.transpose().copy();
            R = R.transpose().copy();

            Matrix old, AS;
            double Y, Y2;
            int I;
            for (int ii = 0; ii < N; ii++)
            {
                old = R.getMatrix(0, R.getRowDimension() - 1, ii, ii);

                AS = A.getMatrix(0, A.getRowDimension() - 1, ii, ii).plus(ST.getMatrix(0, ST.getRowDimension() - 1, ii, ii));

                /*ArrayList<Double> list = new ArrayList(Arrays.asList(AS.getColumnPackedCopy()));
                Y = Collections.max(list);
                I = list.indexOf(Y);
                AS.set(I, 0, -realmax);
                list = Arrays.asList(AS.getColumnPackedCopy());
                Y2 = Collections.max(list);*/

                //R.setMatrix(0, R.getRowDimension() - 1, ii, ii, ST.getMatrix(0, ST.getRowDimension() - 1, ii, ii).minus(new Matrix(ST.getRowDimension(), 1, Y)));
                //R.set(I, ii, ST.get(I, ii) - Y2);
                R.setMatrix(0, R.getRowDimension() - 1, ii, ii, R.getMatrix(0, R.getRowDimension() - 1, ii, ii).times(1 - lam).plus(old.times(lam)));
                for (int k = 0; k < R.getRowDimension(); k++)
                {
                    if (R.get(k, ii) > realmax)
                    {
                        R.set(k, ii, realmax);
                    }
                }
            }
            A = A.transpose();
            R = R.transpose();

            Matrix Rp;
            double dA;

            // Compute availabilities
            for (int jj = 0; jj < N; jj++)
            {
                old = A.getMatrix(0, A.getRowDimension() - 1, jj, jj);

                Rp = new Matrix(N, 1, 0);
                for (int k = 0; k < N; k++)
                {
                    Rp.set(k, 0, Math.max(R.get(k, jj), 0));
                }
                Rp.set(jj, 0, R.get(jj, jj));

                A.setMatrix(0, A.getRowDimension() - 1, jj, jj, Rp.times(-1).plus(new Matrix(N, 1, arraySum(Rp.getColumnPackedCopy()))));
                dA = A.get(jj, jj);
                for (int k = 0; k < N; k++)
                {
                    A.set(k, jj, Math.min(A.get(k, jj), 0));
                }
                A.set(jj, jj, dA);
                A.setMatrix(0, A.getRowDimension() - 1, jj, jj, A.getMatrix(0, A.getRowDimension() - 1, jj, jj).times(1 - lam).plus(old.times(lam)));
            }

            // Check for convergence
            double[] dgA = Diag(A);
            double[] dgR = Diag(R);
            double KK = 0;

            for (int k = 0; k < N; k++)
            {
                if ((dgA[k] + dgR[k]) > 0)
                {
                    KK++;
                    e.set(k, i % convIts, 1);
                }
                else
                {
                    e.set(k, i % convIts, 0);
                }
            }

            if (i >= convIts || i >= maxIts)
            {
                double[] se = new double[N];
                int count = 0;

                for (int k = 0; k < N; k++)
                {
                	// condense the k-th row into a single number
                	se[k] = arraySum(e.getMatrix(k, k, 0, e.getColumnDimension() - 1).getRowPackedCopy());

                	if (se[k] == convIts || se[k] == 0)
                	{
                        count++;
                    }
                }
                
                if (count != N)
                {
                    unconverged = true;
                }
                else
                {
                    unconverged = false;
                }
                
                if (unconverged == false && (KK > 0) || (i == maxIts))
                {
                    dn = true;
                }
            }

        }

        // Identify exemplars
        double[] dgA1 = Diag(A);
        double[] dgR1 = Diag(R);

        List<Integer> II = new ArrayList<Integer>();
        List<Integer> notI = new ArrayList<Integer>();

        for (int k = 0; k < N; k++)
        {
            if ((dgA1[k] + dgR1[k]) > 0)
            {
                II.add(k);
            }
            else
            {
                notI.add(k);
            }
        }
        K = II.size();

        Iexemplars = new int[K];
        copyListToArray(II, Iexemplars);

        int[] tmpidx = new int[N];

        double tmpnetsim = 0;
        double tmpexpref = 0;
        double tmpdpsim = 0;

        if (K > 0)
        {
            // identify clusters
            double[] tmp = new double[N];
            c = new int[N];

            for (int b = 0; b < N; b++)
            {
            	/*ArrayList rowPackedList = new ArrayList();
            	rowPackedList = (ArrayList) Arrays.asList(S.getMatrix(b, b, Iexemplars).getRowPackedCopy());
                tmp[b] = Collections.max(rowPackedList);
                c[b] = rowPackedList.indexOf(tmp[b]);*/
            }

            for (int b = 0; b < K; b++)
            {
                c[Iexemplars[b]] = b;
            }

            // refine the final set of exmplars and clusters
            for (int k = 0; k < K; k++)
            {
                List<Integer> iil = new ArrayList<Integer>();
                for (int dd = 0; dd < N; dd++)
                {
                    if (c[dd] == k)
                    {
                        iil.add(dd);
                    }
                }

                int[] ii = new int[iil.size()];
                copyListToArray(iil, ii);
                
                Matrix sS = S.getMatrix(ii, ii);

                double[] sum = new double[ii.length];
                for (int dd = 0; dd < ii.length; dd++)
                {
                    //sum[dd] = sS.getMatrix(0, sS.getRowDimension() - 1, dd, dd).ColumnPackedCopy.Sum();
                }
                
                double y = 0;//sum.Max();
                int j = 0;//Array.indexOf(sum, y);

                Iexemplars[k] = ii[j];

            }

            for (int dd = 0; dd < N; dd++)
            {
                //tmp[dd] = S.getMatrix(dd, dd, Iexemplars).RowPackedCopy.Max();
                //c[dd] = Array.IndexOf(S.getMatrix(dd, dd, Iexemplars).RowPackedCopy, tmp[dd]);
            }

            for (int dd = 0; dd < K; dd++)
            {
                c[Iexemplars[dd]] = dd;
            }

            for (int dd = 0; dd < N; dd++)
            {
                tmpidx[dd] = Iexemplars[c[dd]];
            }

            tmpdpsim = 0;
            for(Integer ni : notI)
            {
                tmpdpsim += S.get(ni, tmpidx[ni]);
            }

            tmpexpref = 0;
            
            for(Integer ie : Iexemplars)
            {
                tmpexpref += dS[ie];
            }

            tmpnetsim = tmpdpsim + tmpexpref;
        }


        netsim = tmpnetsim;
        dpsim = tmpdpsim;
        expref = tmpexpref;
        idx = new int[N];
        for (int dd = 0; dd < N; dd++)
        {
            idx[dd] = tmpidx[dd];
        }
    }

    private double[] Diag(Matrix A)
    {
        double[] d = new double[A.getRowDimension()];
        for (int i = 0; i < A.getRowDimension(); i++)
        {
            d[i] = A.get(i, i);
        }

        return d;
    }
    
    private double arraySum(double[] input)
    {
    	double sum = 0;
    	for(double i : input)
    	{
    		sum += i;
    	}
    	return sum;
    }
    
    private void copyListToArray(List<Integer> input, int[] output)
    {
        for(int i = 0; i < input.size(); i++)
        {
        	output[i] = input.get(i).intValue();
        }
    }
}