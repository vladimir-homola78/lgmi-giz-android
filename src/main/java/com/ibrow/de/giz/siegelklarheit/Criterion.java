package com.ibrow.de.giz.siegelklarheit;

/**
 * Reresents a criterion for a Siegel.
 *
 * Each criterion has a type (Social, environment, etc.)
 * and a value (star, tick, cross).
 *
 * @see com.ibrow.de.giz.siegelklarheit.CriteriaType
 * @see com.ibrow.de.giz.siegelklarheit.CriteriaValue
 * @see com.ibrow.de.giz.siegelklarheit.ShortSiegelInfo
 */
class Criterion {

    private CriteriaType type;
    private CriteriaValue value;

    public Criterion(CriteriaType type, CriteriaValue value){
        assert type != null;
        assert value!=null;
        this.type = type;
        this.value = value;
    }

    public CriteriaType getType(){
        return type;
    }

    public CriteriaValue getValue(){
        return value;
    }
}
