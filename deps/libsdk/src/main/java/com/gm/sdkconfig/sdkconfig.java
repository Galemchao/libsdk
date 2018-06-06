package com.gm.sdkconfig;

public class sdkconfig {
    public static String UTILS_TAG = "com.gm.utils.Utils";
    public static String SDK_TAG = "com.gm.sdk.sdk";

    public static String DEFAULT_VERSION = "1.0.0";
    public static String DEFAULT_CHANNEL= "android";
    //------------------------------------
    //sdk类型         微信 qq um 等
    //------------------------------------
    public static String SDK_TYPE = "type";
    public static int SDK_TYPE_NORMAL = 0;
    public static int SDK_TYPE_WX = 1;
    public static int SDK_TYPE_WXCIRCLE = 2;
    public static int SDK_TYPE_QQ = 3;


    //-------------------回调事件----------------------------------
    public static String SDK_ERROR = "error";               //0 成功  1 失败
    public static String SDK_ERROR_MSG = "errMsg";          //错误信息
    public static String SDK_EVT = "evt";
    public static String SDK_EVT_NORMAL = "normal";         //默认事件
    public static String SDK_EVT_TOAST = "toast";
    public static String SDK_EVT_LOGIN = "login";           //登录
    public static String SDK_EVT_SHARE = "share";           //分享
    public static String SDK_EVT_INIT = "init";
    public static String SDK_OPENID = "openid";
    public static String SDK_EVT_PICKIMG = "pickimg";        //从相册裁剪图片

    public static String SDK_NAME = "name";
    public static String SDK_ICONURL = "iconurl";
    public static String SDK_GENDER = "gender";
    public static String SDK_ACCESS_TOKEN = "accessToken";
    public static String SDK_REFRESH_TOKEN = "refreshToken";

    public static String SDK_FILENAME = "filename";

    //----wx-pay----
    public static String SDK_EVT_WXPAY = "wxpay";
    public static String SDK_PRICE = "price";

    // --- 录音 ----
    public static String SDK_EVT_RECORD = "record";
    public static String SDK_RECORD_STATE = "state";
    public static String SDK_RECORD_DURATION = "duration";

    // --- 定位 ----
    public static String SDK_EVT_LOCATION = "locate";
    public static String SDK_LOCATION_LONGITUDE = "longitude";
    public static String SDK_LOCATION_LATITUDE = "latitude";
    public static String SDK_LOCATION_ADDRESS = "address";
    public static String SDK_LOCATION_ADDRESS_DESCRIBE = "discribe";
    public static String SDK_LOCATION_ADDRESS_COUNTY = "county";
    public static String SDK_LOCATION_ADDRESS_PROVINCE = "province";
    public static String SDK_LOCATION_ADDRESS_CITY = "city";
    public static String SDK_LOCATION_ADDRESS_DISTRICT = "district";
    public static String SDK_LOCATION_ADDRESS_STREET = "street";
    public static String SDK_LOCATION_ADDRESS_STREETNUMBER = "streetnumber";
    public static String SDK_LOCATION_ADDRESS_DETAIL = "detail";

    // --- 保存图片 ----
    public static String SDK_EVT_SAVE_IMAGE = "saveimage";
    //-----------------------------------------------------
    // --- config ---
    public static String TOKEN_UM_APPKEY = "umappkey";
    public static String TOKEN_WX_APPKEY = "wxappkey";
    public static String TOKEN_WX_APPSECRET = "wxappsecret";
    public static String TOKEN_RECORD_DURATION_LIMIT = "record_duration_limit";

    //reqcode
    public static int BDlOCATE_REQCODE = 100;       //百度授权reqcode
    public static int UM_REQCODE = 101;       //友盟授权reqcode
    public static int RECORD_REQCODE = 102;       //录音授权reqcode
    public static int SYS_REQCODE = 103;       //获取手机信息reqcode

}