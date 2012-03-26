package com.dracode.autotraffic.my;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.dracode.andrdce.ct.UserAppSession;
import com.dracode.autotraffic.R;
import com.dracode.autotraffic.common.helpers.BaseActivityHelper;

public class LoginActivity extends Activity {
	protected BaseActivityHelper baseHelper = new BaseActivityHelper();

	/** 登陆按钮. */
	private Button login_Bt;
	/** 忘记密码. */
	private TextView forget_password;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.act_login);

		baseHelper.init(this);

		initControls();
	}

	/**
	 * 初始化页面控件和添加事件
	 */
	private void initControls() {
		baseHelper.title_view.setText("登陆");
		baseHelper.right_img.setBackgroundResource(R.drawable.icon_register);
		baseHelper.right_text.setText("注册");

		login_Bt = (Button) findViewById(R.id.QueryOk);
		login_Bt.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				UserAppSession.cursession().isLogin = true;
				Intent intent = new Intent(LoginActivity.this,
						MyIndexActivity.class);
				startActivity(intent);
			}
		});

		forget_password = (TextView) findViewById(R.id.forget_password);
		forget_password.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent intent = new Intent(LoginActivity.this,
						FindPasswordActivity.class);
				startActivity(intent);
			}
		});
	}
}
