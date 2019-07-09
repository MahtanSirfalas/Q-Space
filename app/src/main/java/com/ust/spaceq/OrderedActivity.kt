package com.ust.spaceq

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.PopupWindow
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

import kotlinx.android.synthetic.main.activity_ordered.*
import java.io.File
import kotlin.Exception

private lateinit var firebaseAnalytics: FirebaseAnalytics
private lateinit var auth: FirebaseAuth
private lateinit var database: FirebaseDatabase
private lateinit var databaseReference: DatabaseReference
private lateinit var commsReference: DatabaseReference
private lateinit var nick: String
private lateinit var levelKey: String
private lateinit var qAnswer: Map<String, Int>
private var uAnswer: Int = 1
private var answer: Int = 1

@Suppress("RECEIVER_NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
class OrderedActivity : AppCompatActivity() {
    val TAG = "OrderedActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ordered)
        setSupportActionBar(toolbar)

        qAnswer = mapOf("Stage 1" to 95, "Stage 2" to 116)

        firebaseAnalytics = FirebaseAnalytics.getInstance(this)
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
        databaseReference = database.reference.child("Users")
        nick = intent.getStringExtra("tvName")
        levelKey = intent.getStringExtra("levelKey")
        answer = qAnswer[levelKey] as Int

        supportActionBar?.title = levelKey

        levelAdapt(levelKey)

        Log.d(TAG, "levelKey=$levelKey, answer=$answer")
    }

    fun slayButton(view: View?) {
        Log.d(TAG, "slayButton pressed")
        val kontrol = tv_answer1.text.toString()
        if (kontrol.trim().isNotEmpty()) {
            try {
                uAnswer = tv_answer1.text.toString().toInt()
                Log.d(TAG, "uAnswer is assigned as $uAnswer")
            } catch (ex: Exception) {
                ex.message
                Log.d(TAG, "Something's Wrong; uAnswer couldn't assign!")
                Toast.makeText(baseContext, "Please Enter a Valid Value", Toast.LENGTH_SHORT).show()
            }
            if (uAnswer == answer) {
//                TODO("point adding to the database must be done")
//                val points:String = "100"
//                val user = auth.currentUser
//                val userId = user!!.uid
//                val userDb = databaseReference.child(userId)
//                if (userDb.child("points").key.isNullOrBlank()){
//                    userDb.child("points").setValue(points)
//                }else{
//                    userPoints = (userDb.child("points").key!!.toInt()+100) as String
//                    userDb.child("points").setValue(userPoints)
//                }

                val window = PopupWindow(this)
                val show = layoutInflater.inflate(R.layout.layout_popup, null)
                window.contentView = show
                val imageShow = show.findViewById<ImageView>(R.id.iv_spaceMedal)
                imageShow.setOnClickListener{
                    window.dismiss()
                }

                window.showAtLocation(buttAnswer1,1,0,100)

                Log.d(TAG, "$levelKey: Answer ($uAnswer) is equal to $answer; Accepted!")
                Toast.makeText(baseContext, "Bravo! answer is accepted!", Toast.LENGTH_SHORT).show()
            } else {
                Log.d(TAG, "Something's Wrong; $uAnswer != $answer!")
                Toast.makeText(baseContext, "Wrong answer, try again!", Toast.LENGTH_SHORT).show()

            }
        } else {
            Log.d(TAG, "tv_answer1 is empty!")
            Toast.makeText(applicationContext, "Enter Your Answer!", Toast.LENGTH_SHORT).show()
        }
    }

    fun levelAdapt(level: String) {
        when (level) {
            "level1" -> {
                say1.text = "19"
                say2.text = "38"
                say3.text = "57"
                say4.text = "76"
                Log.d(TAG, "$level adaptation is done successfully!")
            }
            "level2" -> {
                say1.text = "24"
                say2.text = "47"
                say3.text = "70"
                say4.text = "93"
                Log.d(TAG, "$level adaptation is done successfully!")
            }
            else -> {
                Log.d(TAG, "Something's Wrong; levelAdapt is failed!")
            }
        }
    }

    fun showComments(view: View?) {
        Log.d(TAG, "Comments button pressed")
        val intent = Intent(this@OrderedActivity, CommentActivity::class.java)
        intent.putExtra("tvName", nick)
//        intent.putExtra("email", email)
        intent.putExtra("levelKey", levelKey)
        startActivity(intent)
    }

    private fun mainMenu(view: View?) {
        Log.d(TAG, "mainMenu pressed..")
        val intent = Intent(this@OrderedActivity, MainActivity::class.java)
//        intent.putExtra("email", email)
        startActivity(intent)
    }

    fun showProfile(view: View?) {
        Log.d(TAG, "Profile pressed..")
        val intent = Intent(this@OrderedActivity, ProfileActivity::class.java)
        intent.putExtra("tvName", nick)
//        intent.putExtra("email", email)
//        intent.putExtra("uid", uid)
        startActivity(intent)
    }

    fun signOut(view: View?) {
        Log.d(TAG, "signOut pressed..")
        auth.signOut()
        startActivity(Intent(this@OrderedActivity, LoginActivity::class.java))
        this@OrderedActivity.finish()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_ordered, menu)
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

