package com.dracode.autotraffic.common.map;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.protocol.HTTP;
import org.json.JSONArray;
import org.json.JSONObject;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.DialogInterface.OnKeyListener;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;

import com.dracode.andrdce.ct.CtRuntimeCancelException;
import com.dracode.andrdce.ct.CtRuntimeException;
import com.dracode.andrdce.ct.NetUtil;
import com.dracode.andrdce.ct.UserAppSession;
import com.dracode.autotraffic.common.map.LocationHelper.OnLocationFoundEvent;
import com.dracode.autotraffic.common.map.PoiListActivity.PoiListAdapter;

public class PoiListActivityHelper {

	public PoiListActivity theAct;
	private String queryCityName;
	private String queryOption;
	private String queryPoiType;
	private String queryPoiName;
	private String queryLon, queryLat, queryRange;
	public int recCount = 0, curPage = 1;
	public String resBndInfo;
	private String curLocation = "";

	private ProgressDialog mProgressDlg = null;

	public void init(PoiListActivity act) {
		theAct = act;

		Bundle ext = theAct.getIntent().getExtras();
		queryPoiName = ext.getString("keywords");
		if (queryPoiName == null)
			queryPoiName = "";
		queryPoiType = ext.getString("poiType");
		if (queryPoiType == null)
			queryPoiType = "";
		if (queryPoiType.length() > 0) {
			queryPoiName = queryPoiType;
			if (queryPoiType.contains("公交"))
				queryPoiType = "BUS";
			else
				queryPoiType = "";
		}
		queryCityName = ext.getString("QueryCityName");
		if (queryCityName == null || "".equals(queryCityName))
			queryCityName = UserAppSession.cur_CityCode;
		queryRange = Integer.toString(ext.getInt("distance"));
		queryOption = ext.getString("QueryOptions");
		if (queryOption == null)
			queryOption = "";

		initPoiSearch();
	}

	private void initPoiSearch() {
		if (queryPoiName.length() > 0)
			theAct.text_Title.setText(queryPoiName);
		else if (queryPoiType.length() > 0)
			theAct.text_Title.setText(queryPoiType);
		else
			theAct.text_Title.setText("附近查找");
		theAct.locView.setText("正在获取位置...");
		OnLocationFoundEvent evt = new OnLocationFoundEvent() {
			@Override
			public void onLocationFound(double lon, double lat, String addr) {
				curLocation = addr;
				theAct.locView.setText("位置: " + curLocation);
				queryLon = Double.toString(lon);
				queryLat = Double.toString(lat);
				executeSearch();
			}

			@Override
			public void onCanceled() {
				theAct.locView.setText("已取消操作");
			}

			@Override
			public void onError(String msg) {
				theAct.locView.setText("定位出错");
				UserAppSession.showToast(theAct,msg);
			}

		};
		LocationHelper.getMyLocation(theAct, evt);
	}

	public void executeSearch() {

		mProgressDlg = new ProgressDialog(theAct);
		mProgressDlg.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		mProgressDlg.setIndeterminate(false);
		mProgressDlg.setCancelable(false);
		mProgressDlg.setMessage("正在搜索...");
		OnKeyListener ls = new OnKeyListener() {
			public boolean onKey(DialogInterface dialog, int keyCode,
					KeyEvent event) {
				if (keyCode == KeyEvent.KEYCODE_BACK) {
					queryCanceled = true;
					if (queryingUrl != null) {
						NetUtil.stopNetUrl(queryingUrl);
					}
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

		PoiGeoSearchTask tsk = new PoiGeoSearchTask();
		tsk.execute();
	}

	private boolean queryCanceled = false;
	private String queryingUrl = null;

	class PoiGeoSearchTask extends AsyncTask<String, Integer, Object> {
		@Override
		protected Object doInBackground(String... params) {
			try {

				queryCanceled = false;
				String url = getDataUrl("POI_GEO_SEARCH");
				try {
					url = url + "&city="
							+ URLEncoder.encode(queryCityName, HTTP.UTF_8);
					url = url + "&x=" + queryLon;
					url = url + "&y=" + queryLat;
					url = url + "&r=" + queryRange;
					url = url + "&key="
							+ URLEncoder.encode(queryPoiName, HTTP.UTF_8);
					url = url + "&tp="
							+ URLEncoder.encode(queryPoiType, HTTP.UTF_8);
					url = url + "&pg=" + Integer.toString(curPage);
				} catch (UnsupportedEncodingException e) {
				}
				String enckey = NetUtil.getEncryptKey(url);
				url = url + "&verifyCode=" + enckey;
				UserAppSession.LogD(url);

				queryingUrl = url;
				JSONObject rJson = UserAppSession.cursession().getUrlJsonData(
						url, null, UserAppSession.CAHCE_EXPIRE_TIME_DAY);

				if (queryCanceled)
					throw new CtRuntimeCancelException("操作中止");

				if (rJson == null)
					throw new CtRuntimeException("未查询到任何信息");

				if (rJson.has("Count"))
					if (curPage == 1) {
						recCount = rJson.getInt("Count");
						resBndInfo = rJson.getString("Bounds");
					}

				List<PoiInfo> res = new ArrayList<PoiInfo>();
				if (rJson.has("PoiInfos")) {
					JSONArray jArr = rJson.getJSONArray("PoiInfos");
					for (int i = 0; i < jArr.length(); i++) {
						JSONArray jPoi = jArr.getJSONArray(i);
						PoiInfo poi = new PoiInfo();
						poi.loadPropsFromJson(jPoi);
						res.add(poi);
					}
				}

				return res;
			} catch (Throwable ex) {
				return ex;
			}
		}

		@Override
		protected void onPostExecute(Object result) {
			if (mProgressDlg != null)
				mProgressDlg.dismiss();

			doPoiGeoResult(result);
		}
	}

	List<PoiInfo> poiInfoList = new ArrayList<PoiInfo>();
	PoiListAdapter poiListAdpt = null;

	@SuppressWarnings("unchecked")
	protected void doPoiGeoResult(Object res) {

		if (UserAppSession.isTaskCancelMessage(res)) {
			UserAppSession.showToast(theAct, "操作被中止");
			return;
		}
		if (res instanceof Throwable) {
			UserAppSession.showToast(theAct,
					"执行出错-" + ((Exception) res).getMessage());
			return;
		}

		theAct.locView.setText("位置: " + curLocation + "，共"
				+ Integer.toString(recCount) + "个结果");

		List<PoiInfo> ps = (List<PoiInfo>) res;
		poiInfoList.clear();
		poiInfoList.addAll(ps);
		if (this.recCount > 20)
			poiInfoList.add(null);
		if (poiListAdpt == null) {
			if (poiInfoList.size() > 0) {
				poiListAdpt = new PoiListAdapter(theAct, poiInfoList);
				theAct.list_Contents.setAdapter(poiListAdpt);
			} else {
				theAct.list_Contents.setVisibility(View.GONE);
				theAct.text_NoData.setVisibility(View.VISIBLE);
			}
		} else
			poiListAdpt.notifyDataSetChanged();
	}

	private String getDataUrl(String tp) {
		if (tp.equals("POI_GEO_SEARCH"))
			return UserAppSession.getBusLineServerUrl() + "&type=8";
		return null;
	}

	public void setCurPage(int _page) {
		if (curPage != _page) {
			curPage = _page;
			executeSearch();
		}
	}
}
