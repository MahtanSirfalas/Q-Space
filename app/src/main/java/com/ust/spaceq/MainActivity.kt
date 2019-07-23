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
import android.widget.PopupWindow
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.squareup.picasso.Picasso

import kotlinx.android.synthetic.main.activity_main.*

private lateinit var firebaseAnalytics: FirebaseAnalytics
private lateinit var auth: FirebaseAuth
private lateinit var database: FirebaseDatabase
private lateinit var databaseReference: DatabaseReference
private lateinit var googleSignInClient: GoogleSignInClient
private lateinit var commsReference: DatabaseReference
var firstRunControl = true

lateinit var email: String
lateinit var uid: String
lateinit var avatar: String
lateinit var uName: String
var points: Long = 0
lateinit var level: String
var verifiedCheck = false

@TargetApi(Build.VERSION_CODES.O)
class MainActivity : AppCompatActivity() {

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
        user.reload()
        val userReference = databaseReference.child(uid)

        /*userName.text  = userReference.orderByChild("nickName").toString()*/

        userReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {
                uName = p0.child("nickName").value as String
                avatar = p0.child("avatar").value as String
                points = p0.child("points").value as Long
                Picasso.get().load(avatar).into(iv_avatar_circle)
                tvName.text = uName
                Log.d(TAG, "onCreate: avatar and uName assigned")
                animationTop()
                levelTagClarification()
            }
            override fun onCancelled(p0: DatabaseError) {
                Log.d(TAG, "Something's Wrong: User information get FAILED")
                Toast.makeText(baseContext, R.string.listener_cancelled,Toast.LENGTH_LONG).show()
            }
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
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)
        commsReference = database.reference.child("Posts")
        //Verified or Not Warning!
        verifiedCheck = user.isEmailVerified
        if (verifiedCheck){
            Log.d(TAG, "verifiedCheck = ${user.isEmailVerified}")
            groupWarn.visibility = View.GONE
        }else{
            Log.d(TAG, "verifiedCheck = ${user.isEmailVerified}")
            groupWarn.visibility = View.VISIBLE
        }
    }

    public fun levelTagClarification(){
        val userReference = databaseReference.child(uid)
        when (points) {
            in 1..1999 -> {userReference.child("level").setValue("Epimetheus")}
            in 2000..3999 -> {userReference.child("level").setValue("Atlas")}
            in 4000..5999 -> {userReference.child("level").setValue("Hyperion")}
            in 6000..8499 -> {userReference.child("level").setValue("Charon")}
            in 8500..10999 -> {userReference.child("level").setValue("Mimas")}
            in 11000..13999 -> {userReference.child("level").setValue("Triton")}
            in 14000..18999 -> {userReference.child("level").setValue("Callisto")}
            in 17000..20499 -> {userReference.child("level").setValue("Ganymede")}
            in 20500..23999 -> {userReference.child("level").setValue("Europa")}
            in 24000..27999 -> {userReference.child("level").setValue("Titan")}
            in 28000..32499 -> {userReference.child("level").setValue("Moon")}
            in 32500..37499 -> {userReference.child("level").setValue("Enceladus")}
            in 37500..42999 -> {userReference.child("level").setValue("Pluto")}
            in 43000..59999 -> {userReference.child("level").setValue("Pluto")}
            else -> {}
        }
    }

    private fun animationTop(){
        val window = PopupWindow(this)
        val show = layoutInflater.inflate(R.layout.layout_popup_internet, null)
        window.isOutsideTouchable = true
        val fadein = AnimationUtils.loadAnimation(this, R.anim.abc_fade_in)
        val atf = AnimationUtils.loadAnimation(this, R.anim.atf)
        val rtl = AnimationUtils.loadAnimation(this, R.anim.rtl)
        iv_avatar_circle.visibility = View.VISIBLE
        iv_avatar_circle.startAnimation(atf)

        atf.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(p0: Animation?) {}
            override fun onAnimationRepeat(p0: Animation?) {}
            override fun onAnimationEnd(p0: Animation?) {
                if (firstRunControl == true){
                    window.contentView = show
                    window.showAtLocation(layoutbg,1,0,0)
                    show.startAnimation(fadein)
                    firstRunControl = false
                }else{Log.d(TAG, "Not first run!")}
                tvName.visibility = View.VISIBLE
                tvName.startAnimation(rtl)
            }
        })
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
        val gfo2 = AnimationUtils.loadAnimation(this, R.anim.gfo2)
        val fo = AnimationUtils.loadAnimation(this, R.anim.abc_fade_out)
        val profil = AnimationUtils.loadAnimation(this, R.anim.profil)
        ivProfile.startAnimation(gfo2)
        buttProfil.startAnimation(fo)
        fo.setAnimationListener(object : Animation.AnimationListener{
            override fun onAnimationRepeat(p0: Animation?) {}
            override fun onAnimationStart(p0: Animation?) {}
            override fun onAnimationEnd(p0: Animation?) {buttProfil.visibility = View.INVISIBLE}
        })
        gfo2.setAnimationListener(object : Animation.AnimationListener {
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
        googleSignInClient.revokeAccess().addOnCompleteListener(this) {
            LoginActivity().updateUI(null)
        }
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