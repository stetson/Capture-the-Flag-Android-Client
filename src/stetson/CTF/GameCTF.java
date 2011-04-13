package stetson.CTF;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;

import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import stetson.CTF.Game.GameCTFOverlays;
import stetson.CTF.Game.GameData;
import stetson.CTF.Game.GameInfoBar;
import stetson.CTF.Game.GameMenu;
import stetson.CTF.Game.GameScores;
import stetson.CTF.Game.Player;
import stetson.CTF.utils.Connections;
import stetson.CTF.utils.CurrentUser;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.os.PowerManager;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.OverlayItem;


public class GameCTF extends MapActivity {
	
	// Constant: Debugging tag
	public static final String TAG = "GameCTF";
	
	// Constant: How often should we wait between game update cycles?
	public static final int GAME_UPDATE_DELAY = 1500;
	
	// Constants: Are we moving a flag?
	public static final int MOVING_FLAG_NONE = -1;
	public static final int MOVING_FLAG_RED = 0;
	public static final int MOVING_FLAG_BLUE = 1;
	
	// Constants: Where should we center to on the next game update cycle?
	public static final int CENTER_NONE = -1;
	public static final int CENTER_ORIGIN = 0;
	public static final int CENTER_SELF = 1;
	public static final int CENTER_RED = 2;
	public static final int CENTER_BLUE = -3;
		
	// Constant: What is the minimum accuracy (in meters) we should expect ?
	public static final int MIN_ACCURACY = 40;	
		
	// Drawables hash map
	private HashMap<Integer,Drawable> drawable = new HashMap<Integer, Drawable>(10);
	
	// Application Mechanics
	private PowerManager.WakeLock ctfWakeLock;
	
	// Map Mechanics
	private MapView mapView;
	private boolean hasCenteredOrigin = false;
	private List<Overlay> mapOverlay;
	private GameCTFOverlays mapOverlayMarkers;
	
	// Game Mechanics
	private int isMovingFlag;
	private GameData myGameData;
	private GameMenu myMenu;
	private GameInfoBar myInfoBar;
	private GameScores myScores;
	private TaskGameProcess cycle;
	private Handler gameHandler = new Handler();
	
	/*----------------------------------*
	/* ACTIVITY RELATED FUNCTIONS START
	/*----------------------------------*/
	/**
	 * Called when the activity is first created.
	 * Default behavior is NULL. Nothing is happening yet!
	 * @param saved instance state
	 */
	public void onCreate(Bundle savedInstanceState) {
		
		Log.i(TAG, "Starting map activity...");
		
		// Restore a saved instance of the application
		super.onCreate(savedInstanceState);
				
		// Make sure the user is actually in a game
		if(CurrentUser.getGameId().equals("")) {
			this.finish();
			return;
		}
		
		// Setup the wake lock
		PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
		ctfWakeLock = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, TAG);
		
		// Move back to the game selection panel
		setContentView(R.layout.game);
		
		// No, we aren't moving the flag right now :)
		isMovingFlag = MOVING_FLAG_NONE;
		
		// Create the menu [must be created after setContentView() is run]
		myMenu = new GameMenu(this);
		myMenu.setMenu(GameMenu.MENU_DEFAULT, null, null);
		
		// Create the game info bar (top of screen)
		myInfoBar = new GameInfoBar(this);
		myInfoBar.setLoading();
		
		// Create the class for game scores
		myScores = new GameScores(this);
		
		// Make sure gps is running at the right speed
		CurrentUser.userLocation((LocationManager) this.getSystemService(Context.LOCATION_SERVICE), StetsonCTF.GPS_UPDATE_FREQUENCY_GAME);
		
 		// Turns on built-in zoom controls and satellite view
		mapView = (MapView) findViewById(R.id.mapView);
		mapView.getController();
		mapView.setBuiltInZoomControls(true);
		mapView.setSatellite(true);
		
		// Build everything we need to run
		buildDrawables();
				
	    // Setup overlay stuff
		mapOverlay = mapView.getOverlays();
        mapOverlayMarkers = new GameCTFOverlays(drawable.get(R.drawable.star), this);
        
        // Start game processing
        myGameData = new GameData();
		gameHandler.postDelayed(gameProcess, GAME_UPDATE_DELAY);

	}

	/**
	 * Run when the activity is stopped, such as .finish()
	 */
	public void onDestroy() {
		
		// Stop any running cycle that may exist
		if(cycle != null) {
			cycle.cancel(true);
			cycle = null;
		}
				
		// Remove the user from the game on the back end
		// We're using an anonymous thread here because we don't care about the response
		// This is not a strictly required event
		String gameUrl = CurrentUser.getGameId().replaceAll(" ", "%20");
		HttpDelete req = new HttpDelete(StetsonCTF.SERVER_URL + "/game/" + gameUrl + "/" + CurrentUser.getUID());
		Connections.sendRequest(req);

		// Remove the user from the game on the front end
		CurrentUser.setGameId("");
		CurrentUser.setLocation(-1, -1);
		CurrentUser.setAccuracy(-1);
		
		// Call last
		super.onDestroy();
	}
	
	/**
	 * The activity has regained focus. Acquire a wake lock.
	 */
	public void onResume() {
	    super.onResume();
	    ctfWakeLock.acquire();
	}
	
	/**
	 * The activity has lost focus. Release the wake lock.
	 */
	public void onPause(){
	   super.onPause();
	   ctfWakeLock.release();
	}
	
	/**
	 * Handles action when the Menu button is pressed.
	 * Toggles the graphical menu.
	 */
	public boolean onCreateOptionsMenu(Menu x) {
		
		myMenu.toggleMenu();
		
		// True means the native menu was launched successfully, so we must return false!
		return false;
		
	}
			
	/**
	 * Centers the map view on the requested centering target.
	 */
	public void centerMapView(int isCentering) {
		
		// We can't center if there isn't any data!
		if(myGameData == null || myGameData.hasError()) {
			return;
		}
		
		GeoPoint location;
		switch(isCentering) {
		
			case CENTER_ORIGIN:
				location = myGameData.getOrigin();
				break;
				
			case CENTER_SELF:
				location = new GeoPoint((int) (1E6 * CurrentUser.getLatitude()), (int) (1E6 * CurrentUser.getLongitude()));
				break;
				
			case CENTER_RED:
				location = myGameData.getRedFlag();
				break;
				
			case CENTER_BLUE:
				location = myGameData.getBlueFlag();
				break;
				
			default:
				return;
		}
		
		// Reset centering
		isCentering = CENTER_NONE;
		
		// Move the map view
		if(location != null) {
			mapView.getController().animateTo(location);
		}
		
	}
	
	/**
	 * Sets up all the drawables to be used as markers
	 */
	private void buildDrawables() {
		
		// All of our markers here
		drawable.put(R.drawable.star, this.getResources().getDrawable(R.drawable.star));
		drawable.put(R.drawable.red_flag, this.getResources().getDrawable(R.drawable.red_flag));
		drawable.put(R.drawable.blue_flag, this.getResources().getDrawable(R.drawable.blue_flag));
		drawable.put(R.drawable.person_red_owner, this.getResources().getDrawable(R.drawable.person_red_owner));
		drawable.put(R.drawable.person_blue_owner, this.getResources().getDrawable(R.drawable.person_blue_owner));
		drawable.put(R.drawable.person_red, this.getResources().getDrawable(R.drawable.person_red));
		drawable.put(R.drawable.person_blue, this.getResources().getDrawable(R.drawable.person_blue));
		drawable.put(R.drawable.grey_observer, this.getResources().getDrawable(R.drawable.grey_observer));
		drawable.put(R.drawable.grey_observer_owner, this.getResources().getDrawable(R.drawable.grey_observer_owner));
		drawable.put(R.drawable.selection_reticle, this.getResources().getDrawable(R.drawable.selection_reticle));
		
		// Set the anchors here
		Collection<Drawable> c = drawable.values();
	    Iterator<Drawable> itr = c.iterator();
	    while(itr.hasNext()) {
	    	Drawable icon = itr.next();
			int redW = icon.getIntrinsicWidth();
			int redH = icon.getIntrinsicHeight();
			icon.setBounds(-redW / 2, -redH, redH / 2, 0);
	  	}

	}
	
	/**
	 * Moves the flag based on the isMovingFlag value. (If allowed!)
	 * @param location
	 */
	public void moveFlag(final GeoPoint loc) {
		
		// If a user doesn't have permissions, don't let them move the flag :)
		if(!myGameData.getCreator().equals(CurrentUser.getUID())) {
			return;
		}
		
		// Lets call for a flag move, we don't really care about the response
		new Thread(new Runnable() {
			public void run() {
				
				String team = "";
				if(isMovingFlag == MOVING_FLAG_RED) {
					team = "red";
				} else if(isMovingFlag == MOVING_FLAG_BLUE) {
					team = "blue";
				}
				
				isMovingFlag = MOVING_FLAG_NONE;
				
				if(!team.equals("")) {
			        List<NameValuePair> params = new ArrayList<NameValuePair>(2);  
		            params.add(new BasicNameValuePair("latitude", "" + (loc.getLatitudeE6() / 1E6)));
		            params.add(new BasicNameValuePair("longitude", "" + (loc.getLongitudeE6() / 1E6)));
		            params.add(new BasicNameValuePair("user_id", CurrentUser.getUID()));
		            params.add(new BasicNameValuePair("game_id", CurrentUser.getGameId()));
		            params.add(new BasicNameValuePair("team", team));
		            try {
						HttpPost req = new HttpPost(StetsonCTF.SERVER_URL + "/flag/");
						req.setEntity(new UrlEncodedFormEntity(params));
						String data = Connections.sendRequest(req);
						Log.i(TAG, "FLAG MOVE: " + data);
					} catch (UnsupportedEncodingException e) {
						e.printStackTrace();
					}
				}

			}
		}).start();
		
	}
	
	/**
	 * Returns false (required by MapActivity)
	 * @return false
	 */
	protected boolean isRouteDisplayed() {
		return false;
	}	
	
	/**
	 * Game processor. Runs the GameProcess task every GAME_UPDATE_DELAY (ms).
	 */
	private final Runnable gameProcess = new Runnable() {
	    public void run() {
			// Run the task only if the previous one is null or finished
			// AsyncTasks are designed to run only ONCE per lifetime
			if(cycle == null || cycle.getStatus() == AsyncTask.Status.FINISHED) {
	    		cycle = new TaskGameProcess();
	    		cycle.execute();
			}
			
			// Call for another execution later
			gameHandler.postDelayed(this, GAME_UPDATE_DELAY);
	
	   }
	};
	
	/**
     * The AsyncTask used for processing game updates.
     * (Generics: Params, Progress, Result)
     */
	public class TaskGameProcess extends AsyncTask<Void, Void, JSONObject> {
		
		/**
		 * Run as the work on another thread.
		 * Sends a location update and grabs data from the server.
		 */
		protected JSONObject doInBackground(Void... params) {
			Log.i(TAG, "Grabbing game data...");
			return Connections.getGameData();
		}
		
		/**
		 * Run after execution on the UI thread.
		 * Processes the game object retrieved from the worker thread.
		 * If game is NULL then the game will be stopped and the activity terminated.
		 */
		protected void onPostExecute(final JSONObject gameObject) {		
			
			myGameData.parseJSONObject(gameObject);
			
			// Handle Errors
			if(myGameData.hasError()) {
				Log.e(TAG, "Error: " + myGameData.getErrorMessage());
				return;
			}
			
			// No errors, parsing was a success!
			Log.i(TAG, "GameProcess()...");
			
			// Is this the first map data we have gotten?
			this.firstCenter();
			
			// Remove all overlay items
			mapOverlay.clear();
			mapOverlayMarkers.clear();
						
			// Update game info bar
			myInfoBar.update(myGameData.getRedScore(), myGameData.getBlueScore(), CurrentUser.getAccuracy());
			
			// Add flags
			addFlagMarker(myGameData.getRedFlag(), "Red Flag", R.drawable.red_flag);
			addFlagMarker(myGameData.getBlueFlag(), "Blue Flag", R.drawable.blue_flag);
					
			// Add players
			Player player;
			for(int p = 0; p < myGameData.getPlayerCount(); p++) {
				player = myGameData.getPlayer(p);
				this.addPlayerMarker(player);
				Log.i(TAG, "Added player: " + player.getName());
			}
			
			// Add boundaries & markers
			mapOverlay.add(myGameData.getBounds());
		    mapOverlay.add(mapOverlayMarkers);
		    
		    // Let the make know we're done!
		    mapView.invalidate();
			
		}
	
		/**
		 * If the map hasn't been centered around the origin and smart-zoomed
		 * this function will take care of that.
		 */
		protected void firstCenter() {
			if(!hasCenteredOrigin) {
				
				hasCenteredOrigin = true;
				
				GeoPoint topLeft = myGameData.getBounds().getTopLeft();
				GeoPoint botRight = myGameData.getBounds().getBottomRight();
				
				int maxLatitude = topLeft.getLatitudeE6();
				int minLatitude = botRight.getLatitudeE6();
				int maxLongitude = topLeft.getLongitudeE6();
				int minLongitude = botRight.getLongitudeE6();
				
				mapView.getController().zoomToSpan((maxLatitude - minLatitude),(maxLongitude - minLongitude));
				centerMapView(GameCTF.CENTER_ORIGIN);
			}
		}
		
		/**
		 * Adds a player marker to the map using the given player data.
		 * @param player
		 */
		protected void addPlayerMarker(Player player) {
			
			GeoPoint playerPoint = new GeoPoint(player.getLatitude(), player.getLongitude());
			OverlayItem playerItem = new OverlayItem(playerPoint, GameCTFOverlays.OVERLAY_PLAYER, player.getUID());
			
			// If we have menu focus on this person, show a reticle
			if(myMenu.getMenuFocus().equals(player.getUID())) {
				this.addSelectionReticle(playerPoint);
			}
			
			boolean isCurrentPlayer = player.getUID().equals(CurrentUser.getUID());
			
			// if player is observer, we don't care about their team
			if(player.hasObserverMode()) {
				if(isCurrentPlayer) {
					playerItem.setMarker(drawable.get(R.drawable.grey_observer_owner));
				} else {
					playerItem.setMarker(drawable.get(R.drawable.grey_observer));
				}
			
			// if player is on the red team
			} else if (player.getTeam().equals("red")) {
				
				// Default marker for a red member
				playerItem.setMarker(drawable.get(R.drawable.person_red));
				
				// Logical order: flag, observer, self
				if(player.hasFlag()) {
					playerItem.setMarker(drawable.get(R.drawable.blue_flag));
				} else if(isCurrentPlayer) {
					playerItem.setMarker(drawable.get(R.drawable.person_red_owner));
				}
			
			// if player is on the blue team
			} else if(player.getTeam().equals("blue")) {
				
				// Default marker for a blue member
				playerItem.setMarker(drawable.get(R.drawable.person_blue));
				
				// Logical order: flag, observer, self
				if(player.hasFlag()) {
					playerItem.setMarker(drawable.get(R.drawable.red_flag));
				} else if(isCurrentPlayer) {
					playerItem.setMarker(drawable.get(R.drawable.person_blue_owner));
				}
			}

			mapOverlayMarkers.addOverlay(playerItem);
		}
		
		/**
		 * Adds a flag given the location, title and drawable id.
		 * @param location
		 * @param team
		 */
		protected void addFlagMarker(GeoPoint location, String title, int iconId) {
			
			// Add selection reticles
			if(myMenu.getMenuFocus().equals(title)) {
				this.addSelectionReticle(location);
			}
			
			// Add flag
			OverlayItem flag = new OverlayItem(location, GameCTFOverlays.OVERLAY_FLAG, title);
			flag.setMarker(drawable.get(iconId));
			mapOverlayMarkers.addOverlay(flag);
			
		}
		
		/**
		 * Adds a selection reticle at the given location.
		 * @param location
		 */
		protected void addSelectionReticle(GeoPoint location) {
			OverlayItem reticleItem = new OverlayItem(location, GameCTFOverlays.OVERLAY_OTHER, "");
			reticleItem.setMarker(drawable.get(R.drawable.selection_reticle));
			mapOverlayMarkers.addOverlay(reticleItem);
		}
	
	}
	
	/**
	 * Returns a reference to the game's menu
	 * @return
	 */
	public GameScores getGameScores() {
		return myScores;
	}
	
	/**
	 * Returns a reference to the game's menu
	 * @return
	 */
	public GameMenu getGameMenu() {
		return myMenu;
	}
	
	/**
	 * Returns a reference to the game's game data
	 * @return
	 */
	public GameData getGameData() {
		return myGameData;
	}
	
	/**
	 * Change if the user is currently moving a flag
	 * @param moving
	 */
	public void setMovingFlag(int moving) {
		isMovingFlag = moving;
	}
	
	/**
	 * Is the user is currently moving a flag.
	 * @return
	 */
	public int isMovingFlag() {
		return isMovingFlag;
	}

}
