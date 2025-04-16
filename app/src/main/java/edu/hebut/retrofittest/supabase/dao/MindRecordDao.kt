package edu.hebut.retrofittest.supabase.dao

import edu.hebut.ActivityLifeCycle.supabaseUtil.SupabaseDataUtils
import edu.hebut.retrofittest.supabase.entity.MindRecord
import edu.hebut.retrofittest.supabase.entity.Note
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.future.future
import kotlinx.coroutines.withContext
import java.io.PrintStream
import java.util.concurrent.CompletableFuture

class MindRecordDao {
    companion object {
        val supabase = SupabaseDataUtils.getClient()

        suspend fun insertMindRecord(mindRecord: MindRecord) {
            supabase.from("mind_record").insert(mindRecord)
        }

        @JvmStatic
        fun deleteMindRecordByNoteId(noteId: Long): CompletableFuture<Unit> {

            return CoroutineScope(Dispatchers.IO).future {
                supabase.from("mind_record").delete() {
                    filter { eq("note_id", noteId) }
                }
            }
        }

        @JvmStatic
        fun getMindRecordsByUserId(userId: Long): CompletableFuture<List<MindRecord>> {
            return CoroutineScope(Dispatchers.IO).future {

                var idList = supabase.from("note").select() {
                    filter {
                        eq("user_id", userId)
                    }
                    limit(30)
                }.decodeList<Note>().map { it.id }

                supabase.from("mind_record").select {
                    filter {
                        MindRecord::id isIn idList
                    }
                }.decodeList<MindRecord>()
            }
        }

        @JvmStatic
        fun getMindRecordByNoteId(id: Long): CompletableFuture<MindRecord> {
            return CoroutineScope(Dispatchers.IO).future {
                supabase.from("mind_record").select {
                    filter {
                        eq("note_id", id)
                    }
                }.decodeSingle()
            }
        }
    }
}

suspend fun main() {
    withContext(Dispatchers.IO) {
        System.setOut(PrintStream(System.out, true, "UTF-8"))
    }
}