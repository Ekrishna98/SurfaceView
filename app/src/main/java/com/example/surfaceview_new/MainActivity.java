package com.example.surfaceview_new;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

public class MainActivity extends AppCompatActivity implements SurfaceHolder.Callback{

    Camera camera;
    SurfaceView surfaceView;
    SurfaceHolder surfaceHolder;
    boolean previewing = false;
    private int rotation;
    private int cameraId;
    Button BtnTakePicture;
    LayoutInflater controlInflater = null;
    final int RESULT_SAVEIMAGE = 0;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        controlInflater = LayoutInflater.from(getBaseContext());
        View viewControl = controlInflater.inflate(R.layout.control, null);
        ViewGroup.LayoutParams layoutParamsControl
                = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT,
                ViewGroup.LayoutParams.FILL_PARENT);
        this.addContentView(viewControl, layoutParamsControl);

        // Camera And ExternalStorage permissions
        if ((ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) || (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED))
        {
            askPerms();
        }



        getWindow().setFormat(PixelFormat.UNKNOWN);
        surfaceView = findViewById(R.id.surfaceView);
        surfaceHolder = surfaceView.getHolder();
        surfaceHolder.addCallback(this);
        surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

        BtnTakePicture = findViewById(R.id.takepicture);
        BtnTakePicture.setVisibility(View.VISIBLE);
        BtnTakePicture.setEnabled(false);

        BtnTakePicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                camera.takePicture(myShutterCallback,
                        myPictureCallback_RAW, myPictureCallback_JPG);
                BtnTakePicture.setEnabled(false);
                surfaceView.setBackground(getDrawable(R.drawable.border_lines_red));
            }});


        surfaceView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                camera.autoFocus(myAutoFocusCallback);
            }
        });

    }

        Camera.ShutterCallback myShutterCallback = new Camera.ShutterCallback(){

            @Override
            public void onShutter() {
                // TODO Auto-generated method stub

            }};

        Camera.PictureCallback myPictureCallback_RAW = new Camera.PictureCallback(){

            @Override
            public void onPictureTaken(byte[] arg0, Camera arg1) {
                // TODO Auto-generated method stub

            }};

        Camera.PictureCallback myPictureCallback_JPG = new Camera.PictureCallback(){

            @Override
            public void onPictureTaken(byte[] arg0, Camera arg1) {
                // TODO Auto-generated method stub
//                Bitmap bitmapPicture
//                        = BitmapFactory.decodeByteArray(arg0, 0, arg0.length);

                Uri uriTarget = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, new ContentValues());
                OutputStream imageFileOS;
                try {
                    imageFileOS = getContentResolver().openOutputStream(uriTarget);
                    imageFileOS.write(arg0);
                    imageFileOS.flush();
                    imageFileOS.close();
                    Toast.makeText(MainActivity.this,
                            "Image saved: " + uriTarget.toString(),
                            Toast.LENGTH_LONG).show();
                    Log.v("SaveFilePath : ","FilePath : "+uriTarget.toString());
                } catch (FileNotFoundException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                camera.startPreview();
            }};




    Camera.AutoFocusCallback myAutoFocusCallback = new Camera.AutoFocusCallback(){

        @Override
        public void onAutoFocus(boolean arg0, Camera arg1) {
            // TODO Auto-generated method stu
            Camera.Parameters mParameters = camera.getParameters();
            mParameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
//        mParameters.setPictureSize(200,300);
            camera.setParameters(mParameters);
            BtnTakePicture.setEnabled(true);
            surfaceView.setBackground(getDrawable(R.drawable.border_lines_green));
        }};



    @Override
    public void surfaceCreated(@NonNull SurfaceHolder holder) {
        camera = Camera.open();
//        camera.autoFocus(myAutoFocusCallback);
//        setFocus(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
    }

    @Override
    public void surfaceChanged(@NonNull SurfaceHolder holder, int format, int width, int height) {
        if(previewing){
            camera.stopPreview();
            previewing = false;
        }

        if (camera != null){
            try {
                setUpCamera(camera);
                camera.setErrorCallback(new Camera.ErrorCallback() {

                    @Override
                    public void onError(int error, Camera camera) {
                        Toast.makeText(MainActivity.this, "Error Camera Not opening", Toast.LENGTH_SHORT).show();
                    }
                });
                camera.setPreviewDisplay(surfaceHolder);
                camera.startPreview();
                previewing = true;

            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    private void setUpCamera(Camera c) {
        Camera.CameraInfo info = new Camera.CameraInfo();
        Camera.getCameraInfo(cameraId, info);
        rotation = getWindowManager().getDefaultDisplay().getRotation();
        int degree = 0;
        switch (rotation) {
            case Surface.ROTATION_0:
                degree = 0;
                break;
            case Surface.ROTATION_90:
                degree = 90;
                break;
            case Surface.ROTATION_180:
                degree = 180;
                break;
            case Surface.ROTATION_270:
                degree = 270;
                break;

            default:
                break;
        }

        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            // frontFacing
            rotation = (info.orientation + degree) % 330;
            rotation = (360 - rotation) % 360;
        } else {
            // Back-facing
//            surfaceView.setBackgroundDrawable(getDrawable(R.drawable.border_lines_green));
            rotation = (info.orientation - degree + 360) % 360;
        }
        c.setDisplayOrientation(rotation);
        Camera.Parameters params = c.getParameters();

//        showFlashButton(params);

//        List<String> focusModes = params.getSupportedFlashModes();
//        if (focusModes != null) {
//            if (focusModes
//                    .contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
//                params.setFlashMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
//            }
//        }

        params.setRotation(rotation);
    }

    @Override
    public void surfaceDestroyed(@NonNull SurfaceHolder holder) {
        camera.stopPreview();
        camera.release();
        camera = null;
        previewing = false;
    }

    /** Ask Permissions  **/
    private void   askPerms() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "Permission Granted");
        } else {
            shouldShowRequestPermissionRationale(Manifest.permission.CAMERA);
        }

    }

    public void onResume(){
        super.onResume();
        BtnTakePicture.setEnabled(false);
        surfaceView.setBackground(getDrawable(R.drawable.border_lines_red));
    }


}











/**
 private void setFocus(String mParameter) {
 Camera.Parameters mParameters = camera.getParameters();
 mParameters.setFocusMode(mParameter);
 //        mParameters.setPictureSize(200,300);
 camera.setParameters(mParameters);
 }  **/
