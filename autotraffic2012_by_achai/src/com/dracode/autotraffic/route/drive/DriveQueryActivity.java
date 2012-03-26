package com.dracode.autotraffic.route.drive;

import com.dracode.autotraffic.R;
import com.dracode.autotraffic.bus.buschange.BusChangeQueryActivity;
import com.dracode.autotraffic.main.MainActivityHelper;
import com.dracode.autotraffic.route.BusRouteQueryActivity;
import com.dracode.autotraffic.route.MetroRouteQueryActivity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;

public class DriveQueryActivity extends BusChangeQueryActivity{
	public DriveQueryActivityHelper driveHelper;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		curQueryType="DRIVEROUTE";
		super.onCreate(savedInstanceState);
	}
	
	@Override
	public void doSetContentView() {
		setContentView(R.layout.act_route_index);
	}

	@Override
	protected void createHelper() {
		driveHelper=new DriveQueryActivityHelper();
		theHelper=driveHelper;
	}

	@Override
	protected void initControls() {
		super.initControls();

		ImageButton imgBtn_Bus = (ImageButton) findViewById(R.id.bus_bt);
		ImageButton imgBtn_Metro = (ImageButton) findViewById(R.id.metro_bt);
		ImageButton imgBtn_Drive = (ImageButton) findViewById(R.id.drive_bt);
		
		imgBtn_Drive.setSelected(true);
		imgBtn_Metro.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				if(MainActivityHelper.getMainActHelper()!=null)
					MainActivityHelper.getMainActHelper().openActInContent(MetroRouteQueryActivity.class);
			}
		});
		imgBtn_Bus.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if(MainActivityHelper.getMainActHelper()!=null)
					MainActivityHelper.getMainActHelper().openActInContent(BusRouteQueryActivity.class);
			}
		});
		
	}
}
