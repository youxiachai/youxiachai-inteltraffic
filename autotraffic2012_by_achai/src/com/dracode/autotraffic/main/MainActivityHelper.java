package com.dracode.autotraffic.main;

import java.util.ArrayList;
import java.util.List;

import com.dracode.andrdce.ct.AutoUpdateUtil;
import com.dracode.andrdce.ct.UserAppSession;
import com.dracode.andrdce.ctact.CvActivity;
import com.dracode.autotraffic.bus.busline.BusLineQueryActivity;
import com.dracode.autotraffic.common.helpers.FeedbackHelper;
import com.dracode.autotraffic.my.MyIndexActivity;
import com.dracode.autotraffic.roadcodition.RoadCoditionMapActivity;
import com.dracode.autotraffic.route.BusRouteQueryActivity;
import com.dracode.autotraffic.serves.ServiceIndexActivity;
import com.dracode.autotraffic.nearby.NearbyQueryActivity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.KeyEvent;
import android.view.Window;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageButton;
import android.widget.LinearLayout;

public class MainActivityHelper {
	public MainActivity theAct;

	public LinearLayout container;// 装载sub Activity的容器
	public List<ImageButton> menuBtns = new ArrayList<ImageButton>();

	/** SharedPreferences对象. */
	private SharedPreferences userHomeSP;
	public static MainActivityHelper getMainActHelper(){
		Context act = UserAppSession.cursession().getMainAct();
		if(act instanceof MainActivity){
			return ((MainActivity)act).theHelper;
		}
		else
			return null;
	}
	
	public void init(MainActivity act) {
		theAct = act;
		userHomeSP = theAct.getSharedPreferences("USER_HOME", Context.MODE_WORLD_READABLE);
		checkAutoUpdate();

		String _userRole = userHomeSP.getString("user_home", "公交");
		if("020".equals(UserAppSession.cur_CityCode)){
			if("公交".equals(_userRole)){
				doClickMenuButton(0);
			} else if("附近".equals(_userRole)){
				doClickMenuButton(1);
			} else if("路况".equals(_userRole)){
				doClickMenuButton(2);
			} else if("路线".equals(_userRole)){
				doClickMenuButton(3);
		    } else if("服务".equals(_userRole)){
		    	doClickMenuButton(4);
		    } else {
		    	doClickMenuButton(0);
		    }
		} else {
			if("公交".equals(_userRole)){
				doClickMenuButton(0);
			} else if("附近".equals(_userRole)){
				doClickMenuButton(1);
			} else if("路况".equals(_userRole)){
				doClickMenuButton(2);
			} else if("服务".equals(_userRole)){
				doClickMenuButton(3);
		    } else if("我的".equals(_userRole)){
		    	doClickMenuButton(4);
		    } else {
		    	doClickMenuButton(0);
		    }
		}	
	}

	public void addMenuBtn(ImageButton btn) {
		btn.setTag((new Integer(menuBtns.size())));
		menuBtns.add(btn);
	}

	protected boolean lastBackKeyDn = false;

	public boolean doDispatchKeyEvent(KeyEvent event) {
		if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
			if (event.getAction() == KeyEvent.ACTION_DOWN) {
				lastBackKeyDn = true;
				return true;
			} else if (event.getAction() == KeyEvent.ACTION_UP) {
				if (!lastBackKeyDn)
					return true;
				lastBackKeyDn = false;
				new AlertDialog.Builder(theAct)
						.setTitle("退出")
						.setMessage("确定要退出智能交通吗？")
						.setPositiveButton("是",
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
											int which) {
										theAct.finish();
									}
								}).setNegativeButton("否", null).show();
				return true;
			}
		}
		return false;
	}

	protected void switchToMap1() {
		// openActInContent(MapTestActivity.class);
	}

	protected void switchToBusLine() {
		openActInContent(BusLineQueryActivity.class);
	}

	protected void switchToNearby() {
		openActInContent(NearbyQueryActivity.class);
	}

	// 跳转到服务首页
	protected void switchToServieIndex() {
		openActInContent(ServiceIndexActivity.class);
	}
	// 跳转到路线首页
	protected void switchToRouteIndex() {
		openActInContent(BusRouteQueryActivity.class);
	}
	protected void switchToCv1() {
		openActInContent(CvActivity.class);
	}

	protected void switchToRoad() {
		openActInContent(RoadCoditionMapActivity.class);
	}

	private void switchToMyIndexOrLogin() {
		openActInContent(MyIndexActivity.class);
	}

	private void checkAutoUpdate() {
		if (AutoUpdateUtil.currentUpdater().isCheckingNewVersion()) {
			AutoUpdateUtil.AutoUpdateEvent evt = new AutoUpdateUtil.AutoUpdateEvent() {
				public void OnError(String res) {
					/*
					 * if (UserSession.isAppExited() ||
					 * MainActivity.this.isFinishing()) return;
					 * UserSession.showToast(MainActivity.this, "check error " +
					 * res);
					 */
				}

				public void OnCheckOk() {
					AutoUpdateUtil.currentUpdater().setLastCheckTime(theAct);
					/*
					 * if (UserSession.isAppExited() ||
					 * MainActivity.this.isFinishing()) return;
					 * UserSession.showToast(MainActivity.this, "check ok");
					 */
				}

				public void OnNewVersionFound(String describ) {
					if (UserAppSession.isAppExited() || theAct.isFinishing())
						return;
					AutoUpdateUtil.currentUpdater().promptDownloadNewVersion(
							theAct);
				}
			};
			AutoUpdateUtil.currentUpdater().setUpdateCheckEvent(evt);
		}
		AutoUpdateUtil.currentUpdater().promptDownloadNewVersion(theAct);
	}

	public void doClickMenuButton(int idx) {
		for (int i = 0; i < menuBtns.size(); i++) {
			if (i == idx)
				menuBtns.get(i).setSelected(true);
			else
				menuBtns.get(i).setSelected(false);
		}
		if("020".equals(UserAppSession.cur_CityCode)){
			switch (idx) {
			case 0:
				switchToBusLine();
				break;
			case 1:
				switchToNearby();
				break;
			case 2:
				switchToRoad();
				break;
			case 3:
				switchToRouteIndex();
				break;
			case 4:
				switchToServieIndex();
				break;
			}
		} else {
			switch (idx) {
			case 0:
				switchToBusLine();
				break;
			case 1:
				switchToNearby();
				break;
			case 2:
				switchToRoad();
				break;
			case 3:
				switchToServieIndex();
				break;
			case 4:
				switchToMyIndexOrLogin();
				break;
			}
		}		
	}

	public void openActInContent(@SuppressWarnings("rawtypes") Class cls) {
		container.removeAllViews();
		Intent intent = new Intent(theAct, cls);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		Window subActivity = theAct.getLocalActivityManager().startActivity(
				"subActivity", intent);
		container.addView(subActivity.getDecorView(), LayoutParams.FILL_PARENT,
				LayoutParams.FILL_PARENT);
	}

	public void doActFinish() {
		UserAppSession.cursession().doAppExit();
		// UserSession.showToast(this, "AndrDCE 程序退出");
	}

	public void showFeedBack() {
		FeedbackHelper fbh=new FeedbackHelper();
		fbh.showFeedBackDialog(theAct, "MAIN", "main_form");
	}
}
