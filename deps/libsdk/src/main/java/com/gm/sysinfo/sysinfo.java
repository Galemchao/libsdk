package com.gm.sysinfo;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

import java.util.Enumeration;
import java.util.HashMap;

import java.util.Locale;

import java.util.Random;
import java.util.UUID;


import org.cocos2dx.lib.Cocos2dxActivity;
import org.json.JSONArray;
import org.json.JSONObject;

import android.Manifest;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;

import android.app.Service;
import android.content.BroadcastReceiver;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.BatteryManager;
import android.os.Bundle;
import android.os.Environment;

import android.os.SystemClock;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Vibrator;
import android.support.v4.app.ActivityCompat;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

import com.gm.sdk.sdk;
import com.gm.sdkconfig.sdkconfig;
import com.gm.utils.Logger;

public class sysinfo {
    //-----------------------------------------------------
    public static int m_battery = 0;
    //-----------------------------------------------------
    //-----------------------------------------------------
    public static Cocos2dxActivity m_context;
    public static PackageManager m_pm = null;
    public static BroadcastReceiver m_broadcastReceiver = null;

    public static void init(Cocos2dxActivity context) {
        m_context = context;
        m_pm = m_context.getPackageManager();
        listenBattary();
    }

    //-----------------------------------------------------
    //0 no
    //1 4g
    //2 wifi
    //3 other
    //网络状态
    public static int getNetState() {
        ConnectivityManager mConnectivityManager = (ConnectivityManager) m_context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo mNetworkInfo = mConnectivityManager.getActiveNetworkInfo();
        if (mNetworkInfo != null && mNetworkInfo.isAvailable()) {
            int t = mNetworkInfo.getType();
            if (ConnectivityManager.TYPE_MOBILE == t) {
                return 1;
            } else if (ConnectivityManager.TYPE_WIFI == t) {
                return 2;
            }
            return 3;
        }
        return 0;
    }

    //网络是否连接
    public static boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) m_context.getSystemService(m_context.CONNECTIVITY_SERVICE);
        if (cm != null) {
            NetworkInfo networkInfo = cm.getActiveNetworkInfo();
            ArrayList networkTypes = new ArrayList();
            networkTypes.add(ConnectivityManager.TYPE_WIFI);
            try {
                networkTypes.add(ConnectivityManager.class.getDeclaredField("TYPE_ETHERNET").getInt(null));
            } catch (NoSuchFieldException nsfe) {
            } catch (IllegalAccessException iae) {
                throw new RuntimeException(iae);
            }
            if (networkInfo != null && networkTypes.contains(networkInfo.getType())) {
                return true;
            }
        }
        return false;
    }

    //电池电量0-100
    public static int getBatteryinfo() {
        return m_battery;
    }

    //获取imsi
    @SuppressLint("HardwareIds")
    public static String getIMSI() {
        String imsi = "";
        TelephonyManager mTelephonyMgr = (TelephonyManager) m_context.getSystemService(m_context.TELEPHONY_SERVICE);
        if (!checkPermission(Manifest.permission.READ_PHONE_STATE)) {
            String[] permissions = new String[]{Manifest.permission.READ_PHONE_STATE};
            sdk.reqPermission(103, permissions, null);
        }
        assert mTelephonyMgr != null;
        imsi = mTelephonyMgr.getSubscriberId();
        return imsi;
    }

    //ip地址
    public static String getIpAddress() {
        int st = getNetState();
        if (0 == st) {
            return "";
        } else if (2 == st) {
            return getWifiIpAddress();
        } else {
            return getLocalIpAddress();
        }
    }

    //获取mac地址
    private static String getMacAddress() {
        WifiManager wifi = (WifiManager) m_context.getApplicationContext().getSystemService(m_context.WIFI_SERVICE);
        String szmac = wifi.getConnectionInfo().getMacAddress();
        if (szmac == null || szmac.equals("")) {
            szmac = getMacAddressLinux();
            if (szmac == null || szmac.equals("")) {
                return "11-22-33-44-55";
            }
        }
        return szmac.replace(":", "-");
    }

    /** MacLinux **/
    private static String getMacAddressLinux() {
        try {
            String szmac = loadFileAsString("/sys/class/net/eth0/address").toUpperCase().substring(0, 17);
            if (szmac == null || szmac.equals("")) {
                szmac = loadFileAsString("/sys/class/net/wlan0/address").toUpperCase().substring(0, 17);
            }
            return szmac;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static String loadFileAsString(String filePath) throws IOException {
        StringBuffer fileData = new StringBuffer(1000);
        BufferedReader reader = new BufferedReader(new FileReader(filePath));
        char[] buf = new char[1024];
        int numRead = 0;
        while ((numRead = reader.read(buf)) != -1) {
            String readData = String.valueOf(buf, 0, numRead);
            fileData.append(readData);
        }
        reader.close();
        return fileData.toString();
    }

    //获取系统版本号
    public static String getSystemVersion() {
        return String.valueOf(android.os.Build.VERSION.SDK_INT);
    }

    //获取应用包名
    public static String getPackageName() {
        return m_context.getPackageName();
    }

    //获取应用名称
    public static String getAppName() {
        try {
            PackageInfo m_pkginfo = m_pm.getPackageInfo(m_context.getPackageName(), 0);
            ApplicationInfo m_appinfo = m_pkginfo.applicationInfo;
            int labelRes = m_appinfo.labelRes;
            return m_context.getResources().getString(labelRes);
        } catch (Exception e) {
            Log.e(sdkconfig.UTILS_TAG, e.toString());
        }
        return "";
    }

    // 获取版本号
    public static String getVersionName() {
        String version = sdkconfig.DEFAULT_VERSION;
        try {
            PackageInfo info = m_context.getPackageManager().getPackageInfo(m_context.getPackageName(), 0);
            if (info != null) {
                version = info.versionName;
                return version;
            }
        } catch (Exception e) {
            Log.e(sdkconfig.UTILS_TAG, e.toString());
        }
        return version;
    }

    // 获取版本号(内部识别号)
    public static int getVersionCode() {
        try {
            PackageInfo pi = m_context.getPackageManager().getPackageInfo(m_context.getPackageName(), 0);
            return pi.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return 0;
        }
    }

    //国家
    public static String getCountry() {
        Locale locale;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            locale = m_context.getResources().getConfiguration().getLocales().get(0);
        } else {
            locale = m_context.getResources().getConfiguration().locale;
        }
        return locale.getCountry();
    }

    //下载地址
    public static String downloadurl() {
        return "";
    }

    //是否安装了某个app
    public static boolean isInstall(String name) {
        android.content.pm.PackageInfo packageInfo;
        try {
            packageInfo = m_context.getPackageManager().getPackageInfo(name, 0);

        } catch (PackageManager.NameNotFoundException e) {
            packageInfo = null;
            e.printStackTrace();
        }
        if (packageInfo == null) {
            System.out.println("not installed");
            return false;
        } else {
            System.out.println("is installed");
            return true;
        }
    }

    //安装apk
    public static void installNewApk(final String filepath) {
        m_context.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.setAction(android.content.Intent.ACTION_VIEW);
                File file = new File(filepath);
                intent.setDataAndType(Uri.fromFile(file),
                        "application/vnd.android.package-archive");
                m_context.startActivity(intent);
            }
        });
    }

    // 获取SD卡路径
    public static String getSDCardDocPath() {
        return Environment.getExternalStorageDirectory().getAbsolutePath();
    }

    //获取设备名称
    public static String getDeviceName() {
        String deviceName = Build.MODEL;
        return deviceName;
    }

    //检测本地权限
    public static boolean checkPermission(String permission) {
        boolean result = false;
        if (Build.VERSION.SDK_INT >= 23) {
            try {
                Class<?> clazz = Class.forName("android.content.Context");
                Method method = clazz.getMethod("checkSelfPermission", String.class);
                int rest = (Integer) method.invoke(m_context, permission);
                if (rest == PackageManager.PERMISSION_GRANTED) {
                    result = true;
                } else {
                    result = false;
                }
            } catch (Exception e) {
                result = false;
            }
        } else {
            PackageManager pm = m_context.getPackageManager();
            if (pm.checkPermission(permission, m_context.getPackageName()) == PackageManager.PERMISSION_GRANTED) {
                result = true;
            }
        }
        return result;
    }

    public static JSONObject getJsonFromAssets(Context ctx, String filename) {
        try {
            InputStream in = ctx.getAssets().open(filename);
            int size = in.available();
            byte[] buffer = new byte[size];
            in.read(buffer);
            in.close();
            String jsonStr = new String(buffer);
            JSONObject jsonObject = new JSONObject(jsonStr);
            return jsonObject;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static JSONArray getJsonArrayFromAssets(Context ctx, String filename) {
        try {
            InputStream in = ctx.getAssets().open(filename);
            int size = in.available();
            byte[] buffer = new byte[size];
            in.read(buffer);
            in.close();
            String jsonStr = new String(buffer);
            JSONArray jsonObject = new JSONArray(jsonStr);
            return jsonObject;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    //打开app
    public static void openApp(final String name) {
        m_context.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                boolean flag = isInstall(name);
                if (flag) {
                    android.content.pm.PackageManager packageManager = m_context.getPackageManager();
                    Intent intent = packageManager.getLaunchIntentForPackage(name);
                    m_context.startActivity(intent);
                } else {
                    Toast.makeText(m_context, "没有安装" + name, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public static String toURLEncoded(String paramString) {
        if (paramString == null || paramString.equals("")) {
            return paramString;
        }
        try {
            String str = new String(paramString.getBytes(), "UTF-8");
            str = URLEncoder.encode(str, "UTF-8");
            return str;
        } catch (Exception localException) {
        }

        return paramString;
    }

    public static String toURLDecoded(String paramString) {
        if (paramString == null || paramString.equals("")) {
            return paramString;
        }
        try {
            String str = new String(paramString.getBytes(), "UTF-8");
            str = URLDecoder.decode(str, "UTF-8");
            return str;
        } catch (Exception localException) {
        }

        return paramString;
    }

    //获取文件MD5
    public static String getFileMD5(File file) {
        if (!file.isFile()) {
            return null;
        }

        MessageDigest digest = null;
        FileInputStream in = null;
        byte buffer[] = new byte[1024];
        int len;
        try {
            digest = MessageDigest.getInstance("MD5");
            in = new FileInputStream(file);
            while ((len = in.read(buffer, 0, 1024)) != -1) {
                digest.update(buffer, 0, len);
            }
            in.close();

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        BigInteger bigInt = new BigInteger(1, digest.digest());

        return bigInt.toString(16);
    }

    // 手机震动
    public static boolean startVibrator(long millseconds) {
        Vibrator vb = (Vibrator) m_context.getSystemService(Service.VIBRATOR_SERVICE);
        if (Build.VERSION.SDK_INT >= 11) {
            if (!vb.hasVibrator()) {
                return false;
            }
        }
        vb.vibrate(millseconds);
        return true;
    }

    // 停止震动
    public static void closeVibrator(Activity context) {
        Vibrator vb = (Vibrator) context.getSystemService(Service.VIBRATOR_SERVICE);
        vb.cancel();
    }

    /**
     * 弹出android  toast
     * @param msg
     */
    public static void showNativeToast(final String msg) {
        HashMap<String, Object> nmap = new HashMap<String, Object>();
        nmap.put(sdkconfig.SDK_EVT, sdkconfig.SDK_EVT_TOAST);
        nmap.put(sdkconfig.SDK_ERROR_MSG, msg);
        sdk.notifyEventByObject(nmap);
    }

    //开机时间秒
    public static long elapsedTime() {
        //可以直接从lua调用
        //public static native long elapsedRealtime();
        return SystemClock.elapsedRealtime() / 1000;
    }

    //-------------------
    public static String getLocalIpAddress() {
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements(); ) {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements(); ) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress()) {
                        return inetAddress.getHostAddress().toString();
                    }
                }
            }
        } catch (SocketException ex) {
            Log.e("local IpAddress", ex.toString());
        }
        return "";
    }
//    public static String getLocalIpAddress() {
//        try {
//            String ipv4;
//            List<NetworkInterface> nilist = Collections.list(NetworkInterface.getNetworkInterfaces());
//            for (NetworkInterface ni : nilist) {
//                List<InetAddress> ialist = Collections.list(ni.getInetAddresses());
//                for (InetAddress address : ialist) {
//                    if (!address.isLoopbackAddress() &&
//                            InetAddressUtils.isIPv4Address(ipv4 = address.getHostAddress())) {
//                        return ipv4;
//                    }
//                }
//            }
//        } catch (SocketException ex) {
//            ex.printStackTrace();
//        }
//        return "";
//    }

    private static String intToIp(int i) {
        return (i & 0xFF) + "." +
                ((i >> 8) & 0xFF) + "." +
                ((i >> 16) & 0xFF) + "." +
                ((i >> 24) & 0xFF);
    }

    //获取网络ip
    public static String getWifiIpAddress() {
        WifiManager wifiMgr = (WifiManager) m_context.getApplicationContext().getSystemService(m_context.WIFI_SERVICE);
        WifiInfo wifiInfo = wifiMgr.getConnectionInfo();
        int ip = wifiInfo.getIpAddress();
        return intToIp(ip);
    }

    //获取应用安装路径
    public static String getAppPath() {
        File file = m_context.getExternalFilesDir(null);
        if (null != file)
            return file.getPath();
        return m_context.getFilesDir().getAbsolutePath();
    }

    //获取应用安装目录
    public static File getAppFile() {
        File file = m_context.getExternalFilesDir(null);
        if (null != file)
            return file;
        return null;
    }

    public static byte[] bmpToByteArray(final Bitmap bmp, final boolean needRecycle) {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.PNG, 100, output);
        if (needRecycle) {
            bmp.recycle();
        }
        byte[] result = output.toByteArray();
        try {
            output.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * 保存图片到本地
     *
     * @param name
     *            文件名
     */
    public static String save2File(Bitmap mBitmap, String name) {
        String mSdCardDir = "";
        // 判断SDcard是否挂载
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            // 获取SDCard指定目录下
            mSdCardDir = Environment.getExternalStorageDirectory() + "/hsgame/";
            File file = new File(mSdCardDir, name + ".png");
            File dirFile = new File(mSdCardDir); // 目录转化成文件夹
            FileOutputStream out = null;

            if (!dirFile.exists()) {
                dirFile.mkdirs();
            }

            try {
                if (!file.exists()) {
                    file.createNewFile();
                }
                out = new FileOutputStream(file);
                mBitmap.compress(Bitmap.CompressFormat.PNG, 100, out);// 把数据写入文件
                return file.getAbsolutePath();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    // 关闭流
                    out.flush();
                    out.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return mSdCardDir;
    }

    //保存图片到系统相册
    public static boolean saveImgToSystemGallery(String imgPath, String fileName) {
        if (null == fileName) {
            fileName = "tmp.png";
        }
        Bitmap bitmap = BitmapFactory.decodeFile(imgPath);
        if (null != bitmap) {
            String saveFilePath = save2File(bitmap, fileName);
            File file = new File(saveFilePath);
            if (file.exists()) {
                Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                Uri uri = Uri.fromFile(file);
                intent.setData(uri);
                m_context.sendBroadcast(intent);// 这个广播的目的就是更新图库，发了这个广播进入相册就可以找到你保存的图片了！，记得要传你更新的file哦
                return true;
            }
        }
        return false;
    }

    //电量检测
    public static void listenBattary() {
        if (m_broadcastReceiver != null) {
            return;
        }
        // 声明广播接受者对象
        m_broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                // TODO Auto-generated method stub
                String action = intent.getAction();
                if (action.equals(Intent.ACTION_BATTERY_CHANGED)) {
                    // 得到电池状态：
                    // BatteryManager.BATTERY_STATUS_CHARGING：充电状态。
                    // BatteryManager.BATTERY_STATUS_DISCHARGING：放电状态。
                    // BatteryManager.BATTERY_STATUS_NOT_CHARGING：未充满。
                    // BatteryManager.BATTERY_STATUS_FULL：充满电。
                    // BatteryManager.BATTERY_STATUS_UNKNOWN：未知状态。
                    int status = intent.getIntExtra("status", 0);
                    // 得到健康状态：
                    // BatteryManager.BATTERY_HEALTH_GOOD：状态良好。
                    // BatteryManager.BATTERY_HEALTH_DEAD：电池没有电。
                    // BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE：电池电压过高。
                    // BatteryManager.BATTERY_HEALTH_OVERHEAT：电池过热。
                    // BatteryManager.BATTERY_HEALTH_UNKNOWN：未知状态。
                    int health = intent.getIntExtra("health", 0);
                    // boolean类型
                    boolean present = intent.getBooleanExtra("present", false);
                    // 得到电池剩余容量
                    int level = intent.getIntExtra("level", 0);

                    m_battery = level;

                    // 得到电池最大值。通常为100。
                    int scale = intent.getIntExtra("scale", 0);
                    // 得到图标ID
                    int icon_small = intent.getIntExtra("icon-small", 0);
                    // 充电方式：　BatteryManager.BATTERY_PLUGGED_AC：AC充电。　BatteryManager.BATTERY_PLUGGED_USB：USB充电。
                    int plugged = intent.getIntExtra("plugged", 0);
                    // 得到电池的电压
                    int voltage = intent.getIntExtra("voltage", 0);
                    // 得到电池的温度,0.1度单位。例如 表示197的时候，意思为19.7度
                    int temperature = intent.getIntExtra("temperature", 0);
                    // 得到电池的类型
                    String technology = intent.getStringExtra("technology");
                    // 得到电池状态
                    String statusString = "";
                    // 根据状态id，得到状态字符串
                    switch (status) {
                        case BatteryManager.BATTERY_STATUS_UNKNOWN:
                            statusString = "unknown";
                            break;
                        case BatteryManager.BATTERY_STATUS_CHARGING:
                            statusString = "charging";
                            break;
                        case BatteryManager.BATTERY_STATUS_DISCHARGING:
                            statusString = "discharging";
                            break;
                        case BatteryManager.BATTERY_STATUS_NOT_CHARGING:
                            statusString = "not charging";
                            break;
                        case BatteryManager.BATTERY_STATUS_FULL:
                            statusString = "full";
                            break;
                    }
                    //得到电池的寿命状态
                    String healthString = "";
                    //根据状态id，得到电池寿命
                    switch (health) {
                        case BatteryManager.BATTERY_HEALTH_UNKNOWN:
                            healthString = "unknown";
                            break;
                        case BatteryManager.BATTERY_HEALTH_GOOD:
                            healthString = "good";
                            break;
                        case BatteryManager.BATTERY_HEALTH_OVERHEAT:
                            healthString = "overheat";
                            break;
                        case BatteryManager.BATTERY_HEALTH_DEAD:
                            healthString = "dead";
                            break;
                        case BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE:
                            healthString = "voltage";
                            break;
                        case BatteryManager.BATTERY_HEALTH_UNSPECIFIED_FAILURE:
                            healthString = "unspecified failure";
                            break;
                    }
                    //得到充电模式
                    String acString = "";
                    //根据充电状态id，得到充电模式
                    switch (plugged) {
                        case BatteryManager.BATTERY_PLUGGED_AC:
                            acString = "plugged ac";
                            break;
                        case BatteryManager.BATTERY_PLUGGED_USB:
                            acString = "plugged usb";
                            break;
                    }
                }
            }
        };
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_BATTERY_CHANGED);
        m_context.registerReceiver(m_broadcastReceiver, filter);
    }

    public static void unRegisterReceiver() {
        if (m_broadcastReceiver != null) {
            m_context.unregisterReceiver(m_broadcastReceiver);
            m_broadcastReceiver = null;
        }
    }

    public static String getMetaData(String key) {
        try {
            ApplicationInfo ai = m_pm.getApplicationInfo(m_context.getPackageName(), PackageManager.GET_META_DATA);
            Bundle bundle = ai.metaData;
            String myApiKey = bundle.getString(key);
            if (myApiKey == null) {
//                int v = bundle.getInt(key,-999);
//                if(v!=-999)
//                {
//                    myApiKey = String.valueOf(v);
//                }
                Object o = bundle.get(key);
                if (o != null) {
                    myApiKey = String.valueOf(o);
                }
            }
            return myApiKey;
        } catch (PackageManager.NameNotFoundException e) {
            Logger.e("Failed to load meta-data, NameNotFound" + e.getMessage());
        } catch (NullPointerException e) {
            Logger.e("Failed to load meta-data, NullPointer: " + e.getMessage());
        }
        return null;
    }

    //系统可用内存
    public static double getAvailMem() {
        ActivityManager acmng = (ActivityManager) m_context.getSystemService(m_context.ACTIVITY_SERVICE);
        ActivityManager.MemoryInfo memoryInfo = new ActivityManager.MemoryInfo();
        if (memoryInfo != null) {
            //获得系统可用内存，保存在MemoryInfo对象上
            acmng.getMemoryInfo(memoryInfo);
            long memSize = memoryInfo.availMem;
            return memSize / (1024 * 1024);
        }
        return 0;
    }

    //系统全部内存
    public static double getTotalMem() {
        ActivityManager acmng = (ActivityManager) m_context.getSystemService(m_context.ACTIVITY_SERVICE);
        ActivityManager.MemoryInfo memoryInfo = new ActivityManager.MemoryInfo();
        if (memoryInfo != null) {
            //获得系统可用内存，保存在MemoryInfo对象上
            acmng.getMemoryInfo(memoryInfo);
            long memSize = memoryInfo.totalMem;
            return memSize / (1024 * 1024);
        }
        return 0;
    }

    public static void test() {
        Log.d("getNetState:", String.valueOf(getNetState()));
        Log.d("getBatteryinfo:", String.valueOf(getBatteryinfo()));
        Log.d("imei:", getIMEI());
        Log.d("getIpAddress:", getIpAddress());
        Log.d("getMacAddress:", getMacAddress());
        Log.d("getDeviceName:", getDeviceName());
        Log.d("getSystemVersion:", getSystemVersion());
        Log.d("getPackageName:", getPackageName());
        Log.d("getAppName:", getAppName());
        Log.d("getVersionName:", getVersionName());
        Log.d("getCountry:", getCountry());
        Log.d("getAvailMem:", String.valueOf(getAvailMem()));
        Log.d("getTotalMem:", String.valueOf(getTotalMem()));
        Log.d("getUUID:", getUUID());
        Log.d("getSDCardDocPath:", getSDCardDocPath());
        startVibrator(5000);
    }

    /** 获取IMEI **/
    @SuppressLint("HardwareIds")
    private static String getIMEI() {
        String imeiStr = "";
        try {
            if (!checkPermission(Manifest.permission.READ_PHONE_STATE)) {
                String[] permissions = new String[]{Manifest.permission.READ_PHONE_STATE};
                String msg = "需要读取手机ID";
                sdk.reqPermission(103, permissions, msg);
            }
            imeiStr = ((TelephonyManager) m_context.getSystemService(Context.TELEPHONY_SERVICE)).getDeviceId();
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (imeiStr == null || imeiStr.length() == 0) {
            imeiStr = getMacAddress();
        }

        if (imeiStr == null || imeiStr.length() == 0) {
            Random r = new Random(System.currentTimeMillis());
            StringBuffer sb = new StringBuffer();
            for (int i = 0; i < 16; i++) {
                int b = r.nextInt(9);
                sb.append(b);
            }
            imeiStr = sb.toString();
        }
        return imeiStr;
    }

    @SuppressLint("HardwareIds")
    @SuppressWarnings("deprecation")
    private static String getMachineID() {
        TelephonyManager tm = (TelephonyManager) m_context.getSystemService(m_context.TELEPHONY_SERVICE);
        final String tmDevice, tmSerial, androidId;
        if (!checkPermission(Manifest.permission.READ_PHONE_STATE)) {
            String[] permissions = new String[]{Manifest.permission.READ_PHONE_STATE};
            sdk.reqPermission(103, permissions, null);
        }
        assert tm != null;
        tmDevice = "" + tm.getDeviceId();
        tmSerial = "" + tm.getSimSerialNumber();
        androidId = "" + android.provider.Settings.Secure.getString(m_context.getContentResolver(),
                android.provider.Settings.Secure.ANDROID_ID);
        UUID deviceUuid = new UUID(androidId.hashCode(), ((long) tmDevice.hashCode() << 32) | tmSerial.hashCode());
        return deviceUuid.toString();
    }
    // 获取uuid
    public static String getUUID() {
        if (null == m_context) {
            return "null_" + System.currentTimeMillis();
        }

        int imei = 0;
        int machineID = 0;
        int macAddress = 0;
        String deviceUuid = "";

        try {
            String sImeiString = getIMEI();
            if (sImeiString != null) {
                imei = sImeiString.hashCode();
            }
        } catch (Exception e) {
        }

        try {
            String sMachineIdString = getMachineID();
            if (sMachineIdString != null) {
                machineID = sMachineIdString.hashCode();
            }
        } catch (Exception e) {
        }

        try {
            String sMacAddresString = getMacAddress();
            if (sMacAddresString != null) {
                macAddress = sMacAddresString.hashCode();
            }
        } catch (Exception e) {
        }

        try {
            deviceUuid = new UUID(imei, ((long) machineID << 32) | macAddress).toString();
            if (deviceUuid == null || deviceUuid.equals("")) {
                deviceUuid = UUID.randomUUID().toString();
            }
        } catch (Exception e) {
        }

        if (deviceUuid == null || deviceUuid.trim() == "") {
            deviceUuid = "null_" + System.currentTimeMillis();
        }
        return deviceUuid.replace("-", "");
    }
}

