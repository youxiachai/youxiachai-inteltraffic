package com.dracode.andrdce.ctact;

import com.dracode.andrdce.ct.NetUtil;
import com.dracode.andrdce.ct.UserAppSession;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.ClipboardManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup.LayoutParams;
import android.widget.ScrollView;
import android.widget.TextView;

public class TraceLogActivity extends Activity {

	private TextView logTextView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		initControls();

		loadNetLogs();
	}

	private void initControls() {
		ScrollView scll = new ScrollView(this);
		scll.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT,
				LayoutParams.FILL_PARENT));

		logTextView = new TextView(this);
		logTextView.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT,
				LayoutParams.WRAP_CONTENT));
		scll.addView(logTextView);

		this.setContentView(scll);
	}

	protected void loadNetLogs() {
		String log = NetUtil.getLastNetResult();
		if(log.length()==0)
			log="没有HTTP请求日志";
		logTextView.setText(log);
	}

	protected void loadSysErrorLogs() {
		String log = getLastSysError();
		logTextView.setText(log);
	}

	protected String getLastSysError() {
		SharedPreferences sp = this.getSharedPreferences("SysError",
				Context.MODE_WORLD_WRITEABLE | Context.MODE_WORLD_READABLE);
		String err = sp.getString("lastErrorMsg", null);
		if (err != null && err.length() == 0)
			err = null;
		if (err == null)
			err = "没有找到系统错误日志";
		return err;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(Menu.NONE, Menu.FIRST+1, 1, "网络日志");
		menu.add(Menu.NONE, Menu.FIRST + 2, 2, "崩溃日志");
		menu.add(Menu.NONE, Menu.FIRST + 3, 3, "切换服务");
		menu.add(Menu.NONE, Menu.FIRST + 97, 97, "复制");
		menu.add(Menu.NONE, Menu.FIRST + 98, 98, "清空");
		menu.add(Menu.NONE, Menu.FIRST + 99, 99, "返回");
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case Menu.FIRST+1:
			loadNetLogs();
			break;
		case Menu.FIRST + 2:
			loadSysErrorLogs();
			break;
		case Menu.FIRST + 3:
			switchServerAddr();
			break;
		case Menu.FIRST + 97:
			copyLogs();
			break;
		case Menu.FIRST + 98:
			clearLogs();
			break;
		case Menu.FIRST + 99:
			finish();
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	private void switchServerAddr() {
		if(UserAppSession.rtBusServerAddr.equals(UserAppSession.baServerAddr))
			UserAppSession.rtBusServerAddr=UserAppSession.baServerAddr_gzjw;
		else
			UserAppSession.rtBusServerAddr=UserAppSession.baServerAddr;
		logTextView.setText("当前公交服务地址临时切换为："+UserAppSession.rtBusServerAddr);
	}

	private void copyLogs() {
		ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
		clipboard.setText(logTextView.getText());
	}

	private void clearLogs() {

		new AlertDialog.Builder(this).setTitle("确定要删除历史日志？")
				.setPositiveButton("确定", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						doClearLogs();
					}

				}).setNegativeButton("取消", null).show();
	}

	private void doClearLogs() {
		SharedPreferences sp = this.getSharedPreferences("SysError",
				Context.MODE_WORLD_WRITEABLE | Context.MODE_WORLD_READABLE);
		sp.edit().putString("lastErrorMsg", "").commit();
		NetUtil.resetNetLogs();
		logTextView.setText("日志已清除");
	}
}
