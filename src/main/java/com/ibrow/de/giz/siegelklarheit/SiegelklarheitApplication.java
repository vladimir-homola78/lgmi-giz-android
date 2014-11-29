package com.ibrow.de.giz.siegelklarheit;

import android.app.Application;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;

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
        /* can't seem to get package manager here (null), setting info in ScanActivity
        try{
            PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            identifyAPI.setVersionInfo(pInfo.versionName, Build.VERSION.RELEASE);
        }
        catch (Exception e){
            Log.e("APPLICATION", "Could not set api version info" + e.getMessage());
        }
        */
    }

    protected final IdentifeyeAPIInterface getAPI() {
        return identifyAPI;
    }

    public static void setCurrentSiegel(ShortSiegelInfo siegel) {
        assert siegel != null;
        LastMatch = siegel;
    }

    public static ShortSiegelInfo getCurrentSiegel() {
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
