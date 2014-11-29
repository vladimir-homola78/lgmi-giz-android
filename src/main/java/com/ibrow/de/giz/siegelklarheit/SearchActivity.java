package com.ibrow.de.giz.siegelklarheit;


import android.app.Activity;
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
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;


/**
* Search activity
 */
public class SearchActivity extends Activity {

    protected IdentifeyeAPIInterface api;
    protected ShortSiegelInfo[] siegel_array;
    protected SiegelArrayAdapter adapter;
    protected ListView listview;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_search);

        //getActionBar().setDisplayHomeAsUpEnabled(true);

        SiegelklarheitApplication app = (SiegelklarheitApplication) getApplicationContext();
        api = app.getAPI();

        try{
            PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            api.setVersionInfo(pInfo.versionName, Build.VERSION.RELEASE);
        }
        catch (Exception e){
            Log.e("SEARCH", "Could not set api version info" + e.getMessage());
        }

        listview = (ListView) findViewById(R.id.list_view);

        (new FetchAllTask(api, this)).execute((Void[])null);

        //adapter = new SiegelArrayAdapter(this, siegel_array);

        //listview.setAdapter(adapter);

        listview.setOnItemClickListener(new ItemListener(this));
    }

    /* menu */

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
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

    private final class FetchAllTask extends AsyncTask<Void, Void, Void> {

        private final IdentifeyeAPIInterface api;
        private final Context context;

        FetchAllTask(IdentifeyeAPIInterface api, Context context){
            this.api = api;
            this.context = context;
        }

        protected Void doInBackground(Void... params) {
            try{
                siegel_array = api.getAll();
            }
            catch (Exception e){
                Log.e("SEARCH", "failed to get all: "+e.getMessage() );
                siegel_array = new ShortSiegelInfo[0];
            }
            return null;
        }

        protected void onPostExecute(Void result) {
            adapter = new SiegelArrayAdapter(context, siegel_array);
            listview.setAdapter(adapter);
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
