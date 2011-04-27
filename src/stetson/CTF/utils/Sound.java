package stetson.CTF.utils;

import stetson.CTF.R;
import android.content.Context;
import android.media.MediaPlayer;

public class Sound {
	//sounds
	private MediaPlayer mp;
	private Context myContext;
	private long observerModeLast;
	private static final int TIME_BETWEEN_SOUNDS = 5000;
	private boolean playedObserver = false;
	
	public Sound(Context context){
		observerModeLast = -1;
		myContext = context;	
	}
	
	public void playInObserver() {
		if(observerModeLast == -1 || (System.currentTimeMillis() - observerModeLast) > TIME_BETWEEN_SOUNDS && !playedObserver && CurrentUser.getIsObserver())
		{
			observerModeLast = System.currentTimeMillis();
			mp = MediaPlayer.create(myContext, R.raw.observermoderobo);
			mp.start();
			playedObserver = true;
		}
	}
	
	public void playOutObserver() {
		mp = MediaPlayer.create(myContext, R.raw.actionrobo);
		mp.start();
	}
	
	public void playTagged() {
		mp = MediaPlayer.create(myContext, R.raw.taggedrobo);
		mp.start();
	}
	
	public void playRedFlagCaptured() {
		mp = MediaPlayer.create(myContext, R.raw.redflagcapturedrobo);
		mp.start();
	}
	
	public void playBlueFlagCaptured() {
		mp = MediaPlayer.create(myContext, R.raw.blueflagcapturedrobo);
		mp.start();
	}
	
	public void playRedWin() {
		mp = MediaPlayer.create(myContext, R.raw.redteamwinsrobo);
		mp.start();
	}
	
	public void playBlueWin() {
		mp = MediaPlayer.create(myContext, R.raw.blueteamwinsrobo);
		mp.start();
	}
	
	public void playCenterFlag(){
		mp = MediaPlayer.create(myContext, R.raw.se1);
		mp.start();
	}
	
	public void stopSound(){
		mp.stop();
	}
	
	public void releaseSound(){
		mp.release();
	}
}
