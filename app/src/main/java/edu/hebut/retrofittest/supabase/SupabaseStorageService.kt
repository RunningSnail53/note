package edu.hebut.ActivityLifeCycle.supabaseUtil

import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import com.google.gson.annotations.SerializedName

// Supabase 配置
object SupabaseConfig {
    const val SUPABASE_URL = "https://jjnragzbvpqdjrcccgdl.supabase.co"
    const val SUPABASE_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImpqbnJhZ3pidnBxZGpyY2NjZ2RsIiwicm9sZSI6InNlcnZpY2Vfcm9sZSIsImlhdCI6MTc0MjIwNDE4OCwiZXhwIjoyMDU3NzgwMTg4fQ.KWViT-rmrQJIBJ3pkkGyDBOoHeYAdeRkolzYl9ZtMIk"
    const val SERVICE_ROLE_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImpqbnJhZ3pidnBxZGpyY2NjZ2RsIiwicm9sZSI6InNlcnZpY2Vfcm9sZSIsImlhdCI6MTc0MjIwNDE4OCwiZXhwIjoyMDU3NzgwMTg4fQ.KWViT-rmrQJIBJ3pkkGyDBOoHeYAdeRkolzYl9ZtMIk"
}

// 上传图像响应数据类
data class UploadResponse(
    @SerializedName("Key") val key: String,
    @SerializedName("Id") val id: String
)

// Retrofit API 接口
interface SupabaseStorageApi {
    @GET("/storage/v1/object/{bucketName}/{fileName}")
    fun getImage(
        @Header("Authorization") authorization: String,
        @Path("bucketName") bucketName: String,
        @Path("fileName") fileName: String
    ): Call<ResponseBody>

    @POST("/storage/v1/object/{bucketName}/{fileName}")
    @Headers("Content-Type: application/octet-stream")
    fun uploadImage(
        @Header("Authorization") authorization: String,
        @Path("bucketName") bucketName: String,
        @Path("fileName") fileName: String,
        @Body fileBody: RequestBody
    ): Call<UploadResponse>
}

// Supabase Storage 工具类
class SupabaseStorageService {
    private val retrofit: Retrofit
    private val storageApi: SupabaseStorageApi

    init {
        val client = OkHttpClient.Builder().build()

        retrofit = Retrofit.Builder()
            .baseUrl(SupabaseConfig.SUPABASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        storageApi = retrofit.create(SupabaseStorageApi::class.java)
    }

    // 获取授权头
    private fun getAuthHeader(): String {
        return "Bearer ${SupabaseConfig.SERVICE_ROLE_KEY}"
    }

    // 获取图像并保存到本地
    fun getImage(bucketName: String, fileName: String, destinationPath: String): Boolean {
        val call = storageApi.getImage(getAuthHeader(), bucketName, fileName)
        val response = call.execute()

        if (response.isSuccessful) {
            val body = response.body() ?: return false
            val inputStream: InputStream = body.byteStream()
            val outputFile = File(destinationPath)

            FileOutputStream(outputFile).use { outputStream ->
                val buffer = ByteArray(4096)
                var bytesRead: Int
                while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                    outputStream.write(buffer, 0, bytesRead)
                }
                outputStream.flush()
            }

            return true
        } else {
            println("获取图像失败: ${response.code()} - ${response.message()}")
            return false
        }
    }

    // 上传图像
    fun uploadImage(bucketName: String, fileName: String, filePath: String): UploadResponse? {
        val file = File(filePath)
        if (!file.exists()) {
            println("文件不存在: $filePath")
            return null
        }

        val requestBody = file.asRequestBody("application/octet-stream".toMediaTypeOrNull())
        val call = storageApi.uploadImage(getAuthHeader(), bucketName, fileName, requestBody)
        val response = call.execute()

        return if (response.isSuccessful) {
            response.body()
        } else {
            println("上传图像失败: ${response.code()} - ${response.message()}")
            null
        }
    }
}

// 使用示例
fun main() {
    val storageService = SupabaseStorageService()

    // 示例1: 获取图像
    val downloadSuccess = storageService.getImage(
        "diary",
        "diarypic.png",
        "C:\\Users\\笙\\Desktop\\AG\\diarypic.png"
    )

    if (downloadSuccess) {
        println("图像下载成功")
    } else {
        println("图像下载失败")
    }

    // 示例2: 上传图像
    val uploadResponse = storageService.uploadImage(
        "diary",
        "new_is.png",
        "C:\\Users\\笙\\Desktop\\AG\\Snipaste_2025-03-09_15-25-36.png"
    )

    uploadResponse?.let {
        println("图像上传成功: Key=${it.key}, Id=${it.id}")
    }
}