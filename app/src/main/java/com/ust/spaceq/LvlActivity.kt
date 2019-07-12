package com.ust.spaceq

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

import kotlinx.android.synthetic.main.activity_lvl.*

private lateinit var firebaseAnalytics: FirebaseAnalytics
private lateinit var auth: FirebaseAuth
private lateinit var database: FirebaseDatabase
private lateinit var databaseReference: DatabaseReference
private lateinit var commsReference: DatabaseReference
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
                    Log.d(TAG, "STAGES; $stages STAGE= $stage")

                    if (stage.contains("Stage 1")){
                        val control = p0.child("stages/Stage 1/control").value as Boolean
                        if (control){
                            Log.d(TAG, "Stage 1: Pass")
                        }else{
                            buttL1.setBackgroundResource(R.drawable.custom_butt_lvl)
                            Log.d(TAG, "Stage 1: Finished")
                        }
                    }else{}
                    if (stage.contains("Stage 2")){
                        val control = p0.child("stages/Stage 2/control").value as Boolean
                        if (control){
                            Log.d(TAG, "Stage 2: Pass")
                        }else{
                            buttL2.setBackgroundResource(R.drawable.custom_butt_lvl)
                            Log.d(TAG, "Stage 2: Finished")
                        }
                    }else{}
                }else{
                    Log.d(TAG, "stages doesn't exist yet!")
                }

            }

        })

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