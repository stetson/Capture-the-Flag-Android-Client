package stetson.CTFGame;

import com.google.android.maps.GeoPoint;

import stetson.CTF.GameCTF;
import stetson.CTF.R;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class GameMenu {
	
	// Constants: To be used across entire application
	public static final String TAG = "GameMenu";
	
	// Constants: Menu options
	public static final int MENU_DEFAULT = 0;
	public static final int MENU_FLAG = 1;
	public static final int MENU_PLAYER = 2;
	
	private String alternateInfo;
	private int menuType;
	private GameCTF myGame;
	
	public GameMenu(GameCTF game) {
		myGame = game;
		buildMenuListeners();
	}
	
	/**
	 * Sets which menu to be displayed. If no menu is visible, one will be come visible.
	 * To set default game menu, set 'info' and 'point' to null.
	 * @param type of menu to be displayed
	 * @param information used in alternate menus
	 * @param point to highlight on the map
	 */
	public void setMenu(int type, String info, GeoPoint point) {
		
		LinearLayout gameMenu = (LinearLayout) myGame.findViewById(R.id.gameMenu);
		LinearLayout altMenu = (LinearLayout) myGame.findViewById(R.id.altMenu);
		
		// Reset alternate menu
		alternateInfo = null;

		// Always clear the alternate menu
		altMenu.removeAllViews();
		
		// Let the client know that our menu type has changed
		menuType = type;
		
		// If we're going back to the default menu...
		if(type == MENU_DEFAULT) {
			altMenu.setVisibility(LinearLayout.GONE);
			gameMenu.setVisibility(LinearLayout.VISIBLE);
			return;
		}
				
		// If we're making a new alternate menu, make sure its visible
		gameMenu.setVisibility(LinearLayout.GONE);
		altMenu.setVisibility(LinearLayout.VISIBLE);
		
		// And make sure we get the info string
		alternateInfo = info;
		
		// Add elements to the new alternate menu based on the type
		if(type == MENU_FLAG){
			altMenu.addView(createMenuOption(R.string.menu_what, R.id.menu_option_what, R.drawable.center_self));
			altMenu.addView(createMenuOption(R.string.menu_move, R.id.menu_option_move, R.drawable.center_self));
		} else if (type == MENU_PLAYER) {
			altMenu.addView(createMenuOption(R.string.menu_who, R.id.menu_option_who, R.drawable.center_self));
			altMenu.addView(createMenuOption(R.string.menu_waypoints, R.id.menu_option_waypoints, R.drawable.center_self));
		}
		
		// Fill the blank spots
		altMenu.addView(createMenuOption(-1, -1, -1));
		altMenu.addView(createMenuOption(-1, -1, -1));
		
		// Back Button
		altMenu.addView(createMenuOption(R.string.menu_back, R.id.menu_option_back, R.drawable.exit));

	}
	
	/**
	 * Toggles the currently visible menu (game or alternate).
	 */
	public void toggleMenu() {
		
		LinearLayout gameMenu = (LinearLayout) myGame.findViewById(R.id.gameMenu);
		LinearLayout altMenu = (LinearLayout) myGame.findViewById(R.id.altMenu);
		
		if(menuType == MENU_DEFAULT) {
			altMenu.setVisibility(LinearLayout.GONE);
			if(gameMenu.getVisibility() == LinearLayout.VISIBLE) {
				gameMenu.setVisibility(LinearLayout.GONE);
			} else {
				gameMenu.setVisibility(LinearLayout.VISIBLE);
			}
		} else {
			gameMenu.setVisibility(LinearLayout.GONE);
			if(altMenu.getVisibility() == LinearLayout.VISIBLE) {
				altMenu.setVisibility(LinearLayout.GONE);
			} else {
				altMenu.setVisibility(LinearLayout.VISIBLE);
			}
		}
		
	}
	
	/**
	 * Creates a TextView button to be used within setMenu()
	 * @param text
	 * @return
	 */
	private TextView createMenuOption(int stringID, int componentID, int drawableID) {
		TextView tv = new TextView(myGame);
		tv.setTextColor(myGame.getResources().getColor(R.color.menu_text));
		tv.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT,(float) 0.20));
		tv.setGravity(Gravity.CENTER);
		
		if(stringID != -1) {
			tv.setText(myGame.getResources().getString(stringID));
		}
		
		if(componentID != -1) {
			tv.setId(componentID);
			tv.setClickable(true);
			tv.setOnClickListener(onMenuClick);
		}
		
		if(drawableID != -1) {
			tv.setCompoundDrawablesWithIntrinsicBounds(null, myGame.getResources().getDrawable(drawableID), null, null);
		}
		
		return tv;
	}
	
	/**
	 * Adds an onClick listener to each of the menu items.
	 */
	private void buildMenuListeners() {
		
		myGame.findViewById(R.id.menu_self).setOnClickListener(onMenuClick);
		myGame.findViewById(R.id.menu_red_flag).setOnClickListener(onMenuClick);
		myGame.findViewById(R.id.menu_blue_flag).setOnClickListener(onMenuClick);
		myGame.findViewById(R.id.menu_scores).setOnClickListener(onMenuClick);
		myGame.findViewById(R.id.menu_quit).setOnClickListener(onMenuClick);

	}
	
	/**
	 * Handles incoming menu clicks.
	 */
	public OnClickListener onMenuClick = new OnClickListener() {
		public void onClick(View v) {
			switch(v.getId()) {

				// Default menu options
				case R.id.menu_self:
					myGame.centerMapView(GameCTF.CENTER_SELF);
					break;
					
				case R.id.menu_red_flag:
					myGame.centerMapView(GameCTF.CENTER_RED);
					break;
					
				case R.id.menu_blue_flag:
					myGame.centerMapView(GameCTF.CENTER_BLUE);
					break;
					
				case R.id.menu_scores:
					myGame.buildScoreBoard();
					break;
					
				case R.id.menu_quit:
					myGame.stopGame();
					break;
				
				// Alternate menu options
				case R.id.menu_option_who:
					menuWho();
					break;
					
				case R.id.menu_option_what:
					menuWhat();
					break;
					
				case R.id.menu_option_move:
					menuMoveFlag();
					break;
					
				case R.id.menu_option_waypoints:
					menuWaypoints();
					break;
					
				case R.id.menu_option_back:
					setMenu(GameMenu.MENU_DEFAULT, null, null);
					break;
					
			}
			
		}
	};
	
	/**
	 * Requests WHO a player is.
	 */
	private void menuWho() {
		
		GameData data = myGame.getGameData();
		Player player;
		for(int p = 0; p < data.getPlayerCount(); p++) {			
			player = data.getPlayer(p);
			if(player.getUID().equals(alternateInfo)) {
				String status = "";
				if(player.hasObserverMode()) {
					status = " (Observer)";
				}			
				sendToast(player.getTeam() + " Player: " + player.getName() + status);
				break;
			}
		}

	}
	
	/**
	 * Requests WHAT the object is.
	 */
	private void menuWhat() {
		sendToast(alternateInfo);
	}
	
	/**
	 * Requests to move the selected flags position.
	 */
	private void menuMoveFlag() {
		sendToast(".. wants to move flag ..");
	}
	
	/**
	 * Requests to send a suggested way point to another player.
	 */
	private void menuWaypoints() {
		sendToast(".. wants to send waypoints ..");
	}
	
	/**
	 * Sends a toast containing the provided text to the upper-right hand corner of the game screen.
	 * @param text
	 */
	private void sendToast(String text) {
		Toast toast = Toast.makeText(myGame, text, Toast.LENGTH_SHORT);
		toast.setGravity(Gravity.TOP | Gravity.RIGHT, 0, 32);
		toast.show();
	}
	
}
