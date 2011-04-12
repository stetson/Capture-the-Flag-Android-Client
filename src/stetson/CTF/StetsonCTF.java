package stetson.CTF;

import java.util.ArrayList;

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
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;
//import android.media.MediaPlayer;

public class StetsonCTF extends Activity {
	
	// Constants: To be used across entire application
	public static final String TAG = "StetsonCTF";
	public static final String SERVER_URL = "http://ctf.no.de";
	
	// Constants: GPS Update Frequency
	public static final int GPS_UPDATE_FREQUENCY_GAME = 3000;
	public static final int GPS_UPDATE_FREQUENCY_INTRO = 10000;
	public static final int GPS_UPDATE_FREQUENCY_BACKGROUND = 60000;
	
	// Constants: GPS Update Threshold
	public static final int GPS_UPDATE_DISTANCE_GAME = 0;
	public static final int GPS_UPDATE_DISTANCE_INTRO = 1;
	public static final int GPS_UPDATE_DISTANCE_BACKGROUND = 10;
	public static boolean firstStart = true;
	//Sound file
	//public MediaPlayer mp = MediaPlayer.create(getBaseContext(), R.raw.absorb1);
	
	// Data Members

	/**
	 * Called when the activity is first created.
	 * @param saved instance state
	 */
	public void onCreate(Bundle savedInstanceState) {
		
		Log.i(TAG, "Starting activity...");
		
		// Restore a saved instance of the application
		super.onCreate(savedInstanceState);
		new Thread(new Runnable() {
		    public void run() {
		    	if(StetsonCTF.firstStart)
				{
					Intent titleScreen = new Intent(getBaseContext(), Title.class);
					startActivity(titleScreen);
					StetsonCTF.firstStart = false;
				}
		    }
		  }).start();
		// Move back to the game selection panel
		setContentView(R.layout.intro);
		// Build listeners
		buildListeners();
		
		Log.i(TAG, "Activity ready!");
		
	}
	
	/**
	 * Start GPS and rebuild games list.
	 */
	public void onResume() {
		
		super.onResume();
		
		// Start GPS
		CurrentUser.userLocation((LocationManager) this.getSystemService(Context.LOCATION_SERVICE), GPS_UPDATE_FREQUENCY_INTRO);
		//mp.start();
		
		
		// Build a new games list
//		buildGamesList();
	
	}
	
	/**
	 * Slow down GPS updates a lot when the application is in the background.
	 */
	public void onPause() {

		super.onPause();
		
		// Stop GPS
		CurrentUser.stopLocation((LocationManager) this.getSystemService(Context.LOCATION_SERVICE));
		//mp.stop();
		
		
	}
	public void onDestroy() {

		super.onDestroy();
		
		// Stop GPS
		CurrentUser.stopLocation((LocationManager) this.getSystemService(Context.LOCATION_SERVICE));
		firstStart = true;
		CurrentUser.setName("");
		//mp.stop();
		//mp.release();
		
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
				
				// Create the new game :)
				joinGame(CurrentUser.getName(), "");
				
			}
		});

		
		// refresh list of games when user clicks
		final Button refreshButton = (Button) findViewById(R.id.refresh_button);
		refreshButton.setOnClickListener(new OnClickListener() {
			public void onClick(View view) {

				buildGamesList();
			}
		});

	}
	
	/**
	 * Retrieves and displays a new games list
	 */
	private void buildGamesList() {
		
		Log.i(TAG, "(UI) Building games list.");
		new TaskGenerateGamesList().execute();
		
	}

	/**
	 * Joins or creates a new game. If game is empty, then a new game will be created.
	 * @param name
	 * @param game
	 */
    protected void joinGame(String name, String game) {
    	Log.i(TAG, "(UI) joinGame(" + name + ", " + game + ")");
    	new TaskJoinGame().execute(name, game);
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
    
    
    /**
     * The AsyncTask used for generating a new list of games.
     * (Generics: Params, Progress, Result)
     */
    private class TaskGenerateGamesList extends AsyncTask<Void, String, ArrayList<String>> {
		
    	protected static final long GPS_CHECK_PAUSE = 500;
		private Context mContext = StetsonCTF.this;
		private int i;
		private TextView[] text;
		private Button[] button;
		private TableRow row;
		private TableLayout table;
		
		/**
		 * Run before execution on the UI thread.
		 * Remove all children from group view and post a loading message.
		 */
		protected void onPreExecute() {
			
			i = 0;
			table = (TableLayout) findViewById(R.id.tableLayout);
			row = new TableRow(mContext);
			row.removeAllViews();
			table.removeAllViews();
			
			
		}
		
		/**
		 * Runs every time publicProgress() is called.
		 * Clears the gamesGroup view and adds a message with the progress text.
		 */
	     protected void onProgressUpdate(String... progress) {
	    	 table.removeAllViews();
	    	 row.removeAllViews();
	    	 TextView text = new TextView(mContext);
	    	 text.setText(progress[0]);
	    	 row.addView(text);
	    	 table.removeAllViews();
	    	 row.removeAllViews();
	     }
	     
		/**
		 * Run after execution on the UI thread.
		 * Clears the gameGroup view and adds a list of games to it.
		 */
		protected void onPostExecute(final ArrayList<String> response) {
			
			row.removeAllViews();
			table.removeAllViews();
			// Something bad happened, unknown error :o
			if(response == null) {
		    	 TextView text = new TextView(mContext);
		    	 text.setText(R.string.no_games_error);
		    	 row.addView(text);
		    	 
		    // We have no games :(
			} else if(response.isEmpty()) {
		    	 TextView text = new TextView(mContext);
		    	 text.setText(R.string.no_games);
		    	 row.addView(text);
		    	 
		    // We have some games! Add them to the list :)
			} else {
				table.removeAllViews();
				text = new TextView[response.size()];
				button = new Button[response.size()];
				for(int i = 0; i < response.size(); i++) {
					Log.i(TAG, "Adding game to view (" + response.get(i) + ")");
					row = new TableRow(mContext);
					text[i] = new TextView(mContext);
					String gameName = response.get(i); 
					 text[i].setText(gameName);
					 
					button[i]=  new Button(mContext);

					button[i].setText("Join");
					button[i].setTag(gameName);
					button[i].setOnClickListener(new listener());
					row.addView(text[i]);
					row.addView(button[i]);
					table.addView(row,new TableLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
				}
			}
			
		}
		
		/**
		 * Run as the work on another thread.
		 */
		protected ArrayList<String> doInBackground(Void... params) {
			
			ArrayList<String> gamesList = new ArrayList<String>();
			
			publishProgress(mContext.getString(R.string.loading_location));
			
			// We might still be waiting for a location...
			while(!CurrentUser.hasLocation()) {
				try {
					Thread.sleep(GPS_CHECK_PAUSE);
				} catch (InterruptedException e) {
					Log.e(TAG, "Can't sleep :(", e);
				}
			}
			
			publishProgress(mContext.getString(R.string.loading_games));
			
			// Sweet, we have a location, lets grab a list of games
			HttpGet req = new HttpGet(SERVER_URL + "/game/?" + CurrentUser.buildQueryParams());
			String data = Connections.sendRequest(req);
			try {
				
				JSONObject games = new JSONObject(data);
				if (games.has("games")) {
					
					// Add all the games to a list
					JSONArray list = games.getJSONArray("games");
					for(int n = 0; n < list.length(); n++) {
						gamesList.add(list.getString(n));
					}
					
					// Ok, that's all, return the games list
					return gamesList;
					
				} else {
					Log.e(TAG, "Unexpected Server Response: " + data);
				}
			} catch (JSONException e) {
				Log.e(TAG, "Error parsing JSON.", e);
			}
			
			// Everything fell through, meaning bad stuff happened, check LogCat :(
			return null;
		}
    }
    
    /**
     * The AsyncTask used for creating and joining a game.
     * (Generics: Params, Progress, Result)
     */
	private class TaskJoinGame extends AsyncTask<String, Void, String> {
		
		private final static String GOOD_RESPONSE = "OK";
		private Context mContext = StetsonCTF.this;
		private ProgressDialog dialog;
		
		/**
		 * Run before execution on the UI thread.
		 */
		protected void onPreExecute() {
			dialog = ProgressDialog.show(mContext, "Joining Game", "Please wait...", true);
		}
		
		/**
		 * Run after execution on the UI thread.
		 * If the response string is equal to GOOD_RESPONSE then the game activity
		 * is started. If not, a toast showing the error message is sent.
		 */
		protected void onPostExecute(final String response) {
			
			dialog.hide();
			
			if(response.equals(GOOD_RESPONSE)) {
			    Intent i = new Intent(mContext, GameCTF.class);
			    startActivity(i);
			} else {
				Toast.makeText(mContext, response, Toast.LENGTH_LONG).show();
			}
			
		}
		
		/**
		 * Run as the work on another thread.
		 */
		protected String doInBackground(final String... params) {
			
			// More friendly parameters :)
			String name = params[0];
			String game = params[1];
			Log.i(TAG, "(WORKER) joinGame(" + name + ", " + game + ")");
			
			// Build a UID
			updateUser(name);
			
			// If a game name wasn't given, then we need to make a game.
			if(game.equals("")) {
				
				CurrentUser.setGameId(CurrentUser.getName());
				HttpPost hp = new HttpPost(SERVER_URL + "/game/");
				CurrentUser.buildHttpParams(hp, CurrentUser.CREATE_PARAMS);
				String data = Connections.sendRequest(hp);
				Log.i(TAG, "RESPONSE: " +data);
				
				try {
					JSONObject response = new JSONObject(data);
					Log.i(TAG, "(WORKER) create game response: " + data);
					if(response.has("response") && !response.getString("response").equals(GOOD_RESPONSE)) {
						return "Unexpected server response #1";
					} else if (response.has("error")) {
						return "Server Error: " + response.get("error");
					}
				} catch (JSONException e) {
					Log.e(TAG, "Error parsing JSON.", e);
					return "Unexpected server response #2";
				}
				
			}
			
			// If a game was created, then it was a success at this point! Now we must join the game.
			String gameUrl = CurrentUser.getGameId().replaceAll(" ", "%20");
			HttpPost hp = new HttpPost(SERVER_URL + "/game/" + gameUrl);
			CurrentUser.buildHttpParams(hp, CurrentUser.JOIN_PARAMS);
			String data = Connections.sendRequest(hp);
			
			try {
				JSONObject jsonGame = new JSONObject(data);
				if(jsonGame.has("error")) {
					return "Server Error: " + jsonGame.get("error");
				}
			} catch (JSONException e) {
				Log.e(TAG, "Error parsing JSON.", e);
				return "Unexpected server response #3";
			}

		    return GOOD_RESPONSE;
		}
	     
	}
	
	private class listener implements OnClickListener
	{

		public void onClick(View v) {
			// TODO Auto-generated method stub
			String myGameName = (String) v.getTag();
			CurrentUser.setGameId(myGameName);
			joinGame(CurrentUser.getName(), myGameName);
		}
		
	}
	
}



