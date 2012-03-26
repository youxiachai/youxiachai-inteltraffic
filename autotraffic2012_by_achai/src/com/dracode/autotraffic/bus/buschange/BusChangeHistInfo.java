package com.dracode.autotraffic.bus.buschange;

import com.dracode.autotraffic.common.helpers.QueryHistoryHelper;

public class BusChangeHistInfo extends QueryHistoryHelper.QueryHistoryInfo {
	public static final String TABLE_BUSSTATION_QUERY_HISTORY = "buschange_query_history";

	public BusChangeHistInfo() {
	}

	public BusChangeHistInfo(String city, String start, String end,
			String qType, String fx, String fy, String tx, String ty,
			String time) {
		id = BusChangeInfo.getBusChangeId(city, start, end, qType, fx, fy, tx,
				ty);
		setCityName(city);
		setStart(start);
		setEnd(end);
		name = start + "--" + end;
		setQType(qType);
		setFx(fx);
		setFy(fy);
		setTx(tx);
		setTy(ty);
		queryTime = time;
	}

	public String getStart() {
		return param5;
	}

	public void setStart(String start) {
		this.param5 = start;
	}

	public String getEnd() {
		return param6;
	}

	public void setEnd(String end) {
		this.param6 = end;
	}

	public String getQType() {
		return param7;
	}

	public void setQType(String qType) {
		this.param7 = qType;
	}

	public String getFx() {
		return param1;
	}

	public void setFx(String fx) {
		this.param1 = fx;
	}

	public String getTx() {
		return param2;
	}

	public void setTx(String tx) {
		this.param2 = tx;
	}

	public String getFy() {
		return param3;
	}

	public void setFy(String fy) {
		this.param3 = fy;
	}

	public String getTy() {
		return param4;
	}

	public void setTy(String ty) {
		this.param4 = ty;
	}

	public void setCityName(String cityName) {
		this.city = cityName;
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
		return getName() + " - " + getCityName() + "   " + queryTime;
	}

}