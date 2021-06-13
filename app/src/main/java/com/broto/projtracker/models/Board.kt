package com.broto.projtracker.models

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Board (
    var id: String = "",
    val name: String = "",
    val imageData: String = "",
    val createdBy: String = "",
    val assignedTo: ArrayList<String> = ArrayList()
): Parcelable