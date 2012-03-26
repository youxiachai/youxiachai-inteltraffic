package com.dracode.autotraffic.common.map;

import org.json.JSONArray;
import org.json.JSONException;

import com.dracode.andrdce.ct.CtRuntimeException;
import com.dracode.autotraffic.R;

/**
 * 查询结果页
 * 
 * @author Figo.Gu
 */
public class PoiInfo {
	/** icon. */
	private int icon;
	/** 商家. */
	private String name;
	/** 地址. */
	private String address;
	/** 距离. */
	private int distance;
	/** 电话号码. */
	private String phone;
	/** 类型编码. */
	private String typeCode;
	/** POI编号. */
	private String poiId;

	private String x;

	public String getX() {
		return x;
	}

	public void setX(String x) {
		this.x = x;
	}

	public String getY() {
		return y;
	}

	public void setY(String y) {
		this.y = y;
	}

	private String y;

	public int getIcon() {
		return icon;
	}

	public void setIcon(int icon) {
		this.icon = icon;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public int getDistance() {
		return distance;
	}

	public void setDistance(int distance) {
		this.distance = distance;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public String getTypeCode() {
		return typeCode;
	}

	public void setTypeCode(String typeCode) {
		this.typeCode = typeCode;
	}

	public String getPoiId() {
		return poiId;
	}

	public void setPoiId(String poiId) {
		this.poiId = poiId;
	}

	public void loadPropsFromJson(JSONArray jPoi) {
		try {
			this.icon = R.drawable.icon_num02;
			this.name = jPoi.getString(0);
			this.address = jPoi.getString(1);
			this.distance = jPoi.getInt(2);
			this.phone = jPoi.getString(3);
			this.typeCode = jPoi.getString(4);
			this.poiId = jPoi.getString(5);
			this.x = jPoi.getString(6);
			this.y = jPoi.getString(7);
		} catch (JSONException e) {
			e.printStackTrace();
			throw new CtRuntimeException("POI数据解释出错:" + e.getMessage());
		}
	}
}
