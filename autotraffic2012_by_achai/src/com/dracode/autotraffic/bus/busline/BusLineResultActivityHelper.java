package com.dracode.autotraffic.bus.busline;

import com.dracode.andrdce.ct.AppUtil;
import com.dracode.andrdce.ct.UserAppSession;
import com.dracode.autotraffic.bus.busstation.BusStationResultActivity;
import com.dracode.autotraffic.common.helpers.FavoriteToolbarHelper;
import com.dracode.autotraffic.common.helpers.FavoriteToolbarHelper.OnGetFavFeedbackInfoEvent;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.DialogInterface.OnKeyListener;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.KeyEvent;

public class BusLineResultActivityHelper {
	public BusLineResultActivity theAct;

	private ProgressDialog mProgressDlg = null;

	public BusLineDataIntf busLineDataIntf = null;
	public String queryBusName = "";
	public String queryCityName = "";
	public String queryDir = "0";
	public String queryOption = "";

	private BusInfo curBusInfo;
	
	public FavoriteToolbarHelper favHelper=new FavoriteToolbarHelper();

	public void init(BusLineResultActivity act) { // 结果界面调用
		theAct = act;

		busLineDataIntf = BusLineDataIntf.getBusLineDataIntf();
		Bundle ext = theAct.getIntent().getExtras();
		queryBusName = ext.getString("QueryBusName");
		queryCityName = ext.getString("QueryCityName");
		if (queryCityName == null || "".equals(queryCityName))
			queryCityName = UserAppSession.cur_CityCode;
		queryDir = ext.getString("QueryBusDir");
		if (queryDir == null || "".equals(queryDir))
			queryDir = "0";
		queryOption = ext.getString("QueryOptions");
		if(queryOption==null)
			queryOption="";

		theAct.setCurrentBusInfo(queryCityName, queryBusName, queryDir);
		
		iniFavHelper();

		execBusLineQuery(false);
	}

	public void iniFavHelper() {
		OnGetFavFeedbackInfoEvent evt=new OnGetFavFeedbackInfoEvent(){
			@Override
			public String onGetFavType() {
				return "BUSLINE";
			}
			@Override
			public String onGetFavName(int idx) {
				return queryBusName;
			}

			@Override
			public String onGetFavValue(int idx) {
				return queryCityName+"\n"+queryBusName+"\n"+queryDir;
			}

			@Override
			public String onGetShareText() {
				if(curBusInfo==null)
					return null;
				return curBusInfo.getShareText();
			}
			@Override
			public int onGetFavCount() {
				return 1;
			}
			
		};
		favHelper.init(theAct, evt);
	}

	protected void toggleBusLineDirQuery() {
		if (queryDir.equals("0"))
			queryDir = "1";
		else
			queryDir = "0";
		execBusLineQuery(false);
	}

	public void execBusLineQuery(boolean forceRefresh) {
		mProgressDlg = new ProgressDialog(theAct);
		mProgressDlg.setProgressStyle(android.R.style.Widget_ProgressBar_Large);
		mProgressDlg.setMessage("正在执行查询...");
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
		BusLineQueryTask mQueryTask;
		mQueryTask = new BusLineQueryTask();
		mQueryTask.execute(queryCityName, queryBusName, queryDir,
				forceRefresh ? "[REFRESH]"+queryOption : queryOption);
	}

	class BusLineQueryTask extends AsyncTask<String, Integer, Object> {

		@Override
		protected Object doInBackground(String... params) {
			try {
				return busLineDataIntf.execBusLineQuery(params[0], params[1],
						params[2], params[3]);
			} catch (Throwable ex) {
				return ex;
			}
		}

		@Override
		protected void onPostExecute(Object result) {
			if (mProgressDlg != null)
				mProgressDlg.dismiss();
			if(result instanceof BusInfo)
				curBusInfo=(BusInfo)result;
			if (theAct != null)
				theAct.refreshBusLineView(result);
		}
	}

	public void showBusStationInfo(String city, String station) {
		Bundle bundle = new Bundle();
		bundle.putString("QueryStationName", station);
		bundle.putString("QueryCityName", city);
		bundle.putString("QueryOptions", "[NOHIST]");
		AppUtil.startActivity(theAct, BusStationResultActivity.class, false,bundle);
	}

}
