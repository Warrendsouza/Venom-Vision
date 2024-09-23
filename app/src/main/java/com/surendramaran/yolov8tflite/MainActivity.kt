package com.surendramaran.yolov8tflite

import BoundingBox
import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Matrix
import android.location.Geocoder
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.AspectRatio
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.surendramaran.yolov8tflite.Constants.LABELS_PATH
import com.surendramaran.yolov8tflite.Constants.MODEL_PATH
import com.surendramaran.yolov8tflite.databinding.ActivityMainBinding
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import android.os.Environment
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.AppCompatImageButton
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.surendramaran.yolov8tflite.History.HistoryActivity
import java.io.File
import java.io.FileOutputStream
import java.util.Locale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.asDeferred
import kotlinx.coroutines.withContext
class MainActivity : AppCompatActivity(), Detector.DetectorListener {
    private lateinit var binding: ActivityMainBinding

    private lateinit var button : Button
    private lateinit var textView1 : TextView
    private lateinit var galleryButton : AppCompatImageButton

    private lateinit var historyButton: AppCompatImageButton

    private val isFrontCamera = false

    private var preview: Preview? = null
    private var imageAnalyzer: ImageAnalysis? = null
    private var camera: Camera? = null
    private var cameraProvider: ProcessCameraProvider? = null
    private lateinit var detector: Detector
    // var addressLocality : String?=null

    private lateinit var cameraExecutor: ExecutorService

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var geocoder : Geocoder


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        geocoder= Geocoder(this, Locale.getDefault())//init geocoder


        //getCurrentLocation()


        button = findViewById(R.id.button1)
        historyButton=findViewById(R.id.imageButton)
        historyButton.setOnClickListener {
            val intent= Intent(this@MainActivity,HistoryActivity::class.java)

            startActivity(intent)
        }
        galleryButton=findViewById(R.id.imageButton5)
        galleryButton.setOnClickListener{

            val locality = getCurrentLocation()
            if (locality != null) {
                val intent=Intent(this@MainActivity,SnakeDetection::class.java)

                startActivity(intent)
            }

        }

        button.setOnClickListener {
            if (bbxPoints.clabel == "snake") {
                val intent = Intent(this@MainActivity, classification::class.java)
                val locality = getCurrentLocation()
                if (locality != null) {
                    //Toast.makeText(this@MainActivity, locality, Toast.LENGTH_SHORT).show()
                    //intent.putExtra("addStr", locality)
                    startActivity(intent)
                } else {
                    Toast.makeText(this@MainActivity, "Unable to get location", Toast.LENGTH_SHORT).show()
                }
            } else {
                val intent2 = Intent(this@MainActivity, no_snake::class.java)
                startActivity(intent2)
            }
        }


        detector = Detector(baseContext, MODEL_PATH, LABELS_PATH, this)
        detector.setup()

        if (allPermissionsGranted()) {
            startCamera()
        } else {
            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS)
        }

        cameraExecutor = Executors.newSingleThreadExecutor()
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            cameraProvider  = cameraProviderFuture.get()
            bindCameraUseCases()
        }, ContextCompat.getMainExecutor(this))
    }

    private fun bindCameraUseCases() {
        val cameraProvider = cameraProvider ?: throw IllegalStateException("Camera initialization failed.")

        val rotation = binding.viewFinder.display.rotation

        val cameraSelector = CameraSelector
            .Builder()
            .requireLensFacing(CameraSelector.LENS_FACING_BACK)
            .build()

        preview =  Preview.Builder()
            .setTargetAspectRatio(AspectRatio.RATIO_4_3)
            .setTargetRotation(rotation)
            .build()

        imageAnalyzer = ImageAnalysis.Builder()
            .setTargetAspectRatio(AspectRatio.RATIO_4_3)
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .setTargetRotation(binding.viewFinder.display.rotation)
            .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888)
            .build()

        imageAnalyzer?.setAnalyzer(cameraExecutor) { imageProxy ->
            val bitmapBuffer =
                Bitmap.createBitmap(
                    imageProxy.width,
                    imageProxy.height,
                    Bitmap.Config.ARGB_8888
                )
            imageProxy.use { bitmapBuffer.copyPixelsFromBuffer(imageProxy.planes[0].buffer) }
            imageProxy.close()

            val matrix = Matrix().apply {
                postRotate(imageProxy.imageInfo.rotationDegrees.toFloat())

                if (isFrontCamera) {
                    postScale(
                        -1f,
                        1f,
                        imageProxy.width.toFloat(),
                        imageProxy.height.toFloat()
                    )
                }
            }

            val rotatedBitmap = Bitmap.createBitmap(
                bitmapBuffer, 0, 0, bitmapBuffer.width, bitmapBuffer.height,
                matrix, true
            )

            // Capture the last frame
            val scaleFactor = 0.60f // You can adjust the scale factor as needed
            val scaledBitmap = Bitmap.createScaledBitmap(rotatedBitmap, (rotatedBitmap.width * scaleFactor).toInt(), (rotatedBitmap.height * scaleFactor).toInt(), true)

            // Capture the last frame
            BitmapHolder.croppedBitmap = scaledBitmap.copy(scaledBitmap.config, true)

            detector.detect(rotatedBitmap)
        }

        cameraProvider.unbindAll()

        try {
            camera = cameraProvider.bindToLifecycle(
                this,
                cameraSelector,
                preview,
                imageAnalyzer
            )

            preview?.setSurfaceProvider(binding.viewFinder.surfaceProvider)
        } catch(exc: Exception) {
            Log.e(TAG, "Use case binding failed", exc)
        }
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(baseContext, it) == PackageManager.PERMISSION_GRANTED
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()) {
        if (it[Manifest.permission.CAMERA] == true) { startCamera() }
    }


    override fun onDestroy() {
        super.onDestroy()
        detector.clear()
        cameraExecutor.shutdown()
    }

    override fun onResume() {
        super.onResume()
        if (allPermissionsGranted()){
            startCamera()

        } else {
            requestPermissionLauncher.launch(REQUIRED_PERMISSIONS)
        }
    }

    companion object {
        private const val TAG = "Camera"
        private const val REQUEST_CODE_PERMISSIONS = 10
        private val REQUIRED_PERMISSIONS = mutableListOf (
            Manifest.permission.CAMERA
        ).toTypedArray()
    }

    override fun onEmptyDetect() {
        binding.overlay.invalidate()
    }
    fun getCurrentLocation(): String? {
        var addL: String? = ""


        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Request permissions if not granted
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION), 101)
        }

        fusedLocationClient.lastLocation
            .addOnSuccessListener { location ->
                if (location != null) {
                    val latitude = location.latitude
                    val longitude = location.longitude
                    // Handle the location data as needed
                    val address = geocoder.getFromLocation(latitude, longitude, 1)
                    if (address != null && address.isNotEmpty()) {
                        bbxPoints.locStrx=address[0].locality.toString()
                        addL = address[0].locality.toString()

                        Toast.makeText(this@MainActivity, addL, Toast.LENGTH_SHORT).show()

                    } else {
                        // Handle null address
                        Toast.makeText(this@MainActivity, "Null address", Toast.LENGTH_SHORT).show()

                    }
                } else {
                    // Handle null location
                    Toast.makeText(this@MainActivity, "Null location", Toast.LENGTH_SHORT).show()

                }
            }
            .addOnFailureListener { exception ->
                // Handle location request failure
                Toast.makeText(this@MainActivity, "location request failed", Toast.LENGTH_SHORT).show()

            }

        return addL
    }

    override fun onDetect(boundingBoxes: List<BoundingBox>, inferenceTime: Long) {

        runOnUiThread {
            binding.inferenceTime.text = "${inferenceTime}ms"
            binding.overlay.apply {
                setResults(boundingBoxes)
                invalidate()
            }

        }
    }
}
