package com.dracode.autotraffic.bus.busstation;

import java.util.List;

import com.dracode.andrdce.ct.AppUtil;
import com.dracode.andrdce.ct.TypeUtil;
import com.dracode.andrdce.ct.UserAppSession;
import com.dracode.autotraffic.common.helpers.FavoriteHelper;
import com.dracode.autotraffic.common.helpers.QueryHistoryHelper;
import com.dracode.autotraffic.common.helpers.FavoriteHelper.Favorite;
import com.dracode.autotraffic.common.helpers.FavoriteHelper.OnFavoriteChosenEvent;
import com.dracode.autotraffic.common.helpers.QueryHistoryHelper.OnHistoryRecallEvent;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.DialogInterface.OnKeyListener;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.KeyEvent;

public class BusStationQueryActivityHelper {

	public BusStationDataIntf busStationDataIntf = null;
	public BusStationQueryActivity theAct = null;
	public QueryHistoryHelper historyHelper = new QueryHistoryHelper();

	public void init(BusStationQueryActivity act) {
		theAct = act;
		busStationDataIntf = BusStationDataIntf.getBusStationDataIntf();

		OnHistoryRecallEvent evt = new OnHistoryRecallEvent() {

			@Override
			public void onRecallHistory(int idx) {
				recallHistBusQuery(idx);
			}

		};
		historyHelper.init(theAct, evt);

		reloadData();
	}

	String lastCityCode="";
	public void reloadData() {
		if(lastCityCode.equals(UserAppSession.cur_CityCode)){
			refreshHistoryList();
			return;
		}
		lastCityCode=UserAppSession.cur_CityCode;
		
		busStationDataIntf.loadData();
		BusStationHistInfo si = busStationDataIntf.getLastStationInfo();
		String station;
		if (si != null) 
			station = si.getStationName();
		else 
			station = "";
		theAct.setCurrentCityAndStation(UserAppSession.cur_CityCode, station);

		refreshHistoryList();
	}

	public void recallHistBusQuery(int po) {
		BusStationHistInfo hi = busStationDataIntf.histQueries.get(po);
		callBusStationQuery(hi.getCityName(), hi.getStationName());
	}

	public void callBusStationQuery(String city, String station) {
		if ("".equals(station)) {
			UserAppSession.showToast(theAct, "请输入查询站点名！");
			return;
		} else {
			Bundle bundle = new Bundle();
			bundle.putString("QueryStationName", station);
			bundle.putString("QueryCityName", city);
			AppUtil.startActivity(theAct, BusStationResultActivity.class,
					false, bundle);
		}
	}

	private String[] busStationArray;
	public List<String> queryingStationNames = null;
	private ProgressDialog mProgressDlg = null;
	String queryCityName = "";
	String queryStationName = "";

	public void callStationNameQuery(String city, String station) {
		if (station == null || "".equals(station.trim())) {
			UserAppSession.showToast(theAct, "请输入查询站点名！");
			return;
		}
		if (queryCityName.equals(city) && queryStationName.equals(station)
				&& queryingStationNames != null) {
			selectBusStation(queryingStationNames);
			return;
		}
		queryingStationNames = null;
		queryCityName = city;
		queryStationName = station;

		mProgressDlg = new ProgressDialog(theAct);
		mProgressDlg.setProgressStyle(android.R.style.Widget_ProgressBar_Large);
		mProgressDlg.setMessage("正在查找站点...");
		mProgressDlg.setCancelable(false);
		OnKeyListener ls = new OnKeyListener() {
			public boolean onKey(DialogInterface dialog, int keyCode,
					KeyEvent event) {
				if (keyCode == KeyEvent.KEYCODE_BACK) {
					busStationDataIntf.cancelQuery();
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
		BusStationNamesQueryTask mQueryTask;
		mQueryTask = new BusStationNamesQueryTask();
		mQueryTask.execute(queryCityName, queryStationName);
	}

	class BusStationNamesQueryTask extends AsyncTask<String, Integer, Object> {
		@Override
		protected Object doInBackground(String... params) {
			try {
				return busStationDataIntf.execBusStationNameQuery(params[0],
						params[1]);
			} catch (Throwable ex) {
				return ex;
			}
		}

		@Override
		protected void onPostExecute(Object result) {
			if (mProgressDlg != null)
				mProgressDlg.dismiss();
			refreshBusNameList(result);
		}
	}

	public void refreshBusNameList(Object res) {
		if (UserAppSession.isTaskCancelMessage(res)) {
			UserAppSession.showToast(theAct, "查询被中止");
			return;
		}
		if (res instanceof Throwable) {
			UserAppSession.showToast(theAct,
					"站点名称查询出错-" + ((Exception) res).getMessage());
			return;
		}

		queryingStationNames = TypeUtil.CastToList_S(res);
		if (queryingStationNames != null) {
			selectBusStation(queryingStationNames);
		}
	}

	/**
	 * 列表转换成数组
	 * 
	 * @param busStationList
	 * @return
	 */
	private String[] ListToArray(List<String> busStationList) {
		busStationArray = new String[busStationList.size()];
		for (int i = 0; i < busStationList.size(); i++) {
			busStationArray[i] = busStationList.get(i);
		}
		return busStationArray;
	}

	/**
	 * 选择站点
	 */
	public void selectBusStation(List<String> busStationList) {
		if (busStationList.size() > 0) {
			// 如果只有一条结果的话，直接使用
			if (busStationList.size() == 1) {
				String s = busStationList.get(0);
				if (s == null || s.length() == 0)
					UserAppSession.showToast(theAct, "查询不到相关站点");
				else
					theAct.doBusNameSelected(s);
				return;
			}
			AlertDialog.Builder builder = new AlertDialog.Builder(theAct);
			builder.setTitle("选择站点");
			builder.setSingleChoiceItems(ListToArray(busStationList),0, 
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int arg1) {
							dialog.dismiss();
							String s = busStationArray[arg1];
							theAct.doBusNameSelected(s);
						}
					});
			builder.create().show();
		} else {
			UserAppSession.showToast(theAct, "查询不到相关站点");
		}
	}

	public void refreshHistoryList() {
		historyHelper.refreshHistoryList(busStationDataIntf.histQueries);
	}

	public void callFavoriteMenu() {

		OnFavoriteChosenEvent evt=new OnFavoriteChosenEvent(){
			@Override
			public void onFavoriteChosen(Favorite fav) {
				if(fav==null)
					return;
				theAct.setCurrentCityAndStation(UserAppSession.cur_CityCode, fav.getName());
			}
		};
		FavoriteHelper.chooseFavorite(theAct, "BUSSTATION", UserAppSession.cur_CityCode, evt);
	}
}
