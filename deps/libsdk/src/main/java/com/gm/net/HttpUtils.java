package com.gm.net;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLHandshakeException;
import javax.net.ssl.TrustManager;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.X509TrustManager;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.params.HttpClientParams;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.params.ConnRoutePNames;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import com.gm.utils.Logger;

//import org.apache.http.conn.ssl.SSLSocketFactory;


import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;



public class HttpUtils {
	private static final int DEFAULT_CONNECT_TIMEOUT = 30000;
	private static final int DEFAULT_SO_TIMEOUT = 30000;
	
    private static final String TAG = "HttpUtils";

    private static String HTTP_CONTENT_USER_AGENT = "ua";
    private static TrustManager[] trustAllCerts = new TrustManager[] {
        new X509TrustManager() {
            @Override
            public void checkClientTrusted(final X509Certificate[] chain, final String authType) {
            }

            @Override
            public void checkServerTrusted(final X509Certificate[] chain, final String authType) {
            }

            @Override
            public X509Certificate[] getAcceptedIssuers() {
                return null;
            }
        }
    };
    
    public static HttpResponse postWithOutTime(Context context, String url, ArrayList<NameValuePair> params,int conOutTime,int soOutTime) throws SSLHandshakeException, ClientProtocolException, IOException {
    	HttpResponse rsp=null;
    	try{
    		boolean isParam = params != null;
    		url = url.replaceAll(" ", "%20");
    		HttpClient httpClient = initHttpClient(url);
    		//setProxyIfNecessary(context, httpClient);
    		setProxyIfWap(context, httpClient);
    		HttpParams httpParams = httpClient.getParams();
    		HttpConnectionParams.setConnectionTimeout(httpParams, conOutTime);
    		HttpConnectionParams.setSoTimeout(httpParams, soOutTime);
    		HttpClientParams.setRedirecting(httpParams, true);

    		HttpPost post = new HttpPost(url);
    		post.setHeader("Content-Type", "application/x-www-form-urlencoded");
    		post.setHeader("Charset", "UTF-8");
    		post.setHeader("User-Agent", HTTP_CONTENT_USER_AGENT);
    		if (isParam) {
    			UrlEncodedFormEntity entity = new UrlEncodedFormEntity(params, "UTF-8");
    			post.setEntity(entity);
    		}
    		rsp = httpClient.execute(post);    		
    	}catch(Exception e){
        	e.printStackTrace();
        }
    	return rsp;
    }
    
    public static HttpResponse postWithOutTime(Context context, String url, byte[] params,int conOutTime,int soOutTime) throws SSLHandshakeException, ClientProtocolException, IOException {
    	HttpResponse rsp=null;
    	try{
    		boolean isParam = params != null;
    		url = url.replaceAll(" ", "%20");
    		HttpClient httpClient = initHttpClient(url);
    		//setProxyIfNecessary(context, httpClient);
    		setProxyIfWap(context, httpClient);
    		HttpParams httpParams = httpClient.getParams();
    		HttpConnectionParams.setConnectionTimeout(httpParams, conOutTime);
    		HttpConnectionParams.setSoTimeout(httpParams, soOutTime);
    		HttpClientParams.setRedirecting(httpParams, true);

    		HttpPost post = new HttpPost(url);
    		//post.setHeader("Content-Type", "application/x-www-form-urlencoded");
    		//must
    		post.setHeader("Content-Type", "application/json");
    		post.setHeader("Charset", "UTF-8");
    		post.setHeader("User-Agent", HTTP_CONTENT_USER_AGENT);
    		if (isParam) {
    			HttpEntity entity = new ByteArrayEntity(params);    			
    			post.setEntity(entity);
    		}
    		rsp = httpClient.execute(post);
    	}catch(Exception e){
        	e.printStackTrace();
        }
    	return rsp;
    }

    public static HttpResponse getWithOutTime(Context context, String url,int conOutTime,int soOutTime) throws SSLHandshakeException, ClientProtocolException, IOException {
    	HttpResponse rsp=null;
    	try{
        	url = url.replaceAll(" ", "%20");
        	HttpClient httpClient = initHttpClient(url);
        	//setProxyIfNecessary(context, httpClient);
    		setProxyIfWap(context, httpClient);
        	HttpParams httpParams = httpClient.getParams();
        	HttpConnectionParams.setConnectionTimeout(httpParams, conOutTime);
        	HttpConnectionParams.setSoTimeout(httpParams, soOutTime);
        	HttpClientParams.setRedirecting(httpParams, true);

        	HttpGet get = new HttpGet(url);
        	get.setHeader("Content-Type", "application/x-www-form-urlencoded");
        	get.setHeader("Charset", "UTF-8");
        	get.setHeader("User-Agent", HTTP_CONTENT_USER_AGENT);
        	rsp = httpClient.execute(get);
        }catch(Exception e){
        	e.printStackTrace();
        }
    	return rsp;
    }

    public static HttpResponse post(Context context, String url, ArrayList<NameValuePair> params) throws SSLHandshakeException, ClientProtocolException, IOException {
       return postWithOutTime(context,url,params,DEFAULT_CONNECT_TIMEOUT,DEFAULT_SO_TIMEOUT);
    }

    public static HttpResponse get(Context context, String url) throws SSLHandshakeException, ClientProtocolException, IOException {
      return getWithOutTime(context,url,DEFAULT_CONNECT_TIMEOUT,DEFAULT_SO_TIMEOUT);
    }

    private static HttpClient initHttpClient(String url) {
        HttpClient httpclient = null;
        try {
            if (url.startsWith("https://")) {
                //SSLSocketFactory ssf = sslSocketFactory();
                //org.apache.http.conn.ssl.SSLSocketFactory ssf2 = new org.apache.http.conn.ssl.SSLSocketFactory(ssf);
            	//������������������������������
            	org.apache.http.conn.ssl.SSLSocketFactory ssf2 = org.apache.http.conn.ssl.SSLSocketFactory.getSocketFactory();
                ssf2.setHostnameVerifier(org.apache.http.conn.ssl.SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);             
            	ClientConnectionManager ccm = new DefaultHttpClient().getConnectionManager();
                SchemeRegistry sr = ccm.getSchemeRegistry();
                sr.register(new Scheme("https", ssf2, 443));
                HttpParams params = new BasicHttpParams();
                params.setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, 1000);
                params.setParameter(CoreConnectionPNames.SO_TIMEOUT, 5000);
                httpclient = new DefaultHttpClient(ccm, params);
            } else {
                httpclient = new DefaultHttpClient();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return httpclient;
    }

    private static SSLSocketFactory sslSocketFactory() {
        SSLSocketFactory sslSocketFactory = null;
        try {
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, trustAllCerts, new java.security.SecureRandom());

            sslSocketFactory = sslContext.getSocketFactory();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (KeyManagementException e) {
            e.printStackTrace();
        }

        return sslSocketFactory;
    }

	@SuppressWarnings("deprecation")
	private static void setProxyIfNecessary(Context context, HttpClient httpClient) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo ni = cm.getActiveNetworkInfo();
        if (ni != null && ni.isAvailable() && ni.getType() == ConnectivityManager.TYPE_MOBILE) {
			String proxyHost = android.net.Proxy.getDefaultHost();            
            int proxyPort = android.net.Proxy.getDefaultPort();
            Logger.i("getDefaultHost : " + proxyHost + ":" + proxyPort);
            if (proxyHost == null || proxyPort < 0) {
                return;
            } 
            Logger.i("setProxyIfNecessary : " + proxyHost + ":" + proxyPort);
            HttpHost host = new HttpHost(proxyHost, proxyPort);            
            httpClient.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY, host);
        }
    }
	
	private static void setProxyIfWap(Context context, HttpClient httpClient) {
		int flag = isCmwapType(context);		
        if (1==flag) {        	
        	Logger.i("setProxyIfWap cmwap");
        	HttpHost host = new HttpHost("10.0.0.172", 80);                    
            httpClient.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY, host);
        }
        else if(2==flag){
        	Logger.i("setProxyIfWap ctwap");
        	HttpHost host = new HttpHost("10.0.0.200", 80);                    
            httpClient.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY, host);
        }
    }
	
	private static int isCmwapType(Context ctx) {
		ConnectivityManager cm = (ConnectivityManager)ctx.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo ni = cm.getActiveNetworkInfo();		
        if (ni != null && ni.isAvailable()) {
			if(ni.getType() == ConnectivityManager.TYPE_MOBILE){
				String extraInfo = ni.getExtraInfo();
				Logger.i("isCmwapType apn:"+extraInfo); 
				if (extraInfo == null) {
					return 0;
				}
				//"10.0.0.172:80"
				if("cmwap".equalsIgnoreCase(extraInfo)
					|| "3gwap".equalsIgnoreCase(extraInfo)
					|| "uniwap".equalsIgnoreCase(extraInfo)){				
					return 1;
				}
				//"10.0.0.200:80"
				else if("ctwap".equalsIgnoreCase(extraInfo)){
				
					return 2;
				}
			}
        }
        return 0;
	}

}
