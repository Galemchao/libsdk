package com.gm.baiduloc;

import java.lang.reflect.Method;

import org.cocos2dx.lib.Cocos2dxActivity;

import android.Manifest;
import android.app.Application;
import android.app.Service;

import android.os.Build;
import android.os.Vibrator;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import com.baidu.location.Address;
import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.Poi;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.geocode.GeoCodeResult;
import com.baidu.mapapi.search.geocode.GeoCoder;
import com.baidu.mapapi.search.geocode.OnGetGeoCoderResultListener;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeOption;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeResult;
import com.gm.baiduloc.service.LocationService;
import com.baidu.mapapi.utils.DistanceUtil;

import pub.devrel.easypermissions.EasyPermissions;

public class BaiduLoc {
	//-----------------------------------------------------
	public static Cocos2dxActivity m_context;
	public static LocationService locationService;
	public static Vibrator vibrator;
	public static BDLocationListener locationListener;
	public static OnGetGeoCoderResultListener geoListener;
	public static GeoCoder geoSearch;

	public static BDLocation s_location;
	public static Address s_address;
	public static String s_address_str;
	public static ReverseGeoCodeResult.AddressComponent s_address_com;

	public static BaiduLocListener s_loc_listener;

	public static void nativeNotifyLocation(int error, double longitude,  double latitude, String address,
											String country,String province,String city,String district,String street,String streetnumb,String detail,String describe)
	{
		s_loc_listener.onLocationResult(error,longitude,latitude,address,country,province,city,district,street,streetnumb,detail,describe);
	}
	public static void init(Cocos2dxActivity context, BaiduLocListener listener) {
		m_context = context;
		s_loc_listener = listener;
		initListener();
		geoSearch = GeoCoder.newInstance();
		geoSearch.setOnGetGeoCodeResultListener(geoListener);
	}
	public static void initApplication(Application context) {
		/***
		 * 初始化定位sdk，建议在Application中创建
		 */
		locationService = new LocationService(context);
		vibrator =(Vibrator)context.getSystemService(Service.VIBRATOR_SERVICE);
		SDKInitializer.initialize(context);
	}

	public static void initListener() {
		locationListener = new BDLocationListener() {
			@Override
			public void onReceiveLocation(BDLocation location) {
				// TODO Auto-generated method stub
				if(null == location ||
						location.getLocType() == BDLocation.TypeServerError ||
						location.getLocType() == BDLocation.TypeNetWorkException ||
						location.getLocType() == BDLocation.TypeCriteriaException
						) {
					nativeNotifyLocation(1,0,0,"","","","","","","","","");
				}
				else{

					s_location = location;
					s_address_str = location.getAddrStr();
					s_address = location.getAddress();
					if(s_address_str==null)
					{
						s_address_str="";
					}
					String decribe = location.getLocationDescribe();
					if(decribe == null)
					{
						decribe = "";
					}
					String county = location.getCountry();
					String province = location.getProvince();
					String city = location.getCity();
					String district = location.getDistrict();
					String street = location.getStreet();
					String streetnumb = location.getStreetNumber();
					String detail = "";

					if(county == null) county = "";
					if(province == null) province = "";
					if(city == null) city = "";
					if(district == null) district = "";
					if(street == null) street = "";
					if(streetnumb == null) streetnumb = "";

					nativeNotifyLocation(0,s_location.getLongitude(),s_location.getLatitude(),s_address_str,
							county,province,city,district,street,streetnumb,detail,decribe);

					if(s_address_str.length()==0){
						searchReverse(s_location.getLongitude(),s_location.getLatitude());
					}
					else{
						locationService.stop();
					}
					if(true)
					{
						StringBuffer sb = new StringBuffer(256);
						sb.append("time : ");
						/**
						 * 时间也可以使用systemClock.elapsedRealtime()方法 获取的是自从开机以来，每次回调的时间；
						 * location.getTime() 是指服务端出本次结果的时间，如果位置不发生变化，则时间不变
						 */
						sb.append(location.getTime());
						sb.append("\nlocType : ");// 定位类型
						sb.append(location.getLocType());
						sb.append("\nlocType description : ");// *****对应的定位类型说明*****
						sb.append("\nlatitude : ");// 纬度
						sb.append(location.getLatitude());
						sb.append("\nlontitude : ");// 经度
						sb.append(location.getLongitude());
						sb.append("\nradius : ");// 半径
						sb.append(location.getRadius());
						sb.append("\nCountryCode : ");// 国家码
						sb.append(location.getCountryCode());
						sb.append("\nCountry : ");// 国家名称
						sb.append(location.getCountry());
						sb.append("\ncitycode : ");// 城市编码
						sb.append(location.getCityCode());
						sb.append("\ncity : ");// 城市
						sb.append(location.getCity());
						sb.append("\nDistrict : ");// 区
						sb.append(location.getDistrict());
						sb.append("\nStreet : ");// 街道
						sb.append(location.getStreet());
						sb.append("\naddr : ");// 地址信息
						sb.append(location.getAddrStr());
						sb.append("\nUserIndoorState: ");// *****返回用户室内外判断结果*****
						sb.append("\nDirection(not all devices have value): ");
						sb.append(location.getDirection());// 方向
						sb.append("\nlocationdescribe: ");
						sb.append(location.getLocationDescribe());// 位置语义化信息
						sb.append("\nPoi: ");// POI信息
						if (location.getPoiList() != null && !location.getPoiList().isEmpty()) {
							for (int i = 0; i < location.getPoiList().size(); i++) {
								Poi poi = (Poi) location.getPoiList().get(i);
								sb.append(poi.getName() + ";");
							}
						}
						if (location.getLocType() == BDLocation.TypeGpsLocation) {// GPS定位结果
							sb.append("\nspeed : ");
							sb.append(location.getSpeed());// 速度 单位：km/h
							sb.append("\nsatellite : ");
							sb.append(location.getSatelliteNumber());// 卫星数目
							sb.append("\nheight : ");
							sb.append(location.getAltitude());// 海拔高度 单位：米
							sb.append("\ngps status : ");
							sb.append("\ndescribe : ");
							sb.append("gps定位成功");
						} else if (location.getLocType() == BDLocation.TypeNetWorkLocation) {// 网络定位结果
							// 运营商信息
							if (location.hasAltitude()) {// *****如果有海拔高度*****
								sb.append("\nheight : ");
								sb.append(location.getAltitude());// 单位：米
							}
							sb.append("\noperationers : ");// 运营商信息
							sb.append(location.getOperators());
							sb.append("\ndescribe : ");
							sb.append("网络定位成功");
						} else if (location.getLocType() == BDLocation.TypeOffLineLocation) {// 离线定位结果
							sb.append("\ndescribe : ");
							sb.append("离线定位成功，离线定位结果也是有效的");
						} else if (location.getLocType() == BDLocation.TypeServerError) {
							sb.append("\ndescribe : ");
							sb.append("服务端网络定位失败，可以反馈IMEI号和大体定位时间到loc-bugs@baidu.com，会有人追查原因");
						} else if (location.getLocType() == BDLocation.TypeNetWorkException) {
							sb.append("\ndescribe : ");
							sb.append("网络不同导致定位失败，请检查网络是否通畅");
						} else if (location.getLocType() == BDLocation.TypeCriteriaException) {
							sb.append("\ndescribe : ");
							sb.append("无法获取有效定位依据导致定位失败，一般是由于手机的原因，处于飞行模式下一般会造成这种结果，可以试着重启手机");
						}
						android.util.Log.d("SDK-Location",sb.toString());
					}
				}
			}

			public void onConnectHotSpotMessage(String s, int i) {
			}
		};

		geoListener = new OnGetGeoCoderResultListener() {
			public void onGetGeoCodeResult(GeoCodeResult result) {
				if (result == null || result.error != SearchResult.ERRORNO.NO_ERROR) {
					//Toast.makeText(m_context, "抱歉，未能找到结果", Toast.LENGTH_LONG).show();
					android.util.Log.d("SDK-Location","GeoCode 抱歉，未能找到结果");
					return;
				}
				result.getLocation();
			}

			public void onGetReverseGeoCodeResult(ReverseGeoCodeResult result) {
				if (result == null || result.error != SearchResult.ERRORNO.NO_ERROR) {
					//Toast.makeText(m_context, "抱歉，未能找到结果", Toast.LENGTH_LONG).show();
					android.util.Log.d("SDK-Location","ReverseGeo 抱歉，未能找到结果");
					return;
				}
				s_address_str = result.getAddress();
				s_address_com = result.getAddressDetail();
				//result.getLocation();
				nativeNotifyLocation(0,s_location.getLongitude(),s_location.getLatitude(),s_address_str,"","","","","","","","");
			}
		};
	}

	//-----------------------------------------------------
	public static void start() {
		reqPermission();
		onStart();
	}
	public static void onStart() {
		locationService.registerListener(locationListener);
		//注册监听
		int type = 0;
		if (type == 0) {
			locationService.setLocationOption(locationService.getDefaultLocationClientOption());
		} else if (type == 1) {
			locationService.setLocationOption(locationService.getOption());
		}
		locationService.start();
	}

	public static void stop() {
		// TODO Auto-generated method stub
		locationService.unregisterListener(locationListener); //注销掉监听
		locationService.stop(); //停止定位服务
	}
	public static String getAddress(){
		return s_address_str;
	}
	public static void searchReverse(double longitude,double latitude) {
		LatLng ptCenter = new LatLng(latitude, longitude);
		// 反Geo搜索
		geoSearch.reverseGeoCode(new ReverseGeoCodeOption()
				.location(ptCenter));
	}
	public static double getDistance(double alongitude,double alatitude,double blongitude,double blatitude){
		LatLng a = new LatLng(alatitude,alongitude);
		LatLng b = new LatLng(blatitude,blongitude);
		return DistanceUtil.getDistance(a, b);
	}
	public static String[] needPermission(){
		int sdkInt=android.os.Build.VERSION.SDK_INT;
		if(sdkInt>=23) {
			return new String[]{
					Manifest.permission.READ_PHONE_STATE,
					Manifest.permission.ACCESS_COARSE_LOCATION,
					Manifest.permission.ACCESS_FINE_LOCATION,
					//Manifest.permission.READ_EXTERNAL_STORAGE,
					//Manifest.permission.WRITE_EXTERNAL_STORAGE,
			};
		}
		else{
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
				return ;
			}
			boolean hasPer = EasyPermissions.hasPermissions(m_context, permissions);
			if (hasPer) {
				//已经同意过
				//dosomething
				//sdkcontrol.onPermissionsHave(reqcode, permissions);
				Log.d("BaiduLoc", "requestPermission:------>hasPer: " + hasPer);
			} else {
				//未同意过,或者说是拒绝了，再次申请权限
				EasyPermissions.requestPermissions(
						m_context,  //上下文
						null, //提示文言
						100, //请求码
						permissions //权限列表
				);
				Log.d("BaiduLoc", "requestPermission:------>hasPer: " + hasPer);
			}
		}

//		try {
//			Class cls = m_context.getClass();
//			Method setMethod = cls.getDeclaredMethod("requestPermission",int.class, String[].class, String.class);
//			setMethod.invoke(m_context, 100, permissions, "防作弊功能需要定位权限");
//		} catch (Exception e) {
//			Log.d("BaiduLoc", "reqPermission: " + e);
//		}
	}
}


