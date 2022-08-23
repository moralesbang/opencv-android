package com.royk.appcpp;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceView;
import android.view.WindowManager;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraActivity;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;

public class MainActivitycpp extends CameraActivity {

    private static String LOGCAT = "OpenCV_Log";
    private CameraBridgeViewBase mOpenCvCameraView;
    private File cascadeFile;

    static {
        System.loadLibrary("appcpp");
    }

    public native void FindFeatures(long addrGray, long addrRGB);
    public native void InitFaceDetector(String filePath);
    public native void DetectFaces(long addrGray, long addrRGB);

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS: {
                    Log.v(LOGCAT, "OpenCV Loaded");
                    mOpenCvCameraView.enableView();
                }
                break;
                default: {
                    super.onManagerConnected(status);
                }
                break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_main_activitycpp);

        try{
            cascadeFile = new File(getCacheDir(), "haarcascade_frontalface_default.xml");
            if(!cascadeFile.exists()){
                InputStream inputStream = getAssets().open("haarcascade_frontalface_default.xml");
                FileOutputStream outputStream = new FileOutputStream(cascadeFile);
                byte[] buffer = new byte[2048];
                int bytesRead = -1;
                while ((bytesRead = inputStream.read(buffer)) != -1){
                    outputStream.write(buffer, 0, bytesRead);
                }
                inputStream.close();
                outputStream.close();
            }
            InitFaceDetector(cascadeFile.getAbsolutePath());
        }catch (IOException e){
            e.printStackTrace();
        }

        mOpenCvCameraView = (CameraBridgeViewBase) findViewById(R.id.opencv_surface_view);
        mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);
        mOpenCvCameraView.setCvCameraViewListener(cvCameraViewListener);
    }

    @Override
    protected List<?extends CameraBridgeViewBase> getCameraViewList() {
        return Collections.singletonList(mOpenCvCameraView);
    }

    private CameraBridgeViewBase.CvCameraViewListener2 cvCameraViewListener = new CameraBridgeViewBase.CvCameraViewListener2() {
        @Override
        public void onCameraViewStarted(int width, int height) {

        }

        @Override
        public void onCameraViewStopped() {

        }

        @Override
        public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
            Mat inputRgba = inputFrame.rgba();
            Mat inputGray = inputFrame.gray();

            // Process the frame in cpp
            //FindFeatures(inputGray.getNativeObjAddr(), inputRgba.getNativeObjAddr());
            DetectFaces(inputGray.getNativeObjAddr(), inputRgba.getNativeObjAddr());

            return inputRgba;
        }
    };

    @Override
    public void onResume() {
        super.onResume();
        if (!OpenCVLoader.initDebug()) {
            Log.d(LOGCAT, "OpenCV Not Found, Initializing...");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION, this, mLoaderCallback);
        } else {
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if(mOpenCvCameraView != null) {
            mOpenCvCameraView.disableView();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(mOpenCvCameraView != null) {
            mOpenCvCameraView.disableView();
        }
    }
}