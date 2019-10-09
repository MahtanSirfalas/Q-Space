package com.ust.qspace.trees

import android.content.Intent
import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.ust.qspace.LoginActivity
import com.ust.qspace.MainActivity
import com.ust.qspace.ProfileActivity
import com.ust.qspace.R

import kotlinx.android.synthetic.main.activity_privacy.*

class PrivacyActivity : AppCompatActivity() {

    val TAG = "PrivacyActivity"
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_privacy)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        auth = FirebaseAuth.getInstance()

        val link = findViewById<TextView>(R.id.privacy_body)
        link.movementMethod = LinkMovementMethod.getInstance()

    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    private fun mainMenu(view: View?){
        Log.d(TAG, "mainMenu pressed..")
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
    }

    fun showProfile(view: View?){
        Log.d(TAG, "Profile pressed..")
        val intent = Intent(this, ProfileActivity::class.java)
        startActivity(intent)
    }

    private fun termsConditions(){
        Log.d(TAG, "privacyPolicy pressed..")
        val intent = Intent(this, TermsActivity::class.java)
        startActivity(intent)
    }

    fun signOut(view: View?){
        Log.d(TAG, "signOut pressed..")
        auth.signOut()
        startActivity(Intent(this, LoginActivity::class.java))
        this.finish()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_privacy, menu)
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
