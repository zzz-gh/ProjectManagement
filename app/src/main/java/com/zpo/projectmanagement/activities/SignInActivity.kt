package com.zpo.projectmanagement.activities

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.zpo.projectmanagement.Firebase.FireStoreClass
import com.zpo.projectmanagement.R
import com.zpo.projectmanagement.models.User
import kotlinx.android.synthetic.main.activity_sign_in.*

class SignInActivity  : BaseActivity() {

    private lateinit var auth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_in)

        setupActionBar()

        auth = FirebaseAuth.getInstance()

        btn_sign_in.setOnClickListener {
            signInRegisteredUser()
        }


    }

    private fun setupActionBar(){
        setSupportActionBar(toolbar_sign_in_activity)

        val actionBar = supportActionBar
        if(actionBar!= null){
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_baseline_arrow_back_24)
        }
        toolbar_sign_in_activity.setNavigationOnClickListener {
            onBackPressed()
        }
    }

    ///Sign In register
    private fun validateForm(email:String,password:String) : Boolean{
        return when{

            TextUtils.isEmpty(email) ->{
                showErrorSnackBar("Please Enter your email")
                return false
            }
            TextUtils.isEmpty(password) ->{
                showErrorSnackBar("Please Enter the password")
                return false
            }
            else ->{
                return true
            }

        }

    }
    private fun signInRegisteredUser(){
        val email = et_email_sign_in.text.toString().trim{ it <= ' '}
        val password = et_password_sign_in.text.toString().trim{ it <= ' '}

        if(validateForm(email, password)){
            showProgressDialog(resources.getString(R.string.please_wait))

            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        FireStoreClass().loadUserData(this@SignInActivity)

                    } else {
                        Log.w("TAG", "createUserWithEmail:failure", task.exception)
                        Toast.makeText(baseContext, "Authentication failed.",
                            Toast.LENGTH_SHORT).show()
                        hideProgressDialog()

                    }


                }

        }
    }

    fun signInSuccess(user: User?){
        hideProgressDialog()
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}