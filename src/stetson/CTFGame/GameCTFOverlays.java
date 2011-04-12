package stetson.CTFGame;

import java.util.ArrayList;

import stetson.CTF.GameCTF;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.drawable.Drawable;
import android.view.Gravity;
import android.view.MotionEvent;
import android.widget.Toast;

import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.MapView;
import com.google.android.maps.OverlayItem;

public class GameCTFOverlays extends ItemizedOverlay<OverlayItem> {

	public final static long LONG_CLICK_TIME = 2000;
	private MapView mapView;
	private GameCTF gameCTF;
	private ArrayList<OverlayItem> mOverlays = new ArrayList<OverlayItem>();
	
	/**
	 * 
	 * @param defaultMarker
	 * @param context
	 */
	public GameCTFOverlays(Drawable defaultMarker, MapView mv, GameCTF game) {
		  super(defaultMarker);
		  mapView = mv;
		  gameCTF = game;
	}
	
	/**
	 * Adds an overlay item to the array of overlays.
	 * @param overlay
	 */
	public void addOverlay(OverlayItem overlay) {
		
		// Is there an empty overlay right now?
		for(int i = 0; i < mOverlays.size(); i++) {
			if(mOverlays.get(i).getMarker(OverlayItem.ITEM_STATE_FOCUSED_MASK) == null) {
				mOverlays.set(i, overlay);
				this.populate();
				return;
			}
		}
		
		// If not, add it to the end
	    mOverlays.add(overlay);
	    populate();
	}
		
	boolean isMovingMap = false;
	boolean isLongTap = false;
	@Override
	public boolean onTouchEvent(MotionEvent event, MapView view) {
		
		// We don't want to mess with multi-touch!
		if(event.getPointerCount() > 1) {
			return super.onTouchEvent(event, view);
		}
		
		// Determine if we had a long tap or a short tap
		switch(event.getAction()) {
		
			// If the map isn't moving, this is a tap!
			case MotionEvent.ACTION_UP:
					long clickTime = event.getEventTime() - event.getDownTime();
					
					// Handle long press
					if(clickTime >= LONG_CLICK_TIME) {
						isLongTap = true;
					// Handle short press
					} else {
						isLongTap = false;
					}

				break;
			   
			// We don't know if the map is moving yet, so lets assume its not
			case MotionEvent.ACTION_DOWN:
				isMovingMap = false;
				break;
			
			// The user has moved in the touch event, so they are moving the map most likely
			case MotionEvent.ACTION_MOVE:
				isMovingMap = true;
				break;
					   
		}
		
		return super.onTouchEvent(event, view);
	}
	
	/**
	 * Displays an alert dialog with the title and snippet of an overlay item.
	 * @param index of an overlay item
	 * @return true
	 */
	protected boolean onTap(int index) {
		
		OverlayItem item = mOverlays.get(index);
		
		// Long Tap (show context menu)
		if(isLongTap) {
			
			String id = item.getTitle();
			String text = item.getSnippet();
			
			
			gameCTF.getGameMenu().setMenu(GameMenu.MENU_FLAG, id, text, item.getPoint());
			
			/*
			final CharSequence[] items = {"Option 1", "Option 2", "Option 3"};
			AlertDialog.Builder builder = new AlertDialog.Builder(mapView.getContext());
			builder.setTitle("Hello There!");
			builder.setItems(items, new DialogInterface.OnClickListener() {
			    public void onClick(DialogInterface dialog, int item) {
			       
			    }
			});
			AlertDialog alert = builder.create();
			*/
			
		// Short Tap (show toast)
		} else {
			Toast toast = Toast.makeText(mapView.getContext(), item.getTitle(), Toast.LENGTH_SHORT);
			toast.setGravity(Gravity.TOP | Gravity.RIGHT, 0, 32);
			toast.show();

		}
		
		// Make sure long tap is reset
		isLongTap = false;
		
		return true;
	}
	
	/**
	 * 
	 * @param index of an overlay item
	 * @return the specified overlay item
	 */
	protected OverlayItem createItem(int i) {
		return mOverlays.get(i);
	}

	/**
	 * @return number of overlays in array
	 */
	public int size() {
		return mOverlays.size();
	}
	
	/**
	 * Clears the overlays array list.
	 */
	public void clear() {
		mOverlays.clear();
	}

}
