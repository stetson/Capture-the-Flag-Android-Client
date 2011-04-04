package stetson.CTF;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

public class StetsonCTF extends Activity {
	
	// All constants for Application
	public static final String TAG = "StetsonCTF";
	public static final String SERVER_URL = "http://ctf.no.de";
	public static final String CREATE_SUCCESS = "{\"response\":\"OK\"}";
	
	// 3 seconds, 10 seconds, 1 minute
	public static final int GPS_UPDATE_FREQUENCY_GAME = 3000;
	public static final int GPS_UPDATE_FREQUENCY_INTRO = 10000;
	public static final int GPS_UPDATE_FREQUENCY_BACKGROUND = 60000;
	
	// meters
	public static final int GPS_UPDATE_DISTANCE_GAME = 0;
	public static final int GPS_UPDATE_DISTANCE_INTRO = 1;
	public static final int GPS_UPDATE_DISTANCE_BACKGROUND = 10;
	
	public static final int LOADING_PAUSE = 1000;
	
	private Handler gamesHandler;
	private boolean isGameStarting = false;
	
	/**
	 * Called when the activity is first created.
	 * @param saved instance state
	 */
	public void onCreate(Bundle savedInstanceState) {
		
		Log.i(TAG, "Starting activity...");
		
		// Restore a saved instance of the application
		super.onCreate(savedInstanceState);
		
		// Move back to the game selection panel
		setContentView(R.layout.intro);
		
		// Connect components
		buildListeners();
		gamesHandler = new Handler();

		Log.i(TAG, "Activity ready!");
	}
	
	/**
	 * Rebuild games list when the application regains focus.
	 * (After leave a game, answering a call, etc...)
	 */
	public void onStart() {
		
		super.onStart();
		buildGamesList();
		isGameStarting = false;
		
		// Change GPS Freq
		CurrentUser.userLocation((LocationManager) this.getSystemService(Context.LOCATION_SERVICE), GPS_UPDATE_FREQUENCY_INTRO);
	}
	
	/**
	 * Slow down GPS updates a lot when the application is in the background.
	 */
	public void onStop() {

		super.onStop();
		
		// Change GPS Freq
		if(!isGameStarting) {
			CurrentUser.userLocation((LocationManager) this.getSystemService(Context.LOCATION_SERVICE), GPS_UPDATE_FREQUENCY_BACKGROUND);
		}
	}
	
	/**
	 * Connects the view components to listeners
	 */
	private void buildListeners() {
		
		Log.i(TAG, "Prepare listeners.");
		
		// Create a new game
		final Button newGameButton = (Button) findViewById(R.id.newgame_button);
		newGameButton.setOnClickListener(new OnClickListener() {
			public void onClick(View view) {
				
				// Do nothing if we have no loction (still loading)
				if(!CurrentUser.hasLocation()) {
					return;
				}
				
				// Get name
				EditText et = (EditText) findViewById(R.id.name_text);
				CurrentUser.setName(et.getText().toString());
				
				// Create the new game :)
				joinGame(CurrentUser.getName(), "");
				
			}
		});
		
		// Join a game
		final Button joinGameButton = (Button) findViewById(R.id.joingame_button);
		joinGameButton.setOnClickListener(new OnClickListener() {
			public void onClick(View view) {
				
				// Do nothing if we have no loction (still loading)
				if(!CurrentUser.hasLocation()) {
					return;
				}
				
				RadioGroup gamesGroup = (RadioGroup) findViewById(R.id.games_list_group);
				String game = "";

				int selected = gamesGroup.getCheckedRadioButtonId();
				
				// Join the specified game
				if(selected > -1) {
					RadioButton rb = (RadioButton) findViewById(selected);
					game = (String) rb.getText();
					CurrentUser.setGameId(game);
					
					// Get name
					EditText et = (EditText) findViewById(R.id.name_text);
					CurrentUser.setName(et.getText().toString());
					
					// Create the new game :)
					joinGame(CurrentUser.getName(), CurrentUser.getGameId());
					
				// No game selected, notify user
				} else {
					Toast.makeText(view.getContext(), R.string.no_game_selected, Toast.LENGTH_SHORT).show();
				}

			}
		});
		
	}
	
	/**
	 * Retrieves and displays a new games list
	 */
	private void buildGamesList() {
		
		Log.i(TAG, "Build games list. (Loading...)");
		
		// Start the handle (so we don't weigh down the thread)
		gamesHandler.post(new Runnable() {
			public void run() {
				
				// Clear all games and messages
				RadioGroup gamesGroup = (RadioGroup) findViewById(R.id.games_list_group);
				gamesGroup.removeAllViews();
				
				TextView loadText;
				
				// Acquiring location
				if(!CurrentUser.hasLocation()) {
					
					// Post a message
					loadText = new TextView(gamesGroup.getContext());
					loadText.setText(R.string.loading_location);
					gamesGroup.addView(loadText);
					
					// Check again in a little bit
					gamesHandler.postDelayed(this, LOADING_PAUSE);
					
				// Acquiring games
				} else {
					
					// Post a message
					loadText = new TextView(gamesGroup.getContext());
					loadText.setText(R.string.loading_games);
					gamesGroup.addView(loadText);
					
					// Build an send a request for game data
					HttpGet req = new HttpGet(SERVER_URL + "/game/?" + CurrentUser.buildQueryParams());
					Connections.sendRequest(req, new ResponseListener() {

						public void onResponseReceived(HttpResponse response) {
							
							RadioGroup gamesGroup = (RadioGroup) findViewById(R.id.games_list_group);
							gamesGroup.removeAllViews();
							
							// Pull response message
							String data = Connections.responseToString(response);
							Log.i(TAG, "Response: " + data);
							
							try {
								
								JSONObject games = new JSONObject(data);
								// Post a message
								TextView textResponse = new TextView(gamesGroup.getContext());

								// There was a server response with an error message
								if(games.has("error")) {
									
									textResponse.setText("Error: " + games.getString("error"));
									
								// A list of games was returned
								} else if (games.has("games")) {
									
									RadioButton rb;
									JSONArray list = games.getJSONArray("games");
									for(int n = 0; n < list.length(); n++) {
										Log.i(TAG, "Adding game to view (" + list.optString(n) + ")");
										rb = new RadioButton(gamesGroup.getContext());
										rb.setText(list.optString(n));
										gamesGroup.addView(rb);
									}
									
									if(list.length() == 0) {
										textResponse.setText(R.string.no_games);
									}
									
								
								// An unexpected response from the server (probably blank)
								} else {
									Log.e(TAG, "Unexpected server response: " + data);
								}
								
								// If we have a text response, diplay it 
								if(!textResponse.getText().equals("")) {
									gamesGroup.addView(textResponse);
								}

							} catch (JSONException e) {
								Log.e(TAG, "Error parsing JSON.", e);
							}
							
						}
						
					});

				}
				

			}
		});
				
	}

	/**
	 * Joins or creates a new game. If game is empty, then a new game will be created.
	 * @param name
	 * @param game
	 * @return did the user join the game successfully
	 */
    protected boolean joinGame(String name, String game) {
    	
    	Log.i(TAG, "joinGame(" + name + ", " + game + ")");
    	
    	// Update the user
    	updateUser(name);
    	
    	// We need to get current location data to make or join a game
		double longitude = CurrentUser.getLongitude();
		double latitude = CurrentUser.getLatitude();
		CurrentUser.setLocation(latitude, longitude);

		// Show the user a loading screen thingy
		ProgressDialog dialog = ProgressDialog.show(this, "", "Loading. Please wait...", true);
    	
		// If a game name wasn't provided, lets make a game! Build the request and stuff :)
		if(game.equals("")) {
			
			// Set the game name to the user's name
			CurrentUser.setGameId(CurrentUser.getName());
			
			// Make the request, NOT asynchronous, we need an answer now
			HttpPost hp = new HttpPost(SERVER_URL + "/game/");
			CurrentUser.buildHttpParams(hp, CurrentUser.CREATE_PARAMS);
			String data = Connections.sendFlatRequest(hp);
			if(!data.equals(CREATE_SUCCESS)) {
				dialog.hide();
				Toast.makeText(this, R.string.failed_to_create, Toast.LENGTH_SHORT).show();
				return false;
			}

		}
		
		// Join the game!
		String gameUrl = CurrentUser.getGameId().replaceAll(" ", "%20");
		HttpPost hp = new HttpPost(SERVER_URL + "/game/" + gameUrl);
		CurrentUser.buildHttpParams(hp, CurrentUser.JOIN_PARAMS);
		String data = Connections.sendFlatRequest(hp);
		
		try {
			JSONObject jsonGame = new JSONObject(data);
			if(jsonGame.has("error")) {
				dialog.hide();
				Toast.makeText(this, "Error: " + jsonGame.getString("error"), Toast.LENGTH_SHORT).show();
				return false;
			}
		} catch (JSONException e) {
			Log.e(TAG, "JSON Parsing Error.", e);
			return false;
		}

		// Nothing else to load, start the game :)
		isGameStarting = true;
		dialog.hide();
	    Intent i = new Intent(this, GameCTF.class);
	    startActivity(i);
	    return true;
    }
    
    /**
     * Sets the user's name and generates a new UID.
     * @param name
     */
    protected void updateUser(String name) {
    	
    	// New name
    	CurrentUser.setName(name);
    	
		// Generate a new uid
		String uid = "xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx";
		while(uid.contains("x")) 
		uid = uid.replaceFirst("x", Long.toHexString(Math.round(Math.random() * 16.0)));
		uid = uid.toUpperCase();
		CurrentUser.setUID(uid);
    }
    	
}
