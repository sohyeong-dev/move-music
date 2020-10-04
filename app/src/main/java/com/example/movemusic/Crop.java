package com.example.movemusic;

import android.util.Log;

import org.opencv.android.OpenCVLoader;
import org.opencv.core.Mat;

import java.util.ArrayList;
import java.util.Arrays;

public class Crop {
    private static final String TAG = "Crop class";

    ArrayList<Mat> cropedList;  // 이미지 분석 결과

    public Crop() {
        this.cropedList = new ArrayList<>();
    }

    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("native-lib");

        if (OpenCVLoader.initDebug()) {
            Log.d(TAG, "static initializer: OpenCV is loaded successfully!");
        } else {
            Log.d(TAG, "static initializer: OpenCV is not loaded!");
        }
    }

    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    public native Mat[] cropingJNI(String imgPath);

    // 이미지 분석
    public ArrayList<Mat> croping(String imgPath) {
        Mat[] tempMatArray = cropingJNI(imgPath);
        Log.d(TAG, "croping: " + tempMatArray.length);

        cropedList.addAll(Arrays.asList(tempMatArray));
        Log.d(TAG, "croping: 곡의 개수는? " + cropedList.size() / 2);

        return cropedList;
    }
}
