package com.IndoorGPS.LocalizerBasicClass;

import android.graphics.Point;

public class DoublePoint {

	        public double X;
	        public double Y;

	        public DoublePoint(double x, double y)
	        {
	            this.X = x;
	            this.Y = y;
	        }

	        public DoublePoint(Point pt)
	        {
	            this.X = pt.x;
	            this.Y = pt.y;
	        }

	        public DoublePoint(int x, int y)
	        {
	            this.X = (double)x;
	            this.Y = (double)y;
	        }

	        public Point Parse2Point()
	        {
	            return new Point((int)Math.round(this.X), (int)Math.round(this.Y));
	        }
	
}
