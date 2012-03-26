package com.dracode.autotraffic.common.map;

import java.net.ConnectException;
import java.util.ArrayList;
import java.util.List;

import com.dracode.autotraffic.R;
import com.dracode.autotraffic.common.map.GeoAddressHelper.OnAddressFoundEvent;
import com.mapabc.mapapi.GeoPoint;
import com.mapabc.mapapi.LocationManagerProxy;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.DialogInterface.OnKeyListener;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;

/**
 * 获取定位信息
 */
public class LocationHelper implements LocationListener {

	protected static final int GEOCODER_RESULT = 3000;
	public static final int ERROR = 1001;

	protected Context theCtx;

	private LocationManagerProxy locationManager = null;
	private LocationListenerProxy mLocationListener = null;
	private static final long mLocationUpdateMinTime = 5000;
	private static final float mLocationUpdateMinDistance = 30.0f;

	private OnLocationFoundEvent theEvt;
	protected GeoPoint myPoint;
	private String myAddr;
	private double geoLon = 0;
	private double geoLat = 0;
	public int convertAddress = 0; // 0无 1短地址名 2城市名 3长地址名
	public boolean useMapabcAddr = true;
	public boolean fireEventOnError = false;
	public boolean locCanceled = false;

	private ProgressDialog mProgressDlg = null;

	public static interface OnLocationFoundEvent {
		public void onCanceled(); //取消
		public void onError(String msg);//出错
		public void onLocationFound(double lon, double lat, String addr);
	}

	public static void getMyLocation(Context ctx, OnLocationFoundEvent evt) {
		LocationHelper lh = new LocationHelper();
		if (evt != null)
			lh.convertAddress = 1;
		lh.init(ctx, evt);
	}

	public void init(Context ctx, OnLocationFoundEvent evt) {
		theCtx = ctx;
		theEvt = evt;

		myAddr = null;

		if (theEvt != null) {
			mProgressDlg = new ProgressDialog(theCtx);
			mProgressDlg.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			mProgressDlg.setIndeterminate(false);
			mProgressDlg.setCancelable(false);
			mProgressDlg.setMessage("正在获取位置...");
			OnKeyListener ls = new OnKeyListener() {
				public boolean onKey(DialogInterface dialog, int keyCode,
						KeyEvent event) {
					if (keyCode == KeyEvent.KEYCODE_BACK) {
						locCanceled = true;
						mProgressDlg.dismiss();
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
					disableMyLocation();
				}
			};
			mProgressDlg.setOnDismissListener(dsls);
			mProgressDlg.show();
		}

		locationManager = new LocationManagerProxy(theCtx, theCtx
				.getResources().getString(R.string.maps_api_key));
		enableMyLocation();
	}

	public boolean enableMyLocation() {
		boolean result = true;
		if (mLocationListener == null) {
			mLocationListener = new LocationListenerProxy(locationManager);
			result = mLocationListener.startListening(this,
					mLocationUpdateMinTime, mLocationUpdateMinDistance);
		}
		return result;
	}

	public void disableMyLocation() {
		if (mLocationListener != null) {
			mLocationListener.stopListening();
		}
		mLocationListener = null;
	}

	@Override
	public void onLocationChanged(Location location) {
		if (locCanceled)
			return;
		if (location != null) {
			geoLat = location.getLatitude();
			geoLon = location.getLongitude();
			final String provider = location.getProvider();
			disableMyLocation();
			Thread t = new Thread(new Runnable() {
				public void run() {
					if (locCanceled)
						return;
					// 只有GPS获取的经纬度需要纠偏，基站和WIFI获取的经纬度我们已纠偏过，用户无需再次纠偏
					if (provider.equals(LocationManagerProxy.GPS_PROVIDER)) {
						try { // 纠偏构造方法
							myPoint = new GeoPoint(geoLat, geoLon, theCtx,
									theCtx.getResources().getString(
											R.string.maps_api_key));
							if (myPoint != null) {
								geoLon = myPoint.getLongitudeE6() / 1E6;
								geoLat = myPoint.getLatitudeE6() / 1E6;
							}
						} catch (ConnectException e) {
							handler.sendMessage(Message.obtain(handler, ERROR));
						}
					}

					FixedLocationInfo fli = getRelateFixedLocaton(geoLon,
							geoLat);
					if (fli != null) {
						geoLon = fli.lon;
						geoLat = fli.lat;
						if (convertAddress > 0) {
							if (convertAddress == 2)
								myAddr = fli.city;
							else
								myAddr = fli.name;
							convertAddress = 0;
						}
					}

					if (convertAddress > 0 && useMapabcAddr) {
						try {
							myAddr = GeoAddressHelper.getGeoAddress(theCtx, geoLon,
									geoLat, convertAddress);
						} catch (Exception e) {
							e.printStackTrace();
							handler.sendMessage(Message.obtain(handler, ERROR));
						}
					}
					handler.sendMessage(Message
							.obtain(handler, GEOCODER_RESULT));
				}
			});
			t.start();
		}
	}

	private Handler handler = new Handler() {

		public void handleMessage(Message msg) {
			if (msg.what == GEOCODER_RESULT) {
				if (mProgressDlg != null)
					mProgressDlg.dismiss();
				if (locCanceled)
					return;

				if (convertAddress > 0 && !useMapabcAddr) {
					OnAddressFoundEvent evt = new OnAddressFoundEvent() {
						@Override
						public void onAddressFound(String cityCode,
								String addrShort, String addrLong) {
							if (locCanceled)
								return;
							myAddr = addrShort;
							if (myAddr != null
									&& myAddr
											.equals(GeoAddressHelper.ADDR_NAME_POINT_ON_MAP))
								myAddr = GeoAddressHelper.ADDR_NAME_MY_POSITION;
							doLocationFound(geoLon, geoLat, myAddr);
						}

						@Override
						public void onCanceled() {
							if(theEvt!=null)
								theEvt.onCanceled();
						}

						@Override
						public void onError(String msg) {
							if(theEvt!=null)
								theEvt.onError(msg);
						}

					};
					GeoAddressHelper.getGeoAddress(theCtx, geoLon, geoLat, evt);
				} else {
					doLocationFound(geoLon, geoLat, myAddr);
				}
			} else if (msg.what == ERROR) {
				if (mProgressDlg != null)
					mProgressDlg.dismiss();
				if (theEvt != null)
					theEvt.onError("获取位置失败，请检查网络连接！");
			}
		}
	};

	private void doLocationFound(double lon, double lat, String addr) {
		if (theEvt != null && !locCanceled) {
			if (addr == null && convertAddress != 2)
				addr = GeoAddressHelper.ADDR_NAME_MY_POSITION;
			theEvt.onLocationFound(lon, lat, addr);
		}
	}

	@Override
	public void onProviderDisabled(String provider) {
	}

	@Override
	public void onProviderEnabled(String provider) {
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
	}

	public static List<FixedLocationInfo> fixedLocations = null;

	public static void initFixedLocations() {
		if (fixedLocations != null)
			return;
		fixedLocations = new ArrayList<FixedLocationInfo>();
		fixedLocations.add(new FixedLocationInfo("广州市", "越秀区广州喜尔宾酒店",
				113.27810143094, 23.122405421655));
		fixedLocations.add(new FixedLocationInfo("广州市", "天河区金海大厦",
				113.3376637226, 23.141092155928));
		fixedLocations.add(new FixedLocationInfo("广州市", "天河区广东全球通大厦",
				113.3221599894, 23.12146805646));
		fixedLocations.add(new FixedLocationInfo("广州市", "海珠区锦安苑小区",
				113.3215168243, 23.099798296023));
	}

	public static FixedLocationInfo getRelateFixedLocaton(double x, double y) {
		if (fixedLocations == null)
			initFixedLocations();
		for (FixedLocationInfo fli : fixedLocations) {
			if (fli.isNearLocation(x, y))
				return fli;
		}
		return null;
	}

	public static class FixedLocationInfo {
		private static final double FIX_LOC_TOL = 0.0075;
		String city;
		String name;
		double lon;
		double lat;

		public FixedLocationInfo(String ct, String n, double x, double y) {
			city = ct;
			name = n + "附近";
			lon = x;
			lat = y;
		}

		public boolean isNearLocation(double x, double y) {
			if (Math.abs(x - lon) <= FIX_LOC_TOL
					&& Math.abs(y - lat) < FIX_LOC_TOL)
				return true;
			else
				return false;
		}
	}

}
