package com.gm.sdk;

import android.Manifest;
import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;
import com.czt.mp3recorder.MP3Recorder;
import com.czt.mp3recorder.RecorderStateListener;
import com.gm.album.AlbumUtils;
import com.gm.baiduloc.BaiduLoc;
import com.gm.baiduloc.BaiduLocListener;
import com.gm.sdkconfig.sdkconfig;
import com.gm.sysinfo.sysinfo;
import com.gm.utils.Logger;
import com.meituan.android.walle.ChannelInfo;
import com.meituan.android.walle.WalleChannelReader;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.DefaultHttpClient;
import org.cocos2dx.lib.Cocos2dxActivity;
import org.cocos2dx.lib.Cocos2dxLuaJavaBridge;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import pub.devrel.easypermissions.EasyPermissions;

/**
 * Created by zhangchao on 2018\5\11 0011.
 */

public class sdk {
    private static RecorderStateListener recorderStateListener = null;
    private static BaiduLocListener s_loc_listener = null;

    private static Cocos2dxActivity m_context;
    // --- clipboard ---
    private static android.content.ClipboardManager m_ClipboardManager11;
    private static android.text.ClipboardManager m_ClipboardManager10;
    private static MP3Recorder mRecorder;
    private static HashMap<String, String> gMap;
    //lua函数回调
    private static int luaevthandler = 0;
    //裁剪图片 数据
    private static Bundle pickImgBundle;
    //定义图片的Uri
    private static Uri photoUri;

    private static void initBaiduLocListener() {
        s_loc_listener = new BaiduLocListener() {
            @Override
            public void onLocationResult(int error, double longitude, double latitude, String address, String country, String province, String city, String district, String street, String streetnumb, String detail, String describe) {
                HashMap<String, Object> nmap = new HashMap<String, Object>();
                //convert to game
				nmap.put(sdkconfig.SDK_EVT, sdkconfig.SDK_EVT_LOCATION);
				nmap.put(sdkconfig.SDK_LOCATION_LONGITUDE, longitude);
				nmap.put(sdkconfig.SDK_LOCATION_LATITUDE, latitude);
				nmap.put(sdkconfig.SDK_LOCATION_ADDRESS, address);
				nmap.put(sdkconfig.SDK_LOCATION_ADDRESS_COUNTY, country);
				nmap.put(sdkconfig.SDK_LOCATION_ADDRESS_PROVINCE, province);
				nmap.put(sdkconfig.SDK_LOCATION_ADDRESS_CITY, city);
				nmap.put(sdkconfig.SDK_LOCATION_ADDRESS_DISTRICT, district);
				nmap.put(sdkconfig.SDK_LOCATION_ADDRESS_STREET, street);
				nmap.put(sdkconfig.SDK_LOCATION_ADDRESS_STREETNUMBER, streetnumb);
				nmap.put(sdkconfig.SDK_LOCATION_ADDRESS_DETAIL, detail);
				nmap.put(sdkconfig.SDK_LOCATION_ADDRESS_DESCRIBE, describe);
				nmap.put(sdkconfig.SDK_ERROR, 0);
				notifyEventByObject(nmap);
            }
        };
    }

    //回调lua
    //-----------------------------------------------------
    public static void notifyEventByObject(HashMap<String, Object> data) {
        try {
            JSONObject jsonObj = new JSONObject();
            for (String key : data.keySet()) {
                jsonObj.put(key, data.get(key));
            }
            String str = jsonObj.toString();
            str = str.replace("\\/", "/");
            notifyEvent(str);
        } catch (JSONException je) {

        }
    }

    public static void notifyEvent(final String str) {
        if (luaevthandler > 0) {
            m_context.runOnGLThread(new Runnable() {
                public void run() {
                    Log.e("sdk", "notifyEvent=====str：" + str);
                    Cocos2dxLuaJavaBridge.callLuaFunctionWithString(luaevthandler, str);
                }
            });
        }
    }

    public static void setEventHandler(int handle) {
        Cocos2dxLuaJavaBridge.releaseLuaFunction(luaevthandler);
        luaevthandler = handle;
    }

    public static void init(Cocos2dxActivity context) {
        m_context = context;
        sysinfo.init(m_context);
//        initBaiduLocListener();
//        BaiduLoc.init(m_context,s_loc_listener);
    }

    public static void initHandle(int handle) {
        luaevthandler = handle;
        //游戏传过来的数据key
        if(null==gMap)
        {
            Logger.d("sdk init error, no config");
            return;
        }
        sdk_um.init(m_context, gMap);
//        sdk.initRecord(gMap);
    }
    //初始化游戏配置
    public static int initConfig(final String params) {
        try {
            JSONObject obj = new JSONObject(params);
            if(null==gMap){
                gMap = new HashMap<String, String>();
            }
            gMap.clear();

            Iterator iterator = obj.keys();
            while(iterator.hasNext()){
                String key = (String) iterator.next();
                String value = obj.getString(key);
                gMap.put(key, value);
            }
            return 0;
        } catch (JSONException e) {
            return -1;
        }
    }

    public static void login(final String params) {
        m_context.runOnUiThread(new Runnable() {
            public void run() {
                try {
                    JSONObject obj = new JSONObject(params);
                    int type = obj.getInt("type");
                    sdk_um.login(type);
                } catch (JSONException e) {
                    HashMap<String, Object> nmap = new HashMap<String, Object>();
                    nmap.put(sdkconfig.SDK_ERROR, -1);
                    nmap.put(sdkconfig.SDK_ERROR_MSG, "sdk login jsonObj data resolution fail");
                    notifyEventByObject(nmap);
                }
            }
        });
    }

    public static void pay(final String params) {
        m_context.runOnUiThread(new Runnable() {
            public void run() {
                try {
                    JSONObject obj = new JSONObject(params);
                    String type = obj.getString("type");
                    if (type.equals(sdkconfig.SDK_TYPE_QQ)) {

                    } else if (type.equals(sdkconfig.SDK_TYPE_WX)) {

                    }
                } catch (JSONException e) {
                    HashMap<String, Object> nmap = new HashMap<String, Object>();
                    nmap.put(sdkconfig.SDK_ERROR, -1);
                    nmap.put(sdkconfig.SDK_ERROR_MSG, "sdk pay jsonObj data resolution fail");
                    notifyEventByObject(nmap);
                }
            }
        });
    }

    // 打开android 默认浏览器
    public static void openBrowser(String url) {
        if ("" != url && null != m_context) {
            Intent intent = new Intent();
            intent.setAction("android.intent.action.VIEW");
            Uri content_url = Uri.parse(url);
            intent.setData(content_url);
            m_context.startActivity(intent);
        }
    }

    public static void openWebView(final String url) {
        m_context.runOnUiThread(new Runnable() {
            public void run() {
                if ((!("".equals(url))) && null !=  m_context) {
                    Intent intent = new Intent();
                    intent.setClass(m_context, WebViewActivity.class);
                    Bundle bunld = new Bundle();
                    bunld.putString("targetUrl", url);
                    intent.putExtras(bunld);
                    m_context.startActivity(intent);
                }
            }
        });
    }

    //分享接口
    public static void share(final String params) {
        m_context.runOnUiThread(new Runnable() {
            public void run() {
                try {
                    JSONObject obj = new JSONObject(params);
                    int type = obj.getInt("type");
                    String title = obj.getString("title");
                    String text = obj.getString("text");
                    String img = obj.getString("img");
                    String url = obj.getString("url");
                    sdk_um.share(type, title, text, img, url);
                } catch (JSONException e) {
                    HashMap<String, Object> nmap = new HashMap<String, Object>();
                    nmap.put(sdkconfig.SDK_EVT, sdkconfig.SDK_EVT_SHARE);
                    nmap.put(sdkconfig.SDK_ERROR, -1);
                    nmap.put(sdkconfig.SDK_ERROR_MSG, "share jsonObj params resolution fail");
                    notifyEventByObject(nmap);
                }
            }
        });
    }

    public static void save_image_album(final String imagefile) {
        m_context.runOnUiThread(new Runnable() {
            public void run() {
                boolean ret = AlbumUtils.saveImage(imagefile, sysinfo.getAppName());
                if(ret)
                {
                    Toast toast= Toast.makeText(m_context.getApplicationContext(),"保存图片成功",Toast.LENGTH_SHORT);
                    toast.setMargin(50,50);
                    toast.show();
                }
                else
                {
                    Toast toast= Toast.makeText(m_context.getApplicationContext(),"保存图片失败",Toast.LENGTH_SHORT);
                    toast.setMargin(50,50);
                    toast.show();
                }
                //notify--look--
                HashMap<String, Object> nmap = new HashMap<String, Object>();
                nmap.put(sdkconfig.SDK_EVT, sdkconfig.SDK_EVT_SAVE_IMAGE);
                if(ret) {
                    nmap.put(sdkconfig.SDK_ERROR, 0);
                }
                else {
                    nmap.put(sdkconfig.SDK_ERROR, -1);
                }
                notifyEventByObject(nmap);
            }
        });
    }

    //剪切板---------------------------------------
    public static void initPasteboard() {
        if (Build.VERSION.SDK_INT >= 11) {
            m_ClipboardManager11 = (android.content.ClipboardManager) m_context.getSystemService(Context.CLIPBOARD_SERVICE);
        } else {
            m_ClipboardManager10 = (android.text.ClipboardManager) m_context.getSystemService(Context.CLIPBOARD_SERVICE);
        }
    }

    public static String getPasteboard() {
        String content = "";
        if (android.os.Build.VERSION.SDK_INT >= 11) {
            content = m_ClipboardManager11.getPrimaryClip().getItemAt(0).getText().toString().trim();
            return content;
        } else {
            content = m_ClipboardManager10.getText().toString().trim();
        }
        return content;
    }

    public static void setPasteboard(String str) {
        if(android.os.Build.VERSION.SDK_INT>=11)
        {
            m_ClipboardManager11.setPrimaryClip(ClipData.newPlainText("1",str));
        } else {
            m_ClipboardManager10.setText(str);
        }
    }

    // 复制剪切板
    public static boolean copyToClipboard(final String str) {
        if (null == m_context) {
            sysinfo.showNativeToast("复制失败！");
            return false;
        }
        try {
            m_context.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    initPasteboard();
                    setPasteboard(str);
                    sysinfo.showNativeToast("复制成功！");
                }
            });
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        sysinfo.showNativeToast("复制失败！");
        return false;
    }
    //录音--------------------------------------
    /*
    初始化录音
   */
    public static boolean initRecord(final HashMap<String, String> data) {
        if (data.containsKey(sdkconfig.TOKEN_RECORD_DURATION_LIMIT)) {
            String aa = (String) data.get(sdkconfig.TOKEN_RECORD_DURATION_LIMIT);
            try {
                Float f = Float.valueOf(aa);
                MP3Recorder.setLimitDuration(f);
            } catch (Exception e) {
                Log.d(sdkconfig.SDK_TAG, "initRecord fail: " + e);
            }
        }
        return true;
    }

    private static void initRecorderStateListener() {
        recorderStateListener = new RecorderStateListener() {
            @Override
            public void onRecorderState(String state, HashMap<String, Object> data) {
                String fileName = (String) data.get(sdkconfig.SDK_FILENAME);
                if (fileName == null) {
                    fileName = "";
                }
                Double duration=0.00;
                if(data.containsKey("duration"))
                {
                    duration = (Double) data.get("duration");
                }
                HashMap<String, Object> nmap = new HashMap<String, Object>();
                nmap.put(sdkconfig.SDK_EVT, sdkconfig.SDK_EVT_RECORD);
                nmap.put(sdkconfig.SDK_ERROR, 0);
                nmap.put(sdkconfig.SDK_RECORD_STATE, state);
                nmap.put(sdkconfig.SDK_FILENAME, fileName);
                nmap.put(sdkconfig.SDK_RECORD_DURATION, duration);
                notifyEventByObject(nmap);
            }
        };
    }
    /**
     * 开始录音
     * @param param
     */
    public static boolean startRecord(final String param) {
        stopRecord();
        if(mRecorder!=null)
        {
            return false;
        }
        //请求权限
        String [] premissions =  new String[]{Manifest.permission.RECORD_AUDIO , Manifest.permission.WRITE_EXTERNAL_STORAGE};
        reqPermission(sdkconfig.RECORD_REQCODE, premissions, null);

        initRecorderStateListener();
        if (recorderStateListener == null) {
            Log.d("sdk", "recorderStateListener is null");
            return false;
        }
                mRecorder = new MP3Recorder(recorderStateListener);
                try {
                    JSONObject jsonObject = new JSONObject(param);
                    String filename = jsonObject.getString(sdkconfig.SDK_FILENAME);
//                    String filename = Environment.getExternalStorageDirectory().getAbsolutePath()+ "/luyin/";//手机设置的存储位置
//                    File file = new File(filename);
//                    if (!file.exists()) {
//                        file.mkdirs();
//                    }
//                    File pcmFile = new File(file, System.currentTimeMillis() + ".pcm");
//                    String pcmFileName =  pcmFile.getAbsolutePath();
                    mRecorder.start(filename);
                    return true;
                } catch (Exception e) {
                    Log.d(sdkconfig.SDK_TAG, "startRcecord fail: " + e);
                }

        return false;
    }
    /**
     * 停止录音
     */
    public static void stopRecord() {
        if (mRecorder == null) {
            return;
        }
        mRecorder.stop();
        if(mRecorder.isStoped())
        {
            mRecorder = null;
        }
    }

    public static int recordGetVolume() {
        if (mRecorder == null) {
            return 0;
        }
        return mRecorder.getVolume();
    }
    //查询录音时长信息
    public static int getAudioDuration(String fileName){
        Log.d(sdkconfig.SDK_TAG, "findAudioDuration" + fileName);
        try{
            MediaMetadataRetriever retriever = new MediaMetadataRetriever();
            String path = fileName;
            if(null != path){
                retriever.setDataSource(path);
                String duration = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
                retriever.release(); //释放
                return TextUtils.isEmpty(duration) ? 0 :(int)Math.ceil( Float.parseFloat(duration) / 1000.0f);
            }
        }catch (Exception e){
            Log.e(sdkconfig.SDK_TAG,e.getMessage());
            return -1;
        }
        return -1;
    }

    public static HashMap getSchemeParams(Intent intent){
        if (null != intent) {
            String action = intent.getAction();
            if (Intent.ACTION_VIEW.equals(action)) {
                Uri data = intent.getData();
                assert data != null;
                String scheme = data.getScheme();//scheme
                if ("hsgamepsz".equals(scheme)) {
                    //  花色牛牛
                    String host = data.getHost();//主机名
                    if ("privateroom".equals(host)) {
                        String query = data.getQuery();         //  参数->query1=value1&query2=value2
                        String[] queryArry = query.split("&");
                        HashMap<String, String> paramsMap = new HashMap<String, String>();
                        for(String s : queryArry) {
                            String[] arr = s.split("=");
                            paramsMap.put(arr[0], arr[1]);
                        }
                        return paramsMap;
                    }
                }
            }
        }
        return null;
    }

    public static String getExternInfo(Context context){
        final ChannelInfo channelInfo = WalleChannelReader.getChannelInfo(context);
        if(channelInfo != null){
            Map<?, ?> extraInfo = channelInfo.getExtraInfo();
            try {
                JSONObject jsonObj = new JSONObject(extraInfo);
                return jsonObj.toString();
            } catch(Exception e) {
                Log.e(sdkconfig.SDK_TAG,e.toString());
            }
        }
        return null;
    }

    public static String  getChannel(Context context){
        String Channel =  sdkconfig.DEFAULT_CHANNEL;
        if(context != null){
            ChannelInfo channelInfo = WalleChannelReader.getChannelInfo(context);
            if (channelInfo != null) {
                Channel = channelInfo.getChannel();
            }
        }else{

        }
        return Channel;
    };

    /**
     * 相册选择图片，替换，能裁剪
     */
    public static void pickImg(final String parmes) {
        m_context.runOnUiThread(new Runnable() {
            public void run() {
                try {
                    JSONObject obj = new JSONObject(parmes);
                    int aspectX = obj.getInt("aspectX") ;
                    int aspectY = obj.getInt("aspectY") ;
                    int outputX = obj.getInt("outputX") ;
                    int outputY = obj.getInt("outputY") ;
                    Boolean needClip = obj.getBoolean("needClip") ;

                    pickImgBundle = new Bundle();
                    pickImgBundle.putInt("aspectX", aspectX);
                    pickImgBundle.putInt("aspectY", aspectY);
                    pickImgBundle.putInt("outputX", outputX);
                    pickImgBundle.putInt("outputY", outputY);
                    pickImgBundle.putBoolean("needClip", needClip);

                    Intent intent = new Intent(Intent.ACTION_PICK, null);
                    intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
                    m_context.startActivityForResult(intent, 2);
                } catch (Exception E) {
                    Log.d("sdk", "pickImg parmes error");
                }
            }
        });
    }

    /**
     * @description 裁剪图片
     */
    private static void startPhotoZoom(Uri uri, int REQUE_CODE_CROP) {
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setDataAndType(uri, "image/*");
        // crop=true是设置在开启的Intent中设置显示的VIEW可裁剪
        intent.putExtra("crop", "true");
        // 去黑边
        intent.putExtra("scale", true);
        intent.putExtra("scaleUpIfNeeded", true);
        // aspectX aspectY 是宽高的比例，根据自己情况修改
        intent.putExtra("aspectX", pickImgBundle.getInt("aspectX"));
        intent.putExtra("aspectY", pickImgBundle.getInt("aspectY"));
        // outputX outputY 是裁剪图片宽高像素
        intent.putExtra("outputX", pickImgBundle.getInt("outputX"));
        intent.putExtra("outputY", pickImgBundle.getInt("outputY"));
        intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());
        //取消人脸识别功能
        intent.putExtra("noFaceDetection", true);
        //设置返回的uri
        intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
        //设置为不返回数据
        intent.putExtra("return-data", false);

        m_context.startActivityForResult(intent, REQUE_CODE_CROP);
    }

    public static void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == m_context.RESULT_OK) {
            //从相册取图片，有些手机有异常情况，请注意
            if (requestCode == 2) { //选择
                if (null != data && null != data.getData()) {
                    Log.i("sdk", "图片选择成功" );
                    photoUri = data.getData();
                    final String picPath = uriToFilePath(photoUri);
					Log.d("sdk", "picPath:" + picPath );

                    if (pickImgBundle.getBoolean("needClip")) {
                        startPhotoZoom(photoUri, 3);
                    } else {
                        new Handler().postDelayed(new Runnable(){
                            public void run() {
                                HashMap<String, Object> nmap = new HashMap<String, Object>();
                                nmap.put(sdkconfig.SDK_EVT, sdkconfig.SDK_EVT_PICKIMG);
                                nmap.put(sdkconfig.SDK_FILENAME, picPath);
                                notifyEventByObject(nmap);
                            }
                        }, 300);
                    }
                } else {
                    Log.d("sdk", "图片选择失败" );
                }
            } else if (requestCode == 3) { //裁剪
                if (photoUri != null) {
                    Bitmap bitmap = decodeUriAsBitmap(photoUri);
                    if (bitmap != null) {
                        final String fileName = writeFileByBitmap(bitmap);
						Log.d("sdk", "fileName: "+ fileName );
                        new Handler().postDelayed(new Runnable(){
                            public void run() {
                                HashMap<String, Object> nmap = new HashMap<String, Object>();
                                nmap.put(sdkconfig.SDK_EVT, sdkconfig.SDK_EVT_PICKIMG);
                                nmap.put(sdkconfig.SDK_FILENAME, fileName);
                                notifyEventByObject(nmap);
                            }
                        }, 300);
                    }
                }

            }

        }
    }

    /**
     * 以时间戳命名将bitmap写入文件
     *
     * @param bitmap
     */
    public static String writeFileByBitmap(Bitmap bitmap) {
        String path = Environment.getExternalStorageDirectory().getAbsolutePath();//手机设置的存储位置
        File file = new File(path);
        final File imageFile = new File(file, System.currentTimeMillis() + ".png");
        if (!file.exists()) {
            file.mkdirs();
        }
        try {
            imageFile.createNewFile();
            FileOutputStream outputStream = new FileOutputStream(imageFile);
            bitmap.compress(Bitmap.CompressFormat.PNG, 90, outputStream);
            outputStream.flush();
            outputStream.close();

            return imageFile.getAbsolutePath();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }

    //uri转换bitmap
    private static Bitmap decodeUriAsBitmap(Uri uri) {
        Bitmap bitmap = null;
        try {
            bitmap = BitmapFactory.decodeStream(m_context.getContentResolver().openInputStream(uri));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        }
        return bitmap;
    }

    //把Uri转换为文件路径
    private static String uriToFilePath(Uri uri) {
        //获取图片数据
        String[] proj = {MediaStore.Images.Media.DATA};
        //查询
        Cursor cursor = m_context.managedQuery(uri, proj, null, null, null);
        //获得用户选择的图片的索引值
        int image_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        //返回图片路径
        return cursor.getString(image_index);
    }

//    public static String findChannel(Cocos2dxActivity m_context){
//        String className = m_context.getPackageName() + ".config.HSConfig";
//        Log.d(sdkconfig.SDK_TAG, "findChannel className = "+ className);
//        String ret = "";
//        try {
//            Class<?> cls = Class.forName(className);
//            Method method = cls.getDeclaredMethod("findChannel", Cocos2dxActivity.class);
//            ret = (String) method.invoke(cls.newInstance(), m_context);
//        } catch (Exception e) {
//            Log.e(sdkconfig.SDK_TAG,"findExternInfo not HSConfig file");
//        }
//        return ret;
//    }
//    public static String findExternInfo(Cocos2dxActivity m_context){
//        String className = m_context.getPackageName() + ".config.HSConfig";
//        Log.d(sdkconfig.SDK_TAG, "findExternInfo className = "+ className);
//        String ret = "";
//        try {
//            Class<?> cls = Class.forName(className);
//            Method method = cls.getDeclaredMethod("findExternInfo", Cocos2dxActivity.class);
//            ret = (String) method.invoke(cls.newInstance(), m_context);
//        } catch (Exception e) {
//            Log.e(sdkconfig.SDK_TAG,"findExternInfo not HSConfig file");
//        }
//        return ret;
//    }

    /**
     * 请求权限
     */
    public static void reqPermission(int requestCode, String[] permissions, String msg) {
        if (Build.VERSION.SDK_INT >= 23) {
            if (permissions == null) {
                return ;
            }
            boolean hasPer = EasyPermissions.hasPermissions(m_context, permissions);
            if (hasPer) {
                //已经同意过
                //dosomething
                //sdkcontrol.onPermissionsHave(reqcode, permissions);
                Log.d("sdk", "requestPermission:------>requestCode, hasPer: "+ requestCode +", "+ hasPer);
            } else {
                //未同意过,或者说是拒绝了，再次申请权限
                EasyPermissions.requestPermissions(
                        m_context,  //上下文
                        msg, //提示文言
                        requestCode, //请求码
                        permissions //权限列表
                );
                Log.d("sdk", "requestPermission:------>requestCode, hasPer: "+ requestCode +", "+ hasPer);
            }
        }
    }
}
