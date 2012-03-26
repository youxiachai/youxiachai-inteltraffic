package com.dracode.andrdce.ct;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.protocol.HTTP;
import org.json.JSONObject;

import com.dracode.autotraffic.common.map.MapViewInfo;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.Toast;

public class UserAppSession extends Application {
	
	public static final String PARAM_NAME_MODIFY_SESSION_DATA = "ModifySession_Data";
	public static final long CAHCE_EXPIRE_TIME_IMMEDIA = 0;
	public static final long CAHCE_EXPIRE_TIME_SOON = 1000L * 30;
	public static final long CAHCE_EXPIRE_TIME_SHORT = 1000L * 60 * 5;
	public static final long CAHCE_EXPIRE_TIME_NORMAL = 1000L * 60 * 30;
	public static final long CAHCE_EXPIRE_TIME_LONG = 1000L * 60 * 60 * 2;
	public static final long CAHCE_EXPIRE_TIME_DAY = 1000L * 60 * 60 * 24;
	public static final long CAHCE_EXPIRE_TIME_WEEK = 1000L * 60 * 60 * 24 * 7;
	public static final long CAHCE_EXPIRE_TIME_MONTH = 1000L * 60 * 60 * 24 * 30;
	public static final long CAHCE_EXPIRE_TIME_YEAR = 1000L * 60 * 60 * 24 * 365;

	public static String serverAddr = "wap.icarcard.com.cn";// "wap.icarcard.com.cn";//
	// "192.0.0.11:8080/cabp";//

	public static String baServerAddr_icc = "wap.icarcard.com.cn/ba_traffic";
	public static String baServerAddr_test = "192.0.0.11:8080/ba_traffic";//"192.168.1.110:8080/ba_traffic";//
	public static String baServerAddr_gzjw = "120.197.4.34:10080/ba_traffic";//
	public static String rtBusServerAddr = baServerAddr_icc;
	public static String baServerAddr = baServerAddr_icc;
	public String sessionId = "";// 事务ID
	public String clientInfo = null;// (System.Environment.OSVersion.ToString()).Replace(' ',
									// '_');

	public static String networkType = "cmnet";
	public static int timeoutConnection = 1000*20;
	public static int timeoutSocket = 1000*90;
	protected boolean appExited = false;
	protected boolean appStarted = false;

	public int lastReqTick = 0;
	public int loginTick = 0;
	public static String appVer = "ANDRDCE_V1.01";
	public static String appCurrentVer = "ANDRDCE_V1.01_BUILD20120209";// 版本信息
	public static String appTitle = "Android DCE";

	public int userID = 123; // 用户ID
	public String userName = "wae";// 用户名
	public String realName = "wae";// 真实姓名
	public String passWord = "";// 密码
	public String roleNames = ""; // 角色
	public int orgId = 1, deptId = 1; // 单位、部门ID
	public String orgName = "icc", deptName = "icc", deptFullName = "ic";// 单位部门名称

	protected boolean loggedIn = false;
	protected Map<String, Object> userProps = new HashMap<String, Object>();

	/** 当前城市. */
	public static String cur_CityName = "";
	public static String cur_CityCode = "";
	public static String cur_CityGeoInfo="";
	
	/** 定位城市.*/
    public static String location_city = "";
    
    /** 最新版本.*/
    public static String newest_version= "";
	public boolean isLogin;
	CacheUtil cacheUtil = null;
	private SharedPreferences app_params;
	public AutoUpdateUtil curUpdater=null;
	
	public static boolean isNeedToHome = true;
	protected static UserAppSession f_curapp=null;
	protected Context mainActivity=null;
	private static UserAppSession cursession(Context ctx) {
		if(ctx!=null)
		 if(f_curapp==null)
			f_curapp=(UserAppSession)ctx.getApplicationContext();
		if(f_curapp==null)
			throw new RuntimeException("cur app context未初始化");
		return f_curapp;
	}

	public static UserAppSession cursession() {;
		return cursession(null);
	}

	@Override
	public void onCreate() {
		super.onCreate();
		LogD("app create");
		this.initApp();
	}

	@Override
	public void onLowMemory() {
		LogD("mem low");
		super.onLowMemory();
	}

	@Override
	public void onTerminate() {
		LogD("app term");
		doAppExit();
		super.onTerminate();
	}

	public void onAppStart() {
		LogD("app on start...");
		if(appExited)
			initApp();
		appExited = false;
		appStarted = false;
	}

	public void initApp() {
		LogD("app init...");
		f_curapp=this;
		NetUtil.resetNetLogs();
		checkNetworkType();
		DceJni.appContext = this;
		app_params = null;

		isNeedToHome = false;
		SharedPreferences city_Pro = getSharedPreferences("CITY_INFO", Context.MODE_WORLD_WRITEABLE | Context.MODE_WORLD_READABLE);
		UserAppSession.cur_CityName = city_Pro.getString("cur_city", "广州");
		UserAppSession.cur_CityCode = city_Pro.getString("cur_code", "020");
		UserAppSession.cur_CityGeoInfo = city_Pro.getString("cur_cityGeoInfo", "113298851,23120136,10");
		MapViewInfo.lastMapView = null;
		
	}
	
	public void setMainAct(Context mainAct){
		LogD("app started...");
		appStarted = true;
		mainActivity=mainAct;
	}
	
	public Context getMainAct(){
		return mainActivity;
	}

	public void checkNetworkType() {
		try {
			networkType = NetUtil.checkNetworkType(this);
		} catch (Throwable ex) {
			networkType = "";
		}
	}

	public static boolean isAppStarted() {
		if(f_curapp==null)
			return false;
		return f_curapp.appStarted;
	}

	public static boolean isAppExited() {
		if(f_curapp==null)
			return true;
		return f_curapp.appExited;
	}

	public void doAppExit() {
		LogD("app exit..");
		appExited = true;
		NetUtil.releaseNetObjs();
		if (cacheUtil != null) {
			cacheUtil.close();
			cacheUtil = null;
		}
		app_params = null;
	}

	protected static void LogE(String msg) {
		Log.e(appTitle, msg);
	}

	public static void LogD(String msg) {
		Log.d(appTitle, msg);
	}

	public static String getServerUrl() {
		return "http://" + serverAddr + "/";
	}

	public static String getBusLineServerUrl() {
		return "http://"
				+ rtBusServerAddr
				+ "/ct/utf8cv.nx?cvId=710001&dataType=json";
	}

	public static String getBaServerUrl() {
		return "http://"
				+ baServerAddr
				+ "/";
	}

	public static String getBaServerCvUrl() {
		return "http://"
				+ baServerAddr
				+ "/ct/utf8cv.nx?dataType=json";
	}

	public static String getBaServerCvtUrl() {
		return "http://"
				+ baServerAddr
				+ "/ct/cvt.nx?isWM=1";
	}
	public static String getSystemUrl(String tp) {
		if (tp.equals("CV"))
			return getBaServerCvUrl();
		if (tp.equals("CVT"))
			return getBaServerCvtUrl();
		return null;
	}

	public static void showToast(String message) {
		showToastEx(null, message, false);
	}

	public static void showToastLong(String message) {
		showToastEx(null, message, true);
	}

	public static void showToast(Context context, String message) {
		showToastEx(context, message, false);
	}

	public static void showToastLong(Context context, String message) {
		showToastEx(context, message, true);
	}

	public static void showToastEx(Context context, String message,
			boolean isLong) {
		if (context == null)
			context = cursession(null);
		if(context==null)
			return;
		Toast.makeText(context, message,
				(isLong ? Toast.LENGTH_LONG : Toast.LENGTH_SHORT)).show();
	}

	// 提交URL
	public String getUrlData(String url) {
		return getUrlData(url, null);
	}

	// 提交URL返回字符串的统一接口
	public String getUrlData(String url, Map<String, Object> params) {
		return NetUtil.getUrlData(url, params, this);
	}

	// 提交URL返回JSON对象的统一接口
	public JSONObject getUrlJsonData(String url, Map<String, Object> params,
			long expireTm) {
		return NetUtil.getUrlJsonData(url, params, this, expireTm);
	}

	public JSONObject getCvtList(int cvtId, String param) {
		if (param == null)
			param = "";
		else if (param.length() > 0 && !param.startsWith("&"))
			param = "&" + param;
		String url = getSystemUrl("CVT") + "&cvtid=" + Integer.toString(cvtId)
				+ param;
		return NetUtil.getUrlJsonData(url, null, this, CAHCE_EXPIRE_TIME_SOON);
	}

	public JSONObject getCvData(int cvId, String param) {
		if (param == null)
			param = "";
		else if (param.length() > 0 && !param.startsWith("&"))
			param = "&" + param;
		String url = getSystemUrl("CV") + "&cvId=" + Integer.toString(cvId)
				+ param;
		return NetUtil.getUrlJsonData(url, null, this, CAHCE_EXPIRE_TIME_SOON);
	}

	public CacheUtil getCacheUtil() {
		if (cacheUtil == null)
			cacheUtil = new CacheUtil(this);
		return cacheUtil;
	}

	public static String urlEncode(String v) {
		try {
			if(v==null)
				return "";
			v = URLEncoder.encode(v, HTTP.UTF_8);
		} catch (UnsupportedEncodingException e) {
		}
		return v;
	}

	public static String urlDecode(String v) {
		try {
			v = URLDecoder.decode(v, HTTP.UTF_8);
		} catch (UnsupportedEncodingException e) {
		}
		return v;
	}

	public Object executeCacheUrl(String url, Map<String, Object> fieldMap) {
		return getCacheUtil().executeCacheUrl(url, fieldMap);
	}

	public Object executeCacheUrl(String url) {
		return getCacheUtil().executeCacheUrl(url);
	}

	public boolean isCacheTableInited(String table) {
		return getCacheUtil().isTableInited(table);
	}

	public void putToCache(String table, String tp,String key, Object value, long expireTm) {
		getCacheUtil().putToCache(table, tp, key, value, expireTm);
	}

	public Object getFromCache(String table,  String key) {
		return getCacheUtil().getFromCache(table, key);
	}

	public void removeFromCache(String table, String tp, String key) {
		getCacheUtil().removeFromCache(table, tp, key);
	}

	public boolean sendSms(String toUser, String data) {
		return false;
	}

	public static boolean isTaskCancelMessage(Object res) {
		if (res == null || (res instanceof CtRuntimeCancelException)
				|| (res instanceof InterruptedException))
			return true;
		else
			return false;
	}

	public static String getCurTimeStr() {
		return TypeUtil.DateTimeConverter.DateTimeToStr(new Date());
	}

	public String getAppParamValue(String name, String defValue) {
		if (app_params == null)
			app_params = getSharedPreferences("APP_PARAM",
					Context.MODE_WORLD_WRITEABLE | Context.MODE_WORLD_READABLE);
		return app_params.getString(name, defValue);
	}

	public void setAppParamValue(String name, String value) {
		if (app_params == null)
			app_params = getSharedPreferences("APP_PARAM",
					Context.MODE_WORLD_WRITEABLE | Context.MODE_WORLD_READABLE);
		app_params.edit().putString(name, value).commit();
	}
}
