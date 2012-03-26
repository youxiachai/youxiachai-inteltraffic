package com.dracode.autotraffic.bus.busline;

import com.dracode.autotraffic.common.helpers.QueryHistoryHelper;

public class BusLineHistInfo extends QueryHistoryHelper.QueryHistoryInfo {
	public static final String TABLE_BUSLINE_QUERY_HISTORY = "busline_query_history";
	
	public BusLineHistInfo(String city, String ln, String dir, String endPlat,
			String hash, String time) {
		id = BusInfo.getBusLineId(city, ln, "0");
		setLineName(ln);
		setCityName(city);
		setLineDirection(dir);
		setEndPlatName(endPlat);
		queryTime = time;
	}

	public BusLineHistInfo() {
	}

	public String getLineName() {
		return name;
	}

	public void setLineName(String lineName) {
		this.name = lineName;
	}

	public String getLineDirection() {
		return param1;
	}

	public void setLineDirection(String lineDirection) {
		this.param1 = lineDirection;
	}

	public String getEndPlatName() {
		return param2;
	}

	public void setEndPlatName(String endPlatName) {
		this.param2 = endPlatName;
	}

	public void setCityName(String cityName) {
		this.city=cityName;
		this.memo = cityName;
	}

	public String getBusName() {
		return BusInfo.getBusQueryName(name);
	}

	public String getCityName() {
		return memo;
	}

	@Override
	public String getName() {
		return getBusName();
	}

	@Override
	public String getMemo() {
		return memo;
	}

	@Override
	public String getCacheTableName() {
		return TABLE_BUSLINE_QUERY_HISTORY;
	}
	
	@Override
	public String toString() {
		return getName() + " - " + getCityName()
				+ "   " + queryTime;
	}

}