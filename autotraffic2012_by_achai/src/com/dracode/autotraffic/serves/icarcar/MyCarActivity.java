package com.dracode.autotraffic.serves.icarcar;

import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import com.dracode.autotraffic.R;

public class MyCarActivity extends IcarBaseActivity {

	@Override    
    public void onCreate(Bundle savedInstanceState) {    
	    super.onCreate(savedInstanceState);    
	    setContentView(R.layout.act_service_mycar); 

	    init();
	    
	    initControls();  
	}
	
	/** 
     * 初始化页面控件和添加事件
	 */
	private void initControls() {
		baseHelper.left_layout.setVisibility(View.VISIBLE);
		baseHelper.right_layout.setVisibility(View.GONE);
		
		baseHelper.title_view.setText("我的爱车");
		
		baseHelper.left_img.setBackgroundResource(R.drawable.header_back);
		baseHelper.left_text.setText("返回");
		
		baseHelper.left_layout.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				finish();
			}
		});		
	}
}
