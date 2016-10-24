package org.binggo.apiwatchdog.processor.alarm;

import org.apache.http.HttpHost;
import org.apache.http.conn.routing.HttpRoute;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;

public class HttpClientUtils {
	
	private static final Integer MAX_CONN_TOTAL = 20;
	private static final Integer MAX_CONN_PER_ROUTE = 3;
	
	private static CloseableHttpClient httpClient;
	
	public static CloseableHttpClient getHttpClient() {
		if (httpClient == null) {
			synchronized (HttpClientUtils.class) {
				if (httpClient == null) {
					// configure the connection manager
					PoolingHttpClientConnectionManager connManager = new PoolingHttpClientConnectionManager();
					connManager.setMaxTotal( MAX_CONN_TOTAL);
					connManager.setDefaultMaxPerRoute(MAX_CONN_PER_ROUTE);
					
					HttpHost localhost = new HttpHost("localhost", 8090);
					connManager.setMaxPerRoute(new HttpRoute(localhost), 8);
					
					// create the HttpClient
					httpClient = HttpClients.custom()
							.setConnectionManager(connManager)
							.build();
				}
			}
		}
		
		return httpClient;
	}
	
}
