package com.moralesbang.opencvandroid;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceView;
import android.view.WindowManager;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraActivity;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfRect;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;

public class MainActivity extends CameraActivity {

    private static String LOGCAT = "OpenCV_Log";
    private CameraBridgeViewBase mOpenCvCameraView;
    File cascFile;

    CascadeClassifier faceDetector;

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS: {
                    Log.v(LOGCAT, "OpenCV Loaded");
                    InputStream is = getResources().openRawResource(R.raw.haarcascade_frontalface_alt2);
                    File cascadeDir = getDir("cascade", Context.MODE_PRIVATE);

                    cascFile = new File(cascadeDir, "haarcascade_frontalface_alt.xml");

                    try{
                        FileOutputStream fos = new FileOutputStream(cascFile);

                        byte[] buffer = new byte[4096];
                        int bytesRead;

                        while((bytesRead = is.read(buffer)) != -1){
                            fos.write(buffer, 0, bytesRead);
                        }

                        is.close();
                        fos.close();
                    }catch (Exception e){
                        Log.v(LOGCAT, e.getStackTrace().toString());
                    }

                    faceDetector = new CascadeClassifier(cascFile.getAbsolutePath());

                    if(faceDetector.empty()){
                        faceDetector = null;
                    }
                    else{
                        cascadeDir.delete();
                    }

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
        setContentView(R.layout.activity_main);

        mOpenCvCameraView = (CameraBridgeViewBase) findViewById(R.id.opencv_surface_view);
        mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);
        mOpenCvCameraView.setCvCameraViewListener(cvCameraViewListener);
    }

    @Override
    protected List<? extends CameraBridgeViewBase> getCameraViewList() {
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

            /*MatOfPoint corners = new MatOfPoint();
            Imgproc.goodFeaturesToTrack(inputGray, corners, 20, 0.01,
                    10, new Mat(), 3, false);
            Point[] cornersArr = corners.toArray();

            for (int i = 0; i < corners.rows(); i++) {
                Imgproc.circle(inputRgba, cornersArr[i], 10, new Scalar(0, 255, 0), 2);
            }*/

            MatOfRect faceDetections = new MatOfRect();
            faceDetector.detectMultiScale(inputRgba, faceDetections);

            for(Rect rect: faceDetections.toArray()){
                Imgproc.rectangle(inputRgba, new Point(rect.x, rect.y),
                        new Point(rect.x + rect.width, rect.y +rect.height),
                        new Scalar(255,0,0));
            }
            //Core.flip(inputRgba.t(), inputRgba, 0);
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
        if (mOpenCvCameraView != null) {
            mOpenCvCameraView.disableView();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mOpenCvCameraView != null) {
            mOpenCvCameraView.disableView();
        }
    }

}