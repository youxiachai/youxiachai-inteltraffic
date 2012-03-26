package com.dracode.autotraffic.nearby;

import com.dracode.andrdce.ct.AppUtil;
import com.dracode.andrdce.ct.UserAppSession;
import com.dracode.autotraffic.R;
import com.dracode.autotraffic.common.helpers.BaseActivityHelper;
import com.dracode.autotraffic.common.map.LocationHelper;
import com.dracode.autotraffic.common.switchcity.SwitchCityActivity;
import com.iflytek.speech.SpeechConfig.RATE;
import com.iflytek.ui.RecognizerDialog;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

public class NearbyQueryActivity extends Activity {
	public NearbyQueryActivityHelper theHelper = new NearbyQueryActivityHelper();
	
	/** 距离分类 .*/
	public Spinner distanceSprinner;
	/** 搜索编辑框 .*/
	public EditText searchEdit;
	/** 搜索/语音按钮 .*/
	public ImageButton searchImgBtn;
	/** 清空按钮 .*/
	public ImageButton delInputImgBtn;
	private TextView text_City;

	private BaseActivityHelper baseHelper;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.act_nearby_query);

		baseHelper = new BaseActivityHelper();
		baseHelper.init(this);

		initControls();
		theHelper.init(this);
		
	}
	@Override
	protected void onResume() {
		super.onResume();
		text_City.setText(UserAppSession.cur_CityName);
		LocationHelper.getMyLocation(this, null);
	}
	@Override
	protected void onPause() {
		super.onPause();
	}
	/** 
	 * 初始化页面控件和添加事件
	 */
	private void initControls() {
		// 距离
		distanceSprinner = (Spinner) findViewById(R.id.distance);
		// 搜索/语音输入
		searchImgBtn = (ImageButton) findViewById(R.id.search_btn);
		// 清空按钮
		delInputImgBtn = (ImageButton) findViewById(R.id.del_input);
		delInputImgBtn.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				searchEdit.setText(null);
			}
		});
		// 搜索编辑框
		searchEdit = (EditText) findViewById(R.id.search_text);
		searchEdit.setFocusable(true);
		searchEdit.addTextChangedListener(new TextWatcher(){
			@Override
			public void afterTextChanged(Editable arg0) {}
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,int after) {}
			@Override
			public void onTextChanged(CharSequence s, int start, int before,int count) {
				if(!"".equals(searchEdit.getText().toString())){
					delInputImgBtn.setVisibility(View.VISIBLE);
					searchImgBtn.setTag("search");
					searchImgBtn.setBackgroundResource(R.drawable.nearby_search_button);
				} else {
					delInputImgBtn.setVisibility(View.GONE);
					searchImgBtn.setTag("speech");
					searchImgBtn.setBackgroundResource(R.drawable.nearby_speech_button);
				}
			}
		});
		// 切换城市
		RelativeLayout switch_city = (RelativeLayout) findViewById(R.id.switch_city_layout);
		if (switch_city != null) {
			switch_city.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					AppUtil.startActivity(NearbyQueryActivity.this, SwitchCityActivity.class, false, null);
				}
			});
		}
		// 当前城市
		text_City = (TextView)this.findViewById(R.id.middle_title);

		RelativeLayout fresh_layout = (RelativeLayout) findViewById(R.id.right_layout);
		fresh_layout.setVisibility(View.VISIBLE);
		fresh_layout.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				theHelper.nearbyDataIntf.getSortList(true);// 加载缓存
			}
		});
		ImageView fresh_img = (ImageView) this.findViewById(R.id.right_img);
		fresh_img.setBackgroundResource(R.drawable.icon_refresh2);
		TextView fresh_text = (TextView) findViewById(R.id.right_text);
		fresh_text.setText("刷新");
	}
	
	/**
	 * 隐藏键盘
	 */
	public void hideKeyboard(View v) {
		InputMethodManager imm = (InputMethodManager) v.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
		if (imm.isActive()) {
			imm.hideSoftInputFromWindow(v.getApplicationWindowToken(), 0);
		}
	}
	
	/**
	 * 语音识别
	 */
	public void showIsrDialog() {
		RecognizerDialog isrDialog = new RecognizerDialog(this, "appid=" + getString(R.string.app_id));
		isrDialog.setListener(theHelper.recoListener);
		isrDialog.setEngine("poi", "ptt=0", null);
		isrDialog.setSampleRate(RATE.rate16k);
		isrDialog.show();
	}
}
