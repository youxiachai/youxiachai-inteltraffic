package com.dracode.autotraffic.serves.train.stationstation;

import com.dracode.autotraffic.R;
import com.dracode.autotraffic.serves.train.TrainSwitchBaseActivity;

import android.os.Bundle;

public class TrainQueryActivity extends TrainSwitchBaseActivity {

	@Override
	public void onCreate(Bundle savedInstanceState){
		 super.onCreate(savedInstanceState);    
		 setContentView(R.layout.act_train_query); 
		        
		 init();
		 initControls();  
	}
	/**
	 * 初始化控件
	 */
	private void initControls(){
		baseHelper.title_view.setText("火车");
		station_Station_Bt.setSelected(true);
		
		baseHelper.right_img.setBackgroundResource(R.drawable.icon_alart);
		baseHelper.right_text.setText("历史");
	}
}
