package com.dracode.autotraffic.serves.passenger;

import com.dracode.autotraffic.R;
import com.dracode.autotraffic.common.helpers.BaseActivityHelper;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class PassengerCarQueryActivity extends Activity {

	private BaseActivityHelper baseHelper;

	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.act_passengercar_query);

		baseHelper = new BaseActivityHelper();
		baseHelper.init(this);

		
		initContros();
	}
	
	private void initContros(){
		baseHelper.right_layout.setVisibility(View.GONE);
		baseHelper.title_view.setText("长途客车");
		
		Button query_Bt = (Button) findViewById(R.id.QueryOk);
		query_Bt.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View arg0) {
				Intent intent = new Intent(baseHelper.theAct,PassengerCarResultListActivity.class);
				startActivity(intent);
			}
		});
	}
}
