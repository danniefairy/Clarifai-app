package com.example.dannie.myapplication.activity;


import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewSwitcher;

import com.example.dannie.myapplication.App;
import com.example.dannie.myapplication.ClarifaiUtil;
import com.example.dannie.myapplication.MainActivity;
import com.example.dannie.myapplication.R;

import java.io.ByteArrayOutputStream;
import java.text.DecimalFormat;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;
import clarifai2.api.ClarifaiResponse;
import clarifai2.dto.input.ClarifaiInput;
import clarifai2.dto.input.image.ClarifaiImage;
import clarifai2.dto.model.ConceptModel;
import clarifai2.dto.model.output.ClarifaiOutput;
import clarifai2.dto.prediction.Concept;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

public final class RecognizeConceptsActivity extends BaseActivity   {
    public static final int PICK_IMAGE = 100;
    Button back,next;
    ListView listView;
    int listnum=10;
    // the view where the image the user selected is displayed
    @BindView(R.id.image)
    ImageView imageView;

    // switches between the text prompting the user to hit the FAB, and the loading spinner
    @BindView(R.id.switcher)
    ViewSwitcher switcher;

    // the FAB that the user clicks to select an image
    @BindView(R.id.fab)
    View fab;

    @Override protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TextView imageinfo;
        imageinfo=(TextView)findViewById(R.id.imagetext);
        imageinfo.setText("請點選左上來進入您的相簿!");
        //拍照完加壓縮---------------------------------------
        String path="sdcard/camera_app/cam_image.jpg";
        final ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        Bitmap bitmap=BitmapFactory.decodeFile(path);
        Intent intent=this.getIntent();
        listnum=Integer.valueOf(intent.getStringExtra("listnum"));
        if(intent.getStringExtra("no photo")==null)
        {
            if(bitmap.getByteCount()>4000000)
                bitmap.compress(Bitmap.CompressFormat.JPEG, 20, outStream);
            else if(bitmap.getByteCount()>2000000&&bitmap.getByteCount()<4000000)
                bitmap.compress(Bitmap.CompressFormat.JPEG, 50, outStream);
            else
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outStream);
            Log.d(TAG, outStream.toByteArray().getClass().toString() + "takephoto-log");
            if(outStream.toByteArray()!=null)
                fromtakepicture(outStream.toByteArray());
        }
        //拍照完加壓縮---------------------------------------
        //返回拍照-------------------------------------------
        back=(Button)findViewById(R.id.button);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent back = new Intent(RecognizeConceptsActivity.this,MainActivity.class);
                startActivity(back);
            }
        });
        //返回拍照--------------------------------------------



    }

    //從相簿找-----------------------------------------------
    @OnClick(R.id.fab)
    void pickImage() {
        startActivityForResult(new Intent(Intent.ACTION_PICK).setType("image/*"), PICK_IMAGE);
    }

    @Override protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (resultCode != RESULT_OK) {
            return;
        }
        switch(requestCode) {
            case PICK_IMAGE:

                final byte[] imageBytes = ClarifaiUtil.retrieveSelectedImage(this, data);

                if (imageBytes != null) {
                    Intent intent=this.getIntent();
                    listnum=Integer.valueOf(intent.getStringExtra("listnum"));
                    onImagePicked(imageBytes);
                }
                break;
        }

    }
    //從相簿找-----------------------------------------------
    //拍完照影像call onImagePicked---------------------------
    private void fromtakepicture(byte[] byteArray){

        TextView imageinfo;
        imageinfo=(TextView)findViewById(R.id.imagetext);
        imageinfo.setText("影像辨認中，請稍後......");
        final byte[] picture =byteArray;
        if (picture != null) {
            onImagePicked(picture);
        }
    }
    //拍完照影像call onImagePicked---------------------------
    private void onImagePicked(@NonNull final byte[]  imageBytes) {
        // Now we will upload our image to the Clarifai API
        setBusy(true);
        TextView imageinfo;
        imageinfo=(TextView)findViewById(R.id.imagetext);
        imageinfo.setText("影像辨認中，請稍後......");
        // Make sure we don't show a list of old concepts while the image is being uploaded

        Log.d(TAG,"APP.GET");
        new AsyncTask<Void, Void, ClarifaiResponse<List<ClarifaiOutput<Concept>>>>() {
            @Override protected ClarifaiResponse<List<ClarifaiOutput<Concept>>> doInBackground(Void... params) {
                // The default Clarifai model that identifies concepts in images
                final ConceptModel generalModel = App.get().clarifaiClient().getDefaultModels().generalModel();

                // Use this model to predict, with the image that the user just selected as the input
                return generalModel.predict()
                        .withInputs(ClarifaiInput.forImage(ClarifaiImage.of(imageBytes)))
                        .executeSync();
            }

            @Override protected void onPostExecute(ClarifaiResponse<List<ClarifaiOutput<Concept>>> response) {
                setBusy(false);
                if (!response.isSuccessful()) {
                    showErrorSnackbar(R.string.error_while_contacting_api);
                    return;
                }
                final List<ClarifaiOutput<Concept>> predictions = response.get();
                if (predictions.isEmpty()) {
                    showErrorSnackbar(R.string.no_results_from_api);
                    Log.d(TAG, "NO RESULT -------------------------------------------");
                    return;
                }

                Log.d(TAG, predictions.get(0).data().getClass().toString() + "information-prediction");
                //顯示list------------------------------------------------
                listView = (ListView) findViewById(R.id.listView);
                final String [][] data=new String[listnum][2];
                String [] array=new String [listnum];
                for(int i=0;i<listnum;i++)
                {
                    DecimalFormat value=new DecimalFormat("#.###");
                    String dv=value.format(predictions.get(0).data().get(i).value());
                    data[i][0]=predictions.get(0).data().get(i).name();
                    data[i][1]=dv;
                    array[i]=data[i][0]+"\n"+data[i][1];
                }
                ArrayAdapter<String> adapter = new ArrayAdapter<String>(RecognizeConceptsActivity.this, android.R.layout.simple_list_item_multiple_choice,array);
                listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
                listView.setAdapter(adapter);
                //顯示list------------------------------------------------


                //輸出勾選值----------------------------------------------
                next=(Button)findViewById(R.id.next);
                next.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        //Log.d(TAG, String.valueOf(listView.getCheckedItemPositions().get(0)) + "choice 1");
                        int count = 0;
                        for (int i = 0; i < listnum; i++) {
                            if (listView.getCheckedItemPositions().get(i)) {
                                Log.d(TAG, data[i][0]);//目前以log顯示
                                count++;
                            }

                        }
                        if (count == 0)
                            Toast.makeText(RecognizeConceptsActivity.this, "請至少勾選一項", Toast.LENGTH_SHORT).show();
                        else
                            Log.d(TAG,"這裡輸出到新的activity");

                    }

                });
                //輸出勾選值----------------------------------------------
                //顯示相片------------------------------------------------
                imageView.setImageBitmap(BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length));
                TextView imageinfo;
                imageinfo=(TextView)findViewById(R.id.imagetext);
                imageinfo.setText("");
                //顯示相片------------------------------------------------
            }

            private void showErrorSnackbar(@StringRes int errorString) {
                Snackbar.make(
                        root,
                        errorString,
                        Snackbar.LENGTH_INDEFINITE
                ).show();
            }
        }.execute();
    }



    @Override protected int layoutRes() { return R.layout.activity_recognize_concepts; }
    private static final String TAG="RecognizeActivity";
    private void setBusy(final boolean busy) {
        Log.d(TAG, "recognize~~~");
        runOnUiThread(new Runnable() {

            @Override public void run() {

                switcher.setDisplayedChild(busy ? 1 : 0);
                imageView.setVisibility(busy ? GONE : VISIBLE);
                fab.setEnabled(!busy);
            }
        });
    }

}