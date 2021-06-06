package com.broto.projtracker.firebase

import android.util.Log
import com.broto.projtracker.activities.SigninActivity
import com.broto.projtracker.activities.SignupActivity
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
        mFirestore.collection(Constants.USERS)
            .document(getCurrentUserId()).set(userInfo, SetOptions.merge())
            .addOnSuccessListener {
                activity.userRegisteredSuccess()
            }
            .addOnFailureListener {
                Log.e(TAG, "Unable to add user signup FireStore. Error: ${it.message}")
                activity.userRegisteredFailed()
            }
    }

    fun getSignInUserData(activity: SigninActivity) {
        mFirestore.collection(Constants.USERS).document(getCurrentUserId()).get()
            .addOnSuccessListener { document ->
                val user = document.toObject(User::class.java)
                activity.userLoginSuccess(user)
            }
            .addOnFailureListener {
                Log.e(TAG, "Unable fetch user data from FireStore. Error: ${it.message}")
                activity.userLoginFailed()
            }

    }

}