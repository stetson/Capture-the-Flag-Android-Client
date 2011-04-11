package stetson.CTF;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;

import java.util.List;

import org.apache.http.client.methods.HttpPost;
import org.json.JSONException;
import org.json.JSONObject;

import stetson.CTFGame.GameCTFOverlays;
import stetson.CTFGame.GameData;
import stetson.CTFGame.Player;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.TextView;
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
	
	// Data members
	private GameData myGameData;
	private PowerManager.WakeLock ctfWakeLock;
	private MapView mapView;
	private Handler gameHandler = new Handler();
	private TaskGameProcess cycle;
	private int isCentering = CENTER_NONE;
	
	private List<Overlay> mapOverlay;
	private GameCTFOverlays mapOverlayMarkers;
	
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
		isCentering = CENTER_ORIGIN;
		
		// Restore a saved instance of the application
		super.onCreate(savedInstanceState);
		
		// Make sure the user is actually in a game
		if(CurrentUser.getGameId().equals("")) {
			this.stopGame();
			return;
		}
		
		// Setup the wake lock
		PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
		ctfWakeLock = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, TAG);
		
		// Move back to the game selection panel
		setContentView(R.layout.game);
		
		// Make sure gps is running at the right speed
		CurrentUser.userLocation((LocationManager) this.getSystemService(Context.LOCATION_SERVICE), StetsonCTF.GPS_UPDATE_FREQUENCY_GAME);
		
 		// Turns on built-in zoom controls
		mapView = (MapView) findViewById(R.id.mapView);
		mapView.getController();
		mapView.setBuiltInZoomControls(true);
		
		// Build everything we need to run
		buildDrawables();
		buildMenuListeners();
		
		// Let the user know we're loading
		clearGameInfo();
		
	    // Setup overlay stuff
		mapOverlay = mapView.getOverlays();
        mapOverlayMarkers = new GameCTFOverlays(drawable.get(R.drawable.star), mapView);
        
        myGameData = new GameData();
		gameHandler.postDelayed(gameProcess, GAME_UPDATE_DELAY);
		
		mapView.setLongClickable(true);
		registerForContextMenu(mapView);
	}

    public void onCreateContextMenu(ContextMenu menu, View v,ContextMenuInfo menuInfo) {
    	super.onCreateContextMenu(menu, v, menuInfo);
		menu.setHeaderTitle("Context Menu");
		menu.add(0, v.getId(), 0, "Action 1");
		menu.add(0, v.getId(), 0, "Action 2");
	}

	/**
	 * When the activity is ended, we need to clear the users game and location.
	 */
	public void onDestroy() {
		
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
			centerMapView();
		}
	};
	
	/*----------------------------------*
	/* GAME RELATED FUNCTIONS START
	/*----------------------------------*/
	
	/**
	 * Centers the map view on the requested centering target.
	 */
	public void centerMapView() {
		
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
	 * Sets the game info bar to a loading state.
	 */
	public void clearGameInfo() {
		TextView text;
		text = (TextView) findViewById(R.id.gameInfo_red);
		text.setText(getString(R.string.game_info_loading));
		text = (TextView) findViewById(R.id.gameInfo_blue);
		text.setText("");
		text = (TextView) findViewById(R.id.gameInfo_connection);
		text.setText("");
	}
	
	/**
	 * Updates the game info bar.
	 */
	public void updateGameInfo() {
		
		if(myGameData == null || myGameData.hasError()) {
			return;
		}
		
		TextView text;
		text = (TextView) findViewById(R.id.gameInfo_red);
		text.setText(getString(R.string.game_info_red) + myGameData.getRedScore());
		text = (TextView) findViewById(R.id.gameInfo_blue);
		text.setText(getString(R.string.game_info_blue) + myGameData.getBlueScore());
		text = (TextView) findViewById(R.id.gameInfo_connection);
		text.setText(getString(R.string.game_info_accuracy) + CurrentUser.getAccuracy());
		
	}
	
	/**
	 * Removes the user from the game and stops contact with the server.
	 * Stops game processing if it is in progress and prohibits it from running until
	 * it is created again (in a new activity).
	 */
	public void stopGame() {
		
		if(cycle != null) {
			cycle.cancel(true);
		}
		
		this.finish();
		
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
			
			myGameData.parseJSONObject(gameObject);
			
			// Handle Errors
			if(myGameData.hasError()) {
				Log.e(TAG, "Error: " + myGameData.getErrorMessage());
				return;
			}
			
			// No errors, parsing was a success!
			Log.i(TAG, "GameProcess()...");
			
			// Remove all overlay items
			mapOverlay.clear();
			mapOverlayMarkers.clear();
			
			// Make sure we update our centering target!
			if(isCentering != CENTER_NONE) {
				centerMapView();
			}
			
			// Update game info bar
			updateGameInfo();
			
			// Add Flags
			if(!myGameData.isRedFlagTaken()) {
				OverlayItem redFlag = new OverlayItem(myGameData.getRedFlag(), "Red Flag", "");
				redFlag.setMarker(drawable.get(R.drawable.red_flag));
				mapOverlayMarkers.addOverlay(redFlag);
				Log.i(TAG, "Added red flag.");
			}
			
			if(!myGameData.isBlueFlagTaken()) {
				OverlayItem blueFlag = new OverlayItem(myGameData.getBlueFlag(), "Blue Flag", "");
				blueFlag.setMarker(drawable.get(R.drawable.blue_flag));
				mapOverlayMarkers.addOverlay(blueFlag);
				Log.i(TAG, "Added blue flag.");
			}
			
			// Add players
			Player player;
			
			for(int p = 0; p < myGameData.getPlayerCount(); p++) {
				
				player = myGameData.getPlayer(p);
	
				Log.i(TAG, "Added player: " + player.getName());
				
				GeoPoint playerPoint = new GeoPoint(player.getLatitude(), player.getLongitude());
				OverlayItem playerItem = new OverlayItem(playerPoint, "Player: " + player.getName(), "");
				
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
			
			// Add boundaries & markers
			mapOverlay.add(myGameData.getBounds());
		    mapOverlay.add(mapOverlayMarkers);
		    
		    // Let the make know we're done!
		    mapView.invalidate();
			
		}
	}
}
