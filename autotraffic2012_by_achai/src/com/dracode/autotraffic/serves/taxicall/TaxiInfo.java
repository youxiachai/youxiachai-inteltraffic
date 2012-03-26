package com.dracode.autotraffic.serves.taxicall;

public class TaxiInfo {

	public String taxi_id;//车牌id
	public String phone;//手机号码
	public String latitude;//经度
	public String longitude;//维度
	public String gpsTime;//定位时间
	public String distance;//距离查询点的时间
	
	public TaxiInfo(String taxi_id,String phone,String latitude,String longitude,String gpsTime,String distance){
		this.taxi_id = taxi_id;
		this.phone = phone;
		this.latitude = latitude;
		this.longitude = longitude;
		this.gpsTime = gpsTime;
		this.distance = distance;
	}
	
	public TaxiInfo() {
	}
}
