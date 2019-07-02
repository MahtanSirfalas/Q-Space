package com.ust.spaceq

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity : AppCompatActivity() {


    private lateinit var auth:FirebaseAuth
    private lateinit var databaseReference: DatabaseReference
    private lateinit var database:FirebaseDatabase

    private lateinit var nickName:String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        database = FirebaseDatabase.getInstance()
        databaseReference = database.reference.child("Users")
        auth = FirebaseAuth.getInstance()

        val yes = findViewById<Button>(R.id.buttVar)
        val yok = findViewById<Button>(R.id.buttYok)
        val nick = findViewById<EditText>(R.id.etNick)
        val gir = findViewById<Button>(R.id.buttSign)
        val yarat = findViewById<Button>(R.id.buttLogin)
        val posta = findViewById<EditText>(R.id.etEmail)
        val sifre = findViewById<EditText>(R.id.etPassword)

        yok.isClickable = false
        yok.visibility = View.INVISIBLE
        gir.isClickable = false
        gir.visibility = View.INVISIBLE

        yes.setOnClickListener {
            yok.isClickable = true
            yok.visibility = View.VISIBLE
            gir.isClickable = true
            gir.visibility = View.VISIBLE
            yes.isClickable = false
            yes.visibility = View.INVISIBLE
            yarat.isClickable = false
            yarat.visibility = View.INVISIBLE
            nick.isClickable = false
            nick.visibility = View.INVISIBLE
        }

        yok.setOnClickListener {
            yok.isClickable = false
            yok.visibility = View.INVISIBLE
            gir.isClickable = false
            gir.visibility = View.INVISIBLE
            yes.isClickable = true
            yes.visibility = View.VISIBLE
            yarat.isClickable = true
            yarat.visibility = View.VISIBLE
            nick.isClickable = true
            nick.visibility = View.VISIBLE
        }

        val loginTextWatcher = object : TextWatcher {

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                val usernameInput = nick.text.toString().trim()
                val mailInput = posta.text.toString().trim()
                val passwordInput = sifre.text.toString().trim()

                if (yok.visibility == View.INVISIBLE){
                    buttLogin.isEnabled = !usernameInput.isEmpty() && !passwordInput.isEmpty() && !mailInput.isEmpty()
                }else{
                    buttSign.isEnabled = !mailInput.isEmpty() && passwordInput.isNotEmpty()
                }

            }

            override fun afterTextChanged(s: Editable) {

            }
        }
        posta.addTextChangedListener(loginTextWatcher)
        sifre.addTextChangedListener(loginTextWatcher)
        nick.addTextChangedListener(loginTextWatcher)
    }
    public override fun onStart() {
        super.onStart()
        //Check user sign(non null); update UI accordingly
        val currentUser = auth.currentUser
        updateUI(currentUser)
    }


    fun buttLoginEvent(view:View){
        LoginToSystem(etEmail.text.toString(), etPassword.text.toString())
    }

    fun LoginToSystem(email:String, password:String){
        nickName = etNick.text.toString()
        Log.d(TAG, "createAccount:$email")
        if(!validateForm()){
            return
        }

        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(this){task ->
            if (task.isSuccessful){
                //Success sign in -> update UI
                Log.d(TAG, "createUserWithEmail:Success")
                val user = auth.currentUser

                val userId = user!!.uid
                val userDb = databaseReference.child(userId)
                userDb.child("nickName").setValue(nickName)
                userDb.child("eMail").setValue(user.email)
                userDb.child("points").setValue("0")
                userDb.child("level").setValue("0")
                updateUI(user)
            }else{
                //Fail -> display message below
                Log.w(TAG, "createUserWithEmail:failure", task.exception)
                Toast.makeText(baseContext, "Authentication failed.", Toast.LENGTH_SHORT).show()
                updateUI(null)
            }
        }
    }

    fun buttSigninEvent(view: View){
        SignInToSystem(etEmail.text.toString(), etPassword.text.toString())
    }

    fun SignInToSystem(email:String, password: String){
        Log.d(TAG, "signIn:$email")

        auth.signInWithEmailAndPassword(email, password).addOnCompleteListener(this){task->
            if (task.isSuccessful){
                Log.d(TAG, "signInWithEmail:success")
                val user = auth.currentUser
                updateUI(user)
            }else{
                Log.w(TAG, "signInWithEmail:failure!",task.exception)
                Toast.makeText(baseContext,"Authentication failed.", Toast.LENGTH_SHORT).show()
                updateUI(null)
            }
        }
    }

    private fun validateForm(): Boolean {
        var valid = true

        val email = etEmail.text.toString()
        if(TextUtils.isEmpty(email)) {
            etEmail.error="Required."
            valid = false
        }else{
            etEmail.error = null
        }

        val password = etPassword.text.toString()
        if (TextUtils.isEmpty(password)){
            etPassword.error = "Required."
            valid = false
        }else {
            etPassword.error = null
        }

        return valid
    }

    private fun updateUI(user: FirebaseUser?){
//        hideProgressDialog()  i don't know wth is that?
        if (user != null) {
            val intent = Intent(this, MainActivity::class.java)
            intent.putExtra("email", user.email)
            intent.putExtra("uid", user.uid)

            startActivity(intent)
        }else{
            Toast.makeText(baseContext, "Log in or Create an Account.", Toast.LENGTH_SHORT).show()
        }
    }

    companion object{
        private const val TAG = "EmailPassword"
    }
}
