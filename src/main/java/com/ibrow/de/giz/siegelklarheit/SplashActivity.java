package com.ibrow.de.giz.siegelklarheit;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

/**
 * Splash screen activity.
 *
 * @see com.ibrow.de.giz.siegelklarheit.StartActivity
 * @author Pete
 */
public class SplashActivity extends Activity implements View.OnClickListener{

    protected NavDrawHelper navDraw;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        navDraw = new NavDrawHelper(this, (DrawerLayout) findViewById(R.id.drawer_layout));

        ((Button) findViewById(R.id.splash_search_btn)).setOnClickListener(this);
        ((Button) findViewById(R.id.splash_scan_btn)).setOnClickListener(this);
        ((Button) findViewById(R.id.splash_infos_btn)).setOnClickListener(this);
    }

    /**
     * Called when one of the buttons is clicked
     * @param v
     */
    public void onClick(View v) {
        int id  = v.getId();
        Intent intent;
        switch (id){
            case R.id.splash_search_btn:
                intent = new Intent(this, SearchActivity.class);
                startActivity(intent);
                finish();
                break;

            case R.id.splash_scan_btn:
                intent = new Intent(this, ScanActivity.class);
                startActivity(intent);
                finish();
                break;

            case R.id.splash_infos_btn:
                intent = new Intent(this, InfosActivity.class);
                startActivity(intent);
                finish();
                break;
        }
    }

     /* menu stuff */

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
}