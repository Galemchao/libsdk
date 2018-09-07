package com.gm.gaodeloc;

import org.cocos2dx.lib.Cocos2dxActivity;

import android.Manifest;
import android.app.Application;

import android.os.Build;
//import android.os.Vibrator;
import android.util.Log;

import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.maps2d.AMapUtils;
import com.amap.api.maps2d.model.LatLng;

import pub.devrel.easypermissions.EasyPermissions;

public class aMap {
    public static Cocos2dxActivity m_context;

    //声明mlocationClient对象
    public static AMapLocationClient mLocationClient = null;
    //声明mLocationOption对象
    public static AMapLocationClientOption mLocationOption = null;

    public static boolean isRun = true;

    public static void initApplication(Application context) {

    }

    public static void init(Cocos2dxActivity context, AMapLocationListener listener) {
        m_context = context;
        //初始化定位
        mLocationClient = new AMapLocationClient(m_context);
        //设置定位回调监听
        mLocationClient.setLocationListener(listener);
        setLocationOption();
        isRun = false;
    }

    public static void setLocationOption() {
        //初始化AMapLocationClientOption对象
        mLocationOption = new AMapLocationClientOption();
        //设置定位场景，目前支持三种场景（签到、出行、运动，默认无场景）
//		mLocationOption.setLocationPurpose(AMapLocationClientOption.AMapLocationPurpose.SignIn);
        //设置定位模式为高精度模式。
        mLocationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
        //获取一次定位结果,该方法默认为false。
        mLocationOption.setOnceLocation(true);
        //自定义连续定位	设置定位间隔,单位毫秒,默认为2000ms，最低1000ms
//		mLocationOption.setInterval(1000);
        //设置是否返回地址信息（默认返回地址信息）
        mLocationOption.setNeedAddress(true);
        //设置是否允许模拟位置,默认为true，允许模拟位置
        mLocationOption.setMockEnable(false);
        //关闭缓存机制
        mLocationOption.setLocationCacheEnable(false);
        //给定位客户端对象设置定位参数
        mLocationClient.setLocationOption(mLocationOption);
    }

    public static double getDistance(double alongitude, double alatitude, double blongitude, double blatitude) {
        LatLng latLng1 = new LatLng(alatitude, alongitude);
        LatLng latLng2 = new LatLng(blatitude, blongitude);
        float distance = AMapUtils.calculateLineDistance(latLng1, latLng2);
        return (double) distance;
    }

    public static void start() {
        if (!isRun) {
            onStart();
        }
    }

    public static void onStart() {
        if (null != mLocationClient) {
            isRun = true;
            mLocationClient.setLocationOption(mLocationOption);
            mLocationClient.startLocation();
        }
    }

    public static void stop() {
        if (isRun && null != mLocationClient) {
            isRun = false;
            mLocationClient.stopLocation();//停止定位后，本地定位服务并不会被销毁
        }
    }

    public static void onDestroy() {
        if (null != mLocationClient) {
            mLocationClient.onDestroy();//销毁定位客户端，同时销毁本地定位服务
        }
    }

    public static String[] needPermission() {
        int sdkInt = android.os.Build.VERSION.SDK_INT;
        if (sdkInt >= 23) {
            return new String[]{
                    Manifest.permission.READ_PHONE_STATE,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    //Manifest.permission.READ_EXTERNAL_STORAGE,
                    //Manifest.permission.WRITE_EXTERNAL_STORAGE,
            };
        } else {
            return null;
        }
    }

    /**
     * 请求权限
     */
    public static void reqPermission() {
        if (Build.VERSION.SDK_INT >= 23) {
            String[] permissions = needPermission();
            if (permissions == null) {
                return;
            }
            boolean hasPer = EasyPermissions.hasPermissions(m_context, permissions);
            if (hasPer) {
                //已经同意过
                //dosomething
                //sdkcontrol.onPermissionsHave(reqcode, permissions);
                Log.d("aMap", "requestPermission:------>hasPer: " + hasPer);
            } else {
                //未同意过,或者说是拒绝了，再次申请权限
                EasyPermissions.requestPermissions(
                        m_context,  //上下文
                        null, //提示文言
                        104, //请求码
                        permissions //权限列表
                );
                Log.d("aMap", "requestPermission:------>hasPer: " + hasPer);
            }
        }
    }
}


