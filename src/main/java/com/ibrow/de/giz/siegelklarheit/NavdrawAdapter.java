package com.ibrow.de.giz.siegelklarheit;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * List adapter for the navigation draw.
 *
 * @author Pete
 * @see com.ibrow.de.giz.siegelklarheit.NavDrawItem
 */
public final class NavdrawAdapter extends BaseAdapter {

    private final Context context;
    private final ArrayList<NavDrawItem> items;
    private final LayoutInflater inflater;


    public NavdrawAdapter(Context context, ArrayList<NavDrawItem> items){
        super();
        this.context = context;
        this.items = items;
        this.inflater = (LayoutInflater) context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public Object getItem(int position) {
        return items.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        /*if (convertView == null) {
            LayoutInflater mInflater = (LayoutInflater)
                    context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
            convertView = mInflater.inflate(R.layout.drawer_list_item, null);
        }*/
        //LayoutInflater inflater = (LayoutInflater) context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);

        View rootview = null;
        NavDrawItem item = items.get(position);

        if(item.isMenuItem()){
            rootview = inflater.inflate(R.layout.fragment_navdraw, null);
            ImageView icon = (ImageView) rootview.findViewById(R.id.navdraw_icon);
            TextView label = (TextView) rootview.findViewById(R.id.navdraw_text);
            label.setText( item.getName() );
            icon.setImageResource( item.getIconResourceId() );
        }
        else { // special infos item at bottom
            rootview = inflater.inflate(R.layout.fragment_navdraw_infos, null);
        }

        return rootview;
    }
}
