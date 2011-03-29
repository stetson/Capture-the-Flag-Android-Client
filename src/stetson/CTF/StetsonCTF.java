package stetson.CTF;

import java.io.IOException;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

public class StetsonCTF extends Activity {
	
	private static final String TAG = "StetsonCTF";
	private static final String NO_GAMES_RESPONSE = "[]";
	
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
		buildGamesList();
		
		Log.i(TAG, "Activity ready!");
	}
	
	/**
	 * Rebuild games list when the application regains focus.
	 * (After leave a game, answering a call, etc...)
	 */
	public void onStart() {
		super.onStart();
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
				Toast.makeText(view.getContext(), "Making a new game.", Toast.LENGTH_LONG).show();
			}
		});
		
		// Join a game
		final Button joinGameButton = (Button) findViewById(R.id.joingame_button);
		joinGameButton.setOnClickListener(new OnClickListener() {
			public void onClick(View view) {
				RadioGroup gamesGroup = (RadioGroup) findViewById(R.id.games_list_group);
				String game = "{NO_GAME}";

				int selected = gamesGroup.getCheckedRadioButtonId();
				
				// Join the specified game
				if(selected > -1) {
					RadioButton rb = (RadioButton) findViewById(selected);
					game = (String) rb.getText();
					//joinGame(game);
					
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
	private void buildGamesList()
	{
		
		Log.i(TAG, "Build games list. (Loading...)");
		
		// let the user know we aren't being lazy
		RadioGroup gamesGroup = (RadioGroup) findViewById(R.id.games_list_group);
		
		TextView loadText = new TextView(gamesGroup.getContext());
		loadText.setText(R.string.loading_games);
		gamesGroup.addView(loadText);

		
		// Build an send a request for game data
		HttpGet req = new HttpGet("http://ctf.no.de/game/");
		sendRequest(req, new ResponseListener() {

			public void onResponseReceived(HttpResponse response) {
				
				// Pull response message
				String data = responseToString(response);
				Log.i(TAG, "Response: " + data);

				// Remove all of the current games on the list
				RadioGroup gamesGroup = (RadioGroup) findViewById(R.id.games_list_group);
				gamesGroup.removeAllViews();
				
				// Oh no, there are no games!
				if (data.equals("") || data.equals(NO_GAMES_RESPONSE)) {
					TextView loadText = new TextView(gamesGroup.getContext());
					loadText.setText(R.string.no_games);
					gamesGroup.addView(loadText);

				// Parse the new data and add games to the list =D
				} else {
					JSONArray jObject;
					try {
						jObject = new JSONArray(data);
						RadioButton rb;
						int index = 0;
						while(!jObject.optString(index).equals("")) {
							Log.i(TAG, "Adding game to view (" + jObject.optString(index) + ")");
							rb = new RadioButton(gamesGroup.getContext());
							rb.setText(jObject.optString(index));
							gamesGroup.addView(rb);
							index ++;
	
						}
					} catch (JSONException e) {
						Log.i(TAG, "There was an error parsing game data!", e);
					}
				}
				
				Log.i(TAG, "Build games list. (Done!)");
			}
			
		});

		
	}
	
	/**
	 * Makes an HTTP request and sends it to a response listener once completed.
	 * @param request
	 * @param responseListener
	 */
	public static void sendRequest(final HttpRequestBase request, ResponseListener responseListener) {
		(new AsynchronousSender(request, new Handler(), new CallbackWrapper(responseListener))).start();
	}
	
	/**
	 * Draws a string from an HttpResponse object.
	 * @param rp
	 * @return
	 */
	public static String responseToString(HttpResponse rp) {
    	String str = "";
    	try {
    		str = EntityUtils.toString(rp.getEntity());
    	} catch(IOException e) {
    		Log.e(TAG, "HttpRequest Error!", e);
    	}  
    	return str;
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
		LocationManager lm = (LocationManager)getSystemService(Context.LOCATION_SERVICE); 
		Location location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
		double longitude = location.getLongitude();
		double latitude = location.getLatitude();
		CurrentUser.setLocation(latitude, longitude);
    	
		// If a game name wasn't provided, lets make a game!
		if(game.equals("")) {
			
		}
		
        Intent i = new Intent(this, GameCTF.class);
        startActivity(i);
        
        return false;
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