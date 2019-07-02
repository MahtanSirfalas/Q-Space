package com.ust.spaceq

import android.annotation.TargetApi
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.SystemClock
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_one.*
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

private lateinit var firebaseAnalytics: FirebaseAnalytics
private lateinit var auth: FirebaseAuth
private lateinit var database: FirebaseDatabase
private lateinit var databaseReference: DatabaseReference
private lateinit var commsReference: DatabaseReference
var seviye = "Q0"


@TargetApi(Build.VERSION_CODES.O)
class MainActivity : AppCompatActivity() {

    val manager = supportFragmentManager
    val TAG = "MainActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        firebaseAnalytics = FirebaseAnalytics.getInstance(this)
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
        databaseReference = database.reference.child("Users")
        val uid = intent.getStringExtra("uid")
        val user = auth.currentUser
        val userReference = databaseReference.child(user!!.uid)

        /*userName.text  = userReference.orderByChild("nickName").toString()*/

        userReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {
                tvName.text = p0.child("nickName").value as? String
            }
            override fun onCancelled(p0: DatabaseError) {}
        })

        view_timer.base = SystemClock.elapsedRealtime()
        view_timer.start()

        buttDeneme.setOnClickListener {
            view_timer.stop()
            var zaman = view_timer.text.toString()
            textDeneme.text = zaman
        }
        commsReference = database.reference.child("Posts")

        showFragmentMenu()
        /*val tx = fragmentManager.beginTransation()
        tx.replace(android.R.id.fragment, FragmentLvl()).addToBackStack("tag").commit()*/

    }

    fun showFragmentMenu(){
        val transaction = manager.beginTransaction()
        val fragment = FragmentMenu()
        transaction.replace(R.id.fragment_holder, fragment)
        transaction.addToBackStack(null)
        transaction.commit()
    }

    fun showFragmentLvl(view: View?) {
        val transaction = manager.beginTransaction()
        val fragment = FragmentLvl()
        transaction.replace(R.id.fragment_holder, fragment)
        transaction.addToBackStack(null)
        transaction.commit()
    }

    fun showFragmentProfile(view: View?){
        Log.d(TAG, "Profile pressed")
        val transaction = manager.beginTransaction()
        val fragment = FragmentProfile()
        transaction.replace(R.id.fragment_holder, fragment)
        transaction.addToBackStack(null)
        transaction.commit()
    }

    fun showFragmentOne(view: View?){
        Log.d(TAG, "buttL1 pressed")
        seviye = "Q1"
        val transaction = manager.beginTransaction()
        val fragment = FragmentOne()
        transaction.replace(R.id.fragment_holder, fragment)
        transaction.addToBackStack(null)
        transaction.commit()
    }

    fun slayButton(view: View?){
        Log.d(TAG, "Slay button pressed")
        val answer = answer.text.toString()
        if (answer == "116"){
            showFragmentLvl(null)
        }else{
            Toast.makeText(baseContext, "Wrong Answer!", Toast.LENGTH_SHORT).show()
        }
    }

    fun showFragmentComment(view: View?){
        val intent = Intent(this@MainActivity, CommentActivity::class.java)
        intent.putExtra("seviye", seviye)

        startActivity(intent)

    }


    fun signOut(view:View?){
        auth.signOut()
        startActivity(Intent(this@MainActivity, LoginActivity::class.java))
        this@MainActivity.finish()
        view_timer.stop()
    }

    override fun onBackPressed() {
        showFragmentMenu()
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
        if (item.itemId == R.id.action_out){
            signOut(null)
        }else if (item.itemId == R.id.action_profile){
            showFragmentProfile(null)
        }

        return when (item.itemId) {
            R.id.action_out -> true
            else -> super.onOptionsItemSelected(item)
        }
    }
}
