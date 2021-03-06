package com.ibrow.de.giz.siegelklarheit;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.widget.Button;


/**
 * Activity that shows "weiter infos".
 *
 * @author Pete
 */
public class InfosActivity extends Activity {

    protected Button startTourBtn;

    protected NavDrawHelper navDraw;
    private IdentifeyeAPIInterface api;    

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_infos);

        SiegelklarheitApplication app = (SiegelklarheitApplication) getApplicationContext();
        api = app.getAPI();
        api.initDiskCache(this);        
        
        WebView html_view=(WebView) findViewById(R.id.info_webview);
        html_view.loadUrl(api.getWebviewBaseURL()+"webviews/weitere-infos");        

        startTourBtn = (Button) findViewById(R.id.start_tour_btn);
        startTourBtn.setOnClickListener( new ButtonListener() );

        navDraw = new NavDrawHelper(this, (DrawerLayout) findViewById(R.id.drawer_layout) );
    }

    /**
     * Starts tour activity - from button click.
     */
    protected void startTour(){
        Intent intent = new Intent (this, TourActivity.class);
        startActivity(intent);
    }

    /* menu */

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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (navDraw.onOptionsItemSelected(item)) {
            return true;
        }
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
