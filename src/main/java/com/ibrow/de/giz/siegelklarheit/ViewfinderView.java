package com.ibrow.de.giz.siegelklarheit;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.view.View;

/**
 * This view is overlaid on top of the camera preview. It adds the viewfinder rectangle and partial
 * transparency outside it.
 *
 * @author Pete
 * @see ScanActivity#connectPreviewFrame()
 */
public final class ViewfinderView extends View {

    /** Surrounding Transparency color aRGB */
    private static final String MASK_COLOUR="#44ffffff";
    /** Color of the 4 right-angles drwan in the corners of the finder. aRGB */
    private static final String FINDER_COLOUR="#88ffffff";



    private final Paint paint;

    private final int maskColor;
    private final int finderColor;

    /** Length of the lines for drawing the 4 corners inside the viewfinder.
     * Proportionally calculated based on the size of the viewfinder. */
    private int finderSize;

    /**How much space between the perimeter of the viewfinder and where we draw the 4 right-angles.
     * Proportionally calculated based on the size of the viewfinder. */
    private int framePad;

    private static final int ZERO=0;
    private static final int ONE=1;

    /**
     * framePad = finder.width / PAD_PROPORTION.
     * @see #framePad
     */
    private static final int PAD_PROPORTION=10;

    /**
     * Used to calculate length of line of the 4 right angles.
     * finderSize = finder.width / FINDER_SIZE_PROPORTION
     * @see #finderSize
     */
    private static final int FINDER_SIZE_PROPORTION=6;

    /**
     * Stroke size of right-angle lines on larger displays.
     */
    private static final float THICK_STROKE=3.0F;

    /**
     * Stroke size of right-angle lines on small displays.
     */
    private static final float THIN_STROKE=1.0F;


    /**
     * At which size we use THICK_STROKE instead of THIN_STROKE
     *  - size of viewfinder.
     */
    private static final int MIN_WIDTH_FOR_THICK_STROKE=350;


    private CameraInterface camera;

    public ViewfinderView(Context context) {
        super(context);
        // Initialize these once for performance rather than calling them every time in onDraw().
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        maskColor = Color.parseColor(MASK_COLOUR);
        finderColor = Color.parseColor(FINDER_COLOUR);
    }


    /**
     * Sets the camera for this view finder view.
     * Used only by the scan activity.
     *
     * @param camera
     * @see com.ibrow.de.giz.siegelklarheit.ScanActivity#connectPreviewFrame()
     */
    public void setCamera(CameraInterface camera) {
        this.camera = camera;
    }


    @Override
    public void onDraw(Canvas canvas) {
        if (camera == null || camera.getIsInitialised()!=true ){
            return; // not ready yet, early draw before done configuring
        }
        Rect frame = camera.getViewFramingRect();

        if (frame == null) {
            return;
        }


        finderSize = frame.width() / FINDER_SIZE_PROPORTION;
        framePad = frame.width() / PAD_PROPORTION;

        assert frame.width() == frame.height(); //should be square!

        int width = canvas.getWidth();
        int height = canvas.getHeight();


        // Draw the exterior (i.e. outside the framing rect)
        paint.setColor( maskColor);
        paint.setStyle(Paint.Style.FILL);
        canvas.drawRect(ZERO, ZERO, width, frame.top, paint);
        canvas.drawRect(ZERO, frame.top, frame.left, frame.bottom + ONE, paint);
        canvas.drawRect(frame.right + ONE, frame.top, width, frame.bottom + ONE, paint);
        canvas.drawRect(ZERO, frame.bottom + ONE, width, height, paint);

        paint.setColor( finderColor);
        paint.setStyle(Paint.Style.STROKE);

        if(frame.width() >  MIN_WIDTH_FOR_THICK_STROKE ){
            paint.setStrokeWidth(THICK_STROKE);
        }
        else {
            paint.setStrokeWidth(THIN_STROKE);
        }
        // top left
        int left = frame.left+framePad;
        int top = frame.top+framePad;
        int bottom= frame.bottom - framePad;
        int right = frame.right - framePad;

        // top left
        canvas.drawLine(left, top, left, top+finderSize, paint); //line down
        canvas.drawLine(left, top, left+finderSize, top, paint); // line accross
        // top right
        canvas.drawLine(right, top, right, top+finderSize, paint); //line down
        canvas.drawLine(right, top, right-finderSize, top, paint); //line accross
        // bottom left
        canvas.drawLine(left, bottom, left, bottom-finderSize, paint); //line up
        canvas.drawLine(left, bottom, left+finderSize, bottom, paint); // line accross
        // bottom right
        canvas.drawLine(right, bottom, right, bottom-finderSize, paint); //line up
        canvas.drawLine(right, bottom, right-finderSize, bottom, paint); //line accross

        //canvas.drawRect(frame, paint);
    }

}
