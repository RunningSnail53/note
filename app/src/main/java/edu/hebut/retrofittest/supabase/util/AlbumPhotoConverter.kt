package edu.hebut.retrofittest.supabase.util

import android.util.Log
import com.yanzhenjie.album.AlbumFile
import edu.hebut.retrofittest.supabase.entity.Photo

object AlbumPhotoConverter {

    // AlbumFile → Photo
    fun toPhoto(albumFile: AlbumFile, noteId: Long = 0): Photo {
        return Photo(
            id = null,
            bucket_name = albumFile.bucketName ?: "default_bucket",
            object_name = generateFileName(albumFile),
            note_id = noteId
        ).also {
            // 日志记录
            Log.d("225188","Converted AlbumFile[${albumFile.path}] to Photo[${it.publicUrl}]")
        }
    }

    // Photo → AlbumFile
    fun toAlbumFile(photo: Photo): AlbumFile {
        return AlbumFile().apply {

            this.bucketName = photo.bucket_name
            this.path = photo.publicUrl // 关键修改：直接使用Supabase URL
            this.mimeType = getMimeType(photo.object_name)

        }
    }

    private fun getMimeType(filename: String): String {
        return when (filename.substringAfterLast(".").lowercase()) {
            "jpg", "jpeg" -> "image/jpeg"
            "png" -> "image/png"
            "gif" -> "image/gif"
            "mp4" -> "video/mp4"
            else -> "application/octet-stream"
        }
    }

    private fun generateFileName(albumFile: AlbumFile): String {
        val ext = albumFile.mimeType?.substringAfter("/") ?: "jpg"
        return "img_${System.currentTimeMillis()}.$ext"
    }
}