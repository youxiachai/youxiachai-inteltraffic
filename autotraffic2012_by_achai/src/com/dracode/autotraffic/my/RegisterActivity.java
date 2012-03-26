package com.dracode.autotraffic.my;

import android.app.Activity;
import android.os.Bundle;

import com.dracode.autotraffic.R;
import com.dracode.autotraffic.common.helpers.BaseActivityHelper;

public class RegisterActivity extends Activity {

	 private BaseActivityHelper baseHelper;

	@Override    
	   public void onCreate(Bundle savedInstanceState) {    
	        super.onCreate(savedInstanceState);    
	        setContentView(R.layout.act_register); 

			baseHelper = new BaseActivityHelper();
			baseHelper.init(this);
	        
	        baseHelper.title_view.setText("注册");
	        baseHelper.right_img.setBackgroundResource(R.drawable.icon_register);
	        baseHelper.right_text.setText("完成");
	   }
}
