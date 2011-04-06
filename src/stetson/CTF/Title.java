package stetson.CTF;

import android.app.Activity;
import android.content.Context;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.widget.ImageView;

public class Title extends Activity {
	
	private static int displayWidth = 0;
    private static int displayHeight = 0;
    ImageView image;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        CurrentUser.userLocation((LocationManager) getSystemService(Context.LOCATION_SERVICE), StetsonCTF.GPS_UPDATE_FREQUENCY_INTRO);
        setContentView(R.layout.main);
        Display display = ((WindowManager)
                getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        displayWidth = display.getWidth();             
        displayHeight = display.getHeight();
        image = (ImageView) findViewById(R.id.loading_image);
        image.setMaxWidth(displayWidth);
        image.setMaxHeight(displayHeight);
        image.setOnTouchListener(new OnTouchListener() {

    		public boolean onTouch(View arg0, MotionEvent arg1) {
    			finish();
    			return false;
    			
    		}
    	 
    	});
     
        new Thread()
		{
			public void run()
			{
				try
				{

					Thread.sleep(8000);


					finish();

				}
				catch (InterruptedException e)
				{

				}
			}
		}.start();
//	Useful image scaling if needed
//        Bitmap bMap = BitmapFactory.decodeResource(getResources(), R.drawable.stetson_ctf);
//        Bitmap bMapScaled = Bitmap.createScaledBitmap(bMap, displayWidth, displayHeight, false);
//        image.setImageBitmap(bMapScaled);
        
        
    }
    

    @Override
    protected void onStart() {
        super.onStart();
        // The activity is about to become visible.
    }
    @Override
    protected void onResume() {
        super.onResume();
        // The activity has become visible (it is now "resumed").
    }
    @Override
    protected void onPause() {
        super.onPause();
        // Another activity is taking focus (this activity is about to be "paused").
    }
    @Override
    protected void onStop() {
        super.onStop();
        // The activity is no longer visible (it is now "stopped")
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        // The activity is about to be destroyed.
    }
}