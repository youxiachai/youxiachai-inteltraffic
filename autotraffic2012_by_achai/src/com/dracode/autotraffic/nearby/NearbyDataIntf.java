package com.dracode.autotraffic.nearby;

import java.util.List;
import java.util.Map;

import com.dracode.andrdce.ct.UserAppSession;
import com.dracode.andrdce.ctact.CvtDataHelper;
import com.dracode.andrdce.ctact.CvtDataHelper.OnCvtDataEvent;

public class NearbyDataIntf {


	public CvtDataHelper cvtHelper = new CvtDataHelper();
	public NearbyQueryActivityHelper nearbyQueryHelper;
	public NearbyQueryActivity theAct = null;

	public List<Map<String, Object>> sortList;
	public Map<String, Object> iconList;

	public void init(NearbyQueryActivity act,
			NearbyQueryActivityHelper nearbyHlp) {
		theAct = act;
		nearbyQueryHelper = nearbyHlp;

		// ["ID","PID","NAME","ENAME","CODE","POITYPE","IMGURL"]
		cvtHelper.init(theAct, 610021);
		cvtHelper.cacheExpireTm = UserAppSession.CAHCE_EXPIRE_TIME_MONTH;
		UserAppSession
				.LogD("expireTm" + Long.toString(cvtHelper.cacheExpireTm));
	}

	/**
	 * 获取分类列表
	 */
	public void getSortList(boolean refresh) {
		if (!refresh) {
			if (cvtHelper.tryLoadFromCache()) {
				sortList = cvtHelper.getCurrentDataListSO();
				iconList = cvtHelper.currentImgs;
				nearbyQueryHelper.fillData(sortList);
				return;
			}
		}

		OnCvtDataEvent evt = new OnCvtDataEvent() {
			@Override
			public void onDataCanceled() {
				UserAppSession.showToast(theAct, "操作被中止");
			}

			@Override
			public void onDataError(String msg) {
				UserAppSession.showToast(theAct, msg);
			}

			@Override
			public void onDataLoaded(CvtDataHelper cvth) {
				sortList = cvtHelper.getCurrentDataListSO();
				iconList = cvtHelper.currentImgs;
				nearbyQueryHelper.fillData(sortList);
			}

			@Override
			public void afterFetchData() {
			}

		};
		cvtHelper.setCvtDataEvt(evt);
		cvtHelper.loadingMessage = "正在获取分类数据...";
		if (refresh)
			cvtHelper.refreshData();
		else
			cvtHelper.loadData();

	}

}
