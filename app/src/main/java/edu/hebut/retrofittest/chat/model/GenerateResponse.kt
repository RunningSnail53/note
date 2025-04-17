package edu.hebut.retrofittest.chat.model

import android.annotation.SuppressLint
import kotlinx.serialization.Serializable


@SuppressLint("UnsafeOptInUsageError")
@Serializable
data class GenerateResponse (
    val status: String,
    val caption: String,
    val style: String?,
    val image_status: String,
    val image_base64: String,
    val image_message: String,
)