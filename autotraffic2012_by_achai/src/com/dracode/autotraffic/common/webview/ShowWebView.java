package com.dracode.autotraffic.common.webview;

import com.dracode.andrdce.ct.AppUtil.ActivityUtil;
import com.dracode.autotraffic.R;
import com.dracode.autotraffic.common.vedio.PlayVedioActivity;

import android.app.AlertDialog.Builder;
import android.net.Uri;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.graphics.Bitmap;
import android.webkit.JsPromptResult;
import android.webkit.JsResult;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;

public class ShowWebView extends Activity {

	private WebView webView1;
	private ProgressBar myProgressBar;
	private static int MAX_RECORD = 100;
	/** 返回.*/
	protected RelativeLayout back_layout;
	/** .*/
	private RelativeLayout right_layout;
	private Context context;
	//** Called when the activity is first created. *//*
	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		setContentView(R.layout.show_web_view);
		context = this;
		Bundle bundle = this.getIntent().getExtras();
		String title = bundle.getString("title");
		
		TextView titleTextView = (TextView) findViewById(R.id.middle_title);
		titleTextView.setText(title); 
		back_layout = (RelativeLayout) findViewById(R.id.left_layout);
		back_layout.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				finish();
			}
		});	
		right_layout = (RelativeLayout) findViewById(R.id.right_layout);
		right_layout.setVisibility(View.GONE);
		myProgressBar = (ProgressBar) findViewById(R.id.progressbar);
		myProgressBar.setMax(MAX_RECORD);
		myProgressBar.setProgress(5);

		String url = bundle.getString("url");
		if(url == null){
			ShowWebView.this.finish();
			url = "";
		}		
		webView1 = (WebView) findViewById(R.id.web_view);
		// 加载页面
		webView1.loadUrl(url);
		// 焦点是否可用
		webView1.setFocusable(true);
		// 获取焦点
		webView1.requestFocus();

		WebSettings webSet = webView1.getSettings();
		// 支持javaScript
		webSet.setJavaScriptEnabled(true);
		// 设置可以支持缩放
		webSet.setSupportZoom(true);
		// 设置出现缩放工具
		webSet.setBuiltInZoomControls(true);
		// 设置允许访问文件数据
		webSet.setAllowFileAccess(true);
		// 设置允许Gears插件来实现网页中的Flash动画显示
		webSet.setPluginsEnabled(true);

		// 覆盖默认后退按钮的作用，替换成WebView里的查看历史页面
		webView1.setOnKeyListener(new View.OnKeyListener() {
			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				if (event.getAction() == KeyEvent.ACTION_DOWN) {
					if ((keyCode == KeyEvent.KEYCODE_BACK) && webView1.canGoBack()) {
						webView1.goBack();
						return true;
					}
				}
				return false;
			}
		});
		// 创建WebViewClient对象
		WebViewClient wvc = new WebViewClient() {
			@Override
			public boolean shouldOverrideUrlLoading(WebView view, String url) {
				// 使用自己的WebView组件来响应Url加载事件，而不是使用默认浏览器器加载页面
				if (url.toLowerCase().startsWith("rtsp://")) {// 检测到是视频链接，调用视频播放器
					ActivityUtil.addActivity((ShowWebView)context);
					Intent _intent = new Intent();
					Bundle _bundle = new Bundle();
					
					_bundle.putString("vedioUrl", url);
					_intent.setClass(ShowWebView.this, PlayVedioActivity.class);
					_intent.putExtras(_bundle);
					_intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
					
					startActivity(_intent);
				} else if (url.toLowerCase().startsWith("wtai:")) {// 拨打电话
					String telephone = null;
					if (url.toLowerCase().startsWith("wtai://wp/mc;")) {
						telephone = url.substring("wtai://wp/mc;".length());
					} else {
						telephone = url.substring("wtai:".length());
					}
					Intent intent = new Intent("android.intent.action.CALL", Uri.parse("tel:" + telephone));
					startActivity(intent);
				} else if(url.toLowerCase().startsWith("tel:")) {// 拨打电话
					String telephone = url.substring("tel:".length());
					Intent intent = new Intent("android.intent.action.CALL", Uri.parse("tel:" + telephone));
					startActivity(intent);
				} else {// HTTP链接					  
					webView1.loadUrl(url);
				}
				// 记得消耗掉这个事件，Android中返回True的意思就是到此为止吧，事件就不会冒泡传递了，我们称之为消耗掉
				return true;
			}

			// 页面开始加载
			@Override
			public void onPageStarted(WebView view, String url, Bitmap favicon) {
				myProgressBar.setVisibility(View.VISIBLE);
				myProgressBar.setProgress(5);
				super.onPageStarted(view, url, favicon);
			}

			// 页面加载完成
			@Override
			public void onPageFinished(WebView view, String url) {
				myProgressBar.setProgress(MAX_RECORD);
				myProgressBar.setVisibility(View.GONE);
				super.onPageFinished(view, url);
			}

			// 页面加载中
			@Override
			public void onLoadResource(WebView view, String url) {
				super.onLoadResource(view, url);
			}

		};
		// 设置WebViewClient对象
		webView1.setWebViewClient(wvc);

		// 创建WebViewChromeClient
		WebChromeClient wvcc = new WebChromeClient() {
			// 设置网页加载的进度条
			@Override
			public void onProgressChanged(WebView view, int newProgress) {
				// ShowWebView.this.getWindow().setFeatureInt(Window.FEATURE_PROGRESS,
				// newProgress * 100);
				myProgressBar.setProgress(newProgress);
				super.onProgressChanged(view, newProgress);
			}

			// 设置应用程序的标题
			@Override
			public void onReceivedTitle(WebView view, String title) {
				// ShowWebView.this.setTitle("可以用onReceivedTitle()方法修改网页标题");
				super.onReceivedTitle(view, title);
			}

			// 处理Alert事件
			@Override
			public boolean onJsAlert(WebView view, String url, String message,
					final JsResult result) {
				// 构建一个Builder来显示网页中的alert对话框
				Builder builder = new Builder(ShowWebView.this);
				builder.setTitle("提示对话框");
				builder.setMessage(message);
				builder.setPositiveButton(android.R.string.ok,
						new AlertDialog.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								result.confirm();

							}
						});
				builder.setCancelable(false);
				builder.create();
				builder.show();
				return true;
			}

			// 处理Confirm事件
			@Override
			public boolean onJsConfirm(WebView view, String url,
					String message, final JsResult result) {
				Builder builder = new Builder(ShowWebView.this);
				builder.setTitle("带选择的对话框");
				builder.setMessage(message);
				builder.setPositiveButton(android.R.string.ok,
						new AlertDialog.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								result.confirm();
							}

						});
				builder.setNeutralButton(android.R.string.cancel,
						new AlertDialog.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								result.cancel();
							}

						});
				builder.setCancelable(false);
				builder.create();
				builder.show();
				return true;
			}

			// 处理提示事件
			@Override
			public boolean onJsPrompt(WebView view, String url, String message,
					String defaultValue, JsPromptResult result) {
				// 看看默认的效果
				return super.onJsPrompt(view, url, message, defaultValue,
						result);
			}
		};
		// 设置setWebChromeClient对象
		webView1.setWebChromeClient(wvcc);
	}
	
	@Override
	public void onResume() {
		super.onResume();
	}
	
	@Override
	public void onStop() {
		super.onStop();
	}
}
