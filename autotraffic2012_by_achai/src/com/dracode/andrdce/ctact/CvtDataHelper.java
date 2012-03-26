package com.dracode.andrdce.ctact;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.dracode.andrdce.ct.CtRuntimeCancelException;
import com.dracode.andrdce.ct.CtRuntimeException;
import com.dracode.andrdce.ct.NetUtil;
import com.dracode.andrdce.ct.UserAppSession;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.DialogInterface.OnKeyListener;
import android.os.AsyncTask;
import android.view.KeyEvent;

public class CvtDataHelper {
	public static final String CVT_CACHE_TABLENAME = "ctsys_cvt";

	public Context theCtx;

	public int cvtId; // DCE服务器上的对应的目录列表ID

	public String cvtTitle;
	public int pcvtId;
	public int ctId;
	public int cvId;

	public String hashVal;
	public String cvtColWidths;
	public String sorCol;
	public String sortWay;
	public int pid;
	public int treePid;
	public int rid;
	public int treeItemId;
	public int pageCount = 0;
	public int pageSize = 2000; // 分页大小
	public int curPageSize = 0;
	public int pageIndex = 0;

	public String extraParams; // 额外的搜索过滤条件（需要在DCE的列表SQL中配置参数），如：&addparam=P_CITYCODE:020&addparam=P_TYPEID:123

	public int colCount;
	public List<String> cvtColNames = new ArrayList<String>();

	@SuppressWarnings("rawtypes")
	protected List currentDataList = new ArrayList(); // 返回数据
	public Map<String, Object> currentImgs=null; //对应的图片数据（如果有的话）

	protected boolean refreshMode = false;
	public String loadingTitle = "提示";
	public String loadingMessage = "正在加载...";
	public ProgressDialog mProgressDlg = null;
	public long cacheExpireTm = UserAppSession.CAHCE_EXPIRE_TIME_SHORT; // 缓存失效时间，默认5分钟
	protected OnCvtDataEvent cvtDataEvt;

	public void init(Context ctx, int cvtId) {
		theCtx = ctx;
		this.cvtId = cvtId;
	}

	public boolean tryLoadFromCache() { // 尝试直接从缓存加载
		try {
			String url = getDataUrl();
			JSONObject res = NetUtil.getUrlJsonDataEx(CVT_CACHE_TABLENAME,
					getCvtCacheType(cvtId), url, null,
					UserAppSession.cursession(), cacheExpireTm, 1);
			if (res == null)
				return false;
			JSONObject data = (JSONObject) res;
			parseJsonData(data);
			//再重新从缓存加载图片
			currentImgs= CtImageHelper.loadImgsFromCache(currentDataList);
			return true;
		} catch (Throwable ex) {
			return false;
		}
	}

	public static void removeCacheOfCvtId(int cvtId) { // 清空缓存
		UserAppSession.cursession().removeFromCache(CVT_CACHE_TABLENAME,
				getCvtCacheType(cvtId), null);
	}

	private static String getCvtCacheType(int cvtId) {
		return "cvtId=" + Integer.toString(cvtId);
	}

	// 线程加载的回调事件
	public static interface OnCvtDataEvent {
		public void onDataCanceled(); // 取消

		public void onDataError(String msg);// 出错

		public void afterFetchData();// 获取完数据事件（下载线程中触发，线程退出前调用）

		public void onDataLoaded(CvtDataHelper cvth);// 数据加载完成（主线程触发）
	}

	public void loadNextPage() {
		loadDataOfPageNo(pageIndex + 1);
	}

	public void loadPrevPage() {
		loadDataOfPageNo(pageIndex - 1);
	}

	public void loadDataOfPageNo(int pg) {
		if (pg < 1 || pg > this.pageCount || pg == this.pageIndex)
			return;
		this.pageIndex = pg;
		loadData();
	}

	public void loadData() {// 从线程中加载
		loadDataEx(false);
	}

	public void refreshData() {
		loadDataEx(true);
	}

	protected void loadDataEx(boolean bRefresh) {
		queryCanceled = false;
		queryingUrl = null;
		refreshMode = bRefresh;

		mProgressDlg = new ProgressDialog(theCtx);
		mProgressDlg.setTitle(this.loadingTitle);
		mProgressDlg.setProgressStyle(android.R.style.Widget_ProgressBar_Large);
		mProgressDlg.setMessage(this.loadingMessage);
		mProgressDlg.setCancelable(false);
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

		CvtLoader loader = new CvtLoader();
		loader.execute();
	}

	private boolean queryCanceled = false;
	private String queryingUrl = null;

	class CvtLoader extends AsyncTask<String, Integer, Object> {
		@Override
		protected Object doInBackground(String... params) {
			try {
				return doGetCvtListData();
			} catch (Throwable ex) {
				ex.printStackTrace();
				return ex;
			}
		}

		@Override
		protected void onPostExecute(Object result) {
			if (mProgressDlg != null)
				mProgressDlg.dismiss();
			doCvtResultProcess(result);
		}
	}

	public Object doGetCvtListData() {
		String url = getDataUrl();
		queryingUrl = url;

		int cacheMode = 5;
		if (refreshMode)
			cacheMode = 4;
		JSONObject res = NetUtil.getUrlJsonDataEx(CVT_CACHE_TABLENAME,
				getCvtCacheType(cvtId), url, null, UserAppSession.cursession(),
				cacheExpireTm, cacheMode);

		if (queryCanceled)
			throw new CtRuntimeCancelException("操作中止");

		if (res == null)
			throw new CtRuntimeException("未查询到任何信息");

		try {
			JSONObject data = (JSONObject) res;
			parseJsonData(data);
		} catch (Throwable ex) {
			String err = "列表数据解释出错：" + ex.getMessage();
			throw new CtRuntimeException(err);
		}

		//遍历数据列表，将其中的图片URL下载保存
		CtImageHelper cih = new CtImageHelper();
		cih.downloadImgsToCache(currentDataList);
		//再重新从缓存加载图片
		this.currentImgs= cih.doLoadImgsFromCache(currentDataList);
		
		if (cvtDataEvt != null)
			cvtDataEvt.afterFetchData();
		return res;
	}

	public void doCvtResultProcess(Object res) {
		if (UserAppSession.isTaskCancelMessage(res)) {
			if (cvtDataEvt != null)
				cvtDataEvt.onDataCanceled();
			else
				UserAppSession.showToast(theCtx, "操作被中止");
			return;
		}
		if (res instanceof Throwable) {
			String err = "执行出错-" + ((Exception) res).getMessage();
			if (cvtDataEvt != null)
				cvtDataEvt.onDataError(err);
			else
				UserAppSession.showToast(theCtx, err);
			return;
		}

		if (cvtDataEvt != null)
			cvtDataEvt.onDataLoaded(this);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void parseJsonData(JSONObject data) throws JSONException {
		currentDataList.clear();
		cvtColNames.clear();

		hashVal = getJSONString(data, "hash");
		cvtTitle = getJSONString(data, "CVT_TITLE");
		pcvtId = getJSONInt(data, "pcvtid");
		ctId = getJSONInt(data, "ctid");
		if (data.has("cvid") && !data.isNull("cvid"))
			cvId = getJSONInt(data, "cvid");
		else
			cvId = 0;
		cvtColWidths = getJSONString(data, "CVT_COLWIDTHS");
		sorCol = getJSONString(data, "sorCol");
		sortWay = getJSONString(data, "sortWay");
		pid = getJSONInt(data, "pid");
		treePid = getJSONInt(data, "treepid");
		rid = getJSONInt(data, "rid");
		treeItemId = getJSONInt(data, "treeitemid");
		pageCount = getJSONInt(data, "pageCount");
		pageSize = getJSONInt(data, "pageSize");
		curPageSize = getJSONInt(data, "curPageSize");
		pageIndex = getJSONInt(data, "pageIndex");

		colCount = 0;
		JSONArray jList = data.getJSONArray("colNameList");
		if (jList != null) {
			colCount = jList.length();
			for (int i = 0; i < colCount; i++)
				cvtColNames.add(jList.getString(i));
		}

		jList = data.getJSONArray("itemList");
		if (jList != null)
			for (int i = 0; i < jList.length(); i++) {
				JSONArray jitem = jList.getJSONArray(i);
				if (jitem.length() != colCount)
					throw new CtRuntimeException("数组长度不一致");
				Map m = new ArrayMap();
				for (int j = 0; j < colCount; j++)
					m.put(cvtColNames.get(j), jitem.getString(j));
				currentDataList.add(m);
			}
	}

	private int getJSONInt(JSONObject data, String nam) {
		if (data.has(nam))
			try {
				return data.getInt(nam);
			} catch (JSONException e) {
				e.printStackTrace();
				return 0;
			}
		else
			return 0;
	}

	private String getJSONString(JSONObject data, String nam)
			throws JSONException {
		String res = null;
		if (data.has(nam))
			res = data.getString(nam);
		if (res == null)
			res = "";
		return res;
	}

	/**
	 * 获取接口地址
	 * 
	 * @param tp
	 * @return
	 */
	public String getDataUrl() {
		String url = UserAppSession.getBaServerCvtUrl() + "&cvtid="
				+ Integer.toString(cvtId) + "&isWM=1";
		url += "&pgsize=" + Integer.toString(pageSize);
		if (pageIndex >= 1)
			url += "&page=" + Integer.toString(pageIndex);

		String param = extraParams;
		if (param == null)
			param = "";
		else if (param.length() > 0 && !param.startsWith("&"))
			param = "&" + param;
		url += param;
		return url;
	}

	@SuppressWarnings("rawtypes")
	public List getCurrentDataList() {
		return currentDataList;
	}

	@SuppressWarnings("unchecked")
	public List<Map<String, String>> getCurrentDataListSS() {
		return currentDataList;
	}

	@SuppressWarnings("unchecked")
	public List<Map<String, Object>> getCurrentDataListSO() {
		return currentDataList;
	}

	public OnCvtDataEvent getCvtDataEvt() {
		return cvtDataEvt;
	}

	public void setCvtDataEvt(OnCvtDataEvent cvtDataEvt) {
		this.cvtDataEvt = cvtDataEvt;
	}

}
