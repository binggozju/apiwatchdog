package org.binggo.apiwatchdog;

import java.util.Map;

import com.google.common.collect.Maps;

/**
 * <p>WatchdogEvent is a message made up of the headers and the body.</p>
 * @author Administrator
 *
 */
public class WatchdogEvent {
	
	private Map<String, String> headers;
	
	private Object body;
	
	public WatchdogEvent(Map<String, String> headers, Object body) {
		this.headers = headers;
		this.body = body;
	}

	public static WatchdogEvent buildEvent(Object body) {
		return new WatchdogEvent(null, body);
	}
	
	public void addHeader(String key, String value) {
		if (headers == null) {
			headers = Maps.newHashMap();
		}
		headers.put(key, value);
	}
	
	public Map<String, String> getHeaders() {
		return headers;
	}

	public void setHeaders(Map<String, String> headers) {
		this.headers = headers;
	}

	public Object getBody() {
		return body;
	}

	public void setBody(Object body) {
		this.body = body;
	}
}
