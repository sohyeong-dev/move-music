package com.example.movemusic;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;

import org.opencv.android.Utils;
import org.opencv.core.Mat;

import java.util.ArrayList;

public class ProcessActivity extends AppCompatActivity {

    private static final String TAG = "ProcessActivity";

    ArrayList<Mat> cropedList;  // 이미지 분석 결과
    ArrayList<Mat> albumimages; // 앨범 이미지 영역
    ArrayList<Mat> ocrimages;   // 곡, 아티스트 문자 영역

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_process);

        albumimages = new ArrayList<>();
        ocrimages = new ArrayList<>();

        // --- selected platform ---
        SharedPreferences sharedPref = getApplicationContext().getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        String platform = sharedPref.getString(getString(R.string.saved_platform_key), "");
        Log.d(TAG, "onCreate: " + platform);

        // --- selected picture ---
        Intent processIntent = getIntent();
        String imgPath = processIntent.getStringExtra("imgPath");
        Log.d(TAG, "onCreate: " + imgPath);

        // --- 플레이리스트의 레이아웃 형태 분석 ---
        Crop crop = new Crop();
        cropedList = crop.croping(imgPath);
        Log.d(TAG, "onCreate: 곡의 개수는? " + cropedList.size() / 2);

        for (int i = 0; i < cropedList.size(); i += 2) {
            albumimages.add(cropedList.get(i));
            ocrimages.add(cropedList.get(i + 1));
        }

        // --- OCR ---

        ArrayList<Bitmap> ocrBitmapImages = new ArrayList<>();
        // Mat to Bitmap
        Bitmap bmp;
        for (Mat ocrimage: ocrimages) {
            bmp = Bitmap.createBitmap(ocrimage.cols(), ocrimage.rows(), Bitmap.Config.RGB_565);
            Utils.matToBitmap(ocrimage, bmp);
            ocrBitmapImages.add(bmp);
        }

        Recognize recognize = new Recognize(this, ocrBitmapImages);

        ArrayList<String> results = recognize.ocr();
        Log.d(TAG, "onCreate: " + results.size());
        for (String keyword: results) {
            Log.d(TAG, "onCreate: " + keyword);
        }
    }
}