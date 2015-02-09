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
@SuppressLint("SetJavaScriptEnabled") public class DetailsActivity extends Activity {

	private IdentifeyeAPIInterface api;

	private static final String DRAWABLE = "@drawable/";
	private static final String STRING = "@string/";

    //protected Drawable blankLogo;

	protected SiegelInfo siegel;

	protected DrawerLayout mainLayout;
	protected ScrollView scrollView;
	protected LinearLayout scrollContainer;
    //protected LinearLayout logoViewContainer;
    //protected LinearLayout ratingView;
    //protected ImageView logoImageView;
	protected WebView htmlView;

	private ShareActionProvider shareActionProvider;
	private boolean haveShareURL = false;

	protected NavDrawHelper navDraw;

	protected boolean linkClicked = false;
	protected String currentNavTitle = "";
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_details);

		// getActionBar().setDisplayHomeAsUpEnabled(true);

		SiegelklarheitApplication app = (SiegelklarheitApplication) getApplicationContext();
		api = app.getAPI();
		api.initDiskCache(this);

		// LogoHelper.initDiskCachePath(this);
		// blankLogo = getResources().getDrawable(R.drawable.blank_label_logo);

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
		//logoViewContainer = (LinearLayout) findViewById(R.id.logo_view_container);
		//ratingView = (LinearLayout) findViewById(R.id.rating_view);
		//logoImageView = (ImageView) findViewById(R.id.logo_view);

		setMainDisplay(siegel_short_info);

		// htmlView.loadUrl("file:///android_asset/loading.html");

                new LoadFullInfoTask(api).execute(new Integer(siegel_short_info
                                                              .getId()));

                /*
		if (siegel_short_info.getRating() != SiegelRating.UNKNOWN
				&& siegel_short_info.getRating() != SiegelRating.NONE) {
			// only load full infos if there's more infos to fetch!
			new LoadFullInfoTask(api).execute(new Integer(siegel_short_info
					.getId()));
		}
                */

		((Button) findViewById(R.id.no_infos_show_list_btn))
				.setOnClickListener(new ButtonListener());

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
		
		setTitle(siegel.getName());
		currentNavTitle = siegel.getName();

		SiegelRating rating = siegel.getRating();
                /*
		View rating_holder = (View) findViewById(R.id.rating_view);

		rating_holder.setBackgroundColor(rating.getColor());
                */
                /*
		if (rating == SiegelRating.UNKNOWN || rating == SiegelRating.NONE) {
			htmlView.setVisibility(View.GONE);
			((LinearLayout) findViewById(R.id.no_infos_holder))
					.setVisibility(View.VISIBLE);
			if (rating == SiegelRating.UNKNOWN) {
				((ImageView) findViewById(R.id.rating_symbol_image))
						.setVisibility(View.GONE);
			}
		}
                */

		// ImageView rating_image_view = (ImageView) findViewById(R.id.rating_symbol_image);
		// rating_image_view.setImageDrawable(getResources().getDrawable(
		// 		getResources().getIdentifier(
		// 				DRAWABLE + rating.getImageIdentifier(), null,
		// 				getPackageName())));

		// TextView rating_text_view = (TextView) findViewById(R.id.rating_symbol_text);
		// rating_text_view.setText(getResources().getText(
		// 		getResources().getIdentifier(
		// 				STRING + rating.getDescriptionIdentifier(), null,
		// 				getPackageName())));
/*
		Bitmap image = LogoHelper.getFromMemoryCache(siegel);
		if (image != null) {
                    //logoImageView.setImageBitmap(image);
		} else {
			LoadSiegelLogoTask logo_task = new LoadSiegelLogoTask();
			logo_task.execute(siegel);
		}
*/
	}

	/* menu */

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.menu_details, menu);

		MenuItem shareItem = menu.findItem(R.id.action_share);
		shareActionProvider = (ShareActionProvider) shareItem
				.getActionProvider();
		Intent intent = new Intent(Intent.ACTION_SEND);
		intent.setType("text/plain");
		if (haveShareURL && siegel != null) { // already fetched (e.g. memory
												// cache) before menu created
												// here
			intent.putExtra(Intent.EXTRA_SUBJECT, siegel.getName());
			intent.putExtra(Intent.EXTRA_TEXT, siegel.getShareURL());
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
		}

		shareActionProvider.setShareIntent(intent);

		return true;
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		MenuItem share = menu.findItem(R.id.action_share);
		share.setEnabled(haveShareURL);
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
	 * We trap the back key here for the web view.
	 * 
	 * @param keyCode
	 * @param event
	 * @return
	 */
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if ((keyCode == KeyEvent.KEYCODE_BACK) && (htmlView != null)
				&& htmlView.canGoBack() && linkClicked) {
		Log.v("Webview", "On Key Down pressed");
                
			htmlView.loadDataWithBaseURL(api.getWebviewBaseURL(),
					siegel.getDetails(), "text/html", "UTF-8", null);
			linkClicked = false;
			mainLayout.removeView(htmlView);
			//mainLayout.addView(scrollView, 0, new DrawerLayout.LayoutParams(
			//		LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
			scrollContainer.addView(htmlView, 0, new LinearLayout.LayoutParams(
					LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
			currentNavTitle = SiegelklarheitApplication
					.getCurrentSiegel().getName();
			new Handler().postDelayed(new Runnable() {
				@Override
				public void run() {
					scrollView.scrollTo(0, 0);
					htmlView.scrollTo(0, 0);
					setTitle(currentNavTitle);
				}
			}, 100);
			return true;
		}
		return super.onKeyDown(keyCode, event);
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

		Log.e("Button", "Button");
		Intent intent = new Intent(this, SearchActivity.class);
		startActivity(intent);
		finish();
	}

	/* internal classes */

	private class LoadFullInfoTask extends AsyncTask<Integer, Void, SiegelInfo> {

		IdentifeyeAPIInterface api;
		Exception error;

		LoadFullInfoTask(final IdentifeyeAPIInterface api) {
			this.api = api;
		}

		protected SiegelInfo doInBackground(Integer... siegel_id) {
			SiegelInfo siegel = null;
			try {
				siegel = api.getInfo(siegel_id[0].intValue());
			} catch (Exception e) {
				Log.e("LoadFullInfoTask-API", e.getMessage());
				error = e;
			}
			return siegel;
		}

		protected void onPostExecute(SiegelInfo result) {
			if (isCancelled()) {
				Log.v("LoadFullInfoTask", "Task cancelled");
				return;
			}
			if (result != null) {
				Log.v("LoadFullInfoTask", "got result for id " + result.getId());
				siegel = result;
				Log.e("WEB_PAGE_URL", api.getWebviewBaseURL());
				htmlView.loadDataWithBaseURL(api.getWebviewBaseURL(),
						result.getDetails(), "text/html", "UTF-8", null);
				htmlView.setWebViewClient(new WebViewClient() {
					@Override
					public boolean shouldOverrideUrlLoading(WebView view,
							String url) {
						Log.e("WEB_PAGE_URL", url);
						if (url.startsWith(api.getWebviewBaseURL())) { // internal
																		// url,
																		// eg.
																		// score
							view.loadUrl(url);
							if (linkClicked == false) {
								linkClicked = true;
								scrollContainer.removeView(htmlView);
								mainLayout.removeView(scrollView);
								mainLayout.addView(htmlView, 0,
										new DrawerLayout.LayoutParams(
												LayoutParams.MATCH_PARENT,
												LayoutParams.MATCH_PARENT));
								new Handler().postDelayed(new Runnable() {
									@Override
									public void run() {
										scrollView.scrollTo(0, 0);
										htmlView.scrollTo(0, 0);
									}
								}, 100);
							}
							return false;
						}
						return true; // external url, open in browser
					}

					@Override
					public void onPageFinished(WebView view, String url) {
						scrollView.scrollTo(0, 0);
						view.scrollTo(0, 0);
						setTitle(currentNavTitle);
					}
				});
				// Log.d("LoadFullInfoTask", "Html:"+result.getDetails());
				String url = result.getShareURL();
				if (!url.isEmpty()) {
					Intent intent = new Intent(Intent.ACTION_SEND);
					intent.setType("text/plain");
					intent.putExtra(Intent.EXTRA_SUBJECT, result.getName());
					intent.putExtra(Intent.EXTRA_TEXT, url);
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
					invalidateOptionsMenu();
				}
				return;
			}
			Log.e("LoadFullInfoTask", "Got null result");
		}
	}

    private final class ButtonListener implements View.OnClickListener{

        /**
         * Starts the tour by calling showList()
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
        public void onTapTabItem(String title)
        {
        	setTitle(title);
        }
        @JavascriptInterface
        public void onTapScoreButton(String title)
        {
        	currentNavTitle = title; // re-set current nav title in order to update nav title when loading is finished.
//  
        }
        @JavascriptInterface
        public void onTapCompareButton(String title)
        {
        	String originTitle = SiegelklarheitApplication
			.getCurrentSiegel().getName();
        	currentNavTitle = title+" "+originTitle;
        }
        
        @JavascriptInterface
        public void onTapExternalLink(String link)
        {
			Intent intent = new Intent(Intent.ACTION_VIEW);
			intent.setData(Uri.parse(link));
			startActivity(intent);
        }
        
        @JavascriptInterface
        public void onTapItemInCompareList(String title)
        {

        	String originTitle = SiegelklarheitApplication
			.getCurrentSiegel().getName();
        	currentNavTitle = title;

        }
    }
}
