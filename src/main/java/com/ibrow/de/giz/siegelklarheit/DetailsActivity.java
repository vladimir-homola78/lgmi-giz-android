package com.ibrow.de.giz.siegelklarheit;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.ShareActionProvider;
import android.widget.TextView;

import static com.ibrow.de.giz.siegelklarheit.LogoHelper.getFromMemoryCache;

/**
 * Shows the details of a Siegel.
 *
 * @author Pete
 */
public class DetailsActivity extends Activity {

    private IdentifeyeAPIInterface api;

    private static final String DRAWABLE="@drawable/";
    private static final String STRING="@string/";

    protected Drawable blankLogo;

    private ShareActionProvider shareActionProvider;
    private boolean haveShareURL = false;

    protected NavDrawHelper navDraw;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);

        //getActionBar().setDisplayHomeAsUpEnabled(true);

        SiegelklarheitApplication app = (SiegelklarheitApplication) getApplicationContext();
        api = app.getAPI();

        LogoHelper.initDiskCachePath(this);
        blankLogo = getResources().getDrawable(R.drawable.blank_label_logo);

        ShortSiegelInfo siegel_short_info = SiegelklarheitApplication.getCurrentSiegel();
        assert siegel_short_info != null;

        setMainDisplay(siegel_short_info);
        WebView html_view=(WebView) findViewById(R.id.details_webview);
        //html_view.loadUrl("file:///android_asset/loading.html");

        new LoadFullInfoTask(api).execute( new Integer(siegel_short_info.getId()) );

        navDraw = new NavDrawHelper(this, (DrawerLayout) findViewById(R.id.drawer_layout) );

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

    private void setMainDisplay(final Siegel siegel){
        setTitle(siegel.getName());

        SiegelRating rating = siegel.getRating();
        View rating_holder=(View) findViewById(R.id.rating_view);

        rating_holder.setBackgroundColor(rating.getColor());

        ImageView rating_image_view = (ImageView) findViewById(R.id.rating_symbol_image);
        rating_image_view.setImageDrawable( getResources().getDrawable(getResources().getIdentifier(DRAWABLE + rating.getImageIdentifier(), null, getPackageName())) );

        TextView rating_text_view = (TextView) findViewById(R.id.rating_symbol_text);
        rating_text_view.setText(
                getResources().getText(
                        getResources().getIdentifier(STRING + rating.getDescriptionIdentifier(), null, getPackageName() )
                )
        );

        Bitmap image = getFromMemoryCache(siegel);
        if(image != null ){
            ImageView logo_image_view = (ImageView) findViewById(R.id.logo_view);
            logo_image_view.setImageBitmap(image);
        }
        else {
            LoadSiegelLogoTask logo_task = new LoadSiegelLogoTask();
            logo_task.execute(siegel);
        }
    }

    /* menu */

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_details, menu);

        MenuItem shareItem = menu.findItem(R.id.action_share);
        shareActionProvider = (ShareActionProvider) shareItem.getActionProvider();
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
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
                intent = new Intent (this, ImprintActivity.class);
                startActivity(intent);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    /* internal classes */


    private class LoadSiegelLogoTask extends LogoLoaderTask{

        private boolean gotImage=false;

        @Override
        protected void onProgressUpdate(Bitmap... progress) {
            ImageView logo_image_view = (ImageView) findViewById(R.id.logo_view);
            logo_image_view.setImageBitmap(progress[0]);
            gotImage=true;
        }

        protected void onPostExecute(Void result){
            if(! gotImage ){
                ImageView logo_image_view = (ImageView) findViewById(R.id.logo_view);
                logo_image_view.setImageDrawable(blankLogo);
            }
        }
    }

    private class LoadFullInfoTask extends AsyncTask<Integer,Void,SiegelInfo>{

        IdentifeyeAPIInterface api;
        Exception error;

        LoadFullInfoTask(final IdentifeyeAPIInterface api){
            this.api = api;
        }
        protected SiegelInfo doInBackground(Integer... siegel_id){
            SiegelInfo siegel=null;
            try{
                siegel = api.getInfo( siegel_id[0].intValue() );
            }
            catch (Exception e){
                Log.e("LoadFullInfoTask-API", e.getMessage());
                error = e;
            }
            return siegel;
        }

        protected void onPostExecute(SiegelInfo result){
            if(result!=null){
                Log.d("LoadFullInfoTask", "got result for id "+result.getId());
                WebView html_view=(WebView) findViewById(R.id.details_webview);
                html_view.getSettings().setJavaScriptEnabled(true);
                html_view.loadDataWithBaseURL(api.getWebviewBaseURL(), result.getDetails(), "text/html", "UTF-8", null);
                //Log.d("LoadFullInfoTask", "Html:"+result.getDetails());
                String url = result.getShareURL();
                if( ! url.isEmpty() ){
                    Intent intent = new Intent(Intent.ACTION_SEND);
                    intent.setType("text/plain");
                    intent.putExtra(Intent.EXTRA_SUBJECT, result.getName());
                    intent.putExtra(Intent.EXTRA_TEXT, url);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
                    shareActionProvider.setShareIntent(intent);
                    haveShareURL = true;
                }
                else {
                    invalidateOptionsMenu();
                }
                return;
            }
            Log.e("LoadFullInfoTask", "Got null result");
        }
    }
}
