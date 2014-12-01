package com.ibrow.de.giz.siegelklarheit;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.widget.Button;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Activity that shows "weiter infos"
 *
 * @author Pete
 */
public class InfosActivity extends Activity {

    protected Button startTourBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_infos);

        WebView html_view=(WebView) findViewById(R.id.info_webview);
        // WebView.setWebContentsDebuggingEnabled(true); <- needs API 19
        html_view.getSettings().setJavaScriptEnabled(true);

        html_view.loadUrl("file:///android_asset/html/weitere-infos.html");

        startTourBtn = (Button) findViewById(R.id.start_tour_btn);
        startTourBtn.setOnClickListener( new ButtonListener() );
    }

    /**
     * Starts tour activity - from button click.
     * @todo implement
     *
     */
    protected void startTour(){
        /*
        Intent intent = new Intent (this, TourActivity.class);
        startActivity(intent);
        */
    }

    /* menu */

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        Intent intent;
        switch(id){
            case R.id.action_scan:
                intent = new Intent (this, ScanActivity.class);
                startActivity(intent);
                break;
            case R.id.action_search:
                intent = new Intent (this, SearchActivity.class);
                startActivity(intent);
                break;
            case R.id.action_info:
                // already here
                break;
            case R.id.action_imprint:
                intent = new Intent (this, ImprintActivity.class);
                startActivity(intent);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    /* Private classes */

    private final class ButtonListener implements View.OnClickListener{

        /**
         * Starts the tour by calling satartTour()
         * @see #startTour()
         * @param v
         */
        public void onClick(View v) {
            startTour();
        }

    }
}