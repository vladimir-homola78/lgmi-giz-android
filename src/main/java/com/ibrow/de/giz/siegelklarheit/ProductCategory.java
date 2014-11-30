package com.ibrow.de.giz.siegelklarheit;

/**
 * Represents a product category, e.g. Textile.
 *
 * @author Pete
 */
class ProductCategory {

    private final String name;
    private final int id;
    private final int[] siegelIds;


    public ProductCategory(int id, String name, int[] siegel_ids){
        this.id = id;
        this.name = name;
        this.siegelIds = siegel_ids;
    }

    public int getId(){
        return id;
    }

    public String getName(){
        return name;
    }

    /**
     * Returns the list of Sigel ids
     * that this category contains.
     *
     * @return
     * @see com.ibrow.de.giz.siegelklarheit.Siegel
     * @see com.ibrow.de.giz.siegelklarheit.IdentifeyeAPIInterface#getInfo(int)
     */
    public int[] getSiegelIds(){
        return siegelIds;
    }
}
