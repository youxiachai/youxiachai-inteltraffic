package com.dracode.autotraffic.bus.busstation;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
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

public class BusStationDataIntf {

	public List<BusStationHistInfo> histQueries = new ArrayList<BusStationHistInfo>();

	private static BusStationDataIntf busStationDataIntf = null;

	public static BusStationDataIntf getBusStationDataIntf() {
		if (busStationDataIntf == null)
			busStationDataIntf = new BusStationDataIntf();
		return busStationDataIntf;
	}

	public void getHistoryQueries() {
		histQueries.clear();
		String url = BusStationHistInfo.getLoadCacheUrl(
				BusStationHistInfo.TABLE_BUSSTATION_QUERY_HISTORY,
				UserAppSession.cur_CityCode, 12);
		Object o = UserAppSession.cursession().executeCacheUrl(url);
		if (o instanceof List) {
			List<Map<String, Object>> ds = TypeUtil.CastToList_SO(o);
			for (Map<String, Object> m : ds) {
				BusStationHistInfo hq = new BusStationHistInfo();
				hq.loadFromMap(m);
				histQueries.add(hq);
			}
		}
	}

	public void addNewBusQueryHist(BusStationHistInfo hq) {
		List<BusStationHistInfo> oldQueries = null;
		for (BusStationHistInfo oq : histQueries) {
			if (oq.getId().equals(hq.getId())) {
				if (oldQueries == null)
					oldQueries = new ArrayList<BusStationHistInfo>();
				oldQueries.add(oq);
			}
		}
		if (oldQueries != null)
			for (BusStationHistInfo oq : oldQueries)
				histQueries.remove(oq);
		histQueries.add(0, hq);
		hq.saveToCache();
	}

	private void addToHistory(BusStationInfo si) {
		BusStationHistInfo hq = new BusStationHistInfo(si.cityName,
				si.stationName, UserAppSession.getCurTimeStr());
		this.addNewBusQueryHist(hq);
	}

	public void loadData() {
		getHistoryQueries();
	}

	public BusStationHistInfo getLastStationInfo() {
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

	public BusStationInfo execBusStationQuery(String city, String station,
			String op) {
		queryCanceled = false;
		station=station.trim();
		String st = station;
		station=BusStationInfo.getQueryStationName(city, station);

		// 加到查询历史中
		if (!op.contains("[NOHIST]")) {
			BusStationHistInfo hq = new BusStationHistInfo(city, station,
					UserAppSession.getCurTimeStr());
			addNewBusQueryHist(hq);
		}

		// 如果本地缓存未过期，直接使用
		BusStationInfo si;

		if (!op.contains("[REFRESH]")
				&& !BusStationInfo.isBusStationCacheExpired(city, station)) {
			si = new BusStationInfo(city, station);
			if (!si.loadFromCache(city, station)) // 加载缓存
				throw new CtRuntimeException("加载站点缓存失败");
		} else {

			// 获取查询站点的HASH值
			String rHash = BusStationInfo.getStationHash(city, station);

			// 生成URL
			String url = getBusStationDataUrl("BUS_STATION_QUERY");
			try {
				url = url + "&station="
						+ URLEncoder.encode(st, HTTP.UTF_8);
				url = url + "&city=" + URLEncoder.encode(city, HTTP.UTF_8);
				url = url + "&hash=" + URLEncoder.encode(rHash, HTTP.UTF_8);
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
			si = new BusStationInfo(city, station);

			try {
				int restp = rJson.getInt("type"); // 判断返回值类型
				if (restp == 1) { // 返回了全新站点信息（hash值无效）
					si.loadFromJsonObj(rJson); // 获取JSON结果
					si.saveToCache(); // 保存到缓存
				} else if (restp == 2) { // hash值有效，没数据返回，直接使用缓存
					if (!si.loadFromCache(city, station)) // 加载站点缓存
						throw new CtRuntimeException("加载站点缓存数据失败");
					si.saveToCache(); // 保存到缓存
				} else
					throw new CtRuntimeException("站点数据类型出错");
			} catch (JSONException e) {
				e.printStackTrace();
				throw new CtRuntimeException("站点数据解释出错");
			}
		}

		if (!op.contains("[NOHIST]"))
			addToHistory(si); // 再次保存查询历史
		return si;
	}

	public List<String> execBusStationNameQuery(String city, String stName) {
		queryCanceled = false;
		stName = stName.trim();
		String url = getBusStationDataUrl("BUS_STATION_NAME_QUERY");
		try {
			url = url + "&station=" + URLEncoder.encode(stName, HTTP.UTF_8);
			url = url + "&city=" + URLEncoder.encode(city, HTTP.UTF_8);
		} catch (UnsupportedEncodingException e) {
		}
		String enckey = NetUtil.getEncryptKey(url);
		url = url + "&verifyCode=" + enckey;

		queryingUrl = url;
		JSONObject rJson = UserAppSession.cursession().getUrlJsonData(url, null, UserAppSession.CAHCE_EXPIRE_TIME_DAY); 

		if (queryCanceled)
			throw new CtRuntimeCancelException("操作中止");

		if (rJson == null)
			throw new CtRuntimeException("未查询到任何信息");

		try {
			JSONArray jnames = rJson.getJSONArray("names");
			if (jnames.length() == 0)
				throw new CtRuntimeException("未查询到此站点信息");
			List<String> res = new ArrayList<String>();
			int len = jnames.length();
			for (int i = 0; i < len; i++) {
				String aname = jnames.getString(i);
				res.add(aname);
			}
			return res;
		} catch (JSONException e) {
			throw new CtRuntimeException("站点查询数据解释出错");
		}
	}

	private String getBusStationDataUrl(String tp) {
		if (tp.equals("BUS_STATION_QUERY"))
			return UserAppSession.getBusLineServerUrl()+"&type=3";
		// return UserSession.getBusStationServerUrl()
		// + "services/servlet/simple?mti=B_SSDZF&v=1.0";
		if (tp.equals("BUS_STATION_NAME_QUERY"))
			return UserAppSession.getBusLineServerUrl()+"&type=4";
		// return UserSession.getBusStationServerUrl()
		// + "services/servlet/simple?mti=B_SNQ&v=1.0";
		return null;
	}

}
