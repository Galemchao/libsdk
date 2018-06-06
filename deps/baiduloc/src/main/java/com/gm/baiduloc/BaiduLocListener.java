package com.gm.baiduloc;
import java.util.HashMap;

/**
 * Created by bolin on 2016/12/29.
 */

public interface BaiduLocListener {
//    public void onLocationResult(int error,double longitude, double latitude, String address, String describe);
    public void onLocationResult(int error,double longitude, double latitude, String address,
                                 String country,String province,String city,String district,String street,String streetnumb,
                                 String detail,String describe);
}
