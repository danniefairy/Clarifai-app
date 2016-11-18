package com.example.dannie.myapplication;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.example.dannie.myapplication.activity.RecognizeConceptsActivity;

import java.io.File;

public class MainActivity extends Activity {
    Button button,go;
    EditText num;
    static final int CAM_REQUEST=1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //檢查相機是否可行-------------------------
        if (checkSelfPermission(Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {

            requestPermissions(new String[]{Manifest.permission.CAMERA},
                    CAM_REQUEST);
        }
        //檢查相機是否可行-------------------------
        //拍照-------------------------------------
        button=(Button)findViewById(R.id.button);

        button.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                Intent camera_intent =new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                File file=getFile();
                camera_intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(file));
                startActivityForResult(camera_intent, CAM_REQUEST);
            }
        });
        //拍照-------------------------------------
        //前往下一頁-------------------------------
        go=(Button)findViewById(R.id.go);
        go.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                Intent i = new Intent(MainActivity.this,RecognizeConceptsActivity.class);
                String nophoto="no photo";
                i.putExtra("no photo",nophoto);
                String listnum;
                num=(EditText)findViewById(R.id.listnum);
                listnum=num.getText().toString();

                if("".equals(num.getText().toString().trim())){
                    i.putExtra("listnum", "10");
                }
                else{
                    if(Integer.valueOf(listnum)>0 && Integer.valueOf(listnum)<=20 )
                    {
                        i.putExtra("listnum", listnum);
                    }
                    else
                    {
                        i.putExtra("listnum", "10");
                    }
                }
                startActivity(i);
            }
        });
        //前往下一頁-------------------------------
    }
    //創資料夾存圖檔-------------------------------
    private File getFile(){
        File folder=new File("sdcard/camera_app");
        if(!folder.exists())
        {
            folder.mkdir();
        }
        File image_file= new File(folder,"cam_image.jpg");

        return image_file;
    }
    //創資料夾存圖檔-------------------------------
    //Log------------------------------------------
    private static final String TAG="MainActivity";
    //Log------------------------------------------
    //拍照-----------------------------------------
    @Override
    protected void onActivityResult(int requestCode,int resultCode,Intent data){
        //super.onActivityResult(requestCode,resultCode,data);
        String path="sdcard/camera_app/cam_image.jpg";
        Log.d(TAG, BitmapFactory.decodeFile(path).getClass().toString() + "MainActivity-log");
        Intent i = new Intent(MainActivity.this,RecognizeConceptsActivity.class);
        String listnum;
        num=(EditText)findViewById(R.id.listnum);
        listnum=num.getText().toString();
        if("".equals(num.getText().toString().trim())){
            i.putExtra("listnum", "10");
        }
        else{
            if(Integer.valueOf(listnum)>0 && Integer.valueOf(listnum)<=20 )
            {
                i.putExtra("listnum", listnum);
            }
            else
            {
                i.putExtra("listnum", "10");
            }
        }
        startActivity(i);
    }
    //拍照-----------------------------------------
}
