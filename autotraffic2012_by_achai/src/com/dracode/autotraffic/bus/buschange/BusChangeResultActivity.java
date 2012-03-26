package com.dracode.autotraffic.bus.buschange;

import com.dracode.andrdce.ct.UserAppSession;
import com.dracode.autotraffic.R;

import com.dracode.autotraffic.common.helpers.BaseActivityHelper;

import android.app.Activity;
import android.os.Bundle;
import android.text.Html;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class BusChangeResultActivity extends Activity {

	private BusChangeResultActivityHelper theHelper = new BusChangeResultActivityHelper();

	private TextView textView_StationTitle;
	private TextView textView_projectNum;
	private RelativeLayout textView_noDataLayout;
	/** 返回. */
	private RelativeLayout layout_back;
	/** 切换. */
	private RelativeLayout layout_switch;
	/** 线路列表. */
	private ListView listView_BusStation;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.act_bus_change_query_result_list);
		
		BaseActivityHelper.initAct(this);
		initControls();
		theHelper.init(this);
	}

	private void initControls() {
		layout_back = (RelativeLayout) findViewById(R.id.left_layout);
		layout_switch = (RelativeLayout) findViewById(R.id.right_layout);
		textView_StationTitle = (TextView) this.findViewById(R.id.middle_title);
		textView_projectNum = (TextView) this.findViewById(R.id.project_num);
		textView_noDataLayout = (RelativeLayout) this.findViewById(R.id.no_data_layout);
		listView_BusStation = (ListView) this.findViewById(R.id.list_contents);
		
		layout_back.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});
		layout_switch.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				theHelper.isSwitch = "true";
				theHelper.toggleQueryDirection();
			}
		});
	}
	/**
	 * 更新界面数据
	 * @param res
	 */
	public void refreshBusChangeView(Object res) {
		if (UserAppSession.isTaskCancelMessage(res)) {
			UserAppSession.showToast(this, "查询被中止");
			return;
		}
		if (res instanceof Throwable) {
			UserAppSession.showToast(this, "查询出错-" + ((Exception) res).getMessage());
			return;
		}
		BusChangeInfo entity = (BusChangeInfo) res;
		// 刷新列表
		refreshTheListView(entity);
	}
	
	/**
	 * 刷新列表
	 * @param bc
	 */
	private void refreshTheListView(BusChangeInfo bc) {
		textView_StationTitle.setText(bc.start + "→" + bc.end);
		textView_StationTitle.setFocusable(true);
		if ( bc.exchangePlans.size() > 0) {
			String num_Str = "总共 <font color=#0089BB>" + bc.exchangePlans.size() + "</font> 种换乘方案可供参考";
			textView_projectNum.setText(Html.fromHtml(num_Str));
			
			BusChangeAdapter adapter = new BusChangeAdapter(this, bc, listView_BusStation);
			listView_BusStation.setAdapter(adapter);
			adapter.notifyDataSetChanged();	
		} else {
			textView_noDataLayout.setVisibility(View.VISIBLE);
		}
	}

	public void setCurrentChangeInfo(String city, String start, String end) {
		textView_StationTitle.setText(start + "→" + end);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		return super.onKeyDown(keyCode, event);
	}

	/**
	 * menu菜单
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		return true;
	}

	/**
	 * menu选择事件
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		return super.onOptionsItemSelected(item);
	}

}
