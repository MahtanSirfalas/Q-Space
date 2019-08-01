package com.ust.spaceq.stages

import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.app.Activity
import android.content.Intent
import android.graphics.drawable.AnimationDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.Gravity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.view.animation.OvershootInterpolator
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.ImageView
import android.widget.PopupWindow
import android.widget.Toast.*
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.ust.spaceq.*
import com.ust.spaceq.R

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
    lateinit var randomNumberTask1: Runnable
    lateinit var randomNumberTask2: Runnable
    lateinit var mainHandler: Handler
    lateinit var updatePointTask: Runnable
    var isRunning = false
    var num1 = 0
    var num2 = 0
    var num3 = 0
    val operator = listOf("+","-")

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
        randomNumberTask1 = object:Runnable{
            override fun run() {
                var numList = mutableListOf<Int>()
                for (i in 10..199){
                    if (i%6==0 && i%8==0){
                        numList.add(i)
                    }else{}
                }
                val ranNum = (numList).random()
                tvRandom.text = ranNum.toString()
                mainHandler.postDelayed(this, 10)
            }
        }
        randomNumberTask2 = object:Runnable{
            override fun run() {
                val numList = mutableListOf<Int>(4,6,8)
                val ranNum = (numList).random()
                tvRandom.text = ranNum.toString()
                mainHandler.postDelayed(this, 100)
            }
        }

        fun startcheck() {
            stageRef.addListenerForSingleValueEvent(object: ValueEventListener {
                override fun onCancelled(p0: DatabaseError) {
                    Log.d(TAG, "stageRef Data couldn't read; No Internet Connection/No Response " +
                            "from database/Wrong datapath")
                    val toast = makeText(baseContext, getString(R.string.listener_cancelled), LENGTH_LONG)
                    toast.setGravity(Gravity.CENTER, 0, 0)
                    toast.show()
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
                            val toast = makeText(baseContext, "You passed that stage before.", LENGTH_SHORT)
                            toast.setGravity(Gravity.TOP, 0, 100)
                            toast.show()
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
            val inputMethodManager = getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
            if(it != null){
                inputMethodManager.hideSoftInputFromWindow(it.windowToken, 0)
            }

            val kontrol = etAnswer.text.toString()
            if (kontrol.trim().isNotEmpty()){
                try {
                    uAnswer = etAnswer.text.toString().toInt()
                    Log.d(TAG, "uAnswer is assigned as $uAnswer")
                }catch (ex:Exception){
                    Log.d(TAG, "Something's Wrong; uAnswer couldn't assign!")
                    val toast = makeText(baseContext, "Please Enter a Valid Value", LENGTH_SHORT)
                    toast.setGravity(Gravity.CENTER, 0, 0)
                    toast.show()
                }
                stageRef.addListenerForSingleValueEvent(object : ValueEventListener{
                    override fun onCancelled(p0: DatabaseError) {
                        Log.d(TAG, "stageRef Data couldn't read; No Internet Connection/No Response from database/Wrong datapath")
                        val toast = makeText(baseContext, getString(R.string.listener_cancelled), LENGTH_LONG)
                        toast.setGravity(Gravity.CENTER, 0, 0)
                        toast.show()
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
                                val toast = makeText(baseContext, getString(R.string.bravo), LENGTH_SHORT)
                                toast.setGravity(Gravity.CENTER, 0, -200)
                                toast.show()
                            }else{
                                point -= 10
                                stageRef.child("point").setValue(point)
                                Log.d(TAG, "Something's Wrong; $uAnswer != $answer! point is updated:$point")
                                val toast = makeText(baseContext, getString(R.string.wrong_answer), LENGTH_SHORT)
                                toast.setGravity(Gravity.CENTER, 0, 0)
                                toast.show()
                            }
                        }else{
                            if (answer == uAnswer){
                                starAnimation()
                                Log.d(TAG, "$levelKey: Answer ($uAnswer) is equal to $answer; " +
                                        "But no points added to the database")
                                val toast = makeText(baseContext, getString(R.string.bravo), LENGTH_SHORT)
                                toast.setGravity(Gravity.CENTER, 0, -200)
                                toast.show()
                            }else{
                                Log.d(TAG, "Something's Wrong; $uAnswer != $answer!")
                                val toast = makeText(baseContext, getString(R.string.come_on), LENGTH_SHORT)
                                toast.setGravity(Gravity.CENTER, 0, 0)
                                toast.show()
                            }
                        }
                    }
                })
            }else{
                Log.d(TAG, "tv_answer1 is empty!")
                val toast = makeText(baseContext, "Enter Your Answer!", LENGTH_SHORT)
                toast.setGravity(Gravity.CENTER, 0, 0)
                toast.show()
            }
        }
        startQuestion()
        startcheck()
    }

    private fun startQuestion(){
        val atf2 = AnimationUtils.loadAnimation(this, R.anim.atf2)
        val gfo1 = AnimationUtils.loadAnimation(this, R.anim.gfo1)
        //Number 1 action
        tvRandom.visibility = View.VISIBLE
        tvRandom.startAnimation(atf2)
        atf2.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(p0: Animation?) {
                Log.d(TAG, "tvRandom 1: START")
                when(levelKey){
                    "Stage 3"->{
                        mainHandler.post(randomNumberTask)
                    }
                    "Stage 8"->{
                        mainHandler.post(randomNumberTask1)
                    }
                }
            }
            override fun onAnimationRepeat(p0: Animation?) {}
            override fun onAnimationEnd(p0: Animation?) {
                Log.d(TAG, "tvRandom 1: END")
                when(levelKey){
                    "Stage 3"->{
                        mainHandler.removeCallbacks(randomNumberTask)
                    }
                    "Stage 8"->{
                        mainHandler.removeCallbacks(randomNumberTask1)
                    }
                }
                tvRandom.startAnimation(gfo1)
                gfo1.setAnimationListener(object : Animation.AnimationListener {
                    override fun onAnimationStart(p0: Animation?) {
                        num1 = tvRandom.text.toString().toInt()
                        Log.d(TAG, "num1=$num1")
                    }
                    override fun onAnimationRepeat(p0: Animation?) {}
                    override fun onAnimationEnd(p0: Animation?) {
                        tvRandom.visibility = View.INVISIBLE
                        //Number 2 action
                        tvRandom.startAnimation(atf2)
                        atf2.setAnimationListener(object : Animation.AnimationListener {
                            override fun onAnimationStart(p0: Animation?) {
                                tvLabel.text = getString(R.string.num2)
                                Log.d(TAG, "tvRandom 2: START")
                                tvRandom.visibility = View.VISIBLE
                                when(levelKey){
                                    "Stage 3"->{
                                        mainHandler.post(randomNumberTask)
                                    }
                                    "Stage 8"->{
                                        mainHandler.post(randomNumberTask2)
                                    }
                                }
                            }
                            override fun onAnimationRepeat(p0: Animation?) {}
                            override fun onAnimationEnd(p0: Animation?) {
                                Log.d(TAG, "tvRandom 2: END")
                                when(levelKey){
                                    "Stage 3"->{
                                        mainHandler.removeCallbacks(randomNumberTask)
                                    }
                                    "Stage 8"->{
                                        mainHandler.removeCallbacks(randomNumberTask2)
                                    }
                                }

                                tvRandom.startAnimation(gfo1)
                                gfo1.setAnimationListener(object : Animation.AnimationListener {
                                    override fun onAnimationStart(p0: Animation?) {
                                        num2 = tvRandom.text.toString().toInt()
                                        Log.d(TAG, "num2=$num2")
                                    }
                                    override fun onAnimationRepeat(p0: Animation?) {}
                                    override fun onAnimationEnd(p0: Animation?) {
                                        tvRandom.visibility = View.INVISIBLE
                                        //Number 3 action
                                        tvRandom.startAnimation(atf2)
                                        atf2.setAnimationListener(object : Animation.AnimationListener {
                                            override fun onAnimationStart(p0: Animation?) {
                                                Log.d(TAG, "tvRandom 3: START")
                                                tvLabel.text = getString(R.string.num3)
                                                tvRandom.visibility = View.VISIBLE
                                                when(levelKey){
                                                    "Stage 3"->{
                                                        mainHandler.post(randomNumberTask)
                                                    }
                                                    "Stage 8"->{
                                                        mainHandler.post(randomNumberTask)
                                                    }
                                                }
                                            }
                                            override fun onAnimationRepeat(p0: Animation?) {}
                                            override fun onAnimationEnd(p0: Animation?) {
                                                Log.d(TAG, "tvRandom 3: END")
                                                when(levelKey){
                                                    "Stage 3"->{
                                                        mainHandler.removeCallbacks(randomNumberTask)
                                                    }
                                                    "Stage 8"->{
                                                        mainHandler.removeCallbacks(randomNumberTask)
                                                    }
                                                }

                                                tvRandom.startAnimation(gfo1)
                                                gfo1.setAnimationListener(object : Animation.AnimationListener {
                                                    override fun onAnimationStart(p0: Animation?) {
                                                        num3 = tvRandom.text.toString().toInt()
                                                        Log.d(TAG, "num3=$num3")
                                                    }
                                                    override fun onAnimationRepeat(p0: Animation?) {}
                                                    override fun onAnimationEnd(p0: Animation?) {
                                                        tvRandom.visibility = View.INVISIBLE

                                                        levelAdapt(levelKey)

                                                        val fadein = AnimationUtils.loadAnimation(baseContext,
                                                            R.anim.abc_fade_in
                                                        )
                                                        val slidein = AnimationUtils.loadAnimation(baseContext,
                                                            R.anim.abc_slide_in_bottom
                                                        )
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
//        window.isOutsideTouchable = true

        //                Pop up window
        val imageShow = show.findViewById<ImageView>(R.id.iv_spaceMedal)
        window.contentView = show
        window.showAtLocation(buttAnswer,1,0,100)
        val scaleX = PropertyValuesHolder.ofFloat(View.SCALE_X, 0.5f,1f)
        val scaleY = PropertyValuesHolder.ofFloat(View.SCALE_Y, 0.5f,1f)
        val alpha = PropertyValuesHolder.ofFloat(View.ALPHA,0f,1f)
        ObjectAnimator.ofPropertyValuesHolder(show, scaleX,scaleY,alpha).apply {
            interpolator = OvershootInterpolator()
            duration = 600
        }.start()
        imageShow.setOnClickListener{
            window.dismiss()
        }
        buttAnswer.visibility = View.INVISIBLE
        etAnswer.isFocusable = false
    }

    private fun levelAdapt(level:String){
        val fadein = AnimationUtils.loadAnimation(this, R.anim.abc_fade_in)
        ib_back.visibility = View.VISIBLE
        ib_next.visibility = View.VISIBLE
        ib_back.startAnimation(fadein)
        ib_next.startAnimation(fadein)
        when (level){
            "Stage 3" -> {
                val operator1 = operator.shuffled()[0]
                val operator2 = operator.shuffled()[1]
                when (operator1){
                    "+"-> if (operator2 == "+"){
                        answer = num1 + num2 + num3
                        tvLabel.text ="${getString(R.string.num1)} + ${getString(R.string.num2)} + ${getString(R.string.num3)}"
                    }else{
                        answer = num1 + num2 - num3
                        tvLabel.text ="${getString(R.string.num1)} + ${getString(R.string.num2)} - ${getString(R.string.num3)}"
                    }
                    "-"-> if (operator2 == "+"){
                        answer = num1 - num2 + num3
                        tvLabel.text ="${getString(R.string.num1)} - ${getString(R.string.num2)} + ${getString(R.string.num3)}"
                    }else{
                        answer = num1 - num2 - num3
                        tvLabel.text ="${getString(R.string.num1)} - ${getString(R.string.num2)} - ${getString(R.string.num3)}"
                    }
                }
            }
            "Stage 8" -> {
                val operator2 = operator.shuffled()[0]
                if (operator2 == "+"){
                    answer = num1 / num2 + num3
                    tvLabel.text = "${getString(R.string.num1)} / ${getString(R.string.num2)} + ${getString(R.string.num3)}"
                }else{
                    answer = num1 / num2 - num3
                    tvLabel.text = "${getString(R.string.num1)} / ${getString(R.string.num2)} - ${getString(R.string.num3)}"
                }
            }
        }
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

    fun showNext(view:View?){
        ib_next.setColorFilter(resources.getColor(R.color.colorPurple))
        when(levelKey){
            "Stage 3" -> {
                levelKey = "Stage 4"
                val intent = Intent(this@RandomActivity, OrderedActivity::class.java)
                intent.putExtra("levelKey", levelKey)
                intent.putExtra("tvName", nick)
                startActivity(intent)
            }
            "Stage 8" -> {
                levelKey = "Stage 9"
                val intent = Intent(this@RandomActivity, OrderedActivity::class.java)
                intent.putExtra("levelKey", levelKey)
                intent.putExtra("tvName", nick)
                startActivity(intent)
            }
            else -> {}
        }
    }

    fun showBack(view:View?){
        ib_back.setColorFilter(resources.getColor(R.color.colorPurple))
        when(levelKey){
            "Stage 3" -> {
                levelKey = "Stage 2"
                val intent = Intent(this@RandomActivity, OrderedActivity::class.java)
                intent.putExtra("levelKey", levelKey)
                intent.putExtra("tvName", nick)
                startActivity(intent)
            }
            "Stage 8" -> {
                levelKey = "Stage 7"
                val intent = Intent(this@RandomActivity, OrderedActivity::class.java)
                intent.putExtra("levelKey", levelKey)
                intent.putExtra("tvName", nick)
                startActivity(intent)
            }
            else -> {}
        }
    }

    fun mainMenu(view: View?) {
        if (isRunning){
            mainHandler.removeCallbacks(updatePointTask)
        }else{
            Log.d(TAG, "isRunning false")
        }
        Log.d(TAG, "mainMenu pressed..")
        val intent = Intent(this@RandomActivity, MainActivity::class.java)
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
