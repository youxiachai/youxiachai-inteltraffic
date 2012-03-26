package com.dracode.andrdce.ct;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.Thread.UncaughtExceptionHandler;
import java.util.Date;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Looper;
import android.util.Log;

public class AppExceptionHandler implements UncaughtExceptionHandler {

	public static final String TAG = AppExceptionHandler.class.getSimpleName();

	private static AppExceptionHandler curErrHandler;
	private Context mContext;

	private UncaughtExceptionHandler oldHandler;

	public static AppExceptionHandler getInstance() {
		if (curErrHandler == null) {
			curErrHandler = new AppExceptionHandler();
		}
		return curErrHandler;
	}

	public void init(Context ctx) {
		mContext = ctx;
		if (oldHandler == null) {
			oldHandler = Thread.getDefaultUncaughtExceptionHandler();
			Thread.setDefaultUncaughtExceptionHandler(this);
		}
	}

	@Override
	public void uncaughtException(Thread thread, Throwable ex) {
		// 强制TRY一下，避免错误处理过程中再次出错
		try {
			handleUncaughtException(thread, ex);
		} catch (Throwable err) {
			err.printStackTrace();
		}
	}

	public void handleUncaughtException(Thread thread, Throwable ex) {
		// 输出错误信息
		ex.printStackTrace();

		ByteArrayOutputStream deo = new ByteArrayOutputStream();
		PrintStream de = new PrintStream(deo);
		ex.printStackTrace(de);
		String curErr = new String(deo.toByteArray());

		// 跳过高德地图下载线程的空指针错误
		if (ex instanceof NullPointerException) {
			if (curErr.indexOf("com.mapabc.mapapi.") > -1) {
				Log.e(TAG, "高德地图出错！");
				return;
			}
		}

		// 保存错误信息
		SharedPreferences sp = mContext.getSharedPreferences("SysError",
				Context.MODE_WORLD_WRITEABLE | Context.MODE_WORLD_READABLE);
		String err = sp.getString("lastErrorMsg", "") + "\n\n"
				+ TypeUtil.formatDate(new Date(), "yyyy-MM-dd HH:mm:ss\n")
				+ curErr;
		sp.edit().putString("lastErrorMsg", err).commit();
		sp.edit().putBoolean("isSysError", true).commit();

		// 清理程序变量
		//UserAppSession.cursession().doAppExit();

		// 显示崩溃错误提示
		showErrorHint(ex);

		//利用系统机制强制结束
		if(oldHandler!=null){
			oldHandler.uncaughtException(thread, ex);
			return;
		}
		
		// Sleep一会后结束程序
		try {
			Thread.sleep(3000);
		} catch (InterruptedException e) {
		}

		UserAppSession.LogD("killing self 1");
		android.os.Process.killProcess(android.os.Process.myPid());
		UserAppSession.LogD("killing self 2");
		System.exit(0);
		UserAppSession.LogD("killing self 3");
	}

	private void showErrorHint(Throwable ex) {
		if (ex == null)
			return;

		final String msg = ex.getLocalizedMessage();

		// 使用Toast来显示异常信息
		new Thread() {
			@Override
			public void run() {
				Looper.prepare();
				Log.e(TAG, "程序出现异常：" + msg);
				UserAppSession.showToastLong(mContext,
						"应用程序出现异常，请重新启动尝试，如多次出现请与开发商联系！");
				Looper.loop();
			}

		}.start();
	}
}