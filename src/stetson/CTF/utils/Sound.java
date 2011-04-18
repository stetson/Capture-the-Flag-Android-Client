package stetson.CTF.utils;

import stetson.CTF.R;
import android.content.Context;
import android.media.MediaPlayer;

public class Sound {
	//sounds
	private MediaPlayer mp;
	private Context myContext;
	
	public Sound(Context context){
		myContext = context;	
	}
	
	public void playInObserver() {
		mp = MediaPlayer.create(myContext, R.raw.se9);
		mp.start();
	}
	
	public void playOutObserver() {
		mp = MediaPlayer.create(myContext, R.raw.se8);
		mp.start();
	}
	
	public void playTagged() {
		mp = MediaPlayer.create(myContext, R.raw.taggedrobo);
		mp.start();
	}
	
	public void playFlagCaptured() {
		mp = MediaPlayer.create(myContext, R.raw.flagcapturedrobo);
		mp.start();
	}
	
	public void playWin() {
		mp = MediaPlayer.create(myContext, R.raw.winnerrobo);
		mp.start();
	}
	
}
