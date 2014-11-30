package com.ibrow.de.giz.siegelklarheit;


import android.app.ActionBar;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
* Search activity.
 *
 * This is the actually the list (all and by category) activity,
 * with a text field filter.
 *
 * @author Pete
 */
public class SearchActivity extends  android.support.v4.app.FragmentActivity implements ActionBar.OnNavigationListener{

    protected IdentifeyeAPIInterface api;
    protected ShortSiegelInfo[] currentList = new ShortSiegelInfo[0];
    protected ShortSiegelInfo[] allSiegelsArray = new ShortSiegelInfo[0];
    protected ShortSiegelArrayList[] sieglsByCategory = new ShortSiegelArrayList[0];
    protected SiegelArrayAdapter adapter;
    protected ListView listview;

    protected ArrayList<String> dropdownList=new ArrayList<String>(5);


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_search);

        //.setDisplayHomeAsUpEnabled(true);

        SiegelklarheitApplication app = (SiegelklarheitApplication) getApplicationContext();
        api = app.getAPI();

        try{
            PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            api.setVersionInfo(pInfo.versionName, Build.VERSION.RELEASE);
        }
        catch (Exception e){
            Log.e("SEARCH", "Could not set api version info" + e.getMessage());
        }

        dropdownList.add(getString(R.string.category_all));

        try{
            final ActionBar ab = getActionBar();
            ArrayAdapter<String> stringArrayAdapter = new ArrayAdapter<String>( ab.getThemedContext() ,android.R.layout.simple_spinner_item ,dropdownList);
            stringArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            ab.setDisplayShowTitleEnabled(false);
            ab.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
            ab.setListNavigationCallbacks(stringArrayAdapter, this);
        }
        catch (Exception e){
            Log.e("SEARCH", "Problem getting action bar: "+e.getMessage());
        }


        listview = (ListView) findViewById(R.id.list_view);

        // need dummy data , race condition with FetchInfoTask
        allSiegelsArray = new ShortSiegelInfo[0];
        currentList = allSiegelsArray;

        (new FetchInfoTask(api, this)).execute((Void[])null);

        //adapter = new SiegelArrayAdapter(this, allSiegelsArray);

        //listview.setAdapter(adapter);

        listview.setOnItemClickListener(new ItemListener(this));
    }

    public boolean onNavigationItemSelected(int position, long itemId) {
        //Log.v("SEARCH", "Dropdown position: "+position);
        ListContentFragment newFragment = new ListContentFragment();
        FragmentTransaction ft =  getSupportFragmentManager().beginTransaction();

        // Replace whatever is in the fragment container with this fragment
        // and give the fragment a tag name equal to the string at the position
        // selected
        try {
            ft.replace(R.id.container, newFragment, dropdownList.get(position));
        }
        catch (Exception e){
            Log.e("SEARCH", e.getMessage());
        }

        // Apply changes
        ft.commit();

        if(position==0){
            currentList = allSiegelsArray;
        }
        else {
            int index = position -1;
            if(sieglsByCategory.length >= position) {
                currentList = sieglsByCategory[index].toArray();
            }
            else{
                currentList = new ShortSiegelInfo[0]; //fix race condition
            }
        }
        adapter = new SiegelArrayAdapter(this, currentList);
        listview.setAdapter(adapter);
        return true;
    }

    /* menu */

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_search, menu);
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

    /* internal classes */

    private final class FetchInfoTask extends AsyncTask<Void, Void, Void> {

        private final IdentifeyeAPIInterface api;
        private final Context context;
        List<ProductCategory> cats;

        FetchInfoTask(IdentifeyeAPIInterface api, Context context){
            this.api = api;
            this.context = context;
        }

        protected Void doInBackground(Void... params) {
            try{
                allSiegelsArray = api.getAll();
            }
            catch (Exception e){
                Log.e("SEARCH", "failed to get all: "+e.getMessage() );
                allSiegelsArray = new ShortSiegelInfo[0];

            }
            try{
                cats = api.getCategories();
                // map siegels to ids
                HashMap<Integer, ShortSiegelInfo> map=new HashMap<Integer, ShortSiegelInfo>(allSiegelsArray.length);
                for(int i=0; i<allSiegelsArray.length; i++){
                    map.put(new Integer(allSiegelsArray[i].getId()), allSiegelsArray[i]);
                }
                sieglsByCategory = new ShortSiegelArrayList[cats.size()];
                int index=-1;
                int siegel_ids_size=0;
                int siegel_ids[];
                ShortSiegelInfo ssi=null;
                for(ProductCategory pc: cats){
                    index++;
                    siegel_ids=pc.getSiegelIds();
                    siegel_ids_size = siegel_ids.length;
                    sieglsByCategory[index] = new ShortSiegelArrayList(siegel_ids_size);
                    for(int i=0; i<siegel_ids_size; i++){
                        ssi = (ShortSiegelInfo) map.get(Integer.valueOf(siegel_ids[i]));
                        if(ssi != null){
                            sieglsByCategory[index].add(ssi);
                        }
                        else {
                            Log.e("SEARCH", "Bad siegel id of "+siegel_ids[i]+" in category "+pc.getName()+", cat id "+pc.getId());
                        }
                    }
                }
            }
            catch (Exception e){
                Log.e("SEARCH", "failed to get categories: "+e.getMessage() );
                sieglsByCategory = new ShortSiegelArrayList[0];
            }

            return null;
        }

        protected void onPostExecute(Void result) {
            currentList = allSiegelsArray;
            adapter = new SiegelArrayAdapter(context, currentList);
            listview.setAdapter(adapter);
            if(allSiegelsArray.length==0) { //something went wrong
                checkOnline();
            }
            if(cats!=null){
                // add names to drop down list - do here as in gui thread
                for (ProductCategory tmp : cats) {
                    dropdownList.add(tmp.getName());
                }
            }
        }
    }

    private final class ItemListener implements AdapterView.OnItemClickListener{

        private final Context context;

        ItemListener(Context c){
            this.context = c;
        }

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            Intent intent = new Intent (context, DetailsActivity.class);
            SiegelklarheitApplication.setCurrentSiegel(currentList[position]);
            startActivity(intent);
        }
    }
}
