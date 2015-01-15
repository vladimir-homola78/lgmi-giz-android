package com.ibrow.de.giz.siegelklarheit;

import java.util.List;

/**
 * Short information about a Siegel.
 *
 * @see com.ibrow.de.giz.siegelklarheit.SiegelRating
 * @author Pete
 */
class ShortSiegelInfo implements Siegel{

    protected int id;

    protected String name="";
    protected String logoURL="";
    protected SiegelRating rating=SiegelRating.UNKNOWN;
    protected List<Criterion> criteria;
    protected int confidence;


   public ShortSiegelInfo(int id){
       assert id > 0;
       this.id=id;
   }

   public ShortSiegelInfo(int id, String name, String logo_url, SiegelRating rating, List<Criterion> criteria){
       this(id);
       this.name = name;
       this.logoURL = logo_url;
       this.rating = rating;
       this.criteria = criteria;
   }

   public int getId(){
       return id;
   }

   public String getName(){
       return name;
   }

   public String getLogoURL(){
       return logoURL;
   }

   public List<Criterion> getCriteria(){
       return criteria;
   }

    /**
     * Sets the optional confidence level when we're performing a match on a scanned image.
     *
     * @param confidence
     * @see com.ibrow.de.giz.siegelklarheit.TestIdentifeyeAPI#identifySiegel(byte[])
     * @see com.ibrow.de.giz.siegelklarheit.IdentifeyeAPI#identifySiegel(byte[])
     * @see com.ibrow.de.giz.siegelklarheit.ScanActivity#ProcessScanResult(List<ShortSiegelInfo>)
     */
   protected void setConfidenceLevel(int confidence){
       this.confidence = confidence;
   }

    /**
     * Get optional confidence level
     *
     * @return the confidence level (higher is better) or null if not set
     * @see #setConfidenceLevel(int)
     */
    public int getConfidenceLevel(){
        return confidence;
   }



    public SiegelRating getRating(){
       return rating;
   }
}
