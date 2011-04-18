package stetson.CTF.utils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.android.maps.GeoPoint;

import stetson.CTF.JoinCTF;
import stetson.CTF.Join.GameItem;

import android.util.Log;

public class Connections {
	
	public final static String TAG = "Connections";
	private final static String GOOD_RESPONSE = "OK";
	public static final int CUSTOM_PARAMS = -1;
	public static final int CREATE_PARAMS = 0;
	public static final int JOIN_PARAMS = 1;
	public static final int UPDATE_PARAMS = 2;
	public static final int LEAVE_PARAMS = 3;
	
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
	 * Generates HttpParams automatically for the current user.
	 * Type:	CREATE_PARAMS 	= lat, long, name, gameId
	 * 			JOIN_PARAMS		= lat, long, accuracy, uid, name
	 * 			UPDATE_PARAMS 	= lat, long, accuracy, uid, name, game
	 *          CUSTOM_PARAMS   = uses ArrayList passed to create FormEntity
	 * @param type
	 * @return
	 */
	public static UrlEncodedFormEntity buildHttpParams(int type, ArrayList<NameValuePair> customList) {

        ArrayList<NameValuePair> params = new ArrayList<NameValuePair>(4);  
        
        boolean location = false;
        boolean user_id = false;
        boolean game_id = false;
        boolean name = false;
        boolean custom = false;
        
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
	        case CUSTOM_PARAMS:
	        	custom = true;
	        	break;
        }
        
        if(custom)
        {
        	params = customList;
        	
        }
        
        // Location Information
        if(location) {
            params.add(new BasicNameValuePair("latitude", Double.toString(CurrentUser.getLatitude())));
            params.add(new BasicNameValuePair("longitude", Double.toString(CurrentUser.getLongitude())));
            params.add(new BasicNameValuePair("accuracy",  Float.toString(CurrentUser.getAccuracy())));
        }
        
        // User UID
        if(user_id) {
        	params.add(new BasicNameValuePair("user_id", CurrentUser.getUID()));
        }
        
        // Game ID
        if(game_id) {
        	params.add(new BasicNameValuePair("game_id", CurrentUser.getGameId()));
        }
        
        // Username
        if(name) {
        	params.add(new BasicNameValuePair("name", CurrentUser.getName()));
        }

        
        try {
        	return new UrlEncodedFormEntity(params);
		} catch (UnsupportedEncodingException e) {
			Log.e(TAG, "Error adding params to request!", e);
		}
		
		return null;
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
	public static String joinOrCreate(String usrName) {
		// More friendly parameters :)
		String name = usrName;
		String game = CurrentUser.getGameId();
		Log.i(TAG, "(WORKER) joinGame(" + name + ", " + game + ")");
		
		// If a game name wasn't given, then we need to make a game.
		if(game.equals("")) {
			
			CurrentUser.setGameId(CurrentUser.getName());
			String data = callPost("/game/", buildHttpParams(CREATE_PARAMS,null));
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
		String data = callPost("/game/" + CurrentUser.getGameId().replaceAll(" ", "%20"),buildHttpParams(JOIN_PARAMS,null));
		
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
	public static ArrayList<GameItem> getGames() {
		
		ArrayList<GameItem> gamesList = new ArrayList<GameItem>();
		// Sweet, we have a location, lets grab a list of games
		HttpGet req = new HttpGet(JoinCTF.SERVER_URL + "/game/?" + CurrentUser.buildQueryParams());
		String data = Connections.sendRequest(req);
		try {
			JSONObject games = new JSONObject(data);
			if (games.has("games")) {
				// Add all the games to a list
				JSONArray list = games.getJSONArray("games");
				for(int n = 0; n < list.length(); n++) {
					GameItem item = new GameItem(list.optJSONObject(n));
					gamesList.add(item);
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
	public static JSONObject getGameData() {
		String data = callPost("/location/", buildHttpParams(UPDATE_PARAMS,null));
		try {
			JSONObject jObject = new JSONObject(data);
			return jObject;
		} catch (JSONException e) {
			Log.e(TAG, "Error parsing JSON.", e);
		}
		return null;
		
	}
	
	/**
	 * Sends server request to move a flag.
	 * Requires that the user is the creator of the game.
	 * @param loc
	 * @param team
	 */
	public static void moveFlag(GeoPoint loc, String team) {
		ArrayList<NameValuePair> params = new ArrayList<NameValuePair>(4);  
		params.add(new BasicNameValuePair("latitude", "" + (loc.getLatitudeE6() / 1E6)));
		params.add(new BasicNameValuePair("longitude", "" + (loc.getLongitudeE6() / 1E6)));
		params.add(new BasicNameValuePair("user_id", CurrentUser.getUID()));
		params.add(new BasicNameValuePair("game_id", CurrentUser.getGameId()));
		params.add(new BasicNameValuePair("team", team));
		String data = callPost("/flag/",buildHttpParams(CUSTOM_PARAMS, params));
	    Log.i(TAG, "FLAG MOVE: " + data);
	    
	}
	/**
	 * Method that calls HttpPost with the servers url
	 * Uses given url and form entity to make post.
	 * 
	 * @param postURL
	 * @param params
	 * @return
	 */
	public static String callPost(String postURL,UrlEncodedFormEntity params)
	{
		// url to post data to
		String url = postURL;
		HttpPost req = new HttpPost(JoinCTF.SERVER_URL + url);
		req.setEntity(params);
		String data = Connections.sendRequest(req);
		return data;
	}

	/**
	 * Request to leave the game that the user is in.
	 * This call does not handle the server's response!
	 * @param game name
	 * @param user uid
	 */
	public static void leaveGame(String gameName, String uid) {
		String gameUrl = gameName.replaceAll(" ", "%20");
		HttpDelete req = new HttpDelete(JoinCTF.SERVER_URL + "/game/" + gameUrl + "/" + uid);
		Connections.sendRequest(req);
	}
	
	
    
}
