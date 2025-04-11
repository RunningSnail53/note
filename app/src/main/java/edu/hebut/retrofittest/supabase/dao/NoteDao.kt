package edu.hebut.retrofittest.supabase.dao

import edu.hebut.ActivityLifeCycle.supabaseUtil.SupabaseDataUtils
import edu.hebut.retrofittest.supabase.entity.Note
import io.github.jan.supabase.postgrest.from

class NoteDao {
    companion object{
        val supabase = SupabaseDataUtils.getClient()

        // 插入日记
        suspend fun insertNote(note: Note) {
            supabase.from("note").insert(note)
        }

        // 根据id删除日记
        suspend fun deleteNoteById(id: Long) {
            supabase.from("note").delete {
                filter {
                    eq("id", id)
                }
            }
        }

        // 根据id获取日记
        suspend fun getNoteById(id: Long): Note {
            val note = supabase.from("note").select {
                filter {
                    eq("id", id)
                }
            }.decodeSingle<Note>()
            return note
        }

        // 根据标题获取日记
        suspend fun getNoteByTitle(title: String): Note {
            val note = supabase.from("note").select {
                filter {
                    eq("title", title)
                }
            }.decodeSingle<Note>()
            return note
        }

        // 根据用户id获取日记
        suspend fun getAllNotesByUserId(userId: Long): List<Note> {
            val notes = supabase.from("note").select {
                filter {
                    eq("user_id", userId)
                }
            }.decodeList<Note>()
            return notes
        }

        // 获取所有日记
        suspend fun getAllNotes(): List<Note> {
            val notes = supabase.from("note").select().decodeList<Note>()
            return notes
        }

        // 更新日记信息
        suspend fun updateNote(note: Note) {
            supabase.from("note").update({
                set("title", note.title)
                set("content", note.content)
                set("updateTime", note.updateTime)
                set("year", note.year)
                set("month", note.month)
                set("day", note.day)
                set("recordPath", note.recordPath)
                set("link", note.link)
                set("weather", note.weather)
            }) {
                filter {
                    eq("id", note.id)
                }
            }
        }

    }
}