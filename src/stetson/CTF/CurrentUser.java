/**
 * CurrentUser.java
 * Used for storage of name, uid and location information of the current user.
 */
package stetson.CTF;
public class CurrentUser {

	// User Info
	private static String name = "";
	private static String uid = "";
	private static double latitude = -1;
	private static double longitude = -1;
	private static double accuracy = -1;
	
	// Game Info
	private static String gameName = "";
	private static String gameUID = "";
	
	private CurrentUser() {
		
	}
	
	public static void setName(String name) {
		CurrentUser.name = name;
	}
	
	public static void setUID(String uid) {
		CurrentUser.uid = uid;
	}
	
	public static String getName() {
		return name;
	}
	
	public static String getUID() {
		return uid;
	}
}
