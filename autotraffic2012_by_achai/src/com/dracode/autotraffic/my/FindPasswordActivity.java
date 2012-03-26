package com.dracode.autotraffic.my;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;

import com.dracode.autotraffic.R;
import com.dracode.autotraffic.common.helpers.BaseActivityHelper;

public class FindPasswordActivity extends Activity {

	protected BaseActivityHelper baseHelper = new BaseActivityHelper();

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.act_find_password_view);

		baseHelper.init(this);
		initControls();
	}

	/**
	 * 初始化页面控件和添加事件
	 */
	private void initControls() {
		baseHelper.title_view.setText("找回密码");
		baseHelper.right_img.setBackgroundResource(R.drawable.icon_register);
		baseHelper.right_layout.setVisibility(View.GONE);
	}
}
