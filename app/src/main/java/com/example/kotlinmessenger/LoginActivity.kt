package com.example.kotlinmessenger

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_login)

        login_button.setOnClickListener{
            val email = email_text_login.text.toString()
            val password = password_text_login.text.toString()

            Log.d("Login", "Email is: "+ email)
            Log.d("Login", "Password is: $password")

            FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
                .addOnCompleteListener {
                    if(!it.isSuccessful) return@addOnCompleteListener
                    Log.d("Main", "Successfully Logged in")

                    val intent = Intent(this, LatestMessagesActivity::class.java)
                    startActivity(intent)
                }
                .addOnFailureListener {
                    Log.d("Main", "Failed to login: ${it.message}")
                    Toast.makeText(this, "${it.message}", Toast.LENGTH_LONG).show()

                }
        }
        backtoreg_login.setOnClickListener {
            finish()
        }
    }
}