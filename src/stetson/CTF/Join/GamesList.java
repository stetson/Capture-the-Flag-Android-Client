package stetson.CTF.Join;

import java.util.ArrayList;
import java.util.List;

import stetson.CTF.JoinCTF;
import stetson.CTF.R;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.util.Log;
public class GamesList {
	
	private JoinCTF myJoin;
	private ListView myListView;
	private List<GameItem> myGames;
	private LayoutInflater myInflater;
	
	/**
	 * Attaches JoinCTF and the list view to GamesList.
	 * Gets access to JoinCTF's layout inflater.
	 * @param join
	 */
	public GamesList(JoinCTF join) {
		myJoin = join;
		myListView = (ListView) myJoin.findViewById(R.id.join_games_list);
		myInflater = (LayoutInflater) myJoin.getLayoutInflater();
		myGames = new ArrayList<GameItem>();
		this.setListAdapter();
		this.setListListener();
	}
	
	/**
	 * Add a game to the games list.
	 * @param item
	 */
	public void addGame(GameItem item) {
		myGames.add(item);
		Log.i("ADD", "Adding game (GamesList1) -> " + item.getName());
	}
	
	/**
	 * Add a game to the games list.
	 * Hides any existing errors.
	 * @param name
	 * @param players
	 * @param distance
	 */
	public void addGame(String name, int players, double distance) {		
		myGames.add(new GameItem(name, players, distance));
		Log.i("ADD", "Adding game (GamesList2) -> " + name);
	}
	
	/**
	 * Removes all games from the games list.
	 */
	public void clearList() {
		myGames.clear();
	}
	
	/**
	 * If the text is not empty, an error message will be shown.
	 * @param text
	 */
	public void setErrorText(String text) {
		TextView view = (TextView) myJoin.findViewById(R.id.join_games_error);
		view.setVisibility(TextView.VISIBLE);
		if(!text.equals("")) {
			view.setText(text);
		} else {
			view.setText(R.string.join_message);
		}
	}

	/**
	 * Makes sure the list is up-to-date data wise.
	 */
	public void updateList() {
		((BaseAdapter) myListView.getAdapter()).notifyDataSetChanged();
	}
	
	/**
	 * If error is not blank, an error message will be shown.
	 * Otherwise, the list will be shown.
	 * @param visible
	 */
	public void setErrorMessage(String error) {
		
	}
	
	/**
	 * Attaches the myGames ArrayList of GameItems to the ListView.
	 */
	private void setListAdapter() {

		myListView.setAdapter(new ArrayAdapter<GameItem>(myJoin, R.layout.join_row, myGames){
			public View getView(int position, View convertView, ViewGroup parent) {

				// Creates a new list item for the given object
				GameItem game = myGames.get(position);
				View row;
				if (null == convertView) {
					row = myInflater.inflate(R.layout.join_row, null);
				} else {
					row = convertView;
				}
				
				// Sets the texts for the new list item
				TextView text;
				text = (TextView) row.findViewById(R.id.join_game_row_name);
				text.setText(game.getName());
				text = (TextView) row.findViewById(R.id.join_game_row_players);
				text.setText(myJoin.getString(R.string.join_list_players) + game.getPlayers());
				text = (TextView) row.findViewById(R.id.join_game_row_distance);
				text.setText(myJoin.getString(R.string.join_list_distance) 
						+ Math.round(game.getDistance())
						+ myJoin.getString(R.string.join_list_distance_units) 
				);

				return row;
			}
		});
		
	}
	
	/**
	 * Handles list clicks.
	 */
	private void setListListener() {
		myListView.setOnItemClickListener(new AdapterView.OnItemClickListener(){
			public void onItemClick(AdapterView<?> parent, View view, int position, long row) {
				GameItem game = myGames.get(position);
				myJoin.joinGame(game.getName());
			}
		});
	}
	
}
