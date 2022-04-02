package com.example.kotlinmessenger

import android.app.Activity
import android.content.Intent
import android.graphics.ImageDecoder
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.parcel.Parcelize
import kotlinx.android.synthetic.main.activity_main.*
import java.io.IOException
import java.util.*


class RegisterActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        button_photo.setOnClickListener {
            Log.d("RegisterActivity", "Try to show photo")

            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, 0)
        }
        register_button.setOnClickListener{

            performRegister()
        }

        already_acc.setOnClickListener {
            Log.d("RegisterActivity", "Try To Show Login Message")

            //Launch the login activity
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }

    }
    var selectedPhotoUri: Uri? = null
    @RequiresApi(Build.VERSION_CODES.P)
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(requestCode == 0 && resultCode == Activity.RESULT_OK && data != null)
        {
            //Proceed and check what the selected image was
            Log.d("RegisterActivity", "Photo Was selected ")

            selectedPhotoUri= data!!.data

            val source = android.graphics.ImageDecoder.createSource(this.contentResolver, selectedPhotoUri!!)

            val bitmap = android.graphics.ImageDecoder.decodeBitmap(source)

            selectPhotoImageView_reg.setImageBitmap(bitmap)

            button_photo.alpha = 0f
            //val bitmapdrawable = BitmapDrawable(this.resources, bitmap)
            //button_photo.setText("")
            //button_photo.setBackground(bitmapdrawable)



        }
    }
    private fun performRegister(){
        val email = email_editText_reg.text.toString()
        val password = password_editText_reg.text.toString()

        if(email.isEmpty() || password.isEmpty()){
            Toast.makeText(this, "Please enter email/pw", Toast.LENGTH_SHORT).show()
            return
        }
        //Firebase Authentication with email and password
        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener {
                if (!it.isSuccessful) return@addOnCompleteListener

                Toast.makeText(this, "Please wait until we complete the registration", Toast.LENGTH_LONG).show()
                Log.d("Main", "Successfully created user with uid: ${it?.result?.user?.uid}")

                uploadPhotoFirebaseStorage()

            }
            .addOnFailureListener{
                Log.d("Main", "Failed to create user: ${it.message}")
                Toast.makeText(this, "Failed to create user: ${it.message}",Toast.LENGTH_SHORT).show()
            }

    }
    private fun uploadPhotoFirebaseStorage(){
        if(selectedPhotoUri==null) {
            Log.d("Register", "UnSuccessfully uploaded message:")
            return
        }
        val filename = UUID.randomUUID().toString()
        val ref = FirebaseStorage.getInstance().getReference("/images/$filename")

        ref.putFile(selectedPhotoUri!!)
            .addOnSuccessListener {
                Log.d("Register", "Successfully uploaded photo: ${it.metadata?.path}")

                ref.downloadUrl.addOnSuccessListener {
                    Log.d("Register", "File Location: $it")

                    saveUserToFirebaseDatabase(it.toString())
                }
            }
    }
    private fun saveUserToFirebaseDatabase(profImg: String) {
        val uid = FirebaseAuth.getInstance().uid ?: ""
        val ref = FirebaseDatabase.getInstance().getReference("users/$uid")
        val usernametext = username_editText_reg.getText().toString()
        Log.d("Username", "$usernametext")
        val user = User(uid, usernametext, profImg)
        ref.setValue(user)
            .addOnSuccessListener {
                Log.d("RegisterActivity", "Finally we saved the user to firebase database")

                val intent = Intent(this, LatestMessagesActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
            }
    }
}
@Parcelize
class User(val uid: String, val username: String, val profImg: String): Parcelable{
    constructor() : this("", "", "")
}