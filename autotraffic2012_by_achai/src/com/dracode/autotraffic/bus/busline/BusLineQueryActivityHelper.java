package com.dracode.autotraffic.bus.busline;

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

public class BusLineQueryActivityHelper {

	public BusLineDataIntf busLineDataIntf = null;
	public BusLineQueryActivity theAct = null;
	public QueryHistoryHelper historyHelper = new QueryHistoryHelper();

	public void init(BusLineQueryActivity act) {
		theAct = act;
		busLineDataIntf = BusLineDataIntf.getBusLineDataIntf();

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
		
		busLineDataIntf.loadData();
		BusLineHistInfo bi = busLineDataIntf.getLastBusInfo();
		String bus;
		if (bi != null)
			bus = bi.getBusName();
		else
			bus = "";
		theAct.setCurrentCityAndBus(UserAppSession.cur_CityCode, bus);

		refreshHistoryList();
	}

	public void recallHistBusQuery(int po) {
		BusLineHistInfo hi = busLineDataIntf.histQueries.get(po);
		callBusLineQuery(hi.getCityName(), hi.getLineName(),
				hi.getLineDirection());
	}

	public void callBusLineQuery(String city, String bus, String dir) {
		if ("".equals(bus)) {
			UserAppSession.showToast(theAct, "请输入查询线路名！");
			return;
		} else {
			Bundle bundle = new Bundle();
			bundle.putString("QueryBusName", bus);
			bundle.putString("QueryCityName", city);
			bundle.putString("QueryBusDir", dir);
			AppUtil.startActivity(theAct, BusLineResultActivity.class, false,
					bundle);
		}
	}

	private String[] busLineArray;
	public List<String> queryingLineNames = null;
	private ProgressDialog mProgressDlg = null;
	String queryCityName = "";
	String queryBusName = "";

	public void callBusNameQuery(String city, String bus) {
		if (bus == null || "".equals(bus.trim())) {
			UserAppSession.showToast(theAct, "请输入查询线路名！");
			return;
		}
		if (queryCityName.equals(city) && queryBusName.equals(bus)
				&& queryingLineNames != null) {
			selectBusLine(queryingLineNames);
			return;
		}
		queryingLineNames = null;
		queryCityName = city;
		queryBusName = bus;

		mProgressDlg = new ProgressDialog(theAct);
		mProgressDlg.setProgressStyle(android.R.style.Widget_ProgressBar_Large);
		mProgressDlg.setMessage("正在查找线路...");
		mProgressDlg.setCancelable(false);
		OnKeyListener ls = new OnKeyListener() {
			public boolean onKey(DialogInterface dialog, int keyCode,
					KeyEvent event) {
				if (keyCode == KeyEvent.KEYCODE_BACK) {
					busLineDataIntf.cancelQuery();
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
		BusLineNamesQueryTask mQueryTask;
		mQueryTask = new BusLineNamesQueryTask();
		mQueryTask.execute(queryCityName, queryBusName);
	}

	class BusLineNamesQueryTask extends AsyncTask<String, Integer, Object> {
		@Override
		protected Object doInBackground(String... params) {
			try {
				return busLineDataIntf.execBusLineNameQuery(params[0],
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
					"线路名称查询出错-" + ((Exception) res).getMessage());
			return;
		}

		queryingLineNames = TypeUtil.CastToList_S(res);
		if (queryingLineNames != null) {
			selectBusLine(queryingLineNames);
		}
	}

	/**
	 * 列表转换成数组
	 * 
	 * @param busLineList
	 * @return
	 */
	private String[] ListToArray(List<String> busLineList) {
		busLineArray = new String[busLineList.size()];
		for (int i = 0; i < busLineList.size(); i++) {
			busLineArray[i] = busLineList.get(i);
		}
		return busLineArray;
	}

	/**
	 * 选择线路
	 */
	public void selectBusLine(List<String> busLineList) {
		if (busLineList.size() > 0) {
			// 如果只有一条结果的话，直接使用
			if (busLineList.size() == 1) {
				String s = busLineList.get(0);
				if (s == null || s.length() == 0)
					UserAppSession.showToast(theAct, "查询不到相关线路");
				else
					theAct.doBusNameSelected(s);
				return;
			}
			AlertDialog.Builder builder = new AlertDialog.Builder(theAct);
			builder.setTitle("选择公交线路");
			builder.setSingleChoiceItems(ListToArray(busLineList),0, 
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int arg1) {
							dialog.dismiss();
							String s = busLineArray[arg1];
							theAct.doBusNameSelected(s);
						}
					});
			/*
			 * builder.setNegativeButton("取消", new
			 * DialogInterface.OnClickListener() {
			 * 
			 * @Override public void onClick(DialogInterface dialog, int which)
			 * { dialog.dismiss(); } });
			 */
			builder.create().show();
		} else {
			UserAppSession.showToast(theAct, "查询不到相关线路");
		}
	}

	public void refreshHistoryList() {
		historyHelper.refreshHistoryList(busLineDataIntf.histQueries);
	}

	public void callFavoriteMenu() {
		OnFavoriteChosenEvent evt=new OnFavoriteChosenEvent(){
			@Override
			public void onFavoriteChosen(Favorite fav) {
				if(fav==null)
					return;
				theAct.setCurrentCityAndBus(UserAppSession.cur_CityCode, fav.getName());
			}
		};
		FavoriteHelper.chooseFavorite(theAct, "BUSLINE", UserAppSession.cur_CityCode, evt);
	}
}
