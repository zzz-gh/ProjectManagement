package com.zpo.projectmanagement.activities

import android.os.Bundle
import android.text.TextUtils
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.zpo.projectmanagement.Firebase.FireStoreClass
import com.zpo.projectmanagement.R
import com.zpo.projectmanagement.models.User
import kotlinx.android.synthetic.main.activity_sign_up.*

class SignUpActivity  : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        setupActionBar()

        btn_sign_up.setOnClickListener {
            registerUser()
        }

    }

    private fun setupActionBar() {

        setSupportActionBar(toolbar_sign_up_activity)

        val actionBar = supportActionBar
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_baseline_arrow_back_24)
        }

        toolbar_sign_up_activity.setNavigationOnClickListener { onBackPressed() }
    }

    /////validate
    private fun validateForm(name:String,email:String,password:String) : Boolean{
        return when{
            TextUtils.isEmpty(name) ->{
                showErrorSnackBar("Please Enter your name")
                return false
            }

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


    ////register
    private fun registerUser(){
        val name = et_name.text.toString().trim{ it <= ' '}
        val email = et_email.text.toString().trim { it <= ' ' }
        val password = et_password.text.toString().trim { it <= ' ' }

        if(validateForm(name,email,password)){

            showProgressDialog(resources.getString(R.string.please_wait))

            FirebaseAuth.getInstance().createUserWithEmailAndPassword(email,password).addOnCompleteListener {
                    task ->
                if (task.isSuccessful){
                    hideProgressDialog()
                    val firebaseUser = task.result!!.user
                    val registeredEmail = firebaseUser!!.email

                    val user  = User(
                        firebaseUser.uid,
                        name,
                        registeredEmail!!
                    )
                    FireStoreClass()
                        .registerUser(this,user)
                }else{
                    Toast.makeText(this,"Registration Failed",
                        Toast.LENGTH_SHORT).show()
                    hideProgressDialog()
                }

            }


        }
    }

    fun userRegisterSuccess(){
        hideProgressDialog()
        Toast.makeText(this," You have successfully registered with email ",
            Toast.LENGTH_SHORT).show()
        FirebaseAuth.getInstance().signOut()
        finish()
    }
}