package com.dracode.autotraffic.bus.busstation;

import com.dracode.autotraffic.common.helpers.QueryHistoryHelper;

public class BusStationHistInfo extends QueryHistoryHelper.QueryHistoryInfo {
	public static final String TABLE_BUSSTATION_QUERY_HISTORY = "busstation_query_history";
	
	public BusStationHistInfo(String city, String st, String time) {
		id = BusStationInfo.getBusStationId(city, st);
		setStationName(st);
		setCityName(city);
		queryTime = time;
	}

	public BusStationHistInfo() {
	}

	public String getStationName() {
		return name;
	}

	public void setStationName(String lineName) {
		this.name = lineName;
	}

	public void setCityName(String cityName) {
		this.city=cityName;
		this.memo = cityName;
	}

	public String getCityName() {
		return memo;
	}

	@Override
	public String getMemo() {
		return memo;
	}

	@Override
	public String getCacheTableName() {
		return TABLE_BUSSTATION_QUERY_HISTORY;
	}
	
	@Override
	public String toString() {
		return getName() + " - " + getCityName()
				+ "   " + queryTime;
	}

}