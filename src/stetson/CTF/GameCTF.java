package stetson.CTF;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;

import java.util.List;

import org.apache.http.client.methods.HttpPost;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.os.PowerManager;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.OverlayItem;


public class GameCTF extends MapActivity {
	
	// Constant: How often should we wait between game update cycles?
	public static final int GAME_UPDATE_DELAY = 2500;
	
	// Constants: Where should we center to on the next game update cycle?
	public static final int CENTER_NONE = -1;
	public static final int CENTER_ORIGIN = 0;
	public static final int CENTER_SELF = 1;
	public static final int CENTER_RED = 2;
	public static final int CENTER_BLUE = -3;
	
	// Constant: What is the minimum accuracy (in meters) we should expect ?
	public static final int MIN_ACCURACY = 40;
	
	// Data members
	private MapView mapView;
	private Handler gameHandler = new Handler();
	private static final String TAG = "GameCTF";
	private boolean isBlueFlagTaken = false;
	private boolean isRedFlagTaken = false;
	private TaskGameProcess cycle;
	
	MapController mapController;
	GameCTFOverlays itemizedoverlay;
	OverlayItem overlayitem;
	List<Overlay> mapOverlays;
	
	boolean isRunning = false;
	int isCentering = CENTER_NONE;
	
	private HashMap<Integer,Drawable> drawable = new HashMap<Integer, Drawable>(10);
	
	Boundaries bounds;
	
	//Dim Wake Lock
	private PowerManager.WakeLock ctfWakeLock;
	
	
	/**
	 * Called when the activity is first created.
	 * Default behavior is NULL. Nothing is happening yet!
	 * @param saved instance state
	 */
	public void onCreate(Bundle savedInstanceState) {
		
		Log.i(TAG, "Starting map activity...");
		isRunning = true;
		isCentering = CENTER_ORIGIN;
		
		// Restore a saved instance of the application
		super.onCreate(savedInstanceState);
		
		// Make sure the user is actually in a game
		if(CurrentUser.getGameId().equals("")) {
			this.stopGame();
			return;
		}
		
		// Move back to the game selection panel
		setContentView(R.layout.game);
		
		// Make sure gps is running at the right speed
		CurrentUser.userLocation((LocationManager) this.getSystemService(Context.LOCATION_SERVICE), StetsonCTF.GPS_UPDATE_FREQUENCY_GAME);
		
 		// Turns on built-in zoom controls
		mapView = (MapView) findViewById(R.id.mapView);
		mapController = mapView.getController();
		mapView.setBuiltInZoomControls(true);
		
		// Setting up the overlay marker images

				
		// Clear game info
		TextView text;
		text = (TextView) findViewById(R.id.gameInfo_red);
		text.setText(getString(R.string.game_info_loading));
		text = (TextView) findViewById(R.id.gameInfo_blue);
		text.setText("");
		text = (TextView) findViewById(R.id.gameInfo_connection);
		text.setText("");
		bounds = new Boundaries();
		
		// Setup the wake lock
		PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
		ctfWakeLock = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, TAG);
		
		// Setup menu button listeners
		buildDrawables();
		buildMenuListeners();
		
		// Start game processor
		gameHandler.postDelayed(gameProcess, GAME_UPDATE_DELAY);

	}
	
	/**
	 * When the activity is ended, we need to clear the users game and location.
	 */
	public void onDestroy() {
		
		// No more game, stop running
		isRunning = false;
		
		super.onDestroy();
		
		Log.i(TAG, "Stopping Map Activity");
		CurrentUser.setGameId("");
		CurrentUser.setLocation(-1, -1);
		CurrentUser.setAccuracy(-1);
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
		
		// Toggle the visibility of the menu
		LinearLayout menu = (LinearLayout) this.findViewById(R.id.gameMenu);
		if(menu.getVisibility() == LinearLayout.VISIBLE) {
			menu.setVisibility(LinearLayout.GONE);
		} else {
			menu.setVisibility(LinearLayout.VISIBLE);
		}
		
		// True means the native menu was launched successfully, so we must return false!
		return false;
	}
	
	

	/**
	 * Removes the user from the game and stops contact with the server.
	 * Stops game processing if it is in progress and prohibits it from running until
	 * it is created again (in a new activity).
	 */
	public void stopGame() {
		new TaskGameProcess().cancel(true);
		isRunning = false;
		this.finish();
	}
	
	/**
	 * Handles incoming menu clicks.
	 */
	private OnClickListener onMenuClick = new OnClickListener() {
		public void onClick(View v) {
			switch(v.getId()) {
				
				case R.id.menu_self:
					isCentering = CENTER_SELF;
					break;
					
				case R.id.menu_red_flag:
					isCentering = CENTER_RED;
					break;
					
				case R.id.menu_blue_flag:
					isCentering = CENTER_BLUE;
					break;
					
				case R.id.menu_scores:
					// display score board
					break;
					
				case R.id.menu_quit:
					stopGame();
					break;
					
			}
		}
	};
	
	/**
	 * Adds an onClick listener to each of the menu items.
	 */
	private void buildMenuListeners() {
		
		findViewById(R.id.menu_self).setOnClickListener(onMenuClick);
		findViewById(R.id.menu_red_flag).setOnClickListener(onMenuClick);
		findViewById(R.id.menu_blue_flag).setOnClickListener(onMenuClick);
		findViewById(R.id.menu_scores).setOnClickListener(onMenuClick);
		findViewById(R.id.menu_quit).setOnClickListener(onMenuClick);

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
		
		// Set the anchors here
		Collection<Drawable> c = drawable.values();
	    Iterator<Drawable> itr = c.iterator();
	    while(itr.hasNext()) {
	    	Drawable icon = itr.next();
			int redW = icon.getIntrinsicWidth();
			int redH = icon.getIntrinsicHeight();
			icon.setBounds(-redW / 2, -redH, redH / 2, 0);
	  	}

	    // Setup overlay stuff
		mapOverlays = mapView.getOverlays();
        itemizedoverlay = new GameCTFOverlays(drawable.get(R.drawable.star), mapView);
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

	    	// Is the game still in progress?
	    	if(isRunning) {
	    		
	    		
	    		// Run the task only if the previous one is null or finished
	    		// AsyncTasks are designed to run only ONCE per lifetime
	    		if(cycle == null || cycle.getStatus() == AsyncTask.Status.FINISHED) {
		    		cycle = new TaskGameProcess();
		    		cycle.execute();
	    		}
	    		
	    		// Call for another execution later
	    		gameHandler.postDelayed(this, GAME_UPDATE_DELAY);
	    		
	    	}
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
			
			HttpPost req = new HttpPost(StetsonCTF.SERVER_URL + "/location/");
			CurrentUser.buildHttpParams(req, CurrentUser.UPDATE_PARAMS);
			String data = Connections.sendRequest(req);
			try {
				JSONObject jObject = new JSONObject(data);
				return jObject;
			} catch (JSONException e) {
				Log.e(TAG, "Error parsing JSON.", e);
			}
			
			// If we get here, we had problems with json.
			return null;
			
		}
		
		/**
		 * Run after execution on the UI thread.
		 * Processes the game object retrieved from the worker thread.
		 * If game is NULL then the game will be stopped and the activity terminated.
		 */
		protected void onPostExecute(final JSONObject gameObject) {		
			
			// Stop game if the game is null
			if(gameObject == null) {
				stopGame();
				return;
			}
			
			if(!isValidGameObject(gameObject)) {
				Log.e(TAG, "Invalid game object!");
				return;
			}
			
			Log.i(TAG, "Processing game data.");
			
			// Clear all overlays
			mapOverlays.clear();
			itemizedoverlay.clear();
			
			// For catching unforeseen errors
			try {

				// Reset flag booleans
				isRedFlagTaken = false;
				isBlueFlagTaken = false;
				
				// Run the processing functions...
				processCentering(gameObject);
				processPlayers(gameObject);
				processGame(gameObject);
				
			    // Add map overlays
				mapOverlays.add(bounds);
			    mapOverlays.add(itemizedoverlay);
			    mapView.invalidate();
			    
			} catch (Exception e) {
				Log.e(TAG, "Critical Error!", e);
			}
			
		}
		
		/**
	     * If there is a request to center around the origin, do it.
	     * @param jObject
	     */
	    private void processCentering(JSONObject jObject) {
	    	
	    	// Do we even have a request to center?
	    	if(isCentering != CENTER_NONE) {
	    		
	    		Log.i(TAG, "Centering = " + isCentering);
	    		
	    		
	    		int lati = 0; 
	    		int loni = 0;
	    		JSONObject subObject;
	    		
				try {
					
					if (isCentering == CENTER_SELF) {
				    	lati = (int) (1E6 * CurrentUser.getLatitude());
				    	loni = (int) (1E6 * CurrentUser.getLongitude());
					} else {
						
						if(isCentering == CENTER_ORIGIN) {
							subObject = jObject.getJSONObject("origin");
						} else if (isCentering == CENTER_RED) {
							subObject = jObject.getJSONObject("red_flag");
						} else if (isCentering == CENTER_BLUE) {
							subObject = jObject.getJSONObject("blue_flag");
						} else {
							isCentering = CENTER_NONE;
							return;
						}
						
				    	lati = (int) (1E6 * Double.parseDouble(subObject.getString("latitude")));
				    	loni = (int) (1E6 * Double.parseDouble(subObject.getString("longitude")));
					}
					
					Log.i(TAG, "Centering @ LAT " + lati + ", LONG " + loni);
			    	GeoPoint origin = new GeoPoint(lati, loni);
			    	mapView.getController().animateTo(origin);
			    	
			    	
				} catch (JSONException e) {
					Log.e(TAG, "Error centering on target!", e);
				}
				
				isCentering = CENTER_NONE;
	    	}
	    	
	    }
	    
	    /**
	     * Handles other players.
	     * @param jSubObj containing all players.
	     */
	    private void processPlayers(JSONObject jObject) {
			try {
	
				// Loop through all players
				JSONObject jSubObj = jObject.getJSONObject("players");
				
				
				JSONObject player;
				String playerKey;
				
				Iterator<String> plrIterator = jSubObj.keys();
			    while(plrIterator.hasNext()) {
			    	
			    	playerKey = plrIterator.next();
			    	player = jSubObj.getJSONObject(playerKey);
			    	

			    	if(player.has("team")) {

				    	int lati = (int) (1E6 * Double.parseDouble(player.getString("latitude")));
				    	int loni = (int) (1E6 * Double.parseDouble(player.getString("longitude")));
	
						Log.i(TAG, "Adding player: " + player.getString("name") + " with  KEY=" + playerKey + " @ LAT " + player.getString("latitude") + ", LONG " + player.getString("longitude"));
						GeoPoint marker = new GeoPoint(lati, loni);
						OverlayItem overlayitem = new OverlayItem(marker, "Player: " + player.getString("name"), player.getString("name"));
						
						boolean isCurrentPlayer = playerKey.equals(CurrentUser.getUID());
						boolean isObserver = player.getBoolean("observer_mode");
						boolean hasFlag = player.getBoolean("has_flag");
						
						String team = player.getString("team");
						
						// Set observer mode bool for current player
						if(isCurrentPlayer) {
							CurrentUser.setIsObserver(isObserver);
						}
						
						// if player is on the red team
						if(team.equals("red")) {
							
							// Default marker for a red member
							overlayitem.setMarker(drawable.get(R.drawable.person_red));
							
							// Logical order: flag, observer, self
							if(hasFlag) {
								overlayitem.setMarker(drawable.get(R.drawable.blue_flag));
								isBlueFlagTaken = true;
							} else if(isObserver) {
								// set observer mode image here
							} else if(isCurrentPlayer) {
								overlayitem.setMarker(drawable.get(R.drawable.person_red_owner));
							}
						
						// if player is on the blue team
						} else if(team.equals("blue")) {
							
							// Default marker for a blue member
							overlayitem.setMarker(drawable.get(R.drawable.person_blue));
							
							// Logical order: flag, observer, self
							if(hasFlag) {
								overlayitem.setMarker(drawable.get(R.drawable.red_flag));
								isRedFlagTaken = true;
							} else if(isObserver) {
								// set observer mode image here
							} else if(isCurrentPlayer) {
								overlayitem.setMarker(drawable.get(R.drawable.person_blue_owner));
							}
							
						}
												
						// Done? Lets add it :D
						itemizedoverlay.addOverlay(overlayitem);
					}

			    }

			} catch (JSONException e) {
				Log.e(TAG, "Error in gameProcess().processPlayers()", e);
			}
			
	    }
		    
	    /**
	     * Handles game data, such as flags and bounds.
	     * @param jSubObj containing the entire game json object.
	     */
	    private void processGame(JSONObject jSubObj) {
			try {
				
				JSONObject game = jSubObj;
				
				// Display game info bar details
				TextView text;
				text = (TextView) findViewById(R.id.gameInfo_red);
				text.setText(getString(R.string.game_info_red) + game.getString("red_score"));
				text = (TextView) findViewById(R.id.gameInfo_blue);
				text.setText(getString(R.string.game_info_blue) + game.getString("blue_score"));
				text = (TextView) findViewById(R.id.gameInfo_connection);
				text.setText(getString(R.string.game_info_accuracy) + CurrentUser.getAccuracy());
				
				int lat,lon;
				
				// Adding red flag, if it hasn't been taken
				if(!isRedFlagTaken) {
					JSONObject red_flag = game.getJSONObject("red_flag");
					lat = (int) (1E6 * Double.parseDouble(red_flag.getString("latitude")));
			    	lon = (int) (1E6 * Double.parseDouble(red_flag.getString("longitude")));
	
					GeoPoint red_marker = new GeoPoint(lat, lon);
					OverlayItem red_overlayitem = new OverlayItem(red_marker, "Red Flag", "");
					red_overlayitem.setMarker(drawable.get(R.drawable.red_flag));
					itemizedoverlay.addOverlay(red_overlayitem);
					
					Log.i(TAG, "Adding red_flag: " + red_flag.getString("latitude") + red_flag.getString("longitude"));
				}
				
				// Adding blue flag, if it hasn't been taken
				if(!isBlueFlagTaken) {
					JSONObject blue_flag = game.getJSONObject("blue_flag");
					lat = (int) (1E6 * Double.parseDouble(blue_flag.getString("latitude")));
			    	lon = (int) (1E6 * Double.parseDouble(blue_flag.getString("longitude")));
	
					GeoPoint blue_marker = new GeoPoint(lat, lon);
					OverlayItem blue_overlayitem = new OverlayItem(blue_marker, "Blue Flag", "");
					blue_overlayitem.setMarker(drawable.get(R.drawable.blue_flag));
					
					itemizedoverlay.addOverlay(blue_overlayitem);
					
					Log.i(TAG, "Adding blue_flag: " + blue_flag.getString("latitude") + blue_flag.getString("longitude"));
				}
				
				// Get Red boundaries
				JSONObject redBounds = game.getJSONObject("red_bounds");
				JSONObject redTopLeft = redBounds.getJSONObject("top_left");
				lat = (int) (1E6 * Double.parseDouble(redTopLeft.getString("latitude")));
		    	lon = (int) (1E6 * Double.parseDouble(redTopLeft.getString("longitude")));
		    	GeoPoint redTopLeftBoundary = new GeoPoint(lat, lon);
		    	JSONObject redBottomRight = redBounds.getJSONObject("bottom_right");
				lat = (int) (1E6 * Double.parseDouble(redBottomRight.getString("latitude")));
		    	lon = (int) (1E6 * Double.parseDouble(redBottomRight.getString("longitude")));
		    	GeoPoint redBottomRightBoundary = new GeoPoint(lat, lon);
		    	bounds.setRedBounds(redTopLeftBoundary, redBottomRightBoundary);
		    	
		    	// Get blue  boundaries
		    	JSONObject blueBounds = game.getJSONObject("blue_bounds");
				JSONObject blueTopLeft = blueBounds.getJSONObject("top_left");
				lat = (int) (1E6 * Double.parseDouble(blueTopLeft.getString("latitude")));
		    	lon = (int) (1E6 * Double.parseDouble(blueTopLeft.getString("longitude")));
		    	GeoPoint blueTopLeftBoundary = new GeoPoint(lat, lon);
		    	JSONObject blueBottomRight = blueBounds.getJSONObject("bottom_right");
				lat = (int) (1E6 * Double.parseDouble(blueBottomRight.getString("latitude")));
		    	lon = (int) (1E6 * Double.parseDouble(blueBottomRight.getString("longitude")));
		    	GeoPoint blueBottomRightBoundary = new GeoPoint(lat, lon);
		    	bounds.setBlueBounds(blueTopLeftBoundary, blueBottomRightBoundary);
		    	
				
			    
			} catch (JSONException e) {
				Log.e(TAG, "Error in gameProcess().processGame()", e);
			}
	    }
	    
	    /**
	     * Is the game object provided valid?
	     * @param jSubObj
	     * @return
	     */
	    private boolean isValidGameObject(JSONObject game) {
    		if(!game.has("origin") || !game.has("red_flag") || !game.has("blue_flag")) {
    			Log.e(TAG, "Missing basic game data!");
    			return false;
    		}
    		if(!game.has("red_bounds") || !game.has("blue_bounds")) {
    			Log.e(TAG, "Missing bounds data!");
    			return false;
    		}
    		if(!game.has("players")) {
    			Log.e(TAG, "Missing bounds data!");
    			return false;
    		}
	    	return true;
	    }
	}
}
