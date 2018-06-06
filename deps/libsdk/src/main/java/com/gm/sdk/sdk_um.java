package com.gm.sdk;

import java.util.HashMap;
import java.util.Map;

import com.umeng.analytics.UMGameAnalytics;
import com.umeng.common.UMCocosConfigure;
import com.umeng.commonsdk.UMConfigure;
import com.umeng.socialize.Config;
import com.umeng.socialize.PlatformConfig;
import com.umeng.socialize.UMShareAPI;
import com.umeng.socialize.UMShareListener;
import com.umeng.socialize.ShareAction;
import com.umeng.socialize.bean.SHARE_MEDIA;
import com.umeng.socialize.media.UMImage;
import com.umeng.socialize.UMAuthListener;
import com.umeng.socialize.common.QueuedWork;
import com.umeng.socialize.media.UMWeb;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import com.gm.sdkconfig.sdkconfig;

import pub.devrel.easypermissions.EasyPermissions;

public class sdk_um {
    @SuppressLint("StaticFieldLeak")
    private static Activity m_context = null;

    public static void init(Activity context, Map<String, String> cfg) {
        m_context = context;
        Config.DEBUG = true;
        QueuedWork.isUseThreadPool = false;
        UMShareAPI.get(m_context);
        reqPermission();
//        UMConfigure.init(m_context, UMConfigure.DEVICE_TYPE_PHONE, null);
        //设置游戏统计场景
        UMGameAnalytics.init(m_context);
        //设置appkey和channelnull
        UMCocosConfigure.init(m_context, cfg.get(sdkconfig.TOKEN_UM_APPKEY),"yange",UMConfigure.DEVICE_TYPE_PHONE,null);
        UMCocosConfigure.setLog(true);
        PlatformConfig.setWeixin(cfg.get(sdkconfig.TOKEN_WX_APPKEY), cfg.get(sdkconfig.TOKEN_WX_APPSECRET));
    }

    public static boolean isInstall(int type) {
        UMShareAPI mShareAPI = UMShareAPI.get(m_context);
        return mShareAPI.isInstall(m_context,SHARE_MEDIA.WEIXIN);
    }

    public static void login(final int type) {
        final UMShareAPI mShareAPI = UMShareAPI.get(m_context);
        final UMAuthListener umUserinfoListener = new UMAuthListener() {
            @Override
            public void onStart(SHARE_MEDIA platform) {
                Log.d("sdk_um", "登录开始：" + platform);
            }
            @Override
            public void onComplete(SHARE_MEDIA platform, int action, Map<String, String> data) {
                Log.d("sdk_um", "登录成功：" + platform);
                sdk_um.um_login_notify(0, type,data);
            }

            @Override
            public void onError(SHARE_MEDIA platform, int action, Throwable t) {
                Log.d("sdk_um", "登录失败：" + platform);
                sdk_um.um_login_notify(1, type,null);
            }

            @Override
            public void onCancel(SHARE_MEDIA platform, int action) {
                Log.d("sdk_um", "取消登录："+ platform);
                sdk_um.um_login_notify(2, type,null);
            }
        };

        if (type == sdkconfig.SDK_TYPE_WX) {
            mShareAPI.getPlatformInfo(m_context, SHARE_MEDIA.WEIXIN, umUserinfoListener);
        } else if (type == sdkconfig.SDK_TYPE_QQ) {
            mShareAPI.getPlatformInfo(m_context, SHARE_MEDIA.QQ, umUserinfoListener);
        }
    }
    // 取消登录
    public static void deleteLogin(final int type) {
        final UMShareAPI mShareAPI = UMShareAPI.get(m_context);
        UMAuthListener deletUmAuthListener = new UMAuthListener() {
            @Override
            public void onStart(SHARE_MEDIA share_media) {
                Log.d("sdk_um", "取消授权开始：" + share_media);
            }
            @Override
            public void onComplete(SHARE_MEDIA platform, int action, Map<String,String> data) {
                Log.d("sdk_um", "取消授权成功：" + platform);
            }
            @Override
            public void onError(SHARE_MEDIA platform, int action, Throwable t) {
                Log.d("sdk_um", "取消授权失败：" + platform);
            }
            @Override
            public void onCancel(SHARE_MEDIA platform, int action) {
                Log.d("sdk_um", "取消授权：" + platform);
            }
        };
        if (type == sdkconfig.SDK_TYPE_WX) {
            mShareAPI.deleteOauth(m_context, SHARE_MEDIA.WEIXIN, deletUmAuthListener);
        } else if (type == sdkconfig.SDK_TYPE_QQ) {
            mShareAPI.deleteOauth(m_context, SHARE_MEDIA.WEIXIN, deletUmAuthListener);
        }
    }
    //分享
    public static void share(final int type, String title, String text, String img, String url) {
        UMShareListener umShareListener = new UMShareListener() {
            @Override
            public void onStart(SHARE_MEDIA platform) {}
            @Override

            public void onResult(SHARE_MEDIA platform) {
                Log.d("sdk_um", "分享成功：" + platform);
                sdk_um.um_share_nofity(0,type);
            }

            @Override
            public void onError(SHARE_MEDIA platform, Throwable t) {
                Log.d("sdk_um", "分享失败：" + platform);
                sdk_um.um_share_nofity(1,type);
            }

            @Override
            public void onCancel(SHARE_MEDIA platform) {
                Log.d("sdk_um", "取消分享：" + platform);
                sdk_um.um_share_nofity(2,type);
            }
        };

        try {
            final ShareAction shareact = new ShareAction(m_context);
            if ( null != img && img.length() > 0) {
            //bmp 分享
                Bitmap bmp_orgin = BitmapFactory.decodeFile(img);
                Bitmap bmp = Bitmap.createScaledBitmap(bmp_orgin, 960, 540, true);
                Bitmap thumb_bmp = Bitmap.createScaledBitmap(bmp_orgin, 128, 72, true);
                UMImage image = new UMImage(m_context, bmp);
                UMImage thumb = new UMImage(m_context, thumb_bmp);
                image.setTitle(title);
                image.setThumb(thumb);
                shareact.withText(text).withMedia(image);
//jpg 分享出来无法点开放大显示，尺寸太大???
//                UMImage image = new UMImage(m_context, img);
//                //image.compressStyle = UMImage.CompressStyle.SCALE;//大小压缩，默认为大小压缩，适合普通很大的图
//                image.compressStyle = UMImage.CompressStyle.QUALITY;//质量压缩，适合长图的分享
//                //image.compressFormat = Bitmap.CompressFormat.PNG;//用户分享透明背景的图片可以设置这种方式，
//                image.compressFormat = Bitmap.CompressFormat.JPEG;

            }
            else if (null != url && url.length() > 0) {
                UMWeb web = new UMWeb(url);
                web.setTitle(title);
                web.setDescription(text);
                shareact.withMedia(web);
                int iconid = getDrawableIconId();
                if(iconid>0)
                {
                    //Bitmap thumb = BitmapFactory.decodeResource(m_context.getResources(), iconid);
                    //int WX_THUMB_SIZE = 72;
                    //Bitmap thumbBmp = Bitmap.createScaledBitmap(thumb, WX_THUMB_SIZE, WX_THUMB_SIZE, true);
                    UMImage image = new UMImage(m_context, iconid);
                    //透明设置，否则icon黑边
                    image.compressFormat = Bitmap.CompressFormat.PNG;
                    web.setThumb(image);
                }
            }
            else{
                shareact.withText(text).withSubject(title);
            }

            shareact.setCallback(umShareListener);
            if (type == sdkconfig.SDK_TYPE_NORMAL) {
                shareact.setDisplayList(SHARE_MEDIA.WEIXIN, SHARE_MEDIA.QQ);
                shareact.open();
            } else if (type == sdkconfig.SDK_TYPE_WX) {
                shareact.setPlatform(SHARE_MEDIA.WEIXIN);
                shareact.share();
            } else if (type == sdkconfig.SDK_TYPE_WXCIRCLE) {
                shareact.setPlatform(SHARE_MEDIA.WEIXIN_CIRCLE);
                shareact.share();
            } else if (type == sdkconfig.SDK_TYPE_QQ) {
                shareact.setPlatform(SHARE_MEDIA.QQ);
                shareact.share();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private static void um_login_notify(int result, int type, Map<String, String> data) {
        HashMap<String, Object> nmap = new HashMap<String, Object>();
        if (result == 0) {
            for (String key : data.keySet()) {
                nmap.put(key, data.get(key));
            }
            //convert to game
            nmap.put(sdkconfig.SDK_NAME, data.get("name"));//screen_name
            nmap.put(sdkconfig.SDK_ICONURL, data.get("iconurl"));//profile_image_url
            nmap.put(sdkconfig.SDK_ACCESS_TOKEN, data.get("accessToken"));//access_token
            nmap.put(sdkconfig.SDK_REFRESH_TOKEN, data.get("refreshToken"));//RefreshToken

            nmap.put(sdkconfig.SDK_EVT, sdkconfig.SDK_EVT_LOGIN);
            nmap.put(sdkconfig.SDK_ERROR, 0);
            nmap.put(sdkconfig.SDK_TYPE, type);
            sdk.notifyEventByObject(nmap);
        } else {
            nmap.put(sdkconfig.SDK_EVT, sdkconfig.SDK_EVT_LOGIN);
            nmap.put(sdkconfig.SDK_ERROR, 1);
            nmap.put(sdkconfig.SDK_TYPE, type);
            sdk.notifyEventByObject(nmap);
        }
    }

    private static void um_share_nofity(int result, int type) {
        HashMap<String, Object> nmap = new HashMap<String, Object>();
        nmap.put(sdkconfig.SDK_EVT, sdkconfig.SDK_EVT_SHARE);
        nmap.put(sdkconfig.SDK_ERROR, result);
        nmap.put(sdkconfig.SDK_TYPE, type);
        sdk.notifyEventByObject(nmap);
    }

    public static void onActivityResult(int requestCode, int resultCode, Intent data) {
        UMShareAPI.get(m_context).onActivityResult(requestCode, resultCode, data);
    }

    private static int getResourceId(String pVariableName, String pResourcename)
    {
        try {
            String pPackageName = m_context.getPackageName();
            return m_context.getResources().getIdentifier(pVariableName, pResourcename, pPackageName);
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }
    private static int getDrawableIconId()
    {
        return getResourceId("icon","drawable");
    }

    private static void reqPermission() {
        if(Build.VERSION.SDK_INT>=23){
            String[] mPermissionList = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.CALL_PHONE,Manifest.permission.READ_LOGS,Manifest.permission.READ_PHONE_STATE, Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.SET_DEBUG_APP,Manifest.permission.SYSTEM_ALERT_WINDOW,Manifest.permission.GET_ACCOUNTS,Manifest.permission.WRITE_APN_SETTINGS};
            boolean hasPer = EasyPermissions.hasPermissions(m_context, mPermissionList);
            if (hasPer) {
                //已经同意过
                //dosomething
                //sdkcontrol.onPermissionsHave(reqcode, permissions);
                Log.d("sdk_um", "requestPermission:------>hasPer: " + hasPer);
            } else {
                //未同意过,或者说是拒绝了，再次申请权限
                EasyPermissions.requestPermissions(
                        m_context,  //上下文
                        null, //提示文言
                        100, //请求码
                        mPermissionList //权限列表
                );
                Log.d("sdk_um", "requestPermission:------>hasPer: " + hasPer);
            }
        }
    }
}


