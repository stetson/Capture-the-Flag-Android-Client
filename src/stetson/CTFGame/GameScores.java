package stetson.CTFGame;

import stetson.CTF.GameCTF;

public class GameScores {
	
	// Constants: To be used across entire application
	public static final String TAG = "GameScores";
	
	private GameCTF myGame;
	
	public GameScores(GameCTF game) {
		myGame = game;
	}
	
	/**
	 * Display the score board.
	 */
	public void showScores() {
		myGame.getTitle();
	}
	
}
