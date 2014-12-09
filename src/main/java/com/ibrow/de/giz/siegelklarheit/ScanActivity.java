package com.ibrow.de.giz.siegelklarheit;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ListView;
//import android.widget.Toast;

import java.util.List;

/**
 * The scan label activity.
 *
 *  @author Pete
 *  @todo Clean the code up
 */
public class ScanActivity extends Activity implements View.OnClickListener, PictureTakenCallback, View.OnTouchListener {


    private CameraInterface camera;
    private CameraPreviewFrame previewFrame;
    private ViewfinderView finder;
    private Button scanButton;

    private IdentifeyeAPIInterface api;

    protected NavDrawHelper navDraw;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_ACTION_BAR_OVERLAY);

        if (Build.VERSION.SDK_INT < 16) {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }

        setContentView(R.layout.activity_scan);


        getActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.navigation_translucent)));

        // need to de the following, even though its in onResume()
        // before calling connectPreviewFrame()
        // so view finder calculated correctly
        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
        decorView.setSystemUiVisibility(uiOptions);

        camera = CameraProvider.getCamera();
        if(camera == null) {
            Log.d("SCAN", "No camera from provider");
        }

        try {
            camera.initalise();
            connectPreviewFrame();
        } catch (Exception e) {
            Log.d("CAMERA", "Could not get camera: " + e.getMessage());
            showCameraErrorDialog(e.getMessage());
        }

        SiegelklarheitApplication app = (SiegelklarheitApplication) getApplicationContext();
        api = app.getAPI();


        try{
            PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            api.setVersionInfo(pInfo.versionName, Build.VERSION.RELEASE);
        }
        catch (Exception e){
            Log.e("SCAN", "Could not set api version info" + e.getMessage());
        }

        scanButton = (Button)findViewById(R.id.button_scan);
        scanButton.setOnClickListener(this);
        scanButton.setEnabled(true);

        new PingTask(api).execute((Void[])null);

        navDraw = new NavDrawHelper(this, (DrawerLayout) findViewById(R.id.drawer_layout) );
    }

    @Override
    protected void onStart(){
        super.onStart();
        checkOnline();
    }

    /**
     * Checks if we've got a network connection,
     * and if not displays a warning dialog.
     *
     * @return true if we have a network connection
     */
    private boolean checkOnline() {
        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected() ){
            return true;
        }
        AlertDialog.Builder builder = new AlertDialog.Builder( this );
        builder.setMessage(getString(R.string.no_connection_msg) );
        builder.setTitle(R.string.warning);
        builder.setPositiveButton(R.string.ok_btn ,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        builder.show();
        return false;
    }

    /**
     * Re-initialse the camera when the activity is resumed.
     */
    private void reinitialiseCamera(){
        if(camera != null){
            if( camera.getIsInitialised() ){
                return; // we've probably been called after onStart()
            }
        }
        else{
            camera = CameraProvider.getCamera();
        }

        try {
            camera.initalise();
            //if(previewFrame==null) {
                connectPreviewFrame();
            //}
            //else {
            //    previewFrame.setCamera(camera);
            //}
        } catch (Exception e) {
            Log.e("CAMERA", "Could not get camera: " + e.getMessage());
            showCameraErrorDialog(e.getMessage());
        }
    }

    @Override
    protected void onResume(){
        Log.v("SCAN", "onResume() called");
        super.onResume();
        hideNavBar();
    }

    private final void hideNavBar(){

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);

            /*
        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;

        // The following hides the stauts bar AND automatically the app navigation bar
        if (Build.VERSION.SDK_INT >= 16) {
            int sys_ui_flay_fullscreen = 0;
            try {
                sys_ui_flay_fullscreen = ((Integer) View.class.getDeclaredField("SYSTEM_UI_FLAG_FULLSCREEN").get(Integer.class)).intValue();
            }
            catch (Exception e){
                Log.e("SYSTEM_UI_FLAG_FULLSCREEN", e.getMessage());
            }
            uiOptions = uiOptions | sys_ui_flay_fullscreen;
            //uiOptions = uiOptions | View.SYSTEM_UI_FLAG_FULLSCREEN  <-- this can't compile against sdk for API 15!
        }

        decorView.setSystemUiVisibility(uiOptions);
        */
    }

    @Override
    protected void onRestart(){
        Log.v("SCAN", "onRestart() called");
        super.onRestart();
        reinitialiseCamera();
    }

    @Override
    protected void onStop (){
        if( camera!=null ){
            camera.release();
            camera = null;
        }
        super.onStop();
    }

    @Override
    protected void onDestroy(){
        // should be already done in onStop, but just to be sure:
        if( camera!=null ){
            camera.release();
            camera = null;
        }
        super.onDestroy();
    }


    /**
     * Adds the camera preview view to display.
     *
     * @see com.ibrow.de.giz.siegelklarheit.CameraPreviewFrame
     */
    private void connectPreviewFrame(){
        Log.v("SCAN", "connecting preview frame");
        previewFrame = new CameraPreviewFrame(this, camera);
        FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);
        preview.removeAllViewsInLayout(); // remove old dead children
        preview.addView(previewFrame);
        finder = new ViewfinderView(this);
        finder.setCamera(camera);
        finder.setOnTouchListener(this);
        preview.addView(finder);
    }

    /**
     * Shows error dialog when we can't connect to the camera.
     *
     * We try again (though unlikey to work) to initialise the camera
     * when the user dismisses the dialog.
     *
     * @param error_message The error message from the exception
     */
    private void showCameraErrorDialog(final String error_message){
        AlertDialog.Builder builder = new AlertDialog.Builder( this );
        builder.setCancelable(false);
        builder.setMessage(getString(R.string.no_camera_message) + " (" + error_message + ")");
        builder.setTitle(R.string.no_camera_title);
        builder.setPositiveButton(R.string.no_camera_retry_btn,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        try {
                            camera.initalise();
                            connectPreviewFrame();
                            // got camera now, so dismiss dialog
                            dialog.cancel();
                        } catch (Exception e) {
                            Log.d("CAMERA", "Could not get camera: " + e.getMessage());
                            //Toast.makeText(getApplicationContext(), R.string.no_camera_title, Toast.LENGTH_SHORT).show();
                            showCameraErrorDialog(e.getMessage());
                        }
                    }
                });
        builder.show();
    }

    /**
     * Called when the user clicks on the scan now button.
     *
     * Calls startScan()
     *
     * @param v
     * @see #startScan()
     */
    public void onClick(View v) {
        //hideNavBar();
        startScan();
    }

    protected void startScan(){
        if(! checkOnline() ){
            return;
        }
        scanButton.setEnabled(false);
        finder.setActive(true);
        camera.takePicture(this);
        //camera.stopPreview(); - not needed, done by camera itself
    }

    /**
     * Callback from camera when it's taken the photo.
     *
     * This forks a ScanPictureTask which calls the API, which when finished calls ProcessScanResult.
     *
     * @param image
     *
     * @see com.ibrow.de.giz.siegelklarheit.PictureTakenCallback
     * @see com.ibrow.de.giz.siegelklarheit.CameraInterface#takePicture(PictureTakenCallback)
     * @see #onClick(android.view.View)
     *
     */
    public void onPictureTaken(byte[] image){
        Log.v("SCAN", "Got photo!");
        //camera.release();
        //camera = null;
        finder.setActive(false);
        new ScanPictureTask(image, api, new ProgressDialog(this) ).execute((Void[]) null);
    }

    /**
     * Called if there's an error calling the API from ScanPictureTask.
     *
     * @param error The error that occurred
     */
    private void apiError(final Exception error){
        String msg;
        if(error != null){ //potential wirdness with async task where exception is null
            msg = error.getMessage();
        }
        else {
            msg = "[unknown]";
        }
        AlertDialog.Builder builder = new AlertDialog.Builder( this );
        builder.setCancelable(false);
        builder.setMessage(getString(R.string.scan_error) + " (" + msg + ")");
        builder.setTitle(R.string.error);
        builder.setPositiveButton(R.string.ok_btn,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        //reinitialiseCamera();
                        camera.startPreview();
                        scanButton.setEnabled(true);
                    }
                });
        builder.show();
    }

    /**
     * Called when the scan is complete.
     *
     * We do 1 of 3 things here.
     *
     * If there's no results, we show a dialog with the option to try again
     * If there's one result only, we show the detail activity for that result
     * If there's more than 1 result, we show a list of the possible matches
     *
     * @param results The result of the API call - maybe empty.
     *
     * @see com.ibrow.de.giz.siegelklarheit.IdentifeyeAPIInterface#identifySiegel(byte[])
     * @see com.ibrow.de.giz.siegelklarheit.ShortSiegelInfo
     * @see com.ibrow.de.giz.siegelklarheit.ScanActivity.ScanPictureTask
     */
    protected void ProcessScanResult(List<ShortSiegelInfo> results){
        Log.v("SCAN", "scan complete");
        int count = results.size();
        if(count == 0){
            showTryAgainDialog();
            return;
        }

        if(count == 1){
            ShortSiegelInfo siegel = results.get(0); // get first and only result
            Intent intent = new Intent (this, DetailsActivity.class);

            SiegelklarheitApplication.setCurrentSiegel(siegel);

            scanButton.setEnabled(true);
            startActivity(intent);

            return;
        }
        // more results
        Intent intent = new Intent (this, MultipleResultsActivity.class);

        SiegelklarheitApplication.setLastMultipleMatches(results);
        scanButton.setEnabled(true);
        startActivity(intent);
    }

    protected void showTryAgainDialog(){
        final Context context = this;
        AlertDialog.Builder builder = new AlertDialog.Builder( this );
        builder.setCancelable(false);
        builder.setMessage(getString(R.string.scan_not_recognised_msg));
        builder.setTitle(R.string.scan_not_recognised_title);
        builder.setPositiveButton(R.string.scan_not_recognised_tryagain_btn,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        camera.startPreview();
                        scanButton.setEnabled(true);
                    }
                });
        builder.setNegativeButton(R.string.scan_not_recognised_search_btn,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(context, SearchActivity.class);
                        scanButton.setEnabled(true);
                        startActivity(intent);
                    }
                });
        builder.show();
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

        if(camera != null){
            if(camera.getIsInitialised()){
                try{
                    camera.release();
                }
                catch (Exception e){
                    Log.e("CAMERA", "Could not release camera [menu click]: "+e.getMessage());
                }
            }
            camera = null;
        }

        switch(id){
            case R.id.action_scan:
                // even though we're already in the scan activity
                // this menu item is active as a fallback
                // to restart the activity e.g. camera crashed
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


    public boolean onTouch(View v, MotionEvent event){
        //Log.d("SCAN", "screen touched");
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        if( camera != null && camera.getIsInitialised() ){
            //Log.d("SCAN","touch, camera ready");
            int action = event.getActionMasked();
            //Log.d("SCAN","action is "+action);
            switch(action) {
                case MotionEvent.ACTION_UP :
                    //Log.d("SCAN", "ACTION up at x "+event.getX()+", y "+event.getY() );

                    int x=(int) event.getX();
                    int y=(int) event.getY();
                    Rect finder=camera.getViewFramingRect();
                    if( (x>finder.left && x<finder.right) && (y>finder.top && y<finder.bottom)  ){
                        Log.v("SCAN", "Finder touched");
                        startScan();
                        //hideNavBar();
                    }
                    break;

            }

        }
        return true;
    }

    /* internal classes */

    /**
     * Pings the API asynch.
     */
    private final class PingTask extends AsyncTask<Void, Void, Void> {
        private IdentifeyeAPIInterface api;

        PingTask(IdentifeyeAPIInterface api){
            this.api = api;
        }

        protected Void doInBackground(Void... params) {
            try {
                Log.d("API", "ping!");
                api.ping();
            }
            catch (Exception e){
                Log.w("API", e.getMessage() );
            }
            return null;
        }

        protected void onPostExecute(Void result) {
            //NOP
        }
    }

    /**
     * Asynchronous task that calls the API with the photo and returns the result of possible matches.
     *
     */
    private final class ScanPictureTask extends AsyncTask<Void, Void, List<ShortSiegelInfo>> {

        private byte[] image;
        private IdentifeyeAPIInterface api;
        private ProgressDialog progressDialog;
        private Exception error;

        ScanPictureTask(byte[] image, IdentifeyeAPIInterface api, ProgressDialog progressDialog){
            super();
            this.image = image;
            this.api = api;
            this.progressDialog = progressDialog;

        }

        protected void onPreExecute (){
            progressDialog.setIndeterminate(true);
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressDialog.setCancelable(false);
            progressDialog.setMessage(getString(R.string.scanning));
            progressDialog.show();
        }

        protected List<ShortSiegelInfo>  doInBackground(Void... params) {
            Log.d("ScanPictureTask", "running...");
            List<ShortSiegelInfo> result;
            try{
                result = api.identifySiegel(image);
                // if(true) {throw new Exception("test foobar");}
            }
            catch(Exception e){
                if(e != null ) {
                    String msg;
                    if(e!=null && e.getMessage()!=null){
                        msg = "ScanPictureTask: " + e.getMessage();
                    }
                    else {
                        msg="[unknown in ScanPictureTask]";
                    }
                    Log.e("API", msg);
                    error = e;
                }
                return null;
            }
            return result;
        }


        protected void onPostExecute(List<ShortSiegelInfo>  result) {
            Log.d("ScanPictureTask", "finished");
            progressDialog.dismiss();
            if(result == null){
                apiError(error);
                return;
            }
            ProcessScanResult(result);
        }
    }
}
