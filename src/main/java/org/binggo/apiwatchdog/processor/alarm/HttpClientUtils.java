package org.binggo.apiwatchdog.processor.alarm;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.http.HttpHost;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.routing.HttpRoute;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import org.springframework.stereotype.Component;

import org.binggo.apiwatchdog.common.ReturnCode;
import org.binggo.apiwatchdog.common.WatchdogException;

@Component
public class HttpClientUtils {
	
	private static final Logger logger = LoggerFactory.getLogger(HttpClientUtils.class);
	
	private static final Integer MAX_CONN_TOTAL = 20;
	private static final Integer MAX_CONN_PER_ROUTE = 3;
	
	private CloseableHttpClient httpClient;
	
	public HttpClientUtils() {
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
	
	public String sendPostRequest(String url, Map<String, String> content) throws WatchdogException {
		HttpPost httpPost = new HttpPost(url);
		
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		for (Map.Entry<String, String> entry : content.entrySet()) {
			NameValuePair pair = new BasicNameValuePair(entry.getKey(), entry.getValue());
			params.add(pair);
		}
		
		try {
			httpPost.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));
		} catch (UnsupportedEncodingException ex) {
			//ex.printStackTrace();
			throw new WatchdogException(ReturnCode.POST_HTTP_REQUEST_FAIL, ex.getMessage());
		}
		
		CloseableHttpResponse httpResponse = null;
		try {
			httpResponse = httpClient.execute(httpPost);
			String respContent = EntityUtils.toString(httpResponse.getEntity());
			
			if (httpResponse != null) {
				try {
					httpResponse.close();
				} catch (IOException ex) {
					logger.error(ex.getMessage());
				} 
			}
			
			return respContent;
			
		} catch (IOException ex) {
			//ex.printStackTrace();
			if (httpResponse != null) {
				try {
					httpResponse.close();
				} catch (IOException e) {
					logger.error(e.getMessage());
				} 
			}
			
			throw new WatchdogException(ReturnCode.POST_HTTP_REQUEST_FAIL, ex.getMessage());
		}
	}
	
	public String sendPostRequest(String url, String jsonContent) throws WatchdogException {
		HttpPost httpPost = new HttpPost(url);
		
		try {
			httpPost.setEntity(new StringEntity(jsonContent));
		} catch (UnsupportedEncodingException ex) {
			//ex.printStackTrace();
			throw new WatchdogException(ReturnCode.POST_HTTP_REQUEST_FAIL, ex.getMessage());
		}

		CloseableHttpResponse httpResponse = null;
		try {
			httpResponse = httpClient.execute(httpPost);
			String respContent = EntityUtils.toString(httpResponse.getEntity());
			
			if (httpResponse != null) {
				try {
					httpResponse.close();
				} catch (IOException ex) {
					logger.error(ex.getMessage());
				} 
			}
			
			return respContent;
			
		} catch (IOException ex) {
			//ex.printStackTrace();
			if (httpResponse != null) {
				try {
					httpResponse.close();
				} catch (IOException e) {
					logger.error(e.getMessage());
				} 
			}
			
			throw new WatchdogException(ReturnCode.POST_HTTP_REQUEST_FAIL, ex.getMessage());
		}
		
	}
	
	public String sendGetRequest(String url) throws WatchdogException {
		HttpGet httpGet = new HttpGet(url);
		
		CloseableHttpResponse httpResponse = null;
		try {
			httpResponse = httpClient.execute(httpGet);
			String respContent = EntityUtils.toString(httpResponse.getEntity());
			
			if (httpResponse != null) {
				try {
					httpResponse.close();
				} catch (IOException ex) {
					logger.error(ex.getMessage());
				} 
			}
			
			return respContent;
		} catch (IOException ex) {
			//ex.printStackTrace();
			if (httpResponse != null) {
				try {
					httpResponse.close();
				} catch (IOException e) {
					logger.error(e.getMessage());
				} 
			}
			
			throw new WatchdogException(ReturnCode.GET_HTTP_REQUEST_FAIL, ex.getMessage());
		}
	}
	
	public void close() {
		try {
			httpClient.close();
		} catch (IOException ex) {
			//ex.printStackTrace();
			logger.error(ex.getMessage());
		}
	}
	
}
