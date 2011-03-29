/**
 * CurrentUser.java
 * Used for storage of name, uid and location information of the current user.
 */
package stetson.CTF;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.message.BasicNameValuePair;

import android.util.Log;

public class CurrentUser {
	
	public static final String TAG = "CurrentUser";
	public static final int CREATE_PARAMS = 0;
	public static final int JOIN_PARAMS = 1;
	public static final int UPDATE_PARAMS = 2;
	
	// User Info
	private static String name = "";
	private static String uid = "";
	private static double latitude = -1;
	private static double longitude = -1;
	private static double accuracy = -1;
	
	// Game Info
	private static String gameId = "";
	
	private CurrentUser() {
		
	}
	
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
	
	/**
	 * Generates HttpParams automatically for the current user.
	 * Type:	CREATE_PARAMS 	= lat, long, name, gameId
	 * 			JOIN_PARAMS		= lat, long, accuracy, uid, name
	 * 			UPDATE_PARAMS 	= lat, long, accuracy, uid, name, game
	 * @param hbr
	 * @param type
	 * @return
	 */
	public static HttpPost buildHttpParams(HttpPost hbr, int type) {

        List<NameValuePair> params = new ArrayList<NameValuePair>(2);  
        
        // Location (in all requests)
        params.add(new BasicNameValuePair("latitude", Double.toString(CurrentUser.latitude)));
        params.add(new BasicNameValuePair("longitude", Double.toString(CurrentUser.longitude)));
        
        // Accuracy and UID for joining a game and requesting location updates
        if(type == JOIN_PARAMS || type == UPDATE_PARAMS) {
        	params.add(new BasicNameValuePair("accuracy",  Double.toString(CurrentUser.accuracy)));
        	params.add(new BasicNameValuePair("user_id", CurrentUser.uid));
        }
        
        // Name (in all requests)
        params.add(new BasicNameValuePair("name", CurrentUser.name));
        
        // Game ID for creating games and updating locations
        if(type == CREATE_PARAMS || type == UPDATE_PARAMS) {
        	params.add(new BasicNameValuePair("game_id", CurrentUser.gameId));
        }
        
        try {
			hbr.setEntity(new UrlEncodedFormEntity(params));
		} catch (UnsupportedEncodingException e) {
			Log.e(TAG, "Error adding params to request!", e);
		}
		
		return hbr;
	}
	

	
}
