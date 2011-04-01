package stetson.CTF;

import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;

public class Boundaries extends Overlay {
	
	public GeoPoint redTopLeftBoundary;
	public GeoPoint redBottomRightBoundary;
	public GeoPoint blueTopLeftBoundary;
	public GeoPoint blueBottomRightBoundary;

	public void setRedBounds(GeoPoint topLeft, GeoPoint bottomRight)
	{
		redTopLeftBoundary = topLeft;
		redBottomRightBoundary = bottomRight;
	}
	public void setBlueBounds(GeoPoint topLeft, GeoPoint bottomRight)
	{
		blueTopLeftBoundary = topLeft;
		blueBottomRightBoundary = bottomRight;
	}
	
	public void draw(android.graphics.Canvas canvas, MapView mapView, boolean shadow) 
	{ 
		
		// Draw Red Rectangle
		
		Paint redPaint = new Paint();
		redPaint.setColor(Color.RED);
		redPaint.setAlpha(40);
		Point redTopLeft = new Point(); 
		Point redBottomRight = new Point();
		mapView.getProjection().toPixels(redTopLeftBoundary,redTopLeft);
		mapView.getProjection().toPixels(redBottomRightBoundary,redBottomRight);
		canvas.drawRect(new Rect(redTopLeft.x,redTopLeft.y,redBottomRight.x,redBottomRight.y), redPaint);
		
		// Draw Blue Rectangle
		
		Paint bluePaint = new Paint();
		bluePaint.setColor(Color.BLUE);
		bluePaint.setAlpha(40);
		Point blueTopLeft = new Point(); 
		Point blueBottomRight = new Point();
		mapView.getProjection().toPixels(blueTopLeftBoundary,blueTopLeft);
		mapView.getProjection().toPixels(blueBottomRightBoundary,blueBottomRight);
		
		canvas.drawRect(new Rect(blueTopLeft.x,blueTopLeft.y,blueBottomRight.x,blueBottomRight.y), bluePaint);
		
	} 
}
