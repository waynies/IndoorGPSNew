package com.IndoorGPS;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.util.Log;

public class Pin
{
	private Bitmap pinImage;		// Image of pin
	private Point imagePos;			// location of pin image on the canvas
	private Point pinTip;			// coordinate of the pin tip at the canvas
	private boolean isPinSelected;	// used to drag the pin
	
	private int pinSizeX;			// size of pin image
	private int pinSizeY;			// size of pin image
	
	private Point scrollVector;		// map scroll x and y, used to handle errors caused by map scrolling
	
	//private DisplayMetrics dm;
	
	//
	// Constructor
	//
	//@brief: create a pin with the drawable resource received at point 0, 0
	public Pin(Context context, int drawable)
	{
		BitmapFactory.Options opts = new BitmapFactory.Options();
		opts.inJustDecodeBounds = true;
		
		pinImage = BitmapFactory.decodeResource(context.getResources(), drawable);
		pinSizeX = pinImage.getWidth();
		pinSizeY = pinImage.getHeight();
		
		imagePos = new Point(0, 0);
		pinTip = new Point(0, 0 + pinSizeY);
		scrollVector = new Point(0, 0);
		
		isPinSelected = false;
	}
	
	
	//
	// Constructor
	//
	//@brief: create a pin with the drawable resource received at the specified point
	public Pin(Context context, int drawable, Point point)
	{
		BitmapFactory.Options opts = new BitmapFactory.Options();
		opts.inJustDecodeBounds = true;
		
		pinImage = BitmapFactory.decodeResource(context.getResources(), drawable);
		pinSizeX = pinImage.getWidth();
		pinSizeY = pinImage.getHeight();
		
		Log.d("pin class", "pin size " + pinSizeX + " " + pinSizeY + " " + pinImage.getDensity());
		imagePos = new Point(point);
		pinTip = new Point(imagePos.x, imagePos.y + pinSizeY);
		scrollVector = new Point(0, 0);
		
		isPinSelected = false;
	}
	
	//------------------------------------------------
	// 				access functions
	//------------------------------------------------
	public boolean isPinSelected()
	{
		return isPinSelected;
	}
	
	public void setPinSelected(boolean newStatus)
	{
		isPinSelected = newStatus;
	}
	
	public void setX(int newValue)
	{
		imagePos.x = newValue;
		pinTip.x = newValue + scrollVector.x;
	}
	
	public void setY(int newValue)
	{
		imagePos.y = newValue;
		pinTip.y = newValue + pinSizeY + scrollVector.y;
	}
	
	public void setScrollOffset(int xOffset, int yOffset)
	{
		scrollVector.x = xOffset;
		scrollVector.y = yOffset;
	}
	
	public void setCenter(int x, int y, int scrollX, int scrollY)
	{
		setX(x - pinSizeX/2);
		setY(y - pinSizeY/2);
	}
	
	public int getRadius()
	{
		return (int)(pinSizeX * 1.414); 
	}
	
	public Point getPosition()
	{
		return imagePos;
	}

	public int getCenterX()
	{
		return (imagePos.x + pinSizeX/2); 
	}
	
	public int getCenterY()
	{
		return (imagePos.y + pinSizeY/2);
	}
	
	public Point getPinTip()
	{	
		return pinTip;
	}
	
	public Bitmap getBitmap()
	{
		return pinImage;
	}
}