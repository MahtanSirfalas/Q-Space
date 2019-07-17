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
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

import kotlinx.android.synthetic.main.activity_random.*
import java.util.function.BinaryOperator

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

        startQuestion()

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
                                                        buttGiveup.visibility = View.VISIBLE
                                                        etAnswer.startAnimation(fadein)
                                                        buttAnswer.startAnimation(fadein)
                                                        buttGiveup.startAnimation(slidein)
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

    fun showComments(view: View?) {
        if (isRunning){
            mainHandler.removeCallbacks(randomNumberTask)
        }else{
            Log.d(TAG, "isRunning false")
        }
        Log.d(TAG, "Comments button pressed")
        val intent = Intent(this@RandomActivity, CommentActivity::class.java)
        intent.putExtra("tvName", nick)
        intent.putExtra("levelKey", levelKey)
        startActivity(intent)
    }

    private fun mainMenu(view: View?) {
        if (isRunning){
            mainHandler.removeCallbacks(randomNumberTask)
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
            mainHandler.removeCallbacks(randomNumberTask)
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
            mainHandler.removeCallbacks(randomNumberTask)
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
            mainHandler.removeCallbacks(randomNumberTask)
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
