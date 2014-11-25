package com.ibrow.de.giz.siegelklarheit;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.Toast;

/* following 2 only needed for logging stacktrace in ScanPictureTask - debugging */
import java.io.PrintWriter;
import java.io.StringWriter;
/* end temp import */
import java.util.List;

/**
 * The scan label activity.
 *
 *  @author Pete
 *  @todo Clean the code up
 */
public class ScanActivity extends Activity implements View.OnClickListener, PictureTakenCallback {


    private CameraInterface camera;
    private CameraPreviewFrame previewFrame;
    private Button scanButton;

    private IdentifeyeAPIInterface api;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_ACTION_BAR_OVERLAY);

        setContentView(R.layout.activity_scan);

        View decorView = getWindow().getDecorView();
        // Hide both the navigation bar and the status bar.
        // SYSTEM_UI_FLAG_FULLSCREEN is only available on Android 4.1 and higher, but as
        // a general rule, you should design your app to hide the status bar whenever you
        // hide the navigation bar.
        int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                /*| View.SYSTEM_UI_FLAG_FULLSCREEN*/;
        decorView.setSystemUiVisibility(uiOptions);



        //getActionBar().setBackgroundDrawable(new ColorDrawable(Color.argb(128, 0, 0, 0)));

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

        scanButton = (Button)findViewById(R.id.button_scan);
        scanButton.setOnClickListener(this);
        scanButton.setEnabled(true);

        new PingTask(api).execute((Void[])null);
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
            if(previewFrame==null) {
                connectPreviewFrame();
            }
            else {
                previewFrame.setCamera(camera);
            }
        } catch (Exception e) {
            Log.d("CAMERA", "Could not get camera: " + e.getMessage());
            showCameraErrorDialog(e.getMessage());
        }
    }

    @Override
    protected void onResume(){
        Log.d("SCAN", "onResume() called");
        super.onResume();
    }

    @Override
    protected void onRestart(){
        Log.d("SCAN", "onRestart() called");
        reinitialiseCamera();
        super.onRestart();
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
        previewFrame = new CameraPreviewFrame(this, camera);
        FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);
        preview.addView(previewFrame);
        ViewfinderView finder = new ViewfinderView(this);
        finder.setCamera(camera);
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
        builder.setPositiveButton(R.string.no_camera_retry_btn ,
            new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    try {
                        camera.initalise();
                        connectPreviewFrame();
                        // got camera now, so dismiss dialog
                        dialog.cancel();
                    } catch (Exception e) {
                        Log.d("CAMERA", "Could not get camera: " + e.getMessage());
                        Toast.makeText(getApplicationContext(), R.string.no_camera_title, Toast.LENGTH_SHORT).show();
                        showCameraErrorDialog(e.getMessage());
                    }
                }
            });
        builder.show();
    }

    /**
     * Called when the user clicks on the scan now button.
     * @param v
     */
    public void onClick(View v) {
        if(! checkOnline() ){
            return;
        }
        scanButton.setEnabled(false);
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
        Toast.makeText(getApplicationContext(), "Got photo!", Toast.LENGTH_SHORT).show();
        //camera.release();
        //camera = null;
        new ScanPictureTask(image, api, new ProgressDialog(this) ).execute((Void[])null);
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
        builder.setPositiveButton(R.string.ok_btn ,
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
        Toast.makeText(getApplicationContext(), "scan complete", Toast.LENGTH_SHORT).show();
        int count = results.size();
        if(count == 0){
            showTryAgainDialog();
            return;
        }

        if(count == 1){
            ShortSiegelInfo siegel = results.get(0); // get first and only result
            Intent intent = new Intent (this, DetailsActivity.class);

            SiegelklarheitApplication.setLastMatch(siegel);

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
        builder.setPositiveButton(R.string.scan_not_recognised_tryagain_btn ,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        camera.startPreview();
                        scanButton.setEnabled(true);
                    }
                });
        builder.setNegativeButton(R.string.scan_not_recognised_search_btn,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent (context, SearchActivity.class);
                        scanButton.setEnabled(true);
                        startActivity(intent);
                    }
                });
        builder.show();
    }

    /* menu */

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        Intent intent;

        switch(id){
            case R.id.action_scan: break; // already scanning
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
                        if(e.getMessage().equals("No authentication challenges found")){ //wierdness!
                            StringWriter sw = new StringWriter();
                            e.printStackTrace(new PrintWriter(sw));
                            String stackTraceAsString = sw.toString();
                            Log.d("ScanPictureTask", "Stacktrace: "+stackTraceAsString);
                        }
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
