package com.ibrow.de.giz.siegelklarheit;

import android.app.Application;

import java.util.List;

/**
 * Sub class of Application to provide static access to the API across activities.
 *
 * @author Pete
 */
public class SiegelklarheitApplication extends Application {

    private static IdentifeyeAPIInterface identifyAPI;

    /**
     * Last match from ScanActivity
     */
    private static ShortSiegelInfo LastMatch;

    /**
     * Last Multiple matches from ScanActivity.
     */
    private static List<ShortSiegelInfo> LastMultipleMatches;

    public SiegelklarheitApplication() {
        super();
        identifyAPI = new TestIdentifeyeAPI();
    }

    protected final IdentifeyeAPIInterface getAPI() {
        return identifyAPI;
    }

    public static void setLastMatch(ShortSiegelInfo siegel) {
        assert siegel != null;
        LastMatch = siegel;
    }

    public static ShortSiegelInfo getLastMatch() {
        return LastMatch;
    }

    public static void setLastMultipleMatches(List<ShortSiegelInfo> matches) {
        assert matches != null;
        LastMultipleMatches = matches;
    }

    public static List<ShortSiegelInfo> getLastMultipleMatches() {
        return LastMultipleMatches;
    }
}
