package com.ibrow.de.giz.siegelklarheit;

/**
 * A Siegel Criterium Type enumeration.
 *
 * @author Pete
 */
public enum CriteriaType {

    SYSTEM("system"),
    SOCIALECONOMIC("socioeconomic"),
    ENVIRONMENT("environment");

    private String name;

    public static final String IDENTIFIER_PREFIX="criterion__type_";

    private CriteriaType(String name){

    }

    /**
     * Gets the iternal name of criterium type
     * @return The internal name - "system", "socioeconomic", or "environment"
     */
    public String getInternalName(){
        return name;
    }

    /**
     * Gets the name identifer suitable for
     * translating from strings.xml
     *
     * @return criterion__type_ + internal name
     */
    public String getNameIdentifier(){
        return IDENTIFIER_PREFIX+name;
    }

    /**
     * Gets a CriteriaType based on an internal name :
     *  "system", "socioeconomic", or "environment".
     *
     *  Matching is case insensitive.
     *
     * @param name
     * @return The criteria type or null if name not recognised
     */
    public static final CriteriaType getFromName(String name){
        assert name != null;
        String name_lc = name.toLowerCase();

        if(name_lc.equals("system") ){
            return CriteriaType.SYSTEM;
        }
        if(name_lc.equals("socioeconomic") ){
            return CriteriaType.SOCIALECONOMIC;
        }
        if(name_lc.equals("environment") ){
            return CriteriaType.ENVIRONMENT;
        }
        return null;
    }
}
