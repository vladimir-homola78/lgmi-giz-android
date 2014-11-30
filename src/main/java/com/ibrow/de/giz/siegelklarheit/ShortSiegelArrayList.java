package com.ibrow.de.giz.siegelklarheit;

import java.util.ArrayList;

/**
 * An array list of Short siegel infomation.
 *
 * @see com.ibrow.de.giz.siegelklarheit.ShortSiegelInfo
 * @author Pete
 */
class ShortSiegelArrayList extends ArrayList<ShortSiegelInfo>{

    ShortSiegelArrayList(){
        super();
    }

    ShortSiegelArrayList(int initialCapacity){
        super(initialCapacity);
    }

    @Override
    public ShortSiegelInfo[] toArray(){
        ShortSiegelInfo[] tmp = new ShortSiegelInfo[this.size()];
        return super.toArray(tmp);
    }
}
