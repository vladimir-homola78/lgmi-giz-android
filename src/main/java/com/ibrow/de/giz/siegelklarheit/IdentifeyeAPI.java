package com.ibrow.de.giz.siegelklarheit;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
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

    /**
     * API endpoint, including protocol and with trailing slash.
     */
    protected final String endPoint="http://www.example.net/";

    /** Base URL for logo images.
     * Without trailing slash .*/
    protected final String imageBase="http://www.example.net/img";

    protected HttpURLConnection conn;

    protected Random rnd = new Random();

    protected final static int EXPECTED_CRITERIA_NUMBER=3;

    private static final String SALT="w1hif53kEr0d4fblaEacBrak2i3s3nas8ielLadge1e86606iCaRast5e592a2smI";

    protected static final int NONCE_MIN=1;
    protected static final int NONCE_MAX=999999;

    protected static final String CRLF= "\r\n";
    protected static final String DASHDASH = "--";

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
            conn.setRequestProperty("Connection", "Keep-Alive");
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
        //Log.d("API", makeGetCall("ping") );
        makeGetCall("ping");
    }

    /**
     * Performs a MD5 hash
     *
     * @param str The string to generate a hash of.
     * @return md5 hash, hexadecimal string
     * @throws NoSuchAlgorithmException If no md5 support on device (highly unlikely)
     */
    protected static String MD5Hash(String str) throws NoSuchAlgorithmException{
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
     * @param nonce intege from 1-99999 as a string
     * @param timestamp UNIX timestamp as a string
     * @return The md5 hash of secret + nonce + timestamp
     *
     * @see #SALT
     * @see #MD5Hash(String)
     * @see #generateNonce()
     * @see #generateTimeStamp()
     */
    private final String generateHash(String nonce, String timestamp) throws NoSuchAlgorithmException{
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
        return new Integer(randomNum).toString();
    }

    /**
     * Gets the current UNIX timestamp, used for a imgage upload request.
     *
     * @return Current UNIX time (seconds since Epoch) as a string
     */
    protected final String generateTimeStamp(){
        return new Long (System.currentTimeMillis()/1000L).toString();
    }

    public List<ShortSiegelInfo> identifySiegel(final byte[] image) throws Exception{
        String nonce = generateNonce();
        String timestamp = generateTimeStamp();
        String hash = generateHash(nonce, timestamp);

        List<ShortSiegelInfo> results = new ArrayList<ShortSiegelInfo>();

        String response = uploadImage(image, nonce, timestamp, hash );
        Log.d("API", response);

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
            Log.e("API", "Missing logo attribute :"+item.toString() );
            logo = "";
        }
        else {
            logo = getImageBase() + logo;
        }

        int rating_number = item.optInt("rating");
        if(item.isNull("rating")){
            Log.e("API", "Missing rating attribute :"+item.toString() );
        }
        SiegelRating rating = SiegelRating.fromNumericId(rating_number);
        if(rating==null){
            Log.e("API", "Invalid rating value of "+rating_number+" :"+item.toString() );
            rating = SiegelRating.UNKNOWN;
        }

        JSONObject criteria = item.optJSONObject("criteria");
        List<Criterion> criteria_list;
        if( criteria!=null){
            criteria_list = parseSiegelCriteria(criteria);

        }
        else{
            criteria_list = (List) new ArrayList <Criterion>(0);
            Log.e("API", "Missing criteria attribute :"+item.toString() );
        }

        ShortSiegelInfo siegel_info = new ShortSiegelInfo(id, name, logo, rating, criteria_list);

        if(! item.isNull("c") ){ // confidence level of match (optional)
            siegel_info.setConfidenceLevel(item.getInt("c") );
        }
        else {
            Log.d("API", "No confidence level for item : "+item.toString());
        }

        return siegel_info;
    }

    /**
     * Parses the criteria part of sigel infomation.
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
                Log.e("API", "Bad criteria name of '"+key+"' : "+criteria.toString());
                continue;
            }



            value_id = criteria.optInt(key, -1);
            if( value_id == -1){
                Log.e("API", "Missing criterium value for '"+key+"' : "+criteria.toString());
                continue;
            }
            value = CriteriaValue.getFromId(value_id);
            if(value==null){
                Log.e("API", "Bad criteria value of '"+value_id+"' : "+criteria.toString());
                continue;
            }

            Criterion criterion = new Criterion(type, value);
            result.add(criterion);
        }

        return result;
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
        conn.setDoOutput(true);
        conn.setChunkedStreamingMode(0);
        conn.setRequestMethod("POST");
        conn.setUseCaches(false);
        conn.setRequestProperty("Connection", "Keep-Alive");
        conn.setRequestProperty("Cache-Control", "no-cache");
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
                Log.e("API", "403 response");
                Log.e("API", "time: "+timestamp);
                Log.e("API", "nonce: "+nonce);
                Log.e("API", "hash: "+hash);
                Log.e("API", "time now: "+generateTimeStamp());
            }
        }
        catch (Exception e){
            if(e.getMessage().equals("No authentication challenges found")){
                Log.e("API", "Bad 401 response");
                Log.e("API", "time: "+timestamp);
                Log.e("API", "nonce: "+nonce);
                Log.e("API", "hash: "+hash);
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

    public SiegelInfo getInfo(int id) throws Exception{
        assert id > 0;
        JSONObject json = new JSONObject( makeGetCall( "info/"+id) );

        String name = json.getString("name");
        assert name != "";

        String logo = json.optString("logo");
        if(logo==null || logo.equals("") ){
            Log.e("API", "Missing logo attribute :"+json.toString() );
            logo = "";
        }
        else {
            logo =getImageBase() + logo;
        }

        int rating_number = json.optInt("rating");
        if(json.isNull("rating")){
            Log.e("API", "Missing rating attribute :"+json.toString() );
        }
        SiegelRating rating = SiegelRating.fromNumericId(rating_number);
        if(rating==null){
            Log.e("API", "Invalid rating value of "+rating_number+" :"+json.toString() );
            rating = SiegelRating.UNKNOWN;
        }

        JSONObject criteria = json.optJSONObject("criteria");
        List<Criterion> criteria_list;
        if( criteria!=null){
            criteria_list = parseSiegelCriteria(criteria);
            if(criteria_list.size() != EXPECTED_CRITERIA_NUMBER ){
                Log.w("API", "Didn't get 3 criteria for siegel: "+json.toString() );
            }
        }
        else{
            criteria_list = (List) new ArrayList <Criterion>(0);
            Log.e("API", "Missing criteria attribute :"+json.toString() );
        }

        String details = json.optString("html");
        if( details==null || details.equals("") ){
            Log.w("API", "Missing or empty details html :"+json.toString());
            details = "<html><head></head><body><!-- no details --></body></html>";
        }


        SiegelInfo siegel = new SiegelInfo(id, name, logo, rating, criteria_list, details);

        return siegel;
    }
}
