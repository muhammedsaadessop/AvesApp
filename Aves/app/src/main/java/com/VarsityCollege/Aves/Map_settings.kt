package com.VarsityCollege.Aves

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

class Map_settings : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.map_settings)
        supportActionBar?.hide()
        val returnbutton = findViewById<Button>(R.id.cancel)
        returnbutton.setOnClickListener()
        {
            val intent = Intent(this, Home::class.java)

            startActivity(intent)
            overridePendingTransition(0, 0)
            finish()

        }
        val settings = FirebaseFirestore.getInstance()
        val user = FirebaseAuth.getInstance().currentUser
        val userEmail = user?.email
        val parts = userEmail!!.split('@', '.')
        val userID = parts[0] + parts[1]
// this sets the settings for and the range of the poplation which is stored in firebase
        val mapsettings = settings.collection("aves").document(userID.lowercase())
        mapsettings.get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    val userSettings = document.get("userSettings") as? Map<String, Any>
                    if (userSettings != null) {
                        val km = userSettings["km"] as? Boolean ?: false
                        val miles = userSettings["miles"] as? Boolean ?: false
                        val range = userSettings["maxDistance"] as? String ?: ""

                        findViewById<RadioButton>(R.id.radio_km).isChecked = km
                        findViewById<RadioButton>(R.id.radio_miles).isChecked = miles
                        findViewById<EditText>(R.id.etDistance).setText(range)
                    }
                }
            }
            .addOnFailureListener { exception ->
                Log.e("TAG", "Error fetching settings from Firestore", exception)
            }

        val rbutton = findViewById<RadioGroup>(R.id.radioGroup)
        val Save = findViewById<Button>(R.id.btnSave)
        Save.setOnClickListener {
//here we set the data and ensure all fields are filled
            val selectedRadioButtonId = rbutton.checkedRadioButtonId
            val km = selectedRadioButtonId == R.id.radio_km
            val mile = selectedRadioButtonId == R.id.radio_miles

            if (!km && !mile) {
                Toast.makeText(this, "Please select a unit of measurement", Toast.LENGTH_SHORT)
                    .show()
                return@setOnClickListener
            }

            val range = findViewById<EditText>(R.id.etDistance).text.toString()
            if (range.toInt() > 65) {
                Toast.makeText(this, "The value cannot be greater than 65", Toast.LENGTH_SHORT)
                    .show()
                return@setOnClickListener
            }


            val mapSettings = Settings(km, mile, range)
// here we save to firebase and ensure its all fine then return to map
            val user = FirebaseAuth.getInstance().currentUser
            val userEmail = user?.email
            val parts = userEmail!!.split('@', '.')
            val userID = parts[0] + parts[1]
            Log.d("userid", userID)
            val db = FirebaseFirestore.getInstance()
            Log.d("TAG", "User ID: $userID")
            if (userID != null) {

                val docRef = db.collection("aves").document(userID)
                docRef.set(
                    mapOf("userSettings" to mapSettings), SetOptions.merge()
                )
            } else {
                //errors
            }
            val intent = Intent(this@Map_settings, Home::class.java)

            startActivity(intent)
            overridePendingTransition(0, 0)
            finish()
        }
        val loginBack = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                val intent = Intent(this@Map_settings, Home::class.java)

                startActivity(intent)
                overridePendingTransition(0, 0)
                finish()
            }
        }
        onBackPressedDispatcher.addCallback(this, loginBack)
        securGuard()
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
}