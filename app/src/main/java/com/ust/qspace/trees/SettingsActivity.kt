package com.ust.qspace.trees

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.CompoundButton
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth
import com.ust.qspace.LoginActivity
import com.ust.qspace.MainActivity
import com.ust.qspace.ProfileActivity
import com.ust.qspace.R
import com.ust.qspace.models.SettingsPrefs
import com.ust.qspace.models.simpleUi
import com.ust.qspace.models.whiteFont

import kotlinx.android.synthetic.main.activity_settings.*

private lateinit var auth: FirebaseAuth

class SettingsActivity : AppCompatActivity() {
    val TAG = "SettingsActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        setSupportActionBar(toolbar)

        auth = FirebaseAuth.getInstance()

        val settings = SettingsPrefs(this)
        val whiteFonts = settings.getSetting(whiteFont)
        val simpleStageUi = settings.getSetting(simpleUi)

        sw_white_font.isChecked = whiteFonts
        sw_simple_stage.isChecked = simpleStageUi


        sw_white_font.setOnCheckedChangeListener(object:CompoundButton.OnCheckedChangeListener{
            override fun onCheckedChanged(p0: CompoundButton?, p1: Boolean) {
                settings.setSetting(whiteFont, p1)
            }
        })
        sw_simple_stage.setOnCheckedChangeListener(object : CompoundButton.OnCheckedChangeListener{
            override fun onCheckedChanged(p0: CompoundButton?, p1: Boolean) {
                settings.setSetting(simpleUi, p1)
            }
        })
    }

    private fun mainMenu(view: View?){
        Log.d(TAG, "mainMenu pressed..")
        val intent = Intent(this@SettingsActivity, MainActivity::class.java)
        startActivity(intent)
    }

    fun showProfile(view: View?){
        Log.d(TAG, "Profile pressed..")
        val intent = Intent(this@SettingsActivity, ProfileActivity::class.java)
        startActivity(intent)
    }

    fun signOut(view: View?){
        Log.d(TAG, "signOut pressed..")
        auth.signOut()
        startActivity(Intent(this@SettingsActivity, LoginActivity::class.java))
        this@SettingsActivity.finish()
    }

    override fun onBackPressed() {
        mainMenu(null)
        this.finish()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_settings, menu)
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
