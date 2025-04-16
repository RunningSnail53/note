package edu.hebut.retrofittest.supabase.entity
import android.annotation.SuppressLint
import android.os.Parcelable
import edu.hebut.retrofittest.supabase.serializer.DateSerializer
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

import java.util.Date


@SuppressLint("UnsafeOptInUsageError")
@Parcelize
@Serializable
data class Note(
    var id: Long,                  // 日记id，主键
    var user_id: Long,             // 用户id，外键
    var title: String,             // 标题
    var content: String,           // 内容
    @Serializable(with = DateSerializer::class) var createTime: Date,     // 创建时间
    @Serializable(with = DateSerializer::class) var updateTime: Date,     // 更新时间
    var year: Int? = null,         // 年份
    var month: Int? = null,        // 月份
    var day: Int? = null,          // 日期
    var recordPath: String? = null, // 录音路径
    var link: String? = null,      // 文档链接
    var weather: Weather           // 天气
) : Parcelable{
    // 手动实现无参构造
    constructor() : this(
        id = 0L,
        user_id = 0L,
        title = "",
        content = "",
        createTime = Date(),
        updateTime = Date(),
        weather = Weather.晴天
    )
}

/**
 * 天气枚举类
 */
@Parcelize
@Serializable
enum class Weather : Parcelable {
    晴天, 阴天, 雨天, 雪天, 大风
}
