package com.ibrow.de.giz.siegelklarheit;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebView;


/**
 * Activity that shows the Impressum
 *
 * @author Pete
 */
public class ImprintActivity extends Activity {

    protected NavDrawHelper navDraw;
    private IdentifeyeAPIInterface api;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_imprint);

        SiegelklarheitApplication app = (SiegelklarheitApplication) getApplicationContext();
        api = app.getAPI();
        api.initDiskCache(this);

        
        WebView html_view=(WebView) findViewById(R.id.imprint_webview);
        html_view.getSettings().setJavaScriptEnabled(true);

        // @todo Should be converted to a helper class 
        html_view.getSettings().setJavaScriptEnabled(true);
        html_view.getSettings().setDomStorageEnabled(true);
        html_view.getSettings().setAppCacheMaxSize(1024*1024*8);
        html_view.getSettings().setAppCachePath("/data/data/"+ getPackageName() +"/cache");        
        html_view.getSettings().setAllowFileAccess(true);
        html_view.getSettings().setAppCacheEnabled(true);        
        html_view.loadUrl(api.getWebviewBaseURL()+"webviews/impressum");
        //html_view.loadUrl("file:///android_asset/html/impressum.html");

        navDraw = new NavDrawHelper(this, (DrawerLayout) findViewById(R.id.drawer_layout) );
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
                intent = new Intent (this, InfosActivity.class);
                startActivity(intent);
                break;
            case R.id.action_imprint:
                // already here do nothing
                break;
        }
        return super.onOptionsItemSelected(item);
    }

}
