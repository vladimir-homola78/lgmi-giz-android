package com.ibrow.de.giz.siegelklarheit;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.util.Log;
import android.util.LruCache;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Helper class for loading the Siegel logo images.
 *
 * @see LogoLoaderTask
 * @see com.ibrow.de.giz.siegelklarheit.ShortSiegelInfo#getLogoURL()
 * @author Pete
 */
final class LogoHelper {

    private static final LruCache<Integer, Bitmap> MemCache = new LruCache<Integer, Bitmap>(IdentifeyeAPIInterface.MAX_ENTRIES);

    private static boolean DiskCachePathInitialised=false;
    private static File DiskCachePath;

    private static final Object DiskCacheLock = new Object();

    private static final String CACHE_NAME="logo_img_cache";
    private static final int IMAGE_SIZE=4096;
    private static final int DISK_CACHE_SIZE = (IdentifeyeAPIInterface.MAX_ENTRIES * IMAGE_SIZE );
    private static final int CACHE_VERSION=1;

    /**
     * Fetches the logo image for a siegel.
     *
     * DO NOT CALL IN THE GUI THREAD - use async task or similar.
     *
     * Call initDiskCachePath once first before this method.
     *
     * @param siegel
     * @return
     *
     * @see #initDiskCachePath(android.content.Context)
     * @see LogoLoaderTask
     *
     */
    public static Bitmap getImage(Siegel siegel) throws Exception{
        Bitmap image;
        // check memory cache first
        Integer siegel_id = new Integer(siegel.getId());
        image = (Bitmap) MemCache.get( siegel_id );
        if(image != null){
            Log.v("LOGOHELPER", "memory cache hit");
            return image;
        }
        // try disk
        if( DiskCachePathInitialised ){
            String filepath= DiskCachePath.getAbsolutePath() + File.separator + siegel_id.toString()+".png";
            File file=new File(filepath);
            if( file.exists() ){
                synchronized (DiskCacheLock){
                    image = BitmapFactory.decodeFile(filepath);
                    //DiskCacheLock.notifyAll();
                }
                MemCache.put(siegel_id, image);
                Log.v("LOGOHELPER", "disk cache hit");
                return image;
            }
        }
        else {
            Log.w("LOGOHELPER", "Disk cache path not initalised");
        }
        // pull from web

        String logo_url = siegel.getLogoURL();
        Log.v("LOGOHELPER", "Downloading "+logo_url);
        HttpURLConnection conn = null;
        InputStream is = null;

        try {
            conn = (HttpURLConnection) (new URL(logo_url)).openConnection();
            conn.connect();

            if (conn.getResponseCode() >= 400 ) {
                throw new Exception("Error, server response: " + conn.getResponseCode());
            }
            is=conn.getInputStream();
            image = BitmapFactory.decodeStream(is);
        }
        catch(Exception e){
            Log.e("LOGOHELPER", e.getMessage() );
            throw e;
        }
        finally {
            if(is != null){
                is.close();
            }
            if(conn != null){
                conn.disconnect();
            }
        }

        Log.v("LOGOHELPER", "got image from "+logo_url);
        // store in memory cache for next call
        MemCache.put(siegel_id, image);

        // store in disk cache for the future
        if( DiskCachePathInitialised ) {
            String filepath = DiskCachePath.getAbsolutePath() + File.separator + siegel_id.toString() + ".png";
            FileOutputStream out = null;
            synchronized (DiskCacheLock) {
                try {
                    out = new FileOutputStream(filepath);
                    image.compress(Bitmap.CompressFormat.PNG, 100, out);
                }
                catch (Exception e) {
                    Log.e("LOGOHELPER", e.getMessage() );
                }
                finally {
                    try {
                        if (out != null) {
                            out.close();
                        }
                    } catch (IOException e) {
                        Log.e("LOGOHELPER", e.getMessage());
                    }
                }
                //DiskCacheLock.notifyAll();
            }

        }

        return image;
    }

    /**
     * Initialsises the disk cache location.
     *
    * @param context
     */
    public static void initDiskCachePath(final Context context){
        if(DiskCachePathInitialised) { // already done
            return;
        }

        String cachePath = Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState()) || !Environment.isExternalStorageRemovable() ?
                        context.getExternalCacheDir().getPath() : context.getCacheDir().getPath();

        DiskCachePath =  new File(cachePath + File.separator + CACHE_NAME);
        if(! DiskCachePath.exists() ){
            try{
                if( DiskCachePath.mkdir() ){
                    DiskCachePathInitialised = true;
                }
            }
            catch (Exception e){
                Log.e("LOGOHELPER", "Error creating cache path: "+e.getMessage());
            }
        }
        else {
            DiskCachePathInitialised = true;
        }
    }
}
