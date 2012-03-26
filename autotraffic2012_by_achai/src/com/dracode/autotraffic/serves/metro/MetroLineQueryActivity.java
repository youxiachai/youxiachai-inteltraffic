package com.dracode.autotraffic.serves.metro;

import com.dracode.autotraffic.R;

import android.os.Bundle;

public class MetroLineQueryActivity extends MetroBaseActivity {

	@Override
	public void onCreate(Bundle savedInstanceState){
		 super.onCreate(savedInstanceState);    
		 setContentView(R.layout.act_metro_line_query); 
		        
		 super.init();
		 initControls();  
	}
	
	private void initControls(){
		baseHelper.title_view.setText("地铁查询");
		metro_line.setSelected(true);
		
		baseHelper.right_img.setBackgroundResource(R.drawable.icon_alart);
		baseHelper.right_text.setText("历史");
	}
}
