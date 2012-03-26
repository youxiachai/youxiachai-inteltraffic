package com.dracode.autotraffic.common.helpers;

import com.dracode.andrdce.ct.UserAppSession;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.ClipboardManager;

public class ShareHelper {

	public static void shareMessage(Context ctx, String type, String msg) {
		if(msg==null || msg.trim().length()==0){
			UserAppSession.showToast("没有可分享的内容");
			return;
		}
		Intent intent = new Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:"));
		intent.putExtra("sms_body", msg);
		ctx.startActivity(intent);
	}
	
	public static void copyMsgToClipboard(Context ctx, String type, String msg){
		if(msg==null || msg.length()==0){
			UserAppSession.showToast("没有可复制的内容");
			return;
		}
		ClipboardManager clipboard = (ClipboardManager) ctx.getSystemService(Context.CLIPBOARD_SERVICE);
		clipboard.setText(msg);
		UserAppSession.showToast("已经复制内容到剪贴板");
	}
}
