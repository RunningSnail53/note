package edu.hebut.retrofittest.supabase.dao

import edu.hebut.ActivityLifeCycle.supabaseUtil.SupabaseDataUtils
import edu.hebut.retrofittest.supabase.entity.User
import io.github.jan.supabase.postgrest.from

class UserDao {
    companion object {
        // 获取supabase实例
        val supabase = SupabaseDataUtils.getClient()

        // 插入用户
        suspend fun insertUser(user: User) {
            supabase.from("user").insert(user)
        }

        // 根据id删除用户
        suspend fun deleteUserById(id: Long) {
            supabase.from("user").delete {
                filter {
                    eq("id", id)
                }
            }
        }

        // 根据id获取用户
        suspend fun getUserById(id: Long): User {
            val user = supabase.from("user").select {
                filter {
                    eq("id", id)
                }
            }.decodeSingle<User>()
            return user
        }

        // 根据用户名获取用户
        suspend fun getUserByName(name: String): User {
            val user = supabase.from("user").select {
                filter {
                    eq("name", name)
                }
            }.decodeSingle<User>()
            return user
        }

        // 根据用户名和密码获取用户
        suspend fun getAllUsers(): List<User> {
            val users = supabase.from("user").select().decodeList<User>()
            return users
        }

        // 更新用户信息
        suspend fun updateUser(user: User) {
            supabase.from("user").update({
                set("name", user.name)
                set("password", user.password)
            }) {
                filter {
                    eq("id", user.id)
                }
            }
        }
    }
}