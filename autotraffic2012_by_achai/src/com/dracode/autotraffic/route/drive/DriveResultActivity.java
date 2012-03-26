package com.dracode.autotraffic.route.drive;

import java.io.IOException;
import java.util.List;

import com.dracode.andrdce.ct.TypeUtil;
import com.dracode.andrdce.ct.UserAppSession;
import com.dracode.autotraffic.R;
import com.dracode.autotraffic.common.map.MapActivityHelper;
import com.mapabc.mapapi.GeoPoint;
import com.mapabc.mapapi.MapActivity;
import com.mapabc.mapapi.MapView;
import com.mapabc.mapapi.Route;
import com.mapabc.mapapi.RouteMessageHandler;
import com.mapabc.mapapi.RouteOverlay;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class DriveResultActivity extends MapActivity implements
		RouteMessageHandler {

	private TextView titleView;
	/** 返回按钮. */
	private RelativeLayout layout_Back;
	/** 起点和终点切换查询. */
	private RelativeLayout layout_Switch;

	private ProgressDialog progDialog;
	private List<Route> routeResult;
	private int mode = Route.DrivingDefault;
	public static final int ROUTE_SEARCH_RESULT = 2002;// 路径规划结果
	public static final int ROUTE_SEARCH_ERROR = 2004;// 路径规划起起始点搜索异常
	private RouteOverlay ol;
	private GeoPoint startPoint = null;
	private GeoPoint endPoint = null;
	private Bundle bundle;
	private MapActivityHelper mapHelper;
	private boolean queryInited=false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		this.setMapMode(MAP_MODE_VECTOR);// 设置地图为矢量模式

		super.onCreate(savedInstanceState);
		setContentView(R.layout.act_drive_result);

		mapHelper = new MapActivityHelper();
		mapHelper.init(this);

		bundle = super.getIntent().getExtras();

		initControls();

	}

	@Override
	protected void onResume() {
		super.onResume();
		mapHelper.title_view.setText(UserAppSession.cur_CityName);
		mapHelper.initMap();
		if(!queryInited){
			queryInited=true;
			doDriveQuery();
		}
	}

	@Override
	protected void onPause() {
		mapHelper.doPause();
		super.onPause();
	}
	@Override
	protected void onStop() {
		mapHelper.doStop();
		super.onStop();
	}

	public void initControls() {
		titleView = (TextView) findViewById(R.id.middle_title);
		// 返回按键
		layout_Back = (RelativeLayout) findViewById(R.id.left_layout);
		if (layout_Back != null) {
			layout_Back.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					finish();
				}
			});
		}
		// 切换出发地和目的地
		layout_Switch = (RelativeLayout) findViewById(R.id.right_layout);
		layout_Switch.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				exchangeStartEnd();
				// queryBusChange(bundle.getString("QueryEnd"),
				// bundle.getString("QueryStart"));
			}
		});
	}
	
	boolean bDirChanged = false;

	protected void exchangeStartEnd() {
		bDirChanged = !bDirChanged;
		doDriveQuery();
	}

	private void doDriveQuery() {

		int startLat = toE6Int(bundle.getString("QueryStartLat"));
		int startLon = toE6Int(bundle.getString("QueryStartLon"));
		int endLat = toE6Int(bundle.getString("QueryEndLat"));
		int endLon = toE6Int(bundle.getString("QueryEndLon"));
		startPoint = new GeoPoint(startLat, startLon);
		endPoint = new GeoPoint(endLat, endLon);

		if (bDirChanged) {
			titleView.setText(bundle.getString("QueryEnd") + "→"
					+ bundle.getString("QueryStart"));
			searchRouteResult(endPoint, startPoint);
		} else {
			titleView.setText(bundle.getString("QueryStart") + "→"
					+ bundle.getString("QueryEnd"));
			searchRouteResult(startPoint, endPoint);
		}
	}

	public static int toE6Int(String v) {
		Double f = TypeUtil.ObjectToDouble(v);
		int e = (int) (f * 1E6);
		return e;
	}

	public void searchRouteResult(GeoPoint startPoint, GeoPoint endPoint) {
		progDialog = ProgressDialog.show(this, null, "正在获取线路", true, true);
		final Route.FromAndTo fromAndTo = new Route.FromAndTo(startPoint,
				endPoint);
		Thread t = new Thread(new Runnable() {
			@Override
			public void run() {

				try {
					routeResult = Route.calculateRoute(
							DriveResultActivity.this, fromAndTo, mode);
					if (progDialog.isShowing()) {
						if (routeResult != null || routeResult.size() > 0) {
							routeHandler.sendMessage(Message.obtain(
									routeHandler, ROUTE_SEARCH_RESULT));
						}
					}
				} catch (IOException e) {
					e.printStackTrace();
					routeHandler.sendMessage(Message.obtain(routeHandler,
							ROUTE_SEARCH_ERROR));
				}
			}
		});
		t.start();

	}

	private Handler routeHandler = new Handler() {
		public void handleMessage(Message msg) {
			if (msg.what == ROUTE_SEARCH_RESULT) {
				progDialog.dismiss();
				Route route = routeResult.get(0);
				if (ol != null) {
					ol.removeFromMap(mapHelper.mMapView);
				}
				ol = new RouteOverlay(DriveResultActivity.this, route);
				// 获取第一条路径的Overlay
				ol.registerRouteMessage(DriveResultActivity.this); // 注册消息处理函数
				ol.addToMap(mapHelper.mMapView); // 加入到地图
				mapHelper.zoomToFitE6(startPoint.getLongitudeE6() ,startPoint.getLatitudeE6() , endPoint
						.getLongitudeE6(),endPoint.getLatitudeE6());

			} else if (msg.what == ROUTE_SEARCH_ERROR) {
				progDialog.dismiss();
				Toast.makeText(getApplicationContext(), "搜索失败，地图服务未返回结果",
						Toast.LENGTH_SHORT).show();
			}
		}
	};

	@Override
	public void onDrag(MapView arg0, RouteOverlay arg1, int arg2, GeoPoint arg3) {
	}

	@Override
	public void onDragBegin(MapView arg0, RouteOverlay arg1, int arg2,
			GeoPoint arg3) {
	}

	@Override
	public void onDragEnd(MapView arg0, RouteOverlay arg1, int arg2,
			GeoPoint arg3) {
	}

	@Override
	public boolean onRouteEvent(MapView arg0, RouteOverlay arg1, int arg2,
			int arg3) {
		return false;
	}
}
