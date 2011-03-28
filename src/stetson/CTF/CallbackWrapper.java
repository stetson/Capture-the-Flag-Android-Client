/**
 * CallbackWrapper.java
 * Creates a base for callback functions, to be used with the asynchronous http request sender.
 * Origin: http://blog.androgames.net/12/retrieving-data-asynchronously/
 */
package stetson.CTF;

import org.apache.http.HttpResponse;

public class CallbackWrapper implements Runnable {
 
	private ResponseListener callbackActivity;
	private HttpResponse response;
 
	public CallbackWrapper(ResponseListener callbackActivity) {
		this.callbackActivity = callbackActivity;
	}
 
	public void run() {
		callbackActivity.onResponseReceived(response);
	}
 
	public void setResponse(HttpResponse response) {
		this.response = response;
	}
 
}