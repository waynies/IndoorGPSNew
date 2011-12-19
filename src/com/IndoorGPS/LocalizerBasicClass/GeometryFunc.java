package com.IndoorGPS.LocalizerBasicClass;


import android.graphics.Point;

public class GeometryFunc
{
    /// <summary>
    /// Find Euclidean distance between two points (in pixels)
    /// </summary>
    /// <param name="node1"></param>
    /// <param name="node2"></param>
    /// <returns>distance in pixels unit</returns>
    public static double EuclideanDistance(Point node1, Point node2)
    {
        return Math.sqrt(Math.pow(node1.x - node2.x, 2) + Math.pow(node1.y - node2.y, 2));
    }
    
    /// <summary>
    /// Find Euclidean distance between two points (in meters) 
    ///  - Conversion units - ConfigSettings.METER_2_PIXEL_X, ConfigSettings.METER_2_PIXEL_Y
    /// </summary>
    /// <param name="node1"></param>
    /// <param name="node2"></param>
    /// <returns>distance in meters</returns>
    public static double EuclideanDistanceInMeter(Point node1, Point node2)
    {
        return Math.sqrt(Math.pow((node1.x - node2.x) / ConfigSettings.METER_2_PIXEL_X, 2) + Math.pow((node1.y - node2.y) / ConfigSettings.METER_2_PIXEL_Y, 2));
    }

    /// <summary>
    /// Compute the angle of the directional vector created from preloc and currentloc, using preloc as origin (using real distance in meters
    /// Orientation: 0 = North; 90 = East; 180 = South; 270 = West
    /// Remember: origin of screen is @ left-top corner; x increases as moves to the right; y increases as moves down
    ///  - Conversion units - ConfigSettings.METER_2_PIXEL_X, ConfigSettings.METER_2_PIXEL_Y
    /// Return -1 if the two points are less than MIN_SEPARATION_M apart and useMinCheck==true
    /// </summary>
    /// <param name="preloc"></param>
    /// <param name="currentloc"></param>
    /// <param name="noMin"></param>
    /// <returns></returns>
    public static int ComputeAngle(Point preloc, Point currentloc, boolean useMinCheck)
    {
        double xdiff = (double)currentloc.x - preloc.x;
        double ydiff = (double)currentloc.y - preloc.y;

        double distance = GeometryFunc.EuclideanDistanceInMeter(preloc, currentloc);

        // two nodes are very close to each other, assume they are the same point. --> no angle
        if (useMinCheck && distance < ConfigSettings.MIN_SEPARATION_M)
        {
            return -1;
        }

        int orientationdeg;

        // filter out the horizontal/vertical orientation first
        if (Math.abs(xdiff) < 6 && ydiff > 0)
        {
            orientationdeg = 180;
        }
        else if (Math.abs(xdiff) < 6 && ydiff < 0)
        {
            orientationdeg = 0;
        }
        else if (Math.abs(ydiff) < 6 && xdiff > 0)
        {
            orientationdeg = 90;
        }
        else if (Math.abs(ydiff) < 6 && xdiff < 0)
        {
            orientationdeg = 270;
        }
        else
        {
            // convert back to meters before finding the actual angle.
            xdiff = xdiff / ConfigSettings.METER_2_PIXEL_X;
            ydiff = ydiff / ConfigSettings.METER_2_PIXEL_Y;

            // find the acute angle of the directional vector (using preloc as the origin and currentloc-preloc)                                
            double thetad = Math.round(Math.atan(Math.abs(ydiff) / Math.abs(xdiff)) / Math.PI * 180);
            int theta = (int)thetad;

            // interpret theta as the true orientation
            if (xdiff > 0 && ydiff < 0)
            {
                orientationdeg = 90 - theta;
            }
            else if (xdiff > 0 && ydiff > 0)
            {
                orientationdeg = 90 + theta;
            }
            else if (xdiff < 0 && ydiff < 0)
            {
                orientationdeg = 270 + theta;
            }
            else
            {
                orientationdeg = 270 - theta;
            }
        }
        return orientationdeg;
    }
    
    /// <summary>
    /// Check is a point P is inside a non-rotated box (box is snapped to xy) defined as its two opposide corner: corner1, corner2
    /// tolerance is used to enlarge the box
    /// </summary>
    /// <param name="P"></param>
    /// <param name="corner1"></param>
    /// <param name="corner2"></param>
    /// <param name="tolerance"></param>
    /// <returns></returns>
    public static boolean IsPtInsideNonRotatedBox(Point P, Point corner1, Point corner2, int tolerance)
    {
        int minX = Math.min(corner1.x, corner2.x) - tolerance;
        int maxX = Math.max(corner1.x, corner2.x) + tolerance;
        int minY = Math.min(corner1.y, corner2.y) - tolerance;
        int maxY = Math.max(corner1.y, corner2.y) + tolerance;

        if (minX < 0) { minX = 0; }
        if (minY < 0) { minY = 0; }

        return minX <= P.x && P.x <= maxX && minY <= P.y && P.y <= maxY;
    }

    /// <summary>
    /// Check is a point P is inside a non-rotated box (box is snapped to xy) defined as its 4 corner: corner1, corner2, corner3, corner4
    /// tolerance is used to enlarge the box
    /// </summary>
    /// <param name="P"></param>
    /// <param name="corner1"></param>
    /// <param name="corner2"></param>
    /// <param name="tolerance"></param>
    /// <returns></returns>
    public static boolean IsPtInsideNonRotatedBox(Point P, Point corner1, Point corner2, Point corner3, Point corner4, int tolerance)
    {
        int minX = Math.min(Math.min(corner1.x, corner2.x), Math.min(corner3.x, corner4.x)) - tolerance;
        int maxX = Math.max(Math.max(corner1.x, corner2.x), Math.max(corner3.x, corner4.x)) + tolerance;
        int minY = Math.min(Math.min(corner1.y, corner2.y), Math.min(corner3.y, corner4.y)) - tolerance;
        int maxY = Math.max(Math.max(corner1.y, corner2.y), Math.max(corner3.y, corner4.y)) + tolerance;

        if (minX < 0) { minX = 0; }
        if (minY < 0) { minY = 0; }

        return minX <= P.x && P.x <= maxX && minY <= P.y && P.y <= maxY;
    }

    /// <summary>
    /// Check if a point is inside a rotated rectangle
    /// P is in the rectangle if and only if
    /// 0 leq dot_product(v,v1) leq dot_product(v1,v1) and 0 leq dot_product(v,v2) leq dot_product(v2,v2)
    /// </summary>
    /// <param name="P">the point under test</param>
    /// <param name="Corner">a corner of the rectangle; treat as origin</param>
    /// <param name="v1">a vector that defines a side of rectangle using C as starting point</param>
    /// <param name="v2">another vecotr that defines the other side of rectangle connecting to C</param>
    /// <returns></returns>
    public static boolean IsPointContainsInBox(Point P, Point Corner, DoublePoint v1, DoublePoint v2)
    {
        // find the relateive vector P - corner
        DoublePoint v = new DoublePoint((double)P.x - Corner.x, (double)P.y - Corner.y);

        return (0 <= DotProduct(v, v1) && DotProduct(v, v1) <= DotProduct(v1, v1)) && (0 <= DotProduct(v, v2) && DotProduct(v, v2) <= DotProduct(v2, v2));
    }
    
    /// <summary>
    /// Check if a point is inside a rotated rectangle
    /// P is in the rectangle if and only if
    /// 0 leq dot_product(v,v1) leq dot_product(v1,v1) and 0 leq dot_product(v,v2) leq dot_product(v2,v2)
    /// </summary>
    /// <param name="P"></param>
    /// <param name="origin"></param>
    /// <param name="corner1"></param>
    /// <param name="corner2"></param>
    /// <returns></returns>
    public static boolean IsPointContainsInBox(Point P, Point origin, Point corner1, Point corner2)
    {
        DoublePoint v = new DoublePoint((double)P.x - origin.x, (double)P.y - origin.y);
        DoublePoint v1 = new DoublePoint((double)corner1.x - origin.x, (double)corner1.y - origin.y);
        DoublePoint v2 = new DoublePoint((double)corner2.x - origin.x, (double)corner2.y - origin.y);

        return (0 <= DotProduct(v, v1) && DotProduct(v, v1) <= DotProduct(v1, v1)) && (0 <= DotProduct(v, v2) && DotProduct(v, v2) <= DotProduct(v2, v2));
    }
    /// <summary>
    /// Calculate dot product of two points.
    /// </summary>
    /// <param name="p1"></param>
    /// <param name="p2"></param>
    /// <returns></returns>
    public static double DotProduct(DoublePoint p1, DoublePoint p2)
    {
        return p1.X * p2.X + p1.Y * p2.Y;
    }
}

