package com.ust.qspace.stages

import android.animation.*
import android.app.Activity
import android.content.Intent
import android.graphics.drawable.AnimationDrawable
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.Gravity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.animation.*
import android.view.inputmethod.InputMethodManager
import android.widget.*
import android.widget.Toast.*
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.animation.doOnEnd
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.InterstitialAd
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.ust.qspace.*
import com.ust.qspace.R
import com.ust.qspace.room.AppRoomEntity
import com.ust.qspace.trees.PrivacyActivity
import com.ust.qspace.trees.SettingsActivity
import com.ust.qspace.trees.TermsActivity

import kotlinx.android.synthetic.main.activity_random.*
import kotlinx.android.synthetic.main.activity_random.ibComment
import kotlinx.android.synthetic.main.activity_random.toolbar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlin.concurrent.thread

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
    lateinit var random9packTask: Runnable
    lateinit var mainHandler: Handler
    lateinit var updatePointTask: Runnable
    var isRunning = false
    var num1 = 0
    var num2 = 0
    var num3 = 0
    val operator = listOf("+","-")
    var objList = mutableListOf("1","2","3","4","5","6","7","8","9","A","C","D","Q","W","E","X","F","J")

    private var mValueEventListener: ValueEventListener? = null

    private lateinit var mediaPlayer: MediaPlayer
    private lateinit var duoUpAnimSet: AnimatorSet
    private lateinit var petAnimSet: AnimatorSet
    private lateinit var mInterstitialAd: InterstitialAd

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

        //InterstitialAd part
        mInterstitialAd = InterstitialAd(this)
        mInterstitialAd.adUnitId = "ca-app-pub-7262139641436003/7403813696"
        mInterstitialAd.loadAd(AdRequest.Builder().build())
        //

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

        random9packTask = object:Runnable {
            override fun run() {
                objList.shuffle()
                tv_r11.text = objList[0]
                tv_r12.text = objList[1]
                tv_r13.text = objList[2]
                tv_r21.text = objList[3]
                tv_r22.text = objList[4]
                tv_r23.text = objList[5]
                tv_r31.text = objList[6]
                tv_r32.text = objList[7]
                tv_r33.text = objList[8]
                mainHandler.postDelayed(this, 20)
            }
        }

        fun startcheck() {

            Thread{
                var dbStage = db.stageDao().getOne(levelKey)

                if (dbStage != null){
                    var point = dbStage.db_stage_points
                    var control = dbStage.db_stage_control
                    stageStartFireDBCheck(point, control)
                    if (control) {
                        point -= 10
                        stagePointControlUpdate(point, control)
                        updatePointTask = object : Runnable{
                            override fun run() {
                                isRunning = true
                                point -= 2
                                stagePointControlUpdate(point, control)
                                Log.d(TAG, "$levelKey point updated to $point")
                                mainHandler.postDelayed(this, 10000)
                            }
                        }
                        mainHandler.post(updatePointTask)
                    }else{

                        Log.d(TAG, "Stage passed before.")
                    }

                }else{
                    val lastInd = levelKey.length
                    val id =
                        if (levelKey == "Stage Ufo"){
                            1000
                        }else{
                           levelKey.substring(6, lastInd).toInt()
                        }

                    var stageEnt = AppRoomEntity(id, levelKey, 10004, true)
                    db.stageDao().insert(stageEnt)
                    stageRef.child("point").setValue(10000)
                    stageRef.child("control").setValue(true)
                    Log.d(TAG, "First run on $levelKey, adaptation DONE!")
                    startcheck()
                }
            }.start()
        }

        mediaPlayer = MediaPlayer.create(this, R.raw.win)
        mediaPlayer.isLooping = false
        mediaPlayer.setVolume(50f, 50f)

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

                runBlocking(Dispatchers.Default) {
                    synchronDBs()
                }

                Thread.sleep(50)

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

                                Thread{//update roomDB the answer is accepted
                                    val lastInd = levelKey.length
                                    val id = levelKey.substring(6, lastInd).toInt()
                                    val stageEnt = AppRoomEntity(id, levelKey, point.toInt(), false)
                                    db.stageDao().update(stageEnt)
                                    Log.d(TAG, "roomDB UPDATED: answer is accepted")
                                }.start()

                                stageRef.child("control").setValue(false)
                                userRef.addListenerForSingleValueEvent(object : ValueEventListener {
                                    override fun onCancelled(p0s: DatabaseError) {
                                        Log.d(TAG, "userRef Data couldn't read; No Internet Connection/No Response from database/Wrong datapath")
                                    }
                                    override fun onDataChange(p0s: DataSnapshot) {
                                        var points = p0s.child("points").value as Long
                                        points += point
                                        userRef.child("points").setValue(points)

                                        mValueEventListener?.let { kopek ->//kopek : ValueEventListener
                                            stageRef.removeEventListener(kopek)
                                            Log.d(TAG, "stageRef EventListener Removed!")
                                        }
                                    }
                                })

                                Log.d(TAG, "$levelKey: Answer ($uAnswer) is equal to $answer; Accepted!")
                                val toast = makeText(baseContext, getString(R.string.bravo), LENGTH_SHORT)
                                toast.setGravity(Gravity.CENTER, 0, -200)
                                toast.show()
                                starAnimation()
                            }else{
                                Thread{//Wrong answer => -10 points to roomDB
                                    val lastInd = levelKey.length
                                    val id = levelKey.substring(6, lastInd).toInt()
                                    point -= 100
                                    val stageEnt = AppRoomEntity(id, levelKey, point.toInt(), false)
                                    db.stageDao().update(stageEnt)
                                    stageRef.child("point").setValue(point)
                                    Log.d(TAG, "roomDB UPDATED: answer is wrong; -10 points")
                                }.start()
                                Log.d(TAG, "Something's Wrong; $uAnswer != $answer! point is updated:$point")
                                val toast = makeText(baseContext, getString(R.string.wrong_answer), LENGTH_SHORT)
                                toast.setGravity(Gravity.CENTER, 0, 0)
                                toast.show()
                                mValueEventListener?.let { kopek ->
                                    stageRef.removeEventListener(kopek)
                                    Log.d(TAG, "stageRef EventListener Removed!")
                                }
                            }
                        }else{
                            if (answer == uAnswer){
                                Log.d(TAG, "$levelKey: Answer ($uAnswer) is equal to $answer; " +
                                        "But no points added to the database")
                                val toast = makeText(baseContext, getString(R.string.bravo), LENGTH_SHORT)
                                toast.setGravity(Gravity.CENTER, 0, -200)
                                toast.show()
                                mValueEventListener?.let { kopek ->
                                    stageRef.removeEventListener(kopek)
                                    Log.d(TAG, "stageRef EventListener Removed!")
                                }
                                starAnimation()
                            }else{
                                Log.d(TAG, "Something's Wrong; $uAnswer != $answer!")
                                val toast = makeText(baseContext, getString(R.string.come_on), LENGTH_SHORT)
                                toast.setGravity(Gravity.CENTER, 0, 0)
                                toast.show()
                                mValueEventListener?.let { kopek ->
                                    stageRef.removeEventListener(kopek)
                                    Log.d(TAG, "stageRef EventListener Removed!")
                                }
                            }
                        }
                    }
                })
            }else{
                Log.d(TAG, "tv_answer1 is empty!")
                val toast = makeText(baseContext, getString(R.string.enter_answer), LENGTH_SHORT)
                toast.setGravity(Gravity.CENTER, 0, 0)
                toast.show()
            }
        }
        if (levelKey == "Stage 23"){
            ninePackQuestion()
        }
        else if(levelKey == "Stage Ufo"){
            ufo_q_human.visibility = View.VISIBLE
        }
        else{
            startQuestion()
        }

        startcheck()
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {

        if(levelKey == "Stage Ufo"){ //That's because onCreate may be early to calc. views' positions etc.
            ufoHumanityQuestion()
        }
    }

    fun View.absX(): Int {
        val location = IntArray(2)
        this.getLocationOnScreen(location)
        return location[0]
    }

    fun View.absY(): Int {
        val location = IntArray(2)
        this.getLocationOnScreen(location)
        return location[1]
    }

    fun ufoQuestDuoUpAnimation(imageThing:ImageView, xFloat:Float, yFloaf:Float){

        val xDistance = (areaPark.left - imageThing.right + xFloat)
        val yDistance = (areaPark.bottom - imageThing.top + yFloaf)
        val yMove = PropertyValuesHolder.ofFloat(View.TRANSLATION_Y,
            yDistance)
        val xMove = PropertyValuesHolder.ofFloat(View.TRANSLATION_X,
            xDistance)
        val upMove = ObjectAnimator.ofPropertyValuesHolder(imageThing, xMove, yMove).apply {
            duration = 10000
        }
        val rotationRightMove = ObjectAnimator.ofFloat(imageThing, View.ROTATION,
            imageThing.rotation + 10f).apply {
            duration = 200
            repeatMode = ValueAnimator.REVERSE
            repeatCount = ValueAnimator.INFINITE
        }
        duoUpAnimSet = AnimatorSet().apply {
            play(upMove).with(rotationRightMove)
        }

        duoUpAnimSet.start()

        upMove.doOnEnd {
            rotationRightMove.cancel()
        }
    }

    fun ufoQuePetAnim(imageThing: ImageView){//Other pets animation
        var transX = 0f
        var transY = 0f
        when(imageThing){
            pet1 -> {
                transX = -20f
                transY = 20f
            }
            pet2 -> {
                transX = -20f
                transY = -20f
            }
            pet3 -> {
                transX = 24f
                transY = 24f
            }
            pet4 ->{
                transX = -24f
                transY = 24f
            }
        }
        val rotateMove = ObjectAnimator.ofFloat(imageThing, View.ROTATION,
            imageThing.rotation+5f).apply {
            duration = 150
            repeatMode = ValueAnimator.REVERSE
            repeatCount = ValueAnimator.INFINITE
        }
        val xMove = PropertyValuesHolder.ofFloat(View.TRANSLATION_X, transX)
        val yMove = PropertyValuesHolder.ofFloat(View.TRANSLATION_Y, transY)
        val petAnim = ObjectAnimator.ofPropertyValuesHolder(imageThing, xMove, yMove).apply {
            duration = 1000
            repeatMode = ValueAnimator.REVERSE
            repeatCount = ValueAnimator.INFINITE
            interpolator = AccelerateInterpolator()
        }
        petAnimSet = AnimatorSet().apply {
            play(rotateMove).with(petAnim)
        }
        petAnimSet.start()
    }

    fun ufoHumanityQuestion(){
        Log.d(TAG, "Ufo Q triggered!")
        ufoQuestDuoUpAnimation(ucgenSah, -60f,-30f)
        ufoQuestDuoUpAnimation(karePet, 0f, 0f)
        ufoQuePetAnim(pet1)
        ufoQuePetAnim(pet2)
        ufoQuePetAnim(pet3)
        ufoQuePetAnim(pet4)
        duoUpAnimSet.doOnEnd {
            val xMove = PropertyValuesHolder.ofFloat(View.TRANSLATION_X, karePet.translationX + 100f)
            val yMove = PropertyValuesHolder.ofFloat(View.TRANSLATION_Y, karePet.translationY - 116f)
            val petMove = ObjectAnimator.ofPropertyValuesHolder(karePet, xMove, yMove).apply {
                duration = 800
            }
            petMove.start()
            petAnimSet.cancel()
            petAnimSet.cancel()
            petAnimSet.cancel()
            petMove.doOnEnd {
                Thread.sleep(400)
                val fadeOut = AnimationUtils.loadAnimation(baseContext, R.anim.abc_fade_out)
                ufo_q_human.startAnimation(fadeOut)
                fadeOut.setAnimationListener(object : Animation.AnimationListener{
                    override fun onAnimationStart(p0: Animation?) {}
                    override fun onAnimationRepeat(p0: Animation?) {}
                    override fun onAnimationEnd(p0: Animation?) {
                        ufo_q_human.visibility = View.GONE
                        val fadein = AnimationUtils.loadAnimation(baseContext, R.anim.abc_fade_in)
                        val slidein = AnimationUtils.loadAnimation(baseContext,R.anim.abc_slide_in_bottom)
                        tv_obj_1.text = getString(R.string.school)
                        tv_obj_2.text = getString(R.string.camp)
                        tv_obj_3.text = getString(R.string.trash)
                        tv_obj_4.text = getString(R.string.dog_park)
                        tv_obj_1.textSize = 16f
                        tv_obj_2.textSize = 16f
                        tv_obj_3.textSize = 16f
                        tv_obj_4.textSize = 16f
                        answer = 4
                        tv_nineQ.text = getString(R.string.reminds_of)
                        ninePackOptionLayout.visibility = View.VISIBLE
                        etAnswer.visibility = View.VISIBLE
                        buttAnswer.visibility = View.VISIBLE
                        etAnswer.startAnimation(fadein)
                        buttAnswer.startAnimation(slidein)
                        etAnswer.isFocusable = false
                        commentAnimation()
                    }
                })
            }
        }
    }

    private fun ninePackQuestion(){
        val ninepackrandom = AnimationUtils.loadAnimation(this, R.anim.nine_pack_random)
        //val ninePackLayout = findViewById<LinearLayout>(R.id.ninePackLayout)

        tvLabel.visibility = View.VISIBLE
        tvLabel.text = getString(R.string.keep_in_mind)
        ObjectAnimator.ofFloat(tvLabel, View.ALPHA, 1f,0f).apply {
            duration = 800
            startDelay = 800
            interpolator = AccelerateInterpolator()
            addListener(object: AnimatorListenerAdapter(){
                override fun onAnimationEnd(p0: Animator?) {
                    ninePackLayout.visibility = View.VISIBLE
                    ninePackLayout.startAnimation(ninepackrandom)
                    ninepackrandom.setAnimationListener(object : Animation.AnimationListener {
                        override fun onAnimationRepeat(p0: Animation?) {}
                        override fun onAnimationStart(p0: Animation?) {
                            mainHandler.post(random9packTask)
                        }
                        override fun onAnimationEnd(p0: Animation?) {
                            mainHandler.removeCallbacks(random9packTask)
                            val alpha = ObjectAnimator.ofFloat(ninePackLayout, View.ALPHA, 1f, 0f).apply {
                                duration = 9000
                                interpolator = AccelerateInterpolator()
                            }
                            val scaleX = PropertyValuesHolder.ofFloat(View.SCALE_X, 1f, 3f)
                            val scaleY = PropertyValuesHolder.ofFloat(View.SCALE_Y, 1f, 3f)
                            val scaleLayout = ObjectAnimator.ofPropertyValuesHolder(ninePackLayout, scaleX, scaleY).apply {
                                duration = 9000
                            }
                            val animSet = AnimatorSet().apply {
                                play(alpha).with(scaleLayout)
                                addListener(object: AnimatorListenerAdapter(){
                                    override fun onAnimationEnd(animation: Animator?) {
                                        ninePackLayout.visibility = View.INVISIBLE
                                        val objShowed = objList.slice(0..8) as MutableList
                                        objShowed.shuffle()
                                        val objsOption = objShowed.slice(0..2) as MutableList
                                        objsOption.add(objList[9])
                                        objsOption.shuffle()
                                        tv_obj_1.text = objsOption[0]
                                        tv_obj_2.text = objsOption[1]
                                        tv_obj_3.text = objsOption[2]
                                        tv_obj_4.text = objsOption[3]
                                        when(objList[9]){
                                            tv_obj_1.text -> answer = 1
                                            tv_obj_2.text -> answer = 2
                                            tv_obj_3.text -> answer = 3
                                            tv_obj_4.text -> answer = 4
                                            else->{}
                                        }
                                        Log.d(TAG, "THE ANSWER = ${objList[9]}")
                                        val fadein = AnimationUtils.loadAnimation(baseContext, R.anim.abc_fade_in)
                                        val slidein = AnimationUtils.loadAnimation(baseContext,R.anim.abc_slide_in_bottom)
                                        ninePackOptionLayout.visibility = View.VISIBLE
                                        etAnswer.visibility = View.VISIBLE
                                        buttAnswer.visibility = View.VISIBLE
                                        ninePackOptionLayout.startAnimation(fadein)
                                        etAnswer.startAnimation(fadein)
                                        buttAnswer.startAnimation(slidein)
                                        etAnswer.isFocusable = false
                                        levelAdapt(levelKey)
                                        commentAnimation()
                                    }
                                })
                            }
                            animSet.start()
                        }
                    })
                }
            })
            start()
        }
    }

    fun checkButton(view: View?){
        val radioId = radioGugu.checkedRadioButtonId

        val radioButton:RadioButton = findViewById(radioId)

        etAnswer.setText(radioButton.text)
    }

    private fun startQuestion(){
        val atf2 = AnimationUtils.loadAnimation(this, R.anim.atf2)
        val gfo1 = AnimationUtils.loadAnimation(this, R.anim.gfo1)

        tvLabel.visibility = View.VISIBLE
        tvLabel.text = getString(R.string.keep_in_mind)
        ObjectAnimator.ofFloat(tvLabel, View.ALPHA, 1f, 0f).apply {
            duration = 800
            startDelay = 800
            interpolator = AccelerateInterpolator()
            addListener(object : AnimatorListenerAdapter(){
                override fun onAnimationEnd(animation: Animator?) {
                    //Number 1 action
                    tvLabel.text = getString(R.string.num1)
                    tvLabel.alpha = 1f
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
            })
        }.start()
    }

    private fun commentAnimation(){
        val commAnim = AnimationUtils.loadAnimation(this, R.anim.commentbub)
        ibComment.visibility = View.VISIBLE
        ibComment.startAnimation(commAnim)
    }

    private fun starAnimation(){
        if(levelKey == "Stage Ufo"){
        }else{
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
        }

        buttAnswer.visibility = View.INVISIBLE
        etAnswer.isFocusable = false
        mediaPlayer.start()
        mediaPlayer.setOnCompletionListener(object: MediaPlayer.OnCompletionListener{
            override fun onCompletion(p0: MediaPlayer?) {
                mainMenu(null)
            }
        })
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
            else->{}
        }
    }

    fun stagePointControlUpdate(point: Int, control: Boolean){
        val lastInd = levelKey.length
        val id =
            if (levelKey == "Stage Ufo"){
                1000
            }else{
                levelKey.substring(6, lastInd).toInt()
            }
        val stageEnt = AppRoomEntity(id, levelKey, point, control)
        Thread{
            db.stageDao().update(stageEnt)
        }.start()
    }

    fun stageStartFireDBCheck(point: Int, control: Boolean){
        //Warranty to make firedb exist at the start of the stage!
        stageRef.addListenerForSingleValueEvent(object: ValueEventListener{
            override fun onCancelled(p0: DatabaseError) {}
            override fun onDataChange(p0: DataSnapshot) {
                if (!p0.exists()){
                    stageRef.child("point").setValue(point.toLong())
                    stageRef.child("control").setValue(control)
                    Log.d(TAG, "stageStartFireDBCheck: fireDB updated")
                }
            }
        })
    }

    fun synchDBsBlock(roomControl:Boolean, fireControl:Boolean, roomPoint:Long, firePoint:String){
        runBlocking (Dispatchers.Default){
            val lastInd = levelKey.length
            val id =
                if (levelKey == "Stage Ufo"){
                    1000
                }else{
                    levelKey.substring(6, lastInd).toInt()
                }
            if (roomControl && fireControl) {
                stageRef.child("point").setValue(roomPoint)
                Log.d(TAG, "roomDB control = $roomControl => fireDB point updated")
            } else if (!roomControl && fireControl) {
                stageRef.child("point").setValue(roomPoint)
                stageRef.child("control").setValue(roomControl)
                Log.d(
                    TAG,
                    "roomDB control = $roomControl => fireDB point+control updated"
                )
            } else if (roomControl && !fireControl) {

                val stageEnt =
                    AppRoomEntity(id, levelKey, firePoint.toInt(), fireControl)
                db.stageDao().update(stageEnt)
                Log.d(TAG, "fireDB control = $fireControl => roomDB updated")
            } else if (!roomControl && !fireControl) {
                val stageEnt =
                    AppRoomEntity(id, levelKey, firePoint.toInt(), fireControl)
                db.stageDao().update(stageEnt)
                Log.d(TAG, "!roomControl && !fireControl => roomDB updated")
            } else if (roomControl != null && fireControl == null) {
                stageRef.child("point").setValue(roomPoint)
                stageRef.child("control").setValue(roomControl)
                Log.d(
                    TAG,
                    "roomDB control = $roomControl => fireDB point+control updated"
                )
            } else {
                Log.d(TAG, "roomControl == null or Something's Wrong!")
            }
        }
    }

    suspend fun synchronDBs(){
        Log.d(TAG, "synchrondbs start")

        val mValueEventListener = object :ValueEventListener{
            override fun onCancelled(p0: DatabaseError) {
                Log.d(TAG, "Listener:onCancelled")
            }
            override fun onDataChange(p0: DataSnapshot) {
                Thread {
                    if (!p0.exists()){
                        Log.d(TAG, "p0 does not exist")
                        var dbStage = db.stageDao().getOne(levelKey)
                        val roomPoint = dbStage.db_stage_points.toLong()
                        val roomControl = dbStage.db_stage_control
                        stageRef.child("point").setValue(roomPoint)
                        Log.d(TAG, "fireDB point set: $roomPoint")
                        stageRef.child("control").setValue(roomControl)
                            .addOnSuccessListener {
                                Log.d(TAG, "fireDB control set: $roomControl")
                                val sPoint = p0.child("point").value
                                Log.d(TAG, "sPoint = $sPoint")
                                var firePoint = sPoint.toString()
                                Log.d(TAG, "firePoint = $firePoint")
                                val fireControl = p0.child("control").value as Boolean
                                synchDBsBlock(roomControl,fireControl,roomPoint,firePoint)
                            }
                    }
                    else{
                        Log.d(TAG, "p0 exists")
                        var dbStage = db.stageDao().getOne(levelKey)
                        val roomPoint = dbStage.db_stage_points.toLong()
                        val roomControl = dbStage.db_stage_control
                        val sPoint = p0.child("point").value
                        Log.d(TAG, "sPoint: $sPoint")
                        var firePoint = sPoint.toString()
                        Log.d(TAG, "firePoint: $firePoint")
                        val fireControl = p0.child("control").value as Boolean
                        synchDBsBlock(roomControl,fireControl,roomPoint,firePoint)

                    }
                }.start()
            }
        }

        stageRef.addValueEventListener(mValueEventListener)

        delay(10)

        this.mValueEventListener = mValueEventListener
        delay(50)
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

        runBlocking(Dispatchers.Default) {
            synchronDBs()
        }

        Thread.sleep(50)

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
                        Thread{
                            val lastInd = levelKey.length
                            val id =
                                if (levelKey == "Stage Ufo"){
                                    1000
                                }else{
                                    levelKey.substring(6, lastInd).toInt()
                                }
                            val stageEnt = AppRoomEntity(id, levelKey, 0, false)
                            db.stageDao().update(stageEnt)
                            Log.d(TAG, "roomDB UPDATED: answer is wrong; -10 points")
                        }.start()
                        if (isRunning){
                            mainHandler.removeCallbacks(updatePointTask)
                        }else{
                            Log.d(TAG, "isRunning false")
                        }
                        mValueEventListener?.let { kopek ->
                            stageRef.removeEventListener(kopek)
                            Log.d(TAG, "stageRef EventListener Removed!")
                        }
                        window.dismiss()
                        startActivity(intent)
                    }
                    buttNo.setOnClickListener {
                        window.dismiss()
                        mValueEventListener?.let { kopek ->
                            stageRef.removeEventListener(kopek)
                            Log.d(TAG, "stageRef EventListener Removed!")
                        }
                    }
                }else{
                    Log.d(TAG, "Comments button pressed")

                    startActivity(intent)
                }
            }
        })
    }

    fun showNextStageGrid(){
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
            "Stage 23" -> {
                levelKey = "Stage 24"
                val intent = Intent(this@RandomActivity, OrderedActivity::class.java)
                intent.putExtra("levelKey", levelKey)
                intent.putExtra("tvName", nick)
                startActivity(intent)
            }
            else -> {}
        }
    }

    fun showNext(view:View?){
        ib_next.setColorFilter(resources.getColor(R.color.colorPurple))
        if (mInterstitialAd.isLoaded) {
            Log.d(TAG, "Ad Must be showed!!!")
            mInterstitialAd.show()
            mInterstitialAd.adListener = object : AdListener() {
                override fun onAdClosed() {
                    mInterstitialAd.loadAd(AdRequest.Builder().build())
                    showNextStageGrid()
                }
            }
        } else {
            Log.d(TAG, "The interstitial wasn't loaded yet.")
            showNextStageGrid()
        }
    }

    fun showBackStageGrid(){
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
            "Stage 23"->{
                levelKey = "Stage 22"
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
        if (mInterstitialAd.isLoaded) {
            Log.d(TAG, "Ad Must be showed!!!")
            mInterstitialAd.show()
            mInterstitialAd.adListener = object : AdListener() {
                override fun onAdClosed() {
                    mInterstitialAd.loadAd(AdRequest.Builder().build())
                    showBackStageGrid()
                }
            }
        } else {
            Log.d(TAG, "The interstitial wasn't loaded yet.")
            showBackStageGrid()
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

    private fun showSettings(view:View?){
        if (isRunning){
            mainHandler.removeCallbacks(updatePointTask)
        }else{
            Log.d(TAG, "isRunning false")
        }
        Log.d(TAG, "action_settings pressed!")
        val intent = Intent(this@RandomActivity, SettingsActivity::class.java)
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

    private fun privacyPolicy(){
        Log.d(TAG, "privacyPolicy pressed..")
        val intent = Intent(this, PrivacyActivity::class.java)
        startActivity(intent)
    }

    private fun termsConditions(){
        Log.d(TAG, "privacyPolicy pressed..")
        val intent = Intent(this, TermsActivity::class.java)
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
            item.itemId == R.id.action_settings -> showSettings(null)
            item.itemId == R.id.pivacy_policy -> privacyPolicy()
            item.itemId == R.id.terms_condition -> termsConditions()
            else -> {
            }
        }

        return when (item.itemId) {
            R.id.action_out -> true
            else -> super.onOptionsItemSelected(item)
        }
    }
}
