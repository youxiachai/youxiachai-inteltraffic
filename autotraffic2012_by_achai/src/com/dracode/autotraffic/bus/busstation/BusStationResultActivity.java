package com.dracode.autotraffic.bus.busstation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.dracode.andrdce.ct.UserAppSession;
import com.dracode.autotraffic.R;
import com.dracode.autotraffic.bus.busline.BusInfo;
import com.dracode.autotraffic.common.helpers.BaseActivityHelper;


import android.app.Activity;
import android.os.Bundle;
import android.text.Html;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

public class BusStationResultActivity extends Activity {

	private BusStationResultActivityHelper theHelper = new BusStationResultActivityHelper();

	private TextView textView_StationTitle;
	private TextView textView_StationInfo;
	/** 返回. */
	private RelativeLayout back_layout;
	/** 切换.*/
	private RelativeLayout switch_layout;
	/** 线路列表. */
	private ListView listView_BusStation;
	
	List<Map<String, Object>> busInfos=null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.act_bus_station_query_result_list);

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
		switch_layout = (RelativeLayout) findViewById(R.id.right_layout);
		switch_layout.setVisibility(View.GONE);
		textView_StationTitle = (TextView) this.findViewById(R.id.middle_title);
		textView_StationInfo = (TextView) this.findViewById(R.id.text01);
		listView_BusStation = (ListView) this.findViewById(R.id.list_contents);
		listView_BusStation.setOnItemClickListener(new OnItemClickListener(){

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				Map<String, Object> bi = busInfos.get(arg2);
				theHelper.showBusLineInfo(theHelper.queryCityName, (String)bi.get("lineName"));
			}
			
		});
	}

	public void refreshBusStationView(Object res) {
		if (UserAppSession.isTaskCancelMessage(res)) {
			UserAppSession.showToast(this, "查询被中止");
			return;
		}
		if (res instanceof Throwable) {
			UserAppSession.showToast(this,
					"站点查询出错-" + ((Exception) res).getMessage());
			return;
		}
		BusStationInfo entity = ((BusStationInfo) res);
		if (entity == null)
			return;
		BusStationInfo st = (BusStationInfo) res;
		refreshTheListView(st);
	}

	private void refreshTheListView(BusStationInfo st) {
		textView_StationTitle.setText(st.stationName);
		textView_StationTitle.setFocusable(true);
		textView_StationInfo.setText(Html.fromHtml("经过<font color=#0089BB>"+st.stationName+"</font>站共有<font color=#0C82AC>"+st.relateBusLines.size()+"</font>条线路可供参考"));
		busInfos = new ArrayList<Map<String, Object>>();
		StringBuffer time = new StringBuffer();
		for (BusInfo bi : st.relateBusLines) {
			Map<String, Object> m = new HashMap<String, Object>();
			m.put("lineName", bi.lineName);
			time.delete(0, time.length());
			if(bi.firstTime==null||"".equals(bi.firstTime)){
				time.append("");
			} else {
				time.append(bi.firstTime.substring(0, 2)+":"+ bi.firstTime.substring(2));	
			}
			if(bi.lastTime==null||"".equals(bi.lastTime)){
				time.append("");
			}else {
				time.append("--"+bi.lastTime.substring(0, 2)+":"+bi.lastTime.substring(2));	
			}
			m.put("startStation", bi.strPlatName+" "+time.toString());
			m.put("endStation", bi.endPlatName);
			
			if(bi.ticketPrice==null||"".equals(bi.ticketPrice)||"0".equals(bi.ticketPrice)){
				m.put("describ", "价格：未知");	
			} else {
				m.put("describ", "价格："+bi.ticketPrice+"元");	
			}			
			busInfos.add(m);
		}

		SimpleAdapter adapter = new SimpleAdapter(this, busInfos,
				R.layout.bus_station_query_result_list_item, new String[] { "lineName",
						"startStation", "endStation", "describ" }, new int[] {
						R.id.bus_name, R.id.start_station, R.id.end_station,
						R.id.brief });
		listView_BusStation.setAdapter(adapter);
	}

	public void setCurrentStationInfo(String city, String station) {
		textView_StationTitle.setText(station);
		textView_StationTitle.setFocusable(true);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		switch (keyCode) {
		case KeyEvent.KEYCODE_MENU:
			openOptionsMenu();
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

}
