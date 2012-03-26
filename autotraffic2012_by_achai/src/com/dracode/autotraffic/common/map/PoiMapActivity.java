package com.dracode.autotraffic.common.map;

import java.util.ArrayList;
import java.util.List;

import com.dracode.andrdce.ct.TypeUtil;
import com.dracode.andrdce.ct.UserAppSession;
import com.dracode.autotraffic.R;
import com.mapabc.mapapi.GeoPoint;
import com.mapabc.mapapi.MapActivity;
import com.mapabc.mapapi.PoiItem;

import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class PoiMapActivity extends MapActivity {

	private MapActivityHelper mapHelper;

	public static List<PoiInfo> poiInfoList = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		this.setMapMode(MAP_MODE_VECTOR);// 设置地图为矢量模式
		super.onCreate(savedInstanceState);
		setContentView(R.layout.act_nearby_map);

		mapHelper = new MapActivityHelper();
		mapHelper.init(this);
		mapHelper.initFastJumpHelper();
		// 初始化控件
		initControls();

	}

	/**
	 * 初始化页面控件和添加事件
	 */
	private void initControls() {
		// 标题
		TextView text_Title = (TextView) findViewById(R.id.middle_title);
		text_Title.setText("查询结果");
		ImageView img_Title = (ImageView) findViewById(R.id.more_city);
		img_Title.setVisibility(View.GONE);
		// 返回
		RelativeLayout layout_Back = (RelativeLayout) findViewById(R.id.left_layout);
		layout_Back.setVisibility(View.VISIBLE);
		layout_Back.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});
		// 定位
		ImageView image_loc = (ImageView) findViewById(R.id.right_img);
		image_loc.setBackgroundResource(R.drawable.icon_locate);
		TextView text_loc = (TextView) findViewById(R.id.right_text);
		text_loc.setText("定位");
		RelativeLayout btn_location = (RelativeLayout) findViewById(R.id.right_layout);
		btn_location.setVisibility(View.VISIBLE);
		btn_location.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(final View v) {
				mapHelper.findMyLocation(true);
			}
		});
	}

	@Override
	protected void onResume() {
		super.onResume();
		mapHelper.title_view.setText(UserAppSession.cur_CityName);
		mapHelper.initMap();
		addPoiInfos(poiInfoList);
	}

	private void addPoiInfos(List<PoiInfo> poiLst) {
		if (mapHelper.extraPoiOverlay != null) {
			mapHelper.extraPoiOverlay.removeFromMap();
		}
		if (poiLst==null)
			return;
		
		List<PoiItem> poiItems = new ArrayList<PoiItem>();
		double x1 = 180, y1 = 90, x2 = -180, y2 = -90;
		for (PoiInfo poi : poiLst) {
			if(poi==null)
				continue;
			double x = TypeUtil.ObjectToDouble(poi.getX());
			double y = TypeUtil.ObjectToDouble(poi.getY());
			if (x1 > x)
				x1 = x;
			if (y1 > y)
				y1 = y;
			if (x2 < x)
				x2 = x;
			if (y2 < y)
				y2 = y;
			GeoPoint pt = new GeoPoint((int) (y * 1E6), (int) (x * 1E6));
			PoiItem item = new PoiItem(poi.getPoiId(), pt, poi.getName(),
					poi.getPhone());
			item.setTel(poi.getPhone());
			item.setTypeCode(poi.getTypeCode());
			poiItems.add(item);
		}
		mapHelper.addExtraPois(poiItems);
		mapHelper.zoomToFit(x1, y1, x2, y2);

		Bundle ext = getIntent().getExtras();
		String focusPoiId = ext.getString("focusPoiId");
		if(focusPoiId!=null && focusPoiId.length()>0){
			mapHelper.extraPoiOverlay.lastPopupItemId=focusPoiId;
			mapHelper.restoreLastPoiPopup();
		}
	}

	@Override
	protected void onPause() {
		mapHelper.doPause();
		super.onPause();
	}

	@Override
	protected void onStop() {
		mapHelper.doStop();
		super.onStop();
	}
}
