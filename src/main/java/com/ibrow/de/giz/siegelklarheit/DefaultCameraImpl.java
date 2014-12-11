package com.ibrow.de.giz.siegelklarheit;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.hardware.Camera;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

/**
 * Default camera interface implementation.
 *
 * Uses the android 4 API, deprecated in API level 21.
 * @see <a href="http://developer.android.com/reference/android/hardware/Camera.html">Camera</a>
 */
class DefaultCameraImpl implements CameraInterface {

    protected final static String LOG_TAG="CAMERA";

    protected android.hardware.Camera camera;
    protected boolean initalised = false;
    protected int cameraId = -1;


    protected static final float zoomFactor=0.65F;
    protected int oldZoomLevel;
    protected int zoomLevel;
    protected boolean canZoom = false;
    protected boolean canSmoothZoom = false;
    protected boolean isZoomedIn = false;

    protected String oldFocusMode;

    protected boolean useAutoFocusCallback = false;
    protected boolean hasContinuousFocus = false;

    protected Rect viewFraming;
    protected int viewFinderSize;


    protected  Rect imageFrame;

    protected int rotation = 0;

    protected boolean previewing = false;

    List<Camera.Size> PreviewModes;
    List<Camera.Size> ImageModes;

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

        this.camera = Camera.open(cameraId); // attempt to get a Camera instance (may throw exception)

        Camera.Parameters params = camera.getParameters();
        // set zoom if poss
        if(params.isZoomSupported() ){
            canZoom = true;
            Log.v(LOG_TAG, "zoom supported");
            oldZoomLevel = params.getZoom();
            zoomLevel = (int) (params.getMaxZoom() * zoomFactor);
            canSmoothZoom = params.isSmoothZoomSupported();
            if( canSmoothZoom ){
                Log.v(LOG_TAG, "smooth zoom supported");
            }
            else {
                params.setZoom(zoomLevel);
                isZoomedIn = true;
                Log.v(LOG_TAG, "Zoom set to " + zoomLevel + " from " + oldZoomLevel);
            }
        }
        else {
            Log.w(LOG_TAG, "Camera has no Zoom Support");
        }

        // steady photo mode if supported
        String old_scene = params.getSceneMode();
        params.setSceneMode(Camera.Parameters.SCENE_MODE_STEADYPHOTO);
        if( params.getSceneMode() != null && params.getSceneMode().equals(Camera.Parameters.SCENE_MODE_STEADYPHOTO)){
            Log.d(LOG_TAG, "Set scene mode to steadyphoto");
        }
        else{
            params.setSceneMode(old_scene);
            Log.w(LOG_TAG, "Could not set scene mode to steadyphoto");
        }

        // video stablisation where supported
        if( params.isVideoStabilizationSupported() ){
            params.setVideoStabilization(true);
            Log.d(LOG_TAG, "Video Stabilization enabled");
        }

        setFocusMode(params);

        // get preview modes
        PreviewModes = params.getSupportedPreviewSizes();
        // get actual image modes
        ImageModes = params.getSupportedPictureSizes();

        try {
            camera.setParameters(params);
        }
        catch( RuntimeException re ){
            Log.e(LOG_TAG, "Could not set camera parameters:" + re.getMessage() );
        }

        initalised = true;
    }

    public boolean canZoom(){
        return canZoom;
    }

    public boolean canSmoothZoom(){
        return canSmoothZoom;
    }

    public boolean isZoomedIn(){
        return isZoomedIn;
    }

    protected boolean zoomTo(final int level){
        if( canZoom ){
            Camera.Parameters params = camera.getParameters();
            params.setZoom(zoomLevel);
            try {
                camera.setParameters(params);
                Log.v(LOG_TAG, "zoomed to "+level);
            }
            catch( RuntimeException re ){
                Log.e(LOG_TAG, "Could not set camera parameters for zoom("+level+") :" + re.getMessage() );
                return false;
            }
            return true;
        }
        Log.e(LOG_TAG, "Zoom not supported - call to zoomTo");
        return false;
    }

    public void zoomOut(){
        if(! isZoomedIn){
            Log.d(LOG_TAG, "Camera already zoomed out");
        }

        if( zoomTo(0) ){
            isZoomedIn = false;
        }
    }

    public void zoomIn(){
        if(isZoomedIn){
            Log.d(LOG_TAG, "Camera already zoomed in");
        }

        if(canSmoothZoom){
            camera.startSmoothZoom(zoomLevel);
        }
        else {
            if (zoomTo(zoomLevel)) {
                isZoomedIn = true;
            }
        }
    }


    /**
     * Sets the camera autofocus mode.
     * Called once by initialse.
     *
     * Does not update the camera params, setParameters(params)
     * MUST be called after this method for changes to take effect.
     *
     * @param params
     * @see #initalise()
     * @see #oldFocusMode
     * @see #oldZoomLevel
     */
    protected final void setFocusMode(Camera.Parameters params){
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
            hasContinuousFocus = true;
            useAutoFocusCallback = true;
            Log.v(LOG_TAG, "Have continuous auto-focus mode");
        }
        else {
            if( haveAutoFocus ){
                params.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
                useAutoFocusCallback = true;
                Log.v(LOG_TAG, "Have normal auto-focus");
            }
            else {
                Log.w(LOG_TAG, "No auto or continuous focus mode");
                if( haveMacroFocus){
                    params.setFocusMode(Camera.Parameters.FOCUS_MODE_MACRO);
                    useAutoFocusCallback = true;
                    Log.d(LOG_TAG, "Using macro focus mode");
                }
                else {
                    Log.w(LOG_TAG, "Not possible to use auto-focus callback - no supported modes");
                }
            }
        }
    }

    /**
     *
     * @param width Display width
     * @param height Display height
     */
    protected void calculateViewFinder(int width, int height) {
        int vf_size = width;

        if (width > height) { // use the smallest of the 2
            vf_size = height;
        }

        viewFinderSize = (int) (VIEWFINDER_SIZE_RELATIVE * vf_size);
        if (viewFinderSize < MIN_VIEWFINDER_SIZE && height > MIN_VIEWFINDER_SIZE && width > MIN_VIEWFINDER_SIZE) {
            viewFinderSize = MIN_VIEWFINDER_SIZE;
        }
        // calculate the view finder size based on the *display* frame size
        int left = (width - viewFinderSize) / 2;
        int top = (height - viewFinderSize) / 2;
        int right = left + viewFinderSize;
        int bottom = top + viewFinderSize;
        viewFraming = new Rect(left, top, right, bottom);

        Log.v(LOG_TAG, "Display width: " + width);
        Log.v(LOG_TAG, "Display height: " + height);
        Log.v(LOG_TAG, "Viewfinder size: " + viewFinderSize);

        // now calculate the real image square
        Camera.Parameters params = camera.getParameters();
        Camera.Size camera_image_size = params.getPictureSize();

        int camera_width = camera_image_size.width;
        int camera_height = camera_image_size.height;

        // camera getPreviewSite does NOT take into account orientation
        // however, the disaply width and height (the params in this method) DO
        Log.v(LOG_TAG,"calculateViewFinder() current camera rotation is: "+rotation);
        if ((rotation >= 90 && rotation < 180) || (rotation >= 270 && rotation < 360)) {
            //  portrait orientation
            int tmp = camera_height;
            camera_height = camera_width;
            camera_width = tmp;
            Log.v(LOG_TAG, "Orientation is portrait, height & width image sizes swapped");
        }


        int image_size;
        float ratio = 0;
        if (height > width) {
            ratio = (float) camera_width / (float) width;
            image_size = (int) (ratio * viewFinderSize);
        } else {
            ratio = (float) camera_height / (float) height;
            image_size = (int) (ratio * viewFinderSize);
        }
        Log.v(LOG_TAG, "ratio: " + ratio);
        Log.v(LOG_TAG, "Crop image size: " + image_size);

        Log.v(LOG_TAG, "camera image width (raw): " + camera_image_size.width);
        Log.v(LOG_TAG, "Camera image height (raw): " + camera_image_size.height);

        if (camera_height > camera_width){ // check against the smallest of the 2 dimensions
            if (image_size > camera_width) {
                Log.w(LOG_TAG, "viewfinder size of "+image_size+" was greater than camera image width of "+camera_width);
                image_size = camera_width;
                Log.v(LOG_TAG, "New crop image size: " + image_size);
            }
        }
        else {
            if (image_size > camera_height) {
                Log.w(LOG_TAG, "viewfinder size of "+image_size+" was greater than camera image height of "+camera_height);
                image_size = camera_height;
                Log.v(LOG_TAG, "New crop image size: " + image_size);
            }
        }



        left=(camera_width - image_size ) / 2;
        top=(camera_height - image_size ) / 2;
        right=left + image_size;
        bottom=top + image_size;

        Log.v(LOG_TAG, "Viewfinder rect - top: "+top);
        Log.v(LOG_TAG, "Viewfinder rect - left: "+left);
        Log.v(LOG_TAG, "Viewfinder rect - right: "+right);
        Log.v(LOG_TAG, "Viewfinder rect - bottom: "+bottom);


        imageFrame = new Rect(left, top, right, bottom);
        Log.v(LOG_TAG, "Viewfinder rect - width: "+imageFrame.width() );
        Log.v(LOG_TAG, "Viewfinder rect - height: "+imageFrame.height());
    }

    public void setPreviewFrameSize(int width, int height){
        setPreviewImageSize(width, height);
        calculateViewFinder(width, height);
    }

    protected void setPreviewImageSize(int display_width, int display_height){
        // we work on a landscape assumption
        if(display_height > display_width){
            int tmp = display_height;
            display_height = display_width;
            display_width = tmp;
        }

        float display_ratio = (float) display_width / (float) display_height;
        display_ratio = roundToOnePlace(display_ratio);

        Camera.Parameters params = camera.getParameters();
        Camera.Size current_mode = params.getPreviewSize();
        Camera.Size best_fit = camera.getParameters().getPreviewSize();
        int last_width_difference = Integer.MAX_VALUE;
        int width_difference;
        int height;
        int width;
        float mode_ratio;

        for( Camera.Size mode : PreviewModes){
            Log.v(LOG_TAG, "Preview mode "+mode.width+"x"+mode.height);
            if( mode.width > mode.height) { //expecting landscape, can't be sure
                height = mode.height;
                width = mode.width;
            }
            else { // portrait preview mode?
                height = mode.width;
                width = mode.height;
            }
            mode_ratio = roundToOnePlace( ((float) width / (float) height) );
            if(mode_ratio == display_ratio){
                width_difference = Math.abs(width - display_width);
                if( width_difference < last_width_difference ||  (width_difference == last_width_difference && width > best_fit.width) ){
                    best_fit = mode;
                    last_width_difference = width_difference;
                    Log.v(LOG_TAG, "Current best fit preview mode "+mode.width+"x"+mode.height);
                }
            }
        } // end loop

        if( best_fit != current_mode ){
            Log.d(LOG_TAG, "Changing preview mode to "+best_fit.width+"x"+best_fit.height);
            params.setPreviewSize(best_fit.width, best_fit.height);
            if( ! previewing ){
                camera.setParameters(params);
            }
            else {
                Log.w(LOG_TAG, "Camera preview already running, stopping before setting camera preview size");
                stopPreview();
                camera.setParameters(params);
                startPreview();
            }
            Log.d(LOG_TAG, "Camera preview size changed");
        }
        else {
            Log.d(LOG_TAG, "Current mode is best fit mode, no change");
        }

        // now the real image size from camera
        if( best_fit.width > best_fit.height) {
            height = best_fit.height;
            width = best_fit.width;
        }
        else { // portrait preview mode?
            height = best_fit.width;
            width = best_fit.height;
        }
        //mode_ratio = roundToOnePlace( ((float) width / (float) height) );
        Log.v(LOG_TAG, "raw ratio of preview mode: "+((float) best_fit.width / (float) best_fit.height));
        mode_ratio = ((float) best_fit.width / (float) best_fit.height);
        Log.v(LOG_TAG, "mode_ratio : "+mode_ratio);
        mode_ratio = roundToOnePlace(mode_ratio);
        Log.v(LOG_TAG, "mode_ratio after rounding: "+mode_ratio);
        setCameraImageSize(mode_ratio);
    }

    protected void setCameraImageSize(float ratio ){
        Log.v(LOG_TAG, "Setting camera real image size, desired ratio is "+ratio);
        Camera.Parameters params = camera.getParameters();
        Camera.Size current_mode = params.getPictureSize();
        Camera.Size best_fit = camera.getParameters().getPictureSize();

        int height;
        int width;
        float mode_ratio;

        int last_width_difference = Integer.MAX_VALUE;
        int width_difference;

        for( Camera.Size mode : ImageModes){
            Log.v(LOG_TAG, "Real image mode "+mode.width+"x"+mode.height);
            if( mode.width > mode.height) { //expecting landscape, can't be sure
                height = mode.height;
                width = mode.width;
            }
            else { // portrait preview mode?
                height = mode.height;
                width = mode.width;
            }
            mode_ratio = roundToOnePlace( ((float) width / (float) height) );
            if(mode_ratio == ratio && height>MIN_VIEWFINDER_SIZE && width >MIN_VIEWFINDER_SIZE){
                width_difference = Math.abs(width - MIN_VIEWFINDER_SIZE );
                if(width_difference < last_width_difference){
                    best_fit =mode;
                    last_width_difference = width_difference;
                }
            }
        }

        if( best_fit != current_mode ){
            Log.d(LOG_TAG, "Changing real image mode to "+best_fit.width+"x"+best_fit.height);
            params.setPictureSize(best_fit.width, best_fit.height);
            if( ! previewing ){
                camera.setParameters(params);
            }
            else {
                Log.w(LOG_TAG, "Camera preview already running, stopping before setting camera real image size");
                stopPreview();
                camera.setParameters(params);
                startPreview();
            }
            Log.d(LOG_TAG, "Camera real image size changed");
        }
        else {
            Log.d(LOG_TAG, "Current real image mode is best fit mode, no change");
        }
    }

    protected final float roundToOnePlace(final float number){
        float x = number * 10;
        x = Math.round(x);
        return x/10;
        //return (Math.round( (number * 10) )) / 10;
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
        Log.v(LOG_TAG,"Setting orientation...");
        Log.v(LOG_TAG,"Current orientation: "+current_Rotation);

        // new rotation needs to be multiple of 90 degrees
        switch (current_Rotation ) {
            case Surface.ROTATION_0: degrees = 0; break;
            case Surface.ROTATION_90: degrees = 90; break;
            case Surface.ROTATION_180: degrees = 180; break;
            case Surface.ROTATION_270: degrees = 270; break;
        }
        int new_rotation = (info.orientation - degrees + 360) % 360;
        //Log.d(LOG_TAG, "current rotation: "+current_Rotation);
        Log.v(LOG_TAG, "camera info rotation: "+info.orientation);
        Log.v(LOG_TAG, "new rotation: "+new_rotation);
        camera.setDisplayOrientation(new_rotation);
        rotation = new_rotation;
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
                Log.e(LOG_TAG, "Error camera.takePicture: " + e.getMessage());
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
        previewing = true;
    }

    @Override
    public void stopPreview() {
        assert camera != null;
        camera.stopPreview();
        previewing = false;
    }

    @Override
    public void release() {
        if(camera != null){
            camera.release();
            camera = null;
            previewing = false;
        }
        else {
            Log.w(LOG_TAG, "Camera already null in call to release()");
        }
    }

    /**
     * Lang. destructor - NOT for direct calling.
     *
     * Calls release if camera object not null.
     *
     * @see #release()
     * @throws Throwable
     */
    protected void finalize() throws Throwable{
        if(camera != null ){
            release();
        }
        super.finalize();
    }

    /**
     * Saves the picture taken to the gallery - used for debugging only.
     *
     */
    private final void saveImage(Bitmap b){
        try{
            java.io.File storageDir = android.os.Environment.getExternalStoragePublicDirectory(
                    android.os.Environment.DIRECTORY_PICTURES);
            if( ! storageDir.exists() ){
                if(storageDir.mkdir()){
                    Log.d(LOG_TAG, "Storage dir created");
                }
            }
            java.io.File image = new java.io.File(storageDir,  "debug-siegelklarheit-last-pic.jpeg");
            b.compress(Bitmap.CompressFormat.JPEG, 100, new FileOutputStream(image));
            Log.d(LOG_TAG, "Pic saved in "+storageDir.getAbsolutePath() );
        }
        catch (Exception e){
            Log.e("CAMAERA", e.getMessage());
        }
    }


    private final class CallbackListener implements android.hardware.Camera.PictureCallback{

        private PictureTakenCallback callback;

        CallbackListener(PictureTakenCallback callback){
            this.callback = callback;
        }

        public void onPictureTaken (byte[] data, Camera camera){
            // crop the camera image based on the view finder
            try {
                Bitmap tmp = BitmapFactory.decodeByteArray(data, 0, data.length);
                Log.v(LOG_TAG, "Image from camera is "+tmp.getWidth()+"px wide, and "+tmp.getHeight()+"px tall");

                Log.v(LOG_TAG, "Camera rotation: "+rotation);
                if ((rotation>= 90 && rotation < 180) || (rotation >= 270 && rotation < 360)) {
                    Log.v(LOG_TAG, "Phone Orientation is portrait, rotating image");
                    Matrix matrix = new Matrix();
                    matrix.postRotate(90);
                    tmp = Bitmap.createBitmap(tmp, 0, 0, tmp.getWidth(), tmp.getHeight(), matrix, true);
                    Log.v(LOG_TAG, "Rotated image is "+tmp.getWidth()+"px wide, and "+tmp.getHeight()+"px tall");
                }

                Bitmap cropped_bitmap = Bitmap.createBitmap(tmp, imageFrame.left, imageFrame.top, imageFrame.width(), imageFrame.height());

                int max_size = (int) (MIN_VIEWFINDER_SIZE * 1.5) ;
                if( cropped_bitmap.getWidth() > max_size ){
                    Log.v(LOG_TAG, "cropped image too big, is "+cropped_bitmap.getWidth()+", scaling to "+max_size+"px");
                    cropped_bitmap = Bitmap.createScaledBitmap(cropped_bitmap, max_size, max_size, false);
                }

                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                cropped_bitmap.compress(Bitmap.CompressFormat.JPEG, 92, baos);
                byte[] cropped_data = baos.toByteArray();
                try {
                    baos.close();
                } catch (IOException e) {
                    Log.e(LOG_TAG, e.getMessage());
                }
                // DEBUGGING
                saveImage(cropped_bitmap);
                // END DEBUGGING
                callback.onPictureTaken(cropped_data);
            }
            catch (Exception e){
                Log.e(LOG_TAG, "Problem with processing taken image: "+e.getMessage());
            }

        }
    }

    private class AutoFocusListener implements Camera.AutoFocusCallback {

        CallbackListener cbl;

        AutoFocusListener(CallbackListener cbl){
            this.cbl = cbl;
        }

        public void onAutoFocus (boolean success, Camera camera){
            if(! success){
                Camera.Parameters params = camera.getParameters();
                Log.w(LOG_TAG, "Auto focus failed, current mode is "+params.getFocusMode());
                if(params.getFocusMode().equals( Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE) ){
                    camera.cancelAutoFocus();
                    Log.d(LOG_TAG, "CF: Continuous mode, not in focus, trying normal auto-focus mode and re-focusing...");
                    params.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
                    camera.setParameters(params);
                    camera.autoFocus(new AutoFocusListener(cbl) );
                    return;
                }
            }
            // http://developer.android.com/reference/android/hardware/Camera.Parameters.html#FOCUS_MODE_CONTINUOUS_VIDEO

            try{
                camera.takePicture(null, null,cbl );
                if(hasContinuousFocus){
                    Log.d(LOG_TAG, "CF: Canceling auto-focus, should re-enable continuous focus after the autoFocus() call");
                    camera.cancelAutoFocus();
                    Camera.Parameters params = camera.getParameters();
                    if( params.getFocusMode().equals( Camera.Parameters.FOCUS_MODE_AUTO) ){
                        Log.d(LOG_TAG, "CF: RE-enable continuous focus mode");
                        params.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
                        camera.setParameters(params);
                    }
                }
            }
            catch (Exception e){
                Log.e(LOG_TAG, "Error camera.takePicture: "+e.getMessage());
            }

        }
    }

}
