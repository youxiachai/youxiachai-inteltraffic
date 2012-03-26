package com.dracode.autotraffic.roadcodition;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;

import com.dracode.andrdce.ct.UserAppSession;
import com.dracode.autotraffic.R;
import com.dracode.autotraffic.common.map.MapActivityHelper;
import com.dracode.autotraffic.common.switchcity.SwitchCityActivity;
import com.mapabc.mapapi.MapActivity;

public class RoadCoditionMapActivity extends MapActivity {

	private MapActivityHelper mapHelper;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.act_road_map);
		
		mapHelper=new MapActivityHelper();
		mapHelper.init(this);
		mapHelper.initForRoadConditionMap();
		initControls();
	}

	/**
	 * 初始化页面控件和添加事件
	 */
	private void initControls() {
		mapHelper.left_layout.setVisibility(View.VISIBLE);
		mapHelper.right_layout.setVisibility(View.VISIBLE);

		mapHelper.left_img.setBackgroundResource(R.drawable.icon_highway);
		mapHelper.left_text.setText("高速");

		mapHelper.left_layout.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent intent = new Intent(mapHelper.theAct, RoadHighSpeedActivity.class);
				startActivity(intent);
			}
		});

		// 定位
		mapHelper.right_layout.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				mapHelper.findMyLocation(true);
			}
		});

		mapHelper.right_img.setBackgroundResource(R.drawable.icon_locate);
		mapHelper.right_text.setText("定位");

		mapHelper.middle_Relative.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(RoadCoditionMapActivity.this,
						SwitchCityActivity.class);
				startActivity(intent);
			}
		});

		// 图层
		ImageButton imgBtn_Layer = (ImageButton) findViewById(R.id.layer);
		imgBtn_Layer.setVisibility(View.VISIBLE);
		imgBtn_Layer.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(final View v) {
				mapHelper.mLayerHelper.selectLayers();
			}
		});
	}

	@Override
	protected void onResume() {
		super.onResume();
		mapHelper.title_view.setText(UserAppSession.cur_CityName);
		mapHelper.initMap();
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
