package com.dracode.autotraffic.my;

import com.dracode.andrdce.ct.UserAppSession;
import com.dracode.autotraffic.R;
import com.dracode.autotraffic.common.helpers.BaseActivityHelper;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

/**
 * 类<code>AgreementActivity</code> 隐私协议页面.
 * 
 * @author Figo.Gu
 * @version 01.00 2012/1/6
 * @see java.lang.Class
 * @since JDK 1.6
 */
public class HelpActivity extends Activity {
	protected BaseActivityHelper baseHelper = null;

	/** 版本. */
	private TextView versionView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.act_help);

		baseHelper = new BaseActivityHelper();
		baseHelper.init(this);

		versionView = (TextView) findViewById(R.id.version);
		String version = this.getResources().getString(
				R.string.app_compare_version);
		if (UserAppSession.newest_version != null
				&& !"".equals(UserAppSession.newest_version)) {
			versionView.setText(" 当前版本：" + version + "\n 最新版本："
					+ UserAppSession.newest_version);
		} else {
			versionView.setText(" 当前版本：" + version + "\n 最新版本：" + version);
		}

		baseHelper.right_layout.setVisibility(View.GONE);
		baseHelper.title_view.setText("帮助指南");
	}
}
