package stetson.CTF.Join;

import org.json.JSONObject;

public class GameItem {
	
	public String myName;
	public int myPlayers;
	public double myDistance;
	
	public GameItem(String name, int players, double distance) {
		myName = name;
		myPlayers = players;
		myDistance = distance;
	}
	
	public GameItem(JSONObject item) {
		
		if(item == null) {
			return;
		}
		
		myName = item.optString("name");
		myPlayers = item.optInt("players");
		myDistance = item.optDouble("distance");
	}
	
	public String getName() {
		return myName;
	}
	
	public int getPlayers() {
		return myPlayers;
	}
	
	public double getDistance() {
		return myDistance;
	}
	
}
