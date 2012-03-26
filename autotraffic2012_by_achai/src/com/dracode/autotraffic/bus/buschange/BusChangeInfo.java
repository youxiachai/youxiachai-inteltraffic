package com.dracode.autotraffic.bus.buschange;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.dracode.andrdce.ct.CtRuntimeException;
import com.dracode.andrdce.ct.TypeUtil;
import com.dracode.andrdce.ct.UserAppSession;
import com.dracode.autotraffic.common.map.GeoAddressHelper;

/**
 * 查询结果页
 * 
 * @author Figo.Gu
 */
public class BusChangeInfo {
	// BusChangeInfo
	// -----------------
	// cityName String
	// start String
	// end String
	// hash String
	// fx String
	// fy String
	// tx String
	// ty String
	public static final String TABLE_BUS_CHANGE_INFO = "bus_change_info";

	public String cityName;
	public String start;
	public String end;
	public String qtype;
	public String hashVal;
	public String fx;
	public String fy;
	public String tx;
	public String ty;

	public List<ExchangePlan> exchangePlans;

	public static class ExchangePlan {
		// ExchangePlan
		// ----------------------
		// footEndLength String
		// expense String
		// bounds String
		// segmentList List

		public String footEndLength; // 换乘方案结束后，步行到终点距离-

		public String expense; // 换乘方案价格

		public List<Segment> segmentList = new ArrayList<Segment>(); // 换乘段列表，每段代表一次换乘

		public String bounds; // bounds值，当前返回的POI点统一个矩形框内，bounds为矩形框的左上右下坐标对

		public void loadFromJsonObj(JSONObject jsObj) throws JSONException {
			footEndLength = jsObj.getString("FootEndLength");
			expense = jsObj.getString("Expense");
			bounds = jsObj.getString("Bounds");
			segmentList.clear();
			JSONArray jsaSegmentList = jsObj.getJSONArray("SegmentList");
			for (int i = 0; i < jsaSegmentList.length(); i++) {
				JSONArray jsSegment = jsaSegmentList.getJSONArray(i);
				Segment vSegment = new Segment();
				vSegment.loadFromJsonArr(jsSegment);
				segmentList.add(vSegment);
			}
		}

		public void saveToJsonObj(JSONObject jsObj) throws JSONException {
			jsObj.put("FootEndLength", footEndLength);
			jsObj.put("Expense", expense);
			jsObj.put("Bounds", bounds);
			JSONArray jsaSegmentList = new JSONArray();
			for (int i = 0; i < segmentList.size(); i++) {
				JSONArray jsSegment = new JSONArray();
				segmentList.get(i).saveToJsonArr(jsSegment);
				jsaSegmentList.put(jsSegment);
			}
			jsObj.put("SegmentList", jsaSegmentList);
		}

		public String getShareText() {
			String s = "";
			for (int i = 0; i < segmentList.size(); i++) {
				Segment seg = segmentList.get(i);
				if (i == 0)
					s = s + "在" + seg.startName + "坐" + seg.getShortBusName()
							+ "到" + seg.endName;
				else
					s = s + ",换乘" + seg.getShortBusName() + "到" + seg.endName;
			}
			return s;
		}

	}

	public static class Segment {
		// Segment
		// ----------------------------
		// busName String //公交名称
		// startName String //起点名称
		// driverLength String //行驶距离
		// footLength String //步行距离
		// passDepotCoordinate String //途径站点坐标，和passDepotCount对应
		// coordinateList String //路线坐标
		// passDepotName String //路径站点
		// endName String //终点名称
		// passDepotCount String //途径站点数

		String busName; // 公交名称
		String startName; // 起点名称
		String driverLength; // 行驶距离
		String footLength; // 步行距离
		String passDepotCoordinate; // 途径站点坐标，和passDepotCount对应
		String coordinateList; // 路线坐标
		String passDepotName; // 路径站点
		String endName; // 终点名称
		String passDepotCount; // 途径站点数

		// 公交名称
		public String getBusName() {
			return busName;
		}

		public String getShortBusName() {
			String res = getBusName();
			int p = res.indexOf('(');
			if (p > 0)
				res = res.substring(0, p);
			return res;
		}

		public void setBusName(String value) {
			this.busName = value;
		}

		// 起点名称
		public String getStartName() {
			return startName;
		}

		public void setStartName(String value) {
			this.startName = value;
		}

		// 行驶距离
		public String getDriverLength() {
			return driverLength;
		}

		public void setDriverLength(String value) {
			this.driverLength = value;
		}

		// 步行距离
		public String getFootLength() {
			return footLength;
		}

		public void setFootLength(String value) {
			this.footLength = value;
		}

		// 途径站点坐标，和passDepotCount对应
		public String getPassDepotCoordinate() {
			return passDepotCoordinate;
		}

		public void setPassDepotCoordinate(String value) {
			this.passDepotCoordinate = value;
		}

		// 路线坐标
		public String getCoordinateList() {
			return coordinateList;
		}

		public void setCoordinateList(String value) {
			this.coordinateList = value;
		}

		// 路径站点
		public String getPassDepotName() {
			return passDepotName;
		}

		public void setPassDepotName(String value) {
			this.passDepotName = value;
		}

		// 终点名称
		public String getEndName() {
			return endName;
		}

		public void setEndName(String value) {
			this.endName = value;
		}

		// 途径站点数
		public String getPassDepotCount() {
			return passDepotCount;
		}

		public void setPassDepotCount(String value) {
			this.passDepotCount = value;
		}

		public void loadFromJsonArr(JSONArray jsArr) throws JSONException {
			busName = jsArr.getString(0);
			startName = jsArr.getString(1);
			endName = jsArr.getString(2);
			passDepotCount = jsArr.getString(3);
			driverLength = jsArr.getString(4);
			footLength = jsArr.getString(5);
			if (jsArr.length() > 6) {
				coordinateList = jsArr.getString(6);
				passDepotCoordinate = jsArr.getString(7);
				passDepotName = jsArr.getString(8);
			}
		}

		public void saveToJsonArr(JSONArray jsObj) throws JSONException {
			jsObj.put(busName);
			jsObj.put(startName);
			jsObj.put(endName);
			jsObj.put(passDepotCount);
			jsObj.put(driverLength);
			jsObj.put(footLength);
			jsObj.put(coordinateList);
			jsObj.put(passDepotCoordinate);
			jsObj.put(passDepotName);
		}

	}

	public BusChangeInfo() {
	}

	public BusChangeInfo(String city, String start, String end, String qType,
			String x1, String y1, String x2, String y2) {
		this.cityName = city;
		this.start = start;
		this.end = end;
		this.qtype = qType;
		fx = x1;
		fy = y1;
		tx = x2;
		ty = y2;
		this.exchangePlans = new ArrayList<ExchangePlan>();
	}

	@Override
	public String toString() {
		String r = null;
		// String r = Integer.toString(index) + ".";
		// if (stationType == StationType_Start)
		// r = r + "[起点]";
		// else if (stationType == StationType_End)
		// r = r + "[终点]";
		// r = r + start;
		// if (inStationCount > 0)
		// r = r + "(" + Integer.toString(inStationCount) + "车进站)";
		// if (leaveStationCount > 0)
		// r = r + "(" + Integer.toString(leaveStationCount) + "车离站)";
		return r;
	}

	public static String getQueryStart(String station) {
		return station.trim();
	}

	public static String getQueryEnd(String station) {
		return station.trim();
	}

	/**
	 * 获取查询的HASH值
	 * 
	 */
	public static String getBusChangeHash(String city, String start,
			String end, String qtype, String fx, String fy, String tx, String ty) {
		String s = getBusChangeId(city, start, end, qtype, fx, fy, tx, ty);
		String url = "sqldb://" + TABLE_BUS_CHANGE_INFO + "/getobj?id="
				+ UserAppSession.urlEncode(s) + "&SelectFields=id,hash_val";
		Object o = UserAppSession.cursession().executeCacheUrl(url);
		if (o instanceof Map) {
			Map<String, Object> m = TypeUtil.CastToMap_SO(o);
			String hash = TypeUtil.ObjectToString(m.get("hash_val"));
			if(hash==null)
				hash="";
			return hash;
		}
		return "";
	}

	/**
	 * 通过JSON对象加载数据
	 * 
	 * @param jstation
	 */
	public void loadFromJsonObj(JSONObject jsObj) throws JSONException {
		loadQueryInfoFromJson(jsObj);
		JSONArray jsaExchangePlans = jsObj.getJSONArray("ExchangePlans");
		loadExchangePlanFromJson(jsaExchangePlans);
	}

	public void loadExchangePlanFromJson(JSONArray jsaExchangePlans)
			throws JSONException {
		exchangePlans.clear();
		for (int i = 0; i < jsaExchangePlans.length(); i++) {
			JSONObject jsExchangePlan = jsaExchangePlans.getJSONObject(i);
			ExchangePlan vExchangePlan = new ExchangePlan();
			vExchangePlan.loadFromJsonObj(jsExchangePlan);
			exchangePlans.add(vExchangePlan);
		}
	}

	public void loadQueryInfoFromJson(JSONObject jsObj) throws JSONException {
		cityName = jsObj.getString("CityName");
		start = jsObj.getString("Start");
		end = jsObj.getString("End");
		if (jsObj.has("Hash"))
			hashVal = jsObj.getString("Hash");
		fx = jsObj.getString("Fx");
		fy = jsObj.getString("Fy");
		tx = jsObj.getString("Tx");
		ty = jsObj.getString("Ty");
	}

	public void saveToJsonObj(JSONObject jsObj) throws JSONException {
		saveQueryInfoToJson(jsObj);
		JSONArray jsaExchangePlans = new JSONArray();
		for (int i = 0; i < exchangePlans.size(); i++) {
			JSONObject jsExchangePlan = new JSONObject();
			exchangePlans.get(i).saveToJsonObj(jsExchangePlan);
			jsaExchangePlans.put(jsExchangePlan);
		}
		jsObj.put("ExchangePlans", jsaExchangePlans);
	}

	public void saveQueryInfoToJson(JSONObject jsObj) throws JSONException {
		jsObj.put("CityName", cityName);
		jsObj.put("Start", start);
		jsObj.put("End", end);
		if (hashVal == null)
			hashVal = "";
		jsObj.put("Hash", hashVal);
		jsObj.put("Fx", fx);
		jsObj.put("Fy", fy);
		jsObj.put("Tx", tx);
		jsObj.put("Ty", ty);
	}

	/**
	 * 转换成是JSON字符串
	 */
	private String toJsonStr() throws JSONException {
		JSONObject jRes = new JSONObject();
		saveToJsonObj(jRes);

		return jRes.toString();
	}

	/**
	 * 缓存
	 */
	public void saveToCache() throws JSONException {
		if (!UserAppSession.cursession().isCacheTableInited(
				TABLE_BUS_CHANGE_INFO)) {
			String url = "sqldb://" + TABLE_BUS_CHANGE_INFO + "/init?id=s";
			url += "&hash_val=s";
			url += "&json_def=s";
			url += "&expire_time=d";
			UserAppSession.cursession().executeCacheUrl(url);
		}

		String url = "sqldb://" + TABLE_BUS_CHANGE_INFO + "/put";
		Map<String, Object> fieldMap = new HashMap<String, Object>();
		String s = getBusChangeId(cityName, start, end, qtype, fx, fy, tx, ty);
		fieldMap.put("id", s);
		fieldMap.put("hash_val", hashVal);
		fieldMap.put("json_def", toJsonStr());
		fieldMap.put("expire_time", System.currentTimeMillis()
				+ UserAppSession.CAHCE_EXPIRE_TIME_DAY);
		UserAppSession.cursession().executeCacheUrl(url, fieldMap);
	}

	/**
	 * 加载缓存
	 */
	public boolean loadFromCache(String city, String start, String end,
			String qType, String x1, String y1, String x2, String y2) {
		String s = getBusChangeId(city, start, end, qType, x1, y1, x2, y2);
		String url = "sqldb://" + TABLE_BUS_CHANGE_INFO + "/getobj?id="
				+ UserAppSession.urlEncode(s);
		Object o = UserAppSession.cursession().executeCacheUrl(url);
		if (o instanceof Map) {
			Map<String, Object> m = TypeUtil.CastToMap_SO(o);
			String jsonStr = TypeUtil.ObjectToString(m.get("json_def"));
			JSONObject jso;
			try {
				jso = new JSONObject(jsonStr);
			} catch (JSONException e) {
				throw new CtRuntimeException("换乘缓存读取解释出错：" + e.getMessage());
			}
			try {
				this.loadFromJsonObj(jso);
			} catch (JSONException e) {
				e.printStackTrace();
				throw new CtRuntimeException("换乘缓存装载出错：" + e.getMessage());
			}
			return true;
		}
		return false;
	}

	public static boolean isBusChangeCacheExpired(String city, String start,
			String end, String qType, String x1, String y1, String x2, String y2) {
		//对于非地名的记录，不缓存
		if(GeoAddressHelper.nameIsSpecAddress(start))
			return true;
		if(GeoAddressHelper.nameIsSpecAddress(end))
			return true;
		String s = getBusChangeId(city, start, end, qType, x1, y1, x2, y2);
		String url = "sqldb://" + TABLE_BUS_CHANGE_INFO + "/getobj?id="
				+ UserAppSession.urlEncode(s) + "&SelectFields=id,expire_time";
		Object o = UserAppSession.cursession().executeCacheUrl(url);
		if (o instanceof Map) {
			Map<String, Object> m = TypeUtil.CastToMap_SO(o);
			Long tm = TypeUtil.ObjectToLong(m.get("expire_time"));
			if (tm == 0 || tm > System.currentTimeMillis())
				return false;
		}
		return true;
	}

	/**
	 * 获取ID
	 * 
	 * @param city
	 * @param start
	 * @param end
	 */
	public static String getBusChangeId(String cityCode, String start,
			String end, String qType, String x1, String y1, String x2, String y2) {
		start = getQueryStart(start);
		end = getQueryEnd(end);
		//对于非地名的记录，只保留名称，不以坐标为ID
		if(GeoAddressHelper.nameIsSpecAddress(start)){
			x1="";
			y1="";
		}
		if(GeoAddressHelper.nameIsSpecAddress(end)){
			x2="";
			y2="";
		}
		return "city=" + UserAppSession.urlEncode(cityCode) + "&start="
				+ UserAppSession.urlEncode(start) + "&end="
				+ UserAppSession.urlEncode(end) + "&qtype="
				+ UserAppSession.urlEncode(qType) + "&fx="
				+ UserAppSession.urlEncode(x1) + "&fy="
				+ UserAppSession.urlEncode(y1) + "&tx="
				+ UserAppSession.urlEncode(x2) + "&ty="
				+ UserAppSession.urlEncode(y2);
	}

	public String getShareText() {
		String s = start + "-" + end + " 换乘:\n";
		if (exchangePlans != null)
			for (int i = 0; i < exchangePlans.size(); i++) {
				if (i >= 5)
					break;
				ExchangePlan cp = exchangePlans.get(i);
				if (i > 0)
					s = s + "\n";
				s = s + "方案" + Integer.toString(i + 1) + ":";
				s = s + cp.getShareText();
			}
		return s;
	}

}