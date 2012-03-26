package com.dracode.andrdce.ctact;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.dracode.andrdce.ct.UserAppSession;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.DialogInterface.OnKeyListener;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.ViewGroup.LayoutParams;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class CvActivity extends Activity {
	private ProgressDialog mProgressDlg = null;
	ListView listviewCv;

	List<Map<String, Object>> currentDataList = new ArrayList<Map<String, Object>>();

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		initControls();
		initData();
	}

	private void initControls() {
		listviewCv = new ListView(this);
		listviewCv.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT,
				LayoutParams.FILL_PARENT));
		this.setContentView(listviewCv);
	}

	private void initData() {

		mProgressDlg = new ProgressDialog(this);
		mProgressDlg.setTitle("提示");
		mProgressDlg.setProgressStyle(android.R.style.Widget_ProgressBar_Large);
		mProgressDlg.setMessage("正在执行...");
		mProgressDlg.setCancelable(false);
		OnKeyListener ls = new OnKeyListener() {
			public boolean onKey(DialogInterface dialog, int keyCode,
					KeyEvent event) {
				if (keyCode == KeyEvent.KEYCODE_BACK) {
					//realTimeBusIntf.cancelQuery();
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
		
		
		CvLoader loader = new CvLoader();
		loader.execute("15097", "&itemId=845");
	}

	class CvLoader extends AsyncTask<String, Integer, Object> {
		@Override
		protected Object doInBackground(String... params) {
			try {
				return UserAppSession.cursession().getCvData(
						Integer.parseInt(params[0]), params[1]);
			} catch (Throwable ex) {
				return ex;
			}
		}

		@Override
		protected void onPostExecute(Object result) {
			if (mProgressDlg != null)
				mProgressDlg.dismiss();

			refreshCvList(result);
		}
	}

	public void refreshCvList(Object res) {
		if (UserAppSession.isTaskCancelMessage(res)) {
			UserAppSession.showToast(this, "操作被中止");
			return;
		}
		if (res instanceof Throwable) {
			UserAppSession.showToast(this,
					"执行出错-" + ((Exception) res).getMessage());
			return;
		}
		
		JSONObject data=(JSONObject)res;
		currentDataList.clear();
		try {
			if (data == null)
				return;
			JSONArray itemList = data.getJSONArray("ROWS");
			if (itemList == null)
				return;
			for (int i = 0; i < itemList.length(); i++) {
				JSONObject item = itemList.getJSONObject(i);
				JSONObject left = null;
				if (item.has("left"))
					left = item.getJSONObject("left");
				JSONObject right = null;
				if (item.has("right"))
					right = item.getJSONObject("right");
				String cap = "", val = "";
				if (left != null)
					cap = left.getString("VALUE");
				if (right != null)
					val = right.getString("VALUE");
				addItemToDs(cap, val);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}

		refreshCvListView();
	}

	public void refreshCvListView() {
		listviewCv
				.setAdapter(new ArrayAdapter<Map<String, Object>>(this,
						android.R.layout.simple_expandable_list_item_1,
						currentDataList));
	}

	private void addItemToDs(String cap, String val) {
		Map<String, Object> m = new HashMap<String, Object>();
		m.put("caption", cap);
		m.put("value", val);
		currentDataList.add(m);
	}

}
