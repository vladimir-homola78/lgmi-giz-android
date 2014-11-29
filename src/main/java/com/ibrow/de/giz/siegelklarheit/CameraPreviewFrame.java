package com.ibrow.de.giz.siegelklarheit;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.IOException;

/**
 * Camera preview frame.
 *
 * @see com.ibrow.de.giz.siegelklarheit.ScanActivity
 * @author Pete
 */
public class CameraPreviewFrame extends SurfaceView implements SurfaceHolder.Callback{


    private SurfaceHolder holder;
    private CameraInterface camera;
    private Activity activity;

    public CameraPreviewFrame(Activity activity, CameraInterface camera) {
        super(activity); // Activity is a subclass of Context
        assert camera != null;
        assert camera.getIsInitialised() == true;

        this.camera = camera;
        holder = getHolder();
        holder.addCallback(this);
        this.activity = activity;
    }


    /**
     * Used to update the camera attatched to the frame
     * when we've needed to reinitialise the camera
     * on a onResume().
     *
     * @param camera
     * @see ScanActivity#reinitialiseCamera()
     */
    public void setCamera(CameraInterface camera) {
        this.camera = camera;
    }

    public void surfaceCreated(SurfaceHolder holder) {
        // The Surface has been created, now tell the camera where to draw the preview.
        try {
            camera.setPreviewDisplay(holder);
            camera.setOrientation(activity.getWindowManager().getDefaultDisplay().getRotation());
            camera.setPreviewFrameSize(this.getWidth(), this.getHeight() );
            camera.startPreview();
        } catch (Exception e) {
            Log.w("CAMERA", "Error setting camera preview: " + e.getMessage());
        }
    }

    public void surfaceDestroyed(SurfaceHolder holder) {
        camera.release();
    }

    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
        if (this.holder.getSurface() == null){
            // preview surface does not exist
            return;
        }

        // stop preview before making changes
        try {
            camera.stopPreview();
        } catch (Exception e){
            Log.e("CAMERA", "Error stopping camera preview: " + e.getMessage());
        }

        camera.setOrientation(activity.getWindowManager().getDefaultDisplay().getRotation());

        // start preview with new settings
        try {
            camera.setPreviewDisplay(this.holder);
            camera.setPreviewFrameSize(this.getWidth(), this.getHeight() );
            camera.startPreview();

        } catch (Exception e){
            Log.w("CAMERA", "Error starting camera preview: " + e.getMessage());
        }
    }
}
