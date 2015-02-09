package com.ibrow.de.giz.siegelklarheit;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.webkit.WebView;

@SuppressLint({ "NewApi", "SetJavaScriptEnabled" }) public class SiegelWebView extends WebView {

	@SuppressWarnings("deprecation")
	public SiegelWebView(Context context) {
		super(context);
		init(context);
	}

	public SiegelWebView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	public SiegelWebView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context);
	}

	public SiegelWebView(Context context, AttributeSet attrs, int defStyleAttr,
			int defStyleRes) {
		super(context, attrs, defStyleAttr, defStyleRes);
		init(context);
	}
	
	@SuppressWarnings("deprecation")
	private void init(Context context) {
		getSettings().setJavaScriptEnabled(true);
        getSettings().setDomStorageEnabled(true);
        getSettings().setAppCacheMaxSize(1024*1024*8);
        getSettings().setAppCachePath(context.getString(R.string.directory)+context.getPackageName()+"/cache");        
        getSettings().setAllowFileAccess(true);
        getSettings().setAppCacheEnabled(true);  
	}

}
