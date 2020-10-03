package com.example.movemusic;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Mat;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "move music";
    private Bitmap bitmap;
    private ImageView imageView;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQ_CODE_SELECT_IMAGE) {
            if (resultCode == RESULT_OK) {
                try {
                    String path = getImagePathFromURI(data.getData());

                    BitmapFactory.Options options = new BitmapFactory.Options();
                    options.inSampleSize = 4;
                    bitmap = BitmapFactory.decodeFile(path, options);

                    if (bitmap != null) {
                        detectEdge();
                        imageView.setImageBitmap(bitmap);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void detectEdge() {
        Mat src = new Mat();
        Utils.bitmapToMat(bitmap, src);

        Mat edge = new Mat();
        nativeDetectAndDisplay(src.getNativeObjAddr(), edge.getNativeObjAddr());

        Utils.matToBitmap(edge, bitmap);
        imageView.setImageBitmap(bitmap);

        src.release();
        edge.release();
    }

    private String getImagePathFromURI(Uri data) {
        String[] proj = {MediaStore.Images.Media.DATA};
        Cursor cursor = getContentResolver().query(data, proj, null, null, null);
        if (cursor == null) {
            return data.getPath();
        } else {
            int idx = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            String imgPath = cursor.getString(idx);
            cursor.close();
            return imgPath;
        }
    }

    private static final int PERMISSIONS_REQUEST_CODE = 100;
    private static final int REQ_CODE_SELECT_IMAGE = 101;
    String[] PERMISSIONS = {"android.permission.READ_EXTERNAL_STORAGE"};

    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("native-lib");
        
        if (OpenCVLoader.initDebug()) {
            Log.d(TAG, "static initializer: OpenCV is loaded successfully!");
        } else {
            Log.d(TAG, "static initializer: OpenCV is not loaded!");
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        bitmap.recycle();
        bitmap = null;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!hasPermissions(PERMISSIONS)) {
                requestPermissions(PERMISSIONS, PERMISSIONS_REQUEST_CODE);
            }
        }

        // Example of a call to a native method
        TextView tv = findViewById(R.id.sample_text);
        tv.setText(stringFromJNI());

        imageView = findViewById(R.id.imageView);
    }

    private boolean hasPermissions(String[] permissions) {
        int result;

        for (String perms : permissions){
            result = ContextCompat.checkSelfPermission(this, perms);

            if (result == PackageManager.PERMISSION_DENIED){
                return false;
            }
        }

        return true;
    }

    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    public native String stringFromJNI();
    public native void nativeDetectAndDisplay(long addrMat, long dstMat);

    public void buttonClicked(View view) {
        // --- Select Platform ---
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.select_platform)
                .setItems(R.array.platforms_array, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // The 'which' argument contains the index position
                        // of the selected item
                        Resources res = getResources();
                        String[] platforms = res.getStringArray(R.array.platforms_array);
                        Toast.makeText(MainActivity.this, platforms[which], Toast.LENGTH_SHORT).show();

                        SharedPreferences sharedPref = getApplicationContext().getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPref.edit();
                        editor.putString(getString(R.string.saved_platform_key), platforms[which]);
                        editor.commit();

                        // --- Select Picture ---
                        Intent intent = new Intent(Intent.ACTION_PICK);
                        intent.setType(android.provider.MediaStore.Images.Media.CONTENT_TYPE);
                        intent.setData(android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                        startActivityForResult(intent, REQ_CODE_SELECT_IMAGE);
                    }
                });
        builder.show();
    }
}
