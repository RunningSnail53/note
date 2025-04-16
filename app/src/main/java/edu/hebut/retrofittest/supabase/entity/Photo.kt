package edu.hebut.retrofittest.supabase.entity

import android.annotation.SuppressLint
import android.os.Parcelable

import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

/**
 * Photo实体类，对应数据库中的photo表
 */
@SuppressLint("UnsafeOptInUsageError")
@Parcelize
@Serializable
data class Photo(
    var id: Long?,                // 图片编号，主键
    var bucket_name: String,     // 桶名
    var object_name: String,     // 图片名称
    var note_id: Long            // 日记编号，外键
) : Parcelable {
    constructor() : this(
        id = 0L,
        bucket_name = "",
        object_name = "",
        note_id = 0L
    )

    // 计算属性：直接获取Supabase公开URL
    val publicUrl: String
        get() = "https://jjnragzbvpqdjrcccgdl.supabase.co/storage/v1/object/public/$bucket_name//$object_name"

    fun decodeUrl(path: String) {
        // 在 Photo 类中添加成员函数
        fun decodeUrl(url: String) {
            val pattern = Regex("""/storage/v1/object/public/([^/]+)//(.+)$""")
            pattern.find(url)?.let {
                bucket_name = it.groupValues[1]
                object_name = it.groupValues[2]
            } ?: throw IllegalArgumentException("Invalid Supabase URL format")
        }
    }
}