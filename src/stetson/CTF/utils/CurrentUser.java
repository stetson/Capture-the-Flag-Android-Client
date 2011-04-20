/**
 * CurrentUser.java
 * Used for storage of name, uid and location information of the current user.
 */
package stetson.CTF.utils;

import stetson.CTF.JoinCTF;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;

public class CurrentUser {
	
	public static final String TAG = "CurrentUser";
	
	
	// User Info
	private static String name = "";
	private static String uid = "";
	private static double latitude = -1;
	private static double longitude = -1;
	private static float accuracy = -1;
	private static boolean isObserver = false;
	private static boolean isFacebookUser = false;
	
	// Game Info
	private static String gameId = "";
	
	// Location members
	private static LocationManager locationManager;
	private static LocationListener locationListener;
	
	// Mutators
	public static void setLocation(double lati, double longi) {
		CurrentUser.latitude = lati;
		CurrentUser.longitude = longi;
	}
	public static void setName(String name) {
		CurrentUser.name = name;
	}
	public static void setUID(String uid) {
		CurrentUser.uid = uid;
	}
	public static void setGameId(String game) {
		CurrentUser.gameId = game;
	}
	public static void setAccuracy(float accuracy) {
		CurrentUser.accuracy = accuracy;
	}
	public static void setIsObserver(boolean isObserver) {
		CurrentUser.isObserver = isObserver;
	}
	
	
	// Accessors
	public static float getAccuracy() {
		return CurrentUser.accuracy;
	}
	public static String getGameId() {
		return CurrentUser.gameId;
	}
	public static String getName() {
		return name;
	}
	public static String getUID() {
		return uid;
	}
	public static double getLongitude() {
		return CurrentUser.longitude;
	}
	public static double getLatitude() {
		return CurrentUser.latitude;
	}
	public static boolean getIsObserver() {
		return isObserver;
	}
    public static void setFacebookUser(boolean facebook) {
    	isFacebookUser = facebook;
    }
    
    public static boolean isFacebookuser() {
    	return isFacebookUser;
    }
	public static boolean hasLocation() {
		if(CurrentUser.accuracy == -1 || CurrentUser.latitude == -1 || CurrentUser.longitude == -1) {
			return false;
		}
		return true;
	}
	
	
	/**
	 * Generates HttpParams automatically for quering the server for a list of games.
	 * @param hbr
	 * @return
	 */
	public static String buildQueryParams() {
		String params = "";
		params += "latitude=" + CurrentUser.latitude;
		params += "&longitude=" + CurrentUser.longitude;
		params += "&accuracy=" + CurrentUser.accuracy;
		params += "&user_id=" + CurrentUser.uid;	
		params += "&name=" + Connections.encString(CurrentUser.name);
		return params;
	}
	
	
	/**
	 * Stops the locationListener given a location manager.
	 * @param lm
	 */
	public static void stopLocation(LocationManager lm) {
		if(locationListener != null) {
			lm.removeUpdates(locationListener);
			locationListener = null;
		}
	}
	
	/**
	 * Periodically updates the users location.
	 * @param lm
	 * @param frequency
	 */
	public static void userLocation(LocationManager lm, int frequency) {
		
		// Setup our location manager
		locationManager = lm;
		
		// Remove all updates for the current listener (if one exists)
		CurrentUser.stopLocation(lm);
		
		// Setup our location listener
		locationListener = new LocationListener() {
				
			public void onLocationChanged(Location location) {
				Log.i(TAG, "Update Location.");
				CurrentUser.setLocation(location.getLatitude(), location.getLongitude());
				CurrentUser.setAccuracy(location.getAccuracy());
			}
	
			public void onStatusChanged(String provider, int status, Bundle extras) {}
			public void onProviderEnabled(String provider) {}
			public void onProviderDisabled(String provider) {}
				
		};
		
		// Minimum distance to move a gps update
		int distThreshold = 0;
		switch(frequency) {
			case JoinCTF.GPS_UPDATE_FREQUENCY_GAME:
				distThreshold = JoinCTF.GPS_UPDATE_DISTANCE_GAME;
				break;
			case JoinCTF.GPS_UPDATE_FREQUENCY_INTRO:
				distThreshold = JoinCTF.GPS_UPDATE_DISTANCE_INTRO;
				break;
			case JoinCTF.GPS_UPDATE_FREQUENCY_BACKGROUND:
				distThreshold = JoinCTF.GPS_UPDATE_DISTANCE_BACKGROUND;
				break;
		}
		
		// Start requesting updates per given time
		Log.i(TAG, "New GPS Parsing: FREQ=" + frequency + ", DIST=" + distThreshold);
		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, frequency, distThreshold, locationListener);
		
	}
	
	 /**
     * generates a new UID.
     * @param name
     */
    public static String genUID() {
		// Generate a new uid
		String uid = "xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx";
		while(uid.contains("x")) 
		uid = uid.replaceFirst("x", Long.toHexString(Math.round(Math.random() * 16.0)));
		uid = uid.toUpperCase();
		setUID(uid);
		return uid;
    }
    
}
