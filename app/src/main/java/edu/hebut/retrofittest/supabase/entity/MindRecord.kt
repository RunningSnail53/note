package edu.hebut.ActivityLifeCycle.supabaseUtil.entity

import kotlinx.serialization.Serializable

@Serializable
data class MindRecord(
    val id: Long,                // 心情编号，主键
    val score: Int,              // 心情得分
    val name: MindName,          // 心情名称
    val note_id: Long,           // 日记编号，外键
    val year: Int? = null,       // 年份
    val month: Int? = null,      // 月份
    val day: Int? = null         // 日期
)

/**
 * 心情名称枚举类
 */
@Serializable
enum class MindName {
    开心, 无感, 疲惫, 焦虑, 愤怒, 哀伤
}