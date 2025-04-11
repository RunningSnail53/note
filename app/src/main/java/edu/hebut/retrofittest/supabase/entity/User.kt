package edu.hebut.ActivityLifeCycle.supabaseUtil.entity

import kotlinx.serialization.Serializable

@Serializable
data class User(
    val id: Long,           // 用户id，主键
    val name: String?,      // 用户名，可为空
    val password: String?   // 密码，可为空
)