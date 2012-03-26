package com.dracode.autotraffic.common.helpers;

import com.dracode.andrdce.ct.AppUtil;
import com.dracode.autotraffic.R;
import com.dracode.autotraffic.bus.buschange.BusChangeQueryActivity;
import com.dracode.autotraffic.bus.busline.BusLineQueryActivity;
import com.dracode.autotraffic.bus.busstation.BusStationQueryActivity;
import com.dracode.autotraffic.common.switchcity.SwitchCityActivity;
import com.dracode.autotraffic.main.MainActivityHelper;
import com.dracode.autotraffic.main.MyApp;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class BaseActivityHelper {

	public Activity theAct;
	public MyApp myApp;

	public static void initAct(Activity act) {
		(new BaseActivityHelper()).init(act);
	}

	public static void initBusQueryAct(Activity act, int tp) {
		BaseActivityHelper bah = new BaseActivityHelper();
		bah.init(act);
		bah.initForBusQuery(tp);
	}

	public void init(Activity act) {
		theAct = act;
		myApp = (MyApp) theAct.getApplication();
		initTitleBar();
	}

	/*
	 * 头部元素
	 */

	/** 头部返回菜单. */
	public RelativeLayout left_layout;
	/** 左边菜单图片. */
	public ImageView left_img;
	/** 左边菜单文字. */
	public TextView left_text;

	/** 头部右边菜单. */
	public RelativeLayout right_layout;
	/** 右边菜单图片. */
	public ImageView right_img;
	/** 右边菜单文字. */
	public TextView right_text;
	/** 头部标题. */
	public TextView title_view;

	public ImageView more_city;
	public RelativeLayout middle_Relative;

	public void initTitleBar() {

		left_layout = (RelativeLayout) theAct.findViewById(R.id.left_layout);
		if (left_layout != null) {
			left_layout.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					theAct.finish();
				}
			});
		}

		left_img = (ImageView) theAct.findViewById(R.id.left_img);
		left_text = (TextView) theAct.findViewById(R.id.left_text);

		right_layout = (RelativeLayout) theAct.findViewById(R.id.right_layout);
		right_img = (ImageView) theAct.findViewById(R.id.right_img);
		right_text = (TextView) theAct.findViewById(R.id.right_text);

		title_view = (TextView) theAct.findViewById(R.id.middle_title);

		middle_Relative = (RelativeLayout) theAct
				.findViewById(R.id.switch_city_layout);
		more_city = (ImageView) theAct.findViewById(R.id.more_city);
	}

	/*
	 * 公交查询
	 */
	public ImageButton busLineBt;
	public ImageButton busStationBt;
	public ImageButton busChangeBt;

	public RelativeLayout switch_city;

	public void initForBusQuery(int tp) {

		busLineBt = (ImageButton) theAct.findViewById(R.id.bus_line_bt);
		if (busLineBt != null) {
			if (tp == 1)
				busLineBt.setSelected(true);
			busLineBt.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					if (MainActivityHelper.getMainActHelper() != null)
						MainActivityHelper.getMainActHelper().openActInContent(
								BusLineQueryActivity.class);
				}
			});
		}

		busStationBt = (ImageButton) theAct.findViewById(R.id.bus_station_bt);
		if (busStationBt != null) {
			if (tp == 2)
				busStationBt.setSelected(true);
			busStationBt.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					if (MainActivityHelper.getMainActHelper() != null)
						MainActivityHelper.getMainActHelper().openActInContent(
								BusStationQueryActivity.class);
				}
			});
		}

		busChangeBt = (ImageButton) theAct.findViewById(R.id.bus_change_bt);
		if (busChangeBt != null) {
			if (tp == 3)
				busChangeBt.setSelected(true);
			busChangeBt.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					if (MainActivityHelper.getMainActHelper() != null)
						MainActivityHelper.getMainActHelper().openActInContent(
								BusChangeQueryActivity.class);
				}
			});
		}
		switchCity(theAct);
	}
	
	private void switchCity(final Activity activity){
		switch_city = (RelativeLayout) activity
				.findViewById(R.id.switch_city_layout);
		if (switch_city != null) {
			switch_city.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
                    Bundle bundle = new Bundle();
					AppUtil.startActivity(activity, SwitchCityActivity.class, false, bundle);
				}
			});
		}
	}

	/*
	 * 交通政策
	 */

	public ImageButton noticeBt;
	public ImageButton queryBt;
	public ImageButton guideBt;

	public void initForTrafficPoliceQuery() {
		noticeBt = (ImageButton) theAct.findViewById(R.id.notice_bt);
		if (noticeBt != null) {
			noticeBt.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {

				}
			});
		}

		queryBt = (ImageButton) theAct.findViewById(R.id.query_bt);
		if (queryBt != null) {
			queryBt.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {

				}
			});
		}

		guideBt = (ImageButton) theAct.findViewById(R.id.guide_bt);
		if (guideBt != null) {
			guideBt.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
				}
			});
		}
	}
}
