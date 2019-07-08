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
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.squareup.picasso.Picasso

import kotlinx.android.synthetic.main.activity_main.*

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

        val user = auth.currentUser
        uid = user!!.uid
        email = user.email.toString()
        val userReference = databaseReference.child(uid)

        /*userName.text  = userReference.orderByChild("nickName").toString()*/

        userReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {
                tvName.text = p0.child("nickName").value as? String
                avatar = p0.child("avatar").value as String
                Picasso.get().load(avatar).into(iv_avatar_circle)

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

    fun showLvl(view: View?) {
        val intent = Intent(this@MainActivity, LvlActivity::class.java)
        intent.putExtra("tvName", tvName.text.toString())
        startActivity(intent)
    }

    fun showProfile(view: View?){
        Log.d(TAG, "Profile pressed")
        val intent = Intent(this@MainActivity, ProfileActivity::class.java)
        intent.putExtra("tvName", tvName.text.toString())
        startActivity(intent)
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
