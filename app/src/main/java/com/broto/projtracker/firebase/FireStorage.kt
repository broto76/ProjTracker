package com.broto.projtracker.firebase

import android.content.ContentResolver
import android.net.Uri
import android.util.Log
import com.broto.projtracker.utils.Constants
import com.google.firebase.storage.FirebaseStorage

class FireStorage private constructor() {

    private val TAG = "FireStorage"

    companion object {
        private lateinit var mInstance: FireStorage

        fun getInstance(): FireStorage {
            if (!this::mInstance.isInitialized) {
                mInstance = FireStorage()
            }
            return mInstance
        }
    }

    fun uploadFileToFirebase(
        uuid: String,
        imageUri: Uri,
        contentResolver: ContentResolver,
        callback: OnFileUploadCallback
    ) {
        if (uuid.isEmpty()) {
            Log.e(TAG, "User details not found")
            return
        }

        val storageReference = FirebaseStorage.getInstance().reference.child(
            "${uuid}_${System.currentTimeMillis()}." +
                    "${Constants.getFileExtension(imageUri, contentResolver)}"
        )

        storageReference.putFile(imageUri).addOnSuccessListener {
            it.metadata?.reference?.downloadUrl?.addOnSuccessListener { uri ->
                Log.d(TAG, "Uploaded Image at: $uri")
                callback.onFileUploadSuccess(uri)
            }?.addOnCanceledListener {
                Log.e(TAG, "File Upload cancelled")
                callback.onFileUploadFailed()
            }?.addOnFailureListener { e ->
                Log.e(TAG,"Failed to upload image. Exception: ${e.message}")
                callback.onFileUploadFailed()
            }
        }.addOnCanceledListener {
            Log.e(TAG, "File Upload cancelled")
            callback.onFileUploadFailed()
        }.addOnFailureListener { e ->
            Log.e(TAG,"Failed to upload image. Exception: ${e.message}")
            callback.onFileUploadFailed()
        }

    }

    interface OnFileUploadCallback {
        fun onFileUploadSuccess(uri: Uri)
        fun onFileUploadFailed()
    }
}