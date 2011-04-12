package stetson.CTFGame;

import android.widget.TextView;
import stetson.CTF.GameCTF;
import stetson.CTF.R;

public class GameInfoBar {
	
	// Constants: To be used across entire application
	public static final String TAG = "GameInfoBar";
	
	private GameCTF myGame;
	
	public GameInfoBar(GameCTF game) {
		myGame = game;
	}

	/**
	 * Sets the info bar to the loading state.
	 */
	public void setLoading() {
		setGameInfoBar(-1, -1, -1, true);
	}
	
	/**
	 * Updates the info bar to the given information.
	 * @param red score
	 * @param blue score
	 * @param gps accuracy
	 */
	public void update(int red, int blue, float accuracy) {
		setGameInfoBar(red, blue, accuracy, false);
	}
	
	/**
	 * Update the info bar to the given information (raw).
	 * If loading is true, all other params will be ignored.
	 * @param red score
	 * @param blue score
	 * @param gps accuracy
	 * @param is loading
	 */
	private void setGameInfoBar(int red, int blue, float accuracy, boolean loading) {
		
		TextView redText = (TextView) myGame.findViewById(R.id.gameInfo_red);
		TextView blueText = (TextView) myGame.findViewById(R.id.gameInfo_blue);
		TextView accuracyText = (TextView) myGame.findViewById(R.id.gameInfo_connection);
		
		if(loading) {
			redText.setText(myGame.getString(R.string.game_info_loading));
			blueText.setText("");
			accuracyText.setText("");
		} else {
			redText.setText(myGame.getString(R.string.game_info_red) + red);
			blueText.setText(myGame.getString(R.string.game_info_blue) + blue);
			accuracyText.setText(myGame.getString(R.string.game_info_accuracy) + accuracy);
		}

	}
}
