package stetson.CTF;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.OverlayItem;

public class GameCTF extends MapActivity {
		
	// Data members
	private CurrentUser usr;
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
	 * Default behavior is NULL. Nothing is happening yet!
	 * @param saved instance state
	 */
	public void onCreate(Bundle savedInstanceState) {
		
		Log.i(TAG, "Starting map activity...");
		
		// Restore a saved instance of the application
		super.onCreate(savedInstanceState);
		
		// Move back to the game selection panel
		setContentView(R.layout.game);
		
 		// Turns on built-in zoom controls
		MapView mapView = (MapView) findViewById(R.id.mapView);
		mapController = mapView.getController();
		mapView.setBuiltInZoomControls(true);
		
		// Setting up the overlays class
		mapOverlays = mapView.getOverlays();

	}

	/**
	 * Create a new user object given a name.
	 * @param name
	 */
	public void CreateUser(String name)
	{
		
		// Generate a new uid
		String uid = "xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx";
		while(uid.contains("x")) 
		uid = uid.replaceFirst("x", Long.toHexString(Math.round(Math.random() * 16.0)));
		uid = uid.toUpperCase();
		
		// Create our new user object
		usr = new CurrentUser(name, uid);

	}
	
	/**
	 * Attempt to join the specified game using the given username.
	 * This will generate a new UID for the user and stuff.
	 * @param gameName
	 * @param userName
	 * @return
	 */
	public boolean joinGame(String gameName, String userName) {
		
        HttpPost hp = new HttpPost("http://ctf.no.de/game/Test%20Game");
        List<NameValuePair> params = new ArrayList<NameValuePair>(2);  
        params.add(new BasicNameValuePair("latitude", "29.0356105"));
        params.add(new BasicNameValuePair("longitude", "-81.3034442"));
        params.add(new BasicNameValuePair("accuracy", "47"));
        params.add(new BasicNameValuePair("user_id", "04176DF0-2D8C-4F49-8466-8C2C608ABFA7"));
        params.add(new BasicNameValuePair("name", "fake-user2"));
        try {
			hp.setEntity(new UrlEncodedFormEntity(params));
		} catch (UnsupportedEncodingException e) {
			Log.e(TAG, "Error adding params to post request!", e);
		}
		sendRequest(hp, new ResponseListener() {
			public void onResponseReceived(HttpResponse response) {
				Log.i(TAG, responseToString(response));
			}
		});
		
		return false;
	}
	
	
	/**
	 * Returns false (required by MapActivity)
	 * @return false
	 */
	protected boolean isRouteDisplayed() {
		return false;
	}
	
	/**
	 * Makes an HTTP request and sends it to a response listener once completed.
	 * @param request
	 * @param responseListener
	 */
	public static void sendRequest(final HttpRequestBase request, ResponseListener responseListener) {
		(new AsynchronousSender(request, new Handler(), new CallbackWrapper(responseListener))).start();
	}
	
	/**
	 * Draws a string from an HttpResponse object.
	 * @param rp
	 * @return
	 */
	public static String responseToString(HttpResponse rp) {
    	String str = "";
    	try {
    		str = EntityUtils.toString(rp.getEntity());
    	} catch(IOException e) {
    		Log.i(TAG, "HttpRequest Error!", e);
    	}  
    	return str;
	}
	
	/**
	 * Currently un-used code. For example only.
	 */
	protected void getLocation()
	{
		locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

		// Define a listener that responds to location updates
		locationListener = new LocationListener() {
			public void onLocationChanged(Location location) {
				// Called when a new location is found by the network location provider.
				playerPoint = new GeoPoint(  (int)(1E6 *location.getLatitude()),  (int)(1E6 *location.getLongitude()));

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
