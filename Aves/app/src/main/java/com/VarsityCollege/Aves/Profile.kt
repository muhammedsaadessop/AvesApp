package com.VarsityCollege.Aves

import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Spinner
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isEmpty
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import de.keyboardsurfer.android.widget.crouton.Crouton
import de.keyboardsurfer.android.widget.crouton.Style


class Profile : AppCompatActivity() {
    private val e = Errors()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.profile)
        supportActionBar?.hide()
        val loginBack = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                val intent = Intent(this@Profile, Home::class.java)

                startActivity(intent)
                overridePendingTransition(0, 0)
                finish()


            }
        }
        onBackPressedDispatcher.addCallback(this, loginBack)
        securGuard()
        dataload()
        val returnbutton = findViewById<ImageButton>(R.id.back_btn)
        returnbutton.setOnClickListener()
        {
            val intent = Intent(this , Home::class.java)

            startActivity(intent)
            overridePendingTransition(0, 0)
            finish()

        }
        val resetpassword:Button = findViewById(R.id.ForgotPassword)
        resetpassword.setOnClickListener()
        {forgotpassword()

        }
        val save: Button = findViewById(R.id.saveButton)
        save.setOnClickListener()
        {

            val namefield: EditText = findViewById(R.id.nameEditText)
            val surname: EditText = findViewById(R.id.surnameEditText)
            val email: EditText = findViewById(R.id.emailEditText)
            val phone: EditText = findViewById(R.id.phoneedittext)
            val levls: Spinner = findViewById(R.id.birdLevel)

            //action statements tp check fields if empty
            when {
                namefield.text.toString().isEmpty() -> {
                    Snackbar.make(email, e.noFName, Snackbar.LENGTH_SHORT).show()
                }

                surname.text.toString().isEmpty() -> {
                    Snackbar.make(email, e.noSName, Snackbar.LENGTH_SHORT).show()
                }
                email.text.toString().isEmpty() -> {
                    Snackbar.make(email, e.emailValidationEmptyError, Snackbar.LENGTH_SHORT).show()
                }

                phone.text.isEmpty() -> {

                    Snackbar.make(email, e.nophonenumber, Snackbar.LENGTH_SHORT).show()

                }
                else -> { // move to the next screen if filled


                    editdata()
                }
            }
        }
    }

    fun dataload() {
        val namefield: EditText = findViewById(R.id.nameEditText)
        val surname: EditText = findViewById(R.id.surnameEditText)
        val email: EditText = findViewById(R.id.emailEditText)
        val phone: EditText = findViewById(R.id.phoneedittext)
        val levls: Spinner = findViewById(R.id.birdLevel)
        val user = FirebaseAuth.getInstance().currentUser



        val maxArray = listOf("Hobbyist", "Amateur", "Intermediate", "Advanced", "Professional")
        val maxAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, maxArray)
        maxAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        levls.adapter = maxAdapter
        val userEmail = user?.email


        val parts = userEmail!!.split('@', '.')
        val userID = parts[0] + parts[1]
        Log.d("userid", userID)
        val db = FirebaseFirestore.getInstance()
        val docRef = db.collection("aves").document(userID)
        docRef.get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    Log.d(TAG, "DocumentSnapshot data: ${document.data}")
                    val userDetails = document.get("userDetails") as Map<String, Any>
                    val name = userDetails["firstname"].toString()
                    val surnames = userDetails["surname"].toString()
                    val emails = userDetails["emailaddress"].toString()
                    val phonenumber = userDetails["phone"].toString()
                    val watchinglevel = userDetails["watchinglevel"].toString()
                    phone.setText(phonenumber)
                    namefield.setText(name)
                    surname.setText(surnames)
                    email.setText(emails)

                } else {
                    Log.d(TAG, "No such document")
                }
            }
            .addOnFailureListener { exception ->
                Log.d(TAG, "get failed with ", exception)
            }


    }

    fun editdata() {
        val namefield: EditText = findViewById(R.id.nameEditText)
        val surname: EditText = findViewById(R.id.surnameEditText)
        val email: EditText = findViewById(R.id.emailEditText)
        val phone: EditText = findViewById(R.id.phoneedittext)
        val levls: Spinner = findViewById(R.id.birdLevel)



        val user = FirebaseAuth.getInstance().currentUser

        val userEmail = user?.email


        val parts = userEmail!!.split('@', '.')
        val userID = parts[0] + parts[1]
        Log.d("userid", userID)
        val db = FirebaseFirestore.getInstance()

        val name = namefield.text.toString().replace("\\s".toRegex(), "")
        val surnames = surname.text.toString().replace("\\s".toRegex(), "")
        val level = levls.selectedItem.toString()
        val emails = email.text.toString().replace("\\s".toRegex(), "")


        val phones = phone.text.toString()
        val users = User(
            name, surnames, emails, level, userID, phones
        )
        val docRef = db.collection("aves").document(userID)
        docRef.set(
            mapOf("userDetails" to users), SetOptions.merge()

        )
        val crouton = Crouton.makeText(this, "user details updated", Style.ALERT)
        crouton.show()
    }

    private fun securGuard() {
// this checks user tokens
        // if invalid forces the user to login again
        // if deleted forces the user out of the app
        val tempusSecurity = FirebaseAuth.getInstance()
        tempusSecurity.addAuthStateListener { firebaseAuth ->
            when (firebaseAuth.currentUser) {
                null -> {
                    val intent = Intent(this, Login::class.java)

                    startActivity(intent)
                    overridePendingTransition(0, 0)
                    finish()

                }
            }
        }
    }
    fun forgotpassword() {
        var email: EditText = findViewById(R.id.emailEditText)
        val auth = FirebaseAuth.getInstance()
        auth.sendPasswordResetEmail(email.text.toString())
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val crouton = Crouton.makeText(this, "forgot email sent", Style.ALERT)
                    crouton.show()
                } else {
                    // There was an error. Handle it here.
                }
            }

    }
}