package com.ibrow.de.giz.siegelklarheit;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.hardware.Camera;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

/**
 * Default camera interface implementation.
 *
 * Uses the android 4 API, deprecated in API level 21.
 * @see <a href="http://developer.android.com/reference/android/hardware/Camera.html">Camera</a>
 */
class DefaultCameraImpl implements CameraInterface {

    protected android.hardware.Camera camera;
    protected boolean initalised = false;
    protected int cameraId = -1;

    protected int oldZoomLevel;

    protected static final float zoomFactor=0.8F;

    protected String oldFocusMode;

    protected boolean useAutoFocusCallback = false;
    protected boolean hasContinuousFocus = false;

    protected Rect viewFraming;
    protected int viewFinderSize;
    protected Camera.Size previewSize;

    protected  Rect imageFrame;

    protected DefaultCameraImpl(){
        //NOP
    }

    @Override
    /** @todo Deal with front-camera only situation */
    public void initalise() throws Exception{
        int num_cameras= Camera.getNumberOfCameras();
        for(int i=0; i< num_cameras; i++){
            Camera.CameraInfo info = new android.hardware.Camera.CameraInfo();
            Camera.getCameraInfo(i, info);
            if (info.facing == Camera.CameraInfo.CAMERA_FACING_BACK){ // first back facing camera
                cameraId = i;
                break;
            }
        }

        if(cameraId == -1){
            throw new Exception("No Camera found");
        }

        Camera c = Camera.open(cameraId); // attempt to get a Camera instance (may throw exception)
        this.camera = c;

        Camera.Parameters params = camera.getParameters();
        // set zoom if poss
        if(params.isZoomSupported() ){
            oldZoomLevel = params.getZoom();
            int new_zoom = (int) (params.getMaxZoom() * zoomFactor);
            params.setZoom(new_zoom);
            Log.d("CAMERA", "Zoom set to "+new_zoom+" from "+oldZoomLevel);
            camera.setParameters(params);
        }
        else {
            Log.d("CAMERA", "Camera has no Zoom Support");
        }

        // try and set to auto focus
        oldFocusMode = params.getFocusMode();
        boolean haveContinuousFocus = false;
        boolean haveAutoFocus = false;
        boolean haveMacroFocus = false;
        String fm;
        List<String> focus_modes = params.getSupportedFocusModes();
        int focus_mode_size = focus_modes.size();
        for(int i=0; i<focus_mode_size; i++ ) {
            fm = focus_modes.get(i);
            if (fm.equals(Camera.Parameters.FOCUS_MODE_AUTO)) {
                haveAutoFocus = true;
            }
            else {
                if (fm.equals(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
                    haveContinuousFocus = true;
                }
                else{
                    if (fm.equals(Camera.Parameters.FOCUS_MODE_MACRO)) {
                        haveMacroFocus = true;
                    }
                }
            }
        }
        if( haveContinuousFocus){
            params.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
            camera.setParameters(params);
            hasContinuousFocus = true;
            useAutoFocusCallback = true;
            Log.d("CAMERA", "Have continiuos autofocus");
        }
        else {
            if( haveAutoFocus ){
                params.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
                camera.setParameters(params);
                useAutoFocusCallback = true;
                Log.d("CAMERA", "Have normal autofocus");
            }
            else {
                Log.w("CAMERA", "No auto focus mode");
                if( haveMacroFocus){
                    params.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
                    camera.setParameters(params);
                    useAutoFocusCallback = true;
                    Log.d("CAMERA", "Using macro focus mode");
                }
            }
        }

        initalised = true;
    }

    /**
     *
     * @param width Display width
     * @param height Display height
     */
    protected void calculateViewFinder(int width, int height){

        Camera.Parameters params = camera.getParameters();
        previewSize = params.getPreviewSize();

        int vf_size= width;

        if( width > height){ // use the smallest of the 2
            vf_size = height;
        }

        viewFinderSize = (int) (VIEWFINDER_SIZE_RELATIVE * vf_size);
        if(viewFinderSize<MIN_VIEWFINDER_SIZE && height > MIN_VIEWFINDER_SIZE && width> MIN_VIEWFINDER_SIZE){
            viewFinderSize = MIN_VIEWFINDER_SIZE;
        }
        // calculate the view finder size based on the *display* frame size
        int left=(width -viewFinderSize ) / 2;
        int top=(height -viewFinderSize ) / 2;
        int right=left + viewFinderSize;
        int bottom=top+viewFinderSize;
        viewFraming = new Rect(left, top, right,bottom);
        Log.d("CAMERA", "Viewfinder size: "+viewFinderSize);

        // now calculate the real image square

        int image_size;
        float ratio=0;
        if(height > width ){
           ratio  =  (float) previewSize.width/(float)width;
        }
        else {
            ratio = (float) previewSize.height/(float)height;
        }
        image_size = (int) (ratio * viewFinderSize);
        Log.d("CAMERA", "Crop image size: "+image_size);


        left=(previewSize.width - image_size ) / 2;
        top=(previewSize.height - image_size ) / 2;
        right=left + image_size;
        bottom=top + image_size;
        Log.d("CAMERA", "Viewfinder rect - top: "+top);
        Log.d("CAMERA", "Viewfinder rect - left: "+left);
        Log.d("CAMERA", "Viewfinder rect - right: "+right);
        Log.d("CAMERA", "Viewfinder rect - bottom: "+bottom);

        imageFrame = new Rect(left, top, right, bottom);

        Log.d("CAMERA", "Viewfinder width: "+imageFrame.width());
        Log.d("CAMERA", "Viewfinder height: "+imageFrame.height());
    }

    public void setPreviewFrameSize(int width, int height){
        calculateViewFinder(width, height);
    }

    @Override
    public boolean getIsInitialised(){
        return (camera != null && initalised);
    }

    @Override
    public void setOrientation(int current_Rotation ){
        Camera.CameraInfo info = new Camera.CameraInfo();
        Camera.getCameraInfo(cameraId, info);
        int degrees = 0;
        // new rotation needs to be multiple of 90 degrees
        switch (current_Rotation ) {
            case Surface.ROTATION_0: degrees = 0; break;
            case Surface.ROTATION_90: degrees = 90; break;
            case Surface.ROTATION_180: degrees = 180; break;
            case Surface.ROTATION_270: degrees = 270; break;
        }
        int new_rotation = (info.orientation - degrees + 360) % 360;
        /*Log.d("CAMERA", "current rotation: "+current_Rotation);
        Log.d("CAMERA", "camera info rotation: "+info.orientation);
        Log.d("CAMERA", "new rotation: "+new_rotation);*/
        camera.setDisplayOrientation(new_rotation);
    }

    public Rect getViewFramingRect(){
        return viewFraming;
    }

    public void takePicture(PictureTakenCallback callback){
        assert camera != null;
        assert initalised == true;
        CallbackListener cbl = new CallbackListener( callback );

        if(useAutoFocusCallback){
            camera.autoFocus(new AutoFocusListener(cbl));
        }
        else {
            try {
                camera.takePicture(null, null, cbl);
            } catch (Exception e) {
                Log.e("CAMERA", "Error camera.takePicture: " + e.getMessage());
            }
        }
    }



    @Override
    public void setPreviewDisplay(SurfaceHolder holder) throws IOException{
        assert camera != null;
        assert holder !=null;
        camera.setPreviewDisplay(holder);
    }

    @Override
    public void startPreview(){
        assert camera != null;
        camera.startPreview();
    }

    @Override
    public void stopPreview() {
        assert camera != null;
        camera.stopPreview();
    }

    @Override
    public void release() {
        if(camera != null){
            camera.release();
            camera = null;
        }
    }


    private final class CallbackListener implements android.hardware.Camera.PictureCallback{

        private PictureTakenCallback callback;

        CallbackListener(PictureTakenCallback callback){
            this.callback = callback;
        }

        public void onPictureTaken (byte[] data, Camera camera){
            // crop the camera image based on the view finder
            Bitmap tmp= BitmapFactory.decodeByteArray(data, 0, data.length);
            Bitmap cropped_bitmap = Bitmap.createBitmap(tmp, imageFrame.left, imageFrame.top, imageFrame.width(), imageFrame.height() );
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            cropped_bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            byte[] cropped_data =baos.toByteArray();
            try{
                baos.close();
            }
            catch (IOException e){
                Log.e("CAMERA", e.getMessage());
            }
            callback.onPictureTaken( cropped_data );
        }
    }

    private class AutoFocusListener implements Camera.AutoFocusCallback {

        CallbackListener cbl;

        AutoFocusListener(CallbackListener cbl){
            this.cbl = cbl;
        }

        public void onAutoFocus (boolean success, Camera camera){
            if(! success){
                Log.w("CAMERA", "Auto focus failed");
                if(hasContinuousFocus){
                    Log.d("CAMERA", "CF: Continious mode, not in focus, tring normal autofocus mode and re-focusing...");
                    Camera.Parameters params = camera.getParameters();
                    params.setFocusMode(Camera.Parameters.FLASH_MODE_AUTO);
                    camera.setParameters(params);
                    camera.autoFocus(new AutoFocusListener(cbl) );
                    return;
                }
            }
            // http://developer.android.com/reference/android/hardware/Camera.Parameters.html#FOCUS_MODE_CONTINUOUS_VIDEO

            try{
                camera.takePicture(null, null,cbl );
                if(hasContinuousFocus){
                    Log.d("CAMERA", "CF: Canceling autoficus, should renable continious focus");
                    camera.cancelAutoFocus();
                    Camera.Parameters params = camera.getParameters();
                    if( params.getFocusMode().equals( Camera.Parameters.FOCUS_MODE_AUTO) ){
                        Log.d("CAMERA", "CF: RE-enable continious focus mode");
                        params.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
                        camera.setParameters(params);
                    }
                }
            }
            catch (Exception e){
                Log.e("CAMERA", "Error camera.takePicture: "+e.getMessage());
            }

        }
    }

}