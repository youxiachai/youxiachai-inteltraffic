package com.dracode.autotraffic.common.map;

import java.util.List;

import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import com.dracode.andrdce.ct.UserAppSession;
import com.dracode.autotraffic.R;
import com.dracode.autotraffic.common.helpers.BaseActivityHelper;
import com.dracode.autotraffic.common.map.MapFastJumpHelper.OnFastJumpEvent;
import com.dracode.autotraffic.common.map.MyLocationOverlayHelper.OnLocationFoundEvent;
import com.mapabc.mapapi.ClsServerUrlSetting;
import com.mapabc.mapapi.GeoPoint;
import com.mapabc.mapapi.MapActivity;
import com.mapabc.mapapi.MapController;
import com.mapabc.mapapi.MapView;
import com.mapabc.mapapi.PoiItem;

public class MapActivityHelper extends BaseActivityHelper {

	public MapActivity mapAct;
	public MapView mMapView;
	public MapController mMapController;
	public MyLocationOverlayHelper mLocationOverlayHelper;
	public MapLayersHelper mLayerHelper;
	public MapFastJumpHelper mFastJumpHelper;
	public MapPoiSearchHelper mPoiSearchHelper;
	public MyPoiOverlay extraPoiOverlay;
	public boolean isRoadMapNeeded = false;

	@Override
	public void init(Activity act) {
		super.init(act);
		mapAct = (MapActivity) act;

		mMapView = (MapView) mapAct.findViewById(R.id.mapView);
	}

	public void initForRoadConditionMap() {
		if (mLayerHelper == null)
			mLayerHelper = new MapLayersHelper(mapAct, mMapView);
		mLayerHelper.sortBoolean[0] = true;
		mLayerHelper.sortBoolean[1] = true;
		isRoadMapNeeded = true;

		initFastJumpHelper();
	}

	public void initFastJumpHelper() {
		OnFastJumpEvent evt = new OnFastJumpEvent() {
			@Override
			public void onFastJump(int centerX, int centerY, int zoomLevel) {
				gotoViewPoint(centerX, centerY, zoomLevel);
			}
		};
		mFastJumpHelper = new MapFastJumpHelper(mapAct, mMapView, evt);
	}

	public void initPoiSearchHelper() {
		mPoiSearchHelper = new MapPoiSearchHelper(mapAct, mMapView);

		OnTouchListener ls = new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				mPoiSearchHelper.resetIdleTimer();
				return false;
			}
		};
		mMapView.setOnTouchListener(ls);
	}

	/**
	 * 初始化地图
	 */
	private int DEFAULT_ZOOM = 15;
	protected String mapInitCity = null;

	public void initMap() {
		if (mapInitCity != null && mapInitCity.equals(UserAppSession.cur_CityCode)) {
			if (mLocationOverlayHelper != null) {
				mLocationOverlayHelper.enableMyLocation();
				mLocationOverlayHelper.enableCompass();
			}
			return;
		}
		if (!UserAppSession.cur_CityCode.equals(mapInitCity) && isRoadMapNeeded) {
			if ("0756".equals(UserAppSession.cur_CityCode)) {
				// 设置服务器地址 企业及用户部署服务用
				ClsServerUrlSetting clsCustomerServer = new ClsServerUrlSetting();
				clsCustomerServer.strTileUrl = "http://120.197.89.33";// 配置地图服务器
				clsCustomerServer.strPoiSearchUrl = "http://120.197.89.33:8082";// 配置poi搜索服务器
				clsCustomerServer.strTmcTileUrl = "http://120.197.89.33";
				mMapView.setServerUrl(clsCustomerServer);
			} else if ("0756".equals(mapInitCity)) {
				ClsServerUrlSetting clsCustomerServer = new ClsServerUrlSetting();
				clsCustomerServer.strTileUrl = "http://emap0.mapabc.com";// 配置地图服务器
				clsCustomerServer.strPoiSearchUrl = "http://search1.mapabc.com:80";// 配置poi搜索服务器
				clsCustomerServer.strTmcTileUrl = "http://tm.mapabc.com";
				mMapView.setServerUrl(clsCustomerServer);
			}
		}
		mMapView.setBuiltInZoomControls(true); // 设置启用内置的缩放控件
		mMapController = mMapView.getController(); // 得到mMapView的控制权,可以用它控制和驱劢平移和缩放

		if (MapViewInfo.lastMapView != null)
			if (!UserAppSession.cur_CityCode
					.equals(MapViewInfo.getLastMapView().lastCityCode))
				MapViewInfo.lastMapView = null;
		if (MapViewInfo.lastMapView == null
				|| (mapInitCity != null && !mapInitCity
						.equals(UserAppSession.cur_CityCode))) {
			MapViewInfo mv = new MapViewInfo();
			if (mv.fromString(UserAppSession.cur_CityGeoInfo)) {
				if (isBigScreen(mMapView))
					mv.zoomLevel = mv.zoomLevel + 1;
				MapViewInfo.lastMapView = mv;
			}
		}
		if (MapViewInfo.lastMapView == null) {
			mMapController.setZoom(DEFAULT_ZOOM);// 设置地图zoom级别
			findMyLocation(true);
		} else {
			gotoViewPoint(MapViewInfo.lastMapView.centerX,
					MapViewInfo.lastMapView.centerY,
					MapViewInfo.lastMapView.zoomLevel);
			findMyLocation(false);
		}

		if (mapInitCity == null
				|| !mapInitCity.equals(UserAppSession.cur_CityCode)) {
			// 图层显示
			if (mLayerHelper != null)
				mLayerHelper.loadLayers();
			if (mFastJumpHelper != null)
				mFastJumpHelper.reset();
		}

		mapInitCity = UserAppSession.cur_CityCode;
	}

	public static boolean isBigScreen(View v) {
		int w = v.getWidth(), h = v.getHeight();
		int d = w * w + h * h;
		if (d > 1200 * 1200)
			return true;
		else
			return false;
	}

	public void gotoViewPoint(int centerX, int centerY, int zoomLevel) {
		GeoPoint p = new GeoPoint(centerY, centerX);
		mMapController.setCenter(p);// 设置地图中心点
		mMapController.setZoom(zoomLevel);// 设置地图zoom级别
	}

	/**
	 * 定位
	 * 
	 * @param animateTo
	 *            是否定位
	 */
	public void findMyLocation(boolean animateTo) {
		if (mLocationOverlayHelper == null) {
			OnLocationFoundEvent evt = new OnLocationFoundEvent() {
				@Override
				public void onLocationFound(int centerX, int centerY) {
					GeoPoint p = new GeoPoint(centerY, centerX);
					mMapController.animateTo(p);
					mMapController.setZoom(DEFAULT_ZOOM);
					MapViewInfo.getLastMapView().myLatitudeE6 = centerY;
					MapViewInfo.getLastMapView().myLongitudeE6 = centerX;
				}
			};
			mLocationOverlayHelper = new MyLocationOverlayHelper(mapAct,
					mMapView, evt);
			mMapView.getOverlays().add(mLocationOverlayHelper);
			mLocationOverlayHelper.enableMyLocation();
			mLocationOverlayHelper.enableCompass();
		}
		if (animateTo)
			mLocationOverlayHelper.animateToLocation();
	}

	/**
	 * 保存地图的一些参数
	 */
	protected void saveCurMapViewInfo() {
		if (mMapView == null)
			return;
		GeoPoint p = mMapView.getMapCenter();
		MapViewInfo.getLastMapView().lastCityCode = UserAppSession.cur_CityCode;
		MapViewInfo.getLastMapView().centerX = p.getLongitudeE6();
		MapViewInfo.getLastMapView().centerY = p.getLatitudeE6();
		MapViewInfo.getLastMapView().zoomLevel = mMapView.getZoomLevel();
	}

	public void doPause() {
		if (mLocationOverlayHelper != null) {
			mLocationOverlayHelper.disableMyLocation();
			mLocationOverlayHelper.disableCompass();
		}
	}

	public void doStop() {
		saveCurMapViewInfo();
		if (this.mLocationOverlayHelper != null) {
			this.mLocationOverlayHelper.disableMyLocation();
			this.mLocationOverlayHelper.disableCompass();
		}
		if ("0756".equals(mapInitCity)) {
			ClsServerUrlSetting clsCustomerServer = new ClsServerUrlSetting();
			clsCustomerServer.strTileUrl = "http://emap0.mapabc.com";// 配置地图服务器
			clsCustomerServer.strPoiSearchUrl = "http://search1.mapabc.com:80";// 配置poi搜索服务器
			clsCustomerServer.strTmcTileUrl = "http://tm.mapabc.com";
			mMapView.setServerUrl(clsCustomerServer);
		}
	}

	public void addExtraPois(List<PoiItem> poiItems) {
		String lastpopid = null;
		if (extraPoiOverlay != null)
			lastpopid = extraPoiOverlay.lastPopupItemId;
		if (extraPoiOverlay != null) {
			extraPoiOverlay.removeFromMap();
		}
		if (poiItems != null && poiItems.size() > 0) {
			int res = R.drawable.icon_num01;
			Drawable drawable = mapAct.getResources().getDrawable(res);
			// 将结果添加到PoiOverlay
			extraPoiOverlay = new MyPoiOverlay(mapAct, drawable, poiItems);
			extraPoiOverlay.addToMap(mMapView); // 将poiOverlay标注在地图上
			mMapView.invalidate();

			extraPoiOverlay.lastPopupItemId = lastpopid;
			restoreLastPoiPopup();
		}
	}

	protected void restoreLastPoiPopup() {
		int lastpop = -1;
		PoiItem item = null;
		String lastpopid = null;
		if (extraPoiOverlay != null)
			lastpopid = extraPoiOverlay.lastPopupItemId;
		if (lastpopid != null) {
			for (int i = 0; i < extraPoiOverlay.size(); i++) {
				item = extraPoiOverlay.getItem(i);
				if (lastpopid
						.equals(item.getTypeCode() + ":" + item.getPoiId())) {
					lastpop = i;
					break;
				}
			}
		}
		if (lastpop > -1) {
			// ToastUtil.showToast(this, Integer.toString(lastpop),
			// Toast.LENGTH_SHORT);
			extraPoiOverlay.showPopupWindow(lastpop);
			mMapView.invalidate();
		}
	}

	protected static int zoomDef[] = { 0, 1, 2, 4, 8, 16, 32, 64, 128, 256,
			512, 1024, 2048, 4096, 8192, 16384, 32768, 65536, 500000000 };

	public void zoomToFitE6(int x1E6, int y1E6, int x2E6, int y2E6) {
		// 定位一下
		int cx = (x1E6 + x2E6) / 2;
		int cy = (y1E6 + y2E6) / 2;
		int zl;
		double w = Math.abs(x2E6 / 1E6 - x1E6 / 1E6);
		double h = Math.abs(y2E6 / 1E6 - y1E6 / 1E6);
		double d = Math.sqrt(w * w + h * h) * 150;
		zl = 12;
		for (int i = 0; i < zoomDef.length - 1; i++) {
			if (d > zoomDef[i] && d <= zoomDef[i + 1]) {
				zl = 17 - i;
				break;
			}
		}

		if (isBigScreen(mMapView))
			zl++;
		if (zl > 17)
			zl = 17;
		if (zl < 4)
			zl = 4;

		GeoPoint p = new GeoPoint(cy, cx);
		mMapController.setCenter(p);// 设置地图中心点
		mMapController.setZoom(zl);// 设置地图zoom级别
	}

	public void zoomToFit(double x1, double y1, double x2, double y2) {
		zoomToFitE6((int) (x1 * 1E6), (int) (y1 * 1E6), (int) (x2 * 1E6),
				(int) (y2 * 1E6));
	}

}
