package com.dracode.autotraffic.bus.busline;

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
import com.dracode.autotraffic.bus.busstation.BusStationInfo;

public class BusInfo {
	public static final String TABLE_BUS_LINE_INFO = "bus_line_info";
	public static boolean realTimeInfoEnabled=true;
	
	public int index;
	public String busName;
	public String cityName;
	public String cityCode;
	public String hashVal;

	public String lineId;
	public String lineName;
	public String lineType;
	public String company;
	public String description;
	public String lineDirection;
	public String lineKey;
	public String strPlatName;
	public String endPlatName;
	public String firstTime;
	public String lastTime;
	public String lineLength;
	public String ticketPrice;
	public String topFare;
	public String ticketMode;
	public String status;

	public List<BusStationInfo> stations;

	public BusInfo(String aBusName, String aCityName, String dir) {
		busName = aBusName;
		lineName = aBusName;
		cityName = aCityName;
		cityCode = aCityName;
		lineDirection = dir;
		stations = new ArrayList<BusStationInfo>();
	}

	public BusInfo() {
	}

	public String getBusName() {
		String res;
		if (lineName != null && lineName.length() > 0)
			res = lineName;
		else
			res = busName;
		res = getBusQueryName(res);
		if ("1".equals(lineDirection))
			res = res + "(下行)";
		else if ("0".equals(lineDirection))
			res = res + "(上行)";
		return res;
	}

	@Override
	public String toString() {
		return getBusName() + " - " + cityName;
	}

	public void loadFromJsonObj(JSONObject line) throws JSONException {

		hashVal = line.getString("Hash");

		JSONArray jProps=line.getJSONArray("Props");
		loadBasicJsonProps(jProps);
		
		stations.clear();
		JSONArray sts = line.getJSONArray("Stations");
		int len = sts.length()/3;
		int j=0;
		for (int i = 0; i < len; i++) {
			BusStationInfo st = new BusStationInfo();
			st.index=i;
			st.stationName=	sts.getString(j);j++;
			st.geo_x=sts.getString(j);j++;
			st.geo_y=sts.getString(j);j++;
			if (i == 0)
				st.stationType = BusStationInfo.StationType_Start;
			else if (i == len - 1)
				st.stationType = BusStationInfo.StationType_End;
			else
				st.stationType = BusStationInfo.StationType_Middle;
			stations.add(st);
		}
	}

	public void loadBasicJsonProps(JSONArray jProps) throws JSONException {
		int i=0;
		lineId = jProps.getString(i);i++;
		lineName = jProps.getString(i);i++;
		lineType = jProps.getString(i);i++;
		company = jProps.getString(i);i++;
		description = jProps.getString(i);i++;
		lineDirection = jProps.getString(i);i++;
		lineKey = jProps.getString(i);i++;
		strPlatName = jProps.getString(i);i++;
		endPlatName = jProps.getString(i);i++;
		firstTime = jProps.getString(i);i++;
		lastTime = jProps.getString(i);i++;
		lineLength = jProps.getString(i);i++;
		ticketPrice = jProps.getString(i);i++;
		topFare = jProps.getString(i);i++;
		ticketMode = jProps.getString(i);i++;
		status=jProps.getString(i);
	}

	public String toJsonStr() throws JSONException {
		JSONObject jsObj = new JSONObject();

	    JSONArray jsArr = new JSONArray();
	    saveBasicJsonProps(jsArr);
	    
	    jsObj.put("Props", jsArr);	    
	    jsObj.put("Hash", hashVal);
	    
		JSONArray sts = new JSONArray();
		for (int i = 0; i < stations.size(); i++) {
			BusStationInfo st = stations.get(i);
			sts.put(st.stationName);
			sts.put(st.geo_x);
			sts.put(st.geo_y);
		}

		jsObj.put("Stations", sts);

		return jsObj.toString();
	}

	public void saveBasicJsonProps(JSONArray jsArr) {
		jsArr.put(lineId);
	    jsArr.put(lineName);
	    jsArr.put(lineType);
	    jsArr.put(company);
	    jsArr.put(description);
	    jsArr.put(lineDirection);
	    jsArr.put(lineKey);
	    jsArr.put(strPlatName);
	    jsArr.put(endPlatName);
	    jsArr.put(firstTime);
	    jsArr.put(lastTime);
	    jsArr.put(lineLength);
	    jsArr.put(ticketPrice);
	    jsArr.put(topFare);
	    jsArr.put(ticketMode);
	    jsArr.put(status);
	}

	public void saveToCache() throws JSONException {
		if (!UserAppSession.cursession().isCacheTableInited(
				TABLE_BUS_LINE_INFO)) {
			String url = "sqldb://" + TABLE_BUS_LINE_INFO
					+ "/init?id=s";
			url += "&hash_val=s";
			url += "&json_def=s";
			url += "&expire_time=d";
			UserAppSession.cursession().executeCacheUrl(url);
		}

		String url = "sqldb://" +TABLE_BUS_LINE_INFO + "/put";
		Map<String, Object> fieldMap = new HashMap<String, Object>();
		String s= getBusLineId(cityName, lineName, lineDirection);
		fieldMap.put("id", s);
		fieldMap.put("hash_val", hashVal);
		fieldMap.put("json_def", toJsonStr());
		fieldMap.put("expire_time", System.currentTimeMillis()+UserAppSession.CAHCE_EXPIRE_TIME_DAY);
		UserAppSession.cursession().executeCacheUrl(url, fieldMap);
	}

	public boolean loadFromCache(String city, String bus, String dir) {
		String s= getBusLineId(city, bus, dir);
		String url = "sqldb://" + TABLE_BUS_LINE_INFO
				+ "/getobj?id="+UserAppSession.urlEncode(s);
		Object o = UserAppSession.cursession().executeCacheUrl(url);
		if (o instanceof Map) {
			Map<String, Object> m = TypeUtil.CastToMap_SO(o);
			String jsonStr=TypeUtil.ObjectToString(m.get("json_def"));
			JSONObject jso;
			try {
				jso = new JSONObject(jsonStr);
			} catch (JSONException e) {
				throw new CtRuntimeException("线路缓存读取解释出错："+e.getMessage());
			}
			try {
				this.loadFromJsonObj(jso);
			} catch (JSONException e) {
				throw new CtRuntimeException("线路缓存装载出错："+e.getMessage());
			}
			for (BusStationInfo st : stations) {
				st.inStationCount = 0;
				st.leaveStationCount = 0;
			}
			return true;
		}
		return false;
	}

	public void setStationInfoStr(String str) {
		try {
			for (BusStationInfo st : stations) {
				st.inStationCount = 0;
				st.leaveStationCount = 0;
				st.lastInTime="";
			}
			if(str==null)
				str="";
			String[] sti = str.split(",");
			if(str.length()==0)
				return;
			int c = sti.length;
			for (int i = 0; i < c; i += 4) {
				int idx = Integer.parseInt(sti[i]);
				int inst = Integer.parseInt(sti[i + 1]);
				int outst = Integer.parseInt(sti[i + 2]);
				String tm = sti[i + 3];
				BusStationInfo st = stations.get(idx);
				st.inStationCount = inst;
				st.leaveStationCount = outst;
				if(!"0".equals(tm))
					st.lastInTime=tm;
			}
		} catch (Exception e) {
			throw new CtRuntimeException("实时到站数据组装出错 " + e.getMessage());
		}
	}

	public static String getBusLineHash(String city, String bus, String dir) {
		String s = getBusLineId(city, bus, dir);
		String url = "sqldb://" + TABLE_BUS_LINE_INFO + "/getobj?id="
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

	public static boolean isBusLineCacheExpired(String city, String bus, String dir) {
		String s = getBusLineId(city, bus, dir);
		String url = "sqldb://" + TABLE_BUS_LINE_INFO + "/getobj?id="
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

	public static String getBusLineId(String cityCode, String bus, String dir) {
		bus = getBusQueryName(bus);
		if(!"1".equals(dir))
			dir="0";
		return "city=" + UserAppSession.urlEncode(cityCode) + "&bus="
				+ UserAppSession.urlEncode(bus) + "&dir="
				+ UserAppSession.urlEncode(dir);
	}

	public static String getBusQueryName(String bus) {
		bus = bus.trim().toUpperCase();
		if (!bus.contains("路") && !bus.contains("线") && !bus.contains("夜"))
			bus = bus + "路";
		return bus;
	}

	private static char numberStrs[] = { '0', '1', '2', '3', '4', '5', '6',
			'7', '8', '9' };

	public static boolean charIsNum(char ch) {
		for (char c : numberStrs)
			if (ch == c)
				return true;
		return false;
	}

	public static String trimForBusLineNum(String bus) {
		for (int i = 0; i < bus.length(); i++) {
			if (charIsNum(bus.charAt(i))) {
				if (i > 0)
					bus = bus.substring(i);
				break;
			}
		}
		for (int i = 0; i < bus.length(); i++) {
			if (!charIsNum(bus.charAt(i))) {
				if (i > 0)
					bus = bus.substring(0, i);
				break;
			}
		}
		return bus;
	}

	public static void setServerEnvInfo(String info) {
		if("nogzjw".equals(info))
			realTimeInfoEnabled=false;
	}

	public String getShareText() {
		String s=getBusName()+" 途经: ";
		if(stations!=null)
			for(int i=0;i<stations.size();i++){
				BusStationInfo st=stations.get(i);
				if(i>0)
					s=s+",";
				s=s+st.stationName;
			}
		return s;
	}

}