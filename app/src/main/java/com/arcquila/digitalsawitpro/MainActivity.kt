package com.arcquila.digitalsawitpro

import android.app.Activity
import android.app.ProgressDialog
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.view.Menu
import android.widget.PopupMenu
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.arcquila.digitalsawitpro.MainActivity.Companion.STORAGE_REQUEST_CODE
import com.arcquila.digitalsawitpro.databinding.ActivityMainBinding
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.TextRecognizer
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private lateinit var _binding : ActivityMainBinding
    val binding get() = _binding

    private companion object{
        private const val CAMERA_REQUEST_CODE = 100
        private const val STORAGE_REQUEST_CODE = 100
    }

    private var imageUri: Uri? = null

    private lateinit var cameraPermissions: Array<String>
    private lateinit var storagePermissions: Array<String>

    private lateinit var progressDialog: ProgressDialog


    private lateinit var textRecognizer: TextRecognizer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        cameraPermissions = arrayOf(android.Manifest.permission.CAMERA, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
        storagePermissions = arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)

        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Please Wait")
        progressDialog.setCanceledOnTouchOutside(false)

        textRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

        binding.inputImageBtn.setOnClickListener {
            showInputDialog()
        }



        val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
        binding.recognizeTextBtn.setOnClickListener {
            if(imageUri == null ){
                showToast("Pick Image First...")
            } else {
                recognizeTextFromimage()
            }
        }

    }

    private fun recognizeTextFromimage() {
        progressDialog.setMessage("Preparing Image..")
        progressDialog.show()

        try{
            val input = InputImage.fromFilePath(this, imageUri!!)
            progressDialog.setMessage("Recognizing text.....")
            val textTaskResult = textRecognizer.process(input).addOnSuccessListener {
                progressDialog.dismiss()
                val recognizedText = it.text
                binding.recognizedTextEt.setText(recognizedText)
            }.addOnFailureListener { e ->
                progressDialog.dismiss()
                showToast("Failed to recognize text due ${e.message}")
            }

        } catch (e: Exception){
            progressDialog.dismiss()
            showToast("Failed to prepare image due to ${e.message}")
        }
    }

    private fun showInputDialog() {
        val popUpMenu = PopupMenu(this, inputImageBtn)
        popUpMenu.menu.add(Menu.NONE,1,1,"CAMERA")
        popUpMenu.menu.add(Menu.NONE,2,2,"GALLERY")
        popUpMenu.show()

        popUpMenu.setOnMenuItemClickListener { menuItem ->
            val id = menuItem.itemId
            if(id == 1){
                if(checkCameraPermisison()){
                    pickImageCamera()
                }else{
                    requestCameraPermissions()
                }
            } else if( id ==2){
                if(checkStoragePermisssion()){
                    pickImageFromGallery()
                } else{
                    requestStoragePermission()
                }
            }

            return@setOnMenuItemClickListener true
        }
    }

    private fun pickImageFromGallery()
    {
        val intent = Intent(Intent.ACTION_PICK)

        intent.type = "image/*"
        galleryActivityResultLauncher.launch(intent)

    }

    private val galleryActivityResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){ result ->
        if(result .resultCode == Activity.RESULT_OK){
            val data = result .data
            imageUri = data!!.data
            binding.imageIv.setImageURI(imageUri)

        }else{
            showToast("Cancelled")
        }
    }

    private fun pickImageCamera(){
        val values = ContentValues()
        values.put(MediaStore.Images.Media.TITLE, "Sample Title")
        values.put(MediaStore.Images.Media.DESCRIPTION, "Sample Description")

        imageUri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)

        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
        cameraActivityReultLauncher.launch(intent)

    }

    private val cameraActivityReultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()){ reslt ->
            if (reslt.resultCode == Activity.RESULT_OK){
                binding.imageIv.setImageURI(imageUri)

            }else{
                showToast("Cancelled")
            }
        }

    private fun checkStoragePermisssion(): Boolean{
        return ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
    }

    private fun checkCameraPermisison(): Boolean{
        val camereraResult = ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        val storageResult = ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED

        return camereraResult && storageResult

    }

    private fun requestStoragePermission(){
        ActivityCompat.requestPermissions(this, storagePermissions, STORAGE_REQUEST_CODE)
    }

    private fun requestCameraPermissions(){
        ActivityCompat.requestPermissions(this, cameraPermissions, CAMERA_REQUEST_CODE)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when(requestCode){
            CAMERA_REQUEST_CODE -> {
                if(grantResults.isNotEmpty()){
                    val cameraAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED
                    val storageAccepted = grantResults[1] == PackageManager.PERMISSION_GRANTED

                    if(cameraAccepted && storageAccepted){
                        pickImageCamera()
                    } else{
                        showToast("Camera & Storage permission are requiredd..")
                    }
                }
            }
            STORAGE_REQUEST_CODE ->{
                if(grantResults.isNotEmpty()){
                    val storageAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED

                    if(storageAccepted){
                        pickImageFromGallery()
                    }else{
                        showToast("Storage permission is required")
                    }

                }

            }
        }

    }

    private fun showToast(message: String){
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()

    }
}