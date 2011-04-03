package stetson.CTF;

import java.util.Iterator;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.OverlayItem;

public class GameCTF extends MapActivity {
	
	// Delay in gameProcess (in ms) [2.5 seconds]
	public static final int GAME_UPDATE_DELAY = 2500;
	
	public static final int CENTER_NONE = -1;
	public static final int CENTER_ORIGIN = 0;
	public static final int CENTER_SELF = 1;
	public static final int CENTER_RED = 2;
	public static final int CENTER_BLUE = -3;
	// accuracy in meters
	public static final int MIN_ACCURACY = 40;
	
	// Data members
	private MapView mapView;
	private Handler gameHandler = new Handler();
	private static final String TAG = "GameCTF";
	
	MapController mapController;
	GameCTFOverlays itemizedoverlay;
	OverlayItem overlayitem;
	List<Overlay> mapOverlays;
	
	boolean isRunning = false;
	int isCentering = CENTER_NONE;
	
	private Drawable drawable_self;
	private Drawable drawable_red_flag;
	private Drawable drawable_blue_flag;
	private Drawable drawable_red_player;
	private Drawable drawable_blue_player;
	Boundaries bounds;
	
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
			this.finish();
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
		drawable_self = this.getResources().getDrawable(R.drawable.star);
		drawable_self.setBounds(0, 0, drawable_self.getIntrinsicWidth(), drawable_self.getIntrinsicHeight());
		
		drawable_red_flag = this.getResources().getDrawable(R.drawable.red_flag);
		int redW = drawable_red_flag.getIntrinsicWidth();
		int redH = drawable_red_flag.getIntrinsicHeight();
		drawable_red_flag.setBounds(-redW / 2, -redH, redH / 2, 0);
		
		drawable_blue_flag = this.getResources().getDrawable(R.drawable.blue_flag);
		int blueW = drawable_blue_flag.getIntrinsicWidth();
		int blueH = drawable_blue_flag.getIntrinsicHeight();
		drawable_blue_flag.setBounds(-blueW / 2, -blueH, blueH / 2, 0);
		
		drawable_red_player = this.getResources().getDrawable(R.drawable.person_red);
		drawable_red_player .setBounds(0, 0, drawable_red_player.getIntrinsicWidth(), drawable_red_player.getIntrinsicHeight());
		
		drawable_blue_player = this.getResources().getDrawable(R.drawable.person_blue);
		drawable_blue_player.setBounds(0, 0, drawable_blue_player.getIntrinsicWidth(), drawable_blue_player.getIntrinsicHeight());
		
		mapOverlays = mapView.getOverlays();
        itemizedoverlay = new GameCTFOverlays(drawable_self);
		
		// Start game processor
		gameHandler.postDelayed(gameProcess, GAME_UPDATE_DELAY);
		
		// Clear game info
		TextView text;
		text = (TextView) findViewById(R.id.gameInfo_red);
		text.setText(getString(R.string.game_info_loading));
		text = (TextView) findViewById(R.id.gameInfo_blue);
		text.setText("");
		text = (TextView) findViewById(R.id.gameInfo_connection);
		text.setText("");
		bounds = new Boundaries();
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
	 * Called when the menu is requested.
	 */
	public boolean onCreateOptionsMenu(Menu menu) {
	   MenuInflater inflater = getMenuInflater();
	   inflater.inflate(R.menu.game_menu, menu);
	   return true;
	}
	
	/**
	 * Handle menu selections.
	 */
	public boolean onOptionsItemSelected(MenuItem item) {
	    
		// which option?
	    switch (item.getItemId()) {

		    case R.id.center_self:
		    	isCentering = CENTER_SELF;
		    	return true;
		    	
		    case R.id.center_red:
		    	isCentering = CENTER_RED;
		    	return true;
		    	
		    case R.id.center_blue:
		    	isCentering = CENTER_BLUE;
		    	return true;
		    	
		    case R.id.game_leave:
		    	this.finish();
		    	return true;

	    	// Can find what we're looking for? Call super
		    default:
		        return super.onOptionsItemSelected(item);
	    }
	}
	/**
	 * Game processor. Runs every GAME_UPDATE_DELAY (ms).
	 */
	private final Runnable gameProcess = new Runnable()
	{
		/**
		 * Main gameProcess function.
		 */
	    public void run() 
	    {
	    	
	    	// Don't run if we don't have a game anymore
	    	if(!isRunning) {
	    		return;
	    	}
	    	
	    	Log.i(TAG, "Game Process()");
	    	
	    	
	    	// If our accuracy doesn't suck, update
	    	if(CurrentUser.getAccuracy() < MIN_ACCURACY) {
	    		String gameUrl = CurrentUser.getGameId().replaceAll(" ", "%20");
				HttpPost req = new HttpPost(StetsonCTF.SERVER_URL + "/game/" + gameUrl);
				CurrentUser.buildHttpParams(req, CurrentUser.UPDATE_PARAMS);
				Connections.sendRequest(req, new ResponseListener() {
					public void onResponseReceived(HttpResponse response) {
						
						// Clear all map points
						mapOverlays.clear();
						itemizedoverlay.clear();
						
						// Pull response message
						String data = Connections.responseToString(response);
						
						// JSON IS FUN!
						try {
							JSONObject jObject, jSubObj;
							jObject = new JSONObject(data);
							
							// Process centering requests
							processCentering(jObject);
							
							// Process Players
							jSubObj = jObject.getJSONObject("players");
							processPlayers(jSubObj);
							
							// Process Game data
							processGame(jObject);
							
						    // Add map overlays
							mapOverlays.add(bounds);
						    mapOverlays.add(itemizedoverlay);
						    mapView.invalidate();
							
						} catch (JSONException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						
						Log.i(TAG, "Game Data: " + data);
						
					}
				});
	    	}
	    	
	    	gameHandler.postDelayed(this, GAME_UPDATE_DELAY);
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
							// nothing to center on
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
	    private void processPlayers(JSONObject jSubObj) {
			try {
	
				// Loop through all players
				JSONObject player;
				String playerKey;
				
				Iterator plrIterator = jSubObj.keys();
			    while(plrIterator.hasNext()) {
			    	
			    	playerKey = (String) plrIterator .next();
			    	player = jSubObj.getJSONObject(playerKey);
			    	
			    	// If a player isn't on a team, we don't care about it at all
			    	if(player.has("team")) {

				    	int lati = (int) (1E6 * Double.parseDouble(player.getString("latitude")));
				    	int loni = (int) (1E6 * Double.parseDouble(player.getString("longitude")));
	
						Log.i(TAG, "Adding player: " + player.getString("name") + " with  KEY=" + playerKey + " @ LAT " + player.getString("latitude") + ", LONG " + player.getString("longitude"));
						GeoPoint marker = new GeoPoint(lati, loni);
						OverlayItem overlayitem = new OverlayItem(marker, player.getString("name"), player.getString("name"));
						
						
						
						boolean hasFlag = player.getBoolean("has_flag");
						boolean isObserverMode = player.getBoolean("observer_mode");
						
						// may be used later
						
//						boolean redFlagCaptured = jSubObj.getBoolean("red_flag_captured");
//						boolean blueFlagCaptured = jSubObj.getBoolean("blue_flag_captured");
						
						
						String team = player.getString("team");
						if(playerKey.equals(CurrentUser.getUID())) {
							
							if(team.equals("red"))
							{
								overlayitem.setMarker(drawable_red_player);
								Log.i(TAG,"Current User is on Red team");
							}
							if(team.equals("blue"))
							{
								overlayitem.setMarker(drawable_blue_player);
								Log.i(TAG,"Current User is on Blue team");
							}
							
							// if Current User is on red team and has the blue flag, change their marker.
							if(team.equals("red") && hasFlag)
							{	
								overlayitem.setMarker(drawable_blue_flag);								
							}
							// if Current User is on blue team and has the red flag, change their marker.
							if(team.equals("blue") && hasFlag)
							{
								overlayitem.setMarker(drawable_red_flag);
							}
							if(isObserverMode)
							{
								Toast.makeText(getBaseContext(), "Observer Mode", 5).show();
								Log.i(TAG,"Current User is in observer mode");
							}
							if(!isObserverMode)
							{
								Toast.makeText(getBaseContext(), "Observer Mode = false", 5).show();

							}
							
						} else if(team.equals("red")) {
							overlayitem.setMarker(drawable_red_player);
						} else if(team.equals("blue")) {
							overlayitem.setMarker(drawable_blue_player);
						}
						
						
						
	
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
				
				// Adding red flag
				JSONObject red_flag = game.getJSONObject("red_flag");
				int lat = (int) (1E6 * Double.parseDouble(red_flag.getString("latitude")));
		    	int lon = (int) (1E6 * Double.parseDouble(red_flag.getString("longitude")));

				GeoPoint red_marker = new GeoPoint(lat, lon);
				OverlayItem red_overlayitem = new OverlayItem(red_marker, "red_flag", "red_flag");
				red_overlayitem.setMarker(drawable_red_flag);
				itemizedoverlay.addOverlay(red_overlayitem);
				
				Log.i(TAG, "Adding red_flag: " + red_flag.getString("latitude") + red_flag.getString("longitude"));
				
				// Adding blue flag
				JSONObject blue_flag = game.getJSONObject("blue_flag");
				lat = (int) (1E6 * Double.parseDouble(blue_flag.getString("latitude")));
		    	lon = (int) (1E6 * Double.parseDouble(blue_flag.getString("longitude")));

				GeoPoint blue_marker = new GeoPoint(lat, lon);
				OverlayItem blue_overlayitem = new OverlayItem(blue_marker, "blue_flag", "blue_flag");
				blue_overlayitem.setMarker(drawable_blue_flag);
				itemizedoverlay.addOverlay(blue_overlayitem);
				
				Log.i(TAG, "Adding blue_flag: " + red_flag.getString("latitude") + red_flag.getString("longitude"));
				
				
				// Adding boundaries
				
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

	};


	/**
	 * Returns false (required by MapActivity)
	 * @return false
	 */
	protected boolean isRouteDisplayed() {
		return false;
	}	
	
}
