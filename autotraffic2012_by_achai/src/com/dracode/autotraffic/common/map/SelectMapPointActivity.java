package com.dracode.autotraffic.common.map;

import java.util.Timer;
import java.util.TimerTask;

import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

import com.dracode.autotraffic.R;
import com.mapabc.mapapi.GeoPoint;
import com.mapabc.mapapi.MapActivity;
import com.mapabc.mapapi.MapView;
import com.mapabc.mapapi.Overlay;

public class SelectMapPointActivity extends MapActivity {

	public static final int SEL_MAP_POINT_REQ_CODE = 3001;

	private MapActivityHelper mapHelper;
	//private ImageButton show_area_Bt;
	public MapPointOverlay selectingPoint;
	private String selParam;

	private Timer idleTimer;

	private TimerTask idlTimerTask;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		this.setMapMode(MAP_MODE_VECTOR);// 设置地图为矢量模式
		super.onCreate(savedInstanceState);
		setContentView(R.layout.act_road_map);

		mapHelper = new MapActivityHelper();
		mapHelper.init(this);
		mapHelper.initFastJumpHelper();
		selectingPoint = new MapPointOverlay(this);
		initControls();

		Bundle ext = getIntent().getExtras();
		if (ext != null) {
			String s = ext.getString("title");
			if (s != null && s.length() > 0)
				mapHelper.title_view.setText(s);
			selParam = ext.getString("param");
			if (selParam == null)
				selParam = "";
		}
	}

	/**
	 * 初始化页面控件和添加事件
	 */
	private void initControls() {
		mapHelper.left_layout.setVisibility(View.VISIBLE);
		mapHelper.right_layout.setVisibility(View.VISIBLE);

		mapHelper.title_view.setText("选择地图上的点");
		mapHelper.more_city.setVisibility(View.GONE);

		mapHelper.right_layout.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				mapHelper.findMyLocation(true);
			}
		});
		mapHelper.right_img.setBackgroundResource(R.drawable.icon_locate);
		mapHelper.right_text.setText("定位");

	}

	@Override
	protected void onResume() {
		super.onResume();
		mapHelper.initMap();
		mapHelper.mMapView.getOverlays().add(selectingPoint);
		delayShowFastJumpMenu();
	}

	private void delayShowFastJumpMenu() {
		(new Handler()).postDelayed(new Runnable() {
			public void run() {
				mapHelper.mFastJumpHelper.openFastViewMenu();
			}
		}, 200);
	}

	@Override
	protected void onPause() {
		killIdleTimer();
		mapHelper.doPause();
		super.onPause();
	}

	@Override
	protected void onStop() {
		killIdleTimer();
		mapHelper.doStop();
		super.onStop();
	}

	protected void resetIdleTimer() {
		if (idleTimer != null || popUpView != null) {
			killIdleTimer();
			return;
		}
		idleTimer = new Timer();
		idlTimerTask = new TimerTask() {
			public void run() {
				idleTimer = null;
				handler.sendMessage(Message.obtain(handler, 10001));
			}
		};
		idleTimer.schedule(idlTimerTask, 600);
	}

	protected void killIdleTimer() {
		if (popUpView != null) {
			mapHelper.mMapView.removeView(popUpView);
			popUpView = null;
		}
		if (idleTimer != null) {
			idleTimer.cancel();
			idleTimer = null;
		}
	}

	protected Handler handler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case 10001:
				if (lastPt != null)
					showSelectMark(lastPt);
				break;
			}
		}
	};

	private GeoPoint lastPt = null;
	private LayoutInflater inflater;
	private View popUpView;

	public void showSelectMark(final GeoPoint point) {
		// Projection接口用于屏幕像素点坐标系统和地球表面经纬度点坐标系统之间的变换
		popUpView = inflater.inflate(R.layout.bus_change_query_popup, null);
		TextView textView = (TextView) popUpView.findViewById(R.id.PoiName);
		textView.setText("点击即可选择此点");

		MapView.LayoutParams lp;
		lp = new MapView.LayoutParams(MapView.LayoutParams.WRAP_CONTENT,
				MapView.LayoutParams.WRAP_CONTENT, point, 0, 0,
				MapView.LayoutParams.BOTTOM_CENTER);
		mapHelper.mMapView.addView(popUpView, lp);
		popUpView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intt = new Intent();
				intt.putExtra("x", point.getLongitudeE6() * 1.0 / 1E6);
				intt.putExtra("y", point.getLatitudeE6() * 1.0 / 1E6);
				intt.putExtra("param", selParam);
				SelectMapPointActivity.this.setResult(RESULT_OK, intt);
				finish();
			}
		});
	}

	public class MapPointOverlay extends Overlay {

		float lastMx = -1, lastMy = -1;

		@Override
		public boolean onTouchEvent(MotionEvent arg0, MapView arg1) {
			if (arg0.getAction() == MotionEvent.ACTION_MOVE) {
				if (Math.abs(arg0.getX() - lastMx) >= 50
						|| Math.abs(arg0.getY() - lastMy) >= 50)
					killIdleTimer();
			} else if (arg0.getAction() == MotionEvent.ACTION_DOWN) {
				lastMx = arg0.getX();
				lastMy = arg0.getY();
				resetIdleTimer();
			}
			return super.onTouchEvent(arg0, arg1);
		}

		public MapPointOverlay(Context context) {
			inflater = (LayoutInflater) context
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		}

		@Override
		public void draw(Canvas canvas, MapView mapView, boolean shadow) {
			super.draw(canvas, mapView, shadow);
		}

		@Override
		public boolean onTap(final GeoPoint point, final MapView view) {
			lastPt = point;
			return super.onTap(point, view);
		}

	}
}
