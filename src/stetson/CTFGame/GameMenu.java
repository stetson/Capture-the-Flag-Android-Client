package stetson.CTFGame;

import com.google.android.maps.GeoPoint;

import stetson.CTF.GameCTF;
import stetson.CTF.R;
import android.graphics.drawable.Drawable;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.TextView;

public class GameMenu {
	
	// Constants: Menu options
	public static final int MENU_DEFAULT = 0;
	public static final int MENU_FLAG = 1;
	public static final int MENU_PLAYER = 2;
	
	private int menuType;
	private GameCTF myGame;
	
	public GameMenu(GameCTF game) {
		myGame = game;
		buildMenuListeners();
	}
	
	/**
	 * 
	 * @param type
	 * @param id
	 * @param text
	 * @param point
	 */
	public void setMenu(int type, String id, String text, GeoPoint point) {
		
		LinearLayout gameMenu = (LinearLayout) myGame.findViewById(R.id.gameMenu);
		LinearLayout altMenu = (LinearLayout) myGame.findViewById(R.id.altMenu);
		
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
		
		// Add elements to the new alternate menu based on the type
		if(type == MENU_FLAG){
			altMenu.addView(createMenuOption("What?", myGame.getResources().getDrawable(R.drawable.center_self)));
			altMenu.addView(createMenuOption("Move", myGame.getResources().getDrawable(R.drawable.center_self)));
		} else if (type == MENU_PLAYER) {
			altMenu.addView(createMenuOption("Who?", myGame.getResources().getDrawable(R.drawable.center_self)));
			altMenu.addView(createMenuOption("Waypoints", myGame.getResources().getDrawable(R.drawable.center_self)));
		}
		
		// Fill the blank spots
		altMenu.addView(createMenuOption("", null));
		altMenu.addView(createMenuOption("", null));
		
		// Back Button
		TextView backButton = createMenuOption("Back", myGame.getResources().getDrawable(R.drawable.exit));
		backButton.setOnClickListener(onMenuClick);

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
	private TextView createMenuOption(String text, Drawable icon) {
		TextView tv = new TextView(myGame);
		tv.setTextColor(myGame.getResources().getColor(R.color.menu_text));
		tv.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT,(float) 0.20));
		tv.setClickable(true);
		tv.setGravity(Gravity.CENTER);
		tv.setText(text);
		tv.setCompoundDrawablesWithIntrinsicBounds(null,icon, null, null);
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
					
			}
			
		}
	};
	
}
