package com.dracode.autotraffic.common.map;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import com.dracode.andrdce.ct.TypeUtil;
import com.dracode.andrdce.ct.UserAppSession;
import com.dracode.autotraffic.R;
import com.dracode.autotraffic.common.helpers.BaseActivityHelper;
import com.dracode.autotraffic.main.MyApp;
import com.mapabc.mapapi.ClsServerUrlSetting;
import com.mapabc.mapapi.GeoPoint;
import com.mapabc.mapapi.MapActivity;
import com.mapabc.mapapi.MapController;
import com.mapabc.mapapi.MapView;
import com.mapabc.mapapi.PoiItem;
import com.mapabc.mapapi.PoiPagedResult;
import com.mapabc.mapapi.PoiSearch;
import com.mapabc.mapapi.PoiTypeDef;
import com.mapabc.mapapi.PoiSearch.SearchBound;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.ListView;
import android.widget.TextView;

public class OldPoiMapActivity extends MapActivity {

	public MapView mMapView;
	protected MapController mMapController;
	protected MyLocationOverlayHelper mLocationOverlay;

	protected ProgressDialog progDialog;

	public static final int FIRST_LOCATION = 1001;
	public static final int SEARCH_POI = 1002;
	public static final int SEARCH_ERROR = 1003;
	public static final int SEARCH_REFRESH = 1004;
	public static final int SEARCH_RECHECK = 1005;
	public static final int LIST_SEARCH_POI = 1006;
	/** 列表模式 .*/
	protected static final String LIST_SEARCH_MODEL = "list";
	/** 地图模式 .*/
	protected static final String MAP_SEARCH_MODEL = "map";
	
	private Timer idleTimer = null;
	private TimerTask idlTimerTask = null;
	
	protected String searchKeyword = "";
	protected int searchDistance = 0;
	protected String searchModel = "";
	protected SearchBound searchBnd;
	protected Thread searchingThread = null;
	protected PoiPagedResult searchResult;
	protected InputStream searchResInput;
	protected MyPoiOverlay poiOverlay;
	protected MyPoiOverlay searchPoiOverlay;
	protected boolean searchingInProgress = false;
	private int lastMapCx = 0, lastMapCy = 0, lastMapSc = 0;
	private int DEFAULT_ZOOM = 15;
	/** 我的位置 .*/
	protected GeoPoint myPoint;
	/** 搜索类别 .*/
	private String poiType;
	/** 分类数据. */
	protected String[] sortData = new String[] {"实时路况", "道路视频", "交通事件"};
	protected boolean[] sortBoolean = new boolean[] {false, false, false};
	/** 列表对象.*/
	private ListView historyListView;
	private List<PoiItem> listPoiItems;
	protected MyApp myApp;
	protected BaseActivityHelper baseHelper;
	
	public OldPoiMapActivity() {}
	
	protected void init(){
		baseHelper = new BaseActivityHelper();
		baseHelper.init(this);
		
		historyListView = (ListView) findViewById(R.id.list_contents);
	}
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		SharedPreferences sp = getSharedPreferences("MapRoadSort", Context.MODE_WORLD_READABLE);
		
		if ("RoadCoditionMapActivity".equals(this.getClass().getSimpleName())) {
			sortBoolean[0] = sp.getBoolean(sortData[0], false);
			sortBoolean[1] = sp.getBoolean(sortData[1], false);
			sortBoolean[2] = sp.getBoolean(sortData[2], false);
		}
		myApp=(MyApp)this.getApplication();
	}
	/**
	 * 初始化地图
	 */
	protected String mapInitCity=null;
	protected void initMap() {
		if(mapInitCity!=null && mapInitCity.equals(UserAppSession.cur_CityCode))
			return;
		mMapView = (MapView) findViewById(R.id.mapView);
		if(!UserAppSession.cur_CityCode.equals(mapInitCity)){
			if ("0756".equals(UserAppSession.cur_CityCode)) {
				// 设置服务器地址 企业及用户部署服务用
				ClsServerUrlSetting clsCustomerServer = new ClsServerUrlSetting();
				clsCustomerServer.strTileUrl = "http://120.197.89.33";// 配置地图服务器
				clsCustomerServer.strPoiSearchUrl = "http://120.197.89.33:8082";//配置poi搜索服务器
				clsCustomerServer.strTmcTileUrl = "http://120.197.89.33";
				mMapView.setServerUrl(clsCustomerServer);
			}
			else if ("0756".equals(mapInitCity)){
				ClsServerUrlSetting clsCustomerServer = new ClsServerUrlSetting();
				clsCustomerServer.strTileUrl = "http://emap0.mapabc.com";// 配置地图服务器
				clsCustomerServer.strPoiSearchUrl = "http://search1.mapabc.com:80";//配置poi搜索服务器
				clsCustomerServer.strTmcTileUrl = "http://tm.mapabc.com";
				mMapView.setServerUrl(clsCustomerServer);
			}
		}
		mMapView.setBuiltInZoomControls(true); // 设置启用内置的缩放控件
		mMapController = mMapView.getController(); // 得到mMapView的控制权,可以用它控制和驱劢平移和缩放

		if(MapViewInfo.lastMapView != null)
			if(!UserAppSession.cur_CityCode.equals(MapViewInfo.getLastMapView().lastCityCode))
				MapViewInfo.lastMapView=null;
		if(MapViewInfo.lastMapView == null || (mapInitCity!=null && !mapInitCity.equals(UserAppSession.cur_CityCode))){
			MapViewInfo mv=new MapViewInfo();
			if(mv.fromString(UserAppSession.cur_CityGeoInfo))
				MapViewInfo.lastMapView=mv;
		}
		if (MapViewInfo.lastMapView == null) {
			mMapController.setZoom(DEFAULT_ZOOM);// 设置地图zoom级别
			findMyLocation(true);
		} else {
			// 用给定的经纬度构造一个GeoPoint，单位是微度 (度 *1E6)
			gotoViewPoint(MapViewInfo.lastMapView.centerX, MapViewInfo.lastMapView.centerY, MapViewInfo.lastMapView.zoomLevel);
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
		mapInitCity=UserAppSession.cur_CityCode;
	}
	
	public void gotoViewPoint(int centerX, int centerY, int zoomLevel) {
		GeoPoint p = new GeoPoint(centerY,centerX);
		mMapController.setCenter(p);// 设置地图中心点
		mMapController.setZoom(zoomLevel);// 设置地图zoom级别
	}

	/**
	 * 搜索
	 * @param keyword
	 * @param distance
	 * @param model 查询模式，列表模式为list，地图模式为map
	 */
	protected void doSearch(String keyword, int distance, String model) {
		searchKeyword = keyword;
		searchDistance = distance;
		searchModel = model;
		handler.sendMessage(Message.obtain(handler, SEARCH_REFRESH));
	}
	/**
	 * 开始搜索
	 * @param _model 查询模式，列表模式为list，地图模式为map
	 */
	protected void onSearch(String _poiType, int _distance, String _model) {
		poiType = _poiType;
		searchDistance = _distance;
		searchModel = _model;
		if (progDialog == null) {
			progDialog = new ProgressDialog(this);
		}
		progDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		progDialog.setIndeterminate(false);
		progDialog.setCancelable(true);
		progDialog.setMessage("正在搜索，请稍等...");
		progDialog.show();
		
		// 根据类别搜索，搜索内容置空
		if (!poiType.equals(PoiTypeDef.All)) {
			searchKeyword = "";
		}
		// 获取地图显示的POI搜索范围
//		searchBnd = new SearchBound(mMapView);
		
		if (searchingThread == null) {
			searchingThread = new Thread(new Runnable() {
				public void run() {
					Looper.prepare();
					while (searchingThread != null) {
						searchingInProgress = true;
						
							try {
								searchResult = null;
								PoiSearch poiSearch = null;
								// 设置搜索字符串
								PoiSearch.Query query = new PoiSearch.Query(searchKeyword, poiType);
								if (LIST_SEARCH_MODEL.equals(searchModel)) {
									poiSearch = new PoiSearch(OldPoiMapActivity.this, getResources().getString(R.string.maps_api_key), query);
								} else {
									poiSearch = new PoiSearch(OldPoiMapActivity.this, query);
								}
								poiSearch.setBound(new SearchBound(myPoint, searchDistance));// 搜索附近2公里
								searchResult = poiSearch.searchPOI();
							} catch (IOException e) {
								hideProgDlg();
								e.printStackTrace();
							}
							if (searchResult != null) {
								if (LIST_SEARCH_MODEL.equals(searchModel)) {
									handler.sendMessage(Message.obtain(handler, LIST_SEARCH_POI));
								} else {
									handler.sendMessage(Message.obtain(handler, SEARCH_POI));
								}
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
					Looper.loop();
				}
			});
			searchingThread.start();
		} else {
			synchronized (searchingThread) {
				searchingThread.notify();
			}
		}
	}
	// 关闭弹出框
	private void hideProgDlg() {
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
			mLocationOverlay = new MyLocationOverlayHelper(this, mMapView, null);
			mMapView.getOverlays().add(mLocationOverlay);
			mLocationOverlay.enableMyLocation();
			mLocationOverlay.enableCompass();
			// 实现初次定位使定位结果居中显示
			if (animateTo) {
				mLocationOverlay.runOnFirstFix(new Runnable() {
					public void run() {
						handler.sendMessage(Message.obtain(handler, FIRST_LOCATION));
					}
				});
			}
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
				myPoint = mLocationOverlay.getMyLocation();
				if (myPoint != null) {
					mMapController.animateTo(myPoint);
					// mMapController.setCenter(point); // 设置地图中心点
					mMapController.setZoom(DEFAULT_ZOOM);
					MapViewInfo.getLastMapView().myLatitudeE6 = myPoint.getLatitudeE6();
					MapViewInfo.getLastMapView().myLongitudeE6 = myPoint.getLongitudeE6();
				} else {
					if ("020".equals(UserAppSession.cur_CityCode)) {
						GeoPoint _point = new GeoPoint((int)(23.120136 * 1E6), (int)(113.298851 * 1E6));
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
				onSearch(PoiTypeDef.All, searchDistance, searchModel);
				break;
			case SEARCH_RECHECK:
				checkResearch();
				break;
			case SEARCH_ERROR:
				hideProgDlg();
				UserAppSession.showToast(OldPoiMapActivity.this, "请求失败，请检查网络连接！");
				break;
			case SEARCH_POI:
				addSearchPois(null);
				break;
			case LIST_SEARCH_POI:
				addListSearchPois();
				break;
			}
		}
	};
	/**
	 * 图层显示
	 */
	protected void switchLayer() {
		searchKeyword = "";
		// 实时路况
		if (sortBoolean[0]) {
			mMapView.setTraffic(true);// 显示
		} else {
			mMapView.setTraffic(false);// 隐藏
		}
		// 道路视频
		if (sortBoolean[1]) {
			addRoadCamPois();// 显示
		} else {
			// 隐藏
			if (poiOverlay != null) {
				poiOverlay.removeFromMap();
			}
		}
		// 交通事件
		if (sortBoolean[2]) {
			// 显示
		} else {
			// 隐藏
		}
	}
	protected void restoreLastPoiPopup() {
		int lastpop = -1;
		PoiItem item = null;
		String lastpopid = null;
		if(searchPoiOverlay!=null)
			lastpopid=searchPoiOverlay.lastPopupItemId;
		if (lastpopid != null) {
			for (int i = 0; i < searchPoiOverlay.size(); i++) {
				item = searchPoiOverlay.getItem(i);
				if (lastpopid.equals(item.getTypeCode() + ":" + item.getPoiId())) {
					lastpop = i;
					break;
				}
			}
		}
		if (lastpop > -1) {
			//ToastUtil.showToast(this, Integer.toString(lastpop), Toast.LENGTH_SHORT);
			searchPoiOverlay.showPopupWindow(lastpop);
			mMapController.animateTo(item.getPoint());
			mMapView.invalidate();
		}
	}
	/**
	 * 添加道路视频图层
	 */
	protected void addRoadCamPois() {
		/*List<PoiItem> poiItems = new ArrayList<PoiItem>();
		if (poiOverlay != null) {
			poiOverlay.removeFromMap();
		}
		MyPoiOverlay.addRoadCamPoi(poiItems);
		if (poiItems != null && poiItems.size() > 0) {
			Drawable drawable = getResources().getDrawable(R.drawable.roadcam);
			// 将结果添加到PoiOverlay
			poiOverlay = new MyPoiOverlay(OldPoiMapActivity.this, drawable, poiItems);
			poiOverlay.addToMap(mMapView);
			mMapView.invalidate();
		}*/
//		hideProgDlg();
	}
	protected void addSearchPois(List<PoiItem> poiItems) {
		try {
			String lastpopid = null;
			if(searchPoiOverlay!=null)
				lastpopid=searchPoiOverlay.lastPopupItemId;
			if (poiItems == null) {
				poiItems = new ArrayList<PoiItem>();
			
				for (int i = 1; i <= searchResult.getPageCount(); i++) {
					List<PoiItem> pis = searchResult.getPage(i);
					poiItems.addAll(pis);
				}
			}
			if (searchPoiOverlay != null) {
				searchPoiOverlay.removeFromMap();
			}
			if (poiItems != null && poiItems.size() > 0) {
				int res = R.drawable.icon_num01;
				Drawable drawable = getResources().getDrawable(res);
				// 将结果添加到PoiOverlay
				searchPoiOverlay = new MyPoiOverlay(OldPoiMapActivity.this, drawable, poiItems);
				searchPoiOverlay.addToMap(mMapView); // 将poiOverlay标注在地图上
				mMapView.invalidate();

				searchPoiOverlay.lastPopupItemId = lastpopid;
				restoreLastPoiPopup();
			}
			hideProgDlg();
		} catch (IOException e) {
			hideProgDlg();
			UserAppSession.showToast(this, "网络连接错误！");
		}
	}
	public void addListSearchPois() {
		try {
			if (listPoiItems == null) {
				listPoiItems = new ArrayList<PoiItem>();
				
				for (int i = 1; i <= searchResult.getPageCount(); i++) {
					List<PoiItem> pis = searchResult.getPage(i);
					listPoiItems.addAll(pis);
				}
				myApp.setPoiItems(listPoiItems);
				myApp.setPoiItemsPageNum(1);
			}
			if (listPoiItems.size() > 0) {
				List<PoiInfo> sortList = new ArrayList<PoiInfo>();
				PoiInfo entity = null;
				int pageCount = (int) Math.ceil(listPoiItems.size() / 10.0);
				int startIndex = myApp.getPoiItemsPageNum() * 10 - 10;
				int endIndex = myApp.getPoiItemsPageNum() * 10;
				if (endIndex > listPoiItems.size()) endIndex = listPoiItems.size();
				for (int i = startIndex; i < endIndex; i++) {
					PoiItem item = listPoiItems.get(i);
					entity = new PoiInfo();
					
					GeoPoint point = item.getPoint();
					double distance = TypeUtil.getDisByCoord(point.getLongitudeE6() / 1E6, 
												  		   point.getLatitudeE6() / 1E6, 
												  		   myPoint.getLongitudeE6() / 1E6, 
												  		   myPoint.getLatitudeE6() / 1E6);
					entity.setIcon(R.drawable.icon_num02);
					entity.setName(item.getTitle());
					entity.setAddress(item.getSnippet());
					entity.setDistance((int) distance);
					entity.setPhone(item.getTel());
					entity.setTypeCode(item.getTypeCode());
					entity.setPoiId(item.getPoiId());
					sortList.add(entity);
				}
				if (pageCount > 1) {
					sortList.add(null);
				}
				if (historyListView != null) {
//					NearbyResultAdapter adapter = new NearbyResultAdapter(this, sortList);
//					historyListView.setAdapter(adapter);
//					adapter.notifyDataSetChanged();
				}
			} else {
				TextView noDataView = (TextView) findViewById(R.id.no_data);
				if (noDataView != null) {
					noDataView.setVisibility(View.VISIBLE);
				}
			}
			hideProgDlg();
		} catch (IOException e) {
			hideProgDlg();
			UserAppSession.showToast(this, "网络连接错误！");
		}
	}
	/**
	 * 保存地图的一些参数
	 */
	protected void saveCurMapViewInfo() {
		if (mMapView == null)
			return;
		GeoPoint p = mMapView.getMapCenter();
		MapViewInfo.getLastMapView().lastCityCode=UserAppSession.cur_CityCode;
		MapViewInfo.getLastMapView().centerX = p.getLongitudeE6();
		MapViewInfo.getLastMapView().centerY = p.getLatitudeE6();
		MapViewInfo.getLastMapView().zoomLevel = mMapView.getZoomLevel();
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
		// 保存地图的一些参数
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
	protected void checkResearch() {
		if ("".equals(searchKeyword) || searchingInProgress) {
			return;
		}
		int cx, cy, sc;
		GeoPoint p = mMapView.getMapCenter();
		cx = p.getLongitudeE6();
		cy = p.getLatitudeE6();
		sc = mMapView.getZoomLevel();
		if (cx != lastMapCx || cy != lastMapCy || sc != lastMapSc) {
			lastMapCx = cx;
			lastMapCy = cy;
			lastMapSc = sc;
//			handler.sendMessage(Message.obtain(handler, SEARCH_REFRESH));
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
