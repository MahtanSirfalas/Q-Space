package com.ust.spaceq

import android.content.Intent
import android.graphics.drawable.AnimationDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.ImageView
import android.widget.PopupWindow
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

import kotlinx.android.synthetic.main.activity_ordered.*
import kotlinx.android.synthetic.main.activity_ordered.toolbar
import kotlin.Exception

private lateinit var firebaseAnalytics: FirebaseAnalytics
private lateinit var auth: FirebaseAuth
private lateinit var database: FirebaseDatabase
private lateinit var databaseReference: DatabaseReference
private lateinit var nick: String
private lateinit var levelKey: String
private lateinit var qAnswer: Map<String, Int>
private var uAnswer: Int = 1
private var answer: Int = 1
private lateinit var stageRef : DatabaseReference

@Suppress("RECEIVER_NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
class OrderedActivity : AppCompatActivity() {
    val TAG = "OrderedActivity"
    lateinit var mainHandler:Handler
    lateinit var updatePointTask: Runnable
    var isRunning = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ordered)
        setSupportActionBar(toolbar)

        mainHandler = Handler(Looper.getMainLooper())

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

        fun startcheck() {
            stageRef.addListenerForSingleValueEvent(object:ValueEventListener{
                override fun onCancelled(p0: DatabaseError) {
                    Log.d(TAG, "stageRef Data couldn't read; No Internet Connection/No Response " +
                            "from database/Wrong datapath")
                    Toast.makeText(baseContext, "WARNING: Be Sure that you have an active internet connection!", Toast.LENGTH_LONG).show()
                }
                override fun onDataChange(p0: DataSnapshot) {
                    if (p0.hasChildren()){
                        Log.d(TAG, "$levelKey; RERUNS!")
                        var point = p0.child("point").value as Long
                        var control = p0.child("control").value as Boolean
                        if (control){
                            point -= 2
                            stageRef.child("point").setValue(point)

                            updatePointTask = object : Runnable {
                                override fun run() {
                                    isRunning = true
                                    point -= 2
                                    Log.d(TAG, "point UPDATED: $point")
                                    stageRef.child("point").setValue(point)
                                    mainHandler.postDelayed(this, 10000)
                                }
                            }
                            mainHandler.post(updatePointTask)
                        }else{
                            Toast.makeText(baseContext,"You passed that stage before.",
                                Toast.LENGTH_SHORT).show()
                        }
                    }else{
                        stageRef.child("point").setValue(1004)
                        stageRef.child("control").setValue(true)
                        Log.d(TAG, "First run on $levelKey, adaptation DONE!")
                        startcheck()
                    }
                }
            })
        }
        animation()
        startcheck()
        commentAnimation()
//        chrono.base = SystemClock.elapsedRealtime()
//        chrono.start()
    }

    private fun animation(){
        val animsay = AnimationUtils.loadAnimation(this, R.anim.abc_slide_in_top)
        val animtv = AnimationUtils.loadAnimation(this, R.anim.abc_slide_in_bottom)
        val atf = AnimationUtils.loadAnimation(this, R.anim.atf)
        say1.startAnimation(animsay)
        say2.startAnimation(animsay)
        say3.startAnimation(animsay)
        say4.startAnimation(animsay)
        textQ.startAnimation(atf)
        tv_answer1.startAnimation(animtv)
        buttAnswer1.startAnimation(animtv)
    }

    /*private fun meteorAnimation(){

        val meteor = AnimationUtils.loadAnimation(baseContext, R.anim.meteor)
        val gfo = AnimationUtils.loadAnimation(baseContext, R.anim.gfo)
        iv_meteor.visibility = View.VISIBLE
        iv_meteor.startAnimation(meteor)
        meteor.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(p0: Animation?) {}
            override fun onAnimationRepeat(p0: Animation?) {}
            override fun onAnimationEnd(p0: Animation?) {
                iv_meteor.startAnimation(gfo)
                gfo.setAnimationListener(object : Animation.AnimationListener {
                    override fun onAnimationStart(p0: Animation?) {}
                    override fun onAnimationRepeat(p0: Animation?) {}
                    override fun onAnimationEnd(p0: Animation?) {}
                })
            }
        })
    }*/

    private fun starAnimation(){
//
        val window = PopupWindow(this)
        val show = layoutInflater.inflate(R.layout.layout_popup, null)
        window.isOutsideTouchable = true
        val atf1 = AnimationUtils.loadAnimation(baseContext, R.anim.atf1)
//
        val starkayar = AnimationUtils.loadAnimation(baseContext, R.anim.starkayar)
        val starkayar1 = AnimationUtils.loadAnimation(baseContext, R.anim.starkayar1)
        val fadein = AnimationUtils.loadAnimation(baseContext, R.anim.abc_fade_in)
        val gfo = AnimationUtils.loadAnimation(baseContext, R.anim.gfo)
        val yellowstar = AnimationUtils.loadAnimation(baseContext, R.anim.yellowstar)
        val yellowstar1 = AnimationUtils.loadAnimation(baseContext, R.anim.yellowstar1)
        iv_starKayar.visibility = View.VISIBLE
        iv_starKayar1.visibility = View.VISIBLE
        iv_starKayar.startAnimation(starkayar)
        iv_starKayar1.startAnimation(starkayar1)
        starkayar.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(p0: Animation?) {}
            override fun onAnimationRepeat(p0: Animation?) {}
            override fun onAnimationEnd(p0: Animation?) {
                iv_starKayar.startAnimation(gfo)
                iv_starKayar1.startAnimation(gfo)
                iv_yellowStar.visibility = View.VISIBLE
                iv_yellowStar.startAnimation(fadein)
//                Pop up window
                val imageShow = show.findViewById<ImageView>(R.id.iv_spaceMedal)
                window.contentView = show
                window.showAtLocation(buttAnswer1,1,0,100)
                show.startAnimation(atf1)
                imageShow.setOnClickListener{
                    window.dismiss()
                }
                fadein.setAnimationListener(object : Animation.AnimationListener{
                    override fun onAnimationStart(p0: Animation?) {}
                    override fun onAnimationRepeat(p0: Animation?) {}
                    override fun onAnimationEnd(p0: Animation?) {
//                        iv_meteor.visibility = View.GONE
                        iv_yellowStar.startAnimation(yellowstar)
                        iv_yellowStar.startAnimation(yellowstar1)
                        iv_starKayar.visibility = View.GONE
                        iv_starKayar1.visibility = View.GONE

                    }
                })
            }
        })
    }

    private fun commentAnimation(){
        val commAnim = AnimationUtils.loadAnimation(this, R.anim.commentbub)
        ibComment.visibility = View.VISIBLE
        ibComment.startAnimation(commAnim)
    }

    fun slayButton(view: View?) {
        Log.d(TAG, "slayButton pressed")
        val kontrol = tv_answer1.text.toString()
        if (kontrol.trim().isNotEmpty()) {
            try {
                uAnswer = tv_answer1.text.toString().toInt()
                Log.d(TAG, "uAnswer is assigned as $uAnswer")
            } catch (ex: Exception) {
                Log.d(TAG, "Something's Wrong; uAnswer couldn't assign!")
                Toast.makeText(baseContext, "Please Enter a Valid Value", Toast.LENGTH_SHORT).show()
            }

            stageRef.addListenerForSingleValueEvent(object:ValueEventListener{
                override fun onCancelled(p0: DatabaseError) {
                    Log.d(TAG, "stageRef Data couldn't read; No Internet Connection/No Response from database/Wrong datapath")
                }

                override fun onDataChange(p0: DataSnapshot) {
                    var point = p0.child("point").value as Long
                    val control = p0.child("control").value as Boolean
                    val userRef = databaseReference.child(uid)
                    if (control){
                        if (uAnswer == answer) {
                            mainHandler.removeCallbacks(updatePointTask)
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
                            starAnimation()

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
                            starAnimation()

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
        }else {
            Log.d(TAG, "tv_answer1 is empty!")
            Toast.makeText(applicationContext, "Enter Your Answer!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun levelAdapt(level: String) {
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
        if (isRunning){
            mainHandler.removeCallbacks(updatePointTask)
        }else{
            Log.d(TAG, "isRunning false")
        }
        val intent = Intent(this@OrderedActivity, CommentActivity::class.java)
        intent.putExtra("tvName", nick)
        intent.putExtra("levelKey", levelKey)

        val window = PopupWindow(this)
        val show = layoutInflater.inflate(R.layout.layout_popup_giveup, null)
        window.isOutsideTouchable = true
        val atf1 = AnimationUtils.loadAnimation(baseContext, R.anim.atf1)

        stageRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {}
            override fun onDataChange(p0: DataSnapshot) {
                val control = p0.child("control").value as Boolean
                if (control){
                    window.contentView = show
                    window.showAtLocation(buttAnswer1,1,0,100)
                    show.startAnimation(atf1)
                    val buttYes = show.findViewById<Button>(R.id.buttYes)
                    val buttNo = show.findViewById<Button>(R.id.buttNo)
                    buttYes.setOnClickListener {
                        stageRef.child("point").setValue(0)
                        stageRef.child("control").setValue(false)
                        if (isRunning){
                            mainHandler.removeCallbacks(updatePointTask)
                        }else{
                            Log.d(TAG, "isRunning false")
                        }
                        window.dismiss()
                        startActivity(intent)

                    }
                    buttNo.setOnClickListener {
                        window.dismiss()
                    }
                }else{
                    Log.d(TAG, "Comments button pressed")

                    startActivity(intent)
                }
            }
        })
    }

    private fun mainMenu(view: View?) {
        if (isRunning){
            mainHandler.removeCallbacks(updatePointTask)
        }else{
            Log.d(TAG, "isRunning false")
        }
        Log.d(TAG, "mainMenu pressed..")
        val intent = Intent(this@OrderedActivity, MainActivity::class.java)
//        intent.putExtra("email", email)
        startActivity(intent)
    }

    fun showProfile(view: View?) {
        if (isRunning){
            mainHandler.removeCallbacks(updatePointTask)
        }else{
            Log.d(TAG, "isRunning false")
        }
        Log.d(TAG, "Profile pressed..")
        val intent = Intent(this@OrderedActivity, ProfileActivity::class.java)
        intent.putExtra("tvName", nick)
        startActivity(intent)
    }

    fun signOut(view: View?) {
        if (isRunning){
            mainHandler.removeCallbacks(updatePointTask)
        }else{
            Log.d(TAG, "isRunning false")
        }
        Log.d(TAG, "signOut pressed..")
        auth.signOut()
        startActivity(Intent(this@OrderedActivity, LoginActivity::class.java))
        this@OrderedActivity.finish()
    }

    override fun onBackPressed() {
        if (isRunning){
            mainHandler.removeCallbacks(updatePointTask)
        }else{
            Log.d(TAG, "isRunning false")
        }
        val intent = Intent(this@OrderedActivity, LvlActivity::class.java)
        intent.putExtra("tvName", nick)
        startActivity(intent)
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

