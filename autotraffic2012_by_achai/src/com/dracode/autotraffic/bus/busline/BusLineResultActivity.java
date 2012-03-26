package com.dracode.autotraffic.bus.busline;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.dracode.andrdce.ct.TypeUtil;
import com.dracode.andrdce.ct.UserAppSession;
import com.dracode.autotraffic.R;
import com.dracode.autotraffic.bus.busstation.BusStationInfo;
import com.dracode.autotraffic.common.helpers.BaseActivityHelper;

import android.app.Activity;
import android.os.Bundle;
import android.text.Html;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class BusLineResultActivity extends Activity {

	private BusLineResultActivityHelper theHelper = new BusLineResultActivityHelper();

	private TextView textView_BusName;
	/** 显示开往目的地名称. */
	private TextView endNameAndTimeView;
	/** 显示价格. */
//	private TextView priceView;
	/** 显示刷新时间.*/
	private TextView fresh_time;
	/** 线路上下行切换. */
	private ImageButton switch_line_Bt;
	/** 返回. */
	private RelativeLayout back_layout;
	/** 刷新. */
	private RelativeLayout fresh_layout;
	private ImageView fresh_img;
	private TextView fresh_text;
	/** 线路列表. */
	private ListView listView_BusLine;

	List<BusStationInfo> stationInfos = new ArrayList<BusStationInfo>();

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.act_bus_line_query_result_list);

		BaseActivityHelper.initAct(this);
		initControls();
		theHelper.init(this);
	}

	private void initControls() {
		back_layout = (RelativeLayout) findViewById(R.id.left_layout);
		back_layout.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				finish();
			}
		});
		fresh_layout = (RelativeLayout) findViewById(R.id.right_layout);
		fresh_layout.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				theHelper.execBusLineQuery(true);
			}
		});
		
		fresh_img = (ImageView) this.findViewById(R.id.right);
		fresh_img.setBackgroundResource(R.drawable.icon_refresh2);
		fresh_text = (TextView) findViewById(R.id.right_text);
		fresh_text.setText("刷新");
		
		textView_BusName = (TextView) this.findViewById(R.id.middle_title);
		
		listView_BusLine = (ListView) this.findViewById(R.id.list_contents);
		listView_BusLine.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				if (stationInfos == null)
					return;
				BusStationInfo st = stationInfos.get(arg2);
				theHelper.showBusStationInfo(theHelper.queryCityName,
						st.stationName);
			}

		});
		listView_BusLine.setDivider(null);
		endNameAndTimeView = (TextView) this.findViewById(R.id.end_station);
	/*	priceView = (TextView) findViewById(R.id.price);*/
		fresh_time = (TextView) findViewById(R.id.fresh_time);
		if("020".equals(UserAppSession.cur_CityCode)){
			fresh_layout.setVisibility(View.VISIBLE);	
			fresh_time.setText("刷新时间：");
		} else {
			fresh_layout.setVisibility(View.GONE);	
			fresh_time.setVisibility(View.GONE);
//			fresh_time.setText("票价：");
		}
		// 执行切换上下行
		switch_line_Bt = (ImageButton) this.findViewById(R.id.switch_up_down);
		switch_line_Bt.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				theHelper.toggleBusLineDirQuery();
			}
		});
	}

	BusStationMapAdapter adapter = null;

	public void refreshBusLineView(Object res) {
		if (UserAppSession.isTaskCancelMessage(res)) {
			UserAppSession.showToast(this, "查询被中止");
			return;
		}
		if (res instanceof Throwable) {
			UserAppSession.showToast(this,
					"线路查询出错-" + ((Exception) res).getMessage());
			return;
		}
		BusInfo entity = ((BusInfo) res);
		stationInfos.clear();
		stationInfos.addAll(entity.stations);
		if (adapter == null) {
			adapter = new BusStationMapAdapter(this, stationInfos);
			listView_BusLine.setAdapter(adapter);
		} else
			adapter.notifyDataSetChanged();

		textView_BusName.setText(entity.getBusName());
		textView_BusName.setFocusable(true);
		StringBuffer time = new StringBuffer();
		if("020".equals(UserAppSession.cur_CityCode)){
			if(entity.firstTime == null || "".equals(entity.firstTime)){
				time.append("");
			} else {
				if(entity.firstTime.indexOf(":")==-1){
					time.append(entity.firstTime.substring(0, 2)+":"+ entity.firstTime.substring(2));
				} else {
					time.append(entity.firstTime);	
				}	
			}
			if(entity.lastTime == null || "".equals(entity.lastTime)){
				time.append("");
			} else {
				if(entity.lastTime.indexOf(":")==-1){
					time.append(" - "+entity.lastTime.substring(0, 2)+":"+entity.lastTime.substring(2));	
				} else {
					time.append(" - "+entity.lastTime);	
				}			
			}
		} else {			
			if(entity.firstTime == null || "".equals(entity.firstTime)){
				time.append("");
			} else {
				if(entity.firstTime.indexOf(":")==-1){
					time.append(entity.firstTime.substring(0, 2)+":"+ entity.firstTime.substring(2));
				} else {
					time.append(entity.firstTime);	
				}		
			}
			if(entity.lastTime == null || "".equals(entity.lastTime)){
				time.append("");
			} else {
				if(entity.lastTime.indexOf(":")==-1){
					time.append(" - "+entity.lastTime.substring(0, 2)+":"+entity.lastTime.substring(2));	
				} else {
					time.append(" - "+entity.lastTime);	
				}			
			}
		}
		endNameAndTimeView.setText(Html.fromHtml("开往：" + entity.endPlatName + "    <font color=#0080AF>"+time.toString()+"</font>"));
		if("020".equals(UserAppSession.cur_CityCode)){
			Date fresh_date = new Date();
			String now_time = TypeUtil.formatDate(fresh_date, "yyyy-MM-dd HH:mm:ss");
			fresh_time.setText("刷新时间：" + now_time);
		} else {
//			if(entity.ticketPrice == null || "".equals(entity.ticketPrice)||"0".equals(entity.ticketPrice)){
//				fresh_time.setText("票价：未知");
//			} else {
//				fresh_time.setText(Html.fromHtml("票价：<font color=#0080AF>" + entity.ticketPrice + "</font>元"));
//			}	
		}	
	}

	public void setCurrentBusInfo(String city, String bus, String dir) {
		textView_BusName.setText(bus);
		textView_BusName.setFocusable(true);
	}

	/**
	 * menu菜单
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
//		menu.add(Menu.NONE, 1, 1, "收藏").setIcon(R.drawable.bus_fav_button);
//		menu.add(Menu.NONE, 2, 2, "复制").setIcon(R.drawable.bus_copy_button);
//		menu.add(Menu.NONE, 3, 3, "分享").setIcon(R.drawable.bus_share_button);
//		menu.add(Menu.NONE, 4, 4, "反馈").setIcon(R.drawable.bus_feedback_button);
		return true;
	}

	/**
	 * menu选择事件
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case 1:
			UserAppSession.showToast(this, "收藏成功！");
			break;
		case 2:
			break;
		case 3:
			break;
		case 4:
			break;
		}
		return super.onOptionsItemSelected(item);
	}
}
