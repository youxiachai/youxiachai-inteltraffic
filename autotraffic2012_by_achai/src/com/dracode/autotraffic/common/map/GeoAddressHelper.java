package com.dracode.autotraffic.common.map;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;

import org.apache.http.protocol.HTTP;
import org.json.JSONException;
import org.json.JSONObject;

import com.dracode.andrdce.ct.CtRuntimeCancelException;
import com.dracode.andrdce.ct.CtRuntimeException;
import com.dracode.andrdce.ct.NetUtil;
import com.dracode.andrdce.ct.UserAppSession;
import com.dracode.autotraffic.R;
import com.dracode.autotraffic.common.map.LocationHelper.FixedLocationInfo;
import com.mapabc.mapapi.Geocoder;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.DialogInterface.OnKeyListener;
import android.location.Address;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;

public class GeoAddressHelper {
	public static final String ADDR_NAME_MY_POSITION = "我的位置";
	public static final String ADDR_NAME_POINT_ON_MAP = "地图上的点";

	protected Context theCtx;
	private OnAddressFoundEvent theEvt;
	private double geoLat = 0;
	private double geoLon = 0;
	private String myCityCode;
	private String myAddrShort;
	private String myAddrLong;
	public boolean useMapabcAddr = true;

	private ProgressDialog mProgressDlg = null;

	public static interface OnAddressFoundEvent {
		public void onCanceled(); //取消
		public void onError(String msg);//出错
		public void onAddressFound(String cityCode, String addrShort,
				String addrLong);
	}

	public static void getGeoAddress(Context ctx, double x, double y,
			OnAddressFoundEvent evt) {
		GeoAddressHelper gah = new GeoAddressHelper();
		gah.init(ctx, x, y, evt);
	}

	public void init(Context ctx, double x, double y, OnAddressFoundEvent evt) {
		theCtx = ctx;
		geoLon = x;
		geoLat = y;
		theEvt = evt;

		mProgressDlg = new ProgressDialog(theCtx);
		mProgressDlg.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		mProgressDlg.setIndeterminate(false);
		mProgressDlg.setCancelable(false);
		mProgressDlg.setMessage("正在获取地址...");
		queryCanceled = false;
		OnKeyListener ls = new OnKeyListener() {
			public boolean onKey(DialogInterface dialog, int keyCode,
					KeyEvent event) {
				if (keyCode == KeyEvent.KEYCODE_BACK) {
					queryCanceled = true;
					if (queryingUrl != null) {
						NetUtil.stopNetUrl(queryingUrl);
					}
					if (theEvt != null)
						theEvt.onCanceled();
					return true;
				}
				return false;
			}
		};
		mProgressDlg.setOnKeyListener(ls);
		OnDismissListener dsls = new OnDismissListener() {
			public void onDismiss(DialogInterface dialog) {
				mProgressDlg = null;
			}
		};
		mProgressDlg.setOnDismissListener(dsls);
		mProgressDlg.show();

		if (useMapabcAddr)
			doGetAddrFromMapabc();
		else
			doGetAddrFromBaServer();
	}

	/*
	 * 以下为调用我们的服务接口
	 */

	public void doGetAddrFromBaServer() {
		myAddrShort = null;
		myAddrLong = null;

		GeoAddressTask tsk = new GeoAddressTask();
		tsk.execute();
	}

	private boolean queryCanceled = false;
	private String queryingUrl = null;

	class GeoAddressTask extends AsyncTask<String, Integer, Object> {
		@Override
		protected Object doInBackground(String... params) {
			try {

				queryCanceled = false;
				String city = UserAppSession.cur_CityCode;
				String url = getDataUrl("POI_GEO_ADDRESS");
				try {
					url = url + "&city=" + URLEncoder.encode(city, HTTP.UTF_8);
					url = url + "&x=" + Double.toString(geoLon);
					url = url + "&y=" + Double.toString(geoLat);
				} catch (UnsupportedEncodingException e) {
				}
				String enckey = NetUtil.getEncryptKey(url);
				url = url + "&verifyCode=" + enckey;

				queryingUrl = url;
				JSONObject rJson = UserAppSession.cursession().getUrlJsonData(
						url, null, UserAppSession.CAHCE_EXPIRE_TIME_DAY);

				if (queryCanceled)
					throw new CtRuntimeCancelException("操作中止");

				if (rJson == null)
					throw new CtRuntimeException("未查询到任何信息");

				return rJson;
			} catch (Throwable ex) {
				return ex;
			}
		}

		@Override
		protected void onPostExecute(Object result) {
			if (mProgressDlg != null)
				mProgressDlg.dismiss();

			doGeoAddrFound(result);
		}
	}

	protected void doGeoAddrFound(Object res) {
		if (queryCanceled || UserAppSession.isTaskCancelMessage(res)) {
			if (theEvt != null)
				theEvt.onCanceled();
			else
				UserAppSession.showToast(theCtx, "操作被中止");
			return;
		}
		if (res instanceof Throwable)
			if (theEvt != null) {
				theEvt.onError("执行出错-" + ((Exception) res).getMessage());
				return;
			}
		if (res instanceof JSONObject) {
			JSONObject data = (JSONObject) res;
			try {
				myCityCode = data.getString("CityCode");
				myAddrShort = data.getString("Address");
				myAddrLong = data.getString("AddressLong");
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}

		if (myAddrShort == null || myAddrShort.length() == 0)
			myAddrShort = GeoAddressHelper.ADDR_NAME_POINT_ON_MAP;
		if (myAddrLong == null || myAddrLong.length() == 0)
			myAddrLong = GeoAddressHelper.ADDR_NAME_POINT_ON_MAP;
		if (theEvt != null)
			theEvt.onAddressFound(myCityCode, myAddrShort, myAddrLong);
	}

	public static boolean nameIsGeoAddress(String name) {
		if (name != null)
			if (name.endsWith("附近") || nameIsSpecAddress(name))
				return true;
		return false;
	}

	public static boolean nameIsSpecAddress(String name) {
		if (name != null)
			if (name.equals(ADDR_NAME_MY_POSITION)
					|| name.equals(ADDR_NAME_POINT_ON_MAP))
				return true;
		return false;
	}

	private String getDataUrl(String tp) {
		if (tp.equals("POI_GEO_ADDRESS"))
			return UserAppSession.getBusLineServerUrl() + "&type=7";
		return null;
	}

	/*
	 * 以下为调用高德地址接口
	 */

	protected static final int GEOADDR_RESULT = 1001;
	protected static final int GEOADDR_ERROR = 1009;

	private String errorMsg;
	private Geocoder geoCoder;
	private List<Address> address;

	public void doGetAddrFromMapabc() {
		myAddrShort = null;
		myAddrLong = null;
		geoCoder = new Geocoder(theCtx, theCtx.getResources().getString(
				R.string.maps_api_key));

		Thread t = new Thread(new Runnable() {
			public void run() {
				try {
					address = geoCoder.getFromLocation(geoLat, geoLon, 5);
					handler.sendMessage(Message.obtain(handler, GEOADDR_RESULT));
				} catch (Throwable ex) {
					ex.printStackTrace();
					errorMsg = ex.getMessage();
					if (errorMsg == null)
						errorMsg = "网络请求失败";
					handler.sendMessage(Message.obtain(handler, GEOADDR_ERROR));
				}
			}
		});
		t.start();
	}

	public void doGetGeoAddress() {
		try {
			if (address == null)
				return;

			for (Address addres : address) {
				String type = addres.getPremises();// Address 类型
				String res = "", resLong;
				if (addres.getAdminArea() != null)
					res += addres.getAdminArea();
				if (addres.getLocality() != null) {
					res += addres.getLocality();
					myCityCode = addres.getLocality();
				}
				resLong = res;

				res = "";
				if (addres.getSubLocality() != null)
					res += addres.getSubLocality();
				res += addres.getFeatureName();
				res += "附近";
				myAddrShort = res;
				myAddrLong = resLong + res;
				if (Geocoder.Street_Road.equals(type)
						|| isGoodAddr(addres.getFeatureName()))
					break;
			}
		} catch (Throwable ex) {
			ex.printStackTrace();
		}
	}

	private static boolean isGoodAddr(String addr) {
		if (addr != null)
			if (addr.endsWith("大厦") || addr.endsWith("酒店")
					|| addr.endsWith("小区") || addr.endsWith("广场")
					|| addr.endsWith("中心") || addr.endsWith("医院"))
				return true;
		return false;
	}

	private Handler handler = new Handler() {

		public void handleMessage(Message msg) {
			if (msg.what == GEOADDR_RESULT) {
				if (address != null)
					doGetGeoAddress();
				if (mProgressDlg != null)
					mProgressDlg.dismiss();
				doGeoAddrFound();
			} else if (msg.what == GEOADDR_ERROR) {
				if (mProgressDlg != null)
					mProgressDlg.dismiss();
				if (theEvt != null)
					theEvt.onError("获取地址出错: " + errorMsg);
				else
					doGeoAddrFound();
			}
		}
	};

	protected void doGeoAddrFound() {
		if (myAddrShort == null || myAddrShort.length() == 0)
			myAddrShort = GeoAddressHelper.ADDR_NAME_POINT_ON_MAP;
		if (myAddrLong == null || myAddrLong.length() == 0)
			myAddrLong = GeoAddressHelper.ADDR_NAME_POINT_ON_MAP;
		if (theEvt != null)
			theEvt.onAddressFound(myCityCode, myAddrShort, myAddrLong);
	}

	public static String getGeoAddress(Context ctx, double x, double y,
			int typeCode) throws Exception {
		// typeCode: 0,1短地址名 2城市名 3长地址名
		try {
			FixedLocationInfo fli = LocationHelper.getRelateFixedLocaton(x, y);
			if (fli != null) {
				if (typeCode == 2)
					return fli.city;
				else
					return fli.name;
			}
			Geocoder coder = new Geocoder(ctx, ctx.getResources().getString(
					R.string.maps_api_key));
			List<Address> address = coder.getFromLocation(y, x, 5);
			if (address == null || address.size() == 0)
				return null;

			String defRes = null;
			for (Address addres : address) {
				String type = addres.getPremises();// Address 类型
				String res;
				if (typeCode == 2) {
					res = addres.getLocality();
					if (res != null && res.length() > 0)
						return res;
				}
				res = "";
				if (typeCode == 3) {
					if (addres.getAdminArea() != null)
						res += addres.getAdminArea();
					if (addres.getLocality() != null)
						res += addres.getLocality();
				}
				if (addres.getSubLocality() != null)
					res += addres.getSubLocality();
				res += addres.getFeatureName() + "附近";
				if (defRes == null)
					defRes = res;
				if (Geocoder.Street_Road.equals(type)
						|| isGoodAddr(addres.getFeatureName())) {
					return res;
				}
			}
			return defRes;
		} catch (Throwable ex) {
			ex.printStackTrace();
			throw new Exception(ex.getMessage());
		}
	}

}
