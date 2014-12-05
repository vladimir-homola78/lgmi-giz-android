package com.ibrow.de.giz.siegelklarheit;

/**
 * Special infos navigation draw item.
 *
 * @author Pete
 * @see com.ibrow.de.giz.siegelklarheit.NavDrawItem
 */
public final class NavDrawInfoItem extends NavDrawItem {

    NavDrawInfoItem() {
        super("Die Bundesregierung", R.drawable.bundesregierung);
    }

    public boolean isMenuItem() {
        return false;
    }

}
