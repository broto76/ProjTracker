package com.broto.projtracker.models

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class User (
        val id: String = "",
        val name: String = "",
        val email: String = "",
        val imageData: String = "",
        val mobile: Long = 0,
        val fcmToken: String = ""
) : Parcelable