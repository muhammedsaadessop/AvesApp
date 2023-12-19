package com.VarsityCollege.Aves

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class SplashScreen : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.splash_screen)
        supportActionBar?.hide()
        // checks if the users logged in and routes accoridngly
        val checkvaliduser = FirebaseAuth.getInstance()
        val loggedin = checkvaliduser.currentUser
        if (loggedin != null) {
            val loadseconds = Handler(Looper.getMainLooper())

            loadseconds.postDelayed({
                val gohome = Intent(this, Home::class.java)
                startActivity(gohome)
                finish()
            }, 10000)
        } else {
            val loadseconds = Handler(Looper.getMainLooper())

            loadseconds.postDelayed({
                val gologin = Intent(this, Login::class.java)
                startActivity(gologin)
                finish()
            }, 2000)
        }

    }
}
