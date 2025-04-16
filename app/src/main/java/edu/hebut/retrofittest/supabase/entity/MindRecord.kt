package edu.hebut.retrofittest.supabase.entity

import android.annotation.SuppressLint
import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

@SuppressLint("UnsafeOptInUsageError")
@Parcelize
@Serializable
data class MindRecord(
    var id: Long,                // 心情编号，主键
    var score: Int,              // 心情得分
    var name: MindName,          // 心情名称
    var note_id: Long,           // 日记编号，外键
    var year: Int? = null,       // 年份
    var month: Int? = null,      // 月份
    var day: Int? = null         // 日期
): Parcelable{
    constructor():this(
        id = 0L,
        score = 0,
        name = MindName.无感,
        note_id = 0L
    )
}

/**
 * 心情名称枚举类
 */
@Parcelize
@Serializable
enum class MindName: Parcelable {
    开心, 无感, 疲惫, 焦虑, 愤怒, 哀伤
}