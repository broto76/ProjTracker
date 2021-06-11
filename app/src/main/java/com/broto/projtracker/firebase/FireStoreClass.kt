package com.broto.projtracker.firebase

import android.util.Log
import com.broto.projtracker.activities.SignupActivity
import com.broto.projtracker.models.Board
import com.broto.projtracker.models.User
import com.broto.projtracker.utils.Constants
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

class FireStoreClass private constructor() {

    private val TAG = "FireStoreClass"

    companion object {
        private lateinit var mInstance: FireStoreClass

        fun getInstance(): FireStoreClass {
            if (!this::mInstance.isInitialized) {
                mInstance = FireStoreClass()
            }
            return mInstance
        }
    }

    private val mFirestore = FirebaseFirestore.getInstance()

    fun getCurrentUserId(): String {
        val currentUser = FirebaseAuth.getInstance().currentUser
        return currentUser?.uid ?: ""
    }

    fun registerUser(activity: SignupActivity, userInfo: User) {
        mFirestore.collection(Constants.COLLECTION_USERS)
            .document(getCurrentUserId()).set(userInfo, SetOptions.merge())
            .addOnSuccessListener {
                activity.userRegisteredSuccess()
            }
            .addOnFailureListener {
                Log.e(TAG, "Unable to add user signup FireStore. Error: ${it.message}")
                activity.userRegisteredFailed()
            }
    }

    fun getCurrentUserData(callback: SignedInUserDetails) {
        mFirestore.collection(Constants.COLLECTION_USERS).document(getCurrentUserId()).get()
            .addOnSuccessListener { document ->
                val user = document.toObject(User::class.java)
                callback.onFetchDetailsSuccess(user)
            }
            .addOnFailureListener {
                Log.e(TAG, "Unable fetch user data from FireStore. Error: ${it.message}")
                callback.onFetchDetailsFailed()
            }

    }

    fun updateUserProfile(callback: UpdateUserDetails, userHashMap: HashMap<String, Any>) {
        mFirestore.collection(Constants.COLLECTION_USERS).document(getCurrentUserId())
            .update(userHashMap)
            .addOnSuccessListener {
                Log.d(TAG,"Profile details updated successfully")
                callback.onUpdateSuccess()
            }
            .addOnFailureListener {
                Log.d(TAG,"Profile details updated failed: ${it.message}")
                callback.onUpdateFailed()
            }
    }

    fun createBoard(callback: CreateBoardCallbacks, board: Board) {
        mFirestore.collection(Constants.COLLECTION_BOARDS)
            .document()
            .set(board, SetOptions.merge())
            .addOnSuccessListener {
                Log.d(TAG, "Board created successfully")
                callback.onBoardCreateSuccess()
            }.addOnCanceledListener {
                Log.d(TAG, "Board creation failed")
                callback.onBoardCreateFailed()
            }.addOnFailureListener {
                Log.d(TAG, "Board creation failed. Error: ${it.message}")
                callback.onBoardCreateFailed()
            }
    }

    interface SignedInUserDetails {
        fun onFetchDetailsSuccess(user: User?)
        fun onFetchDetailsFailed()
    }

    interface UpdateUserDetails {
        fun onUpdateSuccess()
        fun onUpdateFailed()
    }

    interface CreateBoardCallbacks {
        fun onBoardCreateSuccess()
        fun onBoardCreateFailed()
    }

}