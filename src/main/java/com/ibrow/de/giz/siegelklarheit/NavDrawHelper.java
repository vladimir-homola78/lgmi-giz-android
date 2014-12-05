package com.ibrow.de.giz.siegelklarheit;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.widget.DrawerLayout;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.ArrayList;

/**
 * Navigation draw helper for activities.
 *
 * @see com.ibrow.de.giz.siegelklarheit.NavdrawAdapter
 * @see com.ibrow.de.giz.siegelklarheit.NavDrawItem
 */
public class NavDrawHelper extends ActionBarDrawerToggle implements ListView.OnItemClickListener {

    public final static int NUM_ITEMS=5;

    private final Activity activity;
    private final ArrayList<NavDrawItem> items  = new ArrayList<NavDrawItem>(NUM_ITEMS);
    private final NavdrawAdapter adapter;



    NavDrawHelper(Activity activity, DrawerLayout drawer_layout){
        // = (DrawerLayout) activity.findViewById(R.id.drawer_layout);
        super(activity, drawer_layout, R.drawable.nav_drawer, R.string.open, R.string.close);
        this.activity = activity;

        items.add( new NavDrawItem( activity.getString(R.string.menu_search), R.drawable.menu_search) );
        items.add( new NavDrawItem( activity.getString(R.string.menu_scan), R.drawable.menu_camera) );
        items.add( new NavDrawItem( activity.getString(R.string.menu_info), R.drawable.menu_info) );
        items.add( new NavDrawItem( activity.getString(R.string.menu_imprint), R.drawable.menu_legal) );
        items.add( new NavDrawInfoItem() );

        adapter = new NavdrawAdapter(activity, items);

        ListView  listview = (ListView) activity.findViewById(R.id.nav_drawer);
        listview.setAdapter( adapter );
        listview.setOnItemClickListener(this);


        drawer_layout.setDrawerListener(this);

        activity.getActionBar().setDisplayHomeAsUpEnabled(true);
        activity.getActionBar().setHomeButtonEnabled(true);
    }

    @Override
    public void onItemClick(AdapterView parent, View view, int position, long id) {
        Intent intent;

        switch(position){
            case 0:
                intent = new Intent (activity, SearchActivity.class);
                activity.startActivity(intent);
                break;
            case 1:
                intent = new Intent (activity, ScanActivity.class);
                activity.startActivity(intent);
                break;
            case 2:
                intent = new Intent (activity, InfosActivity.class);
                activity.startActivity(intent);
                break;
            case 3:
                intent = new Intent (activity, ImprintActivity.class);
                activity.startActivity(intent);
                break;
            case 4 : // infos item at bottom
                intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse("http://www.giz.de/"));
                activity.startActivity(intent);
                break;
        }
    }
}
