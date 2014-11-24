package com.ibrow.de.giz.siegelklarheit;

import java.util.List;

/**
 * Full info of a Siegel.
 *
 * @author Pete
 */
class SiegelInfo extends ShortSiegelInfo implements Siegel{

    protected String detailsHTML="";

    public SiegelInfo(int id){
        super(id);
    }

    public SiegelInfo(int id, String name, String logo, SiegelRating rating, List<Criterion> criteria, String detailsHTML){
        super(id, name, logo, rating, criteria);
        this.detailsHTML = detailsHTML;
    }

    public String getDetails(){
        return detailsHTML;
    }
}
