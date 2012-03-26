package com.dracode.autotraffic.common.map;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.PopupWindow;
import android.widget.SlidingDrawer;

import com.dracode.andrdce.ct.TypeUtil;
import com.dracode.andrdce.ct.UserAppSession;
import com.dracode.autotraffic.R;
import com.mapabc.mapapi.MapActivity;
import com.mapabc.mapapi.MapView;

public class MapFastJumpHelper {

	public MapActivity mapAct;
	public MapView mMapView;
	public OnFastJumpEvent mEvt;
	
	private SlidingDrawer mdrawer;//定义一个抽屉控件
	private GridView areas;

	public MapFastJumpHelper(MapActivity act, MapView mv, OnFastJumpEvent evt) {
		mapAct = act;
		mMapView = mv;
		mEvt = evt;
		reset();
	}

	public static interface OnFastJumpEvent {
		public void onFastJump(int centerX, int centerY, int zoomLevel);
	}

	/** 自定义菜单 . */
	protected PopupWindow popupWindow;
	/** 菜单内容 . */
	//private GridView menuGrid;
	public List<Map<String, Object>> fastViewItems = null;

	public void reset() {
		initAreaItems();

		mdrawer = (SlidingDrawer)mapAct.findViewById(R.id.slidingdrawer);
		
		areas = (GridView)mapAct. findViewById(R.id.gridview);
		MapSelectedMenuAdapter adpater = new MapSelectedMenuAdapter(mapAct,fastViewItems,mdrawer,this);
		areas.setAdapter(adpater);
		adpater.notifyDataSetChanged();
	}

	/**
	 * 打开自定义的菜单.
	 */
	public final void openFastViewMenu() {
		reset();
		mdrawer.open();
	}

	protected void gotoAreaItem(int idx) {
		if (fastViewItems == null)
			return;
		Map<String, Object> m = fastViewItems.get(idx);
		int x, y, z;
		x = TypeUtil.ObjectToInt(m.get("x"));
		y = TypeUtil.ObjectToInt(m.get("y"));
		z = TypeUtil.ObjectToInt(m.get("z"));
		if (MapActivityHelper.isBigScreen(mMapView))
			z = z + 1;
		if (mEvt != null)
			mEvt.onFastJump(x, y, z);
	}

	private void initAreaItems() {
		fastViewItems = new ArrayList<Map<String, Object>>();
		if ("020".equals(UserAppSession.cur_CityCode)) {
			addFastGoItem("全　市", 113298851, 23120136, 10);
			addFastGoItem("天河区", 113347457, 23146223, 13);
			addFastGoItem("越秀区", 113279792, 23139665, 13);
			addFastGoItem("海珠区", 113326835, 23081293, 12);
			addFastGoItem("荔湾区", 113231903, 23093452, 13);
			addFastGoItem("黄浦区", 113474624, 23107345, 12);
			addFastGoItem("萝岗区", 113504493, 23153757, 12);
			addFastGoItem("白云区", 113263648, 23224763, 12);
			addFastGoItem("番禺区", 113339866, 22986661, 12);
		} else if ("0756".equals(UserAppSession.cur_CityCode)) {
			addFastGoItem("全　市", 113422912, 22256626, 11);
			addFastGoItem("香洲区", 113531410, 22254253, 13);
			addFastGoItem("金湾区", 113353904, 22129478, 13);
			addFastGoItem("斗门区", 113230308, 22289035, 12);
		} else {
			MapViewInfo mvi = new MapViewInfo();
			if (mvi.fromString(UserAppSession.cur_CityGeoInfo)) {
				addFastGoItem("全　市", mvi.centerX, mvi.centerY, mvi.zoomLevel);
			}
		}
	}

	private void addFastGoItem(String nam, int x, int y, int z) {
		Map<String, Object> m = new HashMap<String, Object>();
		m.put("itemImage", R.drawable.icon_arrow);
		m.put("itemName", nam);
		m.put("x", x);
		m.put("y", y);
		m.put("z", z);
		fastViewItems.add(m);
	}

	public void drawerlistener(Activity activity,SlidingDrawer mdrawer) {
		final ImageButton show_area_Bt = (ImageButton) activity.findViewById(R.id.show_area);
		// 这个事件是当抽屉打开时触发的事件，这里所指的“打开”是当抽屉完全到达顶部触发的事件，我们在这里改变了ImageButton按钮的图片
		mdrawer.setOnDrawerOpenListener(new SlidingDrawer.OnDrawerOpenListener() {
			@Override
			public void onDrawerOpened() {
				show_area_Bt.setImageResource(R.drawable.outlayericon2);
			}
		});
		// 这个事件当然就是和上面相对应的事件了。当抽屉完全关闭时触发的事件，我们将ImageButton的图片又变回了最初状态
		mdrawer.setOnDrawerCloseListener(new SlidingDrawer.OnDrawerCloseListener() {
			@Override
			public void onDrawerClosed() {
				show_area_Bt.setImageResource(R.drawable.outlayericon);
			}
		});
		// 这个事件是抽屉的拖动事件，当抽屉在开始拖动和结束拖动时分别触发onScrollStarted() 和onScrollEnded() 事件
		mdrawer.setOnDrawerScrollListener(new SlidingDrawer.OnDrawerScrollListener() {
			// 当手指离开抽屉头时触发此事件（松开ImageButton触发）
			@Override
			public void onScrollEnded() {}
			// 当按下抽屉头时触发此事件（按下ImageButton触发）
			@Override
			public void onScrollStarted() {
				
			}
		});
	}
}
