/**
 * 
 */
package com.IndoorGPS;

import java.util.ArrayList;
import java.util.List;

import com.IndoorGPS.LocalizerBasicClass.LocResult;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.util.Log;

/**
 * Map view for localization
 */
public class LocalizerMapView extends GPSMapView {

	private Canvas canvas = null;
	private boolean mlocalize = false;
	private boolean mnavigate = false;
	private boolean mtrack = false;
	private boolean firstNavi = false;
	
	private Point mPoint = null;
	private List<Point> mRoutPoints = null;
	private float[] fmRoutPoints = null;
	private Point mPoint_S = null;
	private Point mPoint_D = null;
    private float[] fmPoint_SD = new float[4];
    private Path mPath = new Path();
    private List<Path> _graphics = new ArrayList<Path>();
    private String mText = null;
	
	/**
	 * Constructor 1
	 * @param context
	 * @param image
	 * @param width
	 * @param height
	 * @param paint
	 */
	public LocalizerMapView(Context context, Bitmap image, 
			int width, int height, Paint paint)
	{
		super(context, image, width, height, paint);

	}

	/**
	 * Constructor 2
	 * @param context
	 * @param image
	 * @param width
	 * @param height
	 * @param paint
	 * @param scrollHorizontal
	 * @param scrollVertical
	 */
	public LocalizerMapView(Context context, Bitmap image, int width,
			int height, Paint paint, boolean scrollHorizontal,
			boolean scrollVertical) {
		super(context, image, width, height, paint, scrollHorizontal,
				scrollVertical);
		// TODO Auto-generated constructor stub
		//paint = new Paint();
		//paint.setColor(0xFFFFFF00);
	}
	
	/**
	 * onDraw
	 * draw the map
	 */
	@Override
	protected void onDraw(Canvas canvas)
	{
		//this.canvas = canvas;
		canvas.drawBitmap(bufferImage, 0, 0, mPaint);
		
		if (mlocalize){
			mPaint.setColor(0xFFFF00FF);
			drawMarker(canvas);
		}
		if (mtrack){
			mPaint.setColor(0xFFFF00FF);
			drawMarker(canvas);
		}
		if (mnavigate){
			if(firstNavi == true)
			{				
				mPaint.setStrokeWidth(8);
				mPaint.setAntiAlias(true);
				mPaint.setColor(0xFFFF00FF);
				drawMarker(canvas);
				canvas.save();
				
				mPaint.setStrokeWidth(3);
				mPaint.setColor(Color.BLUE);
				drawLine(canvas);
				canvas.save();
			}
			else
			{
				mPaint.setColor(0xFFFF00FF);
				drawMarker(canvas);
				canvas.save();
			}
		}
		//
	}
	
	
	/**
	 * draw a point on the map
	 */
	@Override
	public void drawMarker(Canvas canvas)
	{
		//assert(mPoint != null);
		
		Log.v("loc map view", " in loc map view");
		
		if(mlocalize == true)
		{	
			//canvas.drawBitmap(mRotatedMarker, mPoint.x, mPoint.y, mPaint);
			canvas.drawCircle(level*(mPoint.x) - scrollX, level*(mPoint.y) - scrollY, 5.0f, mPaint);
		}
		else if(mtrack == true)
		{
			canvas.drawCircle(level*(mPoint.x) - scrollX, level*(mPoint.y) - scrollY, 5.0f, mPaint);
			
		}
		else if(mnavigate == true)
		{
			if(firstNavi == true)
			{
				//canvas.drawCircle(mPoint_S.x, mPoint_S.y, 5.0f, mPaint);
				//canvas.save();
				//canvas.drawCircle(mPoint_D.x, mPoint_D.y, 5.0f, mPaint);
				//canvas.save();
				fmPoint_SD[0] = level*(mPoint_S.x) - scrollX;
				fmPoint_SD[1] = level*(mPoint_S.y) - scrollY;
				fmPoint_SD[2] = level*(mPoint_D.x) - scrollX;
				fmPoint_SD[3] = level*(mPoint_D.y) - scrollY;
				canvas.drawPoints(fmPoint_SD, mPaint);
			}
			else
			{
				canvas.drawCircle(level*(mPoint.x) - scrollX, level*(mPoint.y) - scrollY, 5.0f, mPaint);
				canvas.save();
				mPaint.setColor(Color.BLACK);
				mPaint.setStrokeWidth(2);
				canvas.drawText(mText, level*(mPoint.x) - scrollX, level*(mPoint.y) - scrollY, mPaint);
			}
		}
	}
	/**
	 * draw a line on the map
	 */
	@Override
	public void drawLine(Canvas canvas){
		if(mnavigate == true)
		{
			if(firstNavi == true)
			{
				fmRoutPoints = new float [4*(mRoutPoints.size()-2) + 4];
				
				fmRoutPoints[0] = level*(mRoutPoints.get(0).x) - scrollX;
				fmRoutPoints[1] = level*(mRoutPoints.get(0).y) - scrollY;
				for(int i = 1; i < mRoutPoints.size()-1; i++)
				{	
					fmRoutPoints[4*i - 2] = level*(mRoutPoints.get(i).x) - scrollX;
					fmRoutPoints[4*i - 1] = level*(mRoutPoints.get(i).y) - scrollY;
					fmRoutPoints[4*i] = level*(mRoutPoints.get(i).x) - scrollX;
					fmRoutPoints[4*i + 1] = level*(mRoutPoints.get(i).y) - scrollY;
				}
				fmRoutPoints[4*(mRoutPoints.size()-2) + 2] = level*(mRoutPoints.get(mRoutPoints.size()-1).x) - scrollX;
				fmRoutPoints[4*(mRoutPoints.size()-2) + 3] = level*(mRoutPoints.get(mRoutPoints.size()-1).y) - scrollY;
				
				canvas.drawLines(fmRoutPoints, mPaint);
			}
		}
	}
	
	public void drawPath(Canvas canvas){
		
		// add them to path
		mPath.moveTo(level*mRoutPoints.get(0).x - scrollX, level*mRoutPoints.get(0).y - scrollY);
		for(int i = 1; i < mRoutPoints.size(); i++)
		{
			mPath.lineTo(level*mRoutPoints.get(i).x - scrollX, level*mRoutPoints.get(i).y - scrollY);
			_graphics.add(mPath);
		}
		
		for (Path path : _graphics) 
		{
		    canvas.drawPath(path, mPaint);
		}
	}
	
	///////////////////////////////////////////////////////////////////////
	
	public void setLocalize(boolean set){
		mlocalize = set;
	}
	
	public void setTrack(boolean set){
		mtrack = set;
	}
	
	public void setNavigate(boolean set){
		mnavigate = set;
	}
	
	public void setPoint(Point point){
		mPoint = point;
	}
	
	public void setFirstNavi(boolean set){
		firstNavi = set;
	}
	public void setPoints(Point p1, Point p2){
		mPoint_S = p1;
		mPoint_D = p2;

	}
	
	public void setRoutePoints(List<Point> points){
		mRoutPoints = points;
	}
	
	public void setText(String text){
		mText = text;
	}
	
	public void Reset()
	{
		mnavigate = false;
		mtrack = false;
		mlocalize = false;
	}

}
