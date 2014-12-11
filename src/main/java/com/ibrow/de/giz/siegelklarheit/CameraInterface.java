package com.ibrow.de.giz.siegelklarheit;

import android.graphics.Rect;
import android.view.SurfaceHolder;

/**
 * Interface to the camera hardware.
 *
 * This allows us to provide an abstraction to differing underlying
 * API versions (e.g. camera2 in API level 21) and hardware capabilities
 * (e.g. autofocus).
 *
 * Used by ScanActivity
 *
 * @see CameraProvider#getCamera()
 * @see com.ibrow.de.giz.siegelklarheit.ScanActivity
 *
 * @author Pete
 *
 * */
public interface CameraInterface {


    public static final int MIN_VIEWFINDER_SIZE=300;
    public static final double VIEWFINDER_SIZE_RELATIVE=0.75;


    /**
    * Initialises the camera.
    *
    * Must be called prior to all other methods.
    * @throws java.lang.Exception If we cannot access the camera.
    * */
    public void initalise() throws Exception;

    /**
     * Returns if the camera instance has been initalised.
     *
     * @return true if initalise() has been successfully called
     * @see #initalise()
     */
    public boolean getIsInitialised();


    /**
     * Set the camera in portrait or landscape mode.
     *
     * @param current_Rotation The current display rotation - 0, 90, 180 or 270
     * @see android.view.Surface
     */
    public void setOrientation(int current_Rotation );


    /**
     * Tell the camera implementation how big the preview frame is.
     * Called only by CameraPreviewFrame.
     * Used for calculating view finder size.
     *
     * @param width
     * @param height
     * @see CameraPreviewFrame
     * @see #getViewFramingRect()
     */
    public void setPreviewFrameSize(int width, int height);

    /**
    * Sets the preview display item.
    *
    * @param holder The preview surface item
    * */
    public void setPreviewDisplay(SurfaceHolder holder) throws Exception;

    /**
    * Starts the preview
    * */
    public void startPreview();

    /**
    * Ends the preview.
    * */
    public void stopPreview();

    /**
    * Free the camera resource.
    * */
    public void release();

    /**
     * Can this camera zoom.
     *
     * @return true if the camera can zoom
     */
    public boolean canZoom();

    /**
     * Is the camera zoomed in or not?
     *
     * @return true if zoomed in, otherwise false
     */
    public boolean isZoomedIn();


    public void zoomOut();
    public void zoomIn();
    public boolean canSmoothZoom();

    /**
     * Takes a picture.
     * callback.onPictureTaken() is called when complete.
     * @param callback
     */
    public void takePicture(PictureTakenCallback callback);

    /**
     * Returns the view finder shape, relative to the display frame.
     * Used only by ViewFinderView.
     * @return
     * @see com.ibrow.de.giz.siegelklarheit.ViewfinderView
     */
    public Rect getViewFramingRect();
}
