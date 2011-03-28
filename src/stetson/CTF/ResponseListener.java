/**
 * ResponseListener.java
 * An interface for response listeners - just to make sure programmers aren't dumb.
 * Origin: http://blog.androgames.net/12/retrieving-data-asynchronously/
 */
package stetson.CTF;

import org.apache.http.HttpResponse;

public interface ResponseListener {
 
	public void onResponseReceived(HttpResponse response);
 
}