package com.broto.projtracker.activities

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import com.broto.projtracker.R
import com.broto.projtracker.utils.Constants
import com.bumptech.glide.Glide
import com.karumi.dexter.Dexter
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.single.PermissionListener
import kotlinx.android.synthetic.main.activity_create_board.*
import java.lang.Exception

class CreateBoardActivity : BaseActivity() {

    private val TAG = "CreateBoardActivity"

    private var mSelectedImageUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_board)

        setupActionBar()
    }

    fun onImageClickedListener(view: View) {
        Dexter.withContext(this).withPermission(
            Manifest.permission.READ_EXTERNAL_STORAGE
        ).withListener(object: PermissionListener {
            override fun onPermissionGranted(p0: PermissionGrantedResponse?) {
                // Permission available
                Constants.showImagePicker(this@CreateBoardActivity)
            }

            override fun onPermissionDenied(p0: PermissionDeniedResponse?) {
                Log.d(TAG, "External read permission denied")
                showErrorSnackBar(resources.getString(R.string.read_external_denied_message))
            }

            override fun onPermissionRationaleShouldBeShown(
                p0: PermissionRequest?,
                token: PermissionToken?
            ) {
                token?.continuePermissionRequest()
            }

        }).onSameThread().check()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode != Activity.RESULT_OK) {
            Log.d(TAG, "Response Code: $resultCode for Request Code: $requestCode")
            return
        }

        when (requestCode) {
            Constants.PICK_IMAGE_REQUEST_CODE -> {
                if (data?.data == null) {
                    Log.e(TAG, "No data found for request code: $requestCode")
                    return
                }

                mSelectedImageUri = data.data

                try {
                    Glide.with(this)
                        .load(mSelectedImageUri)
                        .centerCrop()
                        .placeholder(R.drawable.ic_board_place_holder)
                        .into(iv_create_board_image)
                } catch (e: Exception) {
                    Log.e(TAG, "Error while loading user image. Details: ${e.message}")
                }
            }
        }
    }

    private fun setupActionBar() {
        setSupportActionBar(toolbar_create_board_activity)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_back_white_color_24dp)
        supportActionBar?.title = resources.getString(R.string.create_board_title)

        toolbar_create_board_activity.setNavigationOnClickListener {
            onBackPressed()
        }
    }
}