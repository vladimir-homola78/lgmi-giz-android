package com.ibrow.de.giz.siegelklarheit;

/**
 * A Siegel Criterium value enumeration.
 * STAR|TICK|CROSS
 *
 * @author Pete
 */
public enum CriteriaValue {

    STAR("star", 1),
    TICK("tick", 2),
    CROSS("cross", 3);

    private String name;
    private int id;

    public static final String IDENTIFIER_PREFIX="criterion__value_";
    public static final String IMAGE_IDENTIFIER_PREFIX="criterion_symbol_";

    private CriteriaValue(String name, int id){
        this.name = name;
        this.id = id;
    }

    /**
     * Gets the iternal name of criterium value
     * @return The internal name - "star", "stick", or "cross"
     */
    public String getInternalName(){
        return name;
    }

    /**
     * Gets the name identifer suitable for
     * loading an image resource
     *
     * @return criterion__symbol_ + internal name
     */
    public String getNameIdentifierForImage(){
        return IMAGE_IDENTIFIER_PREFIX+name;
    }

    /**
     * Gets a CriteriaValue based on an internal name :
     * "star", "stick", or "cross"
     *
     * Matching is case insensitive.
     *
     * @param id
     * @return The criteria value or null if name not recognised
     */
    public static final CriteriaValue getFromId(int id){
        switch (id){
            case 1: return CriteriaValue.STAR;
            case 2: return CriteriaValue.TICK;
            case 3: return CriteriaValue.CROSS;
        }
        return null;
    }
}
