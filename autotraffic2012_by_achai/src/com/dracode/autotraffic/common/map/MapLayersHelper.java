package com.dracode.autotraffic.common.map;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.DialogInterface.OnMultiChoiceClickListener;
import android.graphics.drawable.Drawable;

import com.dracode.andrdce.ct.TypeUtil;
import com.dracode.andrdce.ct.UserAppSession;
import com.dracode.andrdce.ctact.CvtDataHelper;
import com.dracode.andrdce.ctact.CvtDataHelper.OnCvtDataEvent;
import com.dracode.autotraffic.R;
import com.mapabc.mapapi.GeoPoint;
import com.mapabc.mapapi.MapActivity;
import com.mapabc.mapapi.MapView;
import com.mapabc.mapapi.PoiItem;

public class MapLayersHelper {

	public MapActivity mapAct;
	public MapView mMapView;
	protected MyPoiOverlay roadCamOverlay;

	/** 分类数据. */
	protected String[] sortData = new String[] { "实时路况", "道路视频", "交通事件" };
	protected boolean[] sortBoolean = new boolean[] { false, false, false };

	public MapLayersHelper(MapActivity act, MapView mv) {
		mapAct = act;
		mMapView = mv;
	}

	/**
	 * 图层显示
	 */
	protected void loadLayers() {
		// 实时路况
		if (sortBoolean[0]) {
			mMapView.setTraffic(true);// 显示
		} else {
			mMapView.setTraffic(false);// 隐藏
		}
		// 道路视频
		if (sortBoolean[1]) {
			loadRoadCamData(false);// 显示
		} else {
			// 隐藏
			if (roadCamOverlay != null)
				roadCamOverlay.removeFromMap();
		}
		// 交通事件
		if (sortBoolean[2]) {
			// 显示
		} else {
			// 隐藏
		}
	}

	/**
	 * 图层窗口
	 */
	protected boolean[] selectingSortBoolean = new boolean[] { false, false,
			false };

	public void selectLayers() {
		AlertDialog.Builder builder = new AlertDialog.Builder(mapAct);
		builder.setTitle("图层");
		for (int i = 0; i < selectingSortBoolean.length; i++) {
			selectingSortBoolean[i] = sortBoolean[i];
		}
		builder.setMultiChoiceItems(sortData, selectingSortBoolean,
				new OnMultiChoiceClickListener() {

					@Override
					public void onClick(final DialogInterface dialog,
							final int which, final boolean isChecked) {
						selectingSortBoolean[which] = isChecked;
						switch (which) {
						case 2:
							UserAppSession.showToast(mapAct,
									"暂不支持交通事件图层加载，敬请关注！");
							break;
						default:
							break;
						}
					}
				});
		builder.setNegativeButton("确定", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(final DialogInterface dialog, final int which) {
				SharedPreferences roadSp = mapAct.getSharedPreferences(
						"MapRoadSort", Context.MODE_WORLD_READABLE
								| Context.MODE_WORLD_WRITEABLE);
				for (int index = 0; index < sortData.length; index++) {
					String name = sortData[index];
					sortBoolean[index] = selectingSortBoolean[index];
					boolean val = sortBoolean[index];
					roadSp.edit().putBoolean(name, val).commit();
				}
				loadLayers();
			}
		});
		builder.create().show();
	}

	/**
	 * 添加道路视频图层
	 */
	public CvtDataHelper roadCamCvtHelper = null;
	private List<Map<String, String>> roadCamList;

	protected void addRoadCamPois() {
		List<PoiItem> poiItems = new ArrayList<PoiItem>();
		if (roadCamOverlay != null) {
			roadCamOverlay.removeFromMap();
		}
		if (roadCamList != null) {
			for (Map<String, String> cam : roadCamList) {
				addRoadCam(poiItems, cam.get("摄像头名"), cam.get("经度"),
						cam.get("纬度"), cam.get("标清链接"), cam.get("IPHONE链接"));
			}
		}
		// MyPoiOverlay.addRoadCamPoi(poiItems);
		if (poiItems != null && poiItems.size() > 0) {
			Drawable drawable = mapAct.getResources().getDrawable(
					R.drawable.roadcam);
			// 将结果添加到PoiOverlay
			roadCamOverlay = new MyPoiOverlay(mapAct, drawable, poiItems);
			roadCamOverlay.addToMap(mMapView);
			mMapView.invalidate();
		}
		// hideProgDlg();
	}

	private static void addRoadCam(List<PoiItem> poiItems, String cname,
			String sx, String sy, String hdLink, String iphoneLink) {
		double x, y;
		x = TypeUtil.ObjectToDouble(sx);
		y = TypeUtil.ObjectToDouble(sy);
		if (x == 0 || y == 0)
			return;
		GeoPoint pt = new GeoPoint((int) (y * 1E6),
				(int) (x * 1E6));
		PoiItem item = new PoiItem(hdLink, pt, cname, iphoneLink);
		item.setTypeCode("ROAD_CAM");
		poiItems.add(item);
	}

	public void loadRoadCamData(boolean refresh) {
		if (roadCamCvtHelper == null) {
			roadCamCvtHelper = new CvtDataHelper();
			// ["ID","摄像头名","经度","纬度","标清链接","IPHONE链接"]
			roadCamCvtHelper.init(mapAct, 610052);
			roadCamCvtHelper.cacheExpireTm = UserAppSession.CAHCE_EXPIRE_TIME_DAY;
		}
		roadCamCvtHelper.extraParams="addparam=P_CITYCODE:"+UserAppSession.cur_CityCode;
		if (!refresh) {
			if (roadCamCvtHelper.tryLoadFromCache()) {
				roadCamList = roadCamCvtHelper.getCurrentDataListSS();
				addRoadCamPois();
				return;
			}
		}

		OnCvtDataEvent evt = new OnCvtDataEvent() {
			@Override
			public void onDataCanceled() {
				UserAppSession.showToast(mapAct, "操作中止");
			}

			@Override
			public void onDataError(String msg) {
				UserAppSession.showToast(mapAct, msg);
			}

			@Override
			public void onDataLoaded(CvtDataHelper cvth) {
				roadCamList = roadCamCvtHelper.getCurrentDataListSS();
				addRoadCamPois();
			}

			@Override
			public void afterFetchData() {
			}
		};
		roadCamCvtHelper.setCvtDataEvt(evt);
		roadCamCvtHelper.loadingMessage = "正在获取道路视频信息...";
		if (refresh)
			roadCamCvtHelper.refreshData();
		else
			roadCamCvtHelper.loadData();
	}
}
