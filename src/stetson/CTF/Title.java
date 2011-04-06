package stetson.CTF;

import android.app.Activity;
import android.content.Context;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.ImageView;

public class Title extends Activity {
	
    /** Called when the activity is first created. */
    public void onCreate(Bundle savedInstanceState) {
        
    	super.onCreate(savedInstanceState);
    	
    	setContentView(R.layout.main);
    	
    	CurrentUser.userLocation((LocationManager) getSystemService(Context.LOCATION_SERVICE), StetsonCTF.GPS_UPDATE_FREQUENCY_INTRO);
        
        ImageView image = (ImageView) findViewById(R.id.loading_image);
        image.setOnTouchListener(new OnTouchListener() {

    		public boolean onTouch(View arg0, MotionEvent arg1) {
    			finish();
    			return false;
    			
    		}
    	 
    	});
     
        new Thread() {
			public void run() {
				try {
					Thread.sleep(8000);
					finish();
				} catch (InterruptedException e) {
					Log.e("Title", "Error sleeping title thread.");
				}
			}
		}.start();
        
    }
    

}