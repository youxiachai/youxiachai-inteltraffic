package com.dracode.autotraffic.roadcodition;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.JsPromptResult;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.ProgressBar;

import com.dracode.autotraffic.R;
import com.dracode.autotraffic.common.helpers.BaseActivityHelper;
import com.dracode.autotraffic.common.vedio.PlayVedioActivity;

public class RoadHighSpeedActivity extends Activity {

	private WebView webView;
	/** 网页加载进度条. */
	private ProgressBar myProgressBar;
	/** 加载进度条最大值. */
	private static int MAX_RECORD = 100;

	private String[] urls = {
			"http://wap.wirelessgz.cn/phone/freeway/index.vm",
			"http://wap.wirelessgz.cn/phone/freeway/road_list.vm?regionId=1",
			"http://wap.wirelessgz.cn/phone/freeway/road_list.vm?regionId=2",
			"http://wap.wirelessgz.cn/phone/freeway/road_list.vm?regionId=3",
			"http://wap.wirelessgz.cn/phone/freeway/road_list.vm?regionId=4" };
	private String[] navTitles = { "广东高速路况", "珠三角", "粤东", "粤西", "粤北" };
	private Button[] navBtns;
	private int curPageIndex = 0;
	private BaseActivityHelper baseHelper;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.act_hightspeed_road);

		baseHelper = new BaseActivityHelper();
		baseHelper.init(this);
		
		initControls();
	}

	/**
	 * 初始化页面控件和添加事件
	 */
	private void initControls() {
		baseHelper.left_layout.setVisibility(View.VISIBLE);
		baseHelper.right_layout.setVisibility(View.VISIBLE);
		baseHelper.more_city.setVisibility(View.GONE);

		baseHelper.left_img.setBackgroundResource(R.drawable.header_back);
		baseHelper.left_text.setText("返回");

		baseHelper.right_img.setBackgroundResource(R.drawable.icon_list);
		baseHelper.right_text.setText("提示");

		baseHelper.right_layout.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(baseHelper.theAct,
						RoadHighSpeedPromptActivity.class);
				startActivity(intent);
			}
		});

		Button area_Bt1, area_Bt2, area_Bt3, area_Bt4, area_Bt5;
		area_Bt1 = (Button) findViewById(R.id.area01);
		area_Bt2 = (Button) findViewById(R.id.area02);
		area_Bt3 = (Button) findViewById(R.id.area03);
		area_Bt4 = (Button) findViewById(R.id.area04);
		area_Bt5 = (Button) findViewById(R.id.area05);
		navBtns = new Button[] { area_Bt3, area_Bt2, area_Bt1, area_Bt4,
				area_Bt5 };
		OnClickListener bt_listener = new OnClickListener() {
			@Override
			public void onClick(View v) {
				int idx = ((Number) v.getTag()).intValue();
				doClickButton(idx);
			}
		};
		for (int i = 0; i < navBtns.length; i++) {
			Button btn = navBtns[i];
			btn.setTag(i);
			btn.setOnClickListener(bt_listener);
		}

		webView = (WebView) findViewById(R.id.web_view);
		webView.setVisibility(View.INVISIBLE);
		// 加载
		doClickButton(curPageIndex);
	}

	/**
	 * 加载网页
	 * 
	 * @param url
	 */
	private void loadWebContent(String title, String url) {

		baseHelper.title_view.setText(title);
		myProgressBar = (ProgressBar) findViewById(R.id.progressbar);
		myProgressBar.setMax(MAX_RECORD);
		myProgressBar.setProgress(5);

		if (url == null) {
			url = "";
		}

		webView.clearHistory();
		webView.clearCache(true);
		// 加载页面
		webView.loadUrl(url);

		// 焦点是否可用
		webView.setFocusable(true);
		// 获取焦点
		webView.requestFocus();
		// // cmwap代理
		// if ("cmwap".equals(Constant.NETWORK_TYPE.trim().toLowerCase())) {
		// WebView.enablePlatformNotifications();
		// webView.setHttpAuthUsernamePassword("10.0.0.172", "", "", "");
		// }

		WebSettings webSet = webView.getSettings();
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
		webView.setOnKeyListener(new View.OnKeyListener() {
			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				if (event.getAction() == KeyEvent.ACTION_DOWN) {
					if ((keyCode == KeyEvent.KEYCODE_BACK)
							&& webView.canGoBack()) {
						webView.goBack();
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
					Intent _intent = new Intent();
					Bundle _bundle = new Bundle();

					_bundle.putString("vedioUrl", url);
					_intent.setClass(RoadHighSpeedActivity.this,
							PlayVedioActivity.class);
					_intent.putExtras(_bundle);

					startActivity(_intent);
				} else {// HTTP链接
					webView.loadUrl(url);
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
				super.onPageFinished(view, url);
				myProgressBar.setProgress(MAX_RECORD);
				myProgressBar.setVisibility(View.GONE);
				hideWebPageElements();
				webView.setVisibility(View.VISIBLE);
				webView.requestFocus();
			}

			// 页面加载中
			@Override
			public void onLoadResource(WebView view, String url) {
				super.onLoadResource(view, url);
			}

		};
		// 设置WebViewClient对象
		webView.setWebViewClient(wvc);

		// 创建WebViewChromeClient
		WebChromeClient wvcc = new WebChromeClient() {
			// 设置网页加载的进度条
			@Override
			public void onProgressChanged(WebView view, int newProgress) {
				// ShowWebView.this.getWindow().setFeatureInt(Window.FEATURE_PROGRESS,
				// newProgress * 100);
				myProgressBar.setProgress(newProgress);
				if (newProgress >= 5) {
					if (newProgress >= 70) {
						hideWebPageElements();
						webView.setVisibility(View.VISIBLE);
						webView.requestFocus();
					} else if (newProgress >= 30)
						hideWebPageElements();
					else
						webView.setVisibility(View.INVISIBLE);
				}
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
				Builder builder = new Builder(RoadHighSpeedActivity.this);
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
				Builder builder = new Builder(RoadHighSpeedActivity.this);
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
		webView.setWebChromeClient(wvcc);
	}

	protected void hideWebPageElements() {
		try {
			String js;
			if (curPageIndex == 0)
				js = "javascript:var aDivs = document.body.getElementsByTagName(\"DIV\");"
						+ "for(i=0;i<5;i++)aDivs[i].style.display=\"none\";"
						+ "aDivs[6].style.display=\"none\";"
						+ "aDivs[3].style.display=\"block\";"
						+ "var aP = document.body.getElementsByTagName(\"P\");"
						+ "aP[0].style.display=\"none\";"
						+ "var foot=document.getElementById(\"footer\");foot.style.display=\"none\";";
			else
				js = "javascript:var aDivs = document.body.getElementsByTagName(\"DIV\");"
						+ "for(i=0;i<4;i++)aDivs[i].style.display=\"none\";"
						+ "var aP = document.body.getElementsByTagName(\"P\");"
						+ "aP[0].style.display=\"none\";"
						+ "aDivs[3].style.display=\"block\";"
						+ "var foot=document.getElementById(\"footer\");foot.style.display=\"none\";";
			webView.loadUrl(js);
		} catch (Throwable ex) {
		}
	}

	@Override
	public void onResume() {

		super.onResume();
	}

	@Override
	public void onStop() {

		super.onStop();
	}

	public void doClickButton(int idx) {
		curPageIndex = idx;
		for (int i = 0; i < navBtns.length; i++){
			Button btn = navBtns[i];
			btn.setSelected(i == idx);
		}
		loadWebContent(navTitles[idx], urls[idx]);
	}

}
