package edu.hebut.retrofittest.supabase.dao

import edu.hebut.ActivityLifeCycle.supabaseUtil.SupabaseDataUtils
import edu.hebut.retrofittest.supabase.entity.User
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.result.PostgrestResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.future.future
import kotlinx.coroutines.withContext
import java.io.PrintStream
import java.util.concurrent.CompletableFuture

class UserDao {
    companion object {
        // 获取supabase实例
        val supabase = SupabaseDataUtils.getClient()

        @JvmStatic
        // 插入用户
        fun insertUser(user: User): CompletableFuture<PostgrestResult> {
            return CoroutineScope(Dispatchers.IO).future {
                supabase.from("user").insert(
                    mapOf(
                        "name" to user.name,
                        "password" to user.password
                    )
                )
            }
        }

        @JvmStatic
        // 根据id删除用户
        suspend fun deleteUserById(id: Long) {
            supabase.from("user").delete {
                filter {
                    eq("id", id)
                }
            }
        }

        @JvmStatic
        // 根据id获取用户
        fun getUserById(id: Long): CompletableFuture<User> {
            return CoroutineScope(Dispatchers.IO).future {
                supabase.from("user").select {
                    filter {
                        eq("id", id)
                    }
                }.decodeSingle<User>()
            }
        }

        @JvmStatic
        // 根据用户名获取用户
        fun getUserByName(name: String): CompletableFuture<User?> {
            return CoroutineScope(Dispatchers.IO).future {
                try {
                    supabase.from("user")
                        .select {
                            filter {
                                eq("name", name)
                            }
                        }
                        .decodeSingleOrNull<User>() // 使用 decodeSingleOrNull 避免抛出异常
                } catch (e: Exception) {
                    null // 捕获异常并返回 null
                }
            }
        }

        @JvmStatic
        // 根据用户名和密码获取用户/
        fun getUserByNameAndPassword(name: String, password: String): CompletableFuture<User?> {
            return CoroutineScope(Dispatchers.IO).future {
                try {
                    supabase.from("user")
                        .select {
                            filter {
                                eq("name", name)
                                eq("password", password)
                            }
                        }
                        .decodeSingleOrNull<User>() // 使用 decodeSingleOrNull 避免抛出异常
                } catch (e: Exception) {
                    null // 捕获异常并返回 null
                }
            }
        }

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
                    eq("id", user.id!!)
                }
            }
        }
    }
}


suspend fun main() {
    withContext(Dispatchers.IO) {
        System.setOut(PrintStream(System.out, true, "UTF-8"))
    }
    UserDao.getUserByNameAndPassword("adin", "123").join().let {
        println(it)
    }
}