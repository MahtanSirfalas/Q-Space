package com.ust.qspace

import android.annotation.TargetApi
import android.app.Activity
import android.content.Intent
import android.graphics.drawable.AnimationDrawable
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.animation.AnimationUtils
import android.view.inputmethod.InputMethodManager
import android.widget.Toast.*
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.ust.qspace.models.Suggests
import com.ust.qspace.trees.PrivacyActivity
import com.ust.qspace.trees.SettingsActivity
import com.ust.qspace.trees.TermsActivity
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder

import kotlinx.android.synthetic.main.activity_suggest.*
import kotlinx.android.synthetic.main.layout_list_q_adapt.view.*
import java.text.SimpleDateFormat
//import java.time.LocalDateTime
//import java.time.format.DateTimeFormatter
import java.util.*

private lateinit var auth: FirebaseAuth
private lateinit var database: FirebaseDatabase
private lateinit var suggestRef: DatabaseReference

@TargetApi(Build.VERSION_CODES.O)
class SuggestActivity : AppCompatActivity() {
    val TAG = "SuggestActivity"
    lateinit var now:Date
    lateinit var date:String
    lateinit var time:String
    /*val now = LocalDateTime.now()
    var date = DateTimeFormatter.ofPattern("yyyy:MM:dd")
    var time = DateTimeFormatter.ofPattern("HH:mm:ss")*/
    lateinit var mAdView : AdView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_suggest)
        setSupportActionBar(toolbar)

        val constraintLayout = findViewById<ConstraintLayout>(R.id.layoutbg)
        val animationDrawable = constraintLayout.background as AnimationDrawable
        animationDrawable.setEnterFadeDuration(2000)
        animationDrawable.setExitFadeDuration(4000)
        animationDrawable.start()

        MobileAds.initialize(this) {}
        mAdView = findViewById(R.id.adView)
        val adRequest = AdRequest.Builder().build()
        mAdView.loadAd(adRequest)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
        suggestRef = database.reference.child("Suggests/$uid")

        showSuggest(null)

        animations()

        buttSend.setOnClickListener {
            val inputMethodManager = getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
            if (it != null){
                inputMethodManager.hideSoftInputFromWindow(it.windowToken, 0)
            }
            val keyWord = etSuggest.text.toString()
            if(keyWord.length >= 10){
                if(keyWord==getString(R.string.pw)){
                    val fadein  = AnimationUtils.loadAnimation(this,R.anim.abc_fade_in)
                    Log.d(TAG, "SEND pressed; pw accepted")
                    suggests_tab.visibility = View.VISIBLE
                    suggests_tab.startAnimation(fadein)
                    fetchSuggests()
                    etSuggest.text.clear()

                }else{
                    now = getCurrentDateTime()
                    date = now.turnToString("yyyy/MM/dd")
                    time = now.turnToString("HH:mm:ss")
                    suggestRef.child("question").setValue(etSuggest.text.toString())
                    suggestRef.child("time").setValue("$date | $time")
                    suggestRef.child("nickName").setValue(uName)
                    showSuggest(null)
                    Log.d(TAG, "SEND pressed; question saved")
                }
            }else{
                Log.d(TAG, "SEND pressed; less than 10")
                val toast = makeText(baseContext, "Too Short", LENGTH_SHORT)
                toast.setGravity(Gravity.CENTER, 0, 0)
                toast.show()
            }
        }
        ibRemove.setOnClickListener {
            suggestRef.child("question").removeValue()
            suggestRef.child("time").removeValue()
            suggestRef.child("nickName").removeValue()
            etSuggest.text.clear()
            tvZaman.text = getString(R.string.date_time)
            showSuggest(null)
            suggests_tab.visibility = View.GONE
        }
    }

    fun Date.turnToString(format: String, locale: Locale = Locale.getDefault()): String {
        val formatter = SimpleDateFormat(format, locale)
        return formatter.format(this)
    }

    fun getCurrentDateTime(): Date {
        return Calendar.getInstance().time
    }

    private fun showSuggest(view: View?){
        suggestRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
            }
            override fun onDataChange(p0: DataSnapshot) {
                if (p0.exists() && p0.hasChildren()){
                    val question = p0.child("question").value as String
                    val zaman = p0.child("time").value as String
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

    private fun showSettings(view:View?){
        Log.d(TAG, "action_settings pressed!")
        val intent = Intent(this@SuggestActivity, SettingsActivity::class.java)
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
        menuInflater.inflate(R.menu.menu_suggest, menu)
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

    private fun fetchSuggests(){
        val ref = database.getReference("/Suggests").orderByChild("time")

        ref.addListenerForSingleValueEvent(object :ValueEventListener{
            override fun onCancelled(p0: DatabaseError) {
                Log.d(TAG, "WARNING: Comment Fetching FAILED!")
                val toast = makeText(baseContext, getString(R.string.listener_cancelled), LENGTH_LONG)
                toast.setGravity(Gravity.CENTER, 0, 0)
                toast.show()
            }
            override fun onDataChange(p0: DataSnapshot) {
                val adapter = GroupAdapter<ViewHolder>()

                p0.children.forEach {
                    Log.d(TAG, it.toString())
                    val suggest = it.getValue(Suggests::class.java)
                    if(suggest != null){
                        Log.d(TAG, "suggest: $suggest")
                        adapter.add(SuggestItem(suggest))
                    }
                }
                recycle_suggests.adapter = adapter
            }
        })
    }
}
class SuggestItem(val suggest: Suggests): Item<ViewHolder>(){
    val TAG = "SuggestActivity SugItem"
    override fun getLayout(): Int {
        Log.d(TAG, "getLayout start")
        return R.layout.layout_list_q_adapt
        Log.d(TAG, "getLayout done")
    }

    override fun bind(viewHolder: ViewHolder, position: Int) {
        Log.d(TAG, "bind start")
        viewHolder.itemView.tv_userName.text = suggest.nickName
        Log.d(TAG, "bind name done")
        viewHolder.itemView.tv_suggest_body.text = suggest.question
        Log.d(TAG, "bind question done")
        viewHolder.itemView.tv_sug_date_time.text = suggest.time
        Log.d(TAG, "bind time done")
    }

}
