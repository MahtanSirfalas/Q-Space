package com.ust.spaceq

import android.annotation.TargetApi
import android.content.Intent
import android.graphics.drawable.AnimationDrawable
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

import kotlinx.android.synthetic.main.activity_suggest.*
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

private lateinit var auth: FirebaseAuth
private lateinit var database: FirebaseDatabase
private lateinit var suggestRef: DatabaseReference

@TargetApi(Build.VERSION_CODES.O)
class SuggestActivity : AppCompatActivity() {
    val TAG = "SuggestActivity"
    val now = LocalDateTime.now()
    var date = DateTimeFormatter.ofPattern("yyyy:MM:dd")
    var time = DateTimeFormatter.ofPattern("HH:mm:ss")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_suggest)
        setSupportActionBar(toolbar)

        val constraintLayout = findViewById<ConstraintLayout>(R.id.layoutbg)
        val animationDrawable = constraintLayout.background as AnimationDrawable
        animationDrawable.setEnterFadeDuration(2000)
        animationDrawable.setExitFadeDuration(4000)
        animationDrawable.start()

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
        suggestRef = database.reference.child("Suggests/$uid")

        showSuggest(null)

        animations()

        buttSend.setOnClickListener {
            suggestRef.child("question").setValue(etSuggest.text.toString())
            suggestRef.child("when").setValue(date.format(now)+" | "+time.format(now))
            suggestRef.child("nickName").setValue(uName)
            showSuggest(null)
        }
        ibRemove.setOnClickListener {
            suggestRef.child("question").removeValue()
            suggestRef.child("when").removeValue()
            suggestRef.child("nickName").removeValue()
            etSuggest.text.clear()
            tvZaman.text = getString(R.string.date_time)
            showSuggest(null)
        }
    }

    private fun showSuggest(view: View?){
        suggestRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
            }

            override fun onDataChange(p0: DataSnapshot) {
                if (p0.exists() && p0.hasChildren()){
                    val question = p0.child("question").value as String
                    val zaman = p0.child("when").value as String
                    etSuggest.setText(question)
                    tvZaman.text = zaman
                }else{}
            }

        })
    }

    private fun animations(){
        val animtop = AnimationUtils.loadAnimation(this, R.anim.abc_slide_in_top)
        val animbottom = AnimationUtils.loadAnimation(this, R.anim.abc_slide_in_bottom)
        val atf1 = AnimationUtils.loadAnimation(this, R.anim.atf1)
        tvSuggest.startAnimation(animtop)
        etSuggest.startAnimation(atf1)
        tvZaman.startAnimation(atf1)
        buttSend.startAnimation(animbottom)
        ibRemove.startAnimation(animbottom)
    }

    private fun mainMenu(view: View?){
        Log.d(TAG, "mainMenu pressed..")
        val intent = Intent(this@SuggestActivity, MainActivity::class.java)
        startActivity(intent)
    }

    fun showProfile(view: View?){
        Log.d(TAG, "Profile pressed..")
        val intent = Intent(this@SuggestActivity, ProfileActivity::class.java)
        startActivity(intent)
    }

    fun signOut(view: View?){
        Log.d(TAG, "signOut pressed..")
        auth.signOut()
        startActivity(Intent(this@SuggestActivity, LoginActivity::class.java))
        this@SuggestActivity.finish()
    }

    override fun onBackPressed() {
        this.finish()
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

}
