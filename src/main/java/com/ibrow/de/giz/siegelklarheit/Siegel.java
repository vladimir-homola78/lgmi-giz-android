package com.ibrow.de.giz.siegelklarheit;

import java.util.List;

/**
 * Basic Siegel Interface
 */
interface Siegel {

    public int getId();

    public String getName();

    public String getLogoURL();

    public SiegelRating getRating();

    public List<Criterion> getCriteria();

}
