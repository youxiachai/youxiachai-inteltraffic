package com.dracode.autotraffic.main;

import com.dracode.andrdce.ct.UserAppSession;
import com.dracode.autotraffic.R;

import android.app.Activity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.widget.TextView;

public class WelcomeActivity extends Activity {
	private WelcomeActivityHelper theHelper=new WelcomeActivityHelper();

	@Override
	public void onCreate(Bundle savedInstanceState) {
		UserAppSession.cursession().onAppStart();
		super.onCreate(savedInstanceState);
		setContentView(R.layout.act_welcome);

		TextView verView = (TextView) findViewById(R.id.version);
		theHelper.init(this);
		
		verView.setText("Version " + getResources().getString(R.string.app_version));
	}

	@Override
	protected void onResume() {
		theHelper.startProgressAnim();
		super.onResume();
	}

	@Override
	protected void onStop() {
		theHelper.stopProgressAnim();
		super.onStop();
	}

	@Override
	public boolean dispatchKeyEvent(KeyEvent event) {
		if(event.getKeyCode()==KeyEvent.KEYCODE_BACK)
			return true;
		return super.dispatchKeyEvent(event);
	}

}