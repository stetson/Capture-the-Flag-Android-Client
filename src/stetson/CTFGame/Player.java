package stetson.CTFGame;

import org.json.JSONObject;

public class Player {
		
	// Data Members:
	private String myName;
	private String myUID;
	private String myTeam;
	private double myLongitude;
	private double myLatitude;
	private double myAccuracy;
	private int myCaptures;
	private int myTags;
	private boolean hasFlag;
	private boolean hasObserverMode;

	public Player(JSONObject player) {
		this.parseJSONObject(player);
	}
	
	public void parseJSONObject(JSONObject player) {
		myName = player.optString("name", "");
		myTeam = player.optString("team", "");
		myUID = player.optString("user_id", "");
		myLatitude = player.optDouble("latitude", 0);
		myLongitude = player.optDouble("longitude", 0.0);
		myAccuracy = player.optDouble("accuracy", 0.0);
		myCaptures = player.optInt("captures", 0);
		myTags = player.optInt("tags", 0);
		hasFlag = player.optBoolean("has_flag", false);
		hasObserverMode = player.optBoolean("observer_mode", false);
	}
	
	public String getName() {
		return myName;
	}
	public String getTeam() {
		return myTeam;
	}
	public String getUID() {
		return myUID;
	}
	public int getLatitude() {
		return (int) (1E6 * myLatitude);
	}
	public int getLongitude() {
		return (int) (1E6 * myLongitude);
	}
	public double getAccuracy() {
		return myAccuracy;
	}
	public int getTags() {
		return myTags;
	}
	public int getCaptures() {
		return myCaptures;
	}
	public boolean hasFlag() {
		return hasFlag;
	}
	public boolean hasObserverMode() {
		return hasObserverMode;
	}
}
