package com.ibrow.de.giz.siegelklarheit;

/**
 * Activity callback after a picture is taken.
 *
 * @see com.ibrow.de.giz.siegelklarheit.ScanActivity
 * @author Pete
 */
interface PictureTakenCallback {

    public void onPictureTaken(byte[] image);
}
