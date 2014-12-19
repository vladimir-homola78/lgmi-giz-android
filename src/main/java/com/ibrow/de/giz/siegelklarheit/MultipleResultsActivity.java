package com.ibrow.de.giz.siegelklarheit;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;

import java.util.List;

/**
 * Activity when we have more than 1 possible match from an image scan.
 *
 * @author Pete
 */
public class MultipleResultsActivity extends Activity {

    protected IdentifeyeAPIInterface api;
    protected ShortSiegelInfo[] siegel_array;

    protected NavDrawHelper navDraw;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_multipleresults);

        /**
        try{
            getActionBar().setDisplayHomeAsUpEnabled(true);
        }
        catch (NullPointerException npe){
            Log.e("MULTI", "Null for getActionBar(): "+npe.getMessage());
        }
        */

        SiegelklarheitApplication app = (SiegelklarheitApplication) getApplicationContext();
        List<ShortSiegelInfo> siegels = app.getLastMultipleMatches();

        siegel_array=new ShortSiegelInfo[siegels.size()];
        siegel_array = siegels.toArray(siegel_array);

        final ListView listview = (ListView) findViewById(R.id.list_view);

        // footer part
        final LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.listfooter_multiresults, null);
        LinearLayout list_footer = (LinearLayout) view.findViewById(R.id.list_multi_footer);
        listview.addFooterView(list_footer);

        final SiegelArrayAdapter adapter = new SiegelArrayAdapter(this, siegel_array);

        listview.setAdapter(adapter);

        listview.setOnItemClickListener(new ItemListener(this) );

        Button searchButton = (Button)findViewById(R.id.button_search);
        searchButton.setOnClickListener(new ButtonListener(this) );

        navDraw = new NavDrawHelper(this, (DrawerLayout) findViewById(R.id.drawer_layout) );
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        navDraw.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
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
        switch(id){
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
            case R.id.action_scan:
                intent = new Intent (this, ScanActivity.class);
                startActivity(intent);
                break;
            case R.id.action_search:
                // do nothing
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

    // internal classes

    private class ButtonListener implements View.OnClickListener{

        private final Context context;

        ButtonListener(Context c){
            this.context = c;
        }

        public void onClick(View v){
            Intent intent = new Intent(context, SearchActivity.class);
            startActivity(intent);
        }
    }

    private class ItemListener implements AdapterView.OnItemClickListener{

        private final Context context;

        ItemListener(Context c){
            this.context = c;
        }

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            Intent intent = new Intent (context, DetailsActivity.class);
            SiegelklarheitApplication.setCurrentSiegel(siegel_array[position]);
            startActivity(intent);
        }
    }
}
