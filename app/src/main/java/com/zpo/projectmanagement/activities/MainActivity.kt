package com.zpo.projectmanagement.activities

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.GravityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.iid.FirebaseInstanceId
import com.zpo.projectmanagement.Firebase.FireStoreClass
import com.zpo.projectmanagement.R
import com.zpo.projectmanagement.adapters.BoardItemsAdapter
import com.zpo.projectmanagement.models.Board
import com.zpo.projectmanagement.models.User
import kotlinx.android.synthetic.main.activity_main.*
import com.zpo.projectmanagement.utils.Constants
import kotlinx.android.synthetic.main.app_bar_main.*
import kotlinx.android.synthetic.main.main_content.*

class MainActivity: BaseActivity() , NavigationView.OnNavigationItemSelectedListener{

    companion object{
        const val myProfileRequestCode = 11
        const val CREATE_BOARD_REQUEST_CODE = 22
    }
    private lateinit var mSharedPreferences: SharedPreferences

    private lateinit var mUserName : String
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setupActionBar()

        nav_view.setNavigationItemSelectedListener (this)
        FireStoreClass()
            .loadUserData(this@MainActivity,true)

        mSharedPreferences =
            this.getSharedPreferences(Constants.PJMANAGE_PREFERENCES, Context.MODE_PRIVATE)

        val tokenUpdated = mSharedPreferences.getBoolean(Constants.FCM_TOKEN_UPDATED, false)

        // Here if the token is already updated than we don't need to update it every time.
        if (tokenUpdated) {
            // Get the current logged in user details.
            // Show the progress dialog.
            showProgressDialog(resources.getString(R.string.please_wait))
            FireStoreClass()
                .loadUserData(this@MainActivity, true)
        } else {
            FirebaseInstanceId.getInstance()
                .instanceId.addOnSuccessListener(this@MainActivity) { instanceIdResult ->
                    updateFCMToken(instanceIdResult.token)
                }
        }

        fab_create_board.setOnClickListener{
            val intent = Intent(this,
                CreateBoardActivity::class.java)
            intent.putExtra(Constants.NAME,mUserName!!)
            startActivityForResult(intent,
                CREATE_BOARD_REQUEST_CODE
            )
        }


    }

    private fun setupActionBar(){
        setSupportActionBar(toolbar_main_activity)
        toolbar_main_activity.setNavigationIcon(R.drawable.ic_baseline_menu_24)
        toolbar_main_activity.setNavigationOnClickListener {
            toggleDrawer()
        }
    }

    private fun toggleDrawer() {
        if (drawer_layout.isDrawerOpen(GravityCompat.START)) {
            drawer_layout.closeDrawer(GravityCompat.START)
        } else {
            drawer_layout.openDrawer(GravityCompat.START)
        }
    }

    override fun onBackPressed() {
        if (drawer_layout.isDrawerOpen(GravityCompat.START)) {
            drawer_layout.closeDrawer(GravityCompat.START)
        } else {

            doubleBackToExit()
        }
    }

    fun updateNavigationUserDetails(user: User, readBoardList:Boolean) {
        hideProgressDialog()
        mUserName = user.name!!
        val headerView = nav_view.getHeaderView(0)
        val navUserImage = headerView.findViewById<ImageView>(R.id.nav_user_image)

        Glide
            .with(this@MainActivity)
            .load(user.image)
            .centerCrop()
            .placeholder(R.drawable.ic_user_place_holder)
            .into(navUserImage)

        val navUsername = headerView.findViewById<TextView>(R.id.tv_username)
        navUsername.text = user.name

        if(readBoardList){
            showProgressDialog(resources.getString(R.string.please_wait))
            FireStoreClass().getBoardsList(this)
        }
    }


    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.nav_my_profile ->{
                val intent = Intent(this,
                    MyProfileActivity::class.java)
                startActivityForResult(intent,
                    myProfileRequestCode
                )

            }
            R.id.nav_sign_out ->{
                FirebaseAuth.getInstance().signOut()
                mSharedPreferences.edit().clear().apply()
                val intent = Intent(this,
                    IntroActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
                finish()
            }
        }
        drawer_layout.closeDrawer(GravityCompat.START)
        return true
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(resultCode == Activity.RESULT_OK &&
            requestCode == myProfileRequestCode
        ){
            FireStoreClass().loadUserData(this)
        }else if (resultCode == Activity.RESULT_OK){
            if(requestCode == CREATE_BOARD_REQUEST_CODE){
                FireStoreClass().getBoardsList(this)
            }
        }
        else{
            Log.e("Canceled","Canceled")
        }
    }

    fun populateBoardsListToUI(boardList:ArrayList<Board>){
        hideProgressDialog()

        if(boardList.size > 0){
            rv_boards_list.visibility = View.VISIBLE
            tv_no_boards_available.visibility = View.GONE

            rv_boards_list.layoutManager = LinearLayoutManager(this)
            rv_boards_list.setHasFixedSize(true)

            val adapter =
                BoardItemsAdapter(this, boardList)
            rv_boards_list.adapter = adapter

            adapter.setOnClickListener(
                object  : BoardItemsAdapter.OnClickListener{
                    override fun onClick(position: Int, model: Board) {
                        super.onClick(position, model)
                        val intent = Intent(this@MainActivity, TaskListActivity::class.java)
                        intent.putExtra(Constants.DOCUMENT_ID, model.documentId)
                        startActivity(intent)
                    }
                }
            )


        }else{
            rv_boards_list.visibility = View.GONE
            tv_no_boards_available.visibility = View.VISIBLE
        }
    }

    fun tokenUpdateSuccess() {

        hideProgressDialog()

        // Here we have added a another value in shared preference that the token is updated in the database successfully.
        // So we don't need to update it every time.
        val editor: SharedPreferences.Editor = mSharedPreferences.edit()
        editor.putBoolean(Constants.FCM_TOKEN_UPDATED, true)
        editor.apply()

        // Get the current logged in user details.
        // Show the progress dialog.
        showProgressDialog(resources.getString(R.string.please_wait))
        FireStoreClass()
            .loadUserData(this@MainActivity, true)
    }
    private fun updateFCMToken(token: String) {

        val userHashMap = HashMap<String, Any>()
        userHashMap[Constants.FCM_TOKEN] = token

        // Update the data in the database.
        // Show the progress dialog.
        showProgressDialog(resources.getString(R.string.please_wait))
        FireStoreClass()
            .updateUserProfileData(this@MainActivity, userHashMap)
    }
}