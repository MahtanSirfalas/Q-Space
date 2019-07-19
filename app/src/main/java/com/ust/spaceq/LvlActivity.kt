package com.ust.spaceq

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.ust.spaceq.stages.OrderedActivity
import com.ust.spaceq.stages.RandomActivity

import kotlinx.android.synthetic.main.activity_lvl.*

private lateinit var firebaseAnalytics: FirebaseAnalytics
private lateinit var auth: FirebaseAuth
private lateinit var database: FirebaseDatabase
private lateinit var databaseReference: DatabaseReference
private lateinit var qAnswer: Map<String,Int>
private lateinit var nick:String
private lateinit var levelKey: String
private var cevap: Int = 1


class LvlActivity : AppCompatActivity() {
    val TAG = "LvLActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lvl)
        setSupportActionBar(toolbar)

        qAnswer = mapOf("Stage 1" to 95, "Stage 2" to 116)

        supportActionBar?.title = "Stages"

        firebaseAnalytics = FirebaseAnalytics.getInstance(this)
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
        databaseReference = database.reference.child("Users")
        nick = intent.getStringExtra("tvName")
        val userRef = database.reference.child("Users/$uid")
        userRef.addListenerForSingleValueEvent(object: ValueEventListener{
            override fun onCancelled(p0: DatabaseError) {
                Log.d(TAG, "userRef data couldn't read!")
            }

            override fun onDataChange(p0: DataSnapshot) {
                if (p0.child("stages").exists()){
                    val stages = p0.child("stages").value as HashMap<*, *>
                    val stage = stages.keys
                    animations()
                    Log.d(TAG, "STAGES; $stages STAGE= $stage")
                    if (stage.contains("Stage 1")){
                        val control = p0.child("stages/Stage 1/control").value as Boolean
                        val point = p0.child("stages/Stage 1/point").value as Long
                        if (control){
                            Log.d(TAG, "Stage 1: Pass")
                        }else{
                            if (point.toInt() == 0){
                                buttL1.setBackgroundResource(R.drawable.custom_butt_lvl)
                                Log.d(TAG, "Stage 1: Given Up")
                            }else{
                                buttL1.setBackgroundResource(R.drawable.custom_butt_stagewon)
                                Log.d(TAG, "Stage 1: Finished")
                            }
                        }
                    }else{}
                    if (stage.contains("Stage 2")){
                        val control = p0.child("stages/Stage 2/control").value as Boolean
                        val point = p0.child("stages/Stage 2/point").value as Long
                        if (control){
                            Log.d(TAG, "Stage 2: Pass")
                        }else{
                            if (point.toInt() == 0) {
                                buttL2.setBackgroundResource(R.drawable.custom_butt_lvl)
                                Log.d(TAG, "Stage 2: Given Up")
                            }else{
                                buttL2.setBackgroundResource(R.drawable.custom_butt_stagewon)
                                Log.d(TAG, "Stage 2: Finished")
                            }
                        }
                    }else{}
                    if (stage.contains("Stage 3")){
                        val control = p0.child("stages/Stage 3/control").value as Boolean
                        val point = p0.child("stages/Stage 3/point").value as Long
                        if (control){
                            Log.d(TAG, "Stage 3: Pass")
                        }else{
                            if (point.toInt() == 0){
                                buttL3.setBackgroundResource(R.drawable.custom_butt_lvl)
                                Log.d(TAG, "Stage 3: Given Up")
                            }else{
                                buttL3.setBackgroundResource(R.drawable.custom_butt_stagewon)
                                Log.d(TAG, "Stage 3: Finished")
                            }
                        }
                    }else{}
                }else{
                    animations()
                    Log.d(TAG, "stages doesn't exist yet!")
                }

            }

        })
    }

    private fun animations(){
        val ltr = AnimationUtils.loadAnimation(this, R.anim.ltr)
        val ltr1 = AnimationUtils.loadAnimation(this, R.anim.ltr1)
        val ltr2 = AnimationUtils.loadAnimation(this, R.anim.ltr2)
        val ltr3 = AnimationUtils.loadAnimation(this, R.anim.ltr3)
        val ltr4 = AnimationUtils.loadAnimation(this, R.anim.ltr4)
        val ltr5 = AnimationUtils.loadAnimation(this, R.anim.ltr5)
        val ltr6 = AnimationUtils.loadAnimation(this, R.anim.ltr6)
        val atf = AnimationUtils.loadAnimation(this, R.anim.abc_fade_in)
        ivBack.visibility = View.VISIBLE
        ivBack.startAnimation(atf)
        buttL1.visibility = View.VISIBLE
        buttL2.visibility = View.VISIBLE
        buttL3.visibility = View.VISIBLE
        buttL4.visibility = View.VISIBLE
        buttL5.visibility = View.VISIBLE
        buttL6.visibility = View.VISIBLE
        buttL7.visibility = View.VISIBLE
        buttL8.visibility = View.VISIBLE
        buttL9.visibility = View.VISIBLE
        buttL10.visibility = View.VISIBLE
        buttL11.visibility = View.VISIBLE
        buttL12.visibility = View.VISIBLE
        buttL13.visibility = View.VISIBLE
        buttL14.visibility = View.VISIBLE
        buttL15.visibility = View.VISIBLE
        buttL16.visibility = View.VISIBLE
        buttL17.visibility = View.VISIBLE
        buttL18.visibility = View.VISIBLE
        buttL19.visibility = View.VISIBLE
        buttL20.visibility = View.VISIBLE
        buttL21.visibility = View.VISIBLE
        buttL22.visibility = View.VISIBLE
        buttL23.visibility = View.VISIBLE
        buttL24.visibility = View.VISIBLE
        buttL25.visibility = View.VISIBLE
        buttL26.visibility = View.VISIBLE
        buttL27.visibility = View.VISIBLE
        buttL28.visibility = View.VISIBLE
        buttL29.visibility = View.VISIBLE
        buttL30.visibility = View.VISIBLE
        buttL31.visibility = View.VISIBLE
        buttL32.visibility = View.VISIBLE
        buttL33.visibility = View.VISIBLE
        buttL34.visibility = View.VISIBLE
        buttL35.visibility = View.VISIBLE
        buttL1.startAnimation(ltr)
        buttL2.startAnimation(ltr)
        buttL3.startAnimation(ltr)
        buttL4.startAnimation(ltr)
        buttL5.startAnimation(ltr)
        buttL6.startAnimation(ltr1)
        buttL7.startAnimation(ltr1)
        buttL8.startAnimation(ltr1)
        buttL9.startAnimation(ltr1)
        buttL10.startAnimation(ltr1)
        buttL11.startAnimation(ltr2)
        buttL12.startAnimation(ltr2)
        buttL13.startAnimation(ltr2)
        buttL14.startAnimation(ltr2)
        buttL15.startAnimation(ltr2)
        buttL16.startAnimation(ltr3)
        buttL17.startAnimation(ltr3)
        buttL18.startAnimation(ltr3)
        buttL19.startAnimation(ltr3)
        buttL20.startAnimation(ltr3)
        buttL21.startAnimation(ltr4)
        buttL22.startAnimation(ltr4)
        buttL23.startAnimation(ltr4)
        buttL24.startAnimation(ltr4)
        buttL25.startAnimation(ltr4)
        buttL26.startAnimation(ltr5)
        buttL27.startAnimation(ltr5)
        buttL28.startAnimation(ltr5)
        buttL29.startAnimation(ltr5)
        buttL30.startAnimation(ltr5)
        buttL31.startAnimation(ltr6)
        buttL32.startAnimation(ltr6)
        buttL33.startAnimation(ltr6)
        buttL34.startAnimation(ltr6)
        buttL35.startAnimation(ltr6)

    }


    fun showStage1(view: View?){
        levelKey = "Stage 1"
        cevap = qAnswer["Stage 1"] as Int
        val intent = Intent(this@LvlActivity, OrderedActivity::class.java)
        intent.putExtra("tvName", nick)
        intent.putExtra("levelKey", levelKey)
        intent.putExtra("cevap", cevap)
        startActivity(intent)
    }

    fun showStage2(view: View?){
        levelKey = "Stage 2"
        cevap = qAnswer["Stage 2"] as Int
        val intent = Intent(this@LvlActivity, OrderedActivity::class.java)
        intent.putExtra("tvName", nick)
        intent.putExtra("levelKey", levelKey)
        intent.putExtra("cevap", cevap)
        startActivity(intent)
    }

    fun showStage3(view: View?){
        levelKey = "Stage 3"
        val intent = Intent(this@LvlActivity, RandomActivity::class.java)
        intent.putExtra("tvName", nick)
        intent.putExtra("levelKey", levelKey)
        startActivity(intent)
    }

    private fun mainMenu(view: View?){
        Log.d(TAG, "mainMenu pressed..")
        val intent = Intent(this@LvlActivity, MainActivity::class.java)
        startActivity(intent)
    }

    fun showProfile(view: View?){
        Log.d(TAG, "Profile pressed..")
        val intent = Intent(this@LvlActivity, ProfileActivity::class.java)
        intent.putExtra("tvName", nick)
        startActivity(intent)
    }

    fun signOut(view: View?){
        Log.d(TAG, "signOut pressed..")
        auth.signOut()
        startActivity(Intent(this@LvlActivity, LoginActivity::class.java))
        this@LvlActivity.finish()
    }
    override fun onBackPressed() {
        mainMenu(null)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_lvl, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        when {
            item.itemId == R.id.action_out -> signOut(null)
            item.itemId == R.id.action_profile -> showProfile(null)
            item.itemId == R.id.action_home -> mainMenu(null)
            else -> {

            }
        }

        return when (item.itemId) {
            R.id.action_out -> true
            else -> super.onOptionsItemSelected(item)
        }
    }

}