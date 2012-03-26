package com.dracode.autotraffic.route.drive;

import com.dracode.andrdce.ct.AppUtil;
import com.dracode.andrdce.ct.UserAppSession;
import com.dracode.autotraffic.bus.buschange.BusChangeHistInfo;
import com.dracode.autotraffic.bus.buschange.BusChangeQueryActivityHelper;
import android.os.Bundle;

public class DriveQueryActivityHelper extends BusChangeQueryActivityHelper {

	/**
	 * 执行查询
	 */
	@Override
	public void callRouteQuery(String city, String start, String end,
			String x1, String y1, String x2, String y2) {
		BusChangeHistInfo hq = new BusChangeHistInfo(city, start, end,
				busChangeDataIntf.getQType(), x1, y1, x2, y2,
				UserAppSession.getCurTimeStr());
		busChangeDataIntf.addNewBusQueryHist(hq);
		
		Bundle bundle = new Bundle();
		bundle.putString("QueryCityName", city);
		bundle.putString("QueryStart", start);
		bundle.putString("QueryEnd", end);
		bundle.putString("QueryStartLat", y1);
		bundle.putString("QueryStartLon", x1);
		bundle.putString("QueryEndLat", y2);
		bundle.putString("QueryEndLon", x2);
		AppUtil.startActivity(theAct, DriveResultActivity.class, false, bundle);
	}

}
