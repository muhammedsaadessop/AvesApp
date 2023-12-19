package com.VarsityCollege.Aves

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.PopupWindow
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import de.keyboardsurfer.android.widget.crouton.Crouton
import de.keyboardsurfer.android.widget.crouton.Style

class Login : AppCompatActivity() {
    private val e = Errors()
    private val emptyPass = Crouton.makeText(this, e.noNullsPassWord, Style.ALERT)
    private val emptyEmail = Crouton.makeText(this, e.emailValidationEmptyError, Style.ALERT)
    private val noFields = Crouton.makeText(this, e.noDetailsEntered, Style.ALERT)

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.login)
        supportActionBar?.hide()
        permissions()
        autologin()
        signup()
        login()
        notifications()
        FirebaseApp.initializeApp(this)
        val forgotPassword = findViewById<TextView>(R.id.forgotpassword)
        forgotPassword.setOnClickListener()
        {

            forgotpassword()
        }

    }
// this sends an email to your email wiht a forgot password link
    fun forgotpassword() {
        var email: EditText = findViewById(R.id.usernametxt)
        if (email.text.isNullOrEmpty()) {
            emptyEmail.show()
        } else {
            val resetpassword = FirebaseAuth.getInstance()
            resetpassword.sendPasswordResetEmail(email.text.toString())
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val displaymessage = Crouton.makeText(this, "forgot email sent", Style.ALERT)
                        displaymessage.show()
                    } else {
                        // There was an error. Handle it here.
                    }
                }
        }

    }
// this checks if you already have an active token , if not goes back to the login screen
    private fun autologin() {

        val usercheck = FirebaseAuth.getInstance()
        val loggediuser = usercheck.currentUser
        if (loggediuser != null) {
            val intent = Intent(this, Home::class.java)

            startActivity(intent)
            overridePendingTransition(0, 0)
            finish()
        }

    }
// this checks if the email is an email
    private fun notifications() {

        val usernames1: TextView = findViewById(R.id.usernametxt)
        val layoutInflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val emailPopup = layoutInflater.inflate(R.layout.popup_window, null)
        val emailWindow = PopupWindow(
            emailPopup,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT

        )

        val crouton = Crouton.makeText(this, e.illegalCharacterHash, Style.ALERT)
        usernames1?.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val email = s.toString()
                if (isValidString(email)) {


                    login()
                    emailWindow.dismiss()


                } else {

                    if (!emailWindow.isShowing) {
                        emailWindow.showAsDropDown(usernames1, 0, 0)
                    }
                    if (email.contains("#")) {

                        crouton.show()


                    }


                }
            }

            override fun beforeTextChanged(
                s: CharSequence?,
                start: Int,
                count: Int,
                after: Int
            ) {// YET TO REUSED
            }

            override fun onTextChanged(
                s: CharSequence?,
                start: Int,
                before: Int,
                count: Int
            ) {//NO IDEA
            }

        })

        usernames1?.onFocusChangeListener = View.OnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {

                emailWindow.dismiss()
            }
        }


    }
// checks if the email is valid
    fun isValidString(str: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(str).matches()
    }

// takes you to the registration screen
    private fun signup() {
        val signup: TextView = findViewById(R.id.signUp)
        signup.setOnClickListener()
        {
            val registration = Intent(this, Registration::class.java)

            startActivity(registration)
            overridePendingTransition(0, 0)
            finish()

        }

    }
// this checks the details and makes sure nothing is empty
    // then signs you in
    private fun login() {
        val usernames1: EditText =
            findViewById(R.id.usernametxt)

        val pass: EditText = findViewById(R.id.password)


        val signButton: Button = findViewById(R.id.loginbtn)
        signButton.setOnClickListener()
        {
            when {
                pass?.text.isNullOrEmpty() -> {
                    emptyPass.show()

                }

                usernames1?.text.isNullOrEmpty() -> {

                    emptyEmail.show()

                }

                pass?.text.isNullOrEmpty() && usernames1?.text.isNullOrEmpty() -> {

                    noFields.show()

                }

                else -> {

                    val security = Firebase.auth


                    security.signInWithEmailAndPassword(
                        usernames1?.text.toString().trim(),
                        pass?.text.toString().trim()
                    )
                        .addOnCompleteListener(this) { task ->
                            when {
                                task.isSuccessful -> {

                                    val crouton =
                                        Crouton.makeText(this, "Login success", Style.ALERT)
                                    crouton.show()
                                    val intent = Intent(this, Home::class.java)

                                    startActivity(intent)
                                    overridePendingTransition(0, 0)
                                    finish()

                                }

                                else -> {
                                    val crouton = Crouton.makeText(this, e.loginError, Style.ALERT)
                                    crouton.show()
                                }
                            }
                        }


                }
            }
        }

    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun permissions() {
        val code = 0

        when {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_MEDIA_LOCATION
            ) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_MEDIA_IMAGES
            ) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_MEDIA_AUDIO
            ) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_MEDIA_VIDEO
            ) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.INTERNET
            ) != PackageManager.PERMISSION_GRANTED -> {
                ActivityCompat.requestPermissions(
                    this, arrayOf(
                        Manifest.permission.CAMERA,
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.ACCESS_MEDIA_LOCATION,
                        Manifest.permission.READ_MEDIA_IMAGES,
                        Manifest.permission.READ_MEDIA_AUDIO,
                        Manifest.permission.READ_MEDIA_VIDEO,
                        Manifest.permission.POST_NOTIFICATIONS,
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.INTERNET
                    ), code
                )
            }
        }
    }


}