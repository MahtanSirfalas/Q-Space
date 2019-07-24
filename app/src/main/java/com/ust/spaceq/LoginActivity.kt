package com.ust.spaceq

import android.content.Intent
import android.graphics.drawable.AnimationDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.ScrollView
import android.widget.Toast
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_login.*
import android.widget.Button as Button

class LoginActivity : AppCompatActivity() {
    val TAG = "LoginActivity"

    private lateinit var auth:FirebaseAuth
    private lateinit var databaseReference: DatabaseReference
    private lateinit var database:FirebaseDatabase

    private lateinit var nickName:String
    private lateinit var defaultAvatar: String

    private lateinit var googleSignInClient: GoogleSignInClient

//    private lateinit var context:Context
//    private lateinit var file: File
//    private lateinit var path: File
//    private lateinit var letDirectory: File

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val scrollView = findViewById<ScrollView>(R.id.layoutbg)
        val animationDrawable = scrollView.background as AnimationDrawable
        animationDrawable.setEnterFadeDuration(4000)
        animationDrawable.setExitFadeDuration(2000)
        animationDrawable.start()

        defaultAvatar = "https://firebasestorage.googleapis.com/v0/b/mathslayer-2771e.appspot.com/o/Images%2FLumiAvatarStorageVersionForUsers.jpg?alt=media&token=6ff8fd0a-f948-4f7f-b4e4-a1285c273e18"

//        context = this.applicationContext!!
//
//        path = context.filesDir
//        letDirectory = File(path, "LET")

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
        //Start of config_signin
        //Configure Google Sign-In
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        //End of config_signin
        googleSignInClient = GoogleSignIn.getClient(this, gso)
        buttGoogle.setOnClickListener {
            signIn()
        }

    }
    public override fun onStart() {
        super.onStart()
        //Check user sign(non null); update UI accordingly
        var currentUser = auth.currentUser
        updateUI(currentUser)
    }

    fun buttLoginEvent(view:View){
        Log.d(TAG, "Login button pressed!")
        val username = etNick.text.toString()
        var checkNick:Boolean = true

        databaseReference.addListenerForSingleValueEvent(object : ValueEventListener{
            override fun onCancelled(p0: DatabaseError) {
                Log.d(TAG, "dblistenervalueevent cancelled")
            }

            override fun onDataChange(p0: DataSnapshot) {

                p0.children.forEach {
                    Log.d(TAG, it.toString())
                    var nickName = it.child("nickName").value as String
                    if (nickName == username){
                        checkNick = false
                        Log.d(TAG, "checkNick = $checkNick")
                    }else{
                        Log.d(TAG,"checkNick = $checkNick")
                    }
                }
                if (checkNick){
                    Log.d(TAG, "username is available")
                    tvNickError.visibility = View.INVISIBLE
                    LoginToSystem(etEmail.text.toString(), etPassword.text.toString())
                }else{
                    Log.d(TAG, "usernameError; username is already taken $checkNick")
                    tvNickError.text = R.string.nick_error.toString()
                    tvNickError.visibility = View.VISIBLE
                    Toast.makeText(baseContext, R.string.nick_error_toast, Toast.LENGTH_LONG).show()
                }
            }
        })
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
                userDb.child("points").setValue(0)
                userDb.child("level").setValue("Dactyl")
                userDb.child("avatar").setValue(defaultAvatar)

//                letDirectory.mkdirs()
//                file = File(letDirectory, "$nickName.txt")
//                file.writeText("100\n" + "100\n" + "100\n" + "100\n" + "100\n" + "100\n" + "100\n" + "100\n" + "100\n" +
//                        "100\n" + "100\n" + "100\n" + "100\n" + "100\n" + "100\n" + "100\n" + "100\n" + "100\n" + "100\n" + "100\n")

                updateUI(user)
                sendEmailVerification()
            }else{
                //Fail -> display message below
                Log.w(TAG, "createUserWithEmail:failure", task.exception)
                Toast.makeText(baseContext, R.string.auth_fail, Toast.LENGTH_SHORT).show()
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
                Toast.makeText(baseContext,R.string.auth_fail, Toast.LENGTH_SHORT).show()
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

    fun updateUI(user: FirebaseUser?){
//        hideProgressDialog()  i don't know wth is that?
        if (user != null) {

            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        }else{
            Toast.makeText(this, "Log in or Create an Account.", Toast.LENGTH_SHORT).show()
        }
    }

    companion object{
        private const val TAG = "EmailPassword"

        private const val RC_SIGN_IN = 9001
    }

    //GOOGLE
    private fun signIn() {
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                // Google Sign In was successful, authenticate with Firebase
                val account = task.getResult(ApiException::class.java)
                firebaseAuthWithGoogle(account!!)
            } catch (e: ApiException) {
                // Google Sign In failed, update UI appropriately
                Log.w(TAG, "Google sign in failed", e)
                // ...
            }
        }
    }
    private fun firebaseAuthWithGoogle(acct: GoogleSignInAccount) {
        Log.d(TAG, "firebaseAuthWithGoogle:" + acct.id!!)

        val credential = GoogleAuthProvider.getCredential(acct.idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG, "signInWithCredential:success")
                    val user = auth.currentUser

                    val userId = user!!.uid
                    val userDb = databaseReference.child(userId)
                    userDb.child("nickName").setValue(acct.displayName)
                    Log.d(TAG, "nickname = ${acct.displayName}")
                    userDb.child("eMail").setValue(user.email)
                    Log.d(TAG, "email = ${user.email}")
                    userDb.child("points").setValue(0)
                    userDb.child("level").setValue("Dactyl")
                    userDb.child("avatar").setValue(acct.photoUrl.toString())
                    Log.d(TAG, "avatar url = ${acct.photoUrl}")
                    updateUI(user)
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w(TAG, "signInWithCredential:failure", task.exception)
                    Snackbar.make(layoutbg, R.string.auth_fail, Snackbar.LENGTH_SHORT).show()
                    updateUI(null)
                }
                // ...
            }
    }
    //Google End
    private fun sendEmailVerification() {
        // Disable button
//        verifyEmailButton.isEnabled = false

        // [START send_email_verification]
        val user = auth.currentUser
        user?.sendEmailVerification()
            ?.addOnCompleteListener(this) { task ->
                // [START_EXCLUDE]
                // Re-enable button
//                verifyEmailButton.isEnabled = true

                if (task.isSuccessful) {
                    Toast.makeText(baseContext,
                        "Verification email sent to ${user.email} ",
                        Toast.LENGTH_SHORT).show()
                } else {
                    Log.e(TAG, "sendEmailVerification", task.exception)
                    Toast.makeText(baseContext,
                        R.string.failed_verify_email,
                        Toast.LENGTH_SHORT).show()
                }
                // [END_EXCLUDE]
            }
        // [END send_email_verification]
    }
}
