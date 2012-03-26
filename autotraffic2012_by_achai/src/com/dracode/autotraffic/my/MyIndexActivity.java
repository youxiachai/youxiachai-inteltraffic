package com.dracode.autotraffic.my;

import java.util.Date;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.dracode.andrdce.ct.AutoUpdateUtil;
import com.dracode.andrdce.ct.NetUtil;
import com.dracode.andrdce.ct.UserAppSession;
import com.dracode.autotraffic.R;
import com.dracode.autotraffic.common.helpers.BaseActivityHelper;
import com.dracode.autotraffic.common.switchcity.SwitchCityActivity;

public class MyIndexActivity extends Activity {

	//未登录时显示
	private RelativeLayout unLogin_layout;
	//登陆后显示
	private RelativeLayout logined_layout;
	//我的消息
	private RelativeLayout messge_layout;
	//我的订单
	private RelativeLayout my_order_layout;
	//我的收藏
	private RelativeLayout my_collection_layout;
	//切换城市
	private RelativeLayout switch_city;
	//帮助指南
	private RelativeLayout help_layout;
	//设置首页
	private RelativeLayout set_home_layout;
	//首页名称
	private TextView home_name;
	//版本更新
	private RelativeLayout version_update_layout;
	//当前版本号
	private TextView cur_version_text;
	//当前城市
	private TextView cur_City;
	/** 广州首页列表 .*/
	private String[] gzIndexArray = {"公交", "附近", "路况", "路线", "服务"};
	/** 非广州首页列表 .*/
	private String[] indexArray = {"公交", "附近", "路况", "服务", "我的"};
	private String[] myIndexArray = null;
	
	private SharedPreferences sp;
	private BaseActivityHelper baseHelper;
	@Override    
	public void onCreate(Bundle savedInstanceState) {    
	    super.onCreate(savedInstanceState);    
	    setContentView(R.layout.act_my_index); 
	        
	    sp = getSharedPreferences("USER_HOME",
				Context.MODE_WORLD_WRITEABLE | Context.MODE_WORLD_READABLE);

		baseHelper = new BaseActivityHelper();
		baseHelper.init(this);
		
	    initControls();
	}
	
	/** 
	 * 初始化页面控件和添加事件
	 */
	private void initControls() {
		baseHelper.title_view.setText("我的");
		baseHelper.right_img.setBackgroundResource(R.drawable.icon_register);
		baseHelper.right_text.setText("反馈");
		baseHelper.left_layout.setVisibility(View.GONE);		
	   
	   unLogin_layout = (RelativeLayout) findViewById(R.id.login_layout);
	   logined_layout = (RelativeLayout) findViewById(R.id.login_layout1);
	   Button ligon_Bt = (Button) findViewById(R.id.login_bt);
	   Button register_Bt = (Button) findViewById(R.id.register);
	   Button loginOut_Bt = (Button) findViewById(R.id.logout);
	   
	   if(UserAppSession.cursession().isLogin){
		   logined_layout.setVisibility(View.VISIBLE);
		   unLogin_layout.setVisibility(View.GONE);
		   loginOut_Bt.setOnClickListener(new OnClickListener(){

				@Override
				public void onClick(View v) {
					UserAppSession.cursession().isLogin = false;
					unLogin_layout.setVisibility(View.VISIBLE);
				    logined_layout.setVisibility(View.GONE);
				}
		  });
		   
	    } else {
		   unLogin_layout.setVisibility(View.VISIBLE);
		   logined_layout.setVisibility(View.GONE);
		   ligon_Bt.setOnClickListener(new OnClickListener(){

				@Override
				public void onClick(View v) {
					Intent intent = new Intent(MyIndexActivity.this,LoginActivity.class);
					startActivity(intent);
				}
		   });
		   register_Bt.setOnClickListener(new OnClickListener(){

				@Override
				public void onClick(View v) {
					Intent intent = new Intent(MyIndexActivity.this,RegisterActivity.class);
					startActivity(intent);
				}
		   });	   
	   }	   
	   messge_layout = (RelativeLayout) findViewById(R.id.my_message);
	   messge_layout.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(baseHelper.theAct,MyMessageActivity.class);
				startActivity(intent);
			}
	   });
	   my_order_layout = (RelativeLayout) findViewById(R.id.my_order);
	   my_order_layout.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(baseHelper.theAct,MyOrderListActivity.class);
				startActivity(intent);
			}
	   });
	   my_collection_layout = (RelativeLayout) findViewById(R.id.my_collection);
	   my_collection_layout.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {				
				Intent intent = new Intent(baseHelper.theAct,MyCollectionListActivity.class);
				startActivity(intent);
			}
	   });
	   home_name = (TextView) findViewById(R.id.home_name);
	   if("020".equals(UserAppSession.cur_CityCode)){
		   if("我的".equals(sp.getString("user_home", "公交"))){
			   home_name.setText("公交");  
			   sp.edit().putString("user_home", "公交").commit();
		   } else {
			   home_name.setText(sp.getString("user_home", "公交"));
		   }
	   } else {
		   if("路线".equals(sp.getString("user_home", "公交"))){
			   home_name.setText("公交");  
			   sp.edit().putString("user_home", "公交").commit();
		   } else {
			   home_name.setText(sp.getString("user_home", "公交"));
		   }
	   }
	  
	   set_home_layout = (RelativeLayout) findViewById(R.id.set_index);
	   set_home_layout.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {				
				/** 用户当前角色 .*/
				String _userRole = sp.getString("user_home", "公交");
				int index = 0;
				if ("020".equals(UserAppSession.cur_CityCode)){
					myIndexArray = gzIndexArray;
					if ("公交".equals(_userRole)) {
						index = 0;
					} else if ("附近".equals(_userRole)) {
						index = 1;
					} else if ("路况".equals(_userRole)) {
						index = 2;
					} else if ("路线".equals(_userRole)) {
						index = 3;
					} else if ("服务".equals(_userRole)) {
						index = 4;
					} else {
						index = 0;
					}
				} else {
					myIndexArray = indexArray;
					if ("公交".equals(_userRole)) {
						index = 0;
					} else if ("附近".equals(_userRole)) {
						index = 1;
					} else if ("路况".equals(_userRole)) {
						index = 2;
					} else if ("服务".equals(_userRole)) {
						index = 3;
					} else if ("我的".equals(_userRole)) {
						index = 4;
					} else {
						index = 0;
					}
				}
				AlertDialog.Builder builder = new AlertDialog.Builder(baseHelper.theAct);  
				builder.setTitle("设置首页");
				builder.setSingleChoiceItems(myIndexArray, index, new DialogInterface.OnClickListener(){
					@Override
					public void onClick(DialogInterface dialog, int index) {
						sp.edit().putString("user_home", myIndexArray[index]).commit();
						home_name.setText(myIndexArray[index]);
						UserAppSession.isNeedToHome = true;
						dialog.dismiss();
					}
				});
				builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
			    });  
				builder.create().show();
			}
	   });
	   
	   help_layout = (RelativeLayout) findViewById(R.id.my_help);
	   help_layout.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {				
				Intent intent = new Intent(baseHelper.theAct,HelpActivity.class);
				startActivity(intent);
			}
	   });
	   version_update_layout = (RelativeLayout) findViewById(R.id.version_update);
	   version_update_layout.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				SharedPreferences app_params = getSharedPreferences("APP_PARAM",
						Context.MODE_WORLD_WRITEABLE | Context.MODE_WORLD_READABLE);
				app_params.edit().putLong("last_check_update_tm", 0).commit();
				
				String oldVersion = getResources().getString(R.string.app_compare_version);
				String newVersion = NetUtil.getUrlData(AutoUpdateUtil.getAutoUpdateUrl("CHECK_UPDATE_URL") + "?ver="
						+ oldVersion+"&tk="+Long.toString((new Date()).getTime()));

				if(!newVersion.equals("OK")){
					AutoUpdateUtil.currentUpdater().checkAutoUpdate(baseHelper.theAct, getResources().getString(R.string.app_compare_version));
				} else {
					AutoUpdateUtil.promptNoNewVersion(baseHelper.theAct);
				}
			}
	   });
	   cur_version_text = (TextView) findViewById(R.id.version);
	   cur_version_text.setText(R.string.app_compare_version);
	   
	   cur_City = (TextView) findViewById(R.id.cur_city);
	   switch_city = (RelativeLayout) findViewById(R.id.switch_city);
	   switch_city.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(MyIndexActivity.this,SwitchCityActivity.class);
				startActivity(intent);
			}
	   });
	}
	
	@Override
	protected void onResume(){
		cur_City.setText(UserAppSession.cur_CityName);
		super.onResume();
	}
}
