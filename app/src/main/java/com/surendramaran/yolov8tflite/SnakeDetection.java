package com.surendramaran.yolov8tflite;


import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;

public class SnakeDetection extends AppCompatActivity {

    private Bitmap mBitmap;
    private final int mCameraRequestCode = 0;
    private final int mGalleryRequestCode = 2;
    private final int mInputSize = 224;

    ImageView mGalleryButton, mCameraButton, mPhotoImageView;
    Button mDetectButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_snake_detection);

        mPhotoImageView = findViewById(R.id.mPhotoImageView);

        mCameraButton = findViewById(R.id.mCameraButton);
        mCameraButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if (cameraIntent.resolveActivity(getPackageManager()) != null) {
                    startActivityForResult(cameraIntent, mCameraRequestCode);
                } else {
                    Toast.makeText(SnakeDetection.this, "Camera is not available", Toast.LENGTH_SHORT).show();
                }
            }
        });

        mGalleryButton = findViewById(R.id.mGalleryButton);
        mGalleryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent callGalleryIntent = new Intent(Intent.ACTION_PICK);
                callGalleryIntent.setType("image/*");
                startActivityForResult(callGalleryIntent, mGalleryRequestCode);
            }
        });

        mDetectButton = findViewById(R.id.mDetectButton);
        mDetectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mBitmap != null) {
                    BitmapHolder.croppedBitmap = mBitmap;
                    Intent intent = new Intent(SnakeDetection.this, classification.class);
                    startActivity(intent);
                } else {
                    Toast.makeText(SnakeDetection.this, "Please select or capture an image first", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == mCameraRequestCode) {
            if (resultCode == Activity.RESULT_OK && data != null) {
                mBitmap = (Bitmap) data.getExtras().get("data");
                mBitmap = scaleImage(mBitmap);
                Toast toast = Toast.makeText(this, "Image crop to: w= " + mBitmap.getWidth() + " h= " + mBitmap.getHeight(), Toast.LENGTH_LONG);
                toast.setGravity(Gravity.BOTTOM, 0, 20);
                toast.show();
                mPhotoImageView.setImageBitmap(mBitmap);
            } else {
                Toast.makeText(this, "Camera cancel..", Toast.LENGTH_LONG).show();
            }
        } else if (requestCode == mGalleryRequestCode) {
            if (data != null) {
                try {
                    mBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), data.getData());
                } catch (IOException e) {
                    e.printStackTrace();
                }
                mBitmap = scaleImage(mBitmap);
                finalBitmap.croppedBitmap=mBitmap;
                mPhotoImageView.setImageBitmap(mBitmap);
            }
        } else {
            Toast.makeText(this, "Unrecognized request code", Toast.LENGTH_LONG).show();
        }
    }

    private Bitmap scaleImage(Bitmap bitmap) {
        int originalWidth = bitmap.getWidth();
        int originalHeight = bitmap.getHeight();
        float scaleWidth = (float) mInputSize / originalWidth;
        float scaleHeight = (float) mInputSize / originalHeight;
        Matrix matrix = new Matrix();
        matrix.postScale(scaleWidth, scaleHeight);
        return Bitmap.createBitmap(bitmap, 0, 0, originalWidth, originalHeight, matrix, true);
    }
}