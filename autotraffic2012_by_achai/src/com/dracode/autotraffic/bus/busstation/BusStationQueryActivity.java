package com.dracode.autotraffic.bus.busstation;

import com.dracode.andrdce.ct.UserAppSession;
import com.dracode.autotraffic.R;
import com.dracode.autotraffic.common.helpers.BaseActivityHelper;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

public class BusStationQueryActivity  extends Activity {
	public BusStationQueryActivityHelper theHelper=new BusStationQueryActivityHelper();
	
	private EditText editText_StationName;
	private Button button_BusStationQuery;
	private ImageButton del_input;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.act_bus_station_query);

		BaseActivityHelper.initBusQueryAct(this, 2);
		
		initControls();
		theHelper.init(this);
	}

	/** 
	 * 初始化页面控件和添加事件
	 */
	private void initControls() {
		editText_StationName=(EditText)this.findViewById(R.id.start_input) ;
		del_input = (ImageButton) findViewById(R.id.del_input);
		button_BusStationQuery=( Button)this.findViewById(R.id.QueryOk) ;
		
		OnClickListener ls=new OnClickListener(){
			public void onClick(View v) {
				InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.hideSoftInputFromWindow(editText_StationName.getWindowToken(), 0);
				
				theHelper.callStationNameQuery(UserAppSession.cur_CityCode,editText_StationName.getText().toString());
			}
		};
		button_BusStationQuery.setOnClickListener(ls);

		ImageButton bus_celection=(ImageButton)this.findViewById(R.id.bus_celection);
		ls=new OnClickListener(){
			public void onClick(View v) {
				theHelper.callFavoriteMenu();
			}
		};
		bus_celection.setOnClickListener(ls);
		
		editText_StationName.addTextChangedListener(new TextWatcher(){
			@Override
			public void afterTextChanged(Editable arg0) {}
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,int after) {}
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				if(editText_StationName.getText().toString().length() > 0)
					del_input.setVisibility(View.VISIBLE);
				else
					del_input.setVisibility(View.GONE);
			}
		});
		del_input.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				editText_StationName.setText(null);
				editText_StationName.requestFocus();
			}
		});
	}

	@Override
	protected void onResume() {
		theHelper.reloadData();
		super.onResume();
	}

	public void setCurrentCityAndStation(String city, String station) {
		TextView tx = (TextView)this.findViewById(R.id.middle_title);
		tx.setText(UserAppSession.cur_CityName);
		editText_StationName.setText(station);
	}

	public void doBusNameSelected(String stationName){
		theHelper.callBusStationQuery(UserAppSession.cur_CityCode,stationName);
	}
}
