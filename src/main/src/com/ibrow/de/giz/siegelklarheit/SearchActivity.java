package com.ibrow.de.giz.siegelklarheit;


import android.app.ActionBar;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.res.Configuration;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

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

    protected boolean amFiltering = false;
    protected ShortSiegelInfo[] filteredList;

    protected EditText filterText;
    protected String lastFilterText="";


    protected NavDrawHelper navDraw;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_search);
        Log.e("Button clicked", "Button clicked");
        //.setDisplayHomeAsUpEnabled(true);

        SiegelklarheitApplication app = (SiegelklarheitApplication) getApplicationContext();
        api = app.getAPI();
        api.initDiskCache(this);

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

        filterText = (EditText) findViewById(R.id.filter);
        filterText.setText("");
        filterText.setOnEditorActionListener( new FilterTextListener() );
        FilterTextChangedListener ftcl= new FilterTextChangedListener();
        filterText.addTextChangedListener( ftcl );
        //filterText.setOnKeyListener( ftcl );

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
        navDraw.onConfigurationChanged(newConfig);
    }



    public boolean onNavigationItemSelected(int position, long itemId) {
        //Log.v("SEARCH", "Dropdown position: "+position);
        ListContentFragment newFragment = new ListContentFragment();
        FragmentTransaction ft =  getSupportFragmentManager().beginTransaction();

        // Replace whatever is in the fragment container with this fragment
        // and give the fragment a tag name equal to the string at the position
        // selected
        if(position > (dropdownList.size()-1) ){
            position = 0;
        }

        try {
            ft.replace(R.id.dropdown_container, newFragment, dropdownList.get(position));
        }
        catch (Exception e){
            Log.e("SEARCH", e.getMessage());
        }

        // Apply changes
        ft.commit();

        if(position<1){
            currentList = allSiegelsArray;
        }
        else {
            int index = position -1;
            if(sieglsByCategory.length >= position && sieglsByCategory[index]!=null ) {
                currentList = sieglsByCategory[index].toArray();
            }
            else{
                currentList = new ShortSiegelInfo[0]; //fix race condition
            }
        }
        adapter = new SiegelArrayAdapter(this, currentList);
        listview.setAdapter(adapter);

        // remove filter text on category change (?)
        amFiltering = false;
        filterText.setText("");

        return true;
    }

    /**
     * Filter the list by the text input.
     */
    protected void filterList(){
        Log.v("SEARCH", "filter called");
        String text= filterText.getText().toString().trim().toLowerCase();
        if(text.isEmpty()){
            amFiltering = false;
            int position = listview.getSelectedItemPosition();
            if(position<1){
                currentList = allSiegelsArray;
            }
            else {
                int index = position -1;
                if(sieglsByCategory.length >= position && sieglsByCategory[index]!=null ) {
                    currentList = sieglsByCategory[index].toArray();
                }
                else{
                    currentList = new ShortSiegelInfo[0];
                }
            }

            adapter = new SiegelArrayAdapter(this, currentList);
            listview.setAdapter(adapter);
            lastFilterText="";
            Log.v("SEARCH", "filter text empty");
            return;
        }

        amFiltering = true;
        if(lastFilterText.equals(text)){
            // nothing changed (bear in mind trim() etc.
            return;
        }


        ShortSiegelArrayList ssal = new ShortSiegelArrayList(currentList.length);
        //int text_length = text.length();
        for(int i=0; i<currentList.length; i++){
            if( currentList[i].getName().toLowerCase().contains(text) ){
                ssal.add(currentList[i]);
            }
        }
        filteredList = ssal.toArray();

        adapter = new SiegelArrayAdapter(this, filteredList);
        listview.setAdapter(adapter);
        //currentList = filteredList;

        lastFilterText = text;
        Log.v("SEARCH", "filter finsihed");
    }

    /* menu */

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_search, menu);
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
        builder.setTitle(R.string.no_connection_title);
        builder.setPositiveButton(R.string.ok_btn ,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        AlertDialog dialog = builder.show();

        int titleDividerId = getResources().getIdentifier("titleDivider", "id", "android");
        View titleDivider = dialog.findViewById(titleDividerId);
        if (titleDivider != null) titleDivider.setBackgroundColor(getResources().getColor(R.color.orange));
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
            // categories
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
            if( isCancelled() ){
                Log.e("FetchInfoTask", "task cancelled");
                return;
            }
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
        	
        	Log.e("Item Clicked", currentList[position]+"");
            Intent intent = new Intent (context, DetailsActivity.class);
            if(! amFiltering){
                SiegelklarheitApplication.setCurrentSiegel(currentList[position]);
            }
            else {
                SiegelklarheitApplication.setCurrentSiegel(filteredList[position]);
            }
            startActivity(intent);
        }
    }

    private final class FilterTextListener implements TextView.OnEditorActionListener{

        InputMethodManager imm = (InputMethodManager)getSystemService(INPUT_METHOD_SERVICE);



        public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
            boolean handled = false;
            if (actionId == EditorInfo.IME_ACTION_SEARCH || actionId==EditorInfo.IME_ACTION_SEND) {
                filterList();
                imm.hideSoftInputFromWindow(filterText.getWindowToken(), 0);
                handled = true;
            }
            return handled;
        }

    }

    private final class  FilterTextChangedListener implements TextWatcher, View.OnKeyListener{

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
           //NOP
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count,
                                      int after) {
            //NOP
        }

        @Override
        public void afterTextChanged(Editable s) {
            if(amFiltering) {
                Log.v("SEARCH", "textchanged event");
                filterList();
            }
            else{
                if(! filterText.getText().toString().trim().isEmpty()){
                    filterList();
                }
            }
        }

        @Override
        public boolean onKey (View v, int keyCode, KeyEvent event){
            Log.v("SEARCH", "key press");
            if( event.getNumber()!=0){
                filterList();
            }
            return false;
        }
    }
}
