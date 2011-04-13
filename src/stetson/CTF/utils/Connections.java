package stetson.CTF.utils;

import java.io.IOException;
import java.util.ArrayList;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import stetson.CTF.JoinCTF;

import android.util.Log;

public class Connections {
	
	public final static String TAG = "Connections";
	private final static String GOOD_RESPONSE = "OK";
	/**
	 * Makes an HTTP request and returns a response as a string.
	 * @param request
	 * @return response
	 */
	public static String sendRequest(HttpRequestBase request) {
		
		Log.i(TAG, "ConnectionF: " + request.getURI());
		
		try {
			HttpClient client = new DefaultHttpClient();
			HttpResponse resp;
			resp = client.execute(request);
			return responseToString(resp);
			 
		} catch (ClientProtocolException e) {
			Log.e(TAG, "Web Request Failed", e);
		} catch (IOException e) {
			Log.e(TAG, "Web Request Failed", e);
		}
		return "";
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
    		Log.e(TAG, "HttpRequest Error!", e);
    	}  
    	return str;
	}
	/**
	 * Main method to create a game if the game parameter is ""
	 * Main method to join a game if the game parameter is the gameName
	 * 
	 * @param usrName
	 * @param usrGame
	 * @return
	 */
	public static String joinOrCreate(String usrName, String usrGame)
	{
		// More friendly parameters :)
		String name = usrName;
		String game = usrGame;
		Log.i(TAG, "(WORKER) joinGame(" + name + ", " + game + ")");
		
		// Build a UID
		updateUser(name);
		
		// If a game name wasn't given, then we need to make a game.
		if(game.equals("")) {
			
			CurrentUser.setGameId(CurrentUser.getName());
			HttpPost hp = new HttpPost(JoinCTF.SERVER_URL + "/game/");
			hp.setEntity(CurrentUser.buildHttpParams(CurrentUser.CREATE_PARAMS));
			String data = Connections.sendRequest(hp);
			Log.i(TAG, "RESPONSE: " +data);
			
			try {
				JSONObject response = new JSONObject(data);
				Log.i(TAG, "(WORKER) create game response: " + data);
				if(response.has("response") && !response.getString("response").equals(GOOD_RESPONSE)) {
					return "Unexpected server response #1";
				} else if (response.has("error")) {
					return "Server Error: " + response.get("error");
				}
			} catch (JSONException e) {
				Log.e(TAG, "Error parsing JSON.", e);
				return "Unexpected server response #2";
			}
		}
		// If a game was created, then it was a success at this point! Now we must join the game.
		String gameUrl = CurrentUser.getGameId().replaceAll(" ", "%20");
		HttpPost hp = new HttpPost(JoinCTF.SERVER_URL + "/game/" + gameUrl);
		hp.setEntity(CurrentUser.buildHttpParams(CurrentUser.JOIN_PARAMS));
		String data = Connections.sendRequest(hp);
		
		try {
			JSONObject jsonGame = new JSONObject(data);
			if(jsonGame.has("error")) {
				return "Server Error: " + jsonGame.get("error");
			}
		} catch (JSONException e) {
			Log.e(TAG, "Error parsing JSON.", e);
			return "Unexpected server response #3";
		}

	    return GOOD_RESPONSE;
	}
	/**
	 * Method returns ArrayList<String> of the list of games available
	 * 
	 * @return ArrayList<String> games
	 */
	
	public static ArrayList<String> getGames()
	{
		
		ArrayList<String> gamesList = new ArrayList<String>();
		// Sweet, we have a location, lets grab a list of games
		HttpGet req = new HttpGet(JoinCTF.SERVER_URL + "/game/?" + CurrentUser.buildQueryParams());
		String data = Connections.sendRequest(req);
		try {
			JSONObject games = new JSONObject(data);
			if (games.has("games")) {
				// Add all the games to a list
				JSONArray list = games.getJSONArray("games");
				for(int n = 0; n < list.length(); n++) {
					gamesList.add(list.getString(n));
				}
				// Ok, that's all, return the games list
				return gamesList;
				
			} else {
				Log.e(TAG, "Unexpected Server Response: " + data);
			}
		} catch (JSONException e) {
			Log.e(TAG, "Error parsing JSON.", e);
		}
		return null;
	}
	/**
	 * Methods returns all gameData
	 * 
	 * @return JSONObject gameData
	 */
	public static JSONObject getGameData()
	{
		HttpPost req = new HttpPost(JoinCTF.SERVER_URL + "/location/");
		req.setEntity(CurrentUser.buildHttpParams(CurrentUser.UPDATE_PARAMS));
		String data = Connections.sendRequest(req);
		try {
			JSONObject jObject = new JSONObject(data);
			return jObject;
		} catch (JSONException e) {
			Log.e(TAG, "Error parsing JSON.", e);
		}
		return null;
		
	}
		
	 /**
     * Sets the user's name and generates a new UID.
     * @param name
     */
    protected static void updateUser(String name) {
    	// New name
    	CurrentUser.setName(name);
    	
		// Generate a new uid
		String uid = "xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx";
		while(uid.contains("x")) 
		uid = uid.replaceFirst("x", Long.toHexString(Math.round(Math.random() * 16.0)));
		uid = uid.toUpperCase();
		CurrentUser.setUID(uid);
    }
    
}
