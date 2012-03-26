package com.dracode.autotraffic.common.helpers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.dracode.andrdce.ct.TypeUtil;
import com.dracode.andrdce.ct.UserAppSession;
import com.dracode.autotraffic.R;

import android.app.Activity;
import android.graphics.Color;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.URLSpan;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class QueryHistoryHelper {
	public Activity theAct;

	// 查询页面历史记录View
	private RelativeLayout history_layout;
	// 历史记录查询内容显示View
	private TextView history_query_content;
	private OnHistoryRecallEvent recallEvt;

	public static class QueryHistoryInfo {
		protected String id;
		protected String name;
		protected String city;
		protected String memo;
		protected String queryTime;
		protected String param1;
		protected String param2;
		protected String param3;
		protected String param4;
		protected String param5;
		protected String param6;
		protected String param7;
		protected String param8;
		protected String param9;

		public String getId() {
			return id;
		}

		public String getName() {
			return name;
		}

		public String getMemo() {
			return memo;
		}

		public String getQueryTime() {
			return queryTime;
		}

		public String getCacheTableName() {
			return "query_history_def";
		}

		public String getCity() {
			return city;
		}

		public void setCity(String city) {
			this.city = city;
		}

		public static String getLoadCacheUrl(String tbName, String city, int maxCount) {
			if(maxCount<=0)
				maxCount=10;
			return "sqldb://" + tbName + "/get?city="
					+ UserAppSession.urlEncode(city)
					+ "&MaxRowCount="+Integer.toString(maxCount)+"&OrderBy=query_time desc";
		}

		@Override
		public String toString() {
			return getName() + " - " + getMemo() + "   " + getQueryTime();
		}

		public void saveToCache() {
			if (!UserAppSession.cursession().isCacheTableInited(
					getCacheTableName())) {
				String url = "sqldb://" + getCacheTableName() + "/init?id=s";
				url += "&name=s";
				url += "&city=s";
				url += "&memo=s";
				url += "&query_time=s";
				url += "&param1=s";
				url += "&param2=s";
				url += "&param3=s";
				url += "&param4=s";
				url += "&param5=s";
				url += "&param6=s";
				url += "&param7=s";
				url += "&param8=s";
				url += "&param9=s";
				UserAppSession.cursession().executeCacheUrl(url);
			}

			String url = "sqldb://" + getCacheTableName() + "/put";
			Map<String, Object> fieldMap = new HashMap<String, Object>();
			fieldMap.put("id", id);
			fieldMap.put("name", name);
			fieldMap.put("city", city);
			fieldMap.put("memo", memo);
			fieldMap.put("query_time", queryTime);
			fieldMap.put("param1", param1);
			fieldMap.put("param2", param2);
			fieldMap.put("param3", param3);
			fieldMap.put("param4", param4);
			fieldMap.put("param5", param5);
			fieldMap.put("param6", param6);
			fieldMap.put("param7", param7);
			fieldMap.put("param8", param8);
			fieldMap.put("param9", param9);
			UserAppSession.cursession().executeCacheUrl(url, fieldMap);
		}

		public void loadFromMap(Map<String, Object> m) {
			id = TypeUtil.ObjectToString(m.get("id"));
			name = TypeUtil.ObjectToString(m.get("name"));
			city = TypeUtil.ObjectToString(m.get("city"));
			memo = TypeUtil.ObjectToString(m.get("memo"));
			queryTime = TypeUtil.ObjectToString(m.get("query_time"));
			param1 = TypeUtil.ObjectToString(m.get("param1"));
			param2 = TypeUtil.ObjectToString(m.get("param2"));
			param3 = TypeUtil.ObjectToString(m.get("param3"));
			param4 = TypeUtil.ObjectToString(m.get("param4"));
			param5 = TypeUtil.ObjectToString(m.get("param5"));
			param6 = TypeUtil.ObjectToString(m.get("param6"));
			param7 = TypeUtil.ObjectToString(m.get("param7"));
			param8 = TypeUtil.ObjectToString(m.get("param8"));
			param9 = TypeUtil.ObjectToString(m.get("param9"));
		}

	}

	public static interface OnHistoryRecallEvent {
		public void onRecallHistory(int idx);
	}

	public void init(Activity act, OnHistoryRecallEvent evt) {
		theAct = act;
		history_layout = (RelativeLayout) theAct.findViewById(R.id.history_layout);
		history_query_content = (TextView) theAct.findViewById(R.id.history_content);
		recallEvt = evt;

	}

	/**
	 * 刷新历史记录
	 * 
	 * @param histQueries
	 */
	public void refreshHistoryList(List<?> histQueries) {

		if (histQueries.size() > 0) {
			history_layout.setVisibility(View.VISIBLE);
			showTextLink(history_query_content, histQueries);
		} else {
			history_layout.setVisibility(View.GONE);
		}
	}

	/**
	 * 设置文字超链接的效果
	 * 
	 * @param textView
	 * @param showtext
	 */
	private void showTextLink(TextView textView, List<?> history) {
		StringBuffer strBuf = new StringBuffer("历史查询：");
		for (int i = 0; i < history.size(); i++) {
			QueryHistoryInfo hq = (QueryHistoryInfo) history.get(i);
			if (i > 0)
				strBuf.append(",&nbsp ");
			strBuf.append("<a href=\"" + hq.getName() + "\">" + hq.getName()
					+ "</a>");
		}
		textView.setText(Html.fromHtml(strBuf.toString()));
		textView.setMovementMethod(LinkMovementMethod.getInstance());
		CharSequence text = textView.getText();
		if (text instanceof Spannable) {
			int end = text.length();
			Spannable sp = (Spannable) textView.getText();
			URLSpan[] urls = sp.getSpans(0, end, URLSpan.class);
			SpannableStringBuilder style = new SpannableStringBuilder(text);
			style.clearSpans();// should clear old spans
			for (int i = 0; i < urls.length; i++) {
				URLSpan url = urls[i];
				MyURLSpan myURLSpan = new MyURLSpan(url.getURL(), i);
				style.setSpan(myURLSpan, sp.getSpanStart(url),
						sp.getSpanEnd(url), Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
				style.setSpan(new ForegroundColorSpan(Color.rgb(58, 147, 199)),
						sp.getSpanStart(url), sp.getSpanEnd(url),
						Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
			}
			textView.setText(style);
		}
	}

	private class MyURLSpan extends ClickableSpan {

		private int index;

		MyURLSpan(String url, int idx) {
			index = idx;
		}

		@Override
		public void onClick(View widget) {
			if (recallEvt != null)
				recallEvt.onRecallHistory(index);
		}
	}
}
