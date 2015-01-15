package com.ibrow.de.giz.siegelklarheit;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

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

        SharedPreferences settings = getPreferences( Context.MODE_PRIVATE );
        boolean alreadyRun = settings.getBoolean( ALREADY_RUN_ONCE, false );

        if( alreadyRun  ){
            //intent = new Intent( this, ScanActivity.class );
            intent = new Intent( this, SplashActivity.class );
        }
        else {
            intent = new Intent( this, TourActivity.class );
            intent.putExtra(TourActivity.FROM_FIRST_RUN, true);
            SharedPreferences.Editor editor = settings.edit();
            editor.putBoolean( ALREADY_RUN_ONCE, true );
            editor.commit();
        }

        startActivity(intent);
        finish();
    }

}
