package com.dracode.andrdce.ct;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.StatFs;
import android.util.Log;

/*
 * 自动升级工具
 */
public class AutoUpdateUtil {

	protected Thread checkingThread = null;
	protected AutoUpdateEvent auEvent = null;
	protected String newVersionInfo = null;
	protected String oldVersion = null;
	protected String errorInfo =null;
	protected String newVersion = "";

	public static AutoUpdateUtil currentUpdater(){
		if(UserAppSession.cursession().curUpdater==null)
			UserAppSession.cursession().curUpdater=new AutoUpdateUtil();
		return UserAppSession.cursession().curUpdater;
	}
	public Thread getCheckingThread() {
		return checkingThread;
	}

	public void setCheckingThread(Thread checkingThread) {
		this.checkingThread = checkingThread;
	}

	// 检查结果事件
	public interface AutoUpdateEvent {
		void OnError(String res);

		void OnNewVersionFound(String describ);

		void OnCheckOk();
	}

	// 是否正在检测
	public boolean isCheckingNewVersion() {
		return (checkingThread != null);
	}

	// 是否已经检测到新版
	public boolean hasNewVersionFound() {
		return (newVersionInfo != null);
	}

	public static String getAutoUpdateUrl(String tp) {
		if (tp.equals("CHECK_UPDATE_URL"))
			return UserAppSession.getServerUrl() + "autoupdate/termupdate2012.jsp";
		if (tp.equals("NEW_VER_DOWNLOAD_URL"))
			return UserAppSession.getServerUrl() + "autoupdate/autotraffic2012.apk";
		return null;
	}

	// 启动检测线程
	public void checkAutoUpdate(Context ctx, String oldVer) {
		//每隔6小时检测一次，避免频繁启动时浪费带宽
		SharedPreferences app_params = ctx.getSharedPreferences("APP_PARAM",
				Context.MODE_WORLD_WRITEABLE | Context.MODE_WORLD_READABLE);
		long lastTm=app_params.getLong("last_check_update_tm", 0);
		if(lastTm>0 && System.currentTimeMillis()-lastTm<1000*60*60*2)
			return;
		
		newVersionInfo = null;
		oldVersion = oldVer;
		checkingThread = new Thread(new Runnable() {
			public void run() {
				String res =null;
				try{
					res=NetUtil.getUrlData(getAutoUpdateUrl("CHECK_UPDATE_URL") + "?ver="
						+ oldVersion+"&tk="+Long.toString((new Date()).getTime()));
				}
				catch(Throwable ex){
					res="[ERROR]"+ex.getMessage();
				}
				if(UserAppSession.isAppExited())
					return;
				Looper.prepare();
				if (res != null) {
					res = res.trim();
					String newVerFlag = "[NEW_VERSION_FOUND]";
					if (res.startsWith(newVerFlag)) {
						newVersionInfo = res.substring(newVerFlag.length()).trim();
						int index = newVersionInfo.indexOf("最新版本：");
						if (index != -1) {
							newVersion = newVersionInfo.substring(index + 5, index + 9);
							UserAppSession.newest_version = newVersion;
						}
						if (auEvent != null)
							auEvent.OnNewVersionFound(newVersionInfo);
					} else if (res.equals("OK")) {
						if (auEvent != null)
							auEvent.OnCheckOk();
					} else if (auEvent != null){
						errorInfo=res;
						auEvent.OnError(res);
					}
				}
				Looper.loop();
				checkingThread = null;
			}
		});
		checkingThread.start();
	}

	protected Context downCtx = null;

	// 提示用户下载新版
	public void promptDownloadNewVersion(Context ctx) {
		
		if (newVersionInfo == null){
			if(errorInfo!=null){
				//UserAppSession.showToast(ctx,errorInfo);
			}
			return;
		}

		String nv = newVersionInfo;
		newVersionInfo = null;

		Builder buidler = new AlertDialog.Builder(ctx);
		downCtx = ctx;
		buidler.setTitle("发现新版本").setMessage(nv).setPositiveButton("下载新版本",
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						downloadNewApk(downCtx);
					}
				}).setNegativeButton("取消", null).show();

	}
    //没有检测到新版本
	public static void promptNoNewVersion(Context ctx){
		Builder buidler = new AlertDialog.Builder(ctx);
		buidler.setTitle("版本检测");
		buidler.setMessage("已经是最新版本，无需更新！");
		buidler.setPositiveButton("确定",
			new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
				}
			});
		buidler.show();
	}
	public void downloadNewApk(Context ctx) {
		boolean sdstste = DownUtil.checkStorage(ctx, getAutoUpdateUrl("NEW_VER_DOWNLOAD_URL"));
		if (sdstste) {
			ProgressDialog versionUpdateBar = new ProgressDialog(ctx);
			versionUpdateBar.setTitle("正在下载");
			versionUpdateBar.setMessage("请稍等...");
			versionUpdateBar.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
			DownUtil.downFile(ctx, versionUpdateBar, DownUtil.APK_FILE_NAME, getAutoUpdateUrl("NEW_VER_DOWNLOAD_URL"));
		}
	}

	// 设置事件监听
	public void setUpdateCheckEvent(AutoUpdateEvent evt) {
		auEvent = evt;
	}

	public static class DownUtil {
		/** 文件名.*/
		public static String APK_FILE_NAME = "autotraffic_" + UserAppSession.cursession().curUpdater.newVersion + ".apk";
		
		private static File file;
		private static ProgressDialog pd;
	    private static int fileSize;
	    private static int downLoadFileSize;
	    private static Context cont;
	    public static Thread thread;
	    private static long apkSize;
		
		/**
		 * @Title: downFile
		 * @Description: 版本更新：下载新版本
		 * @param context
		 * @param versionUpdateBar
		 * @param apkFileName 下载文件的名称
		 * @param url 下载地址
		 */
		public static void downFile(final Context context, 
									final ProgressDialog versionUpdateBar, 
									final String apkFileName, 
									final String url) {
			cont = context;
			pd = versionUpdateBar;
			pd.show();
			thread = new Thread() {
				public void run() {
					HttpClient client = new DefaultHttpClient();
					// params[0]代表连接的url
					HttpGet get = new HttpGet(url);
					HttpResponse response;
					try {
						response = client.execute(get);
						HttpEntity entity = response.getEntity();
						fileSize =(int)entity.getContentLength();
						InputStream is = entity.getContent();
						FileOutputStream fileOutputStream = null;
						if (is != null) {
							boolean sdCardExit = Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED);
							if (sdCardExit) {
								file=new File(Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "autotraffic_download");
								if(!file.exists()){
									file.mkdir();
								}
								file = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "autotraffic_download", apkFileName);
								if (!file.exists()) {
									file.createNewFile();
								}
								fileOutputStream = new FileOutputStream(file);
								
								downLoadFileSize = 0;
								sendMsg(0);
								byte[] buf = new byte[1024];
								int ch = -1;
								int count = 0;
								while ((ch = is.read(buf)) != -1) {
									fileOutputStream.write(buf, 0, ch);
									count += ch;
									downLoadFileSize =count;
									sendMsg(1);
								}
								if (fileOutputStream != null) {
									fileOutputStream.flush();
									fileOutputStream.close();
								}
								sendMsg(2);
								down(context, pd, apkFileName);
							} else {
								pd.cancel();
							}
						} else {
							pd.cancel();
						}
					} catch (ClientProtocolException e) {
						e.printStackTrace();
						Log.d("DownUtil", e.getMessage());
					} catch (IOException e) {
						e.printStackTrace();
						Log.d("DownUtil", e.getMessage());
					} finally {
						if (thread != null) 
							 thread = null;
					}
				}
			};
			thread.start();
		}
		/**
		 * 检测SD卡是否可用或者SDCard是否足够大
		 */
		public static boolean checkStorage(Context context,String url){
			//检查SD卡状态
			boolean sdCardExit = Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED);
			if(!sdCardExit){
				UserAppSession.showToast(context, "SD卡不可用，检查是否存在SD卡。或者检查USB连接方式是否为磁盘驱动器方式。");
				return false;
			}else{
				long size = checkApkSize(url);	
				File path = Environment.getExternalStorageDirectory();
				StatFs statfs = new StatFs(path.getPath());
				long blocSize=statfs.getBlockSize(); 
				long availableSize = statfs.getAvailableBlocks();
				long surplusSize = availableSize*blocSize;
				Log.e("手机可用空间", surplusSize+"");
				if(surplusSize <= size){
					UserAppSession.showToast(context, "存贮空间不足，请卸载一些应用释放存贮空间！");
					return false;
				}else{
					return true;
				}
			}
		}
		/**
		 * 检查应用安装文件的大小
		 * @return
		 */
		private static long checkApkSize(String url){
			HttpClient client = new DefaultHttpClient();
			HttpGet get = new HttpGet(url);
			HttpResponse response;
			try {
				response = client.execute(get);
				HttpEntity entity = response.getEntity();
				apkSize =(int)entity.getContentLength();
			}catch(ClientProtocolException e){
				Log.d("DownUtil", e.getMessage());
			}catch (IOException e) {
				Log.d("DownUtil", e.getMessage());
			}
			return apkSize;
		}
		/**
		 * 更新进度条
		 */
		private static Handler handler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				switch (msg.what) {
				case 0:
					pd.setMax(fileSize);
					break;
				case 1:
					pd.setProgress(downLoadFileSize);
					break;
				case 2:
					pd.dismiss();
					UserAppSession.showToast(cont, "下载成功，文件在autotraffic_download目录下！");
					break;
				default:
					break;
				}
				super.handleMessage(msg);
			}
		};
		
		private static void sendMsg(int flag) {
			Message msg = new Message();
			msg.what = flag;
			handler.sendMessage(msg);
		}

		/**
		 * @Title: down
		 * @Description: 版本更新：线程
		 * @param context
		 * @param versionUpdateBar
		 * @param apkFileName 下载文件的名称
		 */
		private static void down(final Context context, final ProgressDialog versionUpdateBar, final String apkFileName) {
			versionUpdateBar.dismiss();
			update(context, apkFileName);
		}
		
		/**
		 * @Title: update
		 * @Description: 版本更新：安装新版本
		 * @param context
		 * @param apkFileName 下载文件的名称
		 */
		private static void update(Context context, final String apkFileName) {
			Intent intent = new Intent(Intent.ACTION_VIEW);
			intent.setDataAndType(Uri.fromFile(file),
								  "application/vnd.android.package-archive");
			context.startActivity(intent);
		}
	}

	public void setLastCheckTime(Context ctx) {
		SharedPreferences app_params = ctx.getSharedPreferences("APP_PARAM",
				Context.MODE_WORLD_WRITEABLE | Context.MODE_WORLD_READABLE);
		app_params.edit().putLong("last_check_update_tm", System.currentTimeMillis()).commit();
	}
}
