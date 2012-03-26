package com.dracode.autotraffic.bus.buschange;

import com.dracode.andrdce.ct.UserAppSession;
import com.dracode.autotraffic.common.helpers.FavoriteToolbarHelper;
import com.dracode.autotraffic.common.helpers.FavoriteToolbarHelper.OnGetFavFeedbackInfoEvent;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.DialogInterface.OnKeyListener;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.KeyEvent;

public class BusChangeResultActivityHelper {
	public BusChangeResultActivity theAct;

	private ProgressDialog mProgressDlg = null;

	public BusChangeDataIntf busStationDataIntf = null;
	public String queryStart = "";
	public String queryEnd = "";
	public String queryStart_X = "";
	public String queryStart_Y = "";
	public String queryEnd_X = "";
	public String queryEnd_Y = "";
	public String queryCityName = "";
	public String queryType = "";
	public String isSwitch = "false";

	private BusChangeInfo curBusChangeInfo;
	public FavoriteToolbarHelper favHelper = new FavoriteToolbarHelper();

	public void init(BusChangeResultActivity act) { // 结果界面调用
		theAct = act;

		busStationDataIntf = new BusChangeDataIntf();
		
		Bundle ext = theAct.getIntent().getExtras();
		queryStart = ext.getString("QueryStart");
		queryEnd = ext.getString("QueryEnd");
		queryStart_X = ext.getString("QueryStart_X");
		queryStart_Y = ext.getString("QueryStart_Y");
		queryEnd_X = ext.getString("QueryEnd_X");
		queryEnd_Y = ext.getString("QueryEnd_Y");
		queryCityName = ext.getString("QueryCityName");
		if (queryCityName == null || "".equals(queryCityName))
			queryCityName = UserAppSession.cur_CityCode;
		queryType = ext.getString("QueryType");
		if (queryType == null || "".equals(queryType))
			queryType="BUSCHANGE";

		busStationDataIntf.setQType(queryType);
		
		theAct.setCurrentChangeInfo(queryCityName, queryStart, queryEnd);
		iniFavHelper();

		execBusChangeQuery();
	}

	public void iniFavHelper() {
		OnGetFavFeedbackInfoEvent evt = new OnGetFavFeedbackInfoEvent() {
			@Override
			public String onGetFavType() {
				return "BUSCHANGE";
			}

			@Override
			public String onGetFavName(int idx) {
				if (idx == 0)
					return queryStart;
				else
					return queryEnd;
			}

			@Override
			public String onGetFavValue(int idx) {
				if (idx == 0)
					return queryCityName + "\n" + queryStart + "\n"
							+ queryStart_X + "\n" + queryStart_Y;
				else
					return queryCityName + "\n" + queryEnd + "\n" + queryEnd_X
							+ "\n" + queryEnd_Y;
			}

			@Override
			public String onGetShareText() {
				if (curBusChangeInfo == null)
					return null;
				return curBusChangeInfo.getShareText();
			}

			@Override
			public int onGetFavCount() {
				return 2;
			}

		};
		favHelper.init(theAct, evt);
	}

	public void execBusChangeQuery() {
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

		BusChangeQueryTask mQueryTask;
		mQueryTask = new BusChangeQueryTask();
		mQueryTask.execute(queryCityName, queryStart, queryEnd, queryStart_X,
				queryStart_Y, queryEnd_X, queryEnd_Y, isSwitch);
	}

	/**
	 * 异步获取数据
	 */
	class BusChangeQueryTask extends AsyncTask<String, Integer, Object> {
		@Override
		protected Object doInBackground(String... params) {
			try {
				return busStationDataIntf.exeBusChangeQuery(params[0],
						params[1], params[2], params[3], params[4], params[5],
						params[6], params[7]);
			} catch (Throwable ex) {
				ex.printStackTrace();
				return ex;
			}
		}

		@Override
		protected void onPostExecute(Object result) {
			if (result instanceof BusChangeInfo)
				curBusChangeInfo = (BusChangeInfo) result;
			if (theAct != null)
				theAct.refreshBusChangeView(result);
			if (mProgressDlg != null)
				mProgressDlg.dismiss();
		}
	}

	public void toggleQueryDirection() {
		String t;
		t = queryStart;
		queryStart = queryEnd;
		queryEnd = t;

		t = queryStart_X;
		queryStart_X = queryEnd_X;
		queryEnd_X = t;

		t = queryStart_Y;
		queryStart_Y = queryEnd_Y;
		queryEnd_Y = t;

		theAct.setCurrentChangeInfo(queryCityName, queryStart, queryEnd);

		execBusChangeQuery();
	}

}
