package com.dracode.autotraffic.common.helpers;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONObject;

import com.dracode.andrdce.ct.CtRuntimeCancelException;
import com.dracode.andrdce.ct.CtRuntimeException;
import com.dracode.andrdce.ct.NetUtil;
import com.dracode.andrdce.ct.UserAppSession;
import com.dracode.autotraffic.R;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.DialogInterface.OnKeyListener;
import android.os.AsyncTask;
import android.view.KeyEvent;
import android.widget.EditText;

public class FeedbackHelper {
	private EditText edittext_Msg;
	private Context theCtx;
	private String fbType;
	private String fbMsg;
	private String fbEnvInfo;
	private ProgressDialog mProgressDlg = null;

	public void showFeedBackDialog(Context ctx, String type, String envInfo) {
		theCtx = ctx;
		fbType = type;
		fbEnvInfo = envInfo;
		showFeedbackForm("");
	}

	protected void showFeedbackForm(String msg) {
		edittext_Msg = new EditText(theCtx);
		edittext_Msg.setLines(8);
		edittext_Msg.setText(msg);
		Builder bd = new AlertDialog.Builder(theCtx);
		bd.setTitle("请输入反馈内容").setIcon(android.R.drawable.ic_dialog_info)
				.setView(edittext_Msg)
				.setNeutralButton("提交", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						sendFeedBack();
					}

				}).setNegativeButton("取消", null);
		bd.show();
	}

	protected void sendFeedBack() {
		String msg = edittext_Msg.getText().toString();
		if (msg == null || msg.trim().length() == 0)
			return;
		fbMsg = msg;

		mProgressDlg = new ProgressDialog(theCtx);
		mProgressDlg.setProgressStyle(android.R.style.Widget_ProgressBar_Large);
		mProgressDlg.setMessage("正在提交...");
		mProgressDlg.setCancelable(false);
		OnKeyListener ls = new OnKeyListener() {
			public boolean onKey(DialogInterface dialog, int keyCode,
					KeyEvent event) {
				if (keyCode == KeyEvent.KEYCODE_BACK) {
					cancelQuery();
					return true;
				}
				return false;
			}
		};
		mProgressDlg.setOnKeyListener(ls);
		OnDismissListener dsls = new OnDismissListener() {
			public void onDismiss(DialogInterface dialog) {
				mProgressDlg = null;
			}
		};
		mProgressDlg.setOnDismissListener(dsls);
		mProgressDlg.show();
		FeedbackPostTask mQueryTask;
		mQueryTask = new FeedbackPostTask();
		mQueryTask.execute(UserAppSession.cur_CityCode,
				theCtx.getResources().getString(R.string.app_compare_version)
						+ "@" + UserAppSession.cursession().userName+"@"+fbEnvInfo, fbType,
				fbMsg);
	}

	private boolean queryCanceled = false;
	private String queryingUrl = null;

	public void cancelQuery() {
		queryCanceled = true;
		if (queryingUrl != null) {
			NetUtil.stopNetUrl(queryingUrl);
		}
	}

	class FeedbackPostTask extends AsyncTask<String, Integer, Object> {
		@Override
		protected Object doInBackground(String... params) {
			try {
				String url = getFeedbackDataUrl();
				Map<String, Object> pars = new HashMap<String, Object>();
				pars.put("fb_city", params[0]);
				pars.put("fb_user", params[1]);
				pars.put("fb_type", params[2]);
				pars.put("fb_msg", params[3]);

				// 执行URL
				queryingUrl = url;
				JSONObject rJson = UserAppSession.cursession().getUrlJsonData(
						url, pars, 0);

				if (queryCanceled)
					throw new CtRuntimeCancelException("操作中止");
				if (rJson == null)
					throw new CtRuntimeException("未提交成功数据");

				return rJson.getString("feedback_res");
			} catch (Throwable ex) {
				return ex;
			}
		}

		@Override
		protected void onPostExecute(Object res) {
			if (mProgressDlg != null)
				mProgressDlg.dismiss();

			if (UserAppSession.isTaskCancelMessage(res)) {
				UserAppSession.showToast(theCtx, "提交被中止");
				return;
			}
			if (res instanceof Throwable) {
				new AlertDialog.Builder(theCtx).setTitle("反馈")
						.setMessage("提交出错-" + ((Exception) res).getMessage())
						.setNegativeButton("确定", new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int which) {
								showFeedbackForm(fbMsg);
							}

						})
						.show();
				return;
			} else
				new AlertDialog.Builder(theCtx).setTitle("反馈")
						.setMessage((String) res).setNegativeButton("确定", null)
						.show();

		}
	}

	private String getFeedbackDataUrl() {
		return UserAppSession.getBaServerCvUrl() + "&cvId=710041";
	}

}
