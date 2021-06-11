package com.broto.projtracker.utils

import android.app.Activity
import android.content.ContentResolver
import android.content.Intent
import android.net.Uri
import android.provider.MediaStore
import android.webkit.MimeTypeMap

object Constants {

    const val USERS: String = "users"

    const val IMAGE_DATA = "imageData"
    const val NAME = "name"
    const val MOBILE = "mobile"

    const val PICK_IMAGE_REQUEST_CODE = 1

    fun getFileExtension(uri: Uri, contentResolver: ContentResolver): String? {
        return MimeTypeMap.getSingleton().getExtensionFromMimeType(contentResolver.getType(uri))
    }

    fun showImagePicker(activity: Activity) {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        activity.startActivityForResult(intent, PICK_IMAGE_REQUEST_CODE)
    }

}