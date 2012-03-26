package com.dracode.autotraffic.common.map;

import com.dracode.andrdce.ct.TypeUtil;

public class MapViewInfo {
	public Integer centerX;
	public Integer centerY;
	public Integer zoomLevel;
	public Integer myLatitudeE6;
	public Integer myLongitudeE6;
	public String myAddress;// 详细地址
	public String myRoughAddress;// 粗略地址
	public String lastCityCode = null;

	public static MapViewInfo lastMapView = null;
	
	public static MapViewInfo getLastMapView() {
		if (lastMapView == null) {
			lastMapView = new MapViewInfo();
		}
		return lastMapView;
	}

	@Override
	public String toString() {
		return ""+Integer.toString(centerX)+","+Integer.toString(centerY)+","+Integer.toString(zoomLevel);
	}
	
	public boolean fromString(String str){
		if(str==null)
			return false;
		str=str.trim();
		if(str.length()==0)
			return false;
		String[] s=str.split(",");
		if(s.length!=3)
			return false;

		centerX=TypeUtil.ObjectToInt(s[0]);
		centerY=TypeUtil.ObjectToInt(s[1]);
		zoomLevel=TypeUtil.ObjectToInt(s[2]);
		return true;
	}
}
