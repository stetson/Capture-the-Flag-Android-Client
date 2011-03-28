package stetson.CTF;

import java.util.ArrayList;
import android.app.AlertDialog;
import android.content.Context;
import android.graphics.drawable.Drawable;
import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.OverlayItem;

public class GameCTFOverlays extends ItemizedOverlay {

	
	private Context mContext;
	private ArrayList<OverlayItem> mOverlays = new ArrayList<OverlayItem>();
	
	/**
	 * 
	 * @param defaultMarker
	 */
	public GameCTFOverlays(Drawable defaultMarker) {
		  super(boundCenterBottom(defaultMarker));
	}
	
	/**
	 * 
	 * @param defaultMarker
	 * @param context
	 */
	public GameCTFOverlays(Drawable defaultMarker, Context context) {
		  super(defaultMarker);
		  mContext = context;
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
	  AlertDialog.Builder dialog = new AlertDialog.Builder(mContext);
	  dialog.setTitle(item.getTitle());
	  dialog.setMessage(item.getSnippet());
	  dialog.show();
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

}
