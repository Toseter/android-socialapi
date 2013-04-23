package com.poloniumarts.social;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.RelativeLayout;

public class LoginActivity extends Activity {
	WebView webview;

	static final public String URL = "url";
	static final public String REDIRECT_URL = "redirect_url";
	private String redirect_url;

	@SuppressLint("SetJavaScriptEnabled")
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// redirect_url = Auth.redirect_url;
		// String url = Auth.getUrl(Constants.API_ID, Auth.getSettings());
		String url = getIntent().getStringExtra(URL);
		redirect_url = getIntent().getStringExtra(REDIRECT_URL);
		RelativeLayout layout = new RelativeLayout(this);
		webview = new WebView(this);
		layout.addView(webview);
		setContentView(layout);

		webview.getSettings().setJavaScriptEnabled(true);
		webview.clearCache(true);

		// Чтобы получать уведомления об окончании загрузки страницы
		webview.setWebViewClient(new VkontakteWebViewClient());

		// otherwise CookieManager will fall with
		// java.lang.IllegalStateException: CookieSyncManager::createInstance()
		// needs to be called before CookieSyncManager::getInstance()
		CookieSyncManager.createInstance(this);

		CookieManager cookieManager = CookieManager.getInstance();
		cookieManager.removeAllCookie();

		//
		webview.loadUrl(url);
	}
	
	@Override
	public void onBackPressed() {
		super.onBackPressed();
		setResult(Activity.RESULT_CANCELED);
		finish();
	}

	class VkontakteWebViewClient extends WebViewClient {
		@Override
		public void onPageStarted(WebView view, String url, Bitmap favicon) {
			super.onPageStarted(view, url, favicon);
			parseUrl(url);
		}
	}

	private void parseUrl(String url) {
		try {
			if (url == null)
				return;
			if (url.startsWith(redirect_url)) {
				Intent intent = new Intent();
				intent.putExtra("url", url);
				setResult(Activity.RESULT_OK, intent);
				finish();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}