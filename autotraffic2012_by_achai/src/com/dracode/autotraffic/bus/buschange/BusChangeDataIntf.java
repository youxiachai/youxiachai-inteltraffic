package com.dracode.autotraffic.bus.buschange;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.protocol.HTTP;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.dracode.andrdce.ct.CtRuntimeCancelException;
import com.dracode.andrdce.ct.CtRuntimeException;
import com.dracode.andrdce.ct.NetUtil;
import com.dracode.andrdce.ct.TypeUtil;
import com.dracode.andrdce.ct.UserAppSession;

public class BusChangeDataIntf {

	public List<BusChangeHistInfo> histQueries = new ArrayList<BusChangeHistInfo>();

	private String qType = "BUSCHANGE";

	private static BusChangeDataIntf busChangeDataIntf = null;

	public static BusChangeDataIntf getBusChangeDataIntf() {
		if (busChangeDataIntf == null)
			busChangeDataIntf = new BusChangeDataIntf();
		return busChangeDataIntf;
	}

	public String getQType() {
		return qType;
	}

	public void setQType(String qType) {
		this.qType = qType;
	}

	public static String getLoadCacheUrl(String tbName, String city,
			String qType) {
		return "sqldb://" + tbName + "/get?city="
				+ UserAppSession.urlEncode(city) + "&param7="
				+ UserAppSession.urlEncode(qType)
				+ "&MaxRowCount=8&OrderBy=query_time desc";
	}

	public void getHistoryQueries() {
		histQueries.clear();
		String url = getLoadCacheUrl(
				BusChangeHistInfo.TABLE_BUSSTATION_QUERY_HISTORY,
				UserAppSession.cur_CityCode, qType);

		Object o = UserAppSession.cursession().executeCacheUrl(url);
		if (o instanceof List) {
			List<Map<String, Object>> ds = TypeUtil.CastToList_SO(o);
			for (Map<String, Object> m : ds) {
				BusChangeHistInfo hq = new BusChangeHistInfo();
				hq.loadFromMap(m);
				histQueries.add(hq);
			}
		}
	}

	public void addNewBusQueryHist(BusChangeHistInfo hq) {
		List<BusChangeHistInfo> oldQueries = null;
		for (BusChangeHistInfo oq : histQueries) {
			if (oq.getId().equals(hq.getId())) {
				if (oldQueries == null)
					oldQueries = new ArrayList<BusChangeHistInfo>();
				oldQueries.add(oq);
			}
		}
		if (oldQueries != null)
			for (BusChangeHistInfo oq : oldQueries)
				histQueries.remove(oq);
		histQueries.add(0, hq);
		hq.saveToCache();
	}

	private void addToHistory(BusChangeInfo si) {
		BusChangeHistInfo hq = new BusChangeHistInfo(si.cityName, si.start,
				si.end, si.qtype, si.fx, si.fy, si.tx, si.ty,
				UserAppSession.getCurTimeStr());
		this.addNewBusQueryHist(hq);
	}

	public void loadData() {
		getHistoryQueries();
	}

	public BusChangeHistInfo getLastStationInfo() {
		if (histQueries.size() > 0)
			return histQueries.get(0);
		else
			return null;
	}

	private boolean queryCanceled = false;
	private String queryingUrl = null;

	public void cancelQuery() {
		queryCanceled = true;
		if (queryingUrl != null) {
			NetUtil.stopNetUrl(queryingUrl);
		}
	}

	public BusChangeInfo exeBusChangeQuery(String city, String start,
			String end, String fx, String fy, String tx, String ty,
			String isSwitch) {
		queryCanceled = false;
		// city = City.getCityCode(city);
		start = BusChangeInfo.getQueryStart(start);
		// 获取查询站点的HASH值
		String rHash = BusChangeInfo.getBusChangeHash(city, start, end, qType,
				fx, fy, tx, ty);

		BusChangeInfo si = new BusChangeInfo(city, start, end, qType, fx, fy,
				tx, ty);

		if (!BusChangeInfo.isBusChangeCacheExpired(city, start, end, qType, fx,
				fy, tx, ty)) {
			if (!si.loadFromCache(city, start, end, qType, fx, fy, tx, ty)) // 加载缓存
				throw new CtRuntimeException("加载换乘方案缓存失败");
		} else {

			// 生成URL
			String url = getDataUrl("BUS_ROUTE_QUERY");
			try {
				if (qType.equals("BUSROUTE"))
					url = url + "&subType=2";
				else if (qType.equals("METROROUTE"))
					url = url + "&subType=3";
				else
					url = url + "&subType=1";
				url = url + "&city=" + URLEncoder.encode(city, HTTP.UTF_8);
				url = url + "&fx=" + URLEncoder.encode(fx, HTTP.UTF_8);
				url = url + "&fy=" + URLEncoder.encode(fy, HTTP.UTF_8);
				url = url + "&tx=" + URLEncoder.encode(tx, HTTP.UTF_8);
				url = url + "&ty=" + URLEncoder.encode(ty, HTTP.UTF_8);
				url = url + "&hash=" + URLEncoder.encode(rHash, HTTP.UTF_8);
				url = url + "&tk=" + Long.toString(System.currentTimeMillis());
			} catch (UnsupportedEncodingException e) {
			}
			// 生成加密密钥
			String enckey = NetUtil.getEncryptKey(url);
			url = url + "&verifyCode=" + enckey;

			// 执行URL
			queryingUrl = url;
			JSONObject rJson = UserAppSession.cursession().getUrlJsonData(url,
					null, UserAppSession.CAHCE_EXPIRE_TIME_DAY);

			if (queryCanceled)
				throw new CtRuntimeCancelException("操作中止");
			if (rJson == null)
				throw new CtRuntimeException("未查询到任何数据");

			try {
				int restp = rJson.getInt("type"); // 判断返回值类型
				if (restp == 1) { // 返回了全新站点信息（hash值无效）
					if(!rJson.has("BusList"))
						throw new CtRuntimeException("未查询到换乘方案");
					JSONArray list = rJson.getJSONArray("BusList");
					if (list.length() == 0)
						throw new CtRuntimeException("未查询到换乘方案数据");
					si.loadExchangePlanFromJson(list); // 获取JSON结果
					si.saveToCache(); // 保存到缓存
				} else if (restp == 2) { // hash值有效，没数据返回，直接使用缓存
					if (!si.loadFromCache(city, start, end, qType, fx, fy, tx,
							ty)) // 加载换乘缓存
						throw new CtRuntimeException("加载公交换乘缓存数据失败");
					si.saveToCache(); // 保存到缓存
				} else {
					throw new CtRuntimeException("返回类型出错");
				}
			} catch (JSONException e) {
				throw new CtRuntimeException("换乘方案数据解析出错");
			}
		}
		// 加到查询历史中
		if ("false".equals(isSwitch)) {
			this.addToHistory(si);
		}

		return si;
	}

	public Map<String, Object> exePlaceNameQuery(String city, String start,
			String end) {
		queryCanceled = false;
		// city = City.getCityCode(city);
		start = start.trim();
		end = end.trim();
		String url = getDataUrl("PLACE_NAME_QUERY");
		try {
			url = url + "&city=" + URLEncoder.encode(city, HTTP.UTF_8);
			url = url + "&from=" + URLEncoder.encode(start, HTTP.UTF_8);
			url = url + "&to=" + URLEncoder.encode(end, HTTP.UTF_8);
		} catch (UnsupportedEncodingException e) {
		}
		String enckey = NetUtil.getEncryptKey(url);
		url = url + "&verifyCode=" + enckey;

		queryingUrl = url;
		JSONObject rJson = UserAppSession.cursession().getUrlJsonData(url,
				null, UserAppSession.CAHCE_EXPIRE_TIME_DAY);

		if (queryCanceled)
			throw new CtRuntimeCancelException("操作中止");

		if (rJson == null)
			throw new CtRuntimeException("未查询到任何信息");

		Map<String, Object> mapList = new HashMap<String, Object>();
		try {
			JSONArray startList = rJson.getJSONArray("FromList");
			JSONArray endList = rJson.getJSONArray("ToList");
			// 获取出发地列表
			List<Map<String, Object>> res = new ArrayList<Map<String, Object>>();
			int len = startList.length();
			for (int i = 0; i < len; i++) {
				Map<String, Object> m = new HashMap<String, Object>();
				JSONObject o = startList.getJSONObject(i);
				m.put("Name", o.getString("Name"));
				m.put("X", o.getString("X"));
				m.put("Y", o.getString("Y"));
				res.add(m);
			}
			mapList.put("FromList", res);

			// 获取目的地列表
			res = new ArrayList<Map<String, Object>>();
			len = endList.length();
			for (int i = 0; i < len; i++) {
				Map<String, Object> m = new HashMap<String, Object>();
				JSONObject o = endList.getJSONObject(i);
				m.put("Name", o.getString("Name"));
				m.put("X", o.getString("X"));
				m.put("Y", o.getString("Y"));
				res.add(m);
			}
			mapList.put("ToList", res);

			return mapList;
		} catch (JSONException e) {
			throw new CtRuntimeException("地点查询数据解释出错");
		}
	}

	/**
	 * 获取接口地址
	 * 
	 * @param tp
	 * @return
	 */
	private String getDataUrl(String tp) {
		if (tp.equals("BUS_ROUTE_QUERY"))
			return UserAppSession.getBusLineServerUrl() + "&type=5";
		// return UserSession.getBusStationServerUrl() +
		// "services/servlet/simple?mti=B_SSDZF&v=1.0";
		if (tp.equals("PLACE_NAME_QUERY"))
			return UserAppSession.getBusLineServerUrl() + "&type=6";
		// return UserSession.getBusStationServerUrl() +
		// "services/servlet/simple?mti=B_SNQ&v=1.0";
		return null;
	}

}
