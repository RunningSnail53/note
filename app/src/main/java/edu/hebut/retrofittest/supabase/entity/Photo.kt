package edu.hebut.retrofittest.supabase.entity

import kotlinx.serialization.Serializable

/**
 * Photo实体类，对应数据库中的photo表
 */
@Serializable
data class Photo(
    val id: Long,                // 图片编号，主键
    val bucket_name: String,     // 桶名
    val object_name: String,     // 图片名称
    val note_id: Long            // 日记编号，外键
)
