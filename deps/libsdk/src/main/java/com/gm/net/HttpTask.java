
package com.gm.net;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

import javax.net.ssl.SSLHandshakeException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;

import android.content.Context;
import android.os.AsyncTask;
import android.os.SystemClock;
import android.util.Log;

import com.gm.net.HttpListener;
import com.gm.net.HttpUtils;
import com.gm.utils.Logger;


/***
 * ������http������������������������������http������������
 */
public class HttpTask extends AsyncTask<String, Integer, String> {

    private static final String TAG = "SdkHttpTask";

    private static final int OUT_TIMES = 15000;
    
    private static final int MAX_RETRY_TIME = 3;
    
    private int mMaxRetryCount;
    private int mRetryCount;

    private HttpListener mListener;
    
    private ArrayList<String> mRetryUrls;

    private ArrayList<NameValuePair> mKeyValueArray;
    private byte[] mBytes;

    private boolean mIsHttpPost;

    private Context mContext;
    
    //
    private int mConnetOutTimes;
    private int mSocOutTimes;

    public HttpTask(Context context) {
        mContext = context;
        mRetryCount = 0;
        mMaxRetryCount = MAX_RETRY_TIME;
        mConnetOutTimes = mSocOutTimes = OUT_TIMES;
        Logger.d(TAG, this.toString() + "||HttpTask create mMaxRetryCount =  " + mMaxRetryCount);        
    }
    
    public HttpTask(Context context,int retryTimes, ArrayList<String> retryUrls) {
        mContext = context;
        mRetryCount = 0;
        if(retryTimes>1)
        {
        	mMaxRetryCount = retryTimes;
        }
        else
        {
        	mMaxRetryCount = 1;
        }
        mConnetOutTimes = mSocOutTimes = OUT_TIMES;
        mRetryUrls = retryUrls;
        Logger.d(TAG, this.toString() + "||HttpTask create mMaxRetryCount =  " + mMaxRetryCount);
        Logger.d(TAG, this.toString() + "||HttpTask create retryUrls =  " + retryUrls);        
                
    }
    
    public void setOutTimes(int conTimes,int socTimes){
    	mConnetOutTimes = conTimes;
    	mSocOutTimes = socTimes;
    	checkOutTimes();
    }
    public void setMaxRetryTimes(int retryTimes){
    	if(retryTimes>1)
        {
        	mMaxRetryCount = retryTimes;
        }
        else
        {
        	mMaxRetryCount = 1;
        }
    }
    public void checkOutTimes(){    	
    	if(mConnetOutTimes<5000)
    	{
    		mConnetOutTimes = 5000;
    	}
    	if(mSocOutTimes<5000)
    	{
    		mSocOutTimes = 5000;
    	}
    }    
    
    public void doPost(HttpListener listener, ArrayList<NameValuePair> keyValueArray,
            String url) {
        this.mListener = listener;
        this.mIsHttpPost = true;
        this.mKeyValueArray = keyValueArray;
        this.mRetryCount = 0;
        
        if(mRetryUrls==null)
        {
        	mRetryUrls = new  ArrayList<String>();        	
        }
        if(mRetryUrls.size()==0)
        {
        	mRetryUrls.add(url);
        }
        if(this.mListener!=null){
        	this.mListener.onHttpStart();
        }
        
        execute(url);        
    }
    
    public void doPost(HttpListener listener, byte[] bytes,
            String url) {
        this.mListener = listener;
        this.mIsHttpPost = true;
        this.mBytes = bytes;
        this.mRetryCount = 0;
        
        if(mRetryUrls==null)
        {
        	mRetryUrls = new  ArrayList<String>();        	
        }
        if(mRetryUrls.size()==0)
        {
        	mRetryUrls.add(url);
        }
        if(this.mListener!=null){
        	this.mListener.onHttpStart();
        }        
        
        execute(url);        
    }
    

    public void doGet(HttpListener listener, String url) {
        this.mListener = listener;
        this.mIsHttpPost = false;
        this.mRetryCount = 0;
        
        if(mRetryUrls==null)
        {
        	mRetryUrls = new  ArrayList<String>();        	
        }
        if(mRetryUrls.size()==0)
        {
        	mRetryUrls.add(url);
        }
        if(this.mListener!=null){
        	this.mListener.onHttpStart();
        }
        
        execute(url);
    }
    
    @Override
    protected String doInBackground(String... params) {
        
        String response = null;
        while (response == null && mRetryCount < mMaxRetryCount) {
        	long sdt = SystemClock.uptimeMillis(); 
            if (isCancelled())
                return null;

            try {
                String uri = params[0];
                Logger.d(TAG, this.toString() + "||mRetry  " + mRetryCount + " / " +mMaxRetryCount);
                if(mRetryUrls!=null)
                {
                	int size = mRetryUrls.size();
                	if(size>0)
                	{
                		int idx = mRetryCount%size;
                		uri = mRetryUrls.get(idx);
                		Logger.d(TAG, this.toString() + "||mRetryUrls= " + idx + " : " +uri);
                	}                	
                }
                
                Logger.d(TAG, this.toString() + "||mRetryCount=" + mRetryCount);
                Logger.d(TAG, this.toString() + "||request=" + uri);
                HttpResponse httpResp = executeHttp(mContext, uri);
                if (httpResp != null && !isCancelled()) {

                    int st = httpResp.getStatusLine().getStatusCode();
                    Logger.d(TAG, this.toString() + "||st=" + st);
                    if (st == HttpStatus.SC_OK) {
                    	HttpEntity entity = httpResp.getEntity();
                    	if (entity != null) {
                    		InputStream content = entity.getContent();                    		
                    		if (content != null) {
                    			response = convertStreamToString(content);
                    		}
                    		
                    	}
                    }
                    else
                    {
                    	//output data
                    	HttpEntity entity = httpResp.getEntity();
                    	if (entity != null) {                    		
                    		InputStream content = entity.getContent();
                    		if (content != null) {                    			
                    			Logger.d(TAG, this.toString() + "||Error " + convertStreamToString(content));
                    		}
                    	}
                    }
                }   
                
    			if(mListener!=null && response!=null)
    			{
    				if(false==mListener.onHttpCheckDataValid(response))
    				{
    					response = null;
    					Logger.d(TAG, this.toString() + "||mListener onCheckDataValid false");    					
    				}
    				else
    				{
    					Logger.d(TAG, this.toString() + "||mListener onCheckDataValid true");
    				}
    			}
    			
            } catch (SSLHandshakeException e) {
                e.printStackTrace();
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            } catch (ClientProtocolException e) {
            	e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            
            Logger.d(TAG, this.toString() + "||response=" + response);
            
            mRetryCount++;
            //this function dont used
            //SystemClock.currentThreadTimeMillis(); 
            long edt = SystemClock.uptimeMillis(); 
            if(response==null && edt-sdt<5000 ){            	
	            try {
					Thread.sleep(5000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
            }
            if(response!=null)
            {
            	Logger.w("HttpRsp Time " + mRetryCount + " = " +  (edt-sdt));
            }

        }        
        return response;
    }

    @Override
    protected void onCancelled() {
        super.onCancelled();    
        
        if (mListener != null) {
            Logger.d(TAG, this.toString() + "||onCancelled");
            mListener.onHttpCancelled();
            mListener = null;            
        }
        mRetryCount = 0;
    }
    @Override
    protected void onProgressUpdate(Integer... values) {
        super.onProgressUpdate(values);
    }

    @Override
    protected void onPostExecute(String response) {
        super.onPostExecute(response);

        if (mListener != null && !isCancelled()) {
        	String rsp = "";
        	if(null==response){
        		rsp = "null";
        	}else{
        		rsp = "length = " + response.length();
        	}
            Logger.d(TAG, this.toString() + "||onResponse " + rsp);
            mListener.onHttpResponse(response);
            mListener = null;
        }
    }

    private HttpResponse executeHttp(Context context, String uri) throws SSLHandshakeException,
            ClientProtocolException, IOException {
    	
    	checkOutTimes();
    	if(mIsHttpPost){
    		if(mBytes!=null){    			
    			return HttpUtils.postWithOutTime(context, uri, mBytes,mConnetOutTimes,mSocOutTimes);
    		}else{
    			return HttpUtils.postWithOutTime(context, uri, mKeyValueArray,mConnetOutTimes,mSocOutTimes);	
    		}    		
    	}
    	else{
    		
    		return HttpUtils.getWithOutTime(context, uri,mConnetOutTimes,mSocOutTimes);
    	}
    		
        //return mIsHttpPost ? HttpUtils.postWithOutTime(context, uri, mKeyValueArray,15000,15000) : HttpUtils.getWithOutTime(context, uri,15000,15000);
    }

    private static String convertStreamToString(InputStream is) {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();
        String line = null;
        try {
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return sb.toString();
    }
}
