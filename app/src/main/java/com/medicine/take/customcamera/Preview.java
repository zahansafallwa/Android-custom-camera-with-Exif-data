package com.medicine.take.customcamera;

/**
 * Created by Zahan on 2/14/2016.
 */

import android.content.Context;
import android.hardware.Camera;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.IOException;

public class Preview extends SurfaceView implements SurfaceHolder.Callback {
    private SurfaceHolder holder;
    private Camera camera;

    public Preview(Context context, Camera camera) {
        super(context);
        this.camera = camera;
        holder = getHolder();
        holder.addCallback(this);
        holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        holder.setFixedSize(50, 50);
    }
    public void surfaceCreated(SurfaceHolder holder) {

        try {
            camera.setPreviewDisplay(holder);
            camera.startPreview();
        } catch (IOException e) {
            Log.d("error", "Can't set camera preview: " + e.getMessage());
        }

    }

    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

        if (this.holder.getSurface() == null) {
            return;
        }
        try {
            camera.stopPreview();
        } catch (Exception e) {
        }
        try {
            camera.setPreviewDisplay(this.holder);
            camera.startPreview();
        } catch (Exception e) {
            Log.d("DG_DEBUG", "Error starting camera preview: " + e.getMessage());
        }
    }

    public void surfaceDestroyed(SurfaceHolder holder) {

    }

}
