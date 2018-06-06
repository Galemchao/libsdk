package com.gm.sdk;
import java.util.HashMap;
import java.util.List;

import org.cocos2dx.lib.Cocos2dxActivity;
import android.app.Application;
import android.content.Intent;
import android.util.Log;
import com.gm.baiduloc.BaiduLoc;
import com.gm.sdkconfig.sdkconfig;
import com.gm.sysinfo.sysinfo;
import com.czt.mp3recorder.MP3Recorder;
import com.umeng.analytics.game.UMGameAgent;

import pub.devrel.easypermissions.AppSettingsDialog;
import pub.devrel.easypermissions.EasyPermissions;

public class sdkcontrol {
    public static Cocos2dxActivity m_context;
    public static MP3Recorder mRecorder;

    public static void onActivityResult(int requestCode, int resultCode, Intent data) {
        sdk_um.onActivityResult(requestCode, resultCode, data);
        sdk.onActivityResult(requestCode, resultCode, data);
    }
    //已经有权限
    public static void onPermissionsHave(int requestCode,  String[] permissions) {
//        if (requestCode == sdkconfig.BDlOCATE_REQCODE) {
//            BaiduLoc.onStart();
//        } else if (requestCode == sdkconfig.UM_REQCODE) {
//
//        }
    }
    // 此处表示权限申请已经成功，可以使用该权限完成app的相应的操作了
    public static void onPermissionsGranted(int requestCode, List<String> perms) {
//        if (requestCode == sdkconfig.BDlOCATE_REQCODE) {
//            BaiduLoc.onStart();
//        } else if (requestCode == sdkconfig.UM_REQCODE) {
//
//        }
    }
    // 此处表示权限申请被用户拒绝了，此处可以通过弹框等方式展示申请该权限的原因，以使用户允许使用该权限
    public static void onPermissionsDenied(int requestCode, List<String> perms) {
//        if (requestCode == sdkconfig.BDlOCATE_REQCODE) {
//            BaiduLoc.stop();
//        } else if (requestCode == sdkconfig.UM_REQCODE) {
//
//        }
    }
    //权限申请回调
    public static void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, m_context);
    }

    public static void onDestroy(Cocos2dxActivity activity) {
        if (mRecorder != null) {
            mRecorder.stop();
        }
    }
    public static void onResume(Cocos2dxActivity activity) {
        UMGameAgent.onResume(activity);
    }
    public static void onPause(Cocos2dxActivity activity) {
        UMGameAgent.onPause(activity);
    }
    public static void onStart(Cocos2dxActivity activity) {

    }
    public static void onStop(Cocos2dxActivity activity) {

    }

    public static void init(Cocos2dxActivity context) {
        m_context = context;
        sdk.init(context);
//        test();
    }

    public static void test() {
        sysinfo.test();
    }

    public static void initApplication(Application context) {
        BaiduLoc.initApplication(context);
    }
    //回调lua
    public static void notifyEventByObject(HashMap<String, Object> data) {
        sdk.notifyEventByObject(data);
    }
    public static void setEventHandler(int handle) {
        sdk.setEventHandler(handle);
    }
    public static void initHandle(int handle) {
        sdk.initHandle(handle);
    }
    //初始化游戏配置
    public static int initConfig(final String params) {
        return sdk.initConfig(params);
    }
    //sdk登录
    public static void login(final String params) {
        sdk.login(params);
    }
    //sdk支付
    public static void pay(final String params) {
        sdk.pay(params);
    }
    // 打开android 默认浏览器
    public static void openBrowser(final String url) {
        sdk.openBrowser(url);
    }
    //H5支付
    public  static void openWebView(final String url) {
        sdk.openWebView(url);
    }
    //分享接口
    public static void share(final String params) {
        sdk.share(params);
    }
    //复制剪切板
    public static boolean copyToClipboard(final String str) {
        return sdk.copyToClipboard(str);
    }
    //录音
    public static boolean startRecord(String data) {
        return sdk.startRecord(data);
    }
    //停止录音
    public static void stopRecord() {
        sdk.stopRecord();
    }
    //获取录音音量大小
    public static int recordGetVolume() {
        return sdk.recordGetVolume();
    }
    //查询录音时长
    public static int getAudioDuration(String fileName){
        return sdk.getAudioDuration(fileName);
    }
    //百度定位
    //---------------------------------------
    public static void startLocate() {
        m_context.runOnUiThread(new Runnable() {
            public void run() {
                BaiduLoc.stop();
                BaiduLoc.start();
            }
        });
    }
    public static void stopLocate() {
        m_context.runOnUiThread(new Runnable() {
            public void run() {
                BaiduLoc.stop();
            }
        });
    }
    public static double getDistance(double alongitude,double alatitude,double blongitude,double blatitude) {
        return BaiduLoc.getDistance(alongitude,alatitude,blongitude,blatitude);
    }
    //保存图片（百度定位）
    public static void save_image_album(final String imagefile) {
        sdk.save_image_album(imagefile);
    }
    // 保存图片到相册
    public static boolean saveImgToSystemGallery(String imgPath, String fileName) {
        Log.d("sdkcontrol", "imgPath:" + imgPath + "; fileName:" + fileName);
        return sysinfo.saveImgToSystemGallery(imgPath, fileName);
    }
    //从相册选择图片
    public static void pickImg(final String parmes) {
        sdk.pickImg(parmes);
    }
    public static String getChannel() {
        return sdk.getChannel(m_context);
    }
    public static String getExternInfo() {
        return sdk.getExternInfo(m_context);
    }
    //获取版本
    public static String getVersionName() {
        return sysinfo.getVersionName();
    }
    //手机振动
    public static boolean vibrate(long millseconds) {
        return sysinfo.startVibrator(millseconds);
    }
    // 获取ip信息
    public static String getIpAddress() {
        return sysinfo.getIpAddress();
    }
    // 获取UUID
    public static String getUUID() {
        return sysinfo.getUUID();
    }
    //获取设备名称
    public static String getDeviceName() {
        return sysinfo.getDeviceName();
    }
    // 获取SD卡路径
    public static String getSDCardDocPath() {
        return sysinfo.getSDCardDocPath();
    }
    // 安装更新包
    public static void installNewApk(String path) {
        sysinfo.installNewApk(path);
    }
    //根据key获取metadata值
    public static String getMetaData(String key) {
        return  sysinfo.getMetaData(key);
    }
}


