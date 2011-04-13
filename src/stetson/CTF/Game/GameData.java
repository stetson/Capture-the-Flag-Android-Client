package stetson.CTF.Game;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONObject;

import android.util.Log;

import com.google.android.maps.GeoPoint;

public class GameData {

	private Boundaries myBounds;
	private GeoPoint myOrigin;
	private GeoPoint myFlagBlue;
	private GeoPoint myFlagRed;

	private int myScoreBlue;
	private int myScoreRed;
	
	private int myPlayerCountBlue;
	private int myPlayerCountRed;
	
	private boolean isCapturedBlue;
	private boolean isCapturedRed;
	
	private ArrayList<Player> myPlayer;
	private int myActualPlayers;
	
	private boolean hasError;
	private String myErrorMessage;
	
	private String myCreator;
	
	public void parseJSONObject(JSONObject game) {
		
		myBounds = new Boundaries();
		
		myCreator = game.optString("creator", "");
		
		myScoreRed = game.optInt("red_score", 0);
		myScoreBlue = game.optInt("blue_score", 0);
		
		myPlayerCountRed = game.optInt("red", 0);
		myPlayerCountBlue = game.optInt("blue", 0);
		
		isCapturedRed = game.optBoolean("red_flag_captured", false);
		isCapturedBlue = game.optBoolean("blue_flag_captured", false);
		
		parseOrigin(game);
		parseFlags(game);
		parseBounds(game);
		parsePlayers(game);
		
	}
	
	protected void checkErrors(JSONObject game) {
		hasError = game.has("error");
		if(hasError) {
			myErrorMessage = game.optString("error");
		}
	}
	
	protected void parsePlayers(JSONObject game) {
		
		// Lets try to guess how many players are in the array list
		myActualPlayers = 0;
		myPlayer = new ArrayList<Player>(myPlayerCountRed + myPlayerCountBlue);
		
		// We don't have any players?!
		if(!game.has("players")) {
			Log.i("TAG", "Game has no players...");
			return;
		}

		JSONObject playerObject = game.optJSONObject("players");
		JSONArray playerNames = playerObject.names();
		for(int p = 0; p < playerNames.length(); p++) {
			myActualPlayers++;
			myPlayer.add(new Player(playerObject.optJSONObject(playerNames.optString(p, ""))));
			Log.i("TAG", "Adding a player..." + playerNames.optString(p, "[NONE]"));
		}
						
		
	}
	
	protected void parseOrigin(JSONObject game) {
		
		if(!game.has("origin")) {
			myOrigin = new GeoPoint(0, 0);
			return;
		}
		
		int longitude = (int) (1E6 * game.optJSONObject("origin").optDouble("longitude", 0));
		int latitude = (int) (1E6 * game.optJSONObject("origin").optDouble("latitude", 0));
		myOrigin = new GeoPoint(latitude, longitude);

	}
	
	protected void parseFlags(JSONObject game) {
		
		if(!game.has("red_flag") || !game.has("blue_flag")) {
			myFlagRed = new GeoPoint(0, 0);
			myFlagBlue = new GeoPoint(0, 0);
			return;
		}
		
		int longitude = (int) (1E6 * game.optJSONObject("red_flag").optDouble("longitude", 0));
		int latitude = (int) (1E6 * game.optJSONObject("red_flag").optDouble("latitude", 0));
		myFlagRed = new GeoPoint(latitude, longitude);
		
		longitude = (int) (1E6 * game.optJSONObject("blue_flag").optDouble("longitude", 0));
		latitude = (int) (1E6 * game.optJSONObject("blue_flag").optDouble("latitude", 0));
		myFlagBlue = new GeoPoint(latitude, longitude);
		

	}
	
	protected void parseBounds(JSONObject game) {
		
		if(!game.has("red_bounds") || !game.has("blue_bounds")) {
			myBounds.setBlueBounds(new GeoPoint(0,0), new GeoPoint(0,0));
			myBounds.setRedBounds(new GeoPoint(0,0), new GeoPoint(0,0));
			return;
		}
		
		int botRightLong = (int) (1E6 * game.optJSONObject("red_bounds").optJSONObject("bottom_right").optDouble("longitude", 0));
		int botRightLat = (int) (1E6 * game.optJSONObject("red_bounds").optJSONObject("bottom_right").optDouble("latitude", 0));
		int topLeftLong = (int) (1E6 * game.optJSONObject("red_bounds").optJSONObject("top_left").optDouble("longitude", 0));
		int topLeftLat = (int) (1E6 * game.optJSONObject("red_bounds").optJSONObject("top_left").optDouble("latitude", 0));
		myBounds.setRedBounds(new GeoPoint(topLeftLat, topLeftLong), new GeoPoint(botRightLat, botRightLong));
		
		botRightLong = (int) (1E6 * game.optJSONObject("blue_bounds").optJSONObject("bottom_right").optDouble("longitude", 0));
		botRightLat = (int) (1E6 * game.optJSONObject("blue_bounds").optJSONObject("bottom_right").optDouble("latitude", 0));
		topLeftLong = (int) (1E6 * game.optJSONObject("blue_bounds").optJSONObject("top_left").optDouble("longitude", 0));
		topLeftLat = (int) (1E6 * game.optJSONObject("blue_bounds").optJSONObject("top_left").optDouble("latitude", 0));
		myBounds.setBlueBounds(new GeoPoint(topLeftLat, topLeftLong), new GeoPoint(botRightLat, botRightLong));
		
		
	}
	
	/* Accessors */
	public Boundaries getBounds() {
		return myBounds;
	}
	public String getCreator() {
		return myCreator;
	}
	public Player getPlayer(int index) {
		return myPlayer.get(index);
	}
	public GeoPoint getOrigin() {
		return myOrigin;
	}
	public GeoPoint getRedFlag() {
		return myFlagRed;
	}
	public GeoPoint getBlueFlag() {
		return myFlagBlue;
	}
	public int getBlueScore() {
		return myScoreBlue;
	}
	public int getRedScore() {
		return myScoreRed;
	}
	public int getRedPlayers() {
		return myPlayerCountRed;
	}
	public int getBluePlayers() {
		return myPlayerCountBlue;
	}
	public int getPlayerCount() {
		return myActualPlayers;
	}
	public boolean isRedFlagTaken() {
		return isCapturedRed;
	}
	public boolean isBlueFlagTaken() {
		return isCapturedBlue;
	}
	public boolean hasError() {
		return hasError;
	}
	public String getErrorMessage() {
		return myErrorMessage;
	}
}
