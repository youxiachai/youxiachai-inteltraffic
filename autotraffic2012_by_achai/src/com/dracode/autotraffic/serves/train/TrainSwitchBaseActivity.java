package com.dracode.autotraffic.serves.train;

import android.app.Activity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;

import com.dracode.autotraffic.R;
import com.dracode.autotraffic.common.helpers.BaseActivityHelper;
public class TrainSwitchBaseActivity extends Activity {

	/** 站站.*/
	protected ImageButton station_Station_Bt;
	/** 车次.*/
	protected ImageButton train_Number_Bt;
	/** 车站.*/
	protected ImageButton train_Station_Bt;
	protected BaseActivityHelper baseHelper;
	
	protected void init() {
		baseHelper = new BaseActivityHelper();
		baseHelper.init(this);

		station_Station_Bt = (ImageButton) findViewById(R.id.train_station_station_bt);
		station_Station_Bt.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				
			}
		});
		train_Number_Bt = (ImageButton) findViewById(R.id.train_number_bt);
		train_Number_Bt.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				
			}
		});
		train_Station_Bt = (ImageButton) findViewById(R.id.train_station_bt);
		train_Station_Bt.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				
			}
		});
//		 initTitleBar();
	}
	/*
	protected RelativeLayout left_layout;
	protected ImageView left_img;
	protected TextView left_text;
	protected RelativeLayout right_layout;
	protected ImageView right_img;
	protected TextView right_text;
	protected TextView title;
	
	private void initTitleBar(){		
		left_layout = (RelativeLayout) findViewById(R.id.left_layout);
		if(left_layout!=null){
			left_layout.setOnClickListener(new OnClickListener(){

				@Override
				public void onClick(View v) {
					finish();
				}
			});
		}
		left_img = (ImageView) findViewById(R.id.left);
		left_text = (TextView) findViewById(R.id.left_text);
		
		right_layout = (RelativeLayout) findViewById(R.id.right_layout);
		right_img = (ImageView) findViewById(R.id.right);
		right_text = (TextView) findViewById(R.id.right_text);
		
		title = (TextView) findViewById(R.id.middle_title);
	}*/
}
