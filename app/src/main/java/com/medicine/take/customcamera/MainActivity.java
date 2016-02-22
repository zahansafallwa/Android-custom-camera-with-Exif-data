package com.medicine.take.customcamera;

/**
 * Created by Zahan on 2/14/2016.
 */

import android.app.Activity;
import android.content.Context;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.ExifInterface;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends Activity implements SensorEventListener {
    private Camera camera;
    private Preview preview;
    private SensorManager sensorManager = null;
    private int orientation;
    private ExifInterface exif;
    private int deviceHeight;
    private Button buttonRetake;
    private Button buttonKeep;
    private Button buttonCapture;
    private FrameLayout frameLayout;
    private File rootDirectory;
    private String directory;
    private String fileName;
    private ImageView rotateImage;
    private int degrees = -1;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        rootDirectory = Environment.getExternalStorageDirectory();
        directory = "/custom_camera/";
        // Getting all the needed elements from the layout
        rotateImage = (ImageView) findViewById(R.id.imageView);
        buttonRetake = (Button) findViewById(R.id.buttonRetake);
        buttonKeep = (Button) findViewById(R.id.buttonKeep);
        buttonCapture = (Button) findViewById(R.id.buttonCapture);
        frameLayout = (FrameLayout) findViewById(R.id.buttonContainer);

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        Display display = ((WindowManager) getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        deviceHeight = display.getHeight();

        buttonCapture.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                camera.takePicture(null, null, mPicture);
            }
        });


        buttonRetake.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                File discardedPhoto = new File(Environment.getExternalStorageDirectory() + directory + "/" + fileName);
                discardedPhoto.delete();


                camera.startPreview();

                frameLayout.setVisibility(LinearLayout.VISIBLE);
                buttonRetake.setVisibility(LinearLayout.GONE);
                buttonKeep.setVisibility(LinearLayout.GONE);
            }
        });


        buttonKeep.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                camera.startPreview();
                frameLayout.setVisibility(LinearLayout.VISIBLE);
                buttonRetake.setVisibility(LinearLayout.GONE);
                buttonKeep.setVisibility(LinearLayout.GONE);

            }
        });
    }

    private void createCamera() {

        camera = getCameraInstance();
        Camera.Parameters params = camera.getParameters();
        params.setPictureSize(1600, 1200);
        params.setPictureFormat(PixelFormat.JPEG);
        params.setJpegQuality(85);
        camera.setParameters(params);

        preview = new Preview(this, camera);
        FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);

        float widthFloat = (float) (deviceHeight) * 4 / 3;
        int width = Math.round(widthFloat);

        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(width, deviceHeight);
        preview.setLayoutParams(layoutParams);
        preview.addView(this.preview, 0);
    }

    @Override
    protected void onResume() {
        super.onResume();
        createCamera();
        sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void onPause() {
        super.onPause();
        releaseCamera();
        FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);
        preview.removeViewAt(0);
    }

    private void releaseCamera() {
        if (camera != null) {
            camera.release();
            camera = null;
        }
    }

    public static Camera getCameraInstance() {
        Camera c = null;
        try {

            c = Camera.open();
        } catch (Exception e) {

        }
        return c;
    }

    private PictureCallback mPicture = new PictureCallback() {

        public void onPictureTaken(byte[] data, Camera camera) {

            frameLayout.setVisibility(View.GONE);
            buttonRetake.setVisibility(View.VISIBLE);
            buttonKeep.setVisibility(View.VISIBLE);

            fileName = "img_" + new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date()).toString() + ".jpg";

            File mkDir = new File(Environment.getExternalStorageDirectory() + directory);
            mkDir.mkdirs();
            File pictureFile = new File(Environment.getExternalStorageDirectory() + directory + fileName);

            try {
                FileOutputStream fileOutputStream = new FileOutputStream(pictureFile);
                fileOutputStream.write(data);
                fileOutputStream.close();
            } catch (FileNotFoundException e) {
                Log.d("error", "File not found: " + e.getMessage());
            } catch (IOException e) {
                Log.d("error", "Can't access file: " + e.getMessage());
            }

            try {
                exif = new ExifInterface(Environment.getExternalStorageDirectory() + directory + fileName);
                exif.setAttribute(ExifInterface.TAG_ORIENTATION, "" + orientation);
                exif.saveAttributes();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    };

    public void onSensorChanged(SensorEvent event) {
        synchronized (this) {
            if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                RotateAnimation animation = null;
                if (event.values[0] < 4 && event.values[0] > -4) {
                    if (event.values[1] > 0 && orientation != ExifInterface.ORIENTATION_ROTATE_90) {
                        // UP
                        orientation = ExifInterface.ORIENTATION_ROTATE_90;
                        animation = getRotateAnimation(270);
                        degrees = 270;
                    } else if (event.values[1] < 0 && orientation != ExifInterface.ORIENTATION_ROTATE_270) {
                        // UP SIDE DOWN
                        orientation = ExifInterface.ORIENTATION_ROTATE_270;
                        animation = getRotateAnimation(90);
                        degrees = 90;
                    }
                } else if (event.values[1] < 4 && event.values[1] > -4) {
                    if (event.values[0] > 0 && orientation != ExifInterface.ORIENTATION_NORMAL) {
                        // LEFT
                        orientation = ExifInterface.ORIENTATION_NORMAL;
                        animation = getRotateAnimation(0);
                        degrees = 0;
                    } else if (event.values[0] < 0 && orientation != ExifInterface.ORIENTATION_ROTATE_180) {
                        // RIGHT
                        orientation = ExifInterface.ORIENTATION_ROTATE_180;
                        animation = getRotateAnimation(180);
                        degrees = 180;
                    }
                }
                if (animation != null) {
                    rotateImage.startAnimation(animation);
                }
            }

        }
    }

    private RotateAnimation getRotateAnimation(float toDegrees) {
        float compensation = 0;

        if (Math.abs(degrees - toDegrees) > 180) {
            compensation = 360;
        }
        if (toDegrees == 0) {
            compensation = -compensation;
        }

        RotateAnimation animation = new RotateAnimation(degrees, toDegrees - compensation, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        animation.setDuration(250);
        animation.setFillAfter(true);
        return animation;
    }

    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

}
