package com.ust.qspace

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.google.firebase.auth.FirebaseAuth
import com.ust.qspace.trees.SettingsActivity
import kotlinx.android.synthetic.main.activity_info.*

class InfoActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    val TAG = "InfoActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_info)
        setSupportActionBar(toolbar)

        auth = FirebaseAuth.getInstance()

        tv_info_intro.text = getString(R.string.info_intro, uName)
        tv_info_misvis.text = getString(R.string.info_mission) + "\n" + getString(R.string.info_vision)+
                "\n\n" + getString(R.string.info_how)

    }

    fun faqQuestionClick(view:View){
        when(view.id){
            R.id.butt_info_faq1_q->{
                if (!tv_info_faq1_a.isVisible){
                    tv_info_faq1_a.visibility = View.VISIBLE
                }else{
                    tv_info_faq1_a.visibility = View.GONE
                }
            }
            R.id.butt_info_faq2_q->{
                if (!tv_info_faq2_a.isVisible){
                    tv_info_faq2_a.visibility = View.VISIBLE
                }else{
                    tv_info_faq2_a.visibility = View.GONE
                }
            }
            R.id.butt_info_faq3_q->{
                if (!tv_info_faq3_a.isVisible){
                    tv_info_faq3_a.visibility = View.VISIBLE
                }else{
                    tv_info_faq3_a.visibility = View.GONE
                }
            }
            R.id.butt_info_faq4_q->{
                if (!tv_info_faq4_a.isVisible){
                    tv_info_faq4_a.visibility = View.VISIBLE
                }else{
                    tv_info_faq4_a.visibility = View.GONE
                }
            }
            R.id.butt_info_faq5_q->{
                if (!tv_info_faq5_a.isVisible){
                    tv_info_faq5_a.visibility = View.VISIBLE
                }else{
                    tv_info_faq5_a.visibility = View.GONE
                }
            }
            R.id.butt_info_faq6_q->{
                if (!tv_info_faq6_a.isVisible){
                    tv_info_faq6_a.visibility = View.VISIBLE
                }else{
                    tv_info_faq6_a.visibility = View.GONE
                }
            }
            R.id.butt_info_faq7_q->{
                if (!tv_info_faq7_a.isVisible){
                    tv_info_faq7_a.visibility = View.VISIBLE
                }else{
                    tv_info_faq7_a.visibility = View.GONE
                }
            }
            R.id.butt_info_faq8_q->{
                if (!tv_info_faq8_a.isVisible){
                    tv_info_faq8_a.visibility = View.VISIBLE
                }else{
                    tv_info_faq8_a.visibility = View.GONE
                }
            }
            R.id.butt_info_faq9_q->{
                if (!tv_info_faq9_a.isVisible){
                    tv_info_faq9_a.visibility = View.VISIBLE
                }else{
                    tv_info_faq9_a.visibility = View.GONE
                }
            }
            R.id.butt_info_faq10_q->{
                if (!tv_info_faq10_a.isVisible){
                    tv_info_faq10_a.visibility = View.VISIBLE
                }else{
                    tv_info_faq10_a.visibility = View.GONE
                }
            }
            R.id.butt_info_faq11_q->{
                if (!tv_info_faq11_a.isVisible){
                    tv_info_faq11_a.visibility = View.VISIBLE
                }else{
                    tv_info_faq11_a.visibility = View.GONE
                }
            }
            R.id.butt_info_faq12_q->{
                if (!tv_info_faq12_a.isVisible){
                    tv_info_faq12_a.visibility = View.VISIBLE
                }else{
                    tv_info_faq12_a.visibility = View.GONE
                }
            }
            R.id.butt_info_faq13_q->{
                if (!tv_info_faq13_a.isVisible){
                    tv_info_faq13_a.visibility = View.VISIBLE
                }else{
                    tv_info_faq13_a.visibility = View.GONE
                }
            }
        }
    }

    private fun mainMenu(view: View?){
        Log.d(TAG, "mainMenu pressed..")
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
    }

    fun showProfile(view: View?){
        Log.d(TAG, "Profile pressed..")
        val intent = Intent(this@InfoActivity, ProfileActivity::class.java)
        startActivity(intent)
    }

    private fun showSettings(view:View?){
        Log.d(TAG, "action_settings pressed!")
        val intent = Intent(this@InfoActivity, SettingsActivity::class.java)
        startActivity(intent)
    }

    fun signOut(view: View?){
        Log.d(TAG, "signOut pressed..")
        auth.signOut()
        startActivity(Intent(this@InfoActivity, LoginActivity::class.java))
        this@InfoActivity.finish()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_suggest, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId){
            R.id.action_out -> signOut(null)
            R.id.action_profile -> showProfile(null)
            R.id.action_home -> mainMenu(null)
            R.id.action_settings -> showSettings(null)
            else -> {

            }
        }

        return when (item.itemId) {
            R.id.action_out -> true
            else -> super.onOptionsItemSelected(item)
        }
    }
}
