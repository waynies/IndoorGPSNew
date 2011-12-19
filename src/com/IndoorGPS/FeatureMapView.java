package com.IndoorGPS;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.IndoorGPS.LocalizerBasicClass.ConfigSettings;
import com.IndoorGPS.LocalizerBasicClass.DB;

import android.R.color;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;

public class FeatureMapView extends GPSMapView{

	private Canvas canvas = null;
	private Point mPoint = null;
    private String mText = null;
    
    private List<Point> mRlistPoint = new ArrayList<Point>();
    private List<Integer> mCIndex = new ArrayList<Integer>();
    
    private List<Point> mMFPoints = new ArrayList<Point>();
    
    private boolean mdrawCluster = false;
    private boolean mdrawMF = false;
    
	/**
	 * Constructor 1
	 * @param context
	 * @param image
	 * @param width
	 * @param height
	 * @param paint
	 */
	public FeatureMapView(Context context, Bitmap image, 
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
	public FeatureMapView(Context context, Bitmap image, int width,
			int height, Paint paint, boolean scrollHorizontal,
			boolean scrollVertical) {
		super(context, image, width, height, paint, scrollHorizontal,
				scrollVertical);
	
	}
	
	
	/**
	 * onDraw
	 * draw the map
	 */
	@Override
	protected void onDraw(Canvas canvas)
	{	
		canvas.drawBitmap(bufferImage, 0, 0, mPaint);
		// pin image
		canvas.drawBitmap(pin.getBitmap(), pin.getPosition().x, pin.getPosition().y, mPaint);
		
		mPaint.setStrokeWidth(1);
		
		if(mdrawCluster){
			for (int i = 1; i <= mRlistPoint.size(); i++)
			{
				float x, y;
				Point refpt = new Point();

				refpt = mRlistPoint.get(i - 1);
				x = level*refpt.x - scrollX;
				y = level*refpt.y - scrollY;

				if (mCIndex.size() > 0)
				{
					mPaint.setColor(ConfigSettings.FP_COLOR_LIST.get(mCIndex.get(i - 1) % 16));
				}
				else
				{
					mPaint.setColor(ConfigSettings.FP_COLOR_STD);
				}

				canvas.drawCircle(x, y, 5.0f, mPaint);
				canvas.save();
				mPaint.setColor(Color.BLACK);
				canvas.drawText(i + "", x, y, mPaint);
				canvas.save();

			}
		}else if(mdrawMF){

			//draw mapfeature points
			for(int i=0; i<mMFPoints.size();i++){
				float x, y;
				Point refpt = new Point();

				refpt = mMFPoints.get(i);
				x = level*refpt.x - scrollX;
				y = level*refpt.y - scrollY;

				mPaint.setColor(Color.BLUE);
				canvas.drawCircle(x, y, 5.0f, mPaint);
				canvas.save();
				mPaint.setColor(Color.BLACK);
				canvas.drawText(i + "", x, y, mPaint);
				canvas.save();
			}
		}

	}
	
	/**
	 * draw a point on the map
	 */
	@Override
	public void drawMarker(Canvas canvas)
	{
		canvas.drawCircle(level*mPoint.x-scrollX, level*mPoint.y-scrollY, 5.0f, mPaint);
	}
	
	public void setPoint(Point point){
		mPoint = point;
	}
	
	public void setText(String text){
		mText = text;
	}
	
	public void setRPoints(List<Point> listPoint){
		mRlistPoint = listPoint;
	}
	public void setCIndex(List<Integer> listInteger){
		mCIndex = listInteger;
	}
	
	public void Reset()
	{
		mRlistPoint.clear();
		mMFPoints.clear();
		mCIndex.clear();
		mdrawCluster = false;
		mdrawMF = false;
	}
	
	public void setMFPoints(List<Point> listPoint){
		mMFPoints = listPoint;
	}

	public void setDrawCluster(boolean drawcluster){
		mdrawCluster = drawcluster;
	}
	
	public void setDrawMF(boolean drawmf){
		mdrawMF = drawmf;
	}
}
