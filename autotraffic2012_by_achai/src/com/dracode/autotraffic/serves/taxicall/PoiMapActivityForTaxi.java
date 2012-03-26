package com.dracode.autotraffic.serves.taxicall;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import com.dracode.andrdce.ct.UserAppSession;
import com.dracode.autotraffic.R;
import com.dracode.autotraffic.common.map.MyPoiOverlay;
import com.mapabc.mapapi.GeoPoint;
import com.mapabc.mapapi.Geocoder;
import com.mapabc.mapapi.MapActivity;
import com.mapabc.mapapi.MapController;
import com.mapabc.mapapi.MapView;
import com.mapabc.mapapi.PoiItem;
import com.mapabc.mapapi.PoiOverlay;
import com.mapabc.mapapi.PoiPagedResult;
import com.mapabc.mapapi.PoiSearch.SearchBound;

import android.app.ProgressDialog;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;

public class PoiMapActivityForTaxi extends MapActivity {

	protected MapView mMapView;
	protected MapController mMapController;
	protected MyLocationOverlayProxy mLocationOverlay;

	protected ProgressDialog progDialog;
	protected Thread searchingThread = null;
	public static final int FIRST_LOCATION = 1001;
	public static final int SEARCH_POI = 1002;
	public static final int SEARCH_ERROR = 1003;
	public static final int SEARCH_REFRESH = 1004;
	public static final int SEARCH_RECHECK = 1005;
	public static final int LASTPOI_POPUP = 1007;
	
	private int DEFAULT_ZOOM = 16;
	
	protected SearchBound searchBnd;
	protected PoiPagedResult searchResult;
	protected InputStream searchResInput;
	protected PoiOverlay poiOverlay;
	protected boolean searchingInProgress = false;
	
	private Timer idleTimer = null;
	private TimerTask idlTimerTask = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	protected void initMap() {
		mMapView = (MapView) findViewById(R.id.mapView);
		mMapView.setBuiltInZoomControls(true); // 设置启用内置的缩放控件

		mMapController = mMapView.getController(); // 得到mMapView的控制权,可以用它控制和驱劢平移和缩放
		
		if (MapViewInfoForTaxi.lastMapView == null) {
			/*
			 * // 用给定的经纬度构造一个GeoPoint，单位是微度 (度 * 1E6)
			 * GeoPoint point = new GeoPoint((int) (39.982378 * 1E6), (int) (116.304923 * 1E6)); 
			 * mMapController.setCenter(point); // 设置地图中心点
			 */
			mMapController.setZoom(DEFAULT_ZOOM); // 设置地图zoom级别
			findMyLocation(true);
		} else {
			// 用给定的经纬度构造一个GeoPoint，单位是微度 (度 * 1E6)
			GeoPoint point = new GeoPoint(MapViewInfoForTaxi.lastMapView.centerY, MapViewInfoForTaxi.lastMapView.centerX);
			mMapController.setCenter(point); // 设置地图中心点
			mMapController.setZoom(MapViewInfoForTaxi.lastMapView.zoomLevel); // 设置地图zoom级别
			findMyLocation(false);
		}
		// 图层显示
		switchLayer();

		OnTouchListener ls = new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				resetIdleTimer();
				return false;
			}
		};
		mMapView.setOnTouchListener(ls);
	}

	protected void wakeupSearch() {
		// 获取地图显示的POI搜索范围
		searchBnd = new SearchBound(mMapView);

		if (searchingThread == null) {
			searchingThread = new Thread(new Runnable() {
				public void run() {
					while (searchingThread != null) {
						searchingInProgress = true;
						try {
							searchResInput = null;
							Map<String, Object> params = new HashMap<String, Object>();
							double x = (searchBnd.getLowerLeft().getLongitudeE6() + 
										searchBnd.getUpperRight().getLongitudeE6()) / 1E6 / 2;
							double y = (searchBnd.getLowerLeft().getLatitudeE6() + 
										searchBnd.getUpperRight().getLatitudeE6()) / 1E6 / 2;
							params.put("x", Double.toString(x));
							params.put("y", Double.toString(y));
							params.put("d", "3000");
							params.put("mti", "B_TAXI");
							params.put("v", "1.0");
//							Map<String, Object> resMessage = HttpUtil.getStreamData(sp,params, Config.BUS_INFO_URL, null);
//							searchResInput = (InputStream) resMessage.get("stream");
						} catch (Exception e) {
							hideProgDlg();
							e.printStackTrace();
						}

						if (searchResInput == null) {
							handler.sendMessage(Message.obtain(handler, SEARCH_POI));
						} else {
							handler.sendMessage(Message.obtain(handler, SEARCH_ERROR));
						}
						searchingInProgress = false;
						if (searchingThread == null)
							break;
						try {
							synchronized (searchingThread) {
								searchingThread.wait();
							}
						} catch (InterruptedException e) {
						}
					}
				}

			});
			searchingThread.start();
		} else {
			synchronized (searchingThread) {
				searchingThread.notify();
			}
		}	
	}

	protected void hideProgDlg() {
		if (progDialog != null)
			if (progDialog.isShowing())
				progDialog.dismiss();
	}

	/**
	 * 定位
	 * @param animateTo 是否定位
	 */
	protected void findMyLocation(boolean animateTo) {
		if (mLocationOverlay == null) {
			mLocationOverlay = new MyLocationOverlayProxy(this, mMapView);
			mMapView.getOverlays().add(mLocationOverlay);
			mLocationOverlay.enableMyLocation();
			mLocationOverlay.enableCompass();

			// 实现初次定位使定位结果居中显示
			if (animateTo)
				mLocationOverlay.runOnFirstFix(new Runnable() {
					public void run() {
						handler.sendMessage(Message.obtain(handler, FIRST_LOCATION));
					}
				});
		}

		if (animateTo) {
			UserAppSession.showToast(this, "正在定位...");
			handler.sendMessage(Message.obtain(handler, FIRST_LOCATION));
		}

	}

	protected Handler handler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case FIRST_LOCATION:
				GeoPoint point = mLocationOverlay.getMyLocation();
				if (point != null) {
					try {
						Geocoder mGeocoder01 = new Geocoder((MapActivity) PoiMapActivityForTaxi.this);
						int x = point.getLatitudeE6();
						double x1 = ((double) x) / 1000000;
						int y = point.getLongitudeE6();
						double y1 = ((double) y) / 1000000;
						List<Address> lstAddress = null;
						lstAddress = mGeocoder01.getFromLocation(x1, y1, 3);
						
						if (lstAddress.size() != 0) {
							for (int i = 0; i < lstAddress.size(); i++) {
								Address adsLocation = lstAddress.get(i);
								if (adsLocation.getLocality().indexOf("珠海") > -1) {
									mMapController.animateTo(point);
									// mMapController.setCenter(point); // 设置地图中心点
									mMapController.setZoom(DEFAULT_ZOOM);
								} else {
									point = new GeoPoint((int)(22.266667 * 1E6), (int)(113.566667 * 1E6));
									mMapController.setCenter(point);// 设置地图中心点
									mMapController.setZoom(12);// 设置地图zoom级别
								}
								break;
							}
						} else {
							Log.i(this.getClass().getSimpleName(), "Address GeoPoint Not Found.");
						}
					} catch (IOException e) {
						e.printStackTrace();
						UserAppSession.showToast(PoiMapActivityForTaxi.this, "连接错误！");
					}
				} else {
					if ("020".equals(UserAppSession.cur_CityCode)) {
						GeoPoint _point = new GeoPoint((int)(23.128795 * 1E6), (int)(113.258976 * 1E6));
						mMapController.setCenter(_point);// 设置地图中心点
						mMapController.setZoom(10);// 设置地图zoom级别
					} else if ("0756".equals(UserAppSession.cur_CityCode)) {
						GeoPoint _point = new GeoPoint((int)(22.266667 * 1E6), (int)(113.566667 * 1E6));
						mMapController.setCenter(_point);// 设置地图中心点
						mMapController.setZoom(12);// 设置地图zoom级别
					}
				}
				break;
			case SEARCH_REFRESH:
				wakeupSearch();
				break;
			case SEARCH_RECHECK:
				checkResearch();
				break;
			case SEARCH_ERROR:
				hideProgDlg();
				UserAppSession.showToast(PoiMapActivityForTaxi.this, "请求失败,请检查网络连接！");
				break;
			case SEARCH_POI:
				addSearchPois();
				break;
			case LASTPOI_POPUP:
				restoreLastPoiPopup();
				break;
			}
		}
	};

	protected void switchLayer() {
		if (poiOverlay != null)
			poiOverlay.removeFromMap();
	}

	protected void restoreLastPoiPopup() {
		int lastpop = -1;
		PoiItem item = null;
		String lastpopid = null;//MyPoiOverlay.lastPopupItemId;
		if (lastpopid != null) {
			for (int i = 0; i < poiOverlay.size(); i++) {
				item = poiOverlay.getItem(i);
				if (lastpopid.equals(item.getTypeCode() + ":" + item.getPoiId())) {
					lastpop = i;
					break;
				}
			}
		}
		if (lastpop > -1) {
			poiOverlay.showPopupWindow(lastpop);
			mMapController.animateTo(item.getPoint());
			mMapView.invalidate();
		}
	}

	protected void addSearchPois() {
		try {
			String lastpopid = null;//MyPoiOverlay.lastPopupItemId;
			List<PoiItem> poiItems = new ArrayList<PoiItem>();
//			if (searchResInput == null)
//				return;
//			TaxiLine taxil = TaxiLine.parseFrom(searchResInput);
//			List<cn.com.dracode.osp.taxi.TaxiLineProtos.TaxiLine.List> taxis = taxil.getListList();
//			for (cn.com.dracode.osp.taxi.TaxiLineProtos.TaxiLine.List txi : taxis) {
//				String lat = txi.getLatitude();
//				String lon = txi.getLongitude();
//				
//				GeoPoint pt = new GeoPoint((int) (Double.parseDouble(lat) * 1E6),(int) (Double.parseDouble(lon) * 1E6));
//				PoiItem item = new PoiItem(txi.getId(), pt,txi.getDistance(), txi.getPhone());
//				item.setTypeCode("TAXI");
//				poiItems.add(item);
//			}
			for(int i = 0;i<10;i++){
                if(i<5){
                	GeoPoint pt = new GeoPoint((int) (Double.parseDouble("22.383743") * 1E6),(int) (Double.parseDouble("113.544231") * 1E6));
    				PoiItem item = new PoiItem("CG5078", pt,"1081", "13924701902");
    				item.setTypeCode("TAXI");
    				poiItems.add(item);
                } else if(5<=i&&i<7){
                	GeoPoint pt = new GeoPoint((int) (Double.parseDouble("22.376667") * 1E6),(int) (Double.parseDouble("113.568348") * 1E6));
    				PoiItem item = new PoiItem("C72516", pt,"1559", "7110809002");
    				item.setTypeCode("TAXI");
    				poiItems.add(item);
                } else {					
                	GeoPoint pt = new GeoPoint((int) (Double.parseDouble("22.385857") * 1E6),(int) (Double.parseDouble("113.598888") * 1E6));
    				PoiItem item = new PoiItem("CR4716", pt,"4759", "15812783751");
    				item.setTypeCode("TAXI");
    				poiItems.add(item);
                }
			}
			try {
		     	InputStream rin = searchResInput;
				searchResInput = null;
//				rin.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
			if (poiOverlay != null) {
				poiOverlay.removeFromMap();
			}
			if (poiItems != null && poiItems.size() > 0) {
				int res = R.drawable.taxi_poi;
				Drawable drawable = getResources().getDrawable(res);
				// 将结果添加到PoiOverlay
				poiOverlay = new MyPoiOverlay(PoiMapActivityForTaxi.this, drawable, poiItems);
				poiOverlay.addToMap(mMapView); // 将poiOverlay标注在地图上
				mMapView.invalidate();

				//MyPoiOverlay.lastPopupItemId=lastpopid;
				handler.sendMessage(Message.obtain(handler, LASTPOI_POPUP));
			}
			hideProgDlg();
		} catch (Exception e) {
			hideProgDlg();
			UserAppSession.showToast(PoiMapActivityForTaxi.this, "网络连接错误！");
		}

	}

	protected void saveCurMapViewInfo() {
		if (mMapView == null)
			return;
		if (MapViewInfoForTaxi.lastMapView == null)
			MapViewInfoForTaxi.lastMapView = new MapViewInfoForTaxi();
		GeoPoint p = mMapView.getMapCenter();
		MapViewInfoForTaxi.lastMapView.centerX = p.getLongitudeE6();
		MapViewInfoForTaxi.lastMapView.centerY = p.getLatitudeE6();
		MapViewInfoForTaxi.lastMapView.zoomLevel = mMapView.getZoomLevel();
	}

	@Override
	protected void onPause() {
		if (this.mLocationOverlay != null) {
			this.mLocationOverlay.disableMyLocation();
			this.mLocationOverlay.disableCompass();
		}
		killIdleTimer();
		super.onPause();
	}

	@Override
	protected void onStop() {
		saveCurMapViewInfo();
		if (this.mLocationOverlay != null) {
			this.mLocationOverlay.disableMyLocation();
			this.mLocationOverlay.disableCompass();
		}
		if (searchingThread != null) {
			Thread thr = searchingThread;
			searchingThread = null;
			synchronized (thr) {
				thr.notify();
			}
		}
		killIdleTimer();
		super.onStop();
	}

	@Override
	protected void onResume() {
		if (this.mLocationOverlay != null) {
			this.mLocationOverlay.enableMyLocation();
			this.mLocationOverlay.enableCompass();
		}
		super.onResume();
	}

	int lastMapCx = 0, lastMapCy = 0, lastMapSc = 0;

	protected void checkResearch() {
		if (searchingInProgress)
			return;
		int cx, cy, sc;
		GeoPoint p = mMapView.getMapCenter();
		cx = p.getLongitudeE6();
		cy = p.getLatitudeE6();
		sc = mMapView.getZoomLevel();
		if (cx != lastMapCx || cy != lastMapCy || sc != lastMapSc) {
			lastMapCx = cx;
			lastMapCy = cy;
			lastMapSc = sc;
			handler.sendMessage(Message.obtain(handler, SEARCH_REFRESH));
		}
	}

	protected void resetIdleTimer() {
		killIdleTimer();
		idleTimer = new Timer();
		idlTimerTask = new TimerTask() {
			public void run() {
				idleTimer = null;
				handler.sendMessage(Message.obtain(handler, SEARCH_RECHECK));
			}
		};
		idleTimer.schedule(idlTimerTask, 2000);
	}

	protected void killIdleTimer() {
		if (idleTimer != null)
			idleTimer.cancel();
	}

}
