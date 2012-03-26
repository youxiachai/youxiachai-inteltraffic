package com.dracode.autotraffic.bus.busstation;

import com.dracode.andrdce.ct.AppUtil;
import com.dracode.andrdce.ct.UserAppSession;
import com.dracode.autotraffic.bus.busline.BusLineResultActivity;
import com.dracode.autotraffic.common.helpers.FavoriteToolbarHelper;
import com.dracode.autotraffic.common.helpers.FavoriteToolbarHelper.OnGetFavFeedbackInfoEvent;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.DialogInterface.OnKeyListener;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.KeyEvent;

public class BusStationResultActivityHelper {
	public BusStationResultActivity theAct;

	private ProgressDialog mProgressDlg = null;

	public BusStationDataIntf busStationDataIntf = null;
	public String queryStationName = "";
	public String queryCityName = "";
	public String queryOption = "";
	
	public BusStationInfo curStationInfo;
	public FavoriteToolbarHelper favHelper = new FavoriteToolbarHelper();

	public void init(BusStationResultActivity act) { // 结果界面调用
		theAct = act;

		busStationDataIntf = BusStationDataIntf.getBusStationDataIntf();
		Bundle ext = theAct.getIntent().getExtras();
		queryStationName = ext.getString("QueryStationName");
		queryCityName = ext.getString("QueryCityName");
		if (queryCityName == null || "".equals(queryCityName))
			queryCityName = UserAppSession.cur_CityCode;
		queryOption = ext.getString("QueryOptions");
		if (queryOption == null)
			queryOption = "";

		theAct.setCurrentStationInfo(queryCityName, queryStationName);

		iniFavHelper();

		execBusStationQuery(false);
	}

	public void iniFavHelper() {
		OnGetFavFeedbackInfoEvent evt=new OnGetFavFeedbackInfoEvent(){
			@Override
			public String onGetFavType() {
				return "BUSSTATION";
			}
			@Override
			public String onGetFavName(int idx) {
				return queryStationName;
			}

			@Override
			public String onGetFavValue(int idx) {
				return queryCityName+"\n"+queryStationName;
			}

			@Override
			public String onGetShareText() {
				if(curStationInfo==null)
					return null;
				return curStationInfo.getShareText();
			}
			@Override
			public int onGetFavCount() {
				return 1;
			}
			
		};
		favHelper.init(theAct, evt);
	}
	public void execBusStationQuery(boolean forceRefresh) {
		mProgressDlg = new ProgressDialog(theAct);
		mProgressDlg.setProgressStyle(android.R.style.Widget_ProgressBar_Large);
		mProgressDlg.setMessage("正在执行查询...");
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
		BusStationQueryTask mQueryTask;
		mQueryTask = new BusStationQueryTask();
		mQueryTask.execute(queryCityName, queryStationName,
				forceRefresh ? "[REFRESH]" + queryOption : queryOption);
	}

	class BusStationQueryTask extends AsyncTask<String, Integer, Object> {
		@Override
		protected Object doInBackground(String... params) {
			try {
				return busStationDataIntf.execBusStationQuery(params[0],
						params[1], params[2]);
			} catch (Throwable ex) {
				return ex;
			}
		}

		@Override
		protected void onPostExecute(Object result) {
			if (mProgressDlg != null)
				mProgressDlg.dismiss();

			if (result instanceof BusStationInfo)
				curStationInfo = ((BusStationInfo) result);
			if (theAct != null)
				theAct.refreshBusStationView(result);
		}
	}

	public void showBusLineInfo(String city, String bus) {
		Bundle bundle = new Bundle();
		bundle.putString("QueryBusName", bus);
		bundle.putString("QueryCityName", city);
		bundle.putString("QueryBusDir", "0");
		bundle.putString("QueryOptions", "[NOHIST]");
		AppUtil.startActivity(theAct, BusLineResultActivity.class, false,
				bundle);
	}

}
