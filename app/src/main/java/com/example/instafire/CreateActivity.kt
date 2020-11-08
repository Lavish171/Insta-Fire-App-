package com.example.instafire

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.example.instafire.models.Post
import com.example.instafire.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.android.synthetic.main.activity_create.*
import java.net.URI

private  const val TAG="CreateActivity"
private  const val PICK_PHOTO_CODE=1234

class CreateActivity : AppCompatActivity() {

    private var photoUri: Uri?=null
    private var signedUser: User?=null
    private lateinit var  firestoreDb:FirebaseFirestore
    private  lateinit var storageReference:StorageReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create)

        storageReference=FirebaseStorage.getInstance().reference

        firestoreDb= FirebaseFirestore.getInstance()

        firestoreDb.collection("users")
            .document(FirebaseAuth.getInstance().currentUser?.uid as String)
            .get()
            .addOnSuccessListener {documentSnapshot ->
                signedUser=documentSnapshot.toObject(User::class.java)
                Log.i(TAG,"Signed In user $signedUser")
            }
            .addOnFailureListener { exception ->
                Log.i(TAG,"Error In Fetching Signed In User ",exception)
            }

        btnPickImage.setOnClickListener {
            Log.i(TAG,"Open Up Image Picker On Device")
            val imagePickerIntent=Intent(Intent.ACTION_GET_CONTENT)
            imagePickerIntent.type="image/*"
            if(imagePickerIntent.resolveActivity(packageManager)!=null)
            {
                startActivityForResult(imagePickerIntent,PICK_PHOTO_CODE)
            }
        }

        btnSubmit.setOnClickListener {
            handleSubmitButtonClick()
        }
    }

    private fun handleSubmitButtonClick()
    {
         if(photoUri==null)
         {
              Toast.makeText(this,"No Photo Selected",Toast.LENGTH_SHORT).show()
             return
         }
         if(etDescription.text.isBlank())
         {
             Toast.makeText(this,"No Description Provided",Toast.LENGTH_SHORT).show()
             return
         }
        if(signedUser==null)
        {
            Toast.makeText(this,"No Signed In User,please Wait",Toast.LENGTH_SHORT).show()
            return
        }

        btnSubmit.isEnabled=false

        val photoUploadUri=photoUri as Uri
        val photoReference=storageReference.child("images/${System.currentTimeMillis()}-photo.jpg")
        //upload photo to the firebase
        photoReference.putFile(photoUploadUri)
            .continueWithTask {
                photoUploadTask->
                //retrieve image URL of the uploaded image
                photoReference.downloadUrl
            }.continueWithTask {
                downloadUrlTask->
                //create a post object with the image url  and add that to posts collection
                val post=Post(
                    etDescription.text.toString(),
                    downloadUrlTask.result.toString(),
                    System.currentTimeMillis(),
                    signedUser)
               firestoreDb.collection("posts").add(post)
            }.addOnCompleteListener {
                postCreationTask->

                btnSubmit.isEnabled=true
                if(!postCreationTask.isSuccessful)
                {
                    Log.e(TAG,"Exception During Firebase Operations",postCreationTask.exception)
                    Toast.makeText(this,"Failed to save the post",Toast.LENGTH_SHORT).show()
                }
               etDescription.text.clear()
                imageView.setImageResource(0)
                Toast.makeText(this,"Sucess!",Toast.LENGTH_SHORT).show()
                val profileIntent=Intent(this,ProfileActivity::class.java)
                profileIntent.putExtra(EXTRA_USERNAME,signedUser?.username)

                startActivity(profileIntent)
                finish()
            }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode== PICK_PHOTO_CODE)
        {
            if(resultCode==Activity.RESULT_OK)
            {
               photoUri=data?.data
                Log.i(TAG,"photoUri $photoUri")
                imageView.setImageURI(photoUri)
            }
            else
            {
                Toast.makeText(this,"Image Picker Action Cancelled",Toast.LENGTH_SHORT).show()
            }
        }
    }
}