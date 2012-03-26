package com.dracode.autotraffic.bus.busstation;

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
import com.dracode.autotraffic.bus.busline.BusInfo;

public class BusStationInfo {
	public static final String TABLE_BUS_STATION_INFO = "bus_station_info";
	
	public static final int StationType_Unknown = 0;
	public static final int StationType_Start = 1;
	public static final int StationType_Middle = 2;
	public static final int StationType_End = 3;
	
	public int index;
	public int stationType;
	public String cityName;
	public String stationName;
	public int inStationCount;
	public String lastInTime;
	public int leaveStationCount;
	public String geo_x;
	public String geo_y;
	public String hashVal;
	
	public List<BusInfo> relateBusLines;

	public BusStationInfo() {

	}

	public BusStationInfo(int aid, int stType, String stName,
			int inStCount, int leaveStCount,String inTime) {
		index = aid;
		stationType = stType;
		stationName = stName;
		inStationCount = inStCount;
		leaveStationCount = leaveStCount;
		lastInTime = inTime;
	}

	public BusStationInfo(String city, String station) {
		cityName=city;
		stationName=station;
		relateBusLines = new ArrayList<BusInfo>();
	}

	@Override
	public String toString() {
		String r = Integer.toString(index) + ".";
		if (stationType == StationType_Start)
			r = r + "[起点]";
		else if (stationType == StationType_End)
			r = r + "[终点]";
		r = r + stationName;
		if (inStationCount > 0)
			r = r + "(" + Integer.toString(inStationCount) + "车进站)";
		if (leaveStationCount > 0)
			r = r + "(" + Integer.toString(leaveStationCount) + "车离站)";
		return r;
	}

	public static String getQueryStationName(String city, String station) {
		station=station.trim();
		int len=station.length();
		if(len>2 && "020".equals(city)){
			//广州实时公交：去掉最后的数字
			if(BusInfo.charIsNum(station.charAt(len-1)) && station.charAt(len-2)=='站')
				station=station.substring(0,len-2);
			else if(BusInfo.charIsNum(station.charAt(len-1)))
				station=station.substring(0,len-1);
			else if(station.charAt(len-2)!='总' && station.charAt(len-1)=='站')
				station=station.substring(0,len-1);
		}
		return station;
	}

	public static String getStationHash(String city, String station) {
		String s = getBusStationId(city, station);
		String url = "sqldb://" + TABLE_BUS_STATION_INFO + "/getobj?id="
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

	public void loadFromJsonObj(JSONObject jstation) throws JSONException {

		stationName = jstation.getString("StationName");
		hashVal = jstation.getString("Hash");

		relateBusLines.clear();
		JSONArray bs = jstation.getJSONArray("BusLines");
		int len = bs.length();
		for (int i = 0; i < len; i++) {
			JSONObject jbi = bs.getJSONObject(i);
			BusInfo bi = new BusInfo();
			bi.index = i+1;
			bi.loadBasicJsonProps(jbi.getJSONArray("Props"));
			relateBusLines.add(bi);
		}
	}

	public String toJsonStr() throws JSONException {
		JSONObject jstation = new JSONObject();

		jstation.put("StationName", stationName);
		jstation.put("Hash", hashVal);
		
		JSONArray sts = new JSONArray();
		for (int i = 0; i < relateBusLines.size(); i++) {
			JSONObject jbi = new JSONObject();
			BusInfo bi = relateBusLines.get(i);
			JSONArray jsArr = new JSONArray();
			bi.saveBasicJsonProps(jsArr);
			jbi.put("Props", jsArr);
			sts.put(jbi);
		}

		jstation.put("BusLines", sts);

		return jstation.toString();
	}
	
	public void saveToCache() throws JSONException {
		if (!UserAppSession.cursession().isCacheTableInited(
				TABLE_BUS_STATION_INFO)) {
			String url = "sqldb://" + TABLE_BUS_STATION_INFO
					+ "/init?id=s";
			url += "&hash_val=s";
			url += "&json_def=s";
			url += "&expire_time=d";
			UserAppSession.cursession().executeCacheUrl(url);
		}

		String url = "sqldb://" +TABLE_BUS_STATION_INFO + "/put";
		Map<String, Object> fieldMap = new HashMap<String, Object>();
		String s= getBusStationId(cityName, stationName);
		fieldMap.put("id", s);
		fieldMap.put("hash_val", hashVal);
		fieldMap.put("json_def", toJsonStr());
		fieldMap.put("expire_time", System.currentTimeMillis()+UserAppSession.CAHCE_EXPIRE_TIME_DAY);
		UserAppSession.cursession().executeCacheUrl(url, fieldMap);
	}

	public boolean loadFromCache(String city, String station) {
		String s= getBusStationId(city, station);
		String url = "sqldb://" + TABLE_BUS_STATION_INFO
				+ "/getobj?id="+UserAppSession.urlEncode(s);
		Object o = UserAppSession.cursession().executeCacheUrl(url);
		if (o instanceof Map) {
			Map<String, Object> m = TypeUtil.CastToMap_SO(o);
			String jsonStr=TypeUtil.ObjectToString(m.get("json_def"));
			JSONObject jso;
			try {
				jso = new JSONObject(jsonStr);
			} catch (JSONException e) {
				throw new CtRuntimeException("站点缓存读取解释出错："+e.getMessage());
			}
			try {
				this.loadFromJsonObj(jso);
			} catch (JSONException e) {
				throw new CtRuntimeException("站点缓存装载出错："+e.getMessage());
			}
			return true;
		}
		return false;
	}
	
	public static String getBusStationId(String cityCode, String st) {
		st = getQueryStationName(cityCode, st);
		return "city=" + UserAppSession.urlEncode(cityCode) + "&station="
				+ UserAppSession.urlEncode(st);
	}

	public static boolean isBusStationCacheExpired(String city, String station) {
		String s = getBusStationId(city, station);
		String url = "sqldb://" + TABLE_BUS_STATION_INFO + "/getobj?id="
				+ UserAppSession.urlEncode(s) + "&SelectFields=id,expire_time";
		Object o = UserAppSession.cursession().executeCacheUrl(url);
		if (o instanceof Map) {
			Map<String, Object> m = TypeUtil.CastToMap_SO(o);
			Long tm = TypeUtil.ObjectToLong(m.get("expire_time"));
			if(tm==0 || tm>System.currentTimeMillis())
				return false;
		}
		return true;
	}

	public String getShareText() {
		String s=stationName+" 途经线路: ";
		if(relateBusLines!=null)
			for(int i=0;i<relateBusLines.size();i++){
				BusInfo bi = relateBusLines.get(i);
				if(i>0)
					s=s+",";
				s=s+bi.lineName;
			}
		return s;
	}

}