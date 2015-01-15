package com.ibrow.de.giz.siegelklarheit;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;


/**
 * Concrete Implementation of the identifeye API.
 *
 * @author Pete
 *
 * @see com.ibrow.de.giz.siegelklarheit.IdentifeyeAPIInterface
 * @see <a href="https://bitbucket.org/ibrow/lgmi-giz-api/wiki/Home">API doc</a>
 */
class IdentifeyeAPI implements IdentifeyeAPIInterface {

    protected final static String LOG_TAG="API";
    /**
     * API endpoint, including protocol and with trailing slash.
     */
    protected final String endPoint="http://www.example.net/";

    /** Base URL for logo images.
     * Without trailing slash .*/
    protected final String imageBase="http://www.example.net/img";

    protected HttpURLConnection conn;

    protected Random rnd = new Random();



    private static final String SALT="w1hif53kEr0d4fblaEacBrak2i3s3nas8ielLadge1e86606iCaRast5e592a2smI";

    protected static final int NONCE_MIN=1;
    protected static final int NONCE_MAX=999999;

    protected static final String CRLF= "\r\n";
    protected static final String DASHDASH = "--";

    protected static String userAgent="Siegelklarheit (Android)";

    protected static ShortSiegelInfo[] allSiegels = null;
    protected static ArrayList<ProductCategory> categories = null;

    protected static MaxSizeHashMap<Integer, SiegelInfo> SiegelInfoCache = new MaxSizeHashMap<Integer, SiegelInfo>(3, 7);

    private static boolean DiskCachePathInitialised=false;
    private static File DiskCachePath;

    @Override
    public void setVersionInfo(final String app_version, final String android_version){
        userAgent="Siegelklarheit/"+app_version+" (Android; Android "+android_version+")";
    }

    /**
     * Returns the API endpoint.
     * @return
     */
    protected String getEndPoint(){
        return endPoint;
    }

    /**
     * Returns the base url for siegel logo images.
     * @return
     */
    protected String getImageBase(){
        return imageBase;
    }

    public String getWebviewBaseURL(){
        return endPoint;
    }

    protected void setConnectionDefaults(){
        conn.setConnectTimeout(10000); //10 seconds
        conn.setReadTimeout(30000); // 30 seconds
        conn.setRequestProperty("Connection", "Keep-Alive");
        conn.setRequestProperty("User-Agent", userAgent);
        conn.setRequestProperty("Cache-Control", "no-cache");
    }

    /**
     * Performs a API request, GET method.
     *
     * @param method The API method name (e.g. ping, info/n, etc.)
     * @return The response from the server, usually JSON text.
     * @throws Exception
     *
     * @see #getEndPoint()
     */
    protected String makeGetCall(final String method) throws Exception{
        URL url = new URL(getEndPoint() + method);
        StringBuffer sb = new StringBuffer();
        try {
            conn = (HttpURLConnection) url.openConnection();
            setConnectionDefaults();
            conn.setRequestMethod("GET");
            conn.connect();
            if (conn.getResponseCode() != 200) {
                throw new Exception("Error, server response: " + conn.getResponseCode());
            }
            InputStreamReader is = new InputStreamReader(conn.getInputStream(), "UTF-8" );
            BufferedReader br = new BufferedReader(is);
            String line;
            while( (line=br.readLine() ) != null ){
                sb.append(line);
            }
            br.close();
            is.close();
        }
        catch (Exception e){
            throw e;
        }
        finally {
            conn.disconnect();
        }

        return sb.toString();
    }


    public void ping() throws Exception{
        //Log.d(LOG_TAG, makeGetCall("ping") );
        makeGetCall("ping");
    }

    /**
     * Performs a MD5 hash
     *
     * @param str The string to generate a hash of.
     * @return md5 hash, hexadecimal string
     * @throws NoSuchAlgorithmException If no md5 support on device (highly unlikely)
     */
    protected static String MD5Hash(final String str) throws NoSuchAlgorithmException{
        MessageDigest m =  MessageDigest.getInstance("MD5");
        m.update(str.getBytes(), 0, str.length() );
        String hash = new BigInteger(1, m.digest()).toString(16);
        while(hash.length()!=32) { //re-add trimmed leading zeros otherwise it won't match
            hash = "0" + hash;
        }
        return hash;
    }


    /**
     * md5(s.n.t)
     *
     * @param nonce integer from 1-99999 as a string
     * @param timestamp UNIX timestamp as a string
     * @return The md5 hash of secret + nonce + timestamp
     *
     * @see #SALT
     * @see #MD5Hash(String)
     * @see #generateNonce()
     * @see #generateTimeStamp()
     */
    private final String generateHash(final String nonce, final String timestamp) throws NoSuchAlgorithmException{
        return MD5Hash(SALT+nonce+timestamp);
    }

    /**
     * Gernerates a nonce used for the upload image request.
     *
     * @return A random nonce in the range NONCE_MIN - NONCE_MAX as string
     * @see #NONCE_MIN
     * @see #NONCE_MAX
     */
    protected final String generateNonce(){
        int randomNum = rnd.nextInt((NONCE_MAX - NONCE_MIN) + 1) + NONCE_MIN;
        return Integer.toString(randomNum);
    }

    /**
     * Gets the current UNIX timestamp, used for a image upload request.
     *
     * @return Current UNIX time (seconds since Epoch) as a string
     */
    protected final String generateTimeStamp(){
        return Long.toString(System.currentTimeMillis()/1000L);
    }

    public List<ShortSiegelInfo> identifySiegel(final byte[] image) throws Exception{
        String nonce = generateNonce();
        String timestamp = generateTimeStamp();
        String hash = generateHash(nonce, timestamp);

        List<ShortSiegelInfo> results = new ShortSiegelArrayList(3);

        String response = uploadImage(image, nonce, timestamp, hash );
        Log.d(LOG_TAG, response);

        JSONArray json = new JSONArray( response );
        int results_size = json.length();
        if(results_size == 0){
            // no results, we can stop here and return the empty list
            return results;
        }

        for(int i=0; i<results_size; i++){
            JSONObject item = json.getJSONObject(i);
            results.add( parseSiegel(item) );
        }

        return results;
    }

    /**
     * Parses a JSON object into a short siegel description.
     *
     * Called by identifySiegel()
     *
     * @param item
     * @return A ShortSiegelInfo
     * @throws JSONException If problem parsing, missing essential attributes, etc.
     *
     * @see #identifySiegel(byte[])
     */
    protected  final ShortSiegelInfo parseSiegel(final JSONObject item) throws JSONException{
        int id = item.getInt("id");
        String name = item.getString("name");

        String logo = item.optString("logo");
        if(logo== null || logo.equals("") ){
            Log.e(LOG_TAG, "Missing logo attribute :"+item.toString() );
            logo = "";
        }
        else {
            logo = getImageBase() + logo;
        }

        int rating_number = item.optInt("rating");
        if(item.isNull("rating")){
            Log.e(LOG_TAG, "Missing rating attribute :"+item.toString() );
        }
        SiegelRating rating = SiegelRating.fromNumericId(rating_number);
        if(rating==null){
            Log.e(LOG_TAG, "Invalid rating value of "+rating_number+" :"+item.toString() );
            rating = SiegelRating.UNKNOWN;
        }

        JSONObject criteria = item.optJSONObject("criteria");
        List<Criterion> criteria_list;
        if( criteria!=null){
            criteria_list = parseSiegelCriteria(criteria);

        }
        else{
            criteria_list = (List) new ArrayList <Criterion>(0);
            if(rating==SiegelRating.UNKNOWN || rating==SiegelRating.NONE){
                Log.v(LOG_TAG, "No criteria for siegel with rating unknown/none");
            }
            else {
                Log.e(LOG_TAG, "Missing criteria attribute :" + item.toString());
            }
        }

        ShortSiegelInfo siegel_info = new ShortSiegelInfo(id, name, logo, rating, criteria_list);

        if(! item.isNull("c") ){ // confidence level of match (optional)
            siegel_info.setConfidenceLevel(item.getInt("c") );
        }
        else {
            Log.v(LOG_TAG, "No confidence level for item : "+item.toString());
        }

        return siegel_info;
    }

    /**
     * Parses the criteria part of Siegel information.
     *
     * Glaubwürdigkeit, Umweltfreundlichkeit, Sozialverträglichkeit.
     *
     * @param criteria The 'criteria' part of a JSON sigel object.
     * @return List of criterion, maybe empty, though we expect all 3 to be present.
     * @throws JSONException If problem parsing
     *
     * @see com.ibrow.de.giz.siegelklarheit.Criterion
     */
    protected final List<Criterion> parseSiegelCriteria(final JSONObject criteria) throws JSONException{
        ArrayList<Criterion> result = new ArrayList<Criterion>(3);

        Iterator<String> keys = criteria.keys();
        String key;
        CriteriaType type;
        CriteriaValue value;
        int value_id;

        while(keys.hasNext() ){
            key=keys.next();
            type=CriteriaType.getFromName(key);
            if(type==null){
                Log.e(LOG_TAG, "Bad criteria name of '"+key+"' : "+criteria.toString());
                continue;
            }



            value_id = criteria.optInt(key, -1);
            if( value_id == -1){
                Log.e(LOG_TAG, "Missing criterium value for '"+key+"' : "+criteria.toString());
                continue;
            }
            value = CriteriaValue.getFromId(value_id);
            if(value==null){
                Log.e(LOG_TAG, "Bad criteria value of '"+value_id+"' : "+criteria.toString());
                continue;
            }


            Criterion criterion = new Criterion(type, value);
            result.add(criterion);
        }

        sortCriteria(result);
        return result;
    }

    /**
     * Temp. workaround to sort the criteria list.
     *
     * Must be: Glaubwürdigkeit Umweltfreundlichkeit Sozialverträglichkeit
     *
     * @param criteria
     * @see #parseSiegelCriteria
     */
    private void sortCriteria(ArrayList<Criterion> criteria){
        if (criteria.size() < 2) {
            return; // only 1 (or no) criterion, nothing to sort
        }
        // ensure SYSTEM ( Glaubwürdigkeit) is first
        if(criteria.get(0).getType()!=CriteriaType.SYSTEM) {




            int system_index = -1;
            for (int i = 1; i < criteria.size(); i++) {
                if (criteria.get(i).getType() == CriteriaType.SYSTEM) {
                    system_index = i;
                    break;
                }
            }

            if (system_index != -1) {
                Criterion tmp = criteria.get(0);
                criteria.set(0, criteria.get(system_index));
                criteria.set(system_index, tmp);
            }

        }

        // ensure ENVIRONMENT ( Umweltfreundlichkeit ) is 2nd
        if(criteria.get(1).getType()!=CriteriaType.ENVIRONMENT && criteria.size() > 2) {

            int environment_index = -1;
            for (int i = 2; i < criteria.size(); i++) {
                if (criteria.get(i).getType() == CriteriaType.ENVIRONMENT) {
                    environment_index = i;
                    break;
                }
            }

            if (environment_index != -1) {
                Criterion tmp = criteria.get(1);
                criteria.set(1, criteria.get(environment_index));
                criteria.set(environment_index, tmp);
            }

        }
    }

    /**
     * UPloads a scanned imaged and returns the response from the server.
     *
     * Called by identifySiegel()
     *
     * @param image The canned image
     * @param nonce THe nonce to use for the request
     * @param timestamp current UNIX timestamp
     * @param hash The md5 hash used as a security measure
     * @return The response from the server - JSON.
     * @throws Exception
     *
     * @see #generateNonce()
     * @see #generateTimeStamp()
     * @see #generateHash(String, String)
     * @see #identifySiegel(byte[])
     */
    protected String uploadImage(final byte[] image, final String nonce, final String timestamp, final String hash) throws Exception{
        URL url = new URL(getEndPoint() + "upload/img");
        String boundary="-----------------------------"+generateTimeStamp();
        conn = (HttpURLConnection) url.openConnection();
        setConnectionDefaults();
        conn.setDoOutput(true);
        conn.setChunkedStreamingMode(0); // zero sets chunk length to use default
        conn.setRequestMethod("POST");
        conn.setUseCaches(false);
        conn.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);



        DataOutputStream request = new DataOutputStream(conn.getOutputStream());

        request.writeBytes(DASHDASH + boundary + CRLF );
        request.writeBytes("Content-Disposition: form-data; name=\"" + "img" + "\";filename=\"" + "scan.jpeg" + "\"" + CRLF);
        request.writeBytes("Content-Type: image/jpeg" + CRLF);
        request.writeBytes("Content-Length: " + image.length + CRLF);
        request.writeBytes(CRLF);
        request.write(image);
        request.writeBytes(CRLF);

        request.writeBytes(DASHDASH + boundary + CRLF);
        request.writeBytes("Content-Disposition: form-data; name=\"n\"" + CRLF +CRLF + nonce +CRLF);

        request.writeBytes(DASHDASH + boundary + CRLF);
        request.writeBytes("Content-Disposition: form-data; name=\"t\"" + CRLF +CRLF + timestamp +CRLF);

        request.writeBytes(DASHDASH + boundary + CRLF);
        request.writeBytes("Content-Disposition: form-data; name=\"h\"" + CRLF +CRLF + hash +CRLF);


        request.writeBytes(DASHDASH + boundary + DASHDASH + CRLF);
        request.flush();
        request.close();

        int response_code=0;
        try{
            response_code=conn.getResponseCode();
            if( response_code == 403 ){
                Log.e(LOG_TAG, "403 response");
                Log.e(LOG_TAG, "time: "+timestamp);
                Log.e(LOG_TAG, "nonce: "+nonce);
                Log.e(LOG_TAG, "hash: "+hash);
                Log.e(LOG_TAG, "time now: "+generateTimeStamp());
            }
        }
        catch (Exception e){
            if(e.getMessage().equals("No authentication challenges found")){
                Log.e(LOG_TAG, "Bad 401 response");
                Log.e(LOG_TAG, "time: "+timestamp);
                Log.e(LOG_TAG, "nonce: "+nonce);
                Log.e(LOG_TAG, "hash: "+hash);
            }
        }
        if (response_code != 200) {
            throw new Exception("Error, server response: " + conn.getResponseCode());
        }

        InputStream responseStream = new BufferedInputStream(conn.getInputStream());

        BufferedReader responseStreamReader = new BufferedReader(new InputStreamReader(responseStream));
        String line = "";
        StringBuilder stringBuilder = new StringBuilder();
        while ((line = responseStreamReader.readLine()) != null) {
            stringBuilder.append(line).append("\n");
        }

        responseStreamReader.close();
        responseStream.close();
        conn.disconnect();

        return stringBuilder.toString();
    }

    public SiegelInfo getInfo(final int id) throws Exception{
        assert id > 0;

        SiegelInfo siegel;

        siegel = SiegelInfoCache.get(Integer.valueOf(id));
        if(siegel != null){
            Log.v(LOG_TAG, "getInfo memory cache hit");
            return siegel;
        }

        JSONObject json;

        if(DiskCachePathInitialised) {
            File cache_file = new File(DiskCachePath, id + ".dat");
            if (cache_file.exists()) {
                try {
                    json = new JSONObject(getStringFromFile(cache_file));
                } catch (Exception e) {
                    Log.e(LOG_TAG, "error read cache file: " + e.getMessage());
                    // try to get from API instead
                    json = new JSONObject(makeGetCall("info/" + id));
                }

                Log.v(LOG_TAG, "getInfo disk cache hit");
            } else { // make API call
                String json_string = makeGetCall("info/" + id);
                json = new JSONObject(json_string);

                // write to file cache
                PrintWriter pw = new PrintWriter(cache_file);
                try {
                    pw.println(json_string);
                } catch (Exception e) {
                    Log.e(LOG_TAG, "Problem writing cache file " + e.getMessage());
                } finally {
                    pw.close();
                }
            }
        }
        else {
            json = new JSONObject(makeGetCall("info/" + id));
        }

        String name = json.getString("name");
        assert name != "";

        String logo = json.optString("logo");
        if(logo==null || logo.equals("") ){
            Log.e(LOG_TAG, "Missing logo attribute :"+json.toString() );
            logo = "";
        }
        else {
            logo =getImageBase() + logo;
        }

        int rating_number = json.optInt("rating");
        if(json.isNull("rating")){
            Log.e(LOG_TAG, "Missing rating attribute :"+json.toString() );
        }
        SiegelRating rating = SiegelRating.fromNumericId(rating_number);
        if(rating==null){
            Log.e(LOG_TAG, "Invalid rating value of "+rating_number+" :"+json.toString() );
            rating = SiegelRating.UNKNOWN;
        }

        JSONObject criteria = json.optJSONObject("criteria");
        List<Criterion> criteria_list;
        if( criteria!=null){
            criteria_list = parseSiegelCriteria(criteria);
            if(criteria_list.size() != EXPECTED_CRITERIA_NUMBER ){
                if(rating==SiegelRating.UNKNOWN || rating==SiegelRating.NONE){
                    Log.v(LOG_TAG, criteria_list.size()+" criteria for siegel with rating unknown/none");
                }
                else {
                    Log.w(LOG_TAG, "Didn't get 3 criteria for siegel: "+json.toString() );
                }
            }
        }
        else{
            criteria_list = (List) new ArrayList<Criterion>(0);
            if(rating==SiegelRating.UNKNOWN || rating==SiegelRating.NONE){
                Log.v(LOG_TAG, "No criteria for siegel with rating unknown/none");
            }
            else {
                Log.e(LOG_TAG, "Missing criteria attribute :" + json.toString());
            }
        }

        String details = json.optString("html");
        if( details==null || details.equals("") ){
            Log.w(LOG_TAG, "Missing or empty details html :"+json.toString());
            details = "<html><head></head><body><!-- no details --></body></html>";
        }

        String url = json.optString("shared_url");
        if( url==null || url.isEmpty() ){
            Log.w(LOG_TAG, "Missing or empty share url :"+json.toString());
        }

        siegel = new SiegelInfo(id, name, logo, rating, criteria_list, details, url);

        SiegelInfoCache.put(Integer.valueOf(id), siegel);

        return siegel;
    }

    public ShortSiegelInfo[] getAll() throws Exception{
        if(allSiegels != null) {
            return allSiegels;
        }

        JSONArray json;

        if(DiskCachePathInitialised){
            File cache_file = new File(DiskCachePath, "list.dat");
            if (cache_file.exists()) {
                try {
                    json = new JSONArray(getStringFromFile(cache_file));
                } catch (Exception e) {
                    Log.e(LOG_TAG, "error read cache file: " + e.getMessage());
                    // try to get from API instead
                    json = new JSONArray(makeGetCall("info"));
                }
            } else { // make API call
                String json_string = makeGetCall("info");
                json = new JSONArray(json_string);

                // write to file cache
                PrintWriter pw = new PrintWriter(cache_file);
                try {
                    pw.println(json_string);
                } catch (Exception e) {
                    Log.e(LOG_TAG, "Problem writing cache file " + e.getMessage());
                } finally {
                    pw.close();
                }
            }
        }
        else {
            // fetch from server
            json = new JSONArray(makeGetCall("info"));
        }

        int results_size = json.length();
        allSiegels = new ShortSiegelInfo[results_size];

        for(int i=0; i<results_size; i++){
            JSONObject item = json.getJSONObject(i);
            allSiegels[i] = parseSiegel(item);
        }

        return allSiegels;
    }


    public List<ProductCategory> getCategories() throws Exception{
        if(categories != null){
            return categories;
        }

        JSONArray json;

        if(DiskCachePathInitialised){
            File cache_file = new File(DiskCachePath, "categories.dat");
            if (cache_file.exists()) {
                try {
                    json = new JSONArray(getStringFromFile(cache_file));
                } catch (Exception e) {
                    Log.e(LOG_TAG, "error read cache file: " + e.getMessage());
                    // try to get from API instead
                    json = new JSONArray(makeGetCall("product_category"));
                }
            } else { // make API call
                String json_string = makeGetCall("product_category");
                json = new JSONArray(json_string);

                // write to file cache
                PrintWriter pw = new PrintWriter(cache_file);
                try {
                    pw.println(json_string);
                } catch (Exception e) {
                    Log.e(LOG_TAG, "Problem writing cache file " + e.getMessage());
                } finally {
                    pw.close();
                }
            }
        }
        else {
            json = new JSONArray(makeGetCall("product_category"));
        }

        int results_size = json.length();

        categories = new ArrayList<ProductCategory>(results_size);

        int id;
        String name;
        ProductCategory product_category;
        int[] siegl_ids;

        JSONArray id_array;
        int id_array_size;


        for(int i=0; i<results_size; i++) {
            JSONObject item = json.getJSONObject(i);
            id=item.getInt("id");
            name=item.getString("name");
            id_array = item.getJSONArray("standards");
            id_array_size = id_array.length();
            siegl_ids = new int[id_array_size];
            for(int j=0; j<id_array_size; j++){
                siegl_ids[j] = id_array.getInt(j);
            }
            product_category = new ProductCategory(id, name, siegl_ids);
            categories.add(product_category);
        }

        sortCategories(categories);


        return categories;
    }

    /**
     * Temp. workaround to sort product categories.
     *
     *  Textilien Lebensmittel Papier Holz
     *  @param categories
     *
     *  @see #getCategories()
     */
    private void sortCategories(ArrayList<ProductCategory> categories){
        if(categories.size()==1){
            return;
        }

        sortCategory(categories, "Textilien", 0);
        sortCategory(categories, "Lebensmittel", 1);
        sortCategory(categories, "Papier", 2);
        sortCategory(categories, "Holz", 3);
    }

    /**
     * ensure category with *name* is at *desired_index*.
     *
     * @param categories
     * @param name
     * @param desired_index
     * @see #sortCategories(java.util.ArrayList)
     */
    private void sortCategory(ArrayList<ProductCategory> categories, final String name, int desired_index){
        if(desired_index > (categories.size()-1) ){
            return;
        }

        ProductCategory tmp = categories.get(desired_index);
        if(tmp.getName()==name){
            return; //already there
        }
        int index = -1;
        for(int i=0; i< categories.size(); i++){
            if(categories.get(i).getName().equals(name) ){
                index = i;
                break;
            }
        }

        if(index!=-1){
            categories.set(desired_index, categories.get(index) );
            categories.set(index, tmp);
        }
    }

    /**
     * Initialsises the disk cache location.
     *
     * @param context
     */
    public void initDiskCache(final Context context){
        if(DiskCachePathInitialised) { // already done
            return;
        }

        String cachePath = Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState()) || !Environment.isExternalStorageRemovable() ?
                context.getExternalCacheDir().getPath() : context.getCacheDir().getPath();

        DiskCachePath =  new File(cachePath + File.separator + "data");
        if(! DiskCachePath.exists() ){
            try{
                if( DiskCachePath.mkdir() ){
                    DiskCachePathInitialised = true;
                }
            }
            catch (Exception e){
                Log.e(LOG_TAG, "Error creating cache path: "+e.getMessage());
            }
        }
        else {
            DiskCachePathInitialised = true;
        }
    }

    protected static String convertStreamToString(InputStream is) throws Exception {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();
        String line = null;
        while ((line = reader.readLine()) != null) {
            sb.append(line).append("\n");
        }
        reader.close();
        return sb.toString();
    }

    protected static String getStringFromFile(File filePath) throws Exception {
        FileInputStream fin = new FileInputStream(filePath);
        String ret = convertStreamToString(fin);
        //Make sure you close all streams.
        fin.close();
        return ret;
    }

    // intern classes

    private static class MaxSizeHashMap<K, V> extends LinkedHashMap<K, V> {

        private final int maxSize;

        public MaxSizeHashMap(int initialCapacity, int maxSize) {
            super(initialCapacity);
            this.maxSize = maxSize;
        }

        @Override
        protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
            return size() > maxSize;
        }
    }
}
