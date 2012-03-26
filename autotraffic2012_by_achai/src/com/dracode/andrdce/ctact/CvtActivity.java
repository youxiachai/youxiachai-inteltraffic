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

public class CvtActivity extends Activity {
	private ProgressDialog mProgressDlg = null;
	ListView listviewCvt;

	List<Map<String, Object>> currentDataList = new ArrayList<Map<String, Object>>();

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		initControls();
		initData();
	}

	private void initControls() {
		listviewCvt = new ListView(this);
		listviewCvt.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT,
				LayoutParams.FILL_PARENT));
		this.setContentView(listviewCvt);
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
		
		CvtLoader loader = new CvtLoader();
		loader.execute("14646", "");
	}

	class CvtLoader extends AsyncTask<String, Integer, Object> {
		@Override
		protected Object doInBackground(String... params) {
			try {
				return UserAppSession.cursession().getCvtList(
						Integer.parseInt(params[0]), params[1]);
			} catch (Throwable ex) {
				return ex;
			}
		}

		@Override
		protected void onPostExecute(Object result) {
			if (mProgressDlg != null)
				mProgressDlg.dismiss();
			refreshCvtList(result);
		}
	}

	public void refreshCvtList(Object res) {
		if (UserAppSession.isTaskCancelMessage(res)) {
			UserAppSession.showToast(this, "操作被中止");
			return;
		}
		if (res instanceof Throwable) {
			UserAppSession.showToast(this,
					"执行出错-" + ((Exception) res).getMessage());
			return;
		}

		JSONObject data = (JSONObject) res;
		currentDataList.clear();
		try {
			if (data == null)
				return;
			JSONArray itemList = data.getJSONArray("itemList");
			if (itemList == null)
				return;
			for (int i = 0; i < itemList.length(); i++) {
				JSONObject item = itemList.getJSONObject(i);
				addItemToDs(item.getString("ID"), item.getString("商家名称"),
						item.getString("地址"));
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}

		refreshCvtListView();
	}

	public void refreshCvtListView() {
		listviewCvt
				.setAdapter(new ArrayAdapter<Map<String, Object>>(this,
						android.R.layout.simple_expandable_list_item_1,
						currentDataList));
	}

	private void addItemToDs(String id, String name, String dt) {
		Map<String, Object> m = new HashMap<String, Object>();
		m.put("id", id);
		m.put("name", name);
		m.put("date", dt);
		currentDataList.add(m);
	}

}
