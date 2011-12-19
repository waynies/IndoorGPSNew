package com.IndoorGPS;
/*
 * TODO: completely remove display metrics if it there's no display 
 * density issue across different display devices
 * 
 * */

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.Date;
import java.util.List;


import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Bitmap.CompressFormat;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.ZoomButtonsController;
import android.widget.ZoomButtonsController.OnZoomListener;

//*************************************************************************
//
//					Scrollable map view class
//
//*************************************************************************
class GPSMapView extends View
{
	private final String TAG = "GPSMapView->";
	
	// for scroll
	private  boolean scrollHorizontalEnabled = true;
	private boolean scrollVerticalEnabled = true;
	
	private int scrollRate = 15;
	protected int scrollX = 0;
	protected int scrollY = 0;
	
	private float scrollDistance = 15;
	
	// for zoombuttoncontroller
	private float zoomControlerRate = (float) 1.2;
	private ZoomButtonsController zoomctrl;
	private boolean canZoomOut = true;

	// for multi-touch
	protected int numclick = 0;
    protected int numlongpress = 0;
	protected boolean numdoubleclickzoom = true;
	
	private Point fg1_d = new Point();
	private Point fg2_d = new Point();
	private Point fg1_u = new Point();
	private Point fg2_u = new Point();
	private float dis_d = 0;
	private float dis_u = 0;
	
	private float myOldX = 0;
	private float myOldY = 0;
	private float distanceX = 0;
	private float distanceY = 0;
	private long downtime = 0;
	private long doubleclicktime = 0;
	private long doubleclicktime_previous = 0;
	private String mode = "normal";
	
	////
	
	protected Bitmap image;
	protected Bitmap bufferImage;
	protected Bitmap imagezoom;
	private Bitmap mMarkerImage;
	protected Bitmap mRotatedMarker;
	private Matrix mMarkerMatrix;
	private Matrix mBitMapmatrix;
	protected float level;
	protected Pin pin;
	//private DisplayMetrics dm;
	
	private int maxWidth;      
	private int maxHeight;

	private int pictureWidth;
	private int pictureHeight;
	private float[] mValues = {(float) 0.0};
	
	////


	protected Paint mPaint;
	private GestureDetector gestureScanner;

	// location marker
	//private LocationMarker mLocationMarker;
	
	//**************************************************
	// CONSTRUCTORS 
	//**************************************************
	//
	// Constructor 1
	//
	public GPSMapView(Context context, Bitmap image, 
			int width, int height, Paint mPaint)
	{
		super(context);
		setFocusable(true);		// necessary for getting the touch events

		this.image = image;
		this.imagezoom = image;
		this.mPaint = mPaint;
		this.numclick = 0;
		this.numlongpress = 0;
		this.numdoubleclickzoom = true;
		
		bufferImage = Bitmap.createBitmap(image);
		mMarkerImage = BitmapFactory.decodeResource(getResources(), R.drawable.icon_my_location);
		mMarkerMatrix = new Matrix();
		mBitMapmatrix = new Matrix();
		mRotatedMarker = Bitmap.createBitmap(mMarkerImage, 0, 0, mMarkerImage.getWidth(), mMarkerImage.getHeight(), mMarkerMatrix, true);

		level = 1;
		
		calculateSize(width, height);
		createZoomControlListener();
		createGestureListener();
		
		// setup the pin
		Point pinLocation = new Point(0, 0);
		
		pin = new Pin(context, R.drawable.pin, pinLocation);
	}

	//******************************************************=
	//
	// Constructor 2
	//
	// @ with scroll enable and disable
	//******************************************************
	public GPSMapView(Context context, Bitmap image,
			int width, int height, Paint mPaint,
			boolean scrollHorizontal, boolean scrollVertical)
	{
		super(context);
		setFocusable(true);		// necessary for getting the touch events
		
		Matrix imagematrix = new Matrix();
		float scaleFactorx = (float) width/image.getWidth();
		float scaleFactory = (float) height/image.getHeight();
		
		imagematrix.postScale(scaleFactorx, scaleFactory);
		
		Bitmap imagetemp = Bitmap.createBitmap(image, 0, 0, image.getWidth(), image.getHeight(), imagematrix, true);
		this.image = imagetemp;
		this.imagezoom = imagetemp;
		
		//this.image = image;
		//this.imagezoom = image;
		this.mPaint = mPaint;
		this.numclick = 0;
		this.numlongpress = 0;
		this.numdoubleclickzoom = true;
		
		bufferImage = Bitmap.createBitmap(image);
		mMarkerImage = BitmapFactory.decodeResource(getResources(), R.drawable.arrow);
		mMarkerMatrix = new Matrix();
		mBitMapmatrix = new Matrix();
		mRotatedMarker = Bitmap.createBitmap(mMarkerImage, 0, 0, mMarkerImage.getWidth(), mMarkerImage.getHeight(), mMarkerMatrix, true);
		
		level = 1;
		
		calculateSize(width, height);
		createZoomControlListener();
		createGestureListener();
		
		// setup the pin
		Point pinLocation = new Point(0, 0);
		
		pin = new Pin(context, R.drawable.pin, pinLocation);
		
		// set scrolling
		this.scrollHorizontalEnabled = scrollHorizontal;
		this.scrollVerticalEnabled = scrollVertical;
	}
	
	//******************************************************=
	//
	// Constructor 3
	//
	// @ with scroll enable and disable
	// @ with pin visible
	//******************************************************
	public GPSMapView(Context context, Bitmap image,
			int width, int height, Paint mPaint,
			boolean scrollHorizontal, boolean scrollVertical, Pin pin)
	{
		super(context);
		setFocusable(true);		// necessary for getting the touch events
		
		this.image = image;
		this.imagezoom = image;
		this.mPaint = mPaint;
		this.numclick = 0;
		this.numlongpress = 0;
		this.numdoubleclickzoom = true;
		
		bufferImage = Bitmap.createBitmap(image);
		mMarkerImage = BitmapFactory.decodeResource(getResources(), R.drawable.arrow);
		mMarkerMatrix = new Matrix();
		mBitMapmatrix = new Matrix();
		mRotatedMarker = Bitmap.createBitmap(mMarkerImage, 0, 0, mMarkerImage.getWidth(), mMarkerImage.getHeight(), mMarkerMatrix, true);
		
		level = 1;
		
		calculateSize(width, height);
		createZoomControlListener();
		createGestureListener();
		
		this.pin = pin;
		
		this.pin.setX(30);
		this.pin.setY(30);
		
		// set scrolling
		this.scrollHorizontalEnabled = scrollHorizontal;
		this.scrollVerticalEnabled = scrollVertical;
		
	}
	//******************************************************
	//
	// create zoomControl listener for this view
	//
	//******************************************************
	protected void createZoomControlListener(){
		zoomctrl = new ZoomButtonsController(this);
		zoomctrl.setVisible(true); 
		zoomctrl.setFocusable(true);
		zoomctrl.setZoomInEnabled(true);
		zoomctrl.setZoomOutEnabled(true);
		zoomctrl.setAutoDismissed(false);
		
		zoomctrl.setOnZoomListener(new OnZoomListener(){

			@Override
			public void onVisibilityChanged(boolean visible) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void onZoom(boolean zoomIn) {
				// TODO Auto-generated method stub
				
				if(zoomIn){
					HandleOnZoom(true,0,0);
				}else{
					HandleOnZoom(false,0,0);
				}
			}
			
		});
	}
	
	//******************************************************
	//
	// create onTouchEvent for this view
	//
	//******************************************************
	//@Override
	public boolean onTouchEvent(MotionEvent event) {
		// TODO Auto-generated method stub

		final int actionType = (event.getAction() & MotionEvent.ACTION_MASK);
		switch (actionType){
		case MotionEvent.ACTION_DOWN:
			
			HandleOnDown(event);
			
			myOldX = event.getX();
			myOldY = event.getY();
			downtime = event.getDownTime();
			
			break;
			
		case MotionEvent.ACTION_UP:	
			
			distanceX = event.getX()-myOldX;
			distanceY = event.getY()-myOldY;
			
			downtime = event.getEventTime()-downtime;
			
			if(downtime > 1000 && Math.abs(distanceX) < scrollDistance && Math.abs(distanceY) < scrollDistance){
				HandleOnLongPress(event.getX(),event.getY());
				mode = "normal";
				
			}else if(downtime < 1000 && Math.abs(distanceX) < scrollDistance && Math.abs(distanceY) < scrollDistance){
				HandleOnSingleTapUp();
				mode = "normal";
				
			}
			
			break;
		case MotionEvent.ACTION_POINTER_DOWN:
			fg1_d.x = (int) event.getX(0);
			fg1_d.y = (int) event.getY(0);
			
			fg2_d.x = (int) event.getX(1);
			fg2_d.y = (int) event.getY(1);
			
			dis_d = spacing(event);
	
			break;
		case MotionEvent.ACTION_POINTER_UP:
			
			
			dis_u = spacing(event);
			
			if(dis_d > 10f && dis_u > dis_d){
				mode = "zoomin";
			}else if(dis_d > 10f && dis_u < dis_d){
				mode = "zoomout";
			}
			
		case MotionEvent.ACTION_MOVE:
			
			if(mode.equalsIgnoreCase("zoomin")){
				HandleOnZoom(true,(float)(fg1_d.x + fg2_d.x)/2,(float)(fg1_d.y + fg2_d.y)/2);
				mode = "normal";
			}else if(mode.equalsIgnoreCase("zoomout")){
				HandleOnZoom(false,(float)(fg1_d.x + fg2_d.x)/2,(float)(fg1_d.y + fg2_d.y)/2);
				mode = "normal";
			}
			else{
				distanceX = event.getX()-myOldX;
				distanceY = event.getY()-myOldY;
				
				if(Math.abs(distanceX) > 6f && Math.abs(distanceY) > 6f){
					HandleOnScroll((int)event.getX(),(int)event.getY(),myOldX-event.getX(), myOldY - event.getY());
					mode = "normal";
				}
			}
			break;
		default:
			break;
		}		
		return true;
	}
	
	//******************************************************
	//
	// create gesture listener for this view
	//
	//******************************************************
	protected void createGestureListener()
	{
		setGestureScanner(new GestureDetector(new OnGestureListener()
		{
			public boolean onScroll(MotionEvent event1, MotionEvent event2, float distanceX, float distanceY) 
			{
				HandleOnScroll((int)event2.getX(), (int)event2.getY(), distanceX, distanceY);	
				return true;
			}

			public boolean onDown(MotionEvent event)
			{				
				HandleOnDown(event);				
				return true;
			}

			public boolean onFling(MotionEvent event1, MotionEvent event2, float velocityX, float velocityY)
			{
				// do nothing
				return true;
			}

			public void onLongPress(MotionEvent event)
			{
				HandleOnLongPress(event.getX(),event.getY());
			}

			public void onShowPress(MotionEvent event)
			{
			}

			public boolean onSingleTapUp(MotionEvent event)
			{
				HandleOnSingleTapUp();
				return true;
			}
		}));
	}

	/***********************************************************************
	 * *  Private Functions
	************************************************************************/
	private float spacing(MotionEvent event) {
		//distance between two fingers. Use event.getX(i) for the ith finger
		float x = event.getX(0) - event.getX(1);
		float y = event.getY(0) - event.getY(1);
		return (float) Math.sqrt(x * x + y * y);
	}
	
	private void HandleOnDown(MotionEvent event){
		long doubleclicktime_current = event.getEventTime();
		doubleclicktime = doubleclicktime_current - doubleclicktime_previous;
		doubleclicktime_previous = doubleclicktime_current;
		
		Log.d("onDown ->", "entry");
		
		// check if inside the bound of pin, approximate the pin as a circle
		pin.setPinSelected(false);
		
		int centerX = pin.getCenterX();
		int centerY = pin.getCenterY();

		int XContact = (int)event.getX();
		int YContact = (int)event.getY();
		
		// for pin move
		double contactDistance = Math.sqrt( ((centerX - XContact)*(centerX - XContact) + (centerY - YContact)*(centerY - YContact)));

		if(contactDistance < pin.getRadius())
		{
			Log.d("onDown->", "on pin");
			pin.setPinSelected(true);
			pin.setScrollOffset(scrollX, scrollY);
		}
		
		// for zoomButtonController visibility
		numclick++;
		if(numclick%2==1){
			zoomctrl.setVisible(true);
		}else{
			zoomctrl.setVisible(false);
		}
		
		// for double click
		if(doubleclicktime < 500){
			if(numdoubleclickzoom){
				HandleOnZoom(true,event.getX(),event.getY());
				numdoubleclickzoom = false;
			}else{
				HandleOnZoom(false,event.getX(),event.getY());
				numdoubleclickzoom = true;
			}
		}
	}
	
	
	private void HandleOnLongPress(float x, float y){
		Log.d("OnLongPress-->","entry");
		/*numlongpress++;
		if(numlongpress%2==1){
			HandleOnZoom(true, x,y);
		}else{
			HandleOnZoom(false,x,y);
		}*/
		PrintScreen();
	}
	
	private void PrintScreen(){
		File root = Environment.getExternalStorageDirectory();	
		
		this.setDrawingCacheEnabled(true);
		Bitmap b = this.getDrawingCache();
		
		if(root.canWrite() == true)
		{
			Date date = new Date();
			long hour = date.getHours();
			long minute = date.getMinutes();
			long second = date.getSeconds();

			String fileName = String.format("%s/Maps/image_%sh%sm%ss.jpg", root,hour,minute,second);
		
			try {
				b.compress(CompressFormat.JPEG, 95, new FileOutputStream(fileName));
				Utilities.displayMsgBox("Print Screen", "current image is saved under SDCard/Maps");
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	private void HandleOnScroll(int x, int y, float dx, float dy){
		// handle map scrolling
		Log.d("onScroll->", "entry");
		if(pin.isPinSelected() == true)
		{
			//move pin
			pin.setCenter(x-30, y+20, scrollX, scrollY);
			
			invalidate();
		}
		else
		{
			handleScroll(dx, dy);
		}
	}
	
	private void HandleOnZoom(boolean zoom, float px, float py){
		if(zoom){
			Matrix imagezoomMatrix = new Matrix();
			imagezoomMatrix.postScale(zoomControlerRate, zoomControlerRate);

			mBitMapmatrix.postScale(zoomControlerRate, zoomControlerRate);
			level = level * zoomControlerRate;

			//bufferImage = Bitmap.createBitmap(image, scrollX, scrollY, maxWidth, maxHeight, mBitMapmatrix, true);					
			calculateReSize();
			
			if(imagezoom.getWidth()> (int) (scrollX+px-maxWidth/2)+ maxWidth && imagezoom.getHeight()> (int)(scrollY+py-maxHeight/2)+ maxHeight
					&& (scrollX+px-maxWidth/2) > 0 && (scrollY+py-maxHeight/2)>0){
				scrollX += (int) (px - maxWidth/2);
				scrollY += (int) (py - maxHeight/2);
			}
			
			bufferImage = Bitmap.createBitmap(imagezoom, scrollX, scrollY, maxWidth, maxHeight);

			invalidate();
		}else{
			Matrix imagezoomMatrix = new Matrix();
			imagezoomMatrix.postScale(1/zoomControlerRate, 1/zoomControlerRate);

			//bufferImage = Bitmap.createBitmap(image, scrollX, scrollY, maxWidth, maxHeight, mBitMapmatrix, true);
			if(image.getWidth()*level/zoomControlerRate-scrollX < maxWidth || image.getHeight()*level/zoomControlerRate-scrollY < maxHeight){
				canZoomOut = false;
			}else{
				canZoomOut = true;
			}
			if(level>1 && canZoomOut)
			{
				mBitMapmatrix.postScale(1/zoomControlerRate, 1/zoomControlerRate);
				level = level * (1/zoomControlerRate);

				calculateReSize();
				
				if(imagezoom.getWidth()> (int) (scrollX+px-maxWidth/2)+ maxWidth && imagezoom.getHeight()> (int)(scrollY+py-maxHeight/2)+ maxHeight
						&& (scrollX+px-maxWidth/2) > 0 && (scrollY+py-maxHeight/2)>0){
					scrollX += (int) (px - maxWidth/2);
					scrollY += (int) (py - maxHeight/2);
				}
				
				bufferImage = Bitmap.createBitmap(imagezoom, scrollX, scrollY, imagezoom.getWidth()-scrollX, imagezoom.getHeight()-scrollY);
			}else{
				mBitMapmatrix = new Matrix();
				level = 1;
				scrollX = 0;
				scrollY = 0;
				
				calculateReSize();
				
				bufferImage = Bitmap.createBitmap(imagezoom,0,0,maxWidth, maxHeight);
			
			}
				
			invalidate();	
		}
	}
	
	private void HandleOnSingleTapUp(){
		Log.d("onSingleTapUp-->","entry");				
		
		Log.d(TAG, "tip: " + pin.getPinTip().x + " " + pin.getPinTip().y);
		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getContext());
		int numSample = new Integer(sharedPref.getString("sampleCount", "3"));
		boolean fourOTraining = sharedPref.getBoolean("4oTraining", true);
		Log.d(TAG, "each ap has " + numSample + " samples");
		Log.d(TAG, "Four orienation training: " + fourOTraining);
	}
	
	/***********************************************************************
	 * *  Other Protected Functions
	************************************************************************/


	//******************************************************
	//			calculateSize
	//
	// calculate sizes to set layout parameter
	//
	// width and height = screen dimensions
	//******************************************************
	protected void calculateSize(int width, int height)
	{

		//picture size
		pictureWidth = image.getWidth();
		pictureHeight = image.getHeight();

		//window size
		maxWidth = Math.min(pictureWidth, width);
		maxHeight = Math.min(pictureHeight, height);

		//layout size
		setLayoutParams(new LayoutParams(pictureWidth, pictureHeight));
	}
	
	protected void calculateReSize(){
		
		//picture size
		imagezoom = Bitmap.createBitmap(image, 0, 0, image.getWidth(), image.getHeight(), mBitMapmatrix, true);
		
		pictureWidth = imagezoom.getWidth();
		pictureHeight = imagezoom.getHeight();

		//window size
		maxWidth = Math.min(pictureWidth, maxWidth);
		maxHeight = Math.min(pictureHeight, maxHeight);
		
	}
	//******************************************************
	//
	// onDraw
	//
	// @brief: draw the map image and a pin on the given canvas
	//******************************************************
	@Override
	protected void onDraw(Canvas canvas)
	{
		// map image
		canvas.drawBitmap(bufferImage, 0, 0, mPaint);
		
		// pin image
		canvas.drawBitmap(pin.getBitmap(), pin.getPosition().x, pin.getPosition().y, mPaint);
		
		//marker image
		canvas.drawBitmap(mRotatedMarker, level*(259 - scrollX/level), level*(264 - scrollY/level), mPaint);
		//canvas.drawBitmap(mRotatedMarker, 259, 264, mPaint);
	}

	//******************************************************
	//
	// 	handleScroll
	//
	//******************************************************
	protected void handleScroll(float distX, float distY)
	{
		Log.d("Handle Scroll->", "entry");
		
		int maxScrollX = Math.max(pictureWidth - maxWidth, 0);
		int maxScrollY = Math.max(pictureHeight - maxHeight, 0);

		//X-Axis
		if(scrollHorizontalEnabled)
		{
			if(distX > scrollDistance)
			{
				if (scrollX < maxScrollX - scrollRate)
				{
					scrollX += scrollRate;
				}
				else
				{
					scrollX = maxScrollX;
				}
			}
			else if(distX < -scrollDistance)
			{
				if (scrollX >= scrollRate)
				{
					scrollX -= scrollRate;
				}
				else
				{
					scrollX = 0;
				}
			}
		}

		//Y-AXIS
		if(scrollVerticalEnabled)
		{
			if (distY > scrollDistance)
			{
				if (scrollY < maxScrollY - scrollRate)
				{
					scrollY += scrollRate;
				}
				else
				{

				}
			}
			else if (distY < -scrollDistance)
			{
				if (scrollY >= scrollRate)
				{
					scrollY -= scrollRate;
				}
				else
				{
					scrollY = 0;
				}
			}
		}

		//Swap image
		if ((scrollX <= maxWidth) && (scrollY <= maxHeight))
		{
			swapImage();
			invalidate();
		}
	}

	//******************************************************
	//
	// swapImage
	//
	//******************************************************
	protected void swapImage()
	{
		Log.d("swap image->", "entry");
		//bufferImage = Bitmap.createBitmap(image, scrollX, scrollY, maxWidth, maxHeight);
		//bufferImage = Bitmap.createBitmap(image, scrollX, scrollY, maxWidth, maxHeight,mBitMapmatrix,true);
		bufferImage = Bitmap.createBitmap(imagezoom, scrollX, scrollY, maxWidth, maxHeight);
	}
	
	//******************************************************
	//
	// 	detached
	//
	//******************************************************
	@Override
	protected void onDetachedFromWindow() {
		// TODO Auto-generated method stub
		super.onDetachedFromWindow();
		zoomctrl.setVisible(false);
	}

	/***********************************************************************
	 * *  Public Functions
	************************************************************************/

	//******************************************************
	//				getPinTip
	//@brief: report x and y to database during training
	//******************************************************
	public Point getPinTip()
	{
		Point pinLoc = new Point();
		pinLoc.x = (int) (pin.getPinTip().x/level);
		pinLoc.y = (int) (pin.getPinTip().y/level);
		return pinLoc;
		//return pin.getPinTip();
	}
	
	//******************************************************
	//				setAzimuth
	//@brief: set the new angle between the magnetic north
	// 	and the y axis
	//******************************************************
	public void setAzimuth(float[] newValues)
	{
		mValues = newValues;
	
		mMarkerMatrix.setRotate(-mValues[0], mMarkerImage.getWidth()/2, mMarkerImage.getHeight()/2);
		mRotatedMarker = Bitmap.createBitmap(mMarkerImage, 0, 0, mMarkerImage.getWidth(), mMarkerImage.getHeight(), mMarkerMatrix, true);
	}
	
	/**
	 * @return the gestureScanner
	 */
	public GestureDetector getGestureScanner()
	{
		return gestureScanner;
	}

	/**
	 * @param gestureScanner the gestureScanner to set
	 */
	public void setGestureScanner(GestureDetector gestureScanner)
	{
		this.gestureScanner = gestureScanner;
	}
	/**
	 * abstract method for drawing marker, implemented in LocalizerMapView
	 */
	public void drawMarker(Canvas canvas){
	}
	public void drawLine(Canvas canvas){
	}
	/**
	 * abstract
	 * @param set
	 */
	public void setLocalize(boolean set){
	}
	
	public void setTrack(boolean set){	
	}
	public void setNavigate(boolean set){
	}
	/**
	 * 
	 */
	public void setPoint(Point point){
	}

	public void setFirstNavi(boolean set){
	}
	public void setPoints(Point point1, Point point2){
	}
	public void setRoutePoints(List<Point> points){	
	}
	public void setText(String text){
	}
	public void setRPoints(List<Point> listPoint){
	}
	
	public void setCIndex(List<Integer> listInteger){
	}
	public void Reset() {
		// TODO Auto-generated method stub	
	}

	public void setDrawCluster(boolean b) {
		// TODO Auto-generated method stub
	}
	public void setMFPoints(List<Point> listPoint){
		
	}
	public void setDrawMF(boolean drawmf){
		
	}
	
	public Bitmap changeView(Bitmap image, int width, int height)
	{		
		Matrix imagematrix = new Matrix();
		float scaleFactorx = (float) width/image.getWidth();
		float scaleFactory = (float) height/image.getHeight();
		
		imagematrix.postScale(scaleFactorx, scaleFactory);
		
		Bitmap imagetemp = Bitmap.createBitmap(image, 0, 0, image.getWidth(), image.getHeight(), imagematrix, true);
		this.image = imagetemp;
		this.imagezoom = imagetemp;
		this.bufferImage = imagetemp;
		
		numclick = 0;
		numlongpress = 0;
		numdoubleclickzoom = true;
		level = 1;
		scrollX = 0;
		scrollY = 0;

		
		return imagetemp;
	}
}