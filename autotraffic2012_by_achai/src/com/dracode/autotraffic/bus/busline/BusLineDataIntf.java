package com.dracode.autotraffic.bus.busline;

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

public class BusLineDataIntf {

	public List<BusLineHistInfo> histQueries = new ArrayList<BusLineHistInfo>();

	private static BusLineDataIntf busLineDataIntf = null;

	public static BusLineDataIntf getBusLineDataIntf() {
		if (busLineDataIntf == null)
			busLineDataIntf = new BusLineDataIntf();
		return busLineDataIntf;
	}

	public void getHistoryQueries() {
		histQueries.clear();
		String url = BusLineHistInfo.getLoadCacheUrl(
				BusLineHistInfo.TABLE_BUSLINE_QUERY_HISTORY,
				UserAppSession.cur_CityCode, 12);
		Object o = UserAppSession.cursession().executeCacheUrl(url);
		if (o instanceof List) {
			List<Map<String, Object>> ds = TypeUtil.CastToList_SO(o);
			for (Map<String, Object> m : ds) {
				BusLineHistInfo hq = new BusLineHistInfo();
				hq.loadFromMap(m);
				histQueries.add(hq);
			}
		}
	}

	public void addNewBusQueryHist(BusLineHistInfo hq) {
		List<BusLineHistInfo> oldQueries = null;
		for (BusLineHistInfo oq : histQueries) {
			if (oq.getId().equals(hq.getId())) {
				if (oldQueries == null)
					oldQueries = new ArrayList<BusLineHistInfo>();
				oldQueries.add(oq);
			}
		}
		if (oldQueries != null)
			for (BusLineHistInfo oq : oldQueries)
				histQueries.remove(oq);
		histQueries.add(0, hq);
		hq.saveToCache();
	}

	private void addToHistory(BusInfo bi) {
		BusLineHistInfo hq = new BusLineHistInfo(bi.cityCode, bi.lineName,
				bi.lineDirection, bi.endPlatName, bi.hashVal,
				UserAppSession.getCurTimeStr());
		this.addNewBusQueryHist(hq);
	}

	public void loadData() {
		getHistoryQueries();
	}

	public BusLineHistInfo getLastBusInfo() {
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

	public BusInfo execBusLineQuery(String city, String bus, String dir,
			String op) {
		queryCanceled = false;
		bus = BusInfo.getBusQueryName(bus);

		// 加到查询历史中
		if (!op.contains("[NOHIST]")) {
			BusLineHistInfo hq = new BusLineHistInfo(city, bus, dir, "", "",
					UserAppSession.getCurTimeStr());
			addNewBusQueryHist(hq);
		}
		
		boolean rt = BusInfo.realTimeInfoEnabled;
		if(op.contains("[NOGZJW]")) //不用广州交委数据
			rt=false;
		boolean useHist=true;
		if(rt)
		{
			useHist=false;
		}
		else
		{
			if(op.contains("[REFRESH]") )
					useHist=false;
		}

		// 如果实时到站功能被禁用，且缓存中有未过期的有效数据，则直接用缓存
		BusInfo bi;
		if (useHist
				&& !BusInfo.isBusLineCacheExpired(city, bus, dir)) {
			bi = new BusInfo(bus, city, dir);
			if (!bi.loadFromCache(city, bus, dir)) // 加载线路缓存
				throw new CtRuntimeException("加载线路缓存失败");
		} else {

			// 获取查询线路的HASH值
			String rHash = BusInfo.getBusLineHash(city, bus, dir);

			// 生成URL
			String url = getBusLineDataUrl("BUS_LINE_QUERY");
			try {
				url = url + "&city=" + URLEncoder.encode(city, HTTP.UTF_8);
				url = url + "&bus=" + URLEncoder.encode(bus, HTTP.UTF_8);
				url = url + "&dir=" + URLEncoder.encode(dir, HTTP.UTF_8);
				url = url + "&hash=" + URLEncoder.encode(rHash, HTTP.UTF_8);
				if(op.contains("[NOGZJW]"))
					url = url+ "&op=nogzjw";
				else if(rt)
					url = url + "&tk=" + Long.toString(System.currentTimeMillis());
			} catch (UnsupportedEncodingException e) {
			}
			// 生成加密密钥
			String enckey = NetUtil.getEncryptKey(url);
			url = url + "&verifyCode=" + enckey;

			// 执行URL
			queryingUrl = url;
			JSONObject rJson = UserAppSession.cursession().getUrlJsonData(url,
					null, BusInfo.realTimeInfoEnabled?UserAppSession.CAHCE_EXPIRE_TIME_IMMEDIA:UserAppSession.CAHCE_EXPIRE_TIME_DAY);

			if (queryCanceled)
				throw new CtRuntimeCancelException("操作中止");
			if (rJson == null)
				throw new CtRuntimeException("未查询到任何数据");

			bi = new BusInfo(bus, city, dir);

			try {
				int restp = 1;
				if(rJson.has("type"))
					restp = rJson.getInt("type"); // 判断返回值类型
				if (restp == 1) { // 返回了全部线路信息（hash值无效）
					bi.loadFromJsonObj(rJson); // 获取JSON结果
					bi.saveToCache(); // 保存到缓存
					if (rJson.has("EnvInfo"))
						BusInfo.setServerEnvInfo(rJson.getString("EnvInfo"));
					String stInfo = rJson.getString("RealTimeStr");
					bi.setStationInfoStr(stInfo); // 设置到站信息
				} else if (restp == 2) { // hash值有效，只返回了到站信息
					if (!bi.loadFromCache(city, bus, dir)) // 加载线路缓存
						throw new CtRuntimeException("加载线路缓存数据失败");
					bi.saveToCache(); // 保存到缓存
					String stInfo = rJson.getString("RealTimeStr");
					if (rJson.has("EnvInfo"))
						BusInfo.setServerEnvInfo(rJson.getString("EnvInfo"));
					bi.setStationInfoStr(stInfo); // 设置到站信息
				} else
					throw new CtRuntimeException("线路数据类型出错");
			} catch (JSONException e) {
				e.printStackTrace();
				throw new CtRuntimeException("线路数据解释出错");
			}
		}

		if (!op.contains("[NOHIST]"))
			addToHistory(bi); // 再次保存查询历史
		return bi;
	}

	public List<String> execBusLineNameQuery(String city, String bus) {
		queryCanceled = false;
		bus = bus.trim().toUpperCase();
		// if (!"NO".equals(trimNum))
		// bus = BusInfo.trimForBusLineNum(bus);
		String url = getBusLineDataUrl("BUS_NAME_QUERY");
		try {
			url = url + "&city=" + URLEncoder.encode(city, HTTP.UTF_8);
			url = url + "&bus=" + URLEncoder.encode(bus, HTTP.UTF_8);
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
				throw new CtRuntimeException("未查询到此线路信息");
			List<String> res = new ArrayList<String>();
			int len = jnames.length();
			for (int i = 0; i < len; i++) {
				String aname = jnames.getString(i);
				res.add(aname);
			}
			return res;
		} catch (JSONException e) {
			throw new CtRuntimeException("线路查询数据解释出错");
		}
	}

	private String getBusLineDataUrl(String tp) {
		if (tp.equals("BUS_LINE_QUERY"))
			return UserAppSession.getBusLineServerUrl()+"&type=1";
		// return UserSession.getBusLineServerUrl()
		// + "services/servlet/simple?mti=B_SSDZF&v=1.0";
		if (tp.equals("BUS_NAME_QUERY"))
			return UserAppSession.getBusLineServerUrl()+"&type=2";
		// return UserSession.getBusLineServerUrl()
		// + "services/servlet/simple?mti=B_SNQ&v=1.0";
		return null;
	}

}
