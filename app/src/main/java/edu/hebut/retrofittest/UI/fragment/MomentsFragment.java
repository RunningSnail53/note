package edu.hebut.retrofittest.UI.fragment;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

import edu.hebut.retrofittest.R;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public class MomentsFragment extends Fragment {
    private static final int PICK_IMAGE_REQUEST = 101;
    private static final String SUPABASE_URL = "https://whkjurdnvqfjnuztizew.supabase.co";
    private static final String SUPABASE_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Indoa2p1cmRudnFmam51enRpemV3Iiwicm9sZSI6ImFub24iLCJpYXQiOjE3NDI4NzA3MzUsImV4cCI6MjA1ODQ0NjczNX0.RT4sYBdaVozuaVKKWJq7_ZmiBagXj4Q1_50ZG23AIX0";

    private ProgressBar progressBar;
    private SupabaseStorageApi storageApi;
    private Uri selectedImageUri;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_moments, container, false);

        // 初始化UI组件
        Button btnSelectImage = view.findViewById(R.id.btn_select_image);
        Button btnUpload = view.findViewById(R.id.btn_upload);
        progressBar = view.findViewById(R.id.progress_bar);

        // 初始化Retrofit
        initRetrofit();

        btnSelectImage.setOnClickListener(v -> openFileChooser());
        btnUpload.setOnClickListener(v -> uploadImage());

        return view;
    }

    private void initRetrofit() {
        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(chain -> {
                    Request original = chain.request();
                    Request request = original.newBuilder()
                            .header("Authorization", "Bearer " + SUPABASE_KEY)
                            .header("apikey", SUPABASE_KEY) // 添加这行
                            .build();
                    return chain.proceed(request);
                })
                .addInterceptor(loggingInterceptor)
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(SUPABASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        storageApi = retrofit.create(SupabaseStorageApi.class);
    }

    private void openFileChooser() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && data != null && data.getData() != null) {
            selectedImageUri = data.getData();
            Toast.makeText(getContext(), "Image selected", Toast.LENGTH_SHORT).show();
        }
    }

    private void uploadImage() {
        if (selectedImageUri == null) {
            Toast.makeText(getContext(), "Please select an image first", Toast.LENGTH_SHORT).show();
            return;
        }
        String fileName = UUID.randomUUID().toString() + ".jpg";

        try {
            InputStream inputStream = requireContext().getContentResolver().openInputStream(selectedImageUri);
            byte[] bytes = toByteArray(inputStream);

            // 创建带有进度监听的RequestBody
            RequestBody requestBody = new ProgressRequestBody(
                    RequestBody.create(MediaType.parse("image/*"), bytes),
                    (bytesWritten, totalBytes) -> {
                        int progress = (int) ((100 * bytesWritten) / totalBytes);
                        new Handler(Looper.getMainLooper()).post(() ->
                                progressBar.setProgress(progress));
                    }
            );

            // 调用Supabase存储API（假设使用"photos"存储桶）
            Call<ResponseBody> call = storageApi.uploadFile(
                    "photos",
                    fileName,  // 使用动态生成的文件名
                    requestBody,
                    "image/jpeg"
            );

            call.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    if (response.isSuccessful()) {
                        showToast("Upload successful!");
                    } else {
                        showToast("Upload failed: " + response.code());
                    }
                    progressBar.setProgress(0);
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    showToast("Error: " + t.getMessage());
                    progressBar.setProgress(0);
                }
            });

        } catch (IOException e) {
            e.printStackTrace();
            showToast("Error reading file");
        }
    }

    private byte[] toByteArray(InputStream input) throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        byte[] buffer = new byte[4096];
        int n;
        while (-1 != (n = input.read(buffer))) {
            output.write(buffer, 0, n);
        }
        return output.toByteArray();
    }

    private void showToast(String message) {
        new Handler(Looper.getMainLooper()).post(() ->
                Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show());
    }

    // Retrofit接口
    public interface SupabaseStorageApi {
        @POST("/storage/v1/object/{bucket}/{path}")
        Call<ResponseBody> uploadFile(
                @Path("bucket") String bucket,
                @Path("path") String path,
                @Body RequestBody file,
                @Header("Content-Type") String contentType // 必须显式指定
        );
    }

    // 修改后的 ProgressRequestBody 类
    static class ProgressRequestBody extends RequestBody {
        private final RequestBody requestBody;
        private final ProgressListener listener;

        ProgressRequestBody(RequestBody requestBody, ProgressListener listener) {
            this.requestBody = requestBody;
            this.listener = listener;
        }

        @Override
        public MediaType contentType() {
            return requestBody.contentType();
        }

        @Override
        public void writeTo(okio.BufferedSink sink) throws IOException {
            // 修复点：使用 Okio.buffer() 包装 ForwardingSink
            okio.BufferedSink progressSink = okio.Okio.buffer(new okio.ForwardingSink(sink) {
                long bytesWritten = 0L;
                long totalBytes = -1L;

                @Override
                public void write(okio.Buffer source, long byteCount) throws IOException {
                    super.write(source, byteCount);
                    if (totalBytes == -1) {
                        totalBytes = contentLength();
                    }
                    bytesWritten += byteCount;
                    listener.onProgress(bytesWritten, totalBytes);
                }
            });

            requestBody.writeTo(progressSink);
            progressSink.flush();
        }

        interface ProgressListener {
            void onProgress(long bytesWritten, long totalBytes);
        }
    }
}