package com.ibrow.de.giz.siegelklarheit;

import android.app.Activity;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * List content fragment, used for the drown down menu in Search activity.
 *
 * @see <a href="http://developer.android.com/guide/topics/ui/actionbar.html">Action bar docs</a>
 * @see com.ibrow.de.giz.siegelklarheit.SearchActivity
 */
public class ListContentFragment extends Fragment {
    private String mText;

    @Override
    public void onAttach(Activity activity) {
        // This is the first callback received; here we can set the text for
        // the fragment as defined by the tag specified during the fragment
        // transaction
        super.onAttach(activity);
        mText = getTag();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        //LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rootView = inflater.inflate(R.layout.drowndown_container, container, false);

        // This is called to define the layout for the fragment;
        // we just create a TextView and set its text to be the fragment tag
        //TextView text = new TextView(getActivity());
        TextView text = (TextView) rootView.findViewById(R.id.dropdown_text);
        text.setText(mText);
        //return text;
        return  rootView;
    }
}