package com.ibrow.de.giz.siegelklarheit;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.ShareActionProvider;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Shows the details of a Siegel.
 * 
 * @author Pete
 */
@SuppressLint("SetJavaScriptEnabled")
public class DetailsActivity extends Activity {

	private IdentifeyeAPIInterface api;

	private static final String DRAWABLE = "@drawable/";
	private static final String STRING = "@string/";

	// protected Drawable blankLogo;

	protected SiegelInfo siegel;

	protected DrawerLayout mainLayout;
	protected ScrollView scrollView;
	protected LinearLayout scrollContainer;
	protected WebView htmlView;

	private ShareActionProvider shareActionProvider;
	private boolean haveShareURL = false;

	protected NavDrawHelper navDraw;

	protected boolean linkClicked = false;
	protected String currentNavTitle = "";

	protected String willSharedUrl = "";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_details);

		// when we first start - there is no share url
		willSharedUrl = "";

		// getActionBar().setDisplayHomeAsUpEnabled(true);

		SiegelklarheitApplication app = (SiegelklarheitApplication) getApplicationContext();
		api = app.getAPI();
		api.initDiskCache(this);

		ShortSiegelInfo siegel_short_info = SiegelklarheitApplication
				.getCurrentSiegel();
		assert siegel_short_info != null;

		htmlView = (WebView) findViewById(R.id.details_webview);
		WebSettings webSettings = htmlView.getSettings();
		webSettings.setJavaScriptEnabled(true);
		htmlView.addJavascriptInterface(new WebAppInterface(this), "Android");
		htmlView.getSettings()
				.setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
		htmlView.setVerticalScrollBarEnabled(false);

		String user_agent = "Siegelklarheit (Android)";
		try {
			PackageInfo pInfo = getPackageManager().getPackageInfo(
					getPackageName(), 0);
			user_agent = "Siegelklarheit/" + pInfo.versionName
					+ " (Android; Android " + Build.VERSION.RELEASE + ")";
		} catch (Exception e) {
			Log.wtf("DETAILS", "Could not get version info: " + e.getMessage());
		}
		htmlView.getSettings().setUserAgentString(user_agent);

		mainLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		scrollView = (ScrollView) findViewById(R.id.details_scroll_view);
		scrollContainer = (LinearLayout) findViewById(R.id.details_scroll_container);

		setMainDisplay(siegel_short_info);

		navDraw = new NavDrawHelper(this,
				(DrawerLayout) findViewById(R.id.drawer_layout));
	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		// Sync the toggle state after onRestoreInstanceState has occurred.
		navDraw.syncState();
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		navDraw.onConfigurationChanged(newConfig);
	}

	private void setMainDisplay(final Siegel siegel) {
		String detailsUrl = api.getWebviewBaseURL() + "webviews/details/"
				+ siegel.getId();

		htmlView.loadUrl(detailsUrl);
		htmlView.setWebViewClient(new WebViewClient() {
			@Override
			public boolean shouldOverrideUrlLoading(WebView view, String url) {
				Log.e("WEB_PAGE_URL", url);
				if (url.startsWith(api.getWebviewBaseURL())) {
					Log.v("DETAILS PAGE URL", url);
					view.loadUrl(url);
					return false;
				}
				return true;
			}

			@Override
			public void onPageFinished(WebView view, String url) {

				Log.e("DETAILS PAGE TITLE", view.getTitle());
				scrollView.scrollTo(0, 0);
				view.scrollTo(0, 0);

				// Note: we are now setting the title using the webview page
				// title
				currentNavTitle = view.getTitle();
				setTitle(currentNavTitle);
				htmlView.loadUrl("javascript: window.Android.getShareUrl($(\"meta[property='og:url']\").attr(\"content\"))");
			}
		});

		// @TODO - we need to set the "have share" boolean here
		// this is what it was in the previous version
		/*
		 * String url = result.getShareURL(); if (!url.isEmpty()) { Intent
		 * intent = new Intent(Intent.ACTION_SEND);
		 * intent.setType("text/plain"); intent.putExtra(Intent.EXTRA_SUBJECT,
		 * result.getName()); intent.putExtra(Intent.EXTRA_TEXT, url);
		 * intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET); // race
		 * condition! if (shareActionProvider != null) { // avoid race condition
		 * // if we reach this // point before menu // created
		 * shareActionProvider.setShareIntent(intent); } haveShareURL = true; }
		 * else { invalidateOptionsMenu(); }
		 */
	}

	/* menu */

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.menu_details, menu);

		MenuItem shareItem = menu.findItem(R.id.action_share);
		shareItem.setVisible(haveShareURL);

		shareActionProvider = (ShareActionProvider) shareItem
				.getActionProvider();

		Intent intent = new Intent(Intent.ACTION_SEND);
		intent.setType("text/plain");
		if (haveShareURL) { // already fetched (e.g. memory
							// cache) before menu created
							// here
			intent.putExtra(Intent.EXTRA_SUBJECT, currentNavTitle);
			Log.e("CREATE MENU", "share url = " + willSharedUrl);
			intent.putExtra(Intent.EXTRA_TEXT, willSharedUrl);
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
		}

		shareActionProvider.setShareIntent(intent);

		return true;
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		super.onPrepareOptionsMenu(menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (navDraw.onOptionsItemSelected(item)) {
			return true;
		}
		int id = item.getItemId();
		Intent intent;

		switch (id) {
		case R.id.action_scan:
			intent = new Intent(this, ScanActivity.class);
			startActivity(intent);
			break;
		case R.id.action_search:
			intent = new Intent(this, SearchActivity.class);
			startActivity(intent);
			break;
		case R.id.action_info:
			intent = new Intent(this, InfosActivity.class);
			startActivity(intent);
			break;
		case R.id.action_imprint:
			intent = new Intent(this, ImprintActivity.class);
			startActivity(intent);
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	/**
	 * Handles the back button
	 **/
	@Override
	public void onBackPressed() {
		Log.v("DETAILS", "back pressed");
		if (htmlView.canGoBack()) {
			htmlView.goBack();
		} else {
			super.onBackPressed();
		}
	}

	/**
	 * Starts the search activity.
	 * 
	 * Called by the button click, visible when we have no more infos for a
	 * siegel
	 * 
	 * @see com.ibrow.de.giz.siegelklarheit.DetailsActivity.ButtonListener
	 * @see com.ibrow.de.giz.siegelklarheit.SearchActivity
	 */
	protected final void showList() {

		Intent intent = new Intent(this, SearchActivity.class);
		startActivity(intent);
		finish();
	}

	/* internal classes */

	private final class ButtonListener implements View.OnClickListener {

		/**
		 * Starts the tour by calling showList()
		 * 
		 * @see #showList()
		 * @param v
		 */
		public void onClick(View v) {
			showList();
		}

	}

	private class WebAppInterface {
		private final Context mContext;

		/** Instantiate the interface and set the context */
		WebAppInterface(Context c) {
			mContext = c;
		}

		/** Show a toast from the web page */
		@JavascriptInterface
		public void showToast(String toast) {

			Toast.makeText(mContext, toast, Toast.LENGTH_SHORT).show();
			setTitle(toast);

		}

		@JavascriptInterface
		public void onTapTabItem(String title) {
			setTitle(title);
		}

		@JavascriptInterface
		public void onTapScoreButton(String title) {
			// currentNavTitle = title;

			/*
			 * This isn't working The idea behind this is to open the webview as
			 * a new activity. but I am getting a NullPointerException
			 */
			Log.v("DETAILS", "ON TAP SCORE VERSION");
			/*
			 * Intent intent = new Intent (mContext, WebViewActivity.class);
			 * intent.putExtra("url",
			 * "http://api.siegelklarheit.de/webviews/details/10/score/System");
			 * startActivity(intent);
			 */
		}

		@JavascriptInterface
		public void onTapCompareButton(String title) {
			// String originTitle = SiegelklarheitApplication.getCurrentSiegel()
			// .getName();
			// currentNavTitle = title + " " + originTitle;
		}

		@JavascriptInterface
		public void onTapExternalLink(String link) {
			Intent intent = new Intent(Intent.ACTION_VIEW);
			intent.setData(Uri.parse(link));
			startActivity(intent);
		}

		@JavascriptInterface
		public void onTapItemInCompareList(String title) {

			// String originTitle = SiegelklarheitApplication.getCurrentSiegel()
			// .getName();
			// currentNavTitle = title;

		}

		/**
		 * Share URL functions This is called within the webview
		 * onPageFinished(), passing the share URL of the page - if there is
		 * one.
		 **/
		@JavascriptInterface
		public void getShareUrl(String url) {
			if (!url.equals("undefined")) {
				willSharedUrl = url;
				Intent intent = new Intent(Intent.ACTION_SEND);
				intent.setType("text/plain");
				intent.putExtra(Intent.EXTRA_SUBJECT, currentNavTitle);
				intent.putExtra(Intent.EXTRA_TEXT, willSharedUrl);
				intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
				// race condition!
				if (shareActionProvider != null) { // avoid race condition
													// if we reach this
													// point before menu
													// created
					shareActionProvider.setShareIntent(intent);
				}
				haveShareURL = true;
			} else {
				willSharedUrl = "";
				haveShareURL = false;

			}
			
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					DetailsActivity.this.invalidateOptionsMenu();

				}
			});
			
			Log.e("ROB", "share url = " + willSharedUrl);
			Log.e("Vladimir", " have share url = " + haveShareURL);
			Log.e("Vladimir", " current nav title = " + currentNavTitle);
		}
	}
}
