package com.zpo.projectmanagement.Firebase

import android.app.Activity
import android.util.Log
import android.widget.Toast
import com.zpo.projectmanagement.activities.*
import com.zpo.projectmanagement.models.Board

import com.zpo.projectmanagement.utils.Constants
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.zpo.projectmanagement.models.User


class FireStoreClass {
    private val mFireStore: FirebaseFirestore = FirebaseFirestore.getInstance()

    fun registerUser(activity: SignUpActivity, userInfo: com.zpo.projectmanagement.models.User){
        mFireStore.collection(Constants.USERS)
            .document(getCurrentUserId())
            .set(userInfo, SetOptions.merge())
            .addOnSuccessListener {
                activity.userRegisterSuccess()
            }
            .addOnFailureListener {
                e ->
                Log.e(
                    activity.javaClass.simpleName,
                    "Error while getting log in user details",
                    e
                )
            }
    }

/////creating board in firebase
    fun createBoard(activity: CreateBoardActivity, board: Board){
        mFireStore.collection(Constants.BOARD)
            .document()
            .set(board, SetOptions.merge())
            .addOnSuccessListener {

                Toast.makeText(activity,"Created board successfully",Toast.LENGTH_SHORT).show()
                activity.createdBoardSuccessfully()
            }
            .addOnFailureListener {
                    e ->
                Log.e(
                    activity.javaClass.simpleName,
                    "Error while creating board store in firebase",
                    e
                )
            }
    }
    fun getCurrentUserId(): String {

        val currentUser = FirebaseAuth.getInstance().currentUser

        var currentUserID = ""
        if (currentUser != null) {
            currentUserID = currentUser.uid
        }

        return currentUserID
    }

    fun loadUserData(activity: Activity,readBoardList:Boolean =  false){



        mFireStore.collection(Constants.USERS)
            .document(getCurrentUserId())
            .get()
            .addOnSuccessListener {
                    document ->

                val loggedInUser = document.toObject(User::class.java)
                if(loggedInUser != null)

                    when(activity){
                        is SignInActivity ->{
                            activity.signInSuccess(loggedInUser)
                        }

                        is MainActivity ->{
                            activity.updateNavigationUserDetails(loggedInUser,readBoardList)
                        }

                        is MyProfileActivity ->{
                            activity.setUserDataInUI(loggedInUser)
                        }


                    }



            }
            .addOnFailureListener {
                    e ->
                when(activity){
                    is SignInActivity ->{
                        activity.hideProgressDialog()
                    }
                    is MainActivity ->{
                        activity.hideProgressDialog()
                    }
                    is MyProfileActivity -> {
                        activity.hideProgressDialog()
                    }

                }
                Log.e(
                    "SignIn User",
                    "Error while getting loggedIn user details",
                    e
                )
            }

    }

    fun updateUserProfileData(activity: Activity, userHashMap: HashMap<String, Any>) {
        mFireStore.collection(Constants.USERS) // Collection Name
            .document(getCurrentUserId()) // Document ID
            .update(userHashMap) // A hashmap of fields which are to be updated.
            .addOnSuccessListener {
                Log.e(activity.javaClass.simpleName, "Data updated successfully!")

                // Notify the success result.

                when (activity) {
                    is MainActivity -> {
                        activity.tokenUpdateSuccess()
                    }
                    is MyProfileActivity -> {
                        activity.profileUpdateSuccess()
                    }
                }
            }
            .addOnFailureListener { e ->
                when (activity) {
                    is MainActivity -> {
                        activity.hideProgressDialog()
                    }
                    is MyProfileActivity -> {
                        activity.hideProgressDialog()
                    }
                }

                Log.e(
                    activity.javaClass.simpleName,
                    "Error while creating a board.",
                    e
                )
            }
    }


    fun getBoardsList(activity: MainActivity){
        mFireStore.collection(Constants.BOARD)
            .whereArrayContains(Constants.ASSIGNED_TO,getCurrentUserId())
            .get()
            .addOnSuccessListener { document ->
                Log.e(activity.javaClass.simpleName,document.documents.toString())

                val boardList:ArrayList<Board> = ArrayList()
                for (i in document.documents){
                    val board = i.toObject(Board::class.java)
                    board!!.documentId = i.id
                    boardList.add(board)
                }
                activity.populateBoardsListToUI(boardList)
            }
            .addOnFailureListener {
                e ->
                activity.hideProgressDialog()
                Log.e(activity.javaClass.simpleName,"Error while creating a board.",e)
            }

    }

    fun getBoardDetails(activity: TaskListActivity, documentId: String) {
        mFireStore.collection(Constants.BOARD)
            .document(documentId)
            .get()
            .addOnSuccessListener { document ->
                Log.e(activity.javaClass.simpleName, document.toString())

                val board = document.toObject(Board::class.java)!!
                board.documentId = document.id

                // Send the result of board to the base activity.
                activity.boardDetails(board)
            }
            .addOnFailureListener { e ->
                activity.hideProgressDialog()
                Log.e(activity.javaClass.simpleName, "Error while creating a board.", e)
            }
    }
    fun addUpdateTaskList(activity: Activity, board: Board) {

        val taskListHashMap = HashMap<String, Any>()
        taskListHashMap[Constants.TASK_LIST] = board.taskList

        mFireStore.collection(Constants.BOARD)
            .document(board.documentId)
            .update(taskListHashMap)
            .addOnSuccessListener {
                Log.e(activity.javaClass.simpleName, "TaskList updated successfully.")

                if (activity is TaskListActivity) {
                    activity.addUpdateTaskListSuccess()
                } else if (activity is CardDetailsActivity) {
                    activity.addUpdateTaskListSuccess()
                }
            }
            .addOnFailureListener { e ->
                if (activity is TaskListActivity) {
                    activity.hideProgressDialog()
                } else if (activity is TaskListActivity) {
                    activity.hideProgressDialog()
                }
                Log.e(activity.javaClass.simpleName, "Error while creating a board.", e)
            }
    }


    fun getAssignedMembersListDetails(activity: Activity, assignedTo: ArrayList<String>) {

        mFireStore.collection(Constants.USERS) // Collection Name
            .whereIn(
                Constants.ID,
                assignedTo
            ) // Here the database field name and the id's of the members.
            .get()
            .addOnSuccessListener { document ->
                Log.e(activity.javaClass.simpleName, document.documents.toString())

                val usersList: ArrayList<User> = ArrayList()

                for (i in document.documents) {
                    // Convert all the document snapshot to the object using the data model class.
                    val user = i.toObject(User::class.java)!!
                    usersList.add(user)
                }

                if(activity is MembersActivity) {
                    activity.setupMembersList(usersList)
                }else if(activity is TaskListActivity) {
                    activity.boardMembersDetailList(usersList)
                }
            }
            .addOnFailureListener { e ->
                if(activity is MembersActivity) {
                    activity.hideProgressDialog()
                }else if(activity is TaskListActivity){
                    activity.hideProgressDialog()
                }
                Log.e(
                    activity.javaClass.simpleName,
                    "Error while creating a board.",
                    e
                )
            }
    }

    fun getMemberDetails(activity: MembersActivity, email: String) {

        // Here we pass the collection name from which we wants the data.
        mFireStore.collection(Constants.USERS)
            // A where array query as we want the list of the board in which the user is assigned. So here you can pass the current user id.
            .whereEqualTo(Constants.EMAIL, email)
            .get()
            .addOnSuccessListener { document ->
                Log.e(activity.javaClass.simpleName, document.documents.toString())

                if (document.documents.size > 0) {
                    val user = document.documents[0].toObject(User::class.java)!!
                    // Here call a function of base activity for transferring the result to it.
                    activity.memberDetails(user)
                } else {
                    activity.hideProgressDialog()
                    activity.showErrorSnackBar("No such member found.")
                }

            }
            .addOnFailureListener { e ->
                activity.hideProgressDialog()
                Log.e(
                    activity.javaClass.simpleName,
                    "Error while getting user details",
                    e
                )
            }
    }
    fun assignMemberToBoard(activity: MembersActivity, board: Board, user: com.zpo.projectmanagement.models.User) {

        val assignedToHashMap = HashMap<String, Any>()
        assignedToHashMap[Constants.ASSIGNED_TO] = board.assignedTo

        mFireStore.collection(Constants.BOARD)
            .document(board.documentId)
            .update(assignedToHashMap)
            .addOnSuccessListener {
                Log.e(activity.javaClass.simpleName, "TaskList updated successfully.")
                activity.memberAssignSuccess(user)
            }
            .addOnFailureListener { e ->
                activity.hideProgressDialog()
                Log.e(activity.javaClass.simpleName, "Error while creating a board.", e)
            }
    }
}