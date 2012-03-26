package com.dracode.autotraffic.main;

import android.app.ActivityGroup;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import com.dracode.andrdce.ct.AppUtil;
import com.dracode.andrdce.ct.UserAppSession;
import com.dracode.andrdce.ctact.TraceLogActivity;
import com.dracode.autotraffic.R;
import com.dracode.autotraffic.my.HelpActivity;

public class MainActivity extends ActivityGroup {
	public MainActivityHelper theHelper = new MainActivityHelper();

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.act_main);	
		try {
			initControls();
			theHelper.init(this);
			UserAppSession.cursession().setMainAct(this);

			// cacheTestDemo();
		} catch (Throwable ex) {
			ex.printStackTrace();
			UserAppSession.showToast(this, "系统启动出现异常：" + ex.getMessage());
		}
	}

	private void initControls() {
		theHelper.container = (LinearLayout) this.findViewById(R.id.container);
		OnClickListener ls;
		ls = new OnClickListener() {
			public void onClick(View v) {
				int idx = ((Number) v.getTag()).intValue();
				theHelper.doClickMenuButton(idx);
			}
		};
		theHelper.menuBtns.clear();
		if(UserAppSession.cur_CityCode==null||"".equals(UserAppSession.cur_CityCode)){
			
		} else {
			if(UserAppSession.cur_CityCode.equals("020")){
				theHelper.addMenuBtn((ImageButton) this.findViewById(R.id.footer_one));
				theHelper.addMenuBtn((ImageButton) this.findViewById(R.id.footer_two));
				theHelper.addMenuBtn((ImageButton) this.findViewById(R.id.footer_three));
				theHelper.addMenuBtn((ImageButton) this.findViewById(R.id.footer_six));
				theHelper.addMenuBtn((ImageButton) this.findViewById(R.id.footer_four));
				ImageButton routeBt = (ImageButton) this.findViewById(R.id.footer_six);
				ImageButton myBt = (ImageButton) this.findViewById(R.id.footer_five);
				routeBt.setVisibility(View.VISIBLE);
				myBt.setVisibility(View.GONE);
			} else {
				theHelper.addMenuBtn((ImageButton) this.findViewById(R.id.footer_one));
				theHelper.addMenuBtn((ImageButton) this.findViewById(R.id.footer_two));
				theHelper.addMenuBtn((ImageButton) this.findViewById(R.id.footer_three));
				theHelper.addMenuBtn((ImageButton) this.findViewById(R.id.footer_four));
				theHelper.addMenuBtn((ImageButton) this.findViewById(R.id.footer_five));
				ImageButton routeBt = (ImageButton) this.findViewById(R.id.footer_six);
				ImageButton myBt = (ImageButton) this.findViewById(R.id.footer_five);
				routeBt.setVisibility(View.GONE);
				myBt.setVisibility(View.VISIBLE);
			}
		}
		for (ImageButton btn : theHelper.menuBtns) {
			btn.setOnClickListener(ls);
		}
	}

	@Override
	public boolean dispatchKeyEvent(KeyEvent event) {
		if (theHelper.doDispatchKeyEvent(event))
			return true;
		return super.dispatchKeyEvent(event);
	}

	/**
	 * menu菜单
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(Menu.NONE, Menu.FIRST+1, 1, "帮助").setIcon(R.drawable.icon_help);
		menu.add(Menu.NONE, Menu.FIRST+2, 2, "反馈").setIcon(R.drawable.bus_feedback_button);
		menu.add(Menu.NONE, Menu.FIRST + 88, 88, "调试").setIcon(
				R.drawable.bus_copy_button);
		menu.add(Menu.NONE, Menu.FIRST + 99, 99, "退出").setIcon(
				R.drawable.icon_exit);
		return true;
	}

	/**
	 * menu选择事件
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		// 操作说明
		case Menu.FIRST+1:
			Intent intent = new Intent(MainActivity.this,HelpActivity.class);
		    startActivity(intent);
			break;
		case Menu.FIRST+2:
			theHelper.showFeedBack();
			break;
		case Menu.FIRST + 88:
			AppUtil.startActivity(this, TraceLogActivity.class, false, null);
			break;
		// 退出系统
		case Menu.FIRST + 99:
//			finish();
		    AppUtil.exitApp(this, false);
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	@Override
	protected void onPause() {
		super.onPause();
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		UserAppSession.LogD("main act Save state");
		super.onSaveInstanceState(outState);
	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		UserAppSession.LogD("main act restore state");
		super.onRestoreInstanceState(savedInstanceState);
	}

	@Override
	protected void onResume() {
		UserAppSession.cursession().setMainAct(this);
		initControls();
		if(UserAppSession.isNeedToHome){
			theHelper.init(this);	
		}
		UserAppSession.isNeedToHome = false;
		super.onResume();
	}

	@Override
	protected void onStop() {
		super.onStop();
	}

	@Override
	public void finish() {
		theHelper.doActFinish();
		super.finish();
	}
}