package com.ust.spaceq

import android.annotation.TargetApi
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.AnimationDrawable
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.MotionEvent
import android.view.View
import android.widget.ImageView
import android.widget.PopupWindow
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

import kotlinx.android.synthetic.main.activity_ordered.*
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
private lateinit var stageRef : DatabaseReference

@Suppress("RECEIVER_NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
class OrderedActivity : AppCompatActivity() {
    val TAG = "OrderedActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ordered)
        setSupportActionBar(toolbar)

        val constraintLayout = findViewById<ConstraintLayout>(R.id.layoutbg)
        val animationDrawable = constraintLayout.background as AnimationDrawable
        animationDrawable.setEnterFadeDuration(2000)
        animationDrawable.setExitFadeDuration(4000)
        animationDrawable.start()

        qAnswer = mapOf("Stage 1" to 95, "Stage 2" to 116)

        firebaseAnalytics = FirebaseAnalytics.getInstance(this)
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
        databaseReference = database.reference.child("Users")
        nick = intent.getStringExtra("tvName")
        levelKey = intent.getStringExtra("levelKey")
        answer = qAnswer[levelKey] as Int
        stageRef = database.getReference("Users/$uid/stages/$levelKey")

        supportActionBar?.title = levelKey

        levelAdapt(levelKey)

        Log.d(TAG, "levelKey=$levelKey, answer=$answer")

        stageRef.addListenerForSingleValueEvent(object:ValueEventListener{
            override fun onCancelled(p0: DatabaseError) {
                Log.d(TAG, "stageRef Data couldn't read; No Internet Connection/No Response from database/Wrong datapath")
            }

            override fun onDataChange(p0: DataSnapshot) {
                if (p0.hasChildren()){
                    Log.d(TAG, "$levelKey; RERUNS!")
                    var point = p0.child("point").value as Long
                    var control = p0.child("control").value as Boolean
                    if (control){
                        point -= 1
                        stageRef.child("point").setValue(point)
                    }else{
                        Toast.makeText(baseContext,"You passed that stage before.",
                            Toast.LENGTH_SHORT).show()
                    }
                }else{
                    stageRef.child("point").setValue(1000)
                    stageRef.child("control").setValue(true)
                    Log.d(TAG, "First run on $levelKey, adaptation DONE!")
                }
            }

        })
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
            val window = PopupWindow(this)
            val show = layoutInflater.inflate(R.layout.layout_popup, null)
            window.isOutsideTouchable = true


            stageRef.addListenerForSingleValueEvent(object:ValueEventListener{
                override fun onCancelled(p0: DatabaseError) {
                    Log.d(TAG, "stageRef Data couldn't read; No Internet Connection/No Response from database/Wrong datapath")
                }

                override fun onDataChange(p0: DataSnapshot) {
                    var point = p0.child("point").value as Long
                    var control = p0.child("control").value as Boolean
                    val userRef = databaseReference.child(uid)
                    if (control){
                        if (uAnswer == answer) {
                            stageRef.child("control").setValue(false)
                            userRef.addListenerForSingleValueEvent(object:ValueEventListener{
                                override fun onCancelled(p0s: DatabaseError) {
                                    Log.d(TAG, "userRef Data couldn't read; No Internet Connection/No Response from database/Wrong datapath")
                                }

                                override fun onDataChange(p0s: DataSnapshot) {
                                    var points = p0s.child("points").value as Long
                                    points += point
                                    userRef.child("points").setValue(points)
                                }
                            })
//                            window.setTouchInterceptor(View.OnTouchListener { v, event ->
//                                if (event.action == MotionEvent.ACTION_OUTSIDE) {
//                                    window.dismiss()
//                                }
//                                false
//                            })
//                            window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                            window.contentView = show
                            val imageShow = show.findViewById<ImageView>(R.id.iv_spaceMedal)
                            imageShow.setOnClickListener{
                                window.dismiss()
                            }
                            window.showAtLocation(buttAnswer1,1,0,100)


                            Log.d(TAG, "$levelKey: Answer ($uAnswer) is equal to $answer; Accepted!")
                            Toast.makeText(baseContext, "Bravo! answer is accepted!", Toast.LENGTH_SHORT).show()
                        } else {
                            point -= 10
                            stageRef.child("point").setValue(point)
                            Log.d(TAG, "Something's Wrong; $uAnswer != $answer!")
                            Toast.makeText(baseContext, "Wrong answer, try again!", Toast.LENGTH_SHORT).show()
                        }
                    }else{
                        if(uAnswer == answer){
                            window.contentView = show
                            val imageShow = show.findViewById<ImageView>(R.id.iv_spaceMedal)
                            imageShow.setOnClickListener{
                                window.dismiss()
                            }
                            window.showAtLocation(buttAnswer1,1,0,100)
                            Log.d(TAG, "$levelKey: Answer ($uAnswer) is equal to $answer; " +
                                    "But no points added to the database")
                            Toast.makeText(baseContext, "Bravo! Your answer is accepted!", Toast.LENGTH_SHORT).show()
                        }else{
                            Log.d(TAG, "Something's Wrong; $uAnswer != $answer!")
                            Toast.makeText(baseContext, "Come on mate you answered the question before!", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            })

        } else {
            Log.d(TAG, "tv_answer1 is empty!")
            Toast.makeText(applicationContext, "Enter Your Answer!", Toast.LENGTH_SHORT).show()
        }
    }

    fun levelAdapt(level: String) {
        when (level) {
            "Stage 1" -> {
                say1.text = "19"
                say2.text = "38"
                say3.text = "57"
                say4.text = "76"
                Log.d(TAG, "$level adaptation is done successfully!")
            }
            "Stage 2" -> {
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

