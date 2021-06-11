package com.broto.projtracker.activities

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log
import android.webkit.MimeTypeMap
import android.widget.Toast
import com.broto.projtracker.R
import com.broto.projtracker.firebase.FireStoreClass
import com.broto.projtracker.models.User
import com.broto.projtracker.utils.Constants
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import com.karumi.dexter.Dexter
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.single.PermissionListener
import kotlinx.android.synthetic.main.activity_profile.*
import java.lang.Exception

class ProfileActivity :
    BaseActivity(),
    FireStoreClass.SignedInUserDetails,
    FireStoreClass.UpdateUserDetails {

    companion object {
        private const val PICK_IMAGE_REQUEST_CODE = 1
    }

    private val TAG = "ProfileActivity"

    private var mSelectedImageUri: Uri? = null
    private var mRemoteImageUri: Uri? = null

    private lateinit var mCurrentUser: User

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        setUpActionBar()
        FireStoreClass.getInstance().getCurrentUserData(this)

        profile_user_image.setOnClickListener {
            Dexter.withContext(this).withPermission(
                Manifest.permission.READ_EXTERNAL_STORAGE)
                .withListener(object : PermissionListener {
                    override fun onPermissionGranted(p0: PermissionGrantedResponse?) {
                        // Permission Available
                        showImagePicker()
                    }

                    override fun onPermissionDenied(p0: PermissionDeniedResponse?) {
                        Log.d(TAG, "External Read permission denied")
                        showErrorSnackBar(resources.getString(R.string.read_external_denied_message))
                    }

                    override fun onPermissionRationaleShouldBeShown(
                        request: PermissionRequest?,
                        token: PermissionToken?
                    ) {
                        showPermissionRationale()
                        token?.cancelPermissionRequest()
                    }

                }).onSameThread().check()
        }

        profile_btn_update.setOnClickListener {
            if (mSelectedImageUri != null) {
                uploadUserImage()
            } else {
                showProgressDialog(resources.getString(R.string.please_wait))
                updateUserProfileRemote()
            }
        }
    }

    private fun showImagePicker() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, PICK_IMAGE_REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode != Activity.RESULT_OK) {
            Log.d(TAG, "Response Code: $resultCode for Request Code: $requestCode")
            return
        }

        when(requestCode) {
            PICK_IMAGE_REQUEST_CODE -> {
                if (data?.data == null) {
                    Log.e(TAG, "No data found for request code: $requestCode")
                    return
                }
                mSelectedImageUri = data.data

                try {
                    Glide.with(this)
                        .load(mSelectedImageUri)
                        .centerCrop()
                        .placeholder(R.drawable.ic_user_place_holder)
                        .into(profile_user_image)
                } catch (e: Exception) {
                    Log.e(TAG, "Error while loading user image. Details: ${e.message}")
                }

            }
        }
    }

    private fun showPermissionRationale() {
        AlertDialog.Builder(this)
            .setMessage(getString(R.string.permission_rationale_dialog_title))
            .setPositiveButton(getString(R.string.permission_rationale_dialog_positive_button)) { _, _ ->
                try {
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    val uri = Uri.fromParts("package", packageName, null)
                    intent.data = uri
                    startActivity(intent)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            .setNegativeButton(getString(R.string.permission_rationale_dialog_negative_button)) { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun setUpActionBar() {
        setSupportActionBar(toolbar_profile)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_back_white_color_24dp)
        supportActionBar?.title = resources.getString(R.string.my_profile)

        toolbar_profile.setNavigationOnClickListener {
            onBackPressed()
        }
    }

    override fun onFetchDetailsSuccess(user: User?) {
        if (user == null) {
            Log.e(TAG, "User Details not found for " +
                    "${FirebaseAuth.getInstance().currentUser?.uid}")
            return
        }

        mCurrentUser = user

        Glide.with(this)
            .load(user.imageData)
            .centerCrop()
            .placeholder(R.drawable.ic_user_place_holder)
            .into(profile_user_image)

        profile_et_name.setText(user.name)
        profile_et_email.setText(user.email)
        if (user.mobile != 0L) {
            profile_et_phone_number.setText(user.mobile.toString())
        }

    }

    override fun onFetchDetailsFailed() {
        Log.e(TAG, "Unable fetch user details for UUID: " +
                "${FirebaseAuth.getInstance().currentUser?.uid}")
    }

    private fun getFileExtension(uri: Uri): String? {
        return MimeTypeMap.getSingleton().getExtensionFromMimeType(contentResolver.getType(uri))
    }

    private fun uploadUserImage() {
        showProgressDialog(resources.getString(R.string.please_wait))
        val uuid = FireStoreClass.getInstance().getCurrentUserId()
        if (uuid.isEmpty()) {
            Log.e(TAG, "User details not found")
            return
        }

        // ImageName format: <uuid_timestamp.ext>

        if (mSelectedImageUri != null) {
            val sRef = FirebaseStorage.getInstance().reference.child(
                "${uuid}_${System.currentTimeMillis()}.${getFileExtension(mSelectedImageUri!!)}"
            )
            sRef.putFile(mSelectedImageUri!!).addOnSuccessListener { taskSnapshot ->

                taskSnapshot.metadata?.reference?.downloadUrl?.addOnSuccessListener {
                    Log.d(TAG, "Uploaded Image URI: $it")
                    mRemoteImageUri = it
                    updateUserProfileRemote()
                }
                ?.addOnCanceledListener {
                    hideProgressDialog()
                    showErrorSnackBar(resources.getString(R.string.failed_to_upload_file))
                    Log.e(TAG, "File Upload cancelled")
                }
                ?.addOnFailureListener{
                    hideProgressDialog()
                    showErrorSnackBar(resources.getString(R.string.failed_to_upload_file))
                    Log.e(TAG,"Failed to upload user image. Exception: ${it.message}")
                }
            }
            .addOnCanceledListener {
                hideProgressDialog()
                showErrorSnackBar(resources.getString(R.string.failed_to_upload_file))
                Log.e(TAG, "File Upload cancelled")
            }
            .addOnFailureListener {
                hideProgressDialog()
                showErrorSnackBar(resources.getString(R.string.failed_to_upload_file))
                Log.e(TAG,"Failed to upload user image. Exception: ${it.message}")
            }
        }
    }

    override fun onUpdateSuccess() {
        Log.d(TAG,"Details updated for uuid: ${FirebaseAuth.getInstance().currentUser?.uid}")
        hideProgressDialog()
        finish()
    }

    override fun onUpdateFailed() {
        Log.d(TAG,"Failed to updated for uuid: ${FirebaseAuth.getInstance().currentUser?.uid}")
        hideProgressDialog()
        Toast.makeText(
            this,
            resources.getString(R.string.failed_to_update_user_details),
            Toast.LENGTH_SHORT
        ).show()
        finish()
    }

    private fun updateUserProfileRemote() {
        var flag = false
        var userHashMap = HashMap<String, Any>()
        if (mRemoteImageUri.toString().isNotEmpty() &&
                mRemoteImageUri.toString() != mCurrentUser.imageData) {
            userHashMap[Constants.IMAGE_DATA] = mRemoteImageUri.toString()
            flag = true
        }

        if (profile_et_name.text.toString() != mCurrentUser.name) {
            userHashMap[Constants.NAME] = profile_et_name.text.toString()
            flag = true
        }

        if (profile_et_phone_number.text.toString() != mCurrentUser.name) {
            userHashMap[Constants.MOBILE] = profile_et_phone_number.text.toString().toLong()
            flag = true
        }

        if (flag) {
            Log.d(TAG, "Updated hashmap: $userHashMap")
            FireStoreClass.getInstance().updateUserProfile(this, userHashMap)
        } else {
            Toast.makeText(
                this,
                "no_details_updated",
                Toast.LENGTH_SHORT
            ).show()
            finish()
        }
    }
}