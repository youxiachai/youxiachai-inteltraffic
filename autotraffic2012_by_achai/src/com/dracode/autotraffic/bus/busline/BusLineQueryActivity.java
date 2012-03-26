package com.dracode.autotraffic.bus.busline;

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

public class BusLineQueryActivity  extends Activity {
	public BusLineQueryActivityHelper theHelper=new BusLineQueryActivityHelper();
	
	private EditText editText_BusName;
	private Button button_BusLineQuery;
	private ImageButton del_input;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.act_bus_line_query);

		BaseActivityHelper.initBusQueryAct(this, 1);
		
		initControls();
		theHelper.init(this);
	}

	/** 
	 * 初始化页面控件和添加事件
	 */
	private void initControls() {
		editText_BusName=(EditText)this.findViewById(R.id.start_input) ;
		del_input = (ImageButton) findViewById(R.id.del_input);
		button_BusLineQuery=( Button)this.findViewById(R.id.QueryOk) ;
		
		OnClickListener ls=new OnClickListener(){
			public void onClick(View v) {
				InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.hideSoftInputFromWindow(editText_BusName.getWindowToken(), 0);
				
				theHelper.callBusNameQuery(UserAppSession.cur_CityCode,editText_BusName.getText().toString());
			}
		};
		button_BusLineQuery.setOnClickListener(ls);
		
		editText_BusName.addTextChangedListener(new TextWatcher(){
			@Override
			public void afterTextChanged(Editable arg0) {}
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,int after) {}
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				if(editText_BusName.getText().toString().length() > 0)
					del_input.setVisibility(View.VISIBLE);
				else
					del_input.setVisibility(View.GONE);
			}
		});
		del_input.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				editText_BusName.setText(null);
				editText_BusName.requestFocus();
			}
		});
		

		ImageButton bus_celection=(ImageButton)this.findViewById(R.id.bus_celection);
		ls=new OnClickListener(){
			public void onClick(View v) {
				theHelper.callFavoriteMenu();
			}
		};
		bus_celection.setOnClickListener(ls);
		
	}

	@Override
	protected void onResume() {
		theHelper.reloadData();
		super.onResume();
	}

	public void setCurrentCityAndBus(String city, String bus) {
		TextView tx = (TextView) this.findViewById(R.id.middle_title);
		tx.setText(UserAppSession.cur_CityName);
		editText_BusName.setText(bus);
	}

	public void doBusNameSelected(String busName){
		theHelper.callBusLineQuery(UserAppSession.cur_CityCode,busName, "0");
	}
}
