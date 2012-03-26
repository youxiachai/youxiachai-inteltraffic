package com.dracode.autotraffic.serves.trafficpolice;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;

import com.dracode.autotraffic.R;
import com.dracode.autotraffic.common.helpers.BaseActivityHelper;

public class TrafficPoliceQueryActivty extends Activity {

	private BaseActivityHelper baseHelper;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.act_trafficpolice_query);
		
		baseHelper = new BaseActivityHelper();
		baseHelper.init(this);
		baseHelper.initForTrafficPoliceQuery();

		baseHelper.queryBt.setSelected(true);
		
		initControls();
	}

	/** 
	 * 初始化页面控件和添加事件
	 */
	private void initControls() {
		baseHelper.right_layout.setVisibility(View.GONE);
		baseHelper.title_view.setText("交警专栏");
	}

	@Override
	protected void onResume() {
	
		super.onResume();
	}

}
