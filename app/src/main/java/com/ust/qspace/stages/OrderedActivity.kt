package com.ust.qspace.stages

import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.graphics.Color
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
import android.view.animation.AnimationUtils
import android.view.animation.OvershootInterpolator
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.ImageView
import android.widget.PopupWindow
import android.widget.RadioButton
import android.widget.Toast.LENGTH_SHORT
import android.widget.Toast.makeText
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.InterstitialAd
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.ust.qspace.*
import com.ust.qspace.R
import com.ust.qspace.models.SettingsPrefs
import com.ust.qspace.models.whiteFont
import com.ust.qspace.room.AppRoomEntity
import com.ust.qspace.trees.PrivacyActivity
import com.ust.qspace.trees.SettingsActivity
import com.ust.qspace.trees.TermsActivity
import kotlinx.android.synthetic.main.activity_ordered.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking

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
    var isRunning = false
    lateinit var mainHandler:Handler
    lateinit var updatePointTask: Runnable
    lateinit var window:PopupWindow

    private lateinit var mInterstitialAd: InterstitialAd
    private lateinit var mediaPlayer: MediaPlayer
    private var mValueEventListener: ValueEventListener? = null

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

        //InterstitialAd part
        mInterstitialAd = InterstitialAd(this)
        mInterstitialAd.adUnitId = "ca-app-pub-7262139641436003/7225179289"
        mInterstitialAd.loadAd(AdRequest.Builder().build())
        //

        qAnswer = mapOf("Stage 1" to 95, "Stage 2" to 12, "Stage 4" to 116, "Stage 5" to 119, "Stage 6" to 8,
            "Stage 7" to 99,"Stage 9" to 16, "Stage 10" to 6, "Stage 11" to 215674, "Stage 12" to 80, "Stage 13" to 63,
            "Stage 14" to 98, "Stage 15" to 7, "Stage 16" to 4, "Stage 17" to 87,"Stage 18" to 19, "Stage 19" to 200,
            "Stage 20" to 4, "Stage 21" to -3, "Stage 22" to 3, "Stage 24" to 35, "Stage 25" to 6, "Stage 26" to 1113122113,
            "Stage 27" to 16, "Stage 28" to 40, "Stage 29" to 49, "Stage 30" to 6)

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

        commentAnimation()
//        chrono.base = SystemClock.elapsedRealtime()
//        chrono.start()
        //force keyboard show
        /*textQ.isClickable = true
        textQ.setOnClickListener {
            Log.d(TAG, "textQclicked")
            val inputMethodManager = getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
            inputMethodManager.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY)
        }*/

        mediaPlayer = MediaPlayer.create(this, R.raw.win)
        mediaPlayer.isLooping = false
        mediaPlayer.setVolume(50f, 50f)

        window = PopupWindow(this)
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
                val id = levelKey.substring(6, lastInd).toInt()
                var stageEnt = AppRoomEntity(id, levelKey, 10004, true)
                db.stageDao().insert(stageEnt)
                stageRef.child("point").setValue(10000)
                stageRef.child("control").setValue(true)
                Log.d(TAG, "First run on $levelKey, adaptation DONE!")
                startcheck()
            }
        }.start()
    }

    override fun onStart() {
        Thread{
            startcheck()
        }.start()

        super.onStart()
    }

    fun stagePointControlUpdate(point: Int, control: Boolean){
        val lastInd = levelKey.length
        val id = levelKey.substring(6, lastInd).toInt()
        val stageEnt = AppRoomEntity(id, levelKey, point, control)
        Thread{
            db.stageDao().update(stageEnt)
        }.start()

    }

    private fun animationOrder(){
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

    private fun starAnimation(){
        val show = layoutInflater.inflate(R.layout.layout_popup, null)
//        window.isOutsideTouchable = true

        //                Pop up window
        val imageShow = show.findViewById<ImageView>(R.id.iv_spaceMedal)
        window.contentView = show
        window.showAtLocation(buttAnswer1,1,0,100)
        val scaleX = PropertyValuesHolder.ofFloat(View.SCALE_X, 0.5f,1f)
        val scaleY = PropertyValuesHolder.ofFloat(View.SCALE_Y, 0.5f,1f)
        val alpha = PropertyValuesHolder.ofFloat(View.ALPHA,0f,1f)
        ObjectAnimator.ofPropertyValuesHolder(show, scaleX,scaleY,alpha).apply {
            interpolator = OvershootInterpolator()
            duration = 600
        }.start()
//                show.startAnimation(atf1)
        imageShow.setOnClickListener{
            window.dismiss()
        }
        mediaPlayer.start()
        tv_answer1.isFocusable = false
        buttAnswer1.visibility = View.INVISIBLE
    }

    private fun commentAnimation(){
        val commAnim = AnimationUtils.loadAnimation(this, R.anim.commentbub)
        ibComment.visibility = View.VISIBLE
        ibComment.startAnimation(commAnim)
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

    fun slayButton(view: View?) {
        Log.d(TAG, "slayButton pressed")
        val kontrol = tv_answer1.text.toString()
        val inputMethodManager = getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        if(view != null){
            inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
        }
        if (kontrol.trim().isNotEmpty()) {
            runBlocking(Dispatchers.Default) {
                synchronDBs()
            }
            try {
                uAnswer = tv_answer1.text.toString().toInt()
                Log.d(TAG, "uAnswer is assigned as $uAnswer")
            } catch (ex: Exception) {
                Log.d(TAG, "Something's Wrong; uAnswer couldn't assign!")
                val toast = makeText(baseContext, "Please Enter a Valid Value", LENGTH_SHORT)
                toast.setGravity(Gravity.CENTER, 0, 0)
                toast.show()
            }

            stageRef.addListenerForSingleValueEvent(object:ValueEventListener{
                override fun onCancelled(p0: DatabaseError) {
                    Log.d(TAG, "stageRef Data couldn't read; No Internet Connection/No Response from database/Wrong datapath")
                }

                override fun onDataChange(p0: DataSnapshot) {
                    if (p0.exists()){
                        var point = p0.child("point").value as Long
                        val control = p0.child("control").value as Boolean
                        val userRef = databaseReference.child(uid)
                        if (control){
                            if (uAnswer == answer) {
                                try {
                                    mainHandler.removeCallbacks(updatePointTask)
                                }catch (ex:Exception){ex.message}

                                Thread{//update roomDB the answer is accepted
                                    val lastInd = levelKey.length
                                    val id = levelKey.substring(6, lastInd).toInt()
                                    val stageEnt = AppRoomEntity(id, levelKey, point.toInt(), false)
                                    db.stageDao().update(stageEnt)
                                    Log.d(TAG, "roomDB UPDATED: answer is accepted")
                                }.start()
                                stageRef.child("control").setValue(false)
                                userRef.addListenerForSingleValueEvent(object:ValueEventListener{
                                    override fun onCancelled(p0s: DatabaseError) {
                                        Log.d(TAG, "userRef Data couldn't read; No Internet Connection/No Response from database/Wrong datapath")
                                    }
                                    override fun onDataChange(p0s: DataSnapshot) {
                                        var levelP = p0s.child("level").value as String
                                        var points = p0s.child("points").value as Long
                                        points += point
                                        userRef.child("points").setValue(points)
                                        MainActivity().levelTagClarification()
                                        level = p0s.child("level").value as String
                                        if (levelP != level){
                                            when(level){
                                                "Epimetheus" -> {}
                                                "Atlas" -> {}
                                                "Hyperion" -> {}
                                                "Charon" -> {}
                                                "Mimas" -> {}
                                                "Triton" -> {}
                                                "Callisto" -> {}
                                                "Ganymede" -> {}
                                                "Europa" -> {}
                                                "Titan" -> {}
                                                "Moon" -> {}
                                                "Enceladus" -> {}
                                                "Pluto" -> {}
                                                "Mars" -> {}
                                                else -> {
                                                    Log.d(TAG, "Level didn't change!")
                                                }
                                            }
                                            mValueEventListener?.let {
                                                stageRef.removeEventListener(it)
                                                Log.d(TAG, "stageRef EventListener Removed!")
                                            }
                                        }
                                    }
                                })
                                starAnimation()

                                Log.d(TAG, "$levelKey: Answer ($uAnswer) is equal to $answer; Accepted!")
                                val toast = makeText(baseContext, getString(R.string.bravo), LENGTH_SHORT)
                                toast.setGravity(Gravity.TOP, 0, 100)
                                toast.show()
                            } else {
                                Thread{//Wrong answer => -10 points to roomDB
                                    val lastInd = levelKey.length
                                    val id = levelKey.substring(6, lastInd).toInt()
                                    point -= 100
                                    val stageEnt = AppRoomEntity(id, levelKey, point.toInt(), false)
                                    db.stageDao().update(stageEnt)
                                    stageRef.child("point").setValue(point)
                                    Log.d(TAG, "roomDB UPDATED: answer is wrong; -10 points")
                                }.start()
                                Log.d(TAG, "Something's Wrong; $uAnswer != $answer!")
                                val toast = makeText(baseContext, getString(R.string.wrong_answer), LENGTH_SHORT)
                                toast.setGravity(Gravity.CENTER, 0, 100)
                                toast.show()
                                mValueEventListener?.let {
                                    stageRef.removeEventListener(it)
                                    Log.d(TAG, "stageRef EventListener Removed!")
                                }
                            }
                        }else{
                            if(uAnswer == answer){
                                starAnimation()

                                Log.d(TAG, "$levelKey: Answer ($uAnswer) is equal to $answer; " +
                                        "But no points added to the database")
                                val toast = makeText(baseContext, getString(R.string.bravo), LENGTH_SHORT)
                                toast.setGravity(Gravity.TOP, 0, 100)
                                toast.show()
                                mValueEventListener?.let {
                                    stageRef.removeEventListener(it)
                                    Log.d(TAG, "stageRef EventListener Removed!")
                                }
                            }else{
                                Log.d(TAG, "Something's Wrong; $uAnswer != $answer!")
                                val toast = makeText(baseContext, getString(R.string.come_on), LENGTH_SHORT)
                                toast.setGravity(Gravity.CENTER, 0, 100)
                                toast.show()
                                mValueEventListener?.let {
                                    stageRef.removeEventListener(it)
                                    Log.d(TAG, "stageRef EventListener Removed!")
                                }
                            }
                        }
                    }else{
                        runBlocking(Dispatchers.Default) {
                            synchronDBs()
                        }
                        slayButton(null)
                    }
                }
            })


        }else {
            Log.d(TAG, "tv_answer1 is empty!")
            val toast = makeText(baseContext, getString(R.string.enter_answer), LENGTH_SHORT)
            toast.setGravity(Gravity.CENTER, 0, 100)
            toast.show()
        }
    }

    private fun animationUcgen(){
        val fadein = AnimationUtils.loadAnimation(this, R.anim.abc_fade_in)
        val animtv = AnimationUtils.loadAnimation(this, R.anim.abc_slide_in_bottom)
        groupUcgen.visibility = View.VISIBLE
        uc1_say1.startAnimation(fadein)
        uc1_say2.startAnimation(fadein)
        uc1_say3.startAnimation(fadein)
        uc2_say1.startAnimation(fadein)
        uc2_say2.startAnimation(fadein)
        uc2_say3.startAnimation(fadein)
        uc3_say1.startAnimation(fadein)
        uc3_say2.startAnimation(fadein)
        uc3_say3.startAnimation(fadein)
        uc4_say1.startAnimation(fadein)
        uc4_say2.startAnimation(fadein)
        uc4_say3.startAnimation(fadein)
        tv_answer1.startAnimation(animtv)
        buttAnswer1.startAnimation(animtv)
    }

    private fun showWhiteFonts(){
        val settings = SettingsPrefs(this)
        val whiteFonts = settings.getSetting(whiteFont)
        val levelK: Boolean = levelKey=="Stage 1" || levelKey=="Stage 4" || levelKey=="Stage 5" || levelKey=="Stage 7"
        val levelK1:Boolean = levelKey=="Stage 2" || levelKey=="Stage 9" || levelKey=="Stage 24"
        val levelK2:Boolean = levelKey=="Stage 11" || levelKey=="Stage 12" || levelKey=="Stage 13" ||
                levelKey=="Stage 14" || levelKey=="Stage 16" || levelKey=="Stage 17" || levelKey=="Stage 18" ||
                levelKey=="Stage 25" || levelKey=="Stage 26" || levelKey=="Stage 27" || levelKey=="Stage 28" ||
                levelKey=="Stage 29" || levelKey=="Stage 30"
        if (whiteFonts){
            if(levelK){
                say1.setTextColor(Color.WHITE)
                say2.setTextColor(Color.WHITE)
                say3.setTextColor(Color.WHITE)
                say4.setTextColor(Color.WHITE)
            }
            else if (levelK1){
                uc1_say1.setTextColor(Color.WHITE)
                uc1_say2.setTextColor(Color.WHITE)
                uc1_say3.setTextColor(Color.WHITE)
                uc2_say1.setTextColor(Color.WHITE)
                uc2_say2.setTextColor(Color.WHITE)
                uc2_say3.setTextColor(Color.WHITE)
                uc3_say1.setTextColor(Color.WHITE)
                uc3_say2.setTextColor(Color.WHITE)
                uc3_say3.setTextColor(Color.WHITE)
                uc4_say1.setTextColor(Color.WHITE)
                uc4_say2.setTextColor(Color.WHITE)
                uc4_say3.setTextColor(Color.WHITE)
            }
            else if (levelK2){
                tv_under.setTextColor(Color.WHITE)
                tv_under1.setTextColor(Color.MAGENTA)
            }
            else{}
        }

    }

    @SuppressLint("SetTextI18n")
    private fun levelAdapt(level: String) {
        val fadein = AnimationUtils.loadAnimation(this, R.anim.abc_fade_in)
        val settings = SettingsPrefs(this)
        val whiteFonts = settings.getSetting(whiteFont)
        when (level) {
            "Stage 1" -> {
                groupOrder.visibility = View.VISIBLE
                animationOrder()
                say1.text = "19"
                say2.text = "38"
                say3.text = "57"
                say4.text = "76"
                ib_next.visibility = View.VISIBLE
                ib_next.startAnimation(fadein)
                showWhiteFonts()
                Log.d(TAG, "$level adaptation is done successfully!")
            }
            "Stage 2" -> {
                ib_back.visibility = View.VISIBLE
                ib_next.visibility = View.VISIBLE
                ib_back.startAnimation(fadein)
                ib_next.startAnimation(fadein)
                uc4_say2.setTextColor(Color.WHITE)
                animationUcgen()
                showWhiteFonts()
                if (whiteFonts){
                    uc4_say2.setTextColor(Color.MAGENTA)
                }
                Log.d(TAG, "$level adaptation is done successfully!")
            }
            "Stage 4" -> {
                groupOrder.visibility = View.VISIBLE
                animationOrder()
                say1.text = "24"
                say2.text = "47"
                say3.text = "70"
                say4.text = "93"
                ib_back.visibility = View.VISIBLE
                ib_next.visibility = View.VISIBLE
                ib_back.startAnimation(fadein)
                ib_next.startAnimation(fadein)
                showWhiteFonts()
                Log.d(TAG, "$level adaptation is done successfully!")
            }
            "Stage 5" -> {
                groupOrder.visibility = View.VISIBLE
                animationOrder()
                say1.text = "80"
                say2.text = "88"
                say3.text = "104"
                say4.text = "109"
                ib_back.visibility = View.VISIBLE
                ib_next.visibility = View.VISIBLE
                ib_back.startAnimation(fadein)
                ib_next.startAnimation(fadein)
                showWhiteFonts()
                Log.d(TAG, "$level adaptation is done successfully!")
            }
            "Stage 6" -> {
                groupShapeVisibility()
                g4_iv1.visibility = View.INVISIBLE
                ib_back.visibility = View.VISIBLE
                ib_next.visibility = View.VISIBLE
                ib_back.startAnimation(fadein)
                ib_next.startAnimation(fadein)
                Log.d(TAG, "$level adaptation is done successfully!")
            }
            "Stage 7" -> {
                groupOrder.visibility = View.VISIBLE
                animationOrder()
                say1.text = "6=24"
                say2.text = "8=48"
                say3.text = "10=80"
                say4.text = "11= ?"
                textQ.text = "What should replace with '?'"
                say4.setTextColor(resources.getColor(R.color.colorSpaceWhite))
                ib_back.visibility = View.VISIBLE
                ib_next.visibility = View.VISIBLE
                ib_back.startAnimation(fadein)
                ib_next.startAnimation(fadein)
                showWhiteFonts()
                if(whiteFonts){
                    say4.setTextColor(Color.MAGENTA)
                }
                Log.d(TAG, "$level adaptation is done successfully!")
            }
            "Stage 9"->{
                ib_back.visibility = View.VISIBLE
                ib_next.visibility = View.VISIBLE
                ib_back.startAnimation(fadein)
                ib_next.startAnimation(fadein)
                uc3_say2.setTextColor(Color.WHITE)
                animationUcgen()
                uc1_say1.text = "7"
                uc1_say2.text = "17"
                uc1_say3.text = "27"
                uc2_say1.text = "11"
                uc2_say2.text = "17"
                uc2_say3.text = "23"
                uc3_say1.text = "10"
                uc3_say2.text = ".?"
                uc3_say3.text = "25"
                uc4_say1.text = "16"
                uc4_say2.text = "17"
                uc4_say3.text = "18"
                showWhiteFonts()
                if (whiteFonts){
                    uc3_say2.setTextColor(Color.MAGENTA)
                }
                Log.d(TAG, "$level adaptation is done successfully!")
            }
            "Stage 10"->{
                ib_back.visibility = View.VISIBLE
                ib_next.visibility = View.VISIBLE
                ib_back.startAnimation(fadein)
                ib_next.startAnimation(fadein)
                group_table.visibility = View.VISIBLE
                tv_table_r42.setTextColor(resources.getColor(R.color.colorSpaceWhite))
                tv_table_r42.background = resources.getDrawable(R.drawable.butt_profile_tabs)
                group_table.startAnimation(fadein)
                Log.d(TAG, "$level adaptation is done successfully!")
            }
            "Stage 11"->{
                ib_back.visibility = View.VISIBLE
                ib_next.visibility = View.VISIBLE
                ib_back.startAnimation(fadein)
                ib_next.startAnimation(fadein)
                group_under.visibility = View.VISIBLE
                tv_under.text = """6+3+4 = 182439
                    |9+2+4 = 183652
                    |8+6+4 = 483274
                    |5+4+5 = 202541
                """.trimMargin()
                tv_under1.text = "7+3+8 = ?"
                showWhiteFonts()
                group_under.startAnimation(fadein)
                Log.d(TAG, "$level adaptation is done successfully!")
            }
            "Stage 12"->{
                ib_back.visibility = View.VISIBLE
                ib_next.visibility = View.VISIBLE
                ib_back.startAnimation(fadein)
                ib_next.startAnimation(fadein)
                group_under.visibility = View.VISIBLE
                tv_under.text = """12       8       88
                    | 9        7       56""".trimMargin()
                tv_under1.text = "41        2         ?"
                showWhiteFonts()
                group_under.startAnimation(fadein)
                tv_under.gravity = Gravity.CENTER
                tv_under1.gravity = Gravity.CENTER
                Log.d(TAG, "$level adaptation is done successfully!")
            }
            "Stage 13"->{
                ib_back.visibility = View.VISIBLE
                ib_next.visibility = View.VISIBLE
                ib_back.startAnimation(fadein)
                ib_next.startAnimation(fadein)
                group_under.visibility = View.VISIBLE
                tv_under.text = "18,    46,    94,    ?,    52,    61"
                tv_under1.text = getString(R.string.missing_number)
                val param = tv_under1.layoutParams as ConstraintLayout.LayoutParams
                param.setMargins(0,16,0,0)
                tv_under1.layoutParams = param
                tv_under1.gravity = Gravity.CENTER_HORIZONTAL
                showWhiteFonts()
                group_under.startAnimation(fadein)
                Log.d(TAG, "$level adaptation is done successfully!")
            }
            "Stage 14"->{
                ib_back.visibility = View.VISIBLE
                ib_next.visibility = View.VISIBLE
                ib_back.startAnimation(fadein)
                ib_next.startAnimation(fadein)
                group_under.visibility = View.VISIBLE
                tv_under.text = """3+4 = 18
                    |3+7 = 27 
                    |5+8 = 60
                    |6+7 = 72
                """.trimMargin()
                tv_under1.text = "7+8 =  ?"
                tv_under.gravity = Gravity.START
                tv_under1.gravity = Gravity.START
                showWhiteFonts()
                group_under.startAnimation(fadein)
                Log.d(TAG, "$level adaptation is done successfully!")
            }
            "Stage 15"->{
                ib_back.visibility = View.VISIBLE
                ib_next.visibility = View.VISIBLE
                ib_back.startAnimation(fadein)
                ib_next.startAnimation(fadein)
                group_table_full.visibility = View.VISIBLE
                group_table_full.startAnimation(fadein)
                Log.d(TAG, "$level adaptation is done successfully!")
            }
            "Stage 16"->{
                ib_back.visibility = View.VISIBLE
                ib_next.visibility = View.VISIBLE
                ib_back.startAnimation(fadein)
                ib_next.startAnimation(fadein)
                group_under.visibility = View.VISIBLE
                tv_under.text = """1836 = 6
                    |1732 = 0
                    |9999 = 4
                    |8888 = 8
                """.trimMargin()
                tv_under1.text = "3919 = ?"
                showWhiteFonts()
                group_under.startAnimation(fadein)
                Log.d(TAG, "$level adaptation is done successfully!")
            }
            "Stage 17"->{
                ib_back.visibility = View.VISIBLE
                ib_next.visibility = View.VISIBLE
                ib_back.startAnimation(fadein)
                ib_next.startAnimation(fadein)
                group_under.visibility = View.VISIBLE
                tv_under.text = "l6,    06,    68,    88,    .?,    98"
                tv_under1.text = getString(R.string.missing_number)
                val param = tv_under1.layoutParams as ConstraintLayout.LayoutParams
                param.setMargins(0,16,0,0)
                tv_under1.layoutParams = param
                tv_under1.gravity = Gravity.CENTER_HORIZONTAL
                showWhiteFonts()
                group_under.startAnimation(fadein)
                Log.d(TAG, "$level adaptation is done successfully!")
            }
            "Stage 18"->{
                ib_back.visibility = View.VISIBLE
                ib_next.visibility = View.VISIBLE
                ib_back.startAnimation(fadein)
                ib_next.startAnimation(fadein)
                group_under.visibility = View.VISIBLE
                tv_under.text = """14                       7                        7
                    |
                    |
                    |5                        8                        21
                    |
                    |
                    |19                         ?                        12
                    |
                """.trimMargin()
                tv_under1.text = getString(R.string.missing_number)
                val param = tv_under1.layoutParams as ConstraintLayout.LayoutParams
                param.setMargins(0,16,0,0)
                tv_under1.gravity = Gravity.CENTER
                showWhiteFonts()
                group_under.startAnimation(fadein)
                Log.d(TAG, "$level adaptation is done successfully!")
            }
            "Stage 19"->{
                ib_back.visibility = View.VISIBLE
                ib_next.visibility = View.VISIBLE
                ib_back.startAnimation(fadein)
                ib_next.startAnimation(fadein)
                group_tableQ.visibility = View.VISIBLE
                group_tableQ.startAnimation(fadein)
                Log.d(TAG, "$level adaptation is done successfully!")
            }
            "Stage 20"->{
                ib_back.visibility = View.VISIBLE
                ib_next.visibility = View.VISIBLE
                ib_back.startAnimation(fadein)
                ib_next.startAnimation(fadein)
                group_gaga.visibility = View.VISIBLE
                group_gaga.startAnimation(fadein)
                tv_answer1.isFocusable = false
                Log.d(TAG, "$level adaptation is done successfully!")
            }
            "Stage 21"->{
                ib_back.visibility = View.VISIBLE
                ib_next.visibility = View.VISIBLE
                ib_back.startAnimation(fadein)
                ib_next.startAnimation(fadein)
                group_table_full.visibility = View.VISIBLE
                tv_full_r1s4.visibility = View.GONE
                tv_full_r2s4.visibility = View.GONE
                tv_full_r3s4.visibility = View.GONE
                tv_full_r4s4.visibility = View.GONE
                tv_full_r1s1.text = "8"
                tv_full_r1s2.text = "3"
                tv_full_r1s3.text = "32"
                tv_full_r2s1.text = "34"
                tv_full_r2s2.text = "29"
                tv_full_r2s3.text = "23"
                tv_full_r3s1.text = "42"
                tv_full_r3s2.text = "37"
                tv_full_r3s3.text = "32"
                tv_full_r4s1.text = "7"
                tv_full_r4s2.text = "?"
                tv_full_r4s3.text = "28"
                tv_full_r3s3.background = resources.getDrawable(R.drawable.custom_comment)
                tv_full_r3s3.setTextColor(resources.getColor(R.color.colorPrimaryDark))
                tv_full_r4s2.background = resources.getDrawable(R.drawable.custom_butt_profile_tabs)
                tv_full_r4s2.setTextColor(resources.getColor(R.color.colorSpaceWhite))
                Log.d(TAG, "$level adaptation is done successfully!")
            }
            "Stage 22"->{
                ib_back.visibility = View.VISIBLE
                ib_next.visibility = View.VISIBLE
                ib_back.startAnimation(fadein)
                ib_next.startAnimation(fadein)
                group_gaga.visibility = View.VISIBLE
                ivGaga.setImageResource(R.drawable.ic_tetrisl)
                val constraintSet = ConstraintSet()
                constraintSet.clone(group_gaga)
                constraintSet.clear(tv_qmark.id, ConstraintSet.END)
                constraintSet.connect(tv_qmark.id, ConstraintSet.START, group_gaga.id, ConstraintSet.START, 8)
                constraintSet.connect(tv_qmark.id, ConstraintSet.TOP, group_gaga.id, ConstraintSet.TOP, 16)
                constraintSet.applyTo(group_gaga)
                tv_qmark.textSize = 18f
                tv_qmark.text = "Which figure results\nby combining all four objects at top-right?"
                group_gaga.startAnimation(fadein)
                Log.d(TAG, "$level adaptation is done successfully!")
                tv_answer1.isFocusable = false
            }
            "Stage 24"->{
                ib_back.visibility = View.VISIBLE
                ib_next.visibility = View.VISIBLE
                ib_back.startAnimation(fadein)
                ib_next.startAnimation(fadein)
                uc3_say2.setTextColor(Color.WHITE)
                animationUcgen()
                uc1_say1.text = "7"
                uc1_say2.text = "13"
                uc1_say3.text = "29"
                uc2_say1.text = "11"
                uc2_say2.text = "19"
                uc2_say3.text = "37"
                uc3_say1.text = "15"
                uc3_say2.text = "27"
                uc3_say3.text = "59"
                uc4_say1.text = "19"
                uc4_say2.text = ".?"
                uc4_say3.text = "71"
                uc3_say2.setTextColor(resources.getColor(R.color.colorPurple))
                uc4_say2.setTextColor(resources.getColor(R.color.colorSpaceWhite))
                showWhiteFonts()
                if (whiteFonts){
                    uc4_say2.setTextColor(Color.MAGENTA)
                }
                Log.d(TAG, "$level adaptation is done successfully!")
            }
            "Stage 25"->{
                ib_back.visibility = View.VISIBLE
                ib_next.visibility = View.VISIBLE
                ib_back.startAnimation(fadein)
                ib_next.startAnimation(fadein)
                group_under.visibility = View.VISIBLE
                tv_under.text = """2+9 = 4
                    |4-2 = 9
                """.trimMargin()
                tv_under1.text = "5-13 = ?"
                val param = tv_under1.layoutParams as ConstraintLayout.LayoutParams
                param.setMargins(0,16,0,0)
                tv_under1.layoutParams = param
                showWhiteFonts()
                group_under.startAnimation(fadein)
                Log.d(TAG, "$level adaptation is done successfully!")
            }
            "Stage 26"->{
                ib_back.visibility = View.VISIBLE
                ib_next.visibility = View.VISIBLE
                ib_back.startAnimation(fadein)
                ib_next.startAnimation(fadein)
                group_under.visibility = View.VISIBLE
                tv_under.text = "13, 1113, 3113, 132113, ..?"
                tv_under1.text = getString(R.string.missing_number)
                val param = tv_under1.layoutParams as ConstraintLayout.LayoutParams
                param.setMargins(0,16,0,0)
                tv_under1.layoutParams = param
                tv_under1.gravity = Gravity.CENTER_HORIZONTAL
                showWhiteFonts()
                group_under.startAnimation(fadein)
                Log.d(TAG, "$level adaptation is done successfully!")
            }
            "Stage 27"->{
                ib_back.visibility = View.VISIBLE
                ib_next.visibility = View.VISIBLE
                ib_back.startAnimation(fadein)
                ib_next.startAnimation(fadein)
                group_under.visibility = View.VISIBLE
                tv_under.text = "11X11 = 4\n" +
                        "15X15 = 9"
                tv_under1.text = "14X14 = ?"
                val param = tv_under1.layoutParams as ConstraintLayout.LayoutParams
                param.setMargins(0,8,0,0)
                tv_under1.layoutParams = param
                showWhiteFonts()
                group_under.startAnimation(fadein)
                Log.d(TAG, "$level adaptation is done successfully!")
            }
            "Stage 28"->{
                ib_back.visibility = View.VISIBLE
                ib_next.visibility = View.VISIBLE
                ib_back.startAnimation(fadein)
                ib_next.startAnimation(fadein)
                group_under.visibility = View.VISIBLE
                tv_under.text = "7,    13,    18,    26,    29,    39,    .?."
                tv_under1.text = getString(R.string.missing_number)
                val param = tv_under1.layoutParams as ConstraintLayout.LayoutParams
                param.setMargins(0,16,0,0)
                tv_under1.layoutParams = param
                tv_under1.gravity = Gravity.CENTER_HORIZONTAL
                showWhiteFonts()
                group_under.startAnimation(fadein)
                Log.d(TAG, "$level adaptation is done successfully!")
            }
            "Stage 29"->{
                ib_back.visibility = View.VISIBLE
                ib_next.visibility = View.VISIBLE
                ib_back.startAnimation(fadein)
                ib_next.startAnimation(fadein)
                group_under.visibility = View.VISIBLE
                tv_under.text = "7+3 = 34\n" +
                        "8+2 = 39"
                tv_under1.text = "10-3 = ?"
                val param = tv_under1.layoutParams as ConstraintLayout.LayoutParams
                param.setMargins(0,8,0,0)
                tv_under1.layoutParams = param
                showWhiteFonts()
                group_under.startAnimation(fadein)
                Log.d(TAG, "$level adaptation is done successfully!")
            }
            "Stage 30"->{
                ib_back.visibility = View.VISIBLE
                ib_next.visibility = View.VISIBLE
                ib_back.startAnimation(fadein)
                ib_next.startAnimation(fadein)
                group_under.visibility = View.VISIBLE
                tv_under.text = "12149 = 77"
                tv_under1.text = "94 = ?"
                val param = tv_under1.layoutParams as ConstraintLayout.LayoutParams
                param.setMargins(0,16,0,0)
                tv_under1.layoutParams = param
                showWhiteFonts()
                group_under.startAnimation(fadein)
                Log.d(TAG, "$level adaptation is done successfully!")
            }
            else -> {
                Log.d(TAG, "Something's Wrong; levelAdapt is failed!")
            }
        }
    }

    fun showNextStageGrid(){
        when (levelKey){
            "Stage 1" -> {
                levelKey = "Stage 2"
                val intent = Intent(this@OrderedActivity, OrderedActivity::class.java)
                intent.putExtra("levelKey", levelKey)
                intent.putExtra("tvName", nick)
                startActivity(intent)
            }
            "Stage 2" -> {
                levelKey = "Stage 3"
                val intent = Intent(this@OrderedActivity, RandomActivity::class.java)
                intent.putExtra("levelKey", levelKey)
                intent.putExtra("tvName", nick)
                startActivity(intent)
            }
            "Stage 4" -> {
                levelKey = "Stage 5"
                val intent = Intent(this@OrderedActivity, OrderedActivity::class.java)
                intent.putExtra("levelKey", levelKey)
                intent.putExtra("tvName", nick)
                startActivity(intent)
            }
            "Stage 5" -> {
                levelKey = "Stage 6"
                val intent = Intent(this@OrderedActivity, OrderedActivity::class.java)
                intent.putExtra("levelKey", levelKey)
                intent.putExtra("tvName", nick)
                startActivity(intent)
            }
            "Stage 6" -> {
                levelKey = "Stage 7"
                val intent = Intent(this@OrderedActivity, OrderedActivity::class.java)
                intent.putExtra("levelKey", levelKey)
                intent.putExtra("tvName", nick)
                startActivity(intent)
            }
            "Stage 7" -> {
                levelKey = "Stage 8"
                val intent = Intent(this@OrderedActivity, RandomActivity::class.java)
                intent.putExtra("levelKey", levelKey)
                intent.putExtra("tvName", nick)
                startActivity(intent)
            }
            "Stage 9" -> {
                levelKey = "Stage 10"
                val intent = Intent(this@OrderedActivity, OrderedActivity::class.java)
                intent.putExtra("levelKey", levelKey)
                intent.putExtra("tvName", nick)
                startActivity(intent)
            }
            "Stage 10" -> {
                levelKey = "Stage 11"
                val intent = Intent(this@OrderedActivity, OrderedActivity::class.java)
                intent.putExtra("levelKey", levelKey)
                intent.putExtra("tvName", nick)
                startActivity(intent)
            }
            "Stage 11" -> {
                levelKey = "Stage 12"
                val intent = Intent(this@OrderedActivity, OrderedActivity::class.java)
                intent.putExtra("levelKey", levelKey)
                intent.putExtra("tvName", nick)
                startActivity(intent)
            }
            "Stage 12" -> {
                levelKey = "Stage 13"
                val intent = Intent(this@OrderedActivity, OrderedActivity::class.java)
                intent.putExtra("levelKey", levelKey)
                intent.putExtra("tvName", nick)
                startActivity(intent)
            }
            "Stage 13" -> {
                levelKey = "Stage 14"
                val intent = Intent(this@OrderedActivity, OrderedActivity::class.java)
                intent.putExtra("levelKey", levelKey)
                intent.putExtra("tvName", nick)
                startActivity(intent)
            }
            "Stage 14" -> {
                levelKey = "Stage 15"
                val intent = Intent(this@OrderedActivity, OrderedActivity::class.java)
                intent.putExtra("levelKey", levelKey)
                intent.putExtra("tvName", nick)
                startActivity(intent)
            }
            "Stage 15" -> {
                levelKey = "Stage 16"
                val intent = Intent(this@OrderedActivity, OrderedActivity::class.java)
                intent.putExtra("levelKey", levelKey)
                intent.putExtra("tvName", nick)
                startActivity(intent)
            }
            "Stage 16" -> {
                levelKey = "Stage 17"
                val intent = Intent(this@OrderedActivity, OrderedActivity::class.java)
                intent.putExtra("levelKey", levelKey)
                intent.putExtra("tvName", nick)
                startActivity(intent)
            }
            "Stage 17" -> {
                levelKey = "Stage 18"
                val intent = Intent(this@OrderedActivity, OrderedActivity::class.java)
                intent.putExtra("levelKey", levelKey)
                intent.putExtra("tvName", nick)
                startActivity(intent)
            }
            "Stage 18" -> {
                levelKey = "Stage 19"
                val intent = Intent(this@OrderedActivity, OrderedActivity::class.java)
                intent.putExtra("levelKey", levelKey)
                intent.putExtra("tvName", nick)
                startActivity(intent)
            }
            "Stage 19" -> {
                levelKey = "Stage 20"
                val intent = Intent(this@OrderedActivity, OrderedActivity::class.java)
                intent.putExtra("levelKey", levelKey)
                intent.putExtra("tvName", nick)
                startActivity(intent)
            }
            "Stage 20" -> {
                levelKey = "Stage 21"
                val intent = Intent(this@OrderedActivity, OrderedActivity::class.java)
                intent.putExtra("levelKey", levelKey)
                intent.putExtra("tvName", nick)
                startActivity(intent)
            }
            "Stage 21" -> {
                levelKey = "Stage 22"
                val intent = Intent(this@OrderedActivity, OrderedActivity::class.java)
                intent.putExtra("levelKey", levelKey)
                intent.putExtra("tvName", nick)
                startActivity(intent)
            }
            "Stage 22" -> {
                levelKey = "Stage 23"
                val intent = Intent(this@OrderedActivity, RandomActivity::class.java)
                intent.putExtra("levelKey", levelKey)
                intent.putExtra("tvName", nick)
                startActivity(intent)
            }
            "Stage 24" -> {
                levelKey = "Stage 25"
                val intent = Intent(this@OrderedActivity, OrderedActivity::class.java)
                intent.putExtra("levelKey", levelKey)
                intent.putExtra("tvName", nick)
                startActivity(intent)
            }
            "Stage 25" -> {
                levelKey = "Stage 26"
                val intent = Intent(this@OrderedActivity, OrderedActivity::class.java)
                intent.putExtra("levelKey", levelKey)
                intent.putExtra("tvName", nick)
                startActivity(intent)
            }
            "Stage 26" -> {
                levelKey = "Stage 27"
                val intent = Intent(this@OrderedActivity, OrderedActivity::class.java)
                intent.putExtra("levelKey", levelKey)
                intent.putExtra("tvName", nick)
                startActivity(intent)
            }
            "Stage 27" -> {
                levelKey = "Stage 28"
                val intent = Intent(this@OrderedActivity, OrderedActivity::class.java)
                intent.putExtra("levelKey", levelKey)
                intent.putExtra("tvName", nick)
                startActivity(intent)
            }
            "Stage 28" -> {
                levelKey = "Stage 29"
                val intent = Intent(this@OrderedActivity, OrderedActivity::class.java)
                intent.putExtra("levelKey", levelKey)
                intent.putExtra("tvName", nick)
                startActivity(intent)
            }
            "Stage 29" -> {
                levelKey = "Stage 30"
                val intent = Intent(this@OrderedActivity, OrderedActivity::class.java)
                intent.putExtra("levelKey", levelKey)
                intent.putExtra("tvName", nick)
                startActivity(intent)
            }
            else -> {}
        }
    }

    fun showNext(view: View?){
        if (isRunning){
            mainHandler.removeCallbacks(updatePointTask)
        }else{
            Log.d(TAG, "isRunning false")
        }
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
            "Stage 2" -> {
                levelKey = "Stage 1"
                val intent = Intent(this@OrderedActivity, OrderedActivity::class.java)
                intent.putExtra("levelKey", levelKey)
                intent.putExtra("tvName", nick)
                startActivity(intent)
            }
            "Stage 4" -> {
                levelKey = "Stage 3"
                val intent = Intent(this@OrderedActivity, RandomActivity::class.java)
                intent.putExtra("levelKey", levelKey)
                intent.putExtra("tvName", nick)
                startActivity(intent)
            }
            "Stage 5" -> {
                levelKey = "Stage 4"
                val intent = Intent(this@OrderedActivity, OrderedActivity::class.java)
                intent.putExtra("levelKey", levelKey)
                intent.putExtra("tvName", nick)
                startActivity(intent)
            }
            "Stage 6" -> {
                levelKey = "Stage 5"
                val intent = Intent(this@OrderedActivity, OrderedActivity::class.java)
                intent.putExtra("levelKey", levelKey)
                intent.putExtra("tvName", nick)
                startActivity(intent)
            }
            "Stage 7" -> {
                levelKey = "Stage 6"
                val intent = Intent(this@OrderedActivity, OrderedActivity::class.java)
                intent.putExtra("levelKey", levelKey)
                intent.putExtra("tvName", nick)
                startActivity(intent)
            }
            "Stage 9" -> {
                levelKey = "Stage 8"
                val intent = Intent(this@OrderedActivity, RandomActivity::class.java)
                intent.putExtra("levelKey", levelKey)
                intent.putExtra("tvName", nick)
                startActivity(intent)
            }
            "Stage 10" -> {
                levelKey = "Stage 9"
                val intent = Intent(this@OrderedActivity, OrderedActivity::class.java)
                intent.putExtra("levelKey", levelKey)
                intent.putExtra("tvName", nick)
                startActivity(intent)
            }
            "Stage 11" -> {
                levelKey = "Stage 10"
                val intent = Intent(this@OrderedActivity, OrderedActivity::class.java)
                intent.putExtra("levelKey", levelKey)
                intent.putExtra("tvName", nick)
                startActivity(intent)
            }
            "Stage 12" -> {
                levelKey = "Stage 11"
                val intent = Intent(this@OrderedActivity, OrderedActivity::class.java)
                intent.putExtra("levelKey", levelKey)
                intent.putExtra("tvName", nick)
                startActivity(intent)
            }
            "Stage 13" -> {
                levelKey = "Stage 12"
                val intent = Intent(this@OrderedActivity, OrderedActivity::class.java)
                intent.putExtra("levelKey", levelKey)
                intent.putExtra("tvName", nick)
                startActivity(intent)
            }
            "Stage 14" -> {
                levelKey = "Stage 13"
                val intent = Intent(this@OrderedActivity, OrderedActivity::class.java)
                intent.putExtra("levelKey", levelKey)
                intent.putExtra("tvName", nick)
                startActivity(intent)
            }
            "Stage 15" -> {
                levelKey = "Stage 14"
                val intent = Intent(this@OrderedActivity, OrderedActivity::class.java)
                intent.putExtra("levelKey", levelKey)
                intent.putExtra("tvName", nick)
                startActivity(intent)
            }
            "Stage 16" -> {
                levelKey = "Stage 15"
                val intent = Intent(this@OrderedActivity, OrderedActivity::class.java)
                intent.putExtra("levelKey", levelKey)
                intent.putExtra("tvName", nick)
                startActivity(intent)
            }
            "Stage 17" -> {
                levelKey = "Stage 16"
                val intent = Intent(this@OrderedActivity, OrderedActivity::class.java)
                intent.putExtra("levelKey", levelKey)
                intent.putExtra("tvName", nick)
                startActivity(intent)
            }
            "Stage 18" -> {
                levelKey = "Stage 17"
                val intent = Intent(this@OrderedActivity, OrderedActivity::class.java)
                intent.putExtra("levelKey", levelKey)
                intent.putExtra("tvName", nick)
                startActivity(intent)
            }
            "Stage 19" -> {
                levelKey = "Stage 18"
                val intent = Intent(this@OrderedActivity, OrderedActivity::class.java)
                intent.putExtra("levelKey", levelKey)
                intent.putExtra("tvName", nick)
                startActivity(intent)
            }
            "Stage 20" -> {
                levelKey = "Stage 19"
                val intent = Intent(this@OrderedActivity, OrderedActivity::class.java)
                intent.putExtra("levelKey", levelKey)
                intent.putExtra("tvName", nick)
                startActivity(intent)
            }
            "Stage 21" -> {
                levelKey = "Stage 20"
                val intent = Intent(this@OrderedActivity, OrderedActivity::class.java)
                intent.putExtra("levelKey", levelKey)
                intent.putExtra("tvName", nick)
                startActivity(intent)
            }
            "Stage 22" -> {
                levelKey = "Stage 21"
                val intent = Intent(this@OrderedActivity, OrderedActivity::class.java)
                intent.putExtra("levelKey", levelKey)
                intent.putExtra("tvName", nick)
                startActivity(intent)
            }
            "Stage 24" -> {
                levelKey = "Stage 23"
                val intent = Intent(this@OrderedActivity, RandomActivity::class.java)
                intent.putExtra("levelKey", levelKey)
                intent.putExtra("tvName", nick)
                startActivity(intent)
            }
            "Stage 25" -> {
                levelKey = "Stage 24"
                val intent = Intent(this@OrderedActivity, OrderedActivity::class.java)
                intent.putExtra("levelKey", levelKey)
                intent.putExtra("tvName", nick)
                startActivity(intent)
            }
            "Stage 26" -> {
                levelKey = "Stage 25"
                val intent = Intent(this@OrderedActivity, OrderedActivity::class.java)
                intent.putExtra("levelKey", levelKey)
                intent.putExtra("tvName", nick)
                startActivity(intent)
            }
            "Stage 27" -> {
                levelKey = "Stage 26"
                val intent = Intent(this@OrderedActivity, OrderedActivity::class.java)
                intent.putExtra("levelKey", levelKey)
                intent.putExtra("tvName", nick)
                startActivity(intent)
            }
            "Stage 28" -> {
                levelKey = "Stage 27"
                val intent = Intent(this@OrderedActivity, OrderedActivity::class.java)
                intent.putExtra("levelKey", levelKey)
                intent.putExtra("tvName", nick)
                startActivity(intent)
            }
            "Stage 29" -> {
                levelKey = "Stage 28"
                val intent = Intent(this@OrderedActivity, OrderedActivity::class.java)
                intent.putExtra("levelKey", levelKey)
                intent.putExtra("tvName", nick)
                startActivity(intent)
            }
            "Stage 30" -> {
                levelKey = "Stage 29"
                val intent = Intent(this@OrderedActivity, OrderedActivity::class.java)
                intent.putExtra("levelKey", levelKey)
                intent.putExtra("tvName", nick)
                startActivity(intent)
            }
            else -> {}
        }
    }

    fun showBack(view:View?){
        if (isRunning){
            mainHandler.removeCallbacks(updatePointTask)
        }else{
            Log.d(TAG, "isRunning false")
        }
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

    fun checkButton(view: View?){
        val radioId = radioGaga.checkedRadioButtonId

        val radioButton:RadioButton = findViewById(radioId)

        tv_answer1.setText(radioButton.text)
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

        runBlocking(Dispatchers.Default) {
            synchronDBs()
        }

        Thread.sleep(50)

        stageRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {}
            override fun onDataChange(p0: DataSnapshot) {

                val control = p0.child("control").value as Boolean
                Log.d(TAG, "control == $control")
                if (control){
                    window.contentView = show
                    window.showAtLocation(buttAnswer1,1,0,100)
                    show.startAnimation(atf1)
                    val buttYes = show.findViewById<Button>(R.id.buttYes)
                    val buttNo = show.findViewById<Button>(R.id.buttNo)
                    buttYes.setOnClickListener {
                        stageRef.child("point").setValue(0)
                        stageRef.child("control").setValue(false)
                        Thread{
                            val lastInd = levelKey.length
                            val id = levelKey.substring(6, lastInd).toInt()
                            val stageEnt = AppRoomEntity(id, levelKey, 0, false)
                            db.stageDao().update(stageEnt)
                            Log.d(TAG, "roomDB UPDATED: answer is wrong; -10 points")
                        }.start()
                        mValueEventListener?.let{
                            stageRef.removeEventListener(it)
                        }
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
                        mValueEventListener?.let {
                            stageRef.removeEventListener(it)
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

    fun synchDBsBlock(roomControl:Boolean, fireControl:Boolean, roomPoint:Long, firePoint:String){
        runBlocking (Dispatchers.Default){
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
                val lastInd = levelKey.length
                val id = levelKey.substring(6, lastInd).toInt()
                val stageEnt =
                    AppRoomEntity(id, levelKey, firePoint.toInt(), fireControl)
                db.stageDao().update(stageEnt)
                Log.d(TAG, "fireDB control = $fireControl => roomDB updated")
            } else if (!roomControl && !fireControl) {
                val lastInd = levelKey.length
                val id = levelKey.substring(6, lastInd).toInt()
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
    }

    private fun groupShapeVisibility(){
        val fadein = AnimationUtils.loadAnimation(this, R.anim.abc_fade_in)
        val animtv = AnimationUtils.loadAnimation(this, R.anim.abc_slide_in_bottom)
        g1_iv1.visibility = View.VISIBLE
        g1_tv1.visibility = View.VISIBLE
        g1_iv2.visibility = View.VISIBLE
        g1_tv2.visibility = View.VISIBLE
        g1_tv_result.visibility = View.VISIBLE
        g2_iv1.visibility = View.VISIBLE
        g2_tv1.visibility = View.VISIBLE
        g2_iv2.visibility = View.VISIBLE
        g2_tv2.visibility = View.VISIBLE
        g2_tv_result.visibility = View.VISIBLE
        g3_iv1.visibility = View.VISIBLE
        g3_tv1.visibility = View.VISIBLE
        g3_iv2.visibility = View.VISIBLE
        g3_tv2.visibility = View.VISIBLE
        g3_tv_result.visibility = View.VISIBLE
        g4_tv1.visibility = View.VISIBLE
        g4_iv2.visibility = View.VISIBLE
        g4_tv2.visibility = View.VISIBLE
        g4_tv_result.visibility = View.VISIBLE
        gQ_iv1.visibility = View.VISIBLE
        gQ_tv1.visibility = View.VISIBLE
        g4_iv1.visibility = View.VISIBLE
        g4_ivtv1.visibility = View.VISIBLE
        g1_iv1.startAnimation(fadein)
        g1_tv1.startAnimation(fadein)
        g1_iv2.startAnimation(fadein)
        g1_tv2.startAnimation(fadein)
        g1_tv_result.startAnimation(fadein)
        g2_iv1.startAnimation(fadein)
        g2_tv1.startAnimation(fadein)
        g2_iv2.startAnimation(fadein)
        g2_tv2.startAnimation(fadein)
        g2_tv_result.startAnimation(fadein)
        g3_iv1.startAnimation(fadein)
        g3_tv1.startAnimation(fadein)
        g3_iv2.startAnimation(fadein)
        g3_tv2.startAnimation(fadein)
        g3_tv_result.startAnimation(fadein)
        g4_tv1.startAnimation(fadein)
        g4_iv2.startAnimation(fadein)
        g4_tv2.startAnimation(fadein)
        g4_tv_result.startAnimation(fadein)
        gQ_iv1.startAnimation(fadein)
        gQ_tv1.startAnimation(fadein)
        g4_iv1.startAnimation(fadein)
        g4_ivtv1.startAnimation(fadein)
        tv_answer1.startAnimation(animtv)
        buttAnswer1.startAnimation(animtv)
    }

    fun mainMenu(view: View?) {
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

    private fun showSettings(view:View?){
        if (isRunning){
            mainHandler.removeCallbacks(updatePointTask)
        }else{
            Log.d(TAG, "isRunning false")
        }
        Log.d(TAG, "action_settings pressed!")
        val intent = Intent(this@OrderedActivity, SettingsActivity::class.java)
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
        startActivity(Intent(this@OrderedActivity, LoginActivity::class.java))
        this@OrderedActivity.finish()
    }

    override fun onBackPressed() {
        window.dismiss()
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


