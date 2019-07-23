package com.ust.spaceq

import android.app.Activity
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.drawable.AnimationDrawable
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_profile.*
import java.lang.NullPointerException

private lateinit var database: FirebaseDatabase
private lateinit var commsReference: DatabaseReference
private lateinit var userRef: DatabaseReference

class ProfileActivity : AppCompatActivity() {
    val TAG = "ProfileActivity"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        val constraintLayout = findViewById<ConstraintLayout>(R.id.layoutbg)
        val animationDrawable = constraintLayout.background as AnimationDrawable
        animationDrawable.setEnterFadeDuration(2000)
        animationDrawable.setExitFadeDuration(4000)
        animationDrawable.start()

        database = FirebaseDatabase.getInstance()
        commsReference = database.reference.child("Posts")
        userRef = database.reference.child("Users/$uid")

        userRef.addListenerForSingleValueEvent(object:ValueEventListener{
            override fun onCancelled(p0: DatabaseError) {
                Log.d(TAG, "Something's Wrong: User information get FAILED")
                Toast.makeText(baseContext, R.string.listener_cancelled, Toast.LENGTH_LONG).show()
            }
            override fun onDataChange(p0: DataSnapshot) {
                val userLevel = p0.child("level").value
                val userPoints = p0.child("points").value
                uName = p0.child("nickName").value as String
                avatar = p0.child("avatar").value as String
                textLevel.text = R.string.level.toString() + userLevel.toString()
                textPoints.text = R.string.total_points.toString() + userPoints.toString()
                textName.text = R.string.username.toString() + uName
                Picasso.get().load(avatar).into(ivAvatar_circle)
                animations()
                Log.d(TAG, "user informations parsed")
            }
        })
        textMail.text = R.string.email.toString() + email

        buttAvatar.setOnClickListener {
            Log.d(TAG, "avatar selector clicked")

            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, 0)
        }
        val buttShare = findViewById<Button>(R.id.buttShare)
        buttShare.setOnClickListener {
            share()
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

            ivAvatar_circle.setImageBitmap(bitmap)

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
                    avatar = it.toString()
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

    private fun animations(){
        val stf1 = AnimationUtils.loadAnimation(this, R.anim.stf1)
        val fadein = AnimationUtils.loadAnimation(this, R.anim.abc_fade_in)
        profile_card.startAnimation(stf1)
        stf1.setAnimationListener(object : Animation.AnimationListener{
            override fun onAnimationStart(p0: Animation?) {profile_card.visibility = View.VISIBLE}
            override fun onAnimationRepeat(p0: Animation?) {}
            override fun onAnimationEnd(p0: Animation?) {
                textName.visibility = View.VISIBLE
                textMail.visibility = View.VISIBLE
                textLevel.visibility = View.VISIBLE
                textPoints.visibility = View.VISIBLE
                buttAvatar.visibility = View.VISIBLE
                ivAvatar_circle.visibility = View.VISIBLE
                textName.startAnimation(fadein)
                textMail.startAnimation(fadein)
                textLevel.startAnimation(fadein)
                textPoints.startAnimation(fadein)
                buttAvatar.startAnimation(fadein)
                ivAvatar_circle.startAnimation(fadein)
            }
        })
    }

    fun share(){
        try {
            var imageUri = Uri.parse(
                MediaStore.Images.Media.insertImage(this.contentResolver,
                    BitmapFactory.decodeResource(resources, R.drawable.marsbutton100), null, null
                )
            )
            val sharingIntent = Intent(Intent.ACTION_SEND)
            sharingIntent.type = "image/*"
            sharingIntent.putExtra(Intent.EXTRA_STREAM, imageUri)
            sharingIntent.putExtra(Intent.EXTRA_SUBJECT, "Konulu")
            sharingIntent.putExtra(Intent.EXTRA_TEXT, "Bir varmış bir yokmuş")
            sharingIntent.putExtra(Intent.EXTRA_TITLE,  "Q Space")
            startActivity(Intent.createChooser(sharingIntent, "Share with..."))
        }catch (e:NullPointerException){}

    }

    override fun onBackPressed() {
        Log.d(TAG, "mainMenu pressed..")
        val intent = Intent(this@ProfileActivity, MainActivity::class.java)
        startActivity(intent)
    }
}