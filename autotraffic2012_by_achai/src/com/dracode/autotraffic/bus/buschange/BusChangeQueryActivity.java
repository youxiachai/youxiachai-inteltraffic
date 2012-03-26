package com.dracode.autotraffic.bus.buschange;

import com.dracode.andrdce.ct.UserAppSession;
import com.dracode.autotraffic.R;
import com.dracode.autotraffic.common.helpers.BaseActivityHelper;
import com.dracode.autotraffic.common.map.SelectMapPointActivity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
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

public class BusChangeQueryActivity extends Activity {
	public BusChangeQueryActivityHelper theHelper = null;

	private ImageButton imgBtn_Switch;
	private ImageButton imgBtn_StartDialog;
	private ImageButton imgBtn_EndDialog;
	private ImageButton imgBtn_clearStartInput;
	private ImageButton imgBtn_clearEndInput;
	public EditText editText_Start;
	public EditText editText_End;
	public Button button_Query;

	protected String curQueryType = "BUSCHANGE";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		createHelper();
		doSetContentView();

		BaseActivityHelper.initBusQueryAct(this, 3);

		initControls();
		theHelper.init(this);
	}

	protected void createHelper() {
		theHelper=new BusChangeQueryActivityHelper();
	}

	protected String getCurQueryType() {
		return curQueryType;
	}

	public void doSetContentView() {
		setContentView(R.layout.act_bus_change_query);
	}

	@Override
	protected void onResume() {
		theHelper.reloadData(getCurQueryType());
		super.onResume();
	}

	/**
	 * 初始化页面控件和添加事件
	 */
	protected void initControls() {
		imgBtn_Switch = (ImageButton) findViewById(R.id.switch_pic);
		editText_Start = (EditText) this.findViewById(R.id.start_input);
		editText_End = (EditText) this.findViewById(R.id.end_input);
		button_Query = (Button) this.findViewById(R.id.QueryOk);
		imgBtn_StartDialog = (ImageButton) findViewById(R.id.showdialogOne);
		imgBtn_EndDialog = (ImageButton) findViewById(R.id.showdialogTwo);
		imgBtn_clearStartInput = (ImageButton) findViewById(R.id.clear_start_input);
		imgBtn_clearEndInput = (ImageButton) findViewById(R.id.clear_end_input);

		setListener();
	}

	private void setListener() {
		OnClickListener ls = new OnClickListener() {
			public void onClick(View v) {
				hideKeyboard(v);
				String startPoint = editText_Start.getText().toString();
				String endPoint = editText_End.getText().toString();
				theHelper.callStationNameQuery(UserAppSession.cur_CityCode,
						startPoint, endPoint);
			}
		};
		button_Query.setOnClickListener(ls);

		ls = new OnClickListener() {
			public void onClick(View v) {
				hideKeyboard(v);
				String _start = editText_Start.getText().toString();
				Object tg = editText_Start.getTag();
				editText_Start.setText(editText_End.getText().toString());
				editText_Start.setTag(editText_End.getTag());
				editText_End.setText(_start);
				editText_End.setTag(tg);
			}
		};
		imgBtn_Switch.setOnClickListener(ls);

		ls = new OnClickListener() {
			public void onClick(View v) {
				hideKeyboard(v);
				theHelper.showFavSelector(1);
			}
		};
		imgBtn_StartDialog.setOnClickListener(ls);

		ls = new OnClickListener() {
			public void onClick(View v) {
				hideKeyboard(v);
				theHelper.showFavSelector(2);
			}
		};
		imgBtn_EndDialog.setOnClickListener(ls);

		editText_Start.addTextChangedListener(new TextWatcher() {
			@Override
			public void afterTextChanged(Editable arg0) {
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				if (editText_Start.getText().toString().length() > 0)
					imgBtn_clearStartInput.setVisibility(View.VISIBLE);
				else
					imgBtn_clearStartInput.setVisibility(View.GONE);
			}
		});
		editText_End.addTextChangedListener(new TextWatcher() {
			@Override
			public void afterTextChanged(Editable arg0) {
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				if (editText_End.getText().toString().length() > 0)
					imgBtn_clearEndInput.setVisibility(View.VISIBLE);
				else
					imgBtn_clearEndInput.setVisibility(View.GONE);
			}
		});
		imgBtn_clearStartInput.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				editText_Start.setText(null);
				editText_Start.setTag(null);
				editText_Start.requestFocus();
			}
		});
		imgBtn_clearEndInput.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				editText_End.setText(null);
				editText_End.setTag(null);
				editText_End.requestFocus();
			}
		});
	}

	/**
	 * 隐藏键盘
	 */
	private void hideKeyboard(View v) {
		InputMethodManager imm = (InputMethodManager) v.getContext()
				.getSystemService(Context.INPUT_METHOD_SERVICE);
		if (imm.isActive()) {
			imm.hideSoftInputFromWindow(v.getApplicationWindowToken(), 0);
		}
	}

	public void setCurrentCityAndStation(String city, String start, String end) {
		TextView tx = (TextView) this.findViewById(R.id.middle_title);
		tx.setText(UserAppSession.cur_CityName);
		//editText_Start.setText(start);
		//editText_End.setText(end);
	}


	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		Bundle ext = null;
		if (data != null)
			ext = data.getExtras();
		switch (requestCode) {
		case SelectMapPointActivity.SEL_MAP_POINT_REQ_CODE:
			if (resultCode == RESULT_OK) {
				double x=ext.getDouble("x");
				double y=ext.getDouble("y");
				String param=ext.getString("param");
				theHelper.onMapPointSelected(param, x,y);
			}
		}
	}
}
