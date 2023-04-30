package UtilityClasses;

import static android.graphics.ImageFormat.*;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class VideoView extends SurfaceView implements SurfaceHolder.Callback {
    private Camera camera;
    private SurfaceHolder holder;
    private byte[] buffer;

    public VideoView(Context context) {
        super(context);
        holder = getHolder();
        holder.addCallback(this);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        // Open the camera and set the preview display
        camera = Camera.open();
        try {
            camera.setPreviewDisplay(holder);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        // Start the preview
        camera.startPreview();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        // Release the camera
        camera.stopPreview();
        camera.release();
        camera = null;
    }

    public void setVideoData(byte[] data) {
        // Convert the YUV data to RGB data
        YuvImage yuvImage = new YuvImage(data, NV21, 640, 480, null);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        yuvImage.compressToJpeg(new Rect(0, 0, 640, 480), 50, outputStream);
        byte[] rgbData = outputStream.toByteArray();

        // Copy the RGB data to the buffer
        buffer = rgbData;
        // Invalidate the view to force a redraw
        invalidate();
    }

    @Override
    public void onDraw(Canvas canvas) {
        // Draw the video data to the SurfaceView
        if (buffer != null) {
            Bitmap bitmap = BitmapFactory.decodeByteArray(buffer, 0, buffer.length);
            canvas.drawBitmap(bitmap, 0, 0, null);
        }
    }
}
