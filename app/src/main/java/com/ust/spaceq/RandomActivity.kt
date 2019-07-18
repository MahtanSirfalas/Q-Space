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
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

import kotlinx.android.synthetic.main.activity_random.*
import kotlinx.android.synthetic.main.activity_random.ibComment
import kotlinx.android.synthetic.main.activity_random.toolbar

private lateinit var firebaseAnalytics: FirebaseAnalytics
private lateinit var auth: FirebaseAuth
private lateinit var database: FirebaseDatabase
private lateinit var databaseReference: DatabaseReference
private lateinit var nick: String
private lateinit var levelKey: String
private var uAnswer: Int = 1
private var answer: Int = 1
private lateinit var stageRef : DatabaseReference

class RandomActivity : AppCompatActivity() {
    val TAG = "RandomActivity"
    lateinit var randomNumberTask: Runnable
    lateinit var mainHandler: Handler
    lateinit var updatePointTask: Runnable
    var isRunning = false
    var num1 = 0
    var num2 = 0
    var num3 = 0
    val operator = listOf<String>("+","-")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_random)
        setSupportActionBar(toolbar)

        mainHandler = Handler(Looper.getMainLooper())

        val constraintLayout = findViewById<ConstraintLayout>(R.id.layoutbg)
        val animationDrawable = constraintLayout.background as AnimationDrawable
        animationDrawable.setEnterFadeDuration(2000)
        animationDrawable.setExitFadeDuration(4000)
        animationDrawable.start()

        firebaseAnalytics = FirebaseAnalytics.getInstance(this)
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
        databaseReference = database.reference.child("Users")
        nick = intent.getStringExtra("tvName")
        levelKey = intent.getStringExtra("levelKey")
        stageRef = database.getReference("Users/$uid/stages/$levelKey")

        supportActionBar?.title = levelKey

        randomNumberTask = object : Runnable {
            override fun run() {
                val ranNum = (10..99).random()
                tvRandom.text = ranNum.toString()
                mainHandler.postDelayed(this, 10)
            }
        }
        fun startcheck() {
            stageRef.addListenerForSingleValueEvent(object: ValueEventListener {
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
                            point -= 4
                            stageRef.child("point").setValue(point)

                            updatePointTask = object : Runnable {
                                override fun run() {
                                    isRunning = true
                                    point -= 2
                                    Log.d(TAG, "point UPDATED: $point")
                                    stageRef.child("point").setValue(point)
                                    mainHandler.postDelayed(this, 15000)
                                }
                            }
                            mainHandler.post(updatePointTask)
                        }else{
                            commentAnimation()
                            Toast.makeText(baseContext,"You passed that stage before.",
                                Toast.LENGTH_SHORT).show()
                        }
                    }else{
                        stageRef.child("point").setValue(1006)
                        stageRef.child("control").setValue(true)
                        Log.d(TAG, "First run on $levelKey, adaptation DONE!")
                        startcheck()
                    }
                }
            })
        }

        buttAnswer.setOnClickListener {
            Log.d(TAG, "Slay button pressed")
            val kontrol = etAnswer.text.toString()
            if (kontrol.trim().isNotEmpty()){
                try {
                    uAnswer = etAnswer.text.toString().toInt()
                    Log.d(TAG, "uAnswer is assigned as $uAnswer")
                }catch (ex:Exception){
                    Log.d(TAG, "Something's Wrong; uAnswer couldn't assign!")
                    Toast.makeText(baseContext, "Please Enter a Valid Value", Toast.LENGTH_SHORT).show()
                }
                stageRef.addListenerForSingleValueEvent(object : ValueEventListener{
                    override fun onCancelled(p0: DatabaseError) {
                        Log.d(TAG, "stageRef Data couldn't read; No Internet Connection/No Response from database/Wrong datapath")
                        Toast.makeText(baseContext, "WARNING: Be Sure that you have an active internet connection!", Toast.LENGTH_LONG).show()
                    }
                    override fun onDataChange(p0: DataSnapshot) {
                        var point = p0.child("point").value as Long
                        val control = p0.child("control").value as Boolean
                        val userRef = databaseReference.child(uid)
                        if (control){
                            if (answer == uAnswer){
                                mainHandler.removeCallbacks(updatePointTask)
                                stageRef.child("control").setValue(false)
                                userRef.addListenerForSingleValueEvent(object : ValueEventListener {
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
                            }else{
                                point -= 10
                                stageRef.child("point").setValue(point)
                                Log.d(TAG, "Something's Wrong; $uAnswer != $answer! point is updated:$point")
                                Toast.makeText(baseContext, "Wrong answer, try again!", Toast.LENGTH_SHORT).show()
                            }
                        }else{
                            if (answer == uAnswer){
                                starAnimation()
                                Log.d(TAG, "$levelKey: Answer ($uAnswer) is equal to $answer; " +
                                        "But no points added to the database")
                                Toast.makeText(baseContext, "Bravo! Your answer is accepted!", Toast.LENGTH_SHORT).show()
                            }else{
                                Log.d(TAG, "Something's Wrong; $uAnswer != $answer!")
                                Toast.makeText(baseContext, "Come on mate you passed this stage before!", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                })
            }else{
                Log.d(TAG, "tv_answer1 is empty!")
                Toast.makeText(applicationContext, "Enter Your Answer!", Toast.LENGTH_SHORT).show()
            }
        }

        startQuestion()
        startcheck()

    }

    private fun startQuestion(){
        val atf2 = AnimationUtils.loadAnimation(this, R.anim.atf2)
        val gfo1 = AnimationUtils.loadAnimation(this, R.anim.gfo1)
        tvRandom.visibility = View.VISIBLE
        tvRandom.startAnimation(atf2)
        atf2.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(p0: Animation?) {
                Log.d(TAG, "tvRandom 1: START")
                mainHandler.post(randomNumberTask)
            }
            override fun onAnimationRepeat(p0: Animation?) {}
            override fun onAnimationEnd(p0: Animation?) {
                Log.d(TAG, "tvRandom 1: END")
                mainHandler.removeCallbacks(randomNumberTask)
                tvRandom.startAnimation(gfo1)
                gfo1.setAnimationListener(object : Animation.AnimationListener {
                    override fun onAnimationStart(p0: Animation?) {
                        num1 = tvRandom.text.toString().toInt()
                        Log.d(TAG, "num1=$num1")
                    }
                    override fun onAnimationRepeat(p0: Animation?) {}
                    override fun onAnimationEnd(p0: Animation?) {
                        tvRandom.visibility = View.INVISIBLE
                        tvRandom.startAnimation(atf2)
                        atf2.setAnimationListener(object : Animation.AnimationListener {
                            override fun onAnimationStart(p0: Animation?) {
                                tvLabel.text = "Number 2"
                                Log.d(TAG, "tvRandom 2: START")
                                tvRandom.visibility = View.VISIBLE
                                mainHandler.post(randomNumberTask)
                            }
                            override fun onAnimationRepeat(p0: Animation?) {}
                            override fun onAnimationEnd(p0: Animation?) {
                                Log.d(TAG, "tvRandom 2: END")
                                mainHandler.removeCallbacks(randomNumberTask)

                                tvRandom.startAnimation(gfo1)
                                gfo1.setAnimationListener(object : Animation.AnimationListener {
                                    override fun onAnimationStart(p0: Animation?) {
                                        num2 = tvRandom.text.toString().toInt()
                                        Log.d(TAG, "num2=$num2")
                                    }
                                    override fun onAnimationRepeat(p0: Animation?) {}
                                    override fun onAnimationEnd(p0: Animation?) {
                                        tvRandom.visibility = View.INVISIBLE

                                        tvRandom.startAnimation(atf2)
                                        atf2.setAnimationListener(object : Animation.AnimationListener {
                                            override fun onAnimationStart(p0: Animation?) {
                                                Log.d(TAG, "tvRandom 3: START")
                                                tvLabel.text = "Number 3"
                                                tvRandom.visibility = View.VISIBLE
                                                mainHandler.post(randomNumberTask)
                                            }
                                            override fun onAnimationRepeat(p0: Animation?) {}
                                            override fun onAnimationEnd(p0: Animation?) {
                                                Log.d(TAG, "tvRandom 3: END")
                                                mainHandler.removeCallbacks(randomNumberTask)

                                                tvRandom.startAnimation(gfo1)
                                                gfo1.setAnimationListener(object : Animation.AnimationListener {
                                                    override fun onAnimationStart(p0: Animation?) {
                                                        num3 = tvRandom.text.toString().toInt()
                                                        Log.d(TAG, "num3=$num3")
                                                    }
                                                    override fun onAnimationRepeat(p0: Animation?) {}
                                                    override fun onAnimationEnd(p0: Animation?) {
                                                        tvRandom.visibility = View.INVISIBLE
                                                        val operator1 = operator.shuffled()[0]
                                                        val operator2 = operator.shuffled()[1]

                                                        when (operator1){
                                                            "+"-> if (operator2 == "+"){
                                                                answer = num1 + num2 + num3
                                                                tvLabel.text ="Number1 + Number2 + Number3"
                                                            }else{
                                                                answer = num1 + num2 - num3
                                                                tvLabel.text ="Number1 + Number2 - Number3"
                                                            }
                                                            "-"-> if (operator2 == "+"){
                                                                answer = num1 - num2 + num3
                                                                tvLabel.text ="Number1 - Number2 + Number3"
                                                            }else{
                                                                answer = num1 - num2 - num3
                                                                tvLabel.text ="Number1 - Number2 - Number3"
                                                            }
                                                        }
                                                        val fadein = AnimationUtils.loadAnimation(baseContext, R.anim.abc_fade_in)
                                                        val slidein = AnimationUtils.loadAnimation(baseContext, R.anim.abc_slide_in_bottom)
                                                        etAnswer.visibility = View.VISIBLE
                                                        buttAnswer.visibility = View.VISIBLE
                                                        etAnswer.startAnimation(fadein)
                                                        buttAnswer.startAnimation(slidein)
                                                        commentAnimation()
                                                    }
                                                })
                                            }
                                        })
                                    }
                                })
                            }
                        })
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
                window.showAtLocation(buttAnswer,1,0,100)
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

    fun showComments(view: View?) {
        if (isRunning){
            mainHandler.removeCallbacks(updatePointTask)
        }else{
            Log.d(TAG, "isRunning false")
        }
        Log.d(TAG, "Comments button pressed")
        val intent = Intent(this@RandomActivity, CommentActivity::class.java)
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
                    window.showAtLocation(buttAnswer,1,0,100)
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
        val intent = Intent(this@RandomActivity, MainActivity::class.java)
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
        val intent = Intent(this@RandomActivity, ProfileActivity::class.java)
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
        startActivity(Intent(this@RandomActivity, LoginActivity::class.java))
        this@RandomActivity.finish()
    }

    override fun onBackPressed() {
        if (isRunning){
            mainHandler.removeCallbacks(updatePointTask)
        }else{
            Log.d(TAG, "isRunning false")
        }
        val intent = Intent(this@RandomActivity, LvlActivity::class.java)
        intent.putExtra("tvName", nick)
        startActivity(intent)
        this@RandomActivity.finish()
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
