package com.ibrow.de.giz.siegelklarheit;

import android.app.Activity;
import android.os.Bundle;

/**
 * Activity when we have more than 1 possible match from an image scan.
 *
 * @author Pete
 */
public class MultipleResultsActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_multipleresults);

        getActionBar().setDisplayHomeAsUpEnabled(true);
    }
}
