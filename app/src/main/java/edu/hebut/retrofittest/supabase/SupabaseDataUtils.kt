package edu.hebut.ActivityLifeCycle.supabaseUtil

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.serializer.KotlinXSerializer
import io.github.jan.supabase.storage.Storage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import java.io.PrintStream
import kotlin.time.Duration.Companion.seconds


class SupabaseDataUtils {
    // 设置一个单例模式获取supabase的实例
    companion object {
        private var instance: SupabaseClient? = null
        fun getClient(): SupabaseClient {
            if (instance == null) {
                instance = createSupabaseClient(
                    supabaseUrl = "https://jjnragzbvpqdjrcccgdl.supabase.co",
                    supabaseKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImpqbnJhZ3pidnBxZGpyY2NjZ2RsIiwicm9sZSI6InNlcnZpY2Vfcm9sZSIsImlhdCI6MTc0MjIwNDE4OCwiZXhwIjoyMDU3NzgwMTg4fQ.KWViT-rmrQJIBJ3pkkGyDBOoHeYAdeRkolzYl9ZtMIk",
                ) {
                    defaultSerializer = KotlinXSerializer(Json)
                    install(Postgrest)
                    install(Storage) {
                        transferTimeout = 90.seconds
                    }
                }
            }
            return instance!!
        }
    }

}

suspend fun main() {
    withContext(Dispatchers.IO) {
        System.setOut(PrintStream(System.out, true, "UTF-8"))
    }

    SupabaseDataUtils.getClient()

}