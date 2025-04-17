package edu.hebut.retrofittest.supabase.dao

import edu.hebut.ActivityLifeCycle.supabaseUtil.SupabaseDataUtils
import edu.hebut.retrofittest.supabase.entity.Photo
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.future.future
import kotlinx.coroutines.withContext
import java.io.PrintStream
import java.util.concurrent.CompletableFuture

class PhotoDao {
    companion object {
        val supabase = SupabaseDataUtils.getClient()

        @JvmStatic
        fun insertPhoto(photo: Photo): CompletableFuture<Unit> {
            return CoroutineScope(Dispatchers.IO).future {
                supabase.from("photo").insert(
                    mapOf<String, Any>(
                        "bucket_name" to photo.bucket_name,
                        "object_name" to photo.object_name,
                        "note_id" to photo.note_id
                    )
                )
            }
        }

        @JvmStatic
        fun updatePhoto(photo: Photo): CompletableFuture<Unit> {
            return CoroutineScope(Dispatchers.IO).future {
                supabase.from("photo").update(photo) {
                    filter {
                        eq("id", photo.id!!)
                    }
                }
            }
        }


        @JvmStatic
        fun deletePhotoByNoteId(noteId: Long): CompletableFuture<Unit> {

            return CoroutineScope(Dispatchers.IO).future {
                MusicDao.Companion.supabase.from("photo").delete() {
                    filter { eq("note_id", noteId) }
                }
            }

        }

        @JvmStatic
        suspend fun getPhotoById(id: Long): Photo {
            return supabase.from("photo").select() {
                filter {
                    eq("id", id)
                }
            }.decodeSingle()
        }

        @JvmStatic
        fun getPhotosByNoteId(noteId: Long): CompletableFuture<List<Photo>> {
            return CoroutineScope(Dispatchers.IO).future {
                supabase.from("photo").select() {
                    filter {
                        eq("note_id", noteId)
                    }
                }.decodeList()
            }
        }

        @JvmStatic
        suspend fun getAllPhotos(): List<Photo> {
            return supabase.from("photo").select().decodeList()
        }

    }
}

suspend fun main() {
    withContext(Dispatchers.IO) {
        System.setOut(PrintStream(System.out, true, "UTF-8"))
    }

    PhotoDao.getAllPhotos().forEach(::println)
}