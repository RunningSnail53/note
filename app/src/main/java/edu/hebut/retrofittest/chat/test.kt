package edu.hebut.retrofittest.chat

import android.os.Handler
import android.os.Looper
import edu.hebut.retrofittest.chat.client.RetrofitClient
import edu.hebut.retrofittest.chat.service.ChatApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.ResponseBody
import org.json.JSONException
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.io.PrintStream

class test {
}


private fun handleStreamResponse(body: ResponseBody) {
    try {
        InputStreamReader(body.byteStream(), "UTF-8").use { inputStreamReader ->
            BufferedReader(inputStreamReader).use { bufferedReader ->
                var line: String
                val contentBuilder = StringBuilder()
                while ((bufferedReader.readLine().also { line = it }) != null) {
                    val delta = parseDeltaFromLine(line)
                    if (delta != null) {
                        processDelta(delta, contentBuilder)
                    }
                }
            }
        }
    } catch (e: IOException) {
        showError("流解析失败：" + e.message)
    }
}

private fun parseDeltaFromLine(line: String): String? {
    try {
        val jsonObject = JSONObject(line)
        return jsonObject.optString("delta", null)
    } catch (e: JSONException) {
        return null
    }
}

private fun processDelta(delta: String, contentBuilder: StringBuilder) {
    for (c in delta.toCharArray()) {
        contentBuilder.append(c)
        val finalContent = contentBuilder.toString()

        // 在主线程更新UI
        Handler(Looper.getMainLooper()).post {
            // 这里替换为实际的TextView操作

        }

        // 控制字符显示速度
        try {
            Thread.sleep(50)
        } catch (e: InterruptedException) {
            Thread.currentThread().interrupt()
        }
    }
}

private fun showError(message: String) {
    println(message)
}


suspend fun main() {
    withContext(Dispatchers.IO) {
        System.setOut(PrintStream(System.out, true, "UTF-8"))
    }
    val chatApi = RetrofitClient.getInstance().create(ChatApi::class.java)

    val params: MutableMap<String, Long> = HashMap()

    params["user_id"] = 1L

    chatApi.getSummaryStream(params).enqueue(object : Callback<ResponseBody?> {
        override fun onResponse(call: Call<ResponseBody?>, response: Response<ResponseBody?>) {
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    handleStreamResponse(body)
                } else {
                    showError("响应体为空：" + response.code())
                }
            } else {
                showError("请求失败：" + response.code())
            }
        }

        override fun onFailure(call: Call<ResponseBody?>, t: Throwable) {
            showError("网络连接失败：" + (if (t.message != null) t.message else "未知错误"))
        }
    })


    var client = Retrofit.Builder()
        .baseUrl("http://www.baidu.com/")
        .addConverterFactory(
            GsonConverterFactory.create()
        )
        .build();

}
