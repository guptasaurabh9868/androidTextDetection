package com.machinelearning.guptas.imagetextdetector;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.nbsp.materialfilepicker.MaterialFilePicker;
import com.nbsp.materialfilepicker.ui.FilePickerActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.regex.Pattern;

import cz.msebera.android.httpclient.Header;


public class MainActivity extends AppCompatActivity {

    private static final int MY_CAMERA_PERMISSION_CODE = 10;
    private static final int CAMERA_REQUEST = 20;
    EditText ipaddress, port;
    TextView connectedText, imagepath, result_text;
    Button connectButton, filepickerButton, captureImage;
    ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
                .permitAll().build();
        StrictMode.setThreadPolicy(policy);

        ipaddress = findViewById(R.id.IP_address);
        port = findViewById(R.id.Port_address);
        connectedText = findViewById(R.id.connected);
        imagepath = findViewById(R.id.path);
        connectButton = findViewById(R.id.socketConnect);
        filepickerButton = findViewById(R.id.filepicker);
//        captureImage = findViewById(R.id.captureImage);
        imageView = findViewById(R.id.imageView);
        result_text = findViewById(R.id.result);

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M && checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) !=
                PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA}, 100);
        }


        filepickerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                new MaterialFilePicker()
                        .withActivity(MainActivity.this)
                        .withRequestCode(1000)
                        .withFilter(Pattern.compile(".*\\.jpg$")) // Filtering files and directories by file name using regexp
                        .withFilterDirectories(false) // Set directories filterable (false by default)
                        .withHiddenFiles(true) // Show hidden files and folders
                        .start();
            }
        });

//        captureImage.setOnClickListener(new View.OnClickListener() {
//
//            @Override
//            public void onClick(View v) {
//                Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
//                startActivityForResult(cameraIntent, CAMERA_REQUEST);
//            }
//        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d("HERE","onactivityresult");

        if (requestCode == 1000 && resultCode == RESULT_OK) {
            final String filePath = data.getStringExtra(FilePickerActivity.RESULT_FILE_PATH);
            // Do anything with file
            imagepath.setText(filePath);
            imagepath.setVisibility(View.VISIBLE);
            File imgFile = new File(imagepath.toString());
            if(imgFile.exists()){

                Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());

                ImageView myImage = findViewById(R.id.imageView);

                myImage.setImageBitmap(myBitmap);

            }

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    AsyncHttpClient client = new AsyncHttpClient();
                    final int DEFAULT_TIMEOUT = 180 * 1000;
                    client.setTimeout(DEFAULT_TIMEOUT);
                    RequestParams params = new RequestParams();

                    try {
                        params.put("image", new File(filePath));
                    } catch (IOException ex){
                        ex.printStackTrace();
                    }
//                    aa6aeabc
                    client.post("http://" + ipaddress.getText().toString().trim() + ".ngrok.io/image", params, new JsonHttpResponseHandler() {
                        @Override
                        public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                            // If the response is JSONObject instead of expected JSONArray
                            try {
                                boolean hasText = (boolean) response.get("hasText");
                                if(hasText)
                                    result_text.setText("Image Contains Text");
                                else
                                    result_text.setText("Image does not Contain Text");
                                result_text.setVisibility(View.VISIBLE);
                                System.out.println(hasText);
                            } catch (JSONException ex) {
                                ex.printStackTrace();
                            }

                        }

                        @Override
                        public void onSuccess(int statusCode, Header[] headers, JSONArray timeline) {
                            // Pull out the first event on the public timeline

                        }
                    });
                }
            }, 500);


            Log.d("HERE","filepath");

        }
        if (requestCode == CAMERA_REQUEST && resultCode == Activity.RESULT_OK) {
            Log.d("HERE","camerarequest");

            Bitmap photo = (Bitmap) data.getExtras().get("data");
            imageView.setImageBitmap(photo);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 100:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "Permission Granted!!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Permission Rejected!! Application may misbehave.", Toast.LENGTH_SHORT).show();
                    finish();
                }
                if (requestCode == MY_CAMERA_PERMISSION_CODE) {
                    if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                        Toast.makeText(this, "camera permission granted", Toast.LENGTH_LONG).show();
                        Intent cameraIntent = new
                                Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                        startActivityForResult(cameraIntent, CAMERA_REQUEST);
                    } else {
                        Toast.makeText(this, "camera permission denied", Toast.LENGTH_LONG).show();
                    }
                }
        }
    }
}
