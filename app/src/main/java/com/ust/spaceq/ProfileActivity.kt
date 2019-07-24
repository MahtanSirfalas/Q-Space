package com.ust.spaceq

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.AnimationDrawable
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.*
import androidx.constraintlayout.widget.ConstraintLayout
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_main.*
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

        val stageList = listOf("Stage 1", "Stage 2", "Stage 3", "Stage 4",
            "Stage 5","Stage 6","Stage 7","Stage 8", "Stage 9", "Stage 10")
        val statsList = mutableListOf<String>("","","","","","","","","","")

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
                Toast.makeText(baseContext, getString(R.string.listener_cancelled), Toast.LENGTH_LONG).show()
            }
            override fun onDataChange(p0: DataSnapshot) {
                val userLevel = p0.child("level").value
                val userPoints = p0.child("points").value
                uName = p0.child("nickName").value as String
                avatar = p0.child("avatar").value as String
                textLevel.text = getString(R.string.level) + userLevel.toString()
                textPoints.text = getString(R.string.total_points) + userPoints.toString()
                textName.text = getString(R.string.username) + uName
                Picasso.get().load(avatar).into(ivAvatar_circle)

                Log.d(TAG, "user informations parsed")
                if(p0.child("stages").exists()){
                    val stages = p0.child("stages").value as HashMap<*, *>
                    val stage = stages.keys
                    animations()
                    Log.d(TAG, "STAGES; $stages STAGE= $stage")
                    for (item in stageList){
                        val index = stageList.indexOf(item)
                        if (stage.contains(item)){
                            val control = p0.child("stages/$item/control").value as Boolean
                            val point = p0.child("stages/$item/point").value as Long
                            if (control){
                                statsList[index] = "unfinished"
                                Log.d(TAG, "$item: unfinished")
                            }else{
                                statsList[index] = "$point points"
                                Log.d(TAG, "$item: Finished")
                            }
                            Log.d(TAG, "stageList: $item applied")
                        }else{
                            statsList[index] = "unseen"
                            Log.d(TAG, "$item unseen")
                        }
                    }
                    val stageAdapter = StageListAdapter(this@ProfileActivity, R.layout.layout_list_stages, stageList, statsList)
                    lvStages.adapter = stageAdapter
                }else{
                    animations()
                    Log.d(TAG, "stages doesn't exist yet!")
                }
                if(p0.child("upCount").exists()){
                    val upCount = p0.child("upCount").value as Long
                    when (upCount){
                        in 1..9 ->{
                            tvEarnedUpvote.text = "You are joining community, go on like that; you gained " + upCount.toString()+ " Upvotes"
                        }
                        in 10..49 ->{
                            tvEarnedUpvote.text = "It seems like some of the users remember you; you gained " + upCount.toString()+ " Upvotes"
                        }
                        in 50..99 ->{
                            tvEarnedUpvote.text = "With a quick glance We can tell that you are getting popular in the community; You gained " + upCount.toString()+ " Upvotes"
                        }
                        in 100..10000->{
                            tvEarnedUpvote.text = "You are one of the most popular community members, every player knows you; you gained  "+upCount.toString()+ " Upvotes"
                        }
                    }
                }else{
                    tvEarnedUpvote.text = "You didn't gain any Upvotes for your comments until now. Keep up the good work!"
                }
            }
        })
        textMail.text = getString(R.string.email) + email

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

        val fadein = AnimationUtils.loadAnimation(this, R.anim.abc_fade_in)
        buttStats.setOnClickListener {
            showStats()
            stats_card.startAnimation(fadein)
        }
        buttProfile.setOnClickListener {
            profile_card.visibility = View.VISIBLE
            stats_card.visibility = View.INVISIBLE
            buttStats.isSelected = false
            buttProfile.isSelected = true
            profile_card.startAnimation(fadein)
        }
        val slidein = AnimationUtils.loadAnimation(this, R.anim.abc_slide_in_top)
        selectorButts.startAnimation(slidein)
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
        val fadein = AnimationUtils.loadAnimation(this, R.anim.abc_fade_in)
        profile_card.visibility = View.VISIBLE
        textName.visibility = View.VISIBLE
        textMail.visibility = View.VISIBLE
        textLevel.visibility = View.VISIBLE
        textPoints.visibility = View.VISIBLE
        buttAvatar.visibility = View.VISIBLE
        ivAvatar_circle.visibility = View.VISIBLE
        profile_card.startAnimation(fadein)
    }

    fun showStats(){
        profile_card.visibility = View.INVISIBLE
        stats_card.visibility = View.VISIBLE
        buttStats.isSelected = true
        buttProfile.isSelected = false
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
        Log.d(TAG, "Back Pressed")
        this.finish()
    }
    //Custom stageList Adapter
    private class StageListAdapter(context: Context,val mLayout:Int , var list1:List<String>,
                                   var list2:MutableList<String>): BaseAdapter() {
        private val mContext: Context

        init {
            mContext = context
        }
        override fun getView(p0: Int, convertView: View?, viewGroup: ViewGroup?): View {
            //Rendering out each row
            val layoutInflater = LayoutInflater.from(mContext)
            val rowStage = layoutInflater.inflate(mLayout, viewGroup, false)

            val stagesText = rowStage.findViewById<TextView>(R.id.text1)
            stagesText.text = list1.get(p0)

            val stageStatText = rowStage.findViewById<TextView>(R.id.text2)
            stageStatText.text = list2.get(p0)
            if (list2.get(p0) != "unfinished" && list2.get(p0) != "unseen"){
                stageStatText.setTextColor(Color.parseColor("#669900"))
            }else{}

            return rowStage
        }
        override fun getItem(p0: Int): Any {return "SomeString"}
        override fun getItemId(p0: Int): Long {return p0.toLong()}
        override fun getCount(): Int {
            //for how many rows in the list
            return list1.size
        }
    }
}
