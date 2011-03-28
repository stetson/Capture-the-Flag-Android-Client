/**
 * AsynchronusSender.java
 * Performs asynchronous http requests. Once executed, the response is sent to a callback function.
 * Origin: http://blog.androgames.net/12/retrieving-data-asynchronously/
 * Modifications by Jeremy Yanik to support more request types.
 */
package stetson.CTF;

import java.io.IOException;

import org.apache.http.HttpResponse;
import org.apache.http.HttpRequest;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.DefaultHttpClient;
 
import android.os.Handler;
 
public class AsynchronousSender extends Thread {
 
	private static final DefaultHttpClient httpClient = new DefaultHttpClient();
 
	private HttpRequest request;
	private Handler handler;
	private CallbackWrapper wrapper;
 
	protected AsynchronousSender(HttpRequest request,
			Handler handler, CallbackWrapper wrapper) {
		this.request = request;
		this.handler = handler;
		this.wrapper = wrapper;
	}
 
	public void run() {
		try {
			final HttpResponse response;
			synchronized (httpClient) {
				
				response = getClient().execute((HttpRequestBase) request);
			}
			wrapper.setResponse(response);
			handler.post(wrapper);
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
 
	private HttpClient getClient() {
		return httpClient;
	}
 
}