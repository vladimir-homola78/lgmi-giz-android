package com.ibrow.de.giz.siegelklarheit;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.SimpleOnPageChangeListener;
import android.view.View;
import android.widget.Button;

/**
 * The guided tour activity.
 *
 * @author Pete
 */
public class TourActivity extends FragmentActivity  implements View.OnClickListener {

    protected static final String FROM_FIRST_RUN = "com.ibrow.de.giz.siegelklarheit.firstrun";

    protected static final int NUM_PAGES=3;

    TourPagerAdapter pagerAdapter;
    ViewPager viewPager;

    private boolean returnToScanActivity=false;

    Button skipTourBtn;
    private String skipTourText;
    private String endTourText;

    private Drawable circleFilled;
    private Drawable circleHollow;

    private Button[] navButtons=new Button[NUM_PAGES+1];


    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tour);

        pagerAdapter = new TourPagerAdapter( getSupportFragmentManager() );
        viewPager = (ViewPager) findViewById(R.id.tour_pager);
        viewPager.setAdapter(pagerAdapter);
        viewPager.setOnPageChangeListener( new PagerListener() );

        skipTourText = getString(R.string.skip_tour_btn);
        endTourText = getString(R.string.end_tour_btn);

        if( getIntent().getBooleanExtra(FROM_FIRST_RUN, false ) ){
            returnToScanActivity = true;
        }

        skipTourBtn = (Button) findViewById(R.id.end_tour_btn);
        skipTourBtn.setOnClickListener(this);

        // navigation circle buttons
        circleFilled = getResources().getDrawable( R.drawable.circle);
        circleHollow = getResources().getDrawable( R.drawable.circle_hollow);

        for(int i=1; i < (NUM_PAGES+1 ); i++){
            navButtons[i] = (Button) findViewById( getResources().getIdentifier("@id/tour_nav_btn_"+i, null, getPackageName() ) );
            navButtons[i].setOnClickListener( new NavButtonListener(i) );
        }

    }

    /**
     * Called when the skip tour button is clicked.
     *
     * @param v
     */
    public void onClick(View v) {
        Intent intent;
        if( ! returnToScanActivity ){
            intent = new Intent( this, InfosActivity.class);
        }
        else {
            intent = new Intent( this, ScanActivity.class);
        }
        startActivity(intent);
    }

    /* internal classes */

    private final class PagerListener extends ViewPager.SimpleOnPageChangeListener{

        /**
         * Deals with changing the button label on last page...
         *
         * Also changes the circle of the nav buttons.
         *
         * @param position
         * @see #NUM_PAGES
         */
        @Override
        public void onPageSelected (int position){
            int page = position + 1;
            for(int i=1; i < (NUM_PAGES+1 ); i++){
                if( i == page ){
                    navButtons[i].setBackgroundDrawable(circleFilled);
                }
                else {
                    navButtons[i].setBackgroundDrawable(circleHollow);
                }
            }

            if( page != NUM_PAGES){
                skipTourBtn.setText(skipTourText);
            }
            else {
                skipTourBtn.setText(endTourText);
            }
        }

    }

    private final class NavButtonListener implements View.OnClickListener{

        private int page = 1;

        NavButtonListener(int page){
            this.page = page;
        }

        public void onClick(View v) {
            viewPager.setCurrentItem( (page-1), true);
        }
    }


}
