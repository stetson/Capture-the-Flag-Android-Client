package stetson.CTF;

import java.util.List;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.OverlayItem;

public class StetsonCTF extends MapActivity {


	private LocationManager locationManager;
	private LocationListener locationListener;
	private GeoPoint playerPoint;
	MapController mapController;
	ItemizedOverlays itemizedoverlay;
	OverlayItem overlayitem;
	List<Overlay> mapOverlays;


	/**
	 * Runs when the activity is started (by Android)
	 * @param saved instance state
	 */
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		
		// Turns on built-in zoom controls
		MapView mapView = (MapView) findViewById(R.id.mapView);
		mapController = mapView.getController();
		mapView.setBuiltInZoomControls(true);
		
		getLocation();
		
		
		// Setting up the overlays class
		mapOverlays = mapView.getOverlays();
		Drawable drawable = this.getResources().getDrawable(R.drawable.person_red);
		itemizedoverlay = new ItemizedOverlays(drawable);

		// Adding a marker to the map
		if(playerPoint != null)
		{
		overlayitem = new OverlayItem(playerPoint, "Hola, Mundo!", "I'm in Mexico City!");
		itemizedoverlay.addOverlay(overlayitem);
		mapOverlays.add(itemizedoverlay);
		}
	}

	/**
	 * Returns false (required by MapActivity)
	 * @return false
	 */
	protected boolean isRouteDisplayed() {
		return false;
	}

	protected boolean updateLocation()
	{
		mapController.setCenter(playerPoint);
		overlayitem = new OverlayItem(playerPoint, "Hola, Mundo!", "I'm in Mexico City!");
		itemizedoverlay.addOverlay(overlayitem);
		return true;
	}
	
	protected void getLocation()
	{
		locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

		// Define a listener that responds to location updates
		locationListener = new LocationListener() {
			public void onLocationChanged(Location location) {
				// Called when a new location is found by the network location provider.
				playerPoint = new GeoPoint(  (int)(1E6 *location.getLatitude()),  (int)(1E6 *location.getLongitude()));
				updateLocation();

			}

			public void onStatusChanged(String provider, int status, Bundle extras) {}

			public void onProviderEnabled(String provider) {}

			public void onProviderDisabled(String provider) {}
		};

		// Register the listener with the Location Manager to receive location updates
		//       locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,3, 0, locationListener);

		playerPoint = new GeoPoint((int)(1E6 *locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER).getLatitude()),(int)(1E6 *locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER).getLongitude()));

		while(locationManager.getProvider(LocationManager.GPS_PROVIDER).getAccuracy()!= android.location.Criteria.ACCURACY_FINE)
		{
			playerPoint = new GeoPoint((int)(1E6 *locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER).getLatitude()),(int)(1E6 *locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER).getLongitude()));

		}


		mapController.setCenter(playerPoint);



	}

}