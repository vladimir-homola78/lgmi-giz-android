package com.ibrow.de.giz.siegelklarheit;

/**
 * Item in the navigation draw.
 *
 * @author Pete
 */
public class NavDrawItem {

    private final String name;
    private final int icon;

    /**
     *
     * @param name Translated label text
     * @param icon drwabale resource id
     */
    NavDrawItem(String name, int icon){
        this.name = name;
        this.icon = icon;
    }

    public String getName(){
        return name;
    }

    public int getIconResourceId(){
        return icon;
    }

    public boolean isMenuItem(){
        return true;
    }
}
