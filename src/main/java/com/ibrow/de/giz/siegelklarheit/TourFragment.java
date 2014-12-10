package com.ibrow.de.giz.siegelklarheit;

import android.content.res.Resources;
import android.os.Bundle;

import android.support.v4.app.Fragment;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Tour Fragment object.
 *
 * @see com.ibrow.de.giz.siegelklarheit.TourPagerAdapter
 * @see com.ibrow.de.giz.siegelklarheit.TourActivity
 */
public final class TourFragment extends Fragment {

    private int number=1;

    private final static String PARAM="number";
    private final static String PACKAGE = "com.ibrow.de.giz.siegelklarheit";

    private Resources resources;

    /**
     * Factory method.
     * @param number
     * @return
     */
    static TourFragment newInstance(int number) {
        TourFragment f = new TourFragment();
        Bundle args = new Bundle();
        args.putInt(PARAM, number);
        f.setArguments(args);
        return f;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        resources = getResources();

        number = getArguments() != null ? getArguments().getInt(PARAM) : 1;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate( R.layout.fragment_tour, container, false);
        //Bundle args = getArguments();
        if(resources==null){
            Log.e("TOUR", "Null resources");
        }
        Log.v("TOUR", "Number is "+number);

        ((TextView) rootView.findViewById(R.id.tour_title)).setText(
                getString(
                        resources.getIdentifier("@string/tour_title_" + number, null, PACKAGE)
                )
        );

        ((ImageView) rootView.findViewById(R.id.tour_image)).setImageDrawable(
                resources.getDrawable(
                        resources.getIdentifier("@drawable/tour" + number, null, PACKAGE)
                )
        );

        ((TextView) rootView.findViewById(R.id.tour_desc)).setText(
                getString(
                        resources.getIdentifier("@string/tour_desc_" + number, null, PACKAGE)
                )
        );

        return rootView;
    }
}


