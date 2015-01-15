package com.ibrow.de.giz.siegelklarheit;

/**
 * Factory class that provides a CameraInterface.
 * Used by the scan activity
 *
 * @see com.ibrow.de.giz.siegelklarheit.CameraInterface
 * @see com.ibrow.de.giz.siegelklarheit.ScanActivity
 * @author Pete
 */
public final class CameraProvider {

    /**
     * Prevent initilisation.
     */
    private CameraProvider(){
        // NOP
    }

    /**
     * Provides an interface to the camera.
     *
     * @todo Add driver for camera2 API (new in level 21)
     * */
    public static CameraInterface getCamera(){
        return new DefaultCameraImpl();
    }
}
