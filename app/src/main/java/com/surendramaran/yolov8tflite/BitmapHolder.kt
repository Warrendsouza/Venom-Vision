package com.surendramaran.yolov8tflite

import android.graphics.Bitmap

class BitmapHolder {
    var croppedBitmap: Bitmap? = null

    companion object {
        lateinit var croppedBitmap: Bitmap
    }
}