package com.ibrow.de.giz.siegelklarheit;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import com.crashlytics.android.Crashlytics;

/**
 * This class starts Scan activity,
 * or if this is the first time run
 * the Tour activity.
 *
 * @see com.ibrow.de.giz.siegelklarheit.ScanActivity
 * @see com.ibrow.de.giz.siegelklarheit.TourActivity
 * @author Pete
 */
public class StartActivity extends Activity{

    private Intent intent;

    private static final String ALREADY_RUN_ONCE = "already_run_once";

    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);
		Crashlytics.start(this);
    }
    
    @Override
    public void onStart() {
    	super.onStart();
    	
    	SharedPreferences settings = getPreferences( Context.MODE_PRIVATE );
        boolean alreadyRun = settings.getBoolean( ALREADY_RUN_ONCE, false );
    	//boolean alreadyRun = false;

        if( alreadyRun  ){
            intent = new Intent( this, SplashActivity.class );
            startActivity(intent);
            finish();
        }
        else {
            intent = new Intent( this, TourActivity.class );
            intent.putExtra(TourActivity.FROM_FIRST_RUN, true);
            SharedPreferences.Editor editor = settings.edit();
            editor.putBoolean( ALREADY_RUN_ONCE, true );
            editor.commit();
            startActivityForResult(intent, 100);
        }
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    	
        // Check which request we're responding to
        if (requestCode == 100) {
            // Make sure the request was successful
            if (resultCode == RESULT_OK) {
            	
            	Log.e("Tour is finished", "Tour is finished");
            	intent = new Intent( this, SplashActivity.class);
            	startActivity(intent);
            	finish();
            }
        }
    }
    
    @Override
    public void onDestroy() {
    	super.onDestroy();
    	Log.e("StartActivity", "onDestroy");
    }
    
}
