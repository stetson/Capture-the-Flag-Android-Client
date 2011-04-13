package stetson.CTF.Game;

import java.util.ArrayList;

import stetson.CTF.CurrentUser;
import stetson.CTF.R;
import stetson.CTF.Title;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.util.Log;
import android.view.Gravity;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;
import android.widget.TextView;
import stetson.CTF.GameCTF;

public class GameScores {
	
	// Constants: To be used across entire application
	public static final String TAG = "GameScores";
	private String shareMessage = "StetsonCTF!!\n" + CurrentUser.getName() + " has posted the Scores of the game.\nName:\t" +
	"Tags:\tCaptures:\tTeam:\n";
	private static final int LIST_COUNT = 10;
	
	
	private GameCTF myGame;
	
	public GameScores(GameCTF game) {
		myGame = game;
	}
	
	/**
	 * Display the score board.
	 */
	public void showScores() {

		// Base Dialog
		AlertDialog.Builder builder;
		AlertDialog alertDialog;
		builder = new AlertDialog.Builder(myGame);
		
		// Base layout
		LinearLayout board = new LinearLayout(myGame);
		board.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
		board.setOrientation(LinearLayout.VERTICAL);
		board.setPadding(5, 0, 5, 0);
		board.addView(createHeading());
		buildScoreBoard(board);
		
		// Add base layout to dialog and display
		builder.setView(board);
		alertDialog = builder.create();
		alertDialog.setTitle("Score Board");
		alertDialog.setCancelable(true);
		alertDialog.setButton("Close", onButtonClick);
		alertDialog.setButton2("Share", onButtonClick);
		alertDialog.show();

	}
	
	/**
	 * Handles button clicks on the score board dialog.
	 */
	private DialogInterface.OnClickListener onButtonClick = new DialogInterface.OnClickListener() {
		public void onClick(DialogInterface dialog, int which) {
			switch(which) {
				case DialogInterface.BUTTON1:
					dialog.dismiss();
					break;
				case DialogInterface.BUTTON2:
					// do facebook stuff
					new Thread(new Runnable() {
					    public void run() {
					    	Title.postToWall(shareMessage);
					        }
					  }).start();
					break;
			}
		}
	};
	
	/**
	 * Builds the score board lines.
	 * @param board
	 */
	private void buildScoreBoard(LinearLayout board) {
		
		Player player;
		GameData game = myGame.getGameData();
		ArrayList<ScoreTable> scoreTable = new ArrayList<ScoreTable>();
		
		// Sort the ScoreTables, get the top LIST_COUNT players
		for(int i = 0; i < game.getPlayerCount(); i++) {
			player = game.getPlayer(i);
			ScoreTable score = new ScoreTable(player.getName(), player.getTags(), player.getCaptures(), player.getTeam());
			if(scoreTable.size() == 0) {
				Log.i(TAG, "no other scores... adding first");
				scoreTable.add(score);
			} else {
				for(int r = 0; r < scoreTable.size(); r++) {
					ScoreTable current = scoreTable.get(r);
					if(score.compare(current) >= 0) {
						Log.i(TAG, "replacing #" +r + " score!");
						scoreTable.add(r, score);
						break;
					}
				}
			}
		}
		
		// Add lines
		int actualCount = scoreTable.size() >= LIST_COUNT ? LIST_COUNT : scoreTable.size();
		for(int i =0; i < actualCount; i++) {
			ScoreTable current = scoreTable.get(i);
			shareMessage = shareMessage + current.name + "\t\t"+ current.tags + "\t\t\t"+  current.caps+ "\t\t"+ current.team+ "\n";
			board.addView(createLine(i + 1, current.name, current.tags, current.caps, current.team));
		}
		
		
		
	}
	
	/**
	 * Builds the score board heading line.
	 * @return
	 */
	private LinearLayout createHeading() {
		
		// The line for this player...
		TextView text;
		LinearLayout line = new LinearLayout(myGame);
		line.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
		line.setPadding(5, 2, 5, 2);
		
		text = new TextView(myGame);
		text.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
		text.setText("#");
		text.setWidth(30);
		line.addView(text);
		
		text = new TextView(myGame);
		text.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
		text.setText("Name");
		text.setWidth(100);
		line.addView(text);
		
		text = new TextView(myGame);
		text.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
		text.setText("Tags");
		text.setWidth(50);
		text.setGravity(Gravity.RIGHT);
		line.addView(text);
		
		text = new TextView(myGame);
		text.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
		text.setText("Caps");
		text.setWidth(50);
		text.setGravity(Gravity.RIGHT);
		line.addView(text);
		
		return line;
	}
	
	/**
	 * Builds one score board line.
	 * @param rank
	 * @param name
	 * @param tags
	 * @param caps
	 * @param team
	 * @return
	 */
	private LinearLayout createLine(int rank, String name, int tags, int caps, String team) {
		
		// The line for this player...
		TextView text;
		LinearLayout line = new LinearLayout(myGame);
		line.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
		line.setPadding(5, 2, 5, 2);
		
		// Set a nice background color
		if(team.equals("red")) {
			line.setBackgroundColor(myGame.getResources().getColor(R.color.red_background));
		} else if(team.equals("blue")) {
			line.setBackgroundColor(myGame.getResources().getColor(R.color.blue_background));
		}
		
		text = new TextView(myGame);
		text.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
		text.setText("" + rank + ".");
		text.setWidth(30);
		line.addView(text);
		
		text = new TextView(myGame);
		text.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
		text.setText("" + name);
		text.setWidth(100);
		line.addView(text);
		
		text = new TextView(myGame);
		text.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
		text.setText("" + tags);
		text.setWidth(50);
		text.setGravity(Gravity.RIGHT);
		line.addView(text);
		
		text = new TextView(myGame);
		text.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
		text.setText("" + caps);
		text.setWidth(50);
		text.setGravity(Gravity.RIGHT);
		line.addView(text);
		
		return line;
	}
	
	/**
	 * Private class to be used for sorting score information.
	 */
	private class ScoreTable {
		
		public String name;
		public String team;
		public int tags;
		public int caps;
		
		public ScoreTable(String n, int t, int c, String tm) {
			name = n;
			tags = t;
			caps = c;
			team = tm;
		}

		/**
		 * Compares SELF to OTHER
		 * if SELF > -> 1
		 * if SELF < -> -1
		 * if SELF = -> 0
		 * @param other
		 * @return
		 */
		public int compare(ScoreTable other) {
			if(other.caps < this.caps) {
				return 1;
			}
			if(other.caps > this.caps) {
				return -1;
			}
			if(other.tags < this.tags) {
				return 1;
			}
			if(other.tags > this.tags) {
				return -1;
			}
			return 0;
		}
		
	}
	
}
