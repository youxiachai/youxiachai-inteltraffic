package com.dracode.autotraffic.main;

import java.util.List;

import com.dracode.andrdce.ct.AppExceptionHandler;
import com.dracode.andrdce.ct.UserAppSession;
import com.mapabc.mapapi.PoiItem;


public class MyApp extends UserAppSession {
	
	/** 附近的地图POI列表 .*/
	private List<PoiItem> poiItems;
	/** 附近的地图POI列表的当前页数 .*/
	private Integer poiItemsPageNum = 1;
	/** 最后定位时间 .*/
	private Long locationLastTime;

	@Override
    public void onCreate() {
        super.onCreate();
        AppExceptionHandler crashHandler = AppExceptionHandler.getInstance();
        //注册crashHandler
        crashHandler.init(getApplicationContext());
        //发送以前没发送的报告(可选)
        //crashHandler.sendPreviousReportsToServer();
    }

	public List<PoiItem> getPoiItems() {
		return poiItems;
	}
	public void setPoiItems(List<PoiItem> poiItems) {
		this.poiItems = poiItems;
	}
	public Integer getPoiItemsPageNum() {
		return poiItemsPageNum;
	}
	public void setPoiItemsPageNum(Integer poiItemsPageNum) {
		this.poiItemsPageNum = poiItemsPageNum;
	}
	public Long getLocationLastTime() {
		return locationLastTime;
	}
	public void setLocationLastTime(Long locationLastTime) {
		this.locationLastTime = locationLastTime;
	}
}
