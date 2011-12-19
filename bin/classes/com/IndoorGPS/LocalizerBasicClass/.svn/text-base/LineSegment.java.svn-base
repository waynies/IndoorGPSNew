package com.IndoorGPS.LocalizerBasicClass;

import android.graphics.Point;

public class LineSegment {
	
	        public Point PtS;
	        public Point PtE;
	        public int DirAngle;
	        public double Distance;

	        public LineSegment(Point s, Point e)
	        {
	            this.PtS = s;
	            this.PtE = e;

	            this.Distance = GeometryFunc.EuclideanDistanceInMeter(this.PtS, this.PtE);
	            this.DirAngle = GeometryFunc.ComputeAngle(this.PtS, this.PtE, true);
	        }

	        public LineSegment(Point s, Point e, double dist, int dir)
	        {
	            this.PtS = s;
	            this.PtE = e;
	            this.Distance = dist;
	            this.DirAngle = dir;
	        }

	        /// <summary>
	        /// Return the point on the line segment from this line segment equation given u:
	        /// P = PtS + u(PtE-PtS)
	        /// </summary>
	        /// <param name="u"></param>
	        /// <returns></returns>
	        public Point GetPointFromEqn(double u)
	        {
	            int ptx = (int)Math.round(this.PtS.x + u * (this.PtE.x - this.PtS.x));
	            int pty = (int)Math.round(this.PtS.y + u * (this.PtE.y - this.PtS.y));
	            return new Point(ptx, pty);
	        }

	        public double DistanceBTW2PtsOnLS(double u1, double u2)
	        {
	            double diff = Math.abs(u1 - u2);
	            return diff * this.Distance;
	        }
	        /// <summary>
	        /// Find the shortest distance between a point P and the line segment PtS to PtE
	        /// 1. Equation of line segment: PP = PtS + u(PtE-PtS); 0 leq u leq 1
	        /// 2. The shortest distance occured at the tangent to the line seqment which passes through P3: (P - PP) dot (PtE - PtS) = 0
	        /// 3. Solve for u by subsititude 1. into 2.
	        /// 4. Use u to find PP
	        /// 5. Find distance: distance = ||PP-P|| in meter
	        /// Note: meter2pixel ratios are irrelevant to find the value u.
	        /// </summary>
	        /// <param name="p"></param>
	        /// <param name="u">show where the tangent that depict the shortest distance lies on the line; closer to 0 means near PtS, vice versa</param>
	        public double FindShortestDist2Point(Point p, Double u)
	        {
	            u = ((p.x - this.PtS.x) * (this.PtE.x - this.PtS.x) + (p.y - this.PtS.y) * (this.PtE.y - this.PtS.y)) / (Math.pow(this.PtE.x - this.PtS.x, 2) + Math.pow(this.PtE.y - this.PtS.y, 2));

	            return GeometryFunc.EuclideanDistanceInMeter(p, this.GetPointFromEqn(u));
	        }

	        public double Out_U(Point p, Double u)
	        {
	        	u = ((p.x - this.PtS.x) * (this.PtE.x - this.PtS.x) + (p.y - this.PtS.y) * (this.PtE.y - this.PtS.y)) / (Math.pow(this.PtE.x - this.PtS.x, 2) + Math.pow(this.PtE.y - this.PtS.y, 2));
	        	return u;
	        }
	        /// <summary>
	        /// Determine if this line segment (PtS, PtE) intersects with the given line segment (lineS, lineE).
	        /// 1. Equations of line segments: P1 = PtS + u1(PtE-PtS); P2 = lineS + u2(lineE-lineS)
	        /// 2. Intersect --> P1 = P2; solve for u1; u2
	        /// 3. if the common denominators of u1, u2 is zero --> parallel
	        ///    if both denominators and numerators are zero --> coincident
	        /// 4. Line segment --> u1 and u2 within range [0,1]
	        /// </summary>
	        /// <param name="lineS">Starting point of the given line segment</param>
	        /// <param name="lineE">Ending point of the given line segment</param>
	        /// <param name="u1">return where the intersection point is relative to line segment (PtS, PtE)</param>
	        /// <returns> 1 - intersects within 2 line segments 
	        ///           0 - line segments are parallel
	        ///          -1 - intersection point at line segment (PtS, PtE), and extension of other line segment
	        ///          -2 - intersection point at given line segment (lineS, lineE), and extension of other line segment
	        ///          -3 - intersection point at extension of both segments</returns>
	        public int IsIntersectWithLineSegment(Point lineS, Point lineE, Double u1, Double u2)
	        {
	            double denominator = (lineE.y - lineS.y) * (PtE.x - PtS.x) - (lineE.x - lineS.x) * (PtE.y - PtS.y);

	            if (denominator == 0)
	            {
	                u1 = (double) -1;
	                u2 = (double) -1;
	                return 0;
	            }

	            double u1numerator = (lineE.x - lineS.x) * (PtS.y - lineS.y) - (lineE.y - lineS.y) * (PtS.x - lineS.x);
	            double u2numerator = (PtE.x - PtS.x) * (PtS.y - lineS.y) - (PtE.y - PtS.y) * (PtS.x - lineS.x);

	            u1 = u1numerator / denominator;
	            u2 = u2numerator / denominator;

	            if (0 <= u1 && u1 <= 1 && 0 <= u2 && u2 <= 1)
	            {
	                return 1;
	            }
	            else if (0 <= u1 && u1 <= 1)
	            {
	                return -1;
	            }
	            else if (0 <= u2 && u2 <= 1)
	            {
	                return -2;
	            }
	            else
	            {
	                return -3;
	            }
	        }
	        
	        public Double[] Out_U1_U2(Point lineS, Point lineE, Double u1, Double u2)
	        {
	            Double[] out = new Double[2];
	        	double denominator = (lineE.y - lineS.y) * (PtE.x - PtS.x) - (lineE.x - lineS.x) * (PtE.y - PtS.y);

	            if (denominator == 0)
	            {
	                u1 = (double) -1;
	                u2 = (double) -1;
	                out[0] = u1;
	                out[1] = u2;
	                return out;
	            }

	            double u1numerator = (lineE.x - lineS.x) * (PtS.y - lineS.y) - (lineE.y - lineS.y) * (PtS.x - lineS.x);
	            double u2numerator = (PtE.x - PtS.x) * (PtS.y - lineS.y) - (PtE.y - PtS.y) * (PtS.x - lineS.x);

	            u1 = u1numerator / denominator;
	            u2 = u2numerator / denominator;

	            out[0] = u1;
                out[1] = u2;
                return out;
	        }

	        /// <summary>
	        /// Print out the line segment info.
	        /// </summary>
	        /// <returns></returns>
	        public String Print()
	        {
	            return String.format("S:(%s,%s) E:(%s,%s) Dist:%s Angle:%s", this.PtS.x, this.PtS.y, this.PtE.x, this.PtE.y, this.Distance, this.DirAngle);
	        }
	    

}
