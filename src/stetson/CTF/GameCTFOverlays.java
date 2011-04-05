package stetson.CTF;

import java.util.ArrayList;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.Gravity;
import android.widget.Toast;

import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.MapView;
import com.google.android.maps.OverlayItem;

public class GameCTFOverlays extends ItemizedOverlay<OverlayItem> {

	private MapView mapView;
	private ArrayList<OverlayItem> mOverlays = new ArrayList<OverlayItem>();
	
	/**
	 * 
	 * @param defaultMarker
	 * @param context
	 */
	public GameCTFOverlays(Drawable defaultMarker, MapView mv) {
		  super(defaultMarker);
		  mapView = mv;
	}
	
	/**
	 * Adds an overlay item to the array of overlays.
	 * @param overlay
	 */
	public void addOverlay(OverlayItem overlay) {
	    mOverlays.add(overlay);
	    populate();
	}
	
	/**
	 * Displays an alert dialog with the title and snippet of an overlay item.
	 * @param index of an overlay item
	 * @return true
	 */
	protected boolean onTap(int index) {
		
		OverlayItem item = mOverlays.get(index);
		Log.i("MAP", "Tapped " + item.getTitle());
		
		Toast toast = Toast.makeText(mapView.getContext(), item.getTitle(), Toast.LENGTH_SHORT);
		toast.setGravity(Gravity.TOP | Gravity.RIGHT, 0, 32);
		toast.show();

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
