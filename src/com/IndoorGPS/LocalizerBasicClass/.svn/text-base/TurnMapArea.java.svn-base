package com.IndoorGPS.LocalizerBasicClass;

import android.graphics.Point;

public class TurnMapArea {
		
		public Point[] turnBoundary;
        public Point[] advanceTurnBoundary;

        public void MapInfo(Point[] tb, Point[] adtb)
        {
            this.turnBoundary = tb;
            this.advanceTurnBoundary = adtb;
        }

        public TurnMapArea(String [] tbxylist, String[] adtbxylist)
        {
            Point pt1, pt2;

            turnBoundary = new Point[4];
            advanceTurnBoundary = new Point[4];

            for (int i = 0; i < 8; i += 2)
            {
                pt1 = new Point(Integer.parseInt(tbxylist[i]), Integer.parseInt(tbxylist[i + 1]));
                pt2 = new Point(Integer.parseInt(adtbxylist[i]), Integer.parseInt(adtbxylist[i + 1]));

                turnBoundary[i / 2] = pt1;
                advanceTurnBoundary[i / 2] = pt2;
            }
        }
}
