package com.dracode.andrdce.ct;

import java.util.LinkedList;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

public class AppUtil {
	
	/**
	 * @Title: exitApp
	 * @Description: 退出程序
	 */
	public static void exitApp(final Activity context, boolean isConfirm) {
		
		if (isConfirm) {
			AlertDialog.Builder builder = new Builder(context);
			builder.setMessage("确定要退出程序吗？");
			builder.setTitle("提示");
			builder.setPositiveButton("确认", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					UserAppSession.isNeedToHome = true;
					ActivityUtil.finishAllActivity(context);
					Intent startMain = new Intent(Intent.ACTION_MAIN);
					startMain.addCategory(Intent.CATEGORY_HOME);
					startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					context.startActivity(startMain); 
					System.exit(0);
				}
			});
			builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
			
				}
			});
			builder.create().show();
		} else {
			UserAppSession.isNeedToHome = true;
			ActivityUtil.finishAllActivity(context);
			Intent startMain = new Intent(Intent.ACTION_MAIN);
			startMain.addCategory(Intent.CATEGORY_HOME);
			startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			context.startActivity(startMain);
			System.exit(0); 
		}
	}
	
	/**
	 * @Title: startActivity
	 * @Description: 开启另外一个activity
	 * @param ctx 当前activity上下文
	 * @param nextClass 要开启的activity
	 * @param isFinish 是否销毁activity
	 * @param bundle 传参
	 */
	public static void startActivity(Context ctx, Class<?> nextClass, boolean isFinish, Bundle bundle) {
		Intent intent = new Intent(ctx, nextClass);
		if (bundle != null) intent.putExtras(bundle);
//		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		ctx.startActivity(intent);
		if (isFinish) {
			((Activity) ctx).finish();
		} 
//		else {
//			ActivityUtil.addActivity((Activity) ctx);
//		}
	}
	
	public static class ActivityUtil {
		
		/** 保存activity实例的集合. */
		public static List<Activity> activityList = new LinkedList<Activity>();
		
		/** 添加Activity到容器中. */
		public static void addActivity(Activity act) {
			activityList.add(act);
		}
		
		/** 
		 * 遍历没有finish掉访问过的Activity进行finish.
		 * @param ctx
		 */
		public static void finishAllActivity(Context ctx) {
			Activity curAct = null;
			if (ctx != null) {
				curAct = (Activity) ctx;
			}
			for (int i = activityList.size() - 1; i > -1; i--) {
				try {
					Activity act = activityList.get(i);
					if (act == curAct || act == null) {
						continue;
					}
					act.finish();
				} catch(Throwable e) {
					throw new CtRuntimeException("销毁Activity出错：" + e.getMessage());
				}
			}
			activityList = new LinkedList<Activity>();
		}
	}
}
