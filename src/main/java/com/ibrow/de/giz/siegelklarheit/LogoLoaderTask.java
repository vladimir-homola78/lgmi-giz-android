package com.ibrow.de.giz.siegelklarheit;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.util.Log;

/**
 * Abstract base asynchronous task to load siegel logo images.
 *
 * Override onProgressUpdate() to update the GUI.
 *
 * Uses LogoHelper class
 *
 * @author Pete
 * @see com.ibrow.de.giz.siegelklarheit.LogoHelper#getImage(Siegel)
 */
abstract class LogoLoaderTask extends AsyncTask<Siegel,Bitmap, Void>{

    protected Void doInBackground(Siegel... siegels){
        int count = siegels.length;
        for (int i = 0; i < count; i++) {
            try {
                Bitmap image = LogoHelper.getImage(siegels[i]);
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

    abstract protected void onProgressUpdate(Bitmap... progress); // UI Thread

    protected void onPostExecute(Void result){
        // finsihed, NOP
    }
}
