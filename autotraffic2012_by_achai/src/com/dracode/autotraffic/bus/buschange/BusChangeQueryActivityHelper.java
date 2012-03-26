package com.dracode.autotraffic.bus.buschange;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.dracode.andrdce.ct.AppUtil;
import com.dracode.andrdce.ct.CtRuntimeException;
import com.dracode.andrdce.ct.TypeUtil;
import com.dracode.andrdce.ct.UserAppSession;
import com.dracode.autotraffic.common.helpers.FavoriteHelper;
import com.dracode.autotraffic.common.helpers.QueryHistoryHelper;
import com.dracode.autotraffic.common.helpers.FavoriteHelper.Favorite;
import com.dracode.autotraffic.common.helpers.FavoriteHelper.OnFavoriteChosenEvent;
import com.dracode.autotraffic.common.helpers.QueryHistoryHelper.OnHistoryRecallEvent;
import com.dracode.autotraffic.common.map.GeoAddressHelper;
import com.dracode.autotraffic.common.map.LocationHelper;
import com.dracode.autotraffic.common.map.SelectMapPointActivity;
import com.dracode.autotraffic.common.map.GeoAddressHelper.OnAddressFoundEvent;
import com.dracode.autotraffic.common.map.LocationHelper.OnLocationFoundEvent;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.DialogInterface.OnKeyListener;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.KeyEvent;
import android.widget.EditText;

public class BusChangeQueryActivityHelper {

	public BusChangeDataIntf busChangeDataIntf = null;
	public BusChangeQueryActivity theAct = null;
	public QueryHistoryHelper historyHelper = new QueryHistoryHelper();

	public void init(BusChangeQueryActivity act) {
		theAct = act;
		busChangeDataIntf = new BusChangeDataIntf();
		OnHistoryRecallEvent evt = new OnHistoryRecallEvent() {

			@Override
			public void onRecallHistory(int idx) {
				recallHistBusQuery(idx);
			}

		};
		historyHelper.init(theAct, evt);

	}

	String lastCityCode = "";
	String lastQueryType = "";

	public void reloadData(String qType) {
		if (lastCityCode.equals(UserAppSession.cur_CityCode)
				&& lastQueryType.equals(qType)) {
			busChangeDataIntf.loadData();
			refreshHistoryList();
			return;
		}
		lastQueryType = qType;
		lastCityCode = UserAppSession.cur_CityCode;

		busChangeDataIntf.setQType(qType);
		busChangeDataIntf.loadData();
		BusChangeHistInfo si = busChangeDataIntf.getLastStationInfo();
		String start, end;
		if (si != null) {
			start = si.getStart();
			end = si.getEnd();
		} else {
			start = "";
			end = "";
		}
		theAct.setCurrentCityAndStation(UserAppSession.cur_CityName, start, end);
		refreshHistoryList();
	}

	public void recallHistBusQuery(int po) {
		BusChangeHistInfo hi = busChangeDataIntf.histQueries.get(po);
		callRouteQuery(hi.getCityName(), hi.getStart(), hi.getEnd(),
				hi.getFx(), hi.getFy(), hi.getTx(), hi.getTy());
	}

	/**
	 * 执行查询
	 */
	public void callRouteQuery(String city, String start, String end,
			String x1, String y1, String x2, String y2) {
		Bundle bundle = new Bundle();
		bundle.putString("QueryStart", start);
		bundle.putString("QueryEnd", end);
		bundle.putString("QueryStart_X", x1);
		bundle.putString("QueryStart_Y", y1);
		bundle.putString("QueryEnd_X", x2);
		bundle.putString("QueryEnd_Y", y2);
		bundle.putString("QueryCityName", city);
		bundle.putString("QueryType", busChangeDataIntf.getQType());
		AppUtil.startActivity(theAct, BusChangeResultActivity.class, false,
				bundle);
	}

	private String[] busStationArray;
	public List<Map<String, Object>> queryStartList = null;
	public List<Map<String, Object>> queryEndList = null;
	private ProgressDialog mProgressDlg = null;
	String queryCityName = "";
	String queryStart = "";
	String queryEnd = "";

	public void callStationNameQuery(String city, String start, String end) {
		if (start == null || "".equals(start.trim())) {
			UserAppSession.showToast(theAct, "请输入出发地！");
			return;
		}
		if (end == null || "".equals(end.trim())) {
			UserAppSession.showToast(theAct, "请输入目的地！");
			return;
		}
		queryStartList = null;
		queryEndList = null;
		queryCityName = city;
		queryStart = start;
		queryEnd = end;

		if (queryCityName.equals(city) && queryStart.equals(start)
				&& queryEnd.equals(end) && queryStartList != null
				&& queryEndList != null) {
			selectStart();
			return;
		}

		if (GeoAddressHelper.nameIsGeoAddress(queryStart))
			if (theAct.editText_Start.getTag() instanceof String)
				if (GeoAddressHelper.nameIsGeoAddress(queryEnd))
					if (theAct.editText_End.getTag() instanceof String) {
						selectStart();
						return;
					}

		mProgressDlg = new ProgressDialog(theAct);
		mProgressDlg.setProgressStyle(android.R.style.Widget_ProgressBar_Large);
		mProgressDlg.setMessage("正在获取地点...");
		mProgressDlg.setCancelable(false);
		OnKeyListener ls = new OnKeyListener() {
			public boolean onKey(DialogInterface dialog, int keyCode,
					KeyEvent event) {
				if (keyCode == KeyEvent.KEYCODE_BACK) {
					busChangeDataIntf.cancelQuery();
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
		mQueryTask.execute(queryCityName, queryStart, queryEnd);
	}

	class BusStationNamesQueryTask extends AsyncTask<String, Integer, Object> {
		@Override
		protected Object doInBackground(String... params) {
			try {
				return busChangeDataIntf.exePlaceNameQuery(params[0],
						params[1], params[2]);
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

	@SuppressWarnings("unchecked")
	public void refreshBusNameList(Object res) {
		if (UserAppSession.isTaskCancelMessage(res)) {
			UserAppSession.showToast(theAct, "查询被中止");
			return;
		}
		if (res instanceof Throwable) {
			UserAppSession.showToast(theAct,
					"地点查询出错-" + ((Exception) res).getMessage());
			return;
		}

		Map<String, Object> mapList = TypeUtil.CastToMap_SO(res);
		queryStartList = (List<Map<String, Object>>) mapList.get("FromList");
		queryEndList = (List<Map<String, Object>>) mapList.get("ToList");
		if (queryStartList != null && queryEndList != null) {
			selectStart();
		}
	}

	/**
	 * 列表转换成数组
	 * 
	 * @param busStationList
	 * @return
	 */
	private String[] ListToArray(List<Map<String, Object>> busStationList) {
		busStationArray = new String[busStationList.size()];
		for (int i = 0; i < busStationList.size(); i++) {
			busStationArray[i] = (String) busStationList.get(i).get("Name");
		}
		return busStationArray;
	}

	/**
	 * 选择出发地
	 */
	public void selectStart() {
		if (GeoAddressHelper.nameIsGeoAddress(queryStart))
			if (theAct.editText_Start.getTag() instanceof String) {
				queryStartList = new ArrayList<Map<String, Object>>();
				Map<String, Object> m = new HashMap<String, Object>();
				m.put("Name", queryStart);
				String s = (String) theAct.editText_Start.getTag();
				int p = s.indexOf(',');
				if (p > 0) {
					m.put("X", s.substring(0, p));
					m.put("Y", s.substring(p + 1));
				}
				queryStartList.add(m);
			}
		if (GeoAddressHelper.nameIsGeoAddress(queryEnd))
			if (theAct.editText_End.getTag() instanceof String) {
				queryEndList = new ArrayList<Map<String, Object>>();
				Map<String, Object> m = new HashMap<String, Object>();
				m.put("Name", queryEnd);
				String s = (String) theAct.editText_End.getTag();
				int p = s.indexOf(',');
				if (p > 0) {
					m.put("X", s.substring(0, p));
					m.put("Y", s.substring(p + 1));
				}
				queryEndList.add(m);
			}
		try {
			if (queryStartList.size() == 0) {
				if (queryEndList.size() == 0)
					throw new CtRuntimeException("未查询到出发和目的站点");
				throw new CtRuntimeException("未查询到出发站点");
			}
			if (queryEndList.size() == 0)
				throw new CtRuntimeException("未查询到目的站点");
		} catch (Exception ex) {
			UserAppSession.showToast(theAct, "地点查询出错-" + ex.getMessage());
		}
		if (queryStartList.size() > 0) {
			// 如果只有一条结果的话，直接使用
			if (queryStartList.size() == 1) {
				Map<String, Object> m = queryStartList.get(0);
				selectEnd(m);
				return;
			}
			AlertDialog.Builder builder = new AlertDialog.Builder(theAct);
			builder.setTitle("选择出发地");
			builder.setSingleChoiceItems(ListToArray(queryStartList), 0,
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int arg1) {
							dialog.dismiss();
							selectEnd(queryStartList.get(arg1));
						}
					});
			builder.create().show();
		} else {
			UserAppSession.showToast(theAct, "查询不到出发地点");
		}
	}

	/**
	 * 选择目的地
	 */
	public void selectEnd(final Map<String, Object> sm) {
		if (queryEndList.size() > 0) {
			// 如果只有一条结果的话，直接使用
			if (queryEndList.size() == 1) {
				Map<String, Object> em = queryEndList.get(0);
				callRouteQuery(queryCityName, (String) sm.get("Name"),
						(String) em.get("Name"), (String) sm.get("X"),
						(String) sm.get("Y"), (String) em.get("X"),
						(String) em.get("Y"));
				return;
			}
			AlertDialog.Builder builder = new AlertDialog.Builder(theAct);
			builder.setTitle("选择目的地");
			builder.setSingleChoiceItems(ListToArray(queryEndList), 0,
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int arg1) {
							dialog.dismiss();
							Map<String, Object> em = queryEndList.get(arg1);
							callRouteQuery(queryCityName,
									(String) sm.get("Name"),
									(String) em.get("Name"),
									(String) sm.get("X"), (String) sm.get("Y"),
									(String) em.get("X"), (String) em.get("Y"));
						}
					});
			builder.create().show();
		} else {
			UserAppSession.showToast(theAct, "查询不到相关地点");
		}
	}

	/**
	 * 刷新历史记录
	 */
	public void refreshHistoryList() {
		historyHelper.refreshHistoryList(busChangeDataIntf.histQueries);
	}

	public void showFavSelector(final int btnIdx) {
		OnFavoriteChosenEvent evt = new OnFavoriteChosenEvent() {
			@Override
			public void onFavoriteChosen(Favorite fav) {
				if (fav == null)
					return;
				if (fav.getName()
						.equals(GeoAddressHelper.ADDR_NAME_MY_POSITION))
					getMyLocation(btnIdx);
				else if (fav.getName().equals(
						GeoAddressHelper.ADDR_NAME_POINT_ON_MAP))
					showPoiSelector(btnIdx);
				else {
					EditText setValControl;
					if (btnIdx == 1)
						setValControl = theAct.editText_Start;
					else
						setValControl = theAct.editText_End;
					setValControl.setText(fav.getName());
					if (fav.getValue() != null)
						if (GeoAddressHelper.nameIsGeoAddress(fav.getName())) {
							String sp[] = fav.getValue().split("\n");
							if (sp.length == 4) {
								String xyInfo = sp[2] + "," + sp[3];
								setValControl.setTag(xyInfo);
							}
						}
				}

			}
		};
		String[] ns = { GeoAddressHelper.ADDR_NAME_MY_POSITION,
				GeoAddressHelper.ADDR_NAME_POINT_ON_MAP };
		FavoriteHelper.chooseFavoriteEx(theAct, "BUSCHANGE",
				UserAppSession.cur_CityCode, evt, ns);
	}

	/**
	 * 获取我的位置
	 * 
	 * @param btnIdx
	 */
	public void getMyLocation(final int btnIdx) {
		OnLocationFoundEvent evt = new OnLocationFoundEvent() {

			@Override
			public void onLocationFound(double lon, double lat, String addr) {
				EditText setValControl;
				if (btnIdx == 1)
					setValControl = theAct.editText_Start;
				else
					setValControl = theAct.editText_End;
				if (addr == null || addr.length() == 0)
					addr = GeoAddressHelper.ADDR_NAME_MY_POSITION;
				setValControl.setText(addr);
				String xyInfo = Double.toString(lon) + ","
						+ Double.toString(lat);
				setValControl.setTag(xyInfo);
			}

			@Override
			public void onCanceled() {
				UserAppSession.showToast(theAct,"操作已中止");
			}

			@Override
			public void onError(String msg) {
				UserAppSession.showToast(theAct,msg);
			}

		};
		LocationHelper.getMyLocation(theAct, evt);
	}

	protected void showPoiSelector(int btnIdx) {
		Intent intt = new Intent(theAct, SelectMapPointActivity.class);
		intt.putExtra("title", (btnIdx == 1 ? "选择出发地点" : "选择目的地点"));
		intt.putExtra("param", (btnIdx == 1 ? "startPoint" : "endPoint"));
		theAct.startActivityForResult(intt,
				SelectMapPointActivity.SEL_MAP_POINT_REQ_CODE);
	}

	public void onMapPointSelected(final String selTp, final double x,
			final double y) {

		OnAddressFoundEvent evt = new OnAddressFoundEvent() {
			@Override
			public void onAddressFound(String cityCode, String addrShort, String addrLong) {
				EditText setValControl;
				if (selTp.equals("startPoint"))
					setValControl = theAct.editText_Start;
				else
					setValControl = theAct.editText_End;
				setValControl.setText(addrShort);
				String xyInfo = Double.toString(x) + "," + Double.toString(y);
				setValControl.setTag(xyInfo);
			}

			@Override
			public void onCanceled() {
				UserAppSession.showToast(theAct,"操作已中止");
			}

			@Override
			public void onError(String msg) {
				UserAppSession.showToast(theAct,msg);
			}

		};
		GeoAddressHelper.getGeoAddress(theAct, x, y, evt);
	}

}
