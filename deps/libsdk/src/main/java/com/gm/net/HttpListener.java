
package com.gm.net;
/*
public interface YYHttpListener {

    public void onYYHttpResponse(String response);

    public void onYYHttpCancelled();
    
    public boolean onYYHttpCheckDataValid(String response);

}*/

public abstract class HttpListener {
    
    public abstract void onHttpResponse(String response);
    
    public void onHttpStart(){
    	
    }
    public void onHttpCancelled(){
    }    
    public boolean onHttpCheckDataValid(String response){
    	return true;
    }
}
