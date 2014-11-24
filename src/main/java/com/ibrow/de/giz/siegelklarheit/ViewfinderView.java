package com.ibrow.de.giz.siegelklarheit;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;


import java.util.ArrayList;
import java.util.List;


/**
 * This view is overlaid on top of the camera preview. It adds the viewfinder rectangle and partial
 * transparency outside it.
 */
public final class ViewfinderView extends View {


    private static final int[] SCANNER_ALPHA = {0, 64, 128, 192, 255, 192, 128, 64};
    private static final long ANIMATION_DELAY = 80L;
    private static final int CURRENT_POINT_OPACITY = 0xA0;
    private static final int MAX_RESULT_POINTS = 20;
    private static final int POINT_SIZE = 6;

    private static final String MASK_COLOUR="#44ffffff"; // aRGB
    private static final String FINDER_COLOUR="#88ffffff";



    private final Paint paint;

    private final int maskColor;
    private final int finderColor;

    private int finderSize;
    private int framePad;

    private int scannerAlpha;

    private CameraInterface camera;




    public ViewfinderView(Context context) {
        super(context);


        // Initialize these once for performance rather than calling them every time in onDraw().
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);

        maskColor = Color.parseColor(MASK_COLOUR);
        finderColor = Color.parseColor(FINDER_COLOUR);

    }


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

        finderSize = frame.width() / 6;
        framePad = frame.width() / 10;

        int width = canvas.getWidth();
        int height = canvas.getHeight();


        // Draw the exterior (i.e. outside the framing rect)
        paint.setColor( maskColor);
        paint.setStyle(Paint.Style.FILL);
        canvas.drawRect(0, 0, width, frame.top, paint);
        canvas.drawRect(0, frame.top, frame.left, frame.bottom + 1, paint);
        canvas.drawRect(frame.right + 1, frame.top, width, frame.bottom + 1, paint);
        canvas.drawRect(0, frame.bottom + 1, width, height, paint);

        paint.setColor( finderColor);
        paint.setStyle(Paint.Style.STROKE);

        if(frame.width() > 350 ){
            paint.setStrokeWidth(3.0F);
        }
        else {
            paint.setStrokeWidth(1.0F);
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

/*
    public void drawViewfinder() {
        Bitmap resultBitmap = this.resultBitmap;
        this.resultBitmap = null;
        if (resultBitmap != null) {
            resultBitmap.recycle();
        }
        invalidate();
    }
*/
}
