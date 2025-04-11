package edu.hebut.ActivityLifeCycle.supabaseUtil.entity

import kotlinx.serialization.Serializable

/**
 * Music实体类，对应数据库中的music表
 */
@Serializable
data class Music(
    val id: Long,                // 音乐编号，主键
    val bucket_name: String,     // 桶名
    val object_name: String,     // 音乐名称
    val note_id: Long            // 日记编号，外键
)
