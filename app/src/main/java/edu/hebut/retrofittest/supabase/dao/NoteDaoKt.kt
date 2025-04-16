package edu.hebut.retrofittest.supabase.dao

import edu.hebut.ActivityLifeCycle.supabaseUtil.SupabaseDataUtils
import edu.hebut.retrofittest.supabase.entity.Note
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.future.future
import java.util.concurrent.CompletableFuture

class NoteDaoKt {
    companion object {
        val supabase = SupabaseDataUtils.getClient()

        // 插入日记
        @JvmStatic
        fun insertNote(note: Note): CompletableFuture<Unit> {
            return CoroutineScope(Dispatchers.IO).future {
                supabase.from("note").insert(
                    mapOf(
//                         "user_id" to note.user_id,
                        "title" to note.title,
                        "content" to note.content,
                        "createTime" to note.createTime,
                        "updateTime" to note.updateTime,
                        "year" to note.year,
                        "month" to note.month,
                        "day" to note.day,
                        "recordPath" to note.recordPath,
                        "link" to note.link,
                        "weather" to note.weather

                    )
                )
            }
        }

        @JvmStatic
        fun updateNote(note: Note): CompletableFuture<Unit> {
            return CoroutineScope(Dispatchers.IO).future {
                supabase.from("note").update(note)
            }
        }

        // 根据id删除日记
        @JvmStatic
        fun deleteNoteById(id: Long): CompletableFuture<Unit> {
            return CoroutineScope(Dispatchers.IO).future {
                val result = supabase.from("note").delete {
                    filter {
                        eq("id", id)
                    }
                }
            }
        }

        @JvmStatic
        fun getNoteListByUserId(userId: Long): CompletableFuture<List<Note>> {
            return CoroutineScope(Dispatchers.IO).future {
                supabase.from("note").select {
                    filter {
                        eq("user_id", userId)
                    }
                }.decodeList<Note>()
            }
        }

        @JvmStatic
        fun getNoteListByUserIdAndDate(userId:Long,date: String): CompletableFuture<List<Note>> {
            return CoroutineScope(Dispatchers.IO).future {
                supabase.from("note").select {
                    filter {
                        eq("user_id", userId)
                        eq("createTime", date)
                    }
                }.decodeList<Note>()
            }
        }

    }
}

