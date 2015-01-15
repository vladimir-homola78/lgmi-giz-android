package com.ibrow.de.giz.siegelklarheit;

import android.app.Activity;
import android.os.Bundle;
import android.webkit.WebView;

/**
 * Splash screen activity.
 *
 * @see com.ibrow.de.giz.siegelklarheit.StartActivity
 * @author Pete
 */
public class SampleActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sample);
        
        String url = getIntent().getStringExtra("link_to_go");
        
        WebView webView = (WebView)findViewById(R.id.webView1);
        webView.loadUrl(url);
    }

}