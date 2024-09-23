package com.surendramaran.yolov8tflite


import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.AssetManager
import android.graphics.Bitmap
import android.location.Geocoder
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.io.ByteArrayOutputStream
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

class classification : AppCompatActivity() {
    val mInputSize = 224
    val mModelPath = "model_snake.tflite"
    val mLabelPath = "snakes_labels.txt"
    private lateinit var results1: Classifier.Recognition
    //val mAssetManager: AssetManager =getApplicationContext().getAssets(); // Retrieve AssetManager
    private lateinit var mAssetManager: AssetManager
    private var mClassifier: Classifier? = null
    //val mSamplePath = "diagram.png"
    private  var mDatabase: DatabaseReference? = null
    private var mStorageRef: StorageReference? = null
    private lateinit var button : Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_classification)
       // val stringValue = intent.getStringExtra("addressString")
       // addressLocality=stringValue
        val imageView = findViewById<ImageView>(R.id.imageView)
        //val textview = findViewById<TextView>(R.id.textView2)
        val nameCommon = findViewById<TextView>(R.id.nameCommon)
        val familyName = findViewById<TextView>(R.id.familyName)
        val bioName = findViewById<TextView>(R.id.bioName)
        val typeNature=findViewById<TextView>(R.id.typeNature)
        val confValue = findViewById<TextView>(R.id.accuracyValue)
        mAssetManager = assets
                                          //to upload data to server
        button = findViewById(R.id.buttonUpload)
        button.setOnClickListener{
            storeDataInFirebase(results1)
                }

        //getCurrentLocation()
        // Initialize Firebase Database
        mDatabase = FirebaseDatabase.getInstance().getReference("snake_detection_data")
        // Initialize Firebase Storage
        // Initialize Firebase Storage
        mStorageRef = FirebaseStorage.getInstance().reference
        //val cpBitmap = BitmapHolder.croppedBitmap//croppedBitmap
        mClassifier = Classifier(mAssetManager, mModelPath, mLabelPath, mInputSize)
        val stringValue = intent.getStringExtra("addStr")
        val cpBitmap = finalBitmap.croppedBitmap//region extracted bitmap
        if (cpBitmap != null) {
            imageView.setImageBitmap(cpBitmap)
            val results = if (mClassifier!!.recognizeImage(cpBitmap)
                    .isEmpty()
            ) null else mClassifier!!.recognizeImage(cpBitmap)[0]
            if (results == null || results.confidence < 0.40) {
                // mResultTextView.setText("Snake not detected!");
                println("no snake")
                Toast.makeText(this@classification, "confidence score was less than the  Expected TRESHOLD limit", Toast.LENGTH_SHORT).show()
                val intent2 = Intent(this@classification,no_snake::class.java)
                startActivity(intent2)
            }
            else{
                results1=results
                //textview.setText(results.title)
                val input = results1.title
                val str = input.split(",").toTypedArray()
                nameCommon.setText(str[0])
                bioName.setText(str[1])
                typeNature.setText(str[2])
                familyName.setText(str[3])
                confValue.setText(results1.confidence.toString())
                results1.title=str[0]
                //Toast.makeText(this@classification, stringValue, Toast.LENGTH_SHORT).show()

            }
        } else {
            Log.e("ClassificationActivity", "Failed to retrieve cropped image")
        }

    }
    private fun storeDataInFirebase(results: Classifier.Recognition) {
        val stringValue = intent.getStringExtra("addStr") ?: ""
        val key = mDatabase!!.push().key
        if (key != null) {
            // Create a map to store the data
            val snakeData: MutableMap<String, Any> = HashMap()
            snakeData["title"] = results.title
            snakeData["confidence"] = results.confidence.toString()
            snakeData["location"]=bbxPoints.locStrx.toString()
           // Toast.makeText(this@classification, snakeData["location"].toString(), Toast.LENGTH_SHORT).show()

            // Upload image to Firebase Storage and store data
            uploadImageToStorageAndStoreData(key, snakeData)
        }
    }
    private fun getDownloadUrl(key: String, snakeData: MutableMap<String, Any>) {
        val imageRef = mStorageRef?.child("images/$key.jpg")
        imageRef?.downloadUrl?.addOnSuccessListener { uri ->
            val updatedSnakeData = snakeData.toMutableMap()
            updatedSnakeData["imageUrl"] = uri.toString()
            storeDataInDatabase(key, updatedSnakeData)
        }?.addOnFailureListener { e ->
            Toast.makeText(this@classification, "Error retrieving image URL", Toast.LENGTH_SHORT).show()
        }
    }
    private fun storeDataInDatabase(key: String, snakeData: MutableMap<String, Any>) {
        mDatabase?.child(key)?.setValue(snakeData)
            ?.addOnSuccessListener {
                Toast.makeText(this@classification, "Data stored successfully", Toast.LENGTH_SHORT).show()
            }
            ?.addOnFailureListener { e ->
                Toast.makeText(this@classification, "Error storing data", Toast.LENGTH_SHORT).show()
            }
    }
    private fun uploadImageToStorageAndStoreData(key: String, snakeData: MutableMap<String, Any>) {
        // Convert bitmap to byte array
        val baos = ByteArrayOutputStream()
        val cpBitmap = finalBitmap.croppedBitmap
        if (cpBitmap != null) {
            cpBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
            val data = baos.toByteArray()

            // Create a reference to the location where you want to save the image in Firebase Storage
            val imageRef = mStorageRef?.child("images/$key.jpg")

            // Upload the image to Firebase Storage
            if (imageRef != null) {
                val uploadTask = imageRef.putBytes(data)
                uploadTask.addOnSuccessListener { taskSnapshot ->
                    getDownloadUrl(key, snakeData)
                }.addOnFailureListener { e ->
                    Toast.makeText(this@classification, "Image Upload Failed", Toast.LENGTH_SHORT).show()
                }
            }


        } else {
            Log.e("ClassificationActivity", "Failed to retrieve cropped image")
        }

    }

}