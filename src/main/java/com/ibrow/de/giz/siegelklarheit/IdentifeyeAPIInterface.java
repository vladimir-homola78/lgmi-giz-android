package com.ibrow.de.giz.siegelklarheit;

import java.util.List;

/**
 * Interface to the image recognition API.
 *
 * Bear in mind that the methods will block,
 * and should NOT be called inside the GUI thread -
 * fork and use callbacks.
 *
 * @see <a href="https://bitbucket.org/ibrow/lgmi-giz-api/wiki/Home">API doc</a>
 * @see android.os.AsyncTask
 * @author Pete
 *
 */
public interface IdentifeyeAPIInterface {

    /**
     * How many Siegel are the database.
     * Used for caclulating cache sizes.
     */
    public static final int MAX_ENTRIES = 160;

    public final static int EXPECTED_CRITERIA_NUMBER=3;

    /**
     * Pings the API.
     * Used to prime the HTTP connection.
     * @throws Exception
     */
    public void ping() throws Exception;


    /**
     * Fetches detailed information about a siegel
     *
     * @param id
     * @return
     * @throws Exception
     */
    public SiegelInfo getInfo(int id) throws Exception;

    /**
     * Matches an image to Seigel
     *
     * @param image JPEG image
     * @return List of matches. Maybe empty (i.e. no matches), contain 1 or more matches.
     * @throws Exception
     */
    public List<ShortSiegelInfo> identifySiegel(byte[] image) throws Exception;

    /**
     * Get the base URL for webviews.
     *
     * @return
     */
    public String getWebviewBaseURL();

    /**
     * Sets the system version infomation, used for user-agent strings
     *
     * @param app_version From versionName in manifest
     * @param android_version The android version, e.g. 4.1.3
     */
    public void setVersionInfo(String app_version, String android_version);

}
