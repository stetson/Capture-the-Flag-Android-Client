/**
 * CurrentUser.java
 * Used for storage of name, uid and location information of the current user.
 */
package stetson.CTF.utils;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.message.BasicNameValuePair;

import stetson.CTF.JoinCTF;

import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;

public class CurrentUser {
	
	public static final String TAG = "CurrentUser";
	public static final int CREATE_PARAMS = 0;
	public static final int JOIN_PARAMS = 1;
	public static final int UPDATE_PARAMS = 2;
	public static final int LEAVE_PARAMS = 3;
	
	// User Info
	private static String name = "";
	private static String uid = "";
	private static double latitude = -1;
	private static double longitude = -1;
	private static float accuracy = -1;
	private static boolean isObserver = false;
	
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
	
	public static boolean hasLocation() {
		if(CurrentUser.accuracy == -1 || CurrentUser.latitude == -1 || CurrentUser.longitude == -1) {
			return false;
		}
		return true;
	}
	
	/**
	 * Generates HttpParams automatically for the current user.
	 * Type:	CREATE_PARAMS 	= lat, long, name, gameId
	 * 			JOIN_PARAMS		= lat, long, accuracy, uid, name
	 * 			UPDATE_PARAMS 	= lat, long, accuracy, uid, name, game
	 * @param hbr
	 * @param type
	 * @return
	 */
	public static UrlEncodedFormEntity buildHttpParams(int type) {

        List<NameValuePair> params = new ArrayList<NameValuePair>(2);  
        
        boolean location = false;
        boolean user_id = false;
        boolean game_id = false;
        boolean name = false;
        
        // Determine what is needed for each protocol
        switch(type) {
        
			case CREATE_PARAMS:
	    		location = true;
	    		user_id = true;
	    		name = true;
	    		game_id = true;
	    		break;
	    		
    		case JOIN_PARAMS:
	    		location = true;
	    		user_id = true;
	    		name = true;
	    		break;
    		
        	case UPDATE_PARAMS:
        		location = true;
        		user_id = true;
        		name = true;
        		game_id = true;
        		break;
        		
	        case LEAVE_PARAMS:
	        	user_id = true;
	        	break;
        }
        
        // Location Information
        if(location) {
            params.add(new BasicNameValuePair("latitude", Double.toString(CurrentUser.latitude)));
            params.add(new BasicNameValuePair("longitude", Double.toString(CurrentUser.longitude)));
            params.add(new BasicNameValuePair("accuracy",  Float.toString(CurrentUser.accuracy)));
        }
        
        // User UID
        if(user_id) {
        	params.add(new BasicNameValuePair("user_id", CurrentUser.uid));
        }
        
        // Game ID
        if(game_id) {
        	params.add(new BasicNameValuePair("game_id", CurrentUser.gameId));
        }
        
        // Username
        if(name) {
        	params.add(new BasicNameValuePair("name", CurrentUser.name));
        }

        
        try {
        	return new UrlEncodedFormEntity(params);
		} catch (UnsupportedEncodingException e) {
			Log.e(TAG, "Error adding params to request!", e);
		}
		
		return null;
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
		params += "&name=" + CurrentUser.name;	
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
	
	
}
