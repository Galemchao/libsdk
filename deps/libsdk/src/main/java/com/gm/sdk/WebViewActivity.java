package com.gm.sdk;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ClipData;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import com.libsdk.sdk.R;

public class WebViewActivity extends Activity implements View.OnClickListener {
	private final static int FILE_CHOOSER_RESULT_CODE = 1024;
	private ValueCallback<Uri> uploadMessage;
	// 5.0+ 回调
	private ValueCallback<Uri[]> uploadMessageAboveL;
	private static final int _requestCode = 1010101;
	// private static boolean isFristLoad = true;
	private WebView webView;
	private WebSettings mSettings;
	// private Intent intent;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.webview_activity);
		findViewById(R.id.btn_back).setOnClickListener(this);
		findViewById(R.id.btn_forward).setOnClickListener(this);
		findViewById(R.id.btn_refresh).setOnClickListener(this);
		findViewById(R.id.btn_close).setOnClickListener(this);

		webView = (WebView) findViewById(R.id.webview);
		mSettings = webView.getSettings();
		// 允许js
		mSettings.setJavaScriptEnabled(true);

		Bundle bundle = getIntent().getExtras();
		String targetUrl = bundle.getString("targetUrl");
		// targetUrl = Utils.toURLDecoded(targetUrl);

		// if(WebViewActivity.isFristLoad){
		loadUrl(targetUrl);
		// WebViewActivity.isFristLoad = false;
		// }
	}

	/**
	 *  拉起默认的浏览器 此代码 由首次接入sdk 研发编写  内容看上去冗余 目前逻辑没有bug
	 * @param targetUrl
	 */
	private void loadUrl(String targetUrl) {
		// 处理微信跳转支付  如果是微信支付  目的只是通过网页机制来拉起微信app去支付
		if (targetUrl.startsWith("weixin://")) {
			try {
				// Log.e("xiaxb", "微信跳转");
				startActivityForResult(new Intent(Intent.ACTION_VIEW, Uri.parse(targetUrl)), _requestCode);
				setResult(RESULT_OK); //
				finish();//打开之后就会进行web销毁
			} catch (Exception e) {
				Toast.makeText(WebViewActivity.this, "未检测到微信客户端，请安装后重试！", Toast.LENGTH_LONG).show();
			}
			return;
		}

		if (targetUrl.indexOf("https://pay.ipaynow.cn") >= 0) {
			// Log.e("xiaxb", "银联支付");
			setResult(RESULT_OK);
		}

		webView.loadUrl(targetUrl);
		webView.setWebViewClient(new WebViewClient() {
			@Override
			public boolean shouldOverrideUrlLoading(WebView view, String url) {
				// 处理支付宝跳转支付
				if (url.startsWith("alipays://") || url.startsWith("alipay://") || url.startsWith("weixin://")) {
					try {
						startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
						setResult(RESULT_OK);
						finish();
					} catch (Exception e) {
						Toast.makeText(WebViewActivity.this, "未检测到支付宝客户端，转入网页支付！", Toast.LENGTH_LONG).show();
					}
					return true;
				}

				// 非微信和支付宝支付跳转，正常加载url
				if (!(url.startsWith("http") || url.startsWith("https"))) {
					setResult(RESULT_CANCELED);
					return true;
				}
				webView.loadUrl(url);
				return false;
			}
		});
		webView.setWebChromeClient(new WebChrClient());

	}

	/**
	 * 打开相册选择图片
	 */
	private void openImageChooserActivity() {
		Intent i = new Intent(Intent.ACTION_GET_CONTENT);
		i.addCategory(Intent.CATEGORY_OPENABLE);
		i.setType("image/*");
		startActivityForResult(Intent.createChooser(i, "选择图片..."), FILE_CHOOSER_RESULT_CODE);
	}

	@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
	private void onActivityResultAboveL(int requestCode, int resultCode, Intent intent) {
		if (requestCode != FILE_CHOOSER_RESULT_CODE || uploadMessageAboveL == null)
			return;
		Uri[] results = null;
		if (resultCode == Activity.RESULT_OK) {
			if (intent != null) {
				String dataString = intent.getDataString();
				ClipData clipData = intent.getClipData();
				if (clipData != null) {
					results = new Uri[clipData.getItemCount()];
					for (int i = 0; i < clipData.getItemCount(); i++) {
						ClipData.Item item = clipData.getItemAt(i);
						results[i] = item.getUri();
					}
				}
				if (dataString != null)
					results = new Uri[] { Uri.parse(dataString) };
			}
		}
		uploadMessageAboveL.onReceiveValue(results);
		uploadMessageAboveL = null;
	}

	/**
	 * 不同的android操作系统版本 接口不一样 下面实现了 n个阶段版本的openFileChooser接口
	 * 参考链接https://www.jianshu.com/p/261dfb554bf0
	 */
	private class WebChrClient extends WebChromeClient {
		// For Android < 3.0
		public void openFileChooser(ValueCallback<Uri> valueCallback) {
			uploadMessage = valueCallback;
			openImageChooserActivity();
		}

		// For Android >= 3.0
		public void openFileChooser(ValueCallback valueCallback, String acceptType) {
			uploadMessage = valueCallback;
			openImageChooserActivity();
		}

		// For Android >= 4.1
		public void openFileChooser(ValueCallback<Uri> valueCallback, String acceptType, String capture) {
			uploadMessage = valueCallback;
			openImageChooserActivity();
		}

		// For Android >= 5.0
		@Override
		public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback,
				WebChromeClient.FileChooserParams fileChooserParams) {
			uploadMessageAboveL = filePathCallback;
			openImageChooserActivity();
			return true;
		}
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		// 横竖屏配置 可不做处理
		// if (this.getResources().getConfiguration().orientation ==
		// Configuration.ORIENTATION_LANDSCAPE) {
		//
		// } else if (this.getResources().getConfiguration().orientation ==
		// Configuration.ORIENTATION_PORTRAIT) {
		//
		// }
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == _requestCode) {
			// Log.e("xiaxb", "onActivityResult:支付回调");
			finish();
		} else if (requestCode == FILE_CHOOSER_RESULT_CODE) {
			if (null == uploadMessage && null == uploadMessageAboveL)
				return;
			Uri result = data == null || resultCode != RESULT_OK ? null : data.getData();
			if (uploadMessageAboveL != null) {
				onActivityResultAboveL(requestCode, resultCode, data);
			} else if (uploadMessage != null) {
				uploadMessage.onReceiveValue(result);
				uploadMessage = null;
			}
		}
		// if (webView != null) {
		// webView.onResume();
		// }
	}

	@Override
	protected void onResume() {
		// Log.e("xiaxb", "onResume");
		super.onResume();
		if (webView != null) {
			webView.onResume();
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		if (webView != null) {
			webView.onPause();
		}
	}

	@Override
	public void onBackPressed() {
		if (webView != null && webView.canGoBack()) {
			webView.goBack();
		} else {
			finish();
		}
	}

	// 销毁Webview
	@Override
	protected void onDestroy() {
		if (null != webView) {
			webView.clearHistory();
			webView.destroy();
			webView = null;
		}
		super.onDestroy();
	}

	@Override
	public void onClick(View view) {
		int id = view.getId();
		if (id == R.id.btn_back ) {
			if (webView != null && webView.canGoBack()) {
				webView.goBack();
			}
		} else if (id == R.id.btn_forward ) {
			if (webView != null && webView.canGoForward()) {
				webView.goForward();
			}
		} else if (id == R.id.btn_refresh ) {
			if (webView != null) {
				webView.reload();
			}
		}else if (id == R.id.btn_close ) {
			finish();
		}
	}
}
