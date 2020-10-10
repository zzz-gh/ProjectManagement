package com.zpo.projectmanagement.activities

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.bumptech.glide.Glide
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.zpo.projectmanagement.Firebase.FireStoreClass
import com.zpo.projectmanagement.R
import com.zpo.projectmanagement.models.Board
import com.zpo.projectmanagement.models.User
import com.zpo.projectmanagement.utils.Constants
import kotlinx.android.synthetic.main.activity_create_board.*
import java.io.IOException

class CreateBoardActivity  : BaseActivity() {
    private var mSelectedImageBoardFileUri: Uri? = null
    private lateinit var mUserName:String
    private var mBoardImageURL:String = ""
    private lateinit var mBoardDetails: User

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_board)

        setupActionBar()

        if(intent.hasExtra(Constants.NAME)){
            mUserName = intent.getStringExtra(Constants.NAME).toString()
        }

        iv_board_image.setOnClickListener{
            Constants.imageLoad(this)
        }

        btn_create.setOnClickListener {
            if(mSelectedImageBoardFileUri != null){
                uploadUserImage()
            }else{
                showProgressDialog(resources.getString(R.string.please_wait))
                createBoard()
            }
        }

    }
    /////////////////////////////////////////////////
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK
            && requestCode == Constants.PICK_IMAGE_REQUEST_CODE
            && data!!.data != null
        ) {

            mSelectedImageBoardFileUri = data.data

            try {
                Glide
                    .with(this@CreateBoardActivity)
                    .load(Uri.parse(mSelectedImageBoardFileUri.toString()))
                    .centerCrop()
                    .placeholder(R.drawable.ic_board_place_holder)
                    .into(iv_board_image)
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }



    /////////////////////////

    private fun setupActionBar() {

        setSupportActionBar(toolbar_create_board_activity)

        val actionBar = supportActionBar
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_white_color_back_24dp)
            actionBar.title = resources.getString(R.string.create_board_title)
        }

        toolbar_create_board_activity.setNavigationOnClickListener { onBackPressed() }
    }

    fun createdBoardSuccessfully(){
        hideProgressDialog()
        setResult(Activity.RESULT_OK)
        finish()
    }

    private fun createBoard(){
        val assignedUserArrayList:ArrayList<String> = ArrayList()
        assignedUserArrayList.add(getCurrentUserID())
        val board = Board(
            et_board_name.text.toString(),
            mBoardImageURL,
            mUserName,
            assignedUserArrayList
        )

        FireStoreClass().createBoard(this,board)
    }

    private fun uploadUserImage() {

        showProgressDialog(resources.getString(R.string.please_wait))

        if (mSelectedImageBoardFileUri != null) {


            val sRef: StorageReference = FirebaseStorage.getInstance().reference.child(
                "BOARD_IMAGE" + System.currentTimeMillis() + "." + Constants.getFileExtension(
                    this,mSelectedImageBoardFileUri
                )
            )


            sRef.putFile(mSelectedImageBoardFileUri!!)
                .addOnSuccessListener { taskSnapshot ->

                    Log.e(
                        "Board Image URL",
                        taskSnapshot.metadata!!.reference!!.downloadUrl.toString()
                    )

                    taskSnapshot.metadata!!.reference!!.downloadUrl
                        .addOnSuccessListener { uri ->
                            Log.e("Downloadable ", uri.toString())
                            mBoardImageURL = uri.toString()

                            createBoard()



                        }
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(
                        this@CreateBoardActivity,
                        exception.message,
                        Toast.LENGTH_LONG
                    ).show()

                    hideProgressDialog()
                }
        }
    }



}