package com.dracode.autotraffic.common.helpers;

import com.dracode.andrdce.ct.UserAppSession;
import com.dracode.autotraffic.R;
import com.dracode.autotraffic.common.map.GeoAddressHelper;

import android.app.Activity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;

public class FavoriteToolbarHelper {

	public Activity theAct;

	public ImageButton btn_AddtoFav;
	public ImageButton btn_CopyText;
	public ImageButton btn_Share;
	public ImageButton btn_Feedback;

	private OnGetFavFeedbackInfoEvent fbEvt;

	public static interface OnGetFavFeedbackInfoEvent {
		public int onGetFavCount();

		public String onGetFavType();

		public String onGetFavName(int idx);

		public String onGetFavValue(int idx);

		public String onGetShareText();
	}

	public void init(Activity act, OnGetFavFeedbackInfoEvent evt) {
		theAct = act;
		btn_AddtoFav = (ImageButton) theAct.findViewById(R.id.footer_one);
		btn_CopyText = (ImageButton) theAct.findViewById(R.id.footer_two);
		btn_Share = (ImageButton) theAct.findViewById(R.id.footer_three);
		btn_Feedback = (ImageButton) theAct.findViewById(R.id.footer_four);
		fbEvt = evt;

		if (btn_AddtoFav != null)
			btn_AddtoFav.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					doAddToFav();
				}
			});

		if (btn_CopyText != null)
			btn_CopyText.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					doCopyText();
				}
			});

		if (btn_Share != null)
			btn_Share.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					doShare();
				}
			});

		if (btn_Feedback != null)
			btn_Feedback.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					doFeedBack();
				}
			});
	}

	protected void doFeedBack() {
		String type = fbEvt.onGetFavType();
		String s = "";
		int c = fbEvt.onGetFavCount();
		for (int i = 0; i < c; i++) {
			s += fbEvt.onGetFavName(i) + " ";
		}
		FeedbackHelper fbh = new FeedbackHelper();
		fbh.showFeedBackDialog(theAct, type, s.trim());
	}

	protected void doShare() {
		String type = fbEvt.onGetFavType();
		String msg = fbEvt.onGetShareText();
		ShareHelper.shareMessage(theAct, type, msg);
	}

	protected void doCopyText() {
		String type = fbEvt.onGetFavType();
		String msg = fbEvt.onGetShareText();
		ShareHelper.copyMsgToClipboard(theAct, type, msg);
	}

	protected void doAddToFav() {
		String s = "";
		String type = fbEvt.onGetFavType();
		int c = fbEvt.onGetFavCount();
		for (int i = 0; i < c; i++) {
			String nam = fbEvt.onGetFavName(i);
			String val = fbEvt.onGetFavValue(i);
			String city = UserAppSession.cur_CityCode;
			//对于非地名的记录，不允许收藏
			if (nam == null || nam.length() == 0 || GeoAddressHelper.nameIsSpecAddress(nam)) {
				continue;
			}
			FavoriteHelper.addFavoriteItem(type, city, nam, val);
			if (i > 0)
				s = s + " ";
			s = s + nam;
		}
		if (s.equals(""))
			UserAppSession.showToast("没有可收藏的内容");
		else
			UserAppSession.showToast("收藏成功: " + s);
	}
}
