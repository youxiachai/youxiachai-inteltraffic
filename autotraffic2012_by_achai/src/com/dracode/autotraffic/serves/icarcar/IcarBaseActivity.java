package com.dracode.autotraffic.serves.icarcar;

import android.app.Activity;
import com.dracode.autotraffic.common.helpers.BaseActivityHelper;

public class IcarBaseActivity extends Activity {

	protected BaseActivityHelper baseHelper;
	protected void init() {
		baseHelper = new BaseActivityHelper();
		baseHelper.init(this);
	}
}
