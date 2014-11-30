package com.ibrow.de.giz.siegelklarheit;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Activity that shows the Impressum
 *
 * @author Pete
 */
public class ImprintActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_imprint);

        WebView html_view=(WebView) findViewById(R.id.imprint_webview);
        html_view.getSettings().setJavaScriptEnabled(true);

        StringBuilder buf=new StringBuilder();
        BufferedReader in=null;
        try {
            InputStream is=getAssets().open("html/impressum.html");
            in= new BufferedReader(new InputStreamReader(is, "UTF-8"));
            String str;

            while ((str = in.readLine()) != null) {
                buf.append(str);
            }
        }
        catch (IOException ioe){
            Log.e("IMPRINT", ioe.getMessage());
        }
        finally {
            try {
                in.close();
            }
            catch (IOException ioe){
                Log.e("IMPRINT", ioe.getMessage());
            }
        }


        html_view.loadDataWithBaseURL("file:///android_asset/html/", buf.toString(), "text/html", "UTF-8", null);
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
