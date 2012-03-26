package com.dracode.autotraffic.serves.icarcar;

import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;

import com.dracode.autotraffic.R;

public class IcarGoodsDetailActivity extends IcarBaseActivity {

	@Override    
    public void onCreate(Bundle savedInstanceState) {    
	    super.onCreate(savedInstanceState);    
	    setContentView(R.layout.act_service_icard_goods_detail); 

	    init();
	    initControls();  
	
	}
	private void initControls(){
		baseHelper.left_layout.setVisibility(View.VISIBLE);
		baseHelper.left_layout.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				finish();
			}
		});
		baseHelper.title_view.setText("清洁1/15");
	}
}

