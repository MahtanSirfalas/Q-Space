package com.ust.spaceq

import android.app.Activity
import android.content.Intent
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.activity_profile.*

private lateinit var nick:String
private lateinit var email:String
private lateinit var uid: String
class ProfileActivity : AppCompatActivity() {
    val TAG = "ProfileActivity"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        nick = intent.getStringExtra("tvName")
        email = intent.getStringExtra("eMail")
        uid = intent.getStringExtra("uid")

        textName.text = "Username: "+ nick
        textMail.text = "E-Mail: "+ email

        buttAvatar.setOnClickListener {
            Log.d(TAG, "avatar selector clicked")

            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, 0)
        }
    }
    var selectedPhotoUri: Uri? = null

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 0 && resultCode == Activity.RESULT_OK && data != null) {
            //proceed and check what the selected image was...
            Log.d(TAG,"avatar was selected")

            selectedPhotoUri = data.data

            val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, selectedPhotoUri)

            avatar_imageView.setImageBitmap(bitmap)

            buttAvatar.alpha = 0f

            /*val bitmapDrawable = BitmapDrawable(bitmap)
            buttAvatar.setBackgroundDrawable(bitmapDrawable)*/

            uploadImageToStorage()
        }
    }
    private fun uploadImageToStorage(){
        if (selectedPhotoUri == null) return
        var filename = uid
        val ref = FirebaseStorage.getInstance().getReference("/Images/$filename")

        ref.putFile(selectedPhotoUri!!)
            .addOnSuccessListener {
                Log.d(TAG, "Avatar uploaded: ${it.metadata?.path}")

                ref.downloadUrl.addOnSuccessListener {
                    Log.d(TAG, "Avatar location: $it")

                    saveImageToDatabase(it.toString())
                }
            }
            .addOnFailureListener{
                Log.d(TAG, "Avatar couldn't uploaded!")
            }
    }
    private fun saveImageToDatabase(avatarUrl: String){
        val ref = FirebaseDatabase.getInstance().getReference("/Users/$uid/avatar")

        ref.setValue(avatarUrl)
            .addOnSuccessListener {
                Log.d(TAG, "Avatar added to user database.")
            }
            .addOnFailureListener {
                Log.d(TAG, "Failed to set value to database!")
            }
    }
}