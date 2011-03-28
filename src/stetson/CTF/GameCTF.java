package stetson.CTF;

import java.util.List;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.OverlayItem;

public class GameCTF extends MapActivity {
	
	private static final String TAG = "GameCTF";
	private LocationManager locationManager;
	private LocationListener locationListener;
	private GeoPoint playerPoint;
	MapController mapController;
	GameCTFOverlays itemizedoverlay;
	OverlayItem overlayitem;
	List<Overlay> mapOverlays;
	
	/**
	 * Called when the activity is first created.
	 * @param saved instance state
	 */
	public void onCreate(Bundle savedInstanceState) {
		
		// Yay LogCat!
		Log.i(TAG, "Starting map activity...");
		
		// Restore a saved instance of the application
		super.onCreate(savedInstanceState);
		
		// Move back to the game selection panel
		setContentView(R.layout.game);
		
 		// Turns on built-in zoom controls
		MapView mapView = (MapView) findViewById(R.id.mapView);
		mapController = mapView.getController();
		mapView.setBuiltInZoomControls(true);
		getLocation();
		
		// Setting up the overlays class
		mapOverlays = mapView.getOverlays();
		Drawable drawable = this.getResources().getDrawable(R.drawable.person_red);
		itemizedoverlay = new GameCTFOverlays(drawable);
		
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
