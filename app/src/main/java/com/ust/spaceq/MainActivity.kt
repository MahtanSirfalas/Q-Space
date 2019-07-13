package com.ust.spaceq

import android.annotation.TargetApi
import android.content.Intent
import android.graphics.drawable.AnimationDrawable
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isInvisible
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.squareup.picasso.Picasso

import kotlinx.android.synthetic.main.activity_main.*
import java.util.*
import kotlin.concurrent.schedule
import kotlin.concurrent.thread

private lateinit var firebaseAnalytics: FirebaseAnalytics
private lateinit var auth: FirebaseAuth
private lateinit var database: FirebaseDatabase
private lateinit var databaseReference: DatabaseReference
private lateinit var commsReference: DatabaseReference
private lateinit var uAnswer: Any
private var cevap:Int = 1

lateinit var email: String
lateinit var uid: String
lateinit var avatar: String
lateinit var uName: String



@TargetApi(Build.VERSION_CODES.O)
class MainActivity : AppCompatActivity() {

    val manager = supportFragmentManager
    val TAG = "MainActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        val constraintLayout = findViewById<ConstraintLayout>(R.id.layoutbg)
        val animationDrawable = constraintLayout.background as AnimationDrawable
        animationDrawable.setEnterFadeDuration(2000)
        animationDrawable.setExitFadeDuration(4000)
        animationDrawable.start()

        animation()

        firebaseAnalytics = FirebaseAnalytics.getInstance(this)
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
        databaseReference = database.reference.child("Users")

        val user = auth.currentUser
        uid = user!!.uid
        email = user.email.toString()
        val userReference = databaseReference.child(uid)

        /*userName.text  = userReference.orderByChild("nickName").toString()*/

        userReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {
                avatar = p0.child("avatar").value as String
                Picasso.get().load(avatar).into(iv_avatar_circle)
                uName = p0.child("nickName").value as String
                tvName.text = uName
                Log.d(TAG, "onCreate: avatar and uName assigned")
                animationTop()
            }
            override fun onCancelled(p0: DatabaseError) {}
        })
        //sayaç burada yatmaktadır yiğen
//        view_timer.base = SystemClock.elapsedRealtime()
//        view_timer.start()
//
//        buttDeneme.setOnClickListener {
//            view_timer.stop()
//            var zaman = view_timer.text.toString()
//            textDeneme.text = zaman
//        }
        commsReference = database.reference.child("Posts")

        /*val tx = fragmentManager.beginTransation()
        tx.replace(android.R.id.fragment, FragmentLvl()).addToBackStack("tag").commit()*/

//        val tvAnswer = findViewById<EditText>(R.id.tv_answer1)
//        val answerTextWatcher = object : TextWatcher {
//
//            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
//
//            }
//
//            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
//                val answerInput = tvAnswer.text.toString().trim()
//
//                buttAnswer1.isEnabled = answerInput.isNotEmpty()
//
//            }
//
//            override fun afterTextChanged(s: Editable) {
//                userInput = tvAnswer.text.toString().toInt()
//            }
//        }
//        tvAnswer.addTextChangedListener(answerTextWatcher)
    }
    private fun animationTop(){
        val rtl = AnimationUtils.loadAnimation(this, R.anim.rtl)
        val rtl1 = AnimationUtils.loadAnimation(this, R.anim.rtl1)
        val atf = AnimationUtils.loadAnimation(this, R.anim.atf)
        buttLogout.visibility = View.VISIBLE
        iv_avatar_circle.visibility = View.VISIBLE
        tvName.visibility = View.VISIBLE
        buttLogout.startAnimation(rtl)
        iv_avatar_circle.startAnimation(atf)
        tvName.startAnimation(rtl1)
    }

    private fun animation(){
        val stf = AnimationUtils.loadAnimation(this, R.anim.stf)
        val atf = AnimationUtils.loadAnimation(this, R.anim.atf)
        ivPlay.startAnimation(stf)
        ivProfile.startAnimation(stf)
        ivSuggestQ.startAnimation(stf)
        ivInfo.startAnimation(stf)
        buttOyna.startAnimation(atf)
        buttProfil.startAnimation(atf)
        buttSoru.startAnimation(atf)
        buttInfo.startAnimation(atf)
    }

    fun showLvl(view: View?) {
        val intent = Intent(this@MainActivity, LvlActivity::class.java)
        val gfo = AnimationUtils.loadAnimation(this, R.anim.gfo)
        val fo = AnimationUtils.loadAnimation(this, R.anim.abc_fade_out)
        ivPlay.startAnimation(gfo)
        buttOyna.startAnimation(fo)
        fo.setAnimationListener(object : Animation.AnimationListener{
            override fun onAnimationRepeat(p0: Animation?) {}
            override fun onAnimationStart(p0: Animation?) {}
            override fun onAnimationEnd(p0: Animation?) {buttOyna.visibility = View.INVISIBLE}
        })
        gfo.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(arg0: Animation) {
                intent.putExtra("tvName", tvName.text.toString())
            }
            override fun onAnimationRepeat(arg0: Animation) {}
            override fun onAnimationEnd(arg0: Animation) {
                startActivity(intent)
                ivPlay.visibility = View.INVISIBLE
            }
        })
    }

    fun showProfile(view: View?){
        Log.d(TAG, "Profile pressed")
        val intent = Intent(this@MainActivity, ProfileActivity::class.java)
        val gfo = AnimationUtils.loadAnimation(this, R.anim.gfo)
        val fo = AnimationUtils.loadAnimation(this, R.anim.abc_fade_out)
        ivProfile.startAnimation(gfo)
        buttProfil.startAnimation(fo)
        fo.setAnimationListener(object : Animation.AnimationListener{
            override fun onAnimationRepeat(p0: Animation?) {}
            override fun onAnimationStart(p0: Animation?) {}
            override fun onAnimationEnd(p0: Animation?) {buttProfil.visibility = View.INVISIBLE}
        })
        gfo.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(arg0: Animation) {
                intent.putExtra("tvName", tvName.text.toString())
            }
            override fun onAnimationRepeat(arg0: Animation) {}
            override fun onAnimationEnd(arg0: Animation) {
                startActivity(intent)
                ivProfile.visibility = View.INVISIBLE
            }
        })
    }

    fun showSuggestQ(view: View?){
        Log.d(TAG, "Suggest-Q pressed")
        val intent = Intent(this@MainActivity,SuggestActivity::class.java)
        val gfo = AnimationUtils.loadAnimation(this, R.anim.gfo)
        val fo = AnimationUtils.loadAnimation(this, R.anim.abc_fade_out)
        ivSuggestQ.startAnimation(gfo)
        buttSoru.startAnimation(fo)
        fo.setAnimationListener(object : Animation.AnimationListener{
            override fun onAnimationRepeat(p0: Animation?) {}
            override fun onAnimationStart(p0: Animation?) {}
            override fun onAnimationEnd(p0: Animation?) {buttSoru.visibility = View.INVISIBLE}
        })
        gfo.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(arg0: Animation) {}
            override fun onAnimationRepeat(arg0: Animation) {}
            override fun onAnimationEnd(arg0: Animation) {
                startActivity(intent)
                ivSuggestQ.visibility = View.INVISIBLE
            }
        })
    }

//    fun slayButton(view: View?){
//        Log.d(TAG, "Slay button pressed")
//        uAnswer = (
//            when(seviye){
//                "Q1" -> try {tv_answer1.text.toString().toInt()}
//                        catch (ex:Exception){ex.message
//                            Log.d(TAG, "answer1 is null!")
//                            Toast.makeText(baseContext, "Please Enter A Value", Toast.LENGTH_SHORT).show()}
//                "Q2" -> try{tv_answer2.text.toString().toInt()}
//                        catch (ex:Exception){ ex.message
//                            Log.d(TAG, "answer2 is null!")
//                            Toast.makeText(baseContext, "Please Enter A Value", Toast.LENGTH_SHORT).show()}
//                else -> Log.d(TAG, "WHEN operator didn't work!")
//            })
//        if (uAnswer == cevap){
//            Log.d(TAG, "$seviye: Answer ($uAnswer) is accepted")
//            Toast.makeText(baseContext, "Bravo! answer is accepted!", Toast.LENGTH_SHORT).show()
//            val bos = ""
//            when(seviye){
//                "Q1" -> {
//                    seviye = "Q0"
//                    showLvl(null)
//                    FragmentOne().onDestroy()
//                    FragmentOne().onDetach()
//                }
//                "Q2" -> {
//                    seviye = "Q0"
//                    showLvl(null)
//                    FragmentTwo().onDestroy()
//                    FragmentTwo().onDetach()
//                        }
//                else -> Log.d(TAG, "Something's Wrong; Clearing Answers")
//            }
//
//        }else{
//            Log.d(TAG, "$seviye:Answer is wrong; $uAnswer is not equal to $cevap")
//            Toast.makeText(baseContext, "Wrong Answer!", Toast.LENGTH_SHORT).show()
//        }
//    }

//    fun showFragmentComment(view: View?){
//        val intent = Intent(this@MainActivity, CommentActivity::class.java)
//        intent.putExtra("seviye", seviye)
//        intent.putExtra("tvName", tvName.text.toString())
//        intent.putExtra("avatar", avatarUrl)
//
//        startActivity(intent)
//
//    }

    private fun mainMenu(view: View?){
        Log.d(TAG, "mainMenu pressed..")
        val intent = Intent(this@MainActivity, MainActivity::class.java)
        startActivity(intent)
    }

    fun signOut(view:View?){
        auth.signOut()
        startActivity(Intent(this@MainActivity, LoginActivity::class.java))
        this@MainActivity.finish()
//        view_timer.stop()
    }

    override fun onBackPressed() {
        mainMenu(null)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
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

//    Tried to adapt answers in a single method, failed!!!
//    private fun answerAdapter(sonek: String){
//        when(sonek) {
//            "Q1" -> answerQ1.text.toString().toInt()
//            "Q2" -> answerQ2.text.toString().toInt()
//            else -> {
//                Log.d(TAG, "answer func. failed!!!")
//            }
//        }
//    }

}