package stetson.CTF.Game;

import android.app.Dialog;
import android.os.Handler;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;
import android.widget.TextView;
import stetson.CTF.GameCTF;

public class GameScores {
	
	// Constants: To be used across entire application
	public static final String TAG = "GameScores";
	
	private String[] scoresNames;
	private int[] scoresTags;
	private int[] scoresCaptures;
	
	private GameCTF myGame;
	
	public GameScores(GameCTF game) {
		myGame = game;
	}
	
	/**
	 * Display the score board.
	 */
	public void showScores() {
		
		// Base Dialog
		Dialog dialog = new Dialog(myGame);
		
		// Base layout
		LinearLayout board = new LinearLayout(myGame);
		board.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
		board.setOrientation(LinearLayout.VERTICAL);
		board.setPadding(10, 10, 10, 10);
		buildScoreBoard(board);
		
		// Add base layout to dialog and display
		dialog.setContentView(board);
		dialog.setTitle("Score Board");
		dialog.show();


	}
	
	private void buildScoreBoard(LinearLayout board) {
		
		GameData game = myGame.getGameData();
		Player player;
		for(int i = 0; i < game.getPlayerCount(); i++) {
			
			player = game.getPlayer(i);
			
			// The line for this player...
			TextView text;
			LinearLayout line = new LinearLayout(myGame);
			line.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
			
			text = new TextView(myGame);
			text.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
			text.setText("" + player.getName());
			line.addView(text);
			
			text = new TextView(myGame);
			text.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
			text.setText("" + player.getTags());
			line.addView(text);
			
			text = new TextView(myGame);
			text.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
			text.setText("" + player.getCaptures());
			line.addView(text);
			
			// Add line to board
			board.addView(line);
			
		}
		
	}
	
}
