package com.example.taskflow.firebase

import android.app.Activity
import android.util.Log
import android.widget.Toast
import com.example.taskflow.activities.*
import com.example.taskflow.models.Board
import com.example.taskflow.models.User
import com.example.taskflow.utils.Constants
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

class FirestoreClass{
    private val mFirestore = FirebaseFirestore.getInstance()
    fun registerUser(activity:SignUpActivity,userInfo: User){
        mFirestore.collection(Constants.USERS)
            .document(getCurrentUserId())
                .set(userInfo, SetOptions.merge())
            .addOnSuccessListener {
                activity.userRegisteredSuccess()
            }.addOnFailureListener{
                e->
                Log.e(activity.javaClass.simpleName,"error")
            }
    }
    fun getBoardDetails(activity: TaskListActivity, documentId: String){
        mFirestore.collection(Constants.BOARDS)
            .document(documentId)
            .get()
            .addOnSuccessListener {
                    document ->
                Log.i(activity.javaClass.simpleName, document.toString())
                val board = document.toObject(Board::class.java)!!
                board.documentId = document.id
                activity.boardDetails(board)
            }.addOnFailureListener{
                    e->
                activity.hideProgressDialog()
                Log.e(activity.javaClass.simpleName," error while creating board",e)

            }
    }
    fun createBoard(activity: CreateBoardActivity, board: Board){
        mFirestore.collection(Constants.BOARDS)
            .document()
            .set(board, SetOptions.merge())
            .addOnSuccessListener{
                Toast.makeText(activity,"board created s",Toast.LENGTH_SHORT).show()
                activity.boardCreatedSuccessfully()
            }.addOnFailureListener{
                exception ->
                activity.hideProgressDialog()
                Log.e(
                    activity.javaClass.simpleName,
                    "error while creating board",
                    exception
                )
            }
    }
    fun updateUserProfileData(activity: MyProfileActivity, userHashMap: HashMap<String, Any>){
        mFirestore.collection(Constants.USERS)
            .document(getCurrentUserId())
            .update(userHashMap)
            .addOnSuccessListener {
                Log.i(activity.javaClass.simpleName,"Profile Updated")
                Toast.makeText(activity,"Profile Update success",Toast.LENGTH_SHORT).show()
                activity.profileUpdateSuccess()
            }.addOnFailureListener{
                e->
                activity.hideProgressDialog()
                Log.e(activity.javaClass.simpleName,"Profile update error",e)
                Toast.makeText(activity,"Profile Update failure",Toast.LENGTH_SHORT).show()
            }
    }
    fun getBoardsList(activity: MainActivity){
        mFirestore.collection(Constants.BOARDS)
            .whereArrayContains(Constants.ASSIGNED_TO,getCurrentUserId())
            .get()
            .addOnSuccessListener {
                document ->
                Log.i(activity.javaClass.simpleName, document.documents.toString())
                val boardList : ArrayList<Board> = ArrayList()
                for(i in document.documents){
                    val board = i.toObject(Board::class.java)!!
                    board.documentId =i.id
                    boardList.add(board)
                }
                activity.populateBoardListToUI(boardList)
            }.addOnFailureListener{
                e->
                activity.hideProgressDialog()
                Log.e(activity.javaClass.simpleName," error while creating board",e)

            }
    }
    fun loadUserData(activity: Activity, readBoardsList: Boolean= false){
        mFirestore.collection(Constants.USERS)
            .document(getCurrentUserId())
            .get()
            .addOnSuccessListener {
                    document->
                val loggedInUser= document.toObject(User::class.java)
                if(loggedInUser !=null)
                        when(activity){
                           is SignInActivity ->{
                               activity.signInSuccess(loggedInUser)
                           }
                            is MainActivity ->{
                                activity.updateNavigationUserDetails(loggedInUser, readBoardsList)
                            }
                            is MyProfileActivity ->{
                                activity.setUserDataInUI(loggedInUser)
                            }
                        }
            }.addOnFailureListener{

                    e->
                when(activity){
                    is SignInActivity ->{
                        activity.hideProgressDialog()
                    }
                    is MainActivity ->{
                        activity.hideProgressDialog()
                    }
                }
                Log.e("SignInUser","error")
            }
    }
    fun getCurrentUserId():String{
        var currentUser=FirebaseAuth.getInstance().currentUser
        var currentUserId=""
        if(currentUser!=null){
            currentUserId=currentUser.uid
        }
        return currentUserId
    }
    fun addUpdateTaskList(activity: Activity, board: Board){
        val taskListHashMap = HashMap<String, Any>()
        taskListHashMap[Constants.TASK_LIST]= board.taskList

        mFirestore.collection(Constants.BOARDS)
            .document(board.documentId)
            .update(taskListHashMap)
            .addOnSuccessListener {
                Log.e(activity.javaClass.simpleName," Task List Updated Successfully")
               if(activity is TaskListActivity){
                   activity.addUpdateTaskListSuccess()
               } else if(activity is CardDetailsActivity){
                   activity.addUpdateTaskListSuccess()
               }
            }.addOnFailureListener{
                    exception->
                if(activity is TaskListActivity){
                    activity.hideProgressDialog()
                } else if(activity is CardDetailsActivity){
                    activity.hideProgressDialog()
                }

                Log.e(activity.javaClass.simpleName," error while creating board",exception)
            }
    }
    fun getAssignedMembersListDetails(activity: Activity, assignedTo: ArrayList<String>){
        mFirestore.collection(Constants.USERS).whereIn(Constants.ID, assignedTo).get().addOnSuccessListener {
            document ->
            Log.e(activity.javaClass.simpleName, document.documents.toString())
            val userList : ArrayList<User> = ArrayList()
            for(i in document.documents){
                val user= i.toObject(User::class.java)!!
                userList.add(user)
            }
            if (activity is MembersActivity) {
                activity.setupMembersList(userList)
            } else if (activity is TaskListActivity) {
                activity.boardMembersDetailList(userList)
            }

        }.addOnFailureListener{
                e ->
            if (activity is MembersActivity) {
                activity.hideProgressDialog()
            } else if (activity is TaskListActivity) {
                activity.hideProgressDialog()
            }
                Log.e(activity.javaClass.simpleName," error while creating list",e)
        }
    }
    fun getMemberDetails(activity: MembersActivity, email: String){
        mFirestore.collection(Constants.USERS).whereEqualTo(Constants.EMAIL , email).get().addOnSuccessListener {
            document ->
            if(document.documents.size > 0)
            {
                val user = document.documents[0].toObject(User::class.java)!!
                activity.memberDetails(user)
            }else{
                activity.hideProgressDialog()
                activity.showErrorSnackBar("NO such member found")
            }
        }.addOnFailureListener{
                e ->
            activity.hideProgressDialog()
            Log.e(activity.javaClass.simpleName," error while getting user details",e)
        }

    }
    fun assignMemberToBoard(activity: MembersActivity, board: Board, user: User){

        val assignedToHashMap = HashMap<String, Any>()
        assignedToHashMap[Constants.ASSIGNED_TO] = board.assignedTo
        mFirestore.collection(Constants.BOARDS)
            .document(board.documentId)
            .update(assignedToHashMap)
            .addOnSuccessListener {
                activity.memberAssignSuccess(user)
            }.addOnFailureListener{
                    e ->
                activity.hideProgressDialog()
                Log.e(activity.javaClass.simpleName," error while creating a board",e)
            }

    }
}