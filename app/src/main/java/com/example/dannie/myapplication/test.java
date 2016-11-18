package com.example.dannie.myapplication;

import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

public class test extends AppCompatActivity {
    private static final String TAG = "test";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String path="sdcard/camera_app/cam_image.jpg";

        Log.d(TAG, BitmapFactory.decodeFile(path).getClass().toString() + "test-log");
    }

    //測試-----------------------------------------------
    private void fromtakepicture(byte[] byteArray) {
        //-----------------------------------------------------------

        final byte[] picture = byteArray;

        Log.d(TAG, picture.getClass().toString()+"yes~~");

        //-----------------------------------------------------------
    }
}
