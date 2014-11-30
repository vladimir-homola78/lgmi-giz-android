package com.ibrow.de.giz.siegelklarheit;

import android.graphics.Color;

/**
 * A Siegel's overall rating.
 *
 * @author Pete
 */
enum SiegelRating {

    UNKNOWN("#ffffffff", "unknown"),
    VERY_GOOD("#115746", "verygood"),
    GOOD("#4b9f53", "good"),
    BAD("#cd3333", "bad"),
    NONE("#666666", "none");

    private int color;
    private String name;

    private static final String IMAGE_IDENTIFIER_PREFIX="rating_symbol_";
    private static final String DESCRIPTION_IDENTIFIER_PREFIX="rating_";


    private SiegelRating(String color_hex_string, String name){
        this.color = Color.parseColor(color_hex_string);
        this.name = name;
    }

    /**
     * Returns the color for this rating.
     *
     * @return color as an integer code
     */
    public int getColor(){
        return color;
    }

    /**
     * Returns the filename of the smybol image for this rating,
     * to be used for loading the image as a drawable resource.
     *
     * @return The name of the smybol file for this rating, without the filename prefix
     */
    public String getImageIdentifier(){
        return IMAGE_IDENTIFIER_PREFIX+name;
    }

    public String getDescriptionIdentifier(){
        return DESCRIPTION_IDENTIFIER_PREFIX+name;
    }

    /**
     * Returns a Sieglrating based on a numeric id (-1,0,1,2,3).
     *
     * @param id
     * @return The rating or null if unknown id
     */
    public static final SiegelRating fromNumericId(int id){
        switch (id){
            case -1 : return SiegelRating.UNKNOWN;
            case 0 : return SiegelRating.NONE;
            case 1 : return SiegelRating.BAD;
            case 2 : return SiegelRating.GOOD;
            case 3 : return SiegelRating.VERY_GOOD;
        }
        return null;
    }

}
