package com.zpo.projectmanagement.activities

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.bumptech.glide.Glide
import com.zpo.projectmanagement.Firebase.FireStoreClass
import com.zpo.projectmanagement.R
import com.zpo.projectmanagement.models.User
import com.zpo.projectmanagement.utils.Constants
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.android.synthetic.main.activity_my_profile.*
import java.io.IOException

class MyProfileActivity : BaseActivity() {

    private lateinit var mUserDetails: User
    private var mSelectedImageFileUri: Uri? = null
    private var mProfileImageURL:String = ""

    companion object{
        const val READ_STORAGE_PERMISSION_CODE = 1
        const val PICK_IMAGE_REQUEST_CODE = 2
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_profile)

        setupActionBar()

        FireStoreClass().loadUserData(this)

        iv_profile_user_image.setOnClickListener {
            Constants.imageLoad(this)
        }

        btn_update.setOnClickListener {
            if(mSelectedImageFileUri != null){
                uploadUserImage()
            }else{
                showProgressDialog(resources.getString(R.string.please_wait))
                updateUserProfileData()

            }
        }
    }



    private fun setupActionBar(){
        setSupportActionBar(toolbar_my_profile_activity)
        val actionBar = supportActionBar
        if(actionBar != null){
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_baseline_arrow_back_24)
            actionBar.title = resources.getString(R.string.my_profile_title)
        }
        toolbar_my_profile_activity.setNavigationOnClickListener {
            onBackPressed()
        }

    }


    fun setUserDataInUI(user: User){
        mUserDetails = user
        Glide
            .with(this)
            .load(user?.image)
            .centerCrop()
            .placeholder(R.drawable.ic_user_place_holder)
            .into(iv_profile_user_image)

        et_name_profile.setText(user?.name)
        et_email_profile.setText(user?.email)
        if(user.mobile != 0L){
            et_mobile_profile.setText(user?.mobile.toString())
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK
            && requestCode == PICK_IMAGE_REQUEST_CODE
            && data!!.data != null
        ) {

            mSelectedImageFileUri = data.data

            try {
                Glide
                    .with(this@MyProfileActivity)
                    .load(Uri.parse(mSelectedImageFileUri.toString()))
                    .centerCrop()
                    .placeholder(R.drawable.ic_user_place_holder)
                    .into(iv_profile_user_image)
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    private fun uploadUserImage() {

        showProgressDialog(resources.getString(R.string.please_wait))

        if (mSelectedImageFileUri != null) {


            val sRef: StorageReference = FirebaseStorage.getInstance().reference.child(
                "USER_IMAGE" + System.currentTimeMillis() + "." + Constants.getFileExtension(
                    this,mSelectedImageFileUri
                )
            )


            sRef.putFile(mSelectedImageFileUri!!)
                .addOnSuccessListener { taskSnapshot ->

                    Log.e(
                        "Firebase Image URL",
                        taskSnapshot.metadata!!.reference!!.downloadUrl.toString()
                    )

                    taskSnapshot.metadata!!.reference!!.downloadUrl
                        .addOnSuccessListener { uri ->
                            Log.e("Downloadable", uri.toString())
                            mProfileImageURL = uri.toString()

                            updateUserProfileData()

                        }
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(
                        this@MyProfileActivity,
                        exception.message,
                        Toast.LENGTH_LONG
                    ).show()

                    hideProgressDialog()
                }
        }
    }

    fun profileUpdateSuccess(){
        hideProgressDialog()
        setResult(Activity.RESULT_OK)
        finish()
    }

    ////update user details to firebase
    private fun updateUserProfileData() {

        val userHashMap = HashMap<String, Any>()

        if (mProfileImageURL.isNotEmpty() && mProfileImageURL != mUserDetails.image) {
            userHashMap[Constants.IMAGE] = mProfileImageURL
        }

        if (et_name_profile.text.toString() != mUserDetails.name) {
            userHashMap[Constants.NAME] = et_name_profile.text.toString()
        }

        if (et_mobile_profile.text.toString() != mUserDetails.mobile.toString()) {
            userHashMap[Constants.MOBILE] = et_mobile_profile.text.toString().toLong()
        }
        // Update the data in the database.
        FireStoreClass()
            .updateUserProfileData(this@MyProfileActivity, userHashMap)
    }


}