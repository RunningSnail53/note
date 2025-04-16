package edu.hebut.retrofittest.supabase.dao

import edu.hebut.ActivityLifeCycle.supabaseUtil.SupabaseDataUtils
import edu.hebut.retrofittest.supabase.entity.Music
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.future.future
import kotlinx.coroutines.withContext
import java.io.PrintStream
import java.util.concurrent.CompletableFuture

class MusicDao {
    companion object {
        val supabase = SupabaseDataUtils.getClient()

        suspend fun insertMusic(music: Music) {
            supabase.from("music").insert(music)
        }

        @JvmStatic
        fun deleteMusicByNoteId(noteId: Long): CompletableFuture<Unit> {

           return CoroutineScope(Dispatchers.IO).future {
               supabase.from("music").delete() {
                   filter { eq("note_id", noteId) }
               }
           }

        }

        @JvmStatic
        fun getMusicListByNoteId(noteId: Long): CompletableFuture<List<Music>> {
            return CoroutineScope(Dispatchers.IO).future {
                supabase.from("music").select {
                    filter {
                        eq("note_id", noteId)
                    }
                }.decodeList<Music>()
            }
        }


    }

}


suspend fun main() {
    withContext(Dispatchers.IO) {
        System.setOut(PrintStream(System.out, true, "UTF-8"))
    }
}