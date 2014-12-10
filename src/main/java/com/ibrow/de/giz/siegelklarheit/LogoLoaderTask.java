package com.ibrow.de.giz.siegelklarheit;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.util.Log;

/**
 * Abstract base asynchronous task to load siegel logo images.
 *
 * Override onProgressUpdate() to update the GUI.
 *
 * Uses LogoHelper class.
 *
 * @author Pete
 * @see com.ibrow.de.giz.siegelklarheit.LogoHelper#getImage(Siegel)
 */
abstract class LogoLoaderTask extends AsyncTask<Siegel, Bitmap, Void>{

    /**
     * Current index of fetched Bitmap.
     */
    protected int index=-1;

    @Override
    protected Void doInBackground(Siegel... siegels){
        int count = siegels.length;
        for (int i = 0; i < count; i++) {
            try {
                Bitmap image = LogoHelper.getImage(siegels[i]);
                index = i;
                publishProgress(image);
            }
            catch (Exception e){
                Log.e("LogoLoaderTask", e.getMessage());
            }
            // Escape early if cancel() is called
            if (isCancelled()) break;
        }
        return null;
    }

    /**
     * This is called in the GUI thread.
     * UPdate the GUI with the image here.
     *
     * @param progress The currently fetched bitmaps - entries maybe null.
     * @see #index
     */
    @Override
    abstract protected void onProgressUpdate(Bitmap... progress); // UI Thread


    /**
     * This, by default, does nothing, as the images should be set in onProgressUpdate.
     *
     * Called in the GUI thread.
     *
     * @param result
     * @see #onProgressUpdate(android.graphics.Bitmap...)
     */
    @Override
    protected void onPostExecute(Void result){
        if( isCancelled() ){
            return;
        }
    }
}
