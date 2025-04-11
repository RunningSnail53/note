package edu.hebut.retrofittest.supabase.entity
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import java.util.Date

@Serializable
data class Note(
    val id: Long,                  // 日记id，主键
    val user_id: Long,             // 用户id，外键
    val title: String,             // 标题
    val content: String,           // 内容
    @Contextual val createTime: Date,     // 创建时间
    @Contextual val updateTime: Date,     // 更新时间
    val year: Int? = null,         // 年份
    val month: Int? = null,        // 月份
    val day: Int? = null,          // 日期
    val recordPath: String? = null, // 录音路径
    val link: String? = null,      // 文档链接
    val weather: Weather           // 天气
)

/**
 * 天气枚举类
 */
enum class Weather {
    晴天, 阴天, 雨天, 雪天, 大风
}
