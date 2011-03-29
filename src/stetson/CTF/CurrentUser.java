package stetson.CTF;

public class CurrentUser {

	private String name;
	private String uid;
	private double latitude;
	private double longitude;
	private double accuracy;
	
	public CurrentUser(String name, String uid) {
		this.name = name;
		this.uid = uid;
		this.latitude = -1;
		this.longitude = -1;
		this.accuracy = -1;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public void setUID(String uid) {
		this.uid = uid;
	}
	
	public String getName() {
		return this.name;
	}
	
	public String getUID() {
		return this.uid;
	}
}
