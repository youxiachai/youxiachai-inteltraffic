package com.dracode.autotraffic.serves;

import java.util.ArrayList;
import java.util.HashMap;

import com.dracode.andrdce.ct.AppUtil;
import com.dracode.andrdce.ct.UserAppSession;
import com.dracode.autotraffic.R;
import com.dracode.autotraffic.common.helpers.BaseActivityHelper;
import com.dracode.autotraffic.common.switchcity.SwitchCityActivity;
import com.dracode.autotraffic.common.webview.ShowWebView;
import com.dracode.autotraffic.my.MyIndexActivity;
import com.dracode.autotraffic.serves.taxicall.TaxiCallMapIndex;
import com.dracode.autotraffic.serves.icarcar.IcarCarIndexActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

public class ServiceIndexActivity extends Activity {

	/** 珠海服务.*/
	private int[] zh_menu_imge_array = {R.drawable.service01,R.drawable.service02,R.drawable.service03,R.drawable.service06,R.drawable.service07,R.drawable.service05,R.drawable.service08,R.drawable.service10};
	/**广州下功能 .*/
	private int[] gz_menu_imge_array = {R.drawable.icon_25,R.drawable.service01,R.drawable.service02,R.drawable.service03,R.drawable.service11,R.drawable.service10};
	/** 其他城市服务.*/
	private int[] other_menu_imge_array = {R.drawable.service01,R.drawable.service02,R.drawable.service03};
	/** 图片控件.*/
	private GridView menu_gridView;
    private RelativeLayout right_layout;
//    private ImageView dotIm01g;
    /** 头部标题.*/
	protected TextView title_view;
	protected ImageView more_city;
	private RelativeLayout switch_City;
	private BaseActivityHelper baseHelper;
    
	@Override
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.act_service_index);

		baseHelper = new BaseActivityHelper();
		baseHelper.init(this);

		initControls();
		setGridMenu();
	}
	/**
	 * 初始化页面控件
	 */
	private void initControls(){
		right_layout = (RelativeLayout) findViewById(R.id.right_layout);
		right_layout.setVisibility(View.GONE);
	
		title_view = (TextView) findViewById(R.id.middle_title);
		title_view.setText(UserAppSession.cur_CityName);
		more_city = (ImageView)findViewById(R.id.more_city);
//		dotIm01g = (ImageView) findViewById(R.id.one);
//		dotIm01g.setBackgroundResource(R.drawable.dot_green);
		menu_gridView = (GridView) findViewById(R.id.menu_list);
		switch_City = (RelativeLayout) findViewById(R.id.switch_city_layout);
		switch_City.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(ServiceIndexActivity.this,SwitchCityActivity.class);
				startActivity(intent);
			}
		});
	}
	private void setGridMenu() {
		if("020".equals(UserAppSession.cur_CityCode)){
			menu_gridView.setAdapter(getAdapter(gz_menu_imge_array));
		} else if("0756".equals(UserAppSession.cur_CityCode)){
			menu_gridView.setAdapter(getAdapter(zh_menu_imge_array));
		} else {
			menu_gridView.setAdapter(getAdapter(other_menu_imge_array));
		}
		
		menu_gridView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1,
					int position, long arg3) {
				Bundle bundle = new Bundle();
				String msg = "本应用即将开通，敬请关注。";
				if("020".equals(UserAppSession.cur_CityCode)){
					switch (position) {
					case 0:					
						AppUtil.startActivity(ServiceIndexActivity.this,  MyIndexActivity.class, false, bundle);
						break;
					case 1:	
//						UserAppSession.showToast(baseHelper.theAct, msg);
						bundle.putString("title", "飞机");
						bundle.putString("url", "http://wap.133.cn/outlink/gdwx/index.jsp");
						AppUtil.startActivity(ServiceIndexActivity.this, ShowWebView.class, false, bundle);
						break;
					case 2:
						bundle.putString("title", "火车");
						bundle.putString("url", "http://wap.21rail.com/mobile/rail_flow/index.jsp");
						AppUtil.startActivity(ServiceIndexActivity.this, ShowWebView.class, false, bundle);
						break;
					case 3:
						bundle.putString("title", "客车");
						bundle.putString("url", "http://www.keyunzhan.com/");
						AppUtil.startActivity(ServiceIndexActivity.this, ShowWebView.class, false, bundle);
						break;
					case 4:	
						bundle.putString("title", "地铁");
						bundle.putString("url", "http://wap.wirelessgz.cn/smtf/phone/IG/travel/subwaySearch.vm");
						AppUtil.startActivity(ServiceIndexActivity.this, ShowWebView.class, false, bundle);
						break;
					case 5:	
						bundle.putString("title", "交警专栏");
						bundle.putString("url", "http://wap.wirelessgz.cn/smtf/phone/IG/weibo/index.vm");
						AppUtil.startActivity(ServiceIndexActivity.this, ShowWebView.class, false, bundle);
						break;
					}			
				} else if("0756".equals(UserAppSession.cur_CityCode)){
					switch (position) {
					case 0:
						bundle.putString("title", "飞机");
						bundle.putString("url", "http://wap.133.cn/outlink/gdwx/index.jsp");
						AppUtil.startActivity(ServiceIndexActivity.this, ShowWebView.class, false, bundle);
						break;
					case 1:	
						bundle.putString("title", "火车");
						bundle.putString("url", "http://wap.21rail.com/mobile/rail_flow/index.jsp");
						AppUtil.startActivity(ServiceIndexActivity.this, ShowWebView.class, false, bundle);
//						AppUtil.startActivity(ServiceIndexActivity.this,TrainQueryActivity.class, false, bundle);
						break;
					case 2:
						bundle.putString("title", "客车");
						bundle.putString("url", "http://www.keyunzhan.com/");
						AppUtil.startActivity(ServiceIndexActivity.this, ShowWebView.class, false, bundle);
//						AppUtil.startActivity(ServiceIndexActivity.this,PassengerCarQueryActivity.class, false, bundle);
						break;
					case 3:
//						AppUtil.startActivity(ServiceIndexActivity.this,PlaneFastLineQueryActivity.class, false, bundle);
						break;
					case 4:	
						Toast.makeText(baseHelper.theAct, msg, Toast.LENGTH_LONG).show();
						break;
					case 5:		
//						UserAppSession.showToast(baseHelper.theAct, msg);
						AppUtil.startActivity(ServiceIndexActivity.this,TaxiCallMapIndex.class, false, bundle);
						break;
					case 6:
//						UserAppSession.showToast(baseHelper.theAct, msg);
						AppUtil.startActivity(ServiceIndexActivity.this, IcarCarIndexActivity.class, false, bundle);
						break;
					case 7:
						bundle.putString("title", "交警专栏");
						bundle.putString("url", "http://wap.wirelessgz.cn/smtf/phone/IG/weibo/index.vm");
						AppUtil.startActivity(ServiceIndexActivity.this, ShowWebView.class, false, bundle);
//						AppUtil.startActivity(ServiceIndexActivity.this,TrafficPoliceQueryActivty.class, false, bundle);
						break;
					}			
				} else {
					switch (position) {
					case 0:
						bundle.putString("title", "飞机");
						bundle.putString("url", "http://wap.133.cn/outlink/gdwx/index.jsp");
						AppUtil.startActivity(ServiceIndexActivity.this, ShowWebView.class, false, bundle);
						break;
					case 1:	
						bundle.putString("title", "火车");
						bundle.putString("url", "http://wap.21rail.com/mobile/rail_flow/index.jsp");
						AppUtil.startActivity(ServiceIndexActivity.this, ShowWebView.class, false, bundle);
//						AppUtil.startActivity(ServiceIndexActivity.this,TrainQueryActivity.class, false, bundle);
						break;
					case 2:
						bundle.putString("title", "客车");
						bundle.putString("url", "http://www.keyunzhan.com/");
						AppUtil.startActivity(ServiceIndexActivity.this, ShowWebView.class, false, bundle);
//						AppUtil.startActivity(ServiceIndexActivity.this,PassengerCarQueryActivity.class, false, bundle);
						break;
					}
				}
			}
		});
	}
	//将数据构成simpleAdpter对象
	private SimpleAdapter getAdapter(int[] imageResourceArray) {
		ArrayList<HashMap<String, Object>> data = new ArrayList<HashMap<String, Object>>();
		for (int i = 0; i < imageResourceArray.length; i++) {
			HashMap<String, Object> map = new HashMap<String, Object>();
			map.put("itemImage", imageResourceArray[i]);
			data.add(map);
		}
		SimpleAdapter simperAdapter = new SimpleAdapter(this, data,
				R.layout.service_index_item, new String[] { "itemImage"},
				new int[] { R.id.toolImage });
		return simperAdapter;
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		title_view.setText(UserAppSession.cur_CityName);
		setGridMenu();
	}
}
