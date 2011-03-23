package stetson.CTF;

import java.util.List;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.OverlayItem;

public class StetsonCTF extends MapActivity {
	
	/**
	  * Runs when the activity is started (by Android)
	  * @param saved instance state
	  */
    public void onCreate(Bundle savedInstanceState) {
        
    	super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        // Turns on built-in zoom controls
        MapView mapView = (MapView) findViewById(R.id.mapView);
        mapView.setBuiltInZoomControls(true);
        
        // Setting up the overlays class
        List<Overlay> mapOverlays = mapView.getOverlays();
        Drawable drawable = this.getResources().getDrawable(R.drawable.person_red);
        ItemizedOverlays itemizedoverlay = new ItemizedOverlays(drawable);
        
        // Adding a marker to the map
        GeoPoint point = new GeoPoint(19240000,-99120000);
        OverlayItem overlayitem = new OverlayItem(point, "Hola, Mundo!", "I'm in Mexico City!");
        itemizedoverlay.addOverlay(overlayitem);
        mapOverlays.add(itemizedoverlay);
    }

	/**
	 * Returns false (required by MapActivity)
	 * @return false
	 */
	protected boolean isRouteDisplayed() {
		return false;
	}
	
}