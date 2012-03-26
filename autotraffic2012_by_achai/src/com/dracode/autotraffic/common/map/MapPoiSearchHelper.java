package com.dracode.autotraffic.common.map;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import android.app.ProgressDialog;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import com.dracode.andrdce.ct.UserAppSession;
import com.dracode.autotraffic.R;
import com.mapabc.mapapi.GeoPoint;
import com.mapabc.mapapi.MapActivity;
import com.mapabc.mapapi.MapView;
import com.mapabc.mapapi.PoiItem;
import com.mapabc.mapapi.PoiPagedResult;
import com.mapabc.mapapi.PoiSearch;
import com.mapabc.mapapi.PoiTypeDef;
import com.mapabc.mapapi.PoiSearch.SearchBound;

public class MapPoiSearchHelper {

	public MapActivity mapAct;
	public MapView mMapView;

	public static final int SEARCH_POI = 1002;
	public static final int SEARCH_ERROR = 1003;
	public static final int SEARCH_REFRESH = 1004;
	public static final int SEARCH_RECHECK = 1005;

	protected String searchKeyword = "";
	protected int searchDistance = 0;
	protected SearchBound searchBnd;
	protected Thread searchingThread = null;
	protected PoiPagedResult searchResult;
	protected InputStream searchResInput;
	protected MyPoiOverlay searchPoiOverlay;
	protected boolean searchingInProgress = false;
	private int lastMapCx = 0, lastMapCy = 0, lastMapSc = 0;
	private String poiType;
	private ProgressDialog progDialog;

	public MapPoiSearchHelper(MapActivity act, MapView mv) {
		mapAct = act;
		mMapView = mv;
	}

	/**
	 * 搜索
	 * 
	 * @param keyword
	 * @param distance
	 */
	protected void doSearch(String keyword, int distance) {
		searchKeyword = keyword;
		searchDistance = distance;
		handler.sendMessage(Message.obtain(handler, SEARCH_REFRESH));
	}

	/**
	 * 开始搜索
	 */
	protected void onSearch(String _poiType, int _distance) {
		poiType = _poiType;
		searchDistance = _distance;
		if (progDialog == null) {
			progDialog = new ProgressDialog(mapAct);
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
		searchBnd = new SearchBound(mMapView);

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
							PoiSearch.Query query = new PoiSearch.Query(
									searchKeyword, poiType);
							poiSearch = new PoiSearch(mapAct, query);
							poiSearch.setBound(searchBnd);
							searchResult = poiSearch.searchPOI();
						} catch (IOException e) {
							hideProgDlg();
							e.printStackTrace();
						}
						if (searchResult != null) {
							handler.sendMessage(Message.obtain(handler,
									SEARCH_POI));
						} else {
							handler.sendMessage(Message.obtain(handler,
									SEARCH_ERROR));
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

	public void addSearchPois(List<PoiItem> poiItems) {
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
				Drawable drawable = mapAct.getResources().getDrawable(res);
				// 将结果添加到PoiOverlay
				searchPoiOverlay = new MyPoiOverlay(mapAct, drawable, poiItems);
				searchPoiOverlay.addToMap(mMapView); // 将poiOverlay标注在地图上
				mMapView.invalidate();

				searchPoiOverlay.lastPopupItemId = lastpopid;
				restoreLastPoiPopup();
			}
			hideProgDlg();
		} catch (IOException e) {
			hideProgDlg();
			UserAppSession.showToast(mapAct, "网络连接错误！");
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
				if (lastpopid
						.equals(item.getTypeCode() + ":" + item.getPoiId())) {
					lastpop = i;
					break;
				}
			}
		}
		if (lastpop > -1) {
			searchPoiOverlay.showPopupWindow(lastpop);
			mMapView.invalidate();
		}
	}

	protected Handler handler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case SEARCH_REFRESH:
				onSearch(PoiTypeDef.All, searchDistance);
				break;
			case SEARCH_RECHECK:
				checkResearch();
				break;
			case SEARCH_ERROR:
				hideProgDlg();
				UserAppSession.showToast(mapAct, "请求失败，请检查网络连接！");
				break;
			case SEARCH_POI:
				addSearchPois(null);
				break;
			}
		}
	};

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
			// handler.sendMessage(Message.obtain(handler, SEARCH_REFRESH));
		}
	}

	private Timer idleTimer = null;
	private TimerTask idlTimerTask = null;

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

	public void doPause() {
		 killIdleTimer();
	}

	public void doStop() {
		if (searchingThread != null) {
			Thread thr = searchingThread;
			searchingThread = null;
			synchronized (thr) {
				thr.notify();
			}
		}
		killIdleTimer();
	}

}
