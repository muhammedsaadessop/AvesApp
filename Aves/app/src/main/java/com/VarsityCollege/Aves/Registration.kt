package com.VarsityCollege.Aves

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.PopupWindow
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DatabaseException
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.ktx.Firebase
import de.keyboardsurfer.android.widget.crouton.Crouton
import de.keyboardsurfer.android.widget.crouton.Style


class Registration : AppCompatActivity() {
    private val e = Errors()
    private val emailRegAlready = Crouton.makeText(this, e.regEmailError, Style.ALERT)
    private val invalidCharacter = Crouton.makeText(this, e.InvalidCharacter, Style.ALERT)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.registration)
        supportActionBar?.hide()
        val loginBack = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                val intent = Intent(this@Registration, Login::class.java)

                startActivity(intent)
                overridePendingTransition(0, 0)
                finish()


            }
        }

        onBackPressedDispatcher.addCallback(this, loginBack)
        notifications()
        input()
        returntologin()
    }
// this lets the user know their email isnt matching the format requested
    private fun notifications() {

        val layoutInflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val regPopupView = layoutInflater.inflate(R.layout.popup_window, null)
        val newPopupWindow = PopupWindow(
            regPopupView, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT
        )
        val submitting: Button = findViewById(R.id.signupButton)
        var firstname = findViewById<EditText>(R.id.firstname)
        var surname: EditText = findViewById(R.id.surname)


        var password1: EditText = findViewById(R.id.password)


        var email1: EditText = findViewById(R.id.email)
        email1.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val email = s.toString()
                when {
                    check(email) -> {

                        newPopupWindow.dismiss()
                        input()


                    }

                    else -> {

                        when {
                            !newPopupWindow.isShowing -> {
                                newPopupWindow.showAsDropDown(email1, 0, 0)
                            }
                        }
                        when {
                            email.contains("#") -> {
                                Log.d("TAG", "hashCharacter.show() called")

                                Snackbar.make(email1, e.illegalCharacterHash, Snackbar.LENGTH_SHORT)
                                    .show()

                            }
                        }

                    }
                }
            }

            override fun beforeTextChanged(
                s: CharSequence?, start: Int, count: Int, after: Int
            ) {// stuff to do
            }

            override fun onTextChanged(
                s: CharSequence?, start: Int, before: Int, count: Int
            ) { // stuff to do


            }

        })

        email1.onFocusChangeListener = View.OnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {

                newPopupWindow.dismiss()
            }
        }


    }
// this makes sure the email entered is an email
    fun check(str: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(str).matches()
    }

    // if they accidently click sign up they can go back
    fun returntologin() {
        var returns: TextView = findViewById(R.id.returnbtn)
        returns.setOnClickListener() {
            val loginpage = Intent(this, Login::class.java)
            startActivity(loginpage)
            overridePendingTransition(0, 0)
            finish()
        }
    }

    // this checks the password against industry regex
    fun isValidPassword(password: String): Boolean {
        val regex = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[!?*@#$%^&+=])(?=\\S+$).{8,}$".toRegex()
        return regex.matches(password)
    }

    // this makes sure no fields are empty
    // then checks to see if it is a secure password
    // then checks to see if it meets the min lentgh
    // if so it registers the user to auth
    fun input() { //variables


        val submitting: Button = findViewById(R.id.signupButton)

        var firstname = findViewById<EditText>(R.id.firstname)
        var surname: EditText = findViewById(R.id.surname)


        var password1: EditText = findViewById(R.id.password)

        var phones: EditText = findViewById(R.id.PhoneNumTxt)
        var email1: EditText = findViewById(R.id.email)
        val db = FirebaseFirestore.getInstance()

        var birdspinner: Spinner = findViewById(R.id.birdwatchinglevel)
        var confirmpassword: EditText = findViewById(R.id.confirmpassword)


        val maxArray = listOf("Hobbyist", "Amateur", "Intermediate", "Advanced", "Professional")
        val maxAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, maxArray)
        maxAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        birdspinner.adapter = maxAdapter

        submitting.setOnClickListener()

        {

            //action statements tp check fields if empty
            when {
                firstname?.text.toString().isEmpty() -> {
                    Snackbar.make(email1, e.noFName, Snackbar.LENGTH_SHORT).show()
                }

                surname.text.toString().isEmpty() -> {
                    Snackbar.make(email1, e.noSName, Snackbar.LENGTH_SHORT).show()
                }

                !isValidPassword(password1.text.toString()) -> {
                    Snackbar.make(
                        email1,
                        " Password must be at least 8 characters long, contain at least one digit, one lower case letter, one upper case letter, and one special character.",
                        Snackbar.LENGTH_SHORT
                    ).show()
                }

                password1.text.toString().isEmpty() -> {
                    Snackbar.make(email1, e.passwordCantBeEmpty, Snackbar.LENGTH_SHORT).show()
                }

                confirmpassword.text.toString().length < 8 -> {

                    Snackbar.make(email1, e.confirmPasswordTooShort, Snackbar.LENGTH_SHORT).show()
                }

                confirmpassword.text.toString().isEmpty() -> {

                    Snackbar.make(email1, e.confirmPasswordCantBeEmpty, Snackbar.LENGTH_SHORT)
                        .show()
                }

                confirmpassword.text.toString() != password1.text.toString() -> {
                    Snackbar.make(email1, e.passwordNotMatch, Snackbar.LENGTH_SHORT).show()
                }

                email1.text.toString().isEmpty() -> {
                    Snackbar.make(email1, e.emailValidationEmptyError, Snackbar.LENGTH_SHORT).show()
                }

                phones.text.isEmpty() -> {

                    Snackbar.make(email1, e.nophonenumber, Snackbar.LENGTH_SHORT).show()

                }


                else -> { // move to the next screen if filled


                    val auth = Firebase.auth
                    // Capture user details
                    val name = firstname?.text.toString().replace("\\s".toRegex(), "")
                    val surname = surname.text.toString().replace("\\s".toRegex(), "")
                    val level = birdspinner.selectedItem.toString().replace("\\s".toRegex(), "")
                    val email = email1.text.toString().replace("\\s".toRegex(), "")
                    val password = password1.text.toString().replace("\\s".toRegex(), "")
                    val parts = email1.text.split('@', '.')
                    val userID = parts[0] + parts[1]
                    val phone = phones.text.toString()

                    auth.createUserWithEmailAndPassword(email.lowercase(), password)
                        .addOnCompleteListener(this) { task ->
                            val auth = FirebaseAuth.getInstance()
                            val currentUser = auth.currentUser

                            val exception = task.exception
                            when {

                                task.isSuccessful && currentUser != null -> {
                                    try {
                                        auth.currentUser

                                        val users = User(
                                            name,
                                            surname,
                                            email.lowercase(),
                                            level,
                                            userID.lowercase(),
                                            phone
                                        )
                                        val docRef =
                                            db.collection("aves").document(userID.lowercase())
                                        docRef.set(
                                            mapOf("userDetails" to users), SetOptions.merge()
                                        )


                                        val message = "USER ${firstname?.text} HAS REGISTERED "
                                        Toast.makeText(
                                            applicationContext, message, Toast.LENGTH_SHORT
                                        ).show()


                                    } catch (E: DatabaseException) {
                                        Snackbar.make(
                                            email1, e.InvalidCharacter, Snackbar.LENGTH_SHORT
                                        ).show()

                                    } catch (e: Exception) {// stuff to do
                                        Snackbar.make(email1, e.toString(), Snackbar.LENGTH_SHORT)
                                            .show()

                                    }
                                }

                                task.isSuccessful && currentUser == null -> {


                                    val loginpage = Intent(this, Login::class.java)
                                    loginpage.putExtra("login", R.layout.login)

                                    overridePendingTransition(0, 0)
                                    startActivity(loginpage)
                                    finish()

                                }

                                exception is FirebaseAuthException && exception.errorCode == "ERROR_EMAIL_ALREADY_IN_USE" -> {
                                    Snackbar.make(email1, e.regEmailError, Snackbar.LENGTH_SHORT)
                                        .show()


                                }

                                else -> {
                                    // STUFF TO DO

                                }
                            }
                        }


                }
            }


        }
    }

}