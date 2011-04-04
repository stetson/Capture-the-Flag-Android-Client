package stetson.CTF;

import java.io.IOException;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import android.os.Handler;
import android.util.Log;

public class Connections {
	
	public final static String TAG = "Connections";
	
	/**
	 * Makes an HTTP request and sends it to a response listener once completed.
	 * @param request
	 * @param responseListener
	 */
	public static void sendRequest(final HttpRequestBase request, ResponseListener responseListener) {
		Log.i(TAG, "ConnectionR: " + request.getURI());
		(new AsynchronousSender(request, new Handler(), new CallbackWrapper(responseListener))).start();
	}
	
	/**
	 * Makes an HTTP request and returns a response as a string.
	 * @param request
	 * @return response
	 */
	public static String sendFlatRequest(HttpRequestBase request) {
		
		Log.i(TAG, "ConnectionF: " + request.getURI());
		
		try {
			HttpClient client = new DefaultHttpClient();
			HttpResponse resp;
			resp = client.execute(request);
			return responseToString(resp);
			 
		} catch (ClientProtocolException e) {
			Log.e(TAG, "Web Request Failed", e);
		} catch (IOException e) {
			Log.e(TAG, "Web Request Failed", e);
		}
		return "";
	}
	
	/**
	 * Draws a string from an HttpResponse object.
	 * @param rp
	 * @return
	 */
	public static String responseToString(HttpResponse rp) {
    	String str = "";
    	try {
    		str = EntityUtils.toString(rp.getEntity());
    	} catch(IOException e) {
    		Log.e(TAG, "HttpRequest Error!", e);
    	}  
    	return str;
	}
}
