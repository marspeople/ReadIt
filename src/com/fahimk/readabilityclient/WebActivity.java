package com.fahimk.readabilityclient;

import static com.fahimk.readabilityclient.JavascriptModifyFunctions.*;
import static com.fahimk.readabilityclient.HelperMethods.*;

import com.fahimk.readabilityclient.MainMenu.MessageHandler;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class WebActivity extends Activity {
	private WebView webView;
	int key=0;
	double scrollPosition = 0.0;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.getWindow().requestFeature(Window.FEATURE_PROGRESS);
		setContentView(R.layout.web);
		getWindow().setFeatureInt( Window.FEATURE_PROGRESS, Window.PROGRESS_VISIBILITY_ON);

		webView = (WebView)this.findViewById(R.id.webView);

		final Activity MyActivity = this;
		webView.setWebChromeClient(new WebChromeClient() {
			public void onProgressChanged(WebView view, int progress)   
			{
				//Make the bar disappear after URL is loaded, and changes string to Loading...
				MyActivity.setTitle("Loading...");
				MyActivity.setProgress(progress * 100); //Make the bar disappear after URL is loaded

				// Return the app name after finish loading
				if(progress == 100)
					MyActivity.setTitle(R.string.app_name);
			}
		});

		Bundle data = getIntent().getExtras();
		String content = "";
		String url = "";

		boolean isLocal = false;
		if(data != null) {
			content = data.getString("article_content");
			url = data.getString("article_url");
			isLocal = data.getBoolean("local");
			scrollPosition = data.getDouble("scroll_position");
		}
		Log.e("abc", String.format("%s,%s,%s", content, url, isLocal));
		if(!isLocal && url != null) {
			try {
				String s = "https://www.readability.com/mobile/articles/"+url;
				getHTMLThread(s);
			} catch (Exception e) {
				Log.e("exception loading url", e.getMessage());
			}
		}
		else {
			initializeWV(content);
		}
		//Log.e("content", content);

	}

	public void initializeWV(String content) {
		webView.getSettings().setCacheMode(WebSettings.LOAD_NORMAL);
		webView.getSettings().setJavaScriptEnabled(true);
		webView.setScrollBarStyle(WebView.SCROLLBARS_OUTSIDE_OVERLAY);
		webView.setWebViewClient(new CustomWebView());
		webView.loadDataWithBaseURL("", content, "text/html", "UTF-8", "");
	}



	private void getHTMLThread(final String url) throws Exception {
		ProgressDialog pDialog = HelperMethods.createProgressDialog(WebActivity.this, "Loading", "retrieving content...");
		pDialog.show();
		final Handler myHandler = new WebHandler(pDialog);
		final Message msg = new Message();
		new Thread() {
			public void run() {
				try {
					Looper.prepare();
					String html = parseHTML(url);
					msg.obj = html;
					msg.what = MSG_END;
					myHandler.sendMessage(msg);

				}
				catch(Exception e) {
					e.printStackTrace();
					msg.what = MSG_FAIL;
					myHandler.sendMessage(msg);

				}
			}

		}.start();
	}
	private void setupCustomPanel() {
		final EditPanel popup = (EditPanel) findViewById(R.id.popup_window);
		popup.setVisibility(View.GONE);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {

		if(event.getAction() == KeyEvent.ACTION_DOWN)
		{
			switch(keyCode)
			{
			case KeyEvent.KEYCODE_MENU:
				final EditPanel popup = (EditPanel) findViewById(R.id.popup_window);
				if(key==0){
					key=1;
					popup.setVisibility(View.VISIBLE);
					webView.setClickable(false);
					popup.setClickable(true);
				}
				else if(key==1){
					key=0;
					popup.setVisibility(View.GONE);
					webView.setClickable(true);
					popup.setClickable(false);
				}
				return true;
			}
		}

		return super.onKeyDown(keyCode, event);
	}

	private class CustomWebView extends WebViewClient {

		public boolean shouldOverrideUrlLoading(WebView view, String url) {
			Log.e("url", url);
			if (!url.startsWith("https://www.readability.com")) {  

				Intent i = new Intent(Intent.ACTION_VIEW);
				i.setData(Uri.parse(url));
				startActivity(i);

				return true;  
			} else {
				view.loadUrl(url);
				return true;
			}
		}


		@Override
		public void onPageFinished(WebView view, String url){

			view.loadUrl("javascript:(function() { " +  
					"var readBar = document.getElementById('read-bar'); readBar.parentNode.removeChild(readBar);"+
					"var footNote = document.getElementById('article-marketing'); footNote.parentNode.removeChild(footNote);"+
			"})()");  

			//need better algorithm for this based on number of words
			//view.scrollTo(0, (int) (scrollPosition * view.getContentHeight() + view.getHeight()));
			addButtonListeners(findViewById(R.id.mainFrame), webView);
			setupCustomPanel();
			setupDefaultTheme(webView);
		} 

	}
	
	public class WebHandler extends Handler {

		ProgressDialog pDialog;

		public WebHandler(ProgressDialog pd) {
			super();
			pDialog = pd;
		}
		@Override
		public void handleMessage(Message msg) {
			switch(msg.what) {
			case MSG_END:
				if(pDialog.isShowing())
					pDialog.dismiss();
				initializeWV((String) msg.obj);
				break;
			case MSG_FAIL:
				if(pDialog.isShowing())
					pDialog.dismiss();
				displayAlert(pDialog.getContext(), "Error", "Could not connect to readability.com, please check your internet connection status and try again.");
				break;
			}
		}

	}
}


