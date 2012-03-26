package com.dracode.autotraffic.serves.taxicall;

import com.dracode.andrdce.ct.UserAppSession;
import com.dracode.autotraffic.R;
import com.dracode.autotraffic.common.helpers.BaseActivityHelper;

import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;

public class TaxiCallMapIndex extends PoiMapActivityForTaxi {
	
	private TaxiCallMapIndexHelper taxiHelper = new TaxiCallMapIndexHelper();;
	
	protected BaseActivityHelper baseHelper;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		this.setMapMode(MAP_MODE_VECTOR);// 设置地图为矢量模式
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.act_taxi_call);
		
		initControler();	
		taxiHelper.init(this);
	}

	private void initControler(){
		baseHelper = new BaseActivityHelper();
		baseHelper.init(this);
		baseHelper.title_view.setText("出租电召");
		
		baseHelper.left_layout.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				finish();
			}
		});
		baseHelper.right_layout.setVisibility(View.GONE);
		initMap();
		UserAppSession.showToast(this, "正在查找周边的士...");
	}
	@Override
	protected void onStop() {
		taxiHelper.killTaxiTimer();
		super.onStop();
	}

	@Override
	protected void addSearchPois() {
		super.addSearchPois();
		taxiHelper.resetTaxiTimer(10000);
	}	
}
