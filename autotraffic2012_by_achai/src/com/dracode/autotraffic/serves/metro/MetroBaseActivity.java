package com.dracode.autotraffic.serves.metro;

import android.app.Activity;
import android.content.Intent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;

import com.dracode.autotraffic.R;
import com.dracode.autotraffic.common.helpers.BaseActivityHelper;

public class MetroBaseActivity extends Activity {

	/** 地铁换乘.*/
	protected ImageButton metro_change;
	/** 地铁线路.*/
	protected ImageButton metro_line;
	protected BaseActivityHelper baseHelper;
	protected void init(){
		baseHelper = new BaseActivityHelper();
		baseHelper.init(this);
		
		metro_change = (ImageButton) findViewById(R.id.metro_change_bt);
		if(metro_change!=null){
			metro_change.setOnClickListener(new OnClickListener(){

				@Override
				public void onClick(View v) {
					if (!"MetroChangeQueryActivity".equals(MetroBaseActivity.this.getClass().getSimpleName())) {
						Intent intent = new Intent(MetroBaseActivity.this,MetroChangeQueryActivity.class);
						startActivity(intent);
						finish();
					}	
				}
			});
		}
		
		metro_line = (ImageButton) findViewById(R.id.metro_line_bt);
		if(metro_line!=null){
			metro_line.setOnClickListener(new OnClickListener(){

				@Override
				public void onClick(View v) {
					if (!"MetroLineQueryActivity".equals(MetroBaseActivity.this.getClass().getSimpleName())) {
						Intent intent = new Intent(MetroBaseActivity.this,MetroLineQueryActivity.class);
						startActivity(intent);
						finish();
					}
				}
			});
		}
	}
}
