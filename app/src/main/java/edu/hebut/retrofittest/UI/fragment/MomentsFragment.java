package edu.hebut.retrofittest.UI.fragment;

import static android.app.Activity.RESULT_OK;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import edu.hebut.ActivityLifeCycle.supabaseUtil.SupabaseStorageService;
import edu.hebut.ActivityLifeCycle.supabaseUtil.UploadResponse;
import edu.hebut.retrofittest.R;
import edu.hebut.retrofittest.Util.NumberManager;
import edu.hebut.retrofittest.chat.client.RetrofitClient;
import edu.hebut.retrofittest.chat.model.GenerateResponse;
import edu.hebut.retrofittest.chat.service.ChatApi;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MomentsFragment extends Fragment {
    public static final int PICK_IMAGE_REQUEST = 1001;
    private static final String WECHAT_PACKAGE = "com.tencent.mm";
    private static String SHARE_TEXT = "分享我的精彩瞬间！"; // 统一文案
    private static final String WECHAT_FRIEND_CLASS = "com.tencent.mm.ui.tools.ShareImgUI";
    private static final String WECHAT_MOMENT_CLASS = "com.tencent.mm.ui.tools.ShareToTimeLineUI";
    private static final String QQ_PACKAGE = "com.tencent.mobileqq";

    private Button btnGenerate;
    private Button btnShareMoment;
    private Button btnShareWechat;
    private Button btnShareQQ;
    private ProgressBar progressBar;
    private NumberManager numberManager;
    private int currentFileNumber;

    private RadioGroup rgMood;

    private CheckBox redrawCheckBox;

    private TextView tvCaption;

    // 临时文件
    private ImageView ivPreview;
    private ImageView ivSelect;
    private File cachedImageFile;
    private Uri selectedImageUri;

    private String uploadedFileName;

    private String selectedMood;

    Boolean isRedraw = false;

    private ChatApi chatApi;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_moments, container, false);

        chatApi = RetrofitClient.getInstance().create(ChatApi.class);

        //在这里添加服务器除了文件之外的其他参数
        //.addFormDataPart("参数1", "值1")
        //.addFormDataPart("参数2", "值2");

        // 初始化视图
        btnGenerate = view.findViewById(R.id.btn_upload);
        btnShareMoment = view.findViewById(R.id.btn_share_moment);
        btnShareWechat = view.findViewById(R.id.btn_share_wechat);
        btnShareQQ = view.findViewById(R.id.btn_share_qq);
        progressBar = view.findViewById(R.id.progressBar);
        ivPreview = view.findViewById(R.id.iv_preview);
        ivSelect = view.findViewById(R.id.iv_select);
        rgMood = view.findViewById(R.id.rg_mood);
        redrawCheckBox = view.findViewById(R.id.ck_redraw);
        tvCaption = view.findViewById(R.id.tv_caption);

        numberManager = new NumberManager(requireContext());

        setupClickListeners();
        return view;
    }

    private void setupClickListeners() {

        ivSelect.setOnClickListener(v -> openImagePicker());
        // 原上传按钮
        btnGenerate.setOnClickListener(v -> {
            if (selectedImageUri == null) {
                Toast.makeText(requireContext(), "请选择一张图片", Toast.LENGTH_SHORT).show();
                return;
            }

            if (selectedMood == null) {
                Toast.makeText(requireContext(), "请选择心情", Toast.LENGTH_SHORT).show();
            }

            showLoading(true);

            List<MultipartBody.Part> parts = null;
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {

                    parts = new MultipartBody.Builder()
                            .setType(MultipartBody.FORM)
                            .addFormDataPart("image",
                                    "image.jpg",
                                    RequestBody.create(
                                            getContext().getContentResolver().openInputStream(selectedImageUri).readAllBytes(),
                                            MediaType.parse("image/*")))
                            .addFormDataPart("style", selectedMood)
                            .build().parts();
                }
            } catch (IOException e) {
                showLoading(false);
                Toast.makeText(requireContext(), "图片解析失败", Toast.LENGTH_SHORT).show();
                throw new RuntimeException(e);
            }
            if (isRedraw) {
                chatApi.generateImageAndCaption(parts).enqueue(new retrofit2.Callback<GenerateResponse>() {
                    @Override
                    public void onResponse(Call<GenerateResponse> call, Response<GenerateResponse> response) {


                        new Handler(Looper.getMainLooper()).post(() -> {
                            Log.d("225188", response.body().toString());
                            showLoading(false);
                            Glide.with(getContext())
                                    .load("data:image/png;base64," + response.body().getImage_base64())
                                    .into(ivPreview);
                            ivPreview.setVisibility(View.VISIBLE);
                            try {
                                // 生成唯一文件名
                                String fileName = "generated_" + System.currentTimeMillis() + ".png";
                                cachedImageFile = new File(requireContext().getCacheDir(), fileName);

                                // 解码 Base64
                                String base64Data = response.body().getImage_base64();
                                byte[] imageBytes = android.util.Base64.decode(
                                        base64Data,
                                        android.util.Base64.DEFAULT
                                );

                                // 写入文件
                                try (FileOutputStream fos = new FileOutputStream(cachedImageFile)) {
                                    fos.write(imageBytes);
                                    fos.flush();
                                    Log.d("FileSave", "图片已保存至：" + cachedImageFile.getAbsolutePath());
                                }

                                // 显示分享按钮
                                btnShareMoment.setVisibility(View.VISIBLE);
                                btnShareWechat.setVisibility(View.VISIBLE);
                                btnShareQQ.setVisibility(View.VISIBLE);

                            } catch (IOException e) {
                                Log.e("FileSave", "文件保存失败", e);
                                showToast("图片保存失败");
                            }
                            SHARE_TEXT = response.body().getCaption();
                            tvCaption.setText(response.body().getCaption());
                            tvCaption.setVisibility(View.VISIBLE);

                        });

                    }

                    @Override
                    public void onFailure(Call<GenerateResponse> call, Throwable throwable) {

                        new Handler(Looper.getMainLooper()).post(() -> {
                            showLoading(false);
                            Toast.makeText(requireContext(), "连接服务器失败", Toast.LENGTH_SHORT).show();
                        });

                    }
                });
            } else {

                chatApi.generateCaption(parts).enqueue(new Callback<GenerateResponse>() {
                    @Override
                    public void onResponse(Call<GenerateResponse> call, Response<GenerateResponse> response) {
                        new Handler(Looper.getMainLooper()).post(() -> {
                            showLoading(false);
                            SHARE_TEXT = response.body().getCaption();
                            tvCaption.setText(response.body().getCaption());
                            tvCaption.setVisibility(View.VISIBLE);

                        });
                    }

                    @Override
                    public void onFailure(Call<GenerateResponse> call, Throwable throwable) {
                        new Handler(Looper.getMainLooper()).post(() -> {
                            showLoading(false);
                            Toast.makeText(requireContext(), "连接服务器失败", Toast.LENGTH_SHORT).show();
                        });
                    }
                });
            }
        });

        rgMood.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId != -1) {
                    selectedMood = ((RadioButton) group.findViewById(checkedId)).getText().toString();
                }
            }
        });

        redrawCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            isRedraw = isChecked;
        });

        // 朋友圈分享按钮
        btnShareMoment.setOnClickListener(v -> {
            copyToClipboard(SHARE_TEXT);
            try {
                shareToWeChatMoment(cachedImageFile);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

        // 微信好友分享按钮
        btnShareWechat.setOnClickListener(v -> {
            copyToClipboard(SHARE_TEXT);
            shareToWeChatFriend(cachedImageFile);
        });

        // QQ好友分享按钮
        btnShareQQ.setOnClickListener(v -> {
            copyToClipboard(SHARE_TEXT);
            shareToQQ(cachedImageFile);
        });
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
            selectedImageUri = data.getData();
            try {
                // 通过输入流读取内容
                InputStream inputStream = requireContext().getContentResolver().openInputStream(selectedImageUri);

                // 创建缓存文件（示例文件名）
                String fileName = "cached_image_" + System.currentTimeMillis() + ".jpg";
                cachedImageFile = new File(requireContext().getCacheDir(), fileName);

                // 写入本地缓存
                try (FileOutputStream fos = new FileOutputStream(cachedImageFile)) {
                    byte[] buffer = new byte[4096];
                    int bytesRead;
                    while ((bytesRead = inputStream.read(buffer)) != -1) {
                        fos.write(buffer, 0, bytesRead);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(requireContext(), "文件读取失败", Toast.LENGTH_SHORT).show();
            }

            showPreview(selectedImageUri);  // 新增预览方法

            /*uploadImageToSupabase();*/
        }
    }

    private void showPreview(Uri imageUri) {
        try {
            // 使用Glide加载图片（推荐）
            Glide.with(this)
                    .load(imageUri)
                    .into(ivPreview);


            ivPreview.setVisibility(View.VISIBLE);
        } catch (Exception e) {
            showToast("图片加载失败: " + e.getMessage());
        }
    }

    private void uploadImageToSupabase() {
        showLoading(true);

        new Thread(() -> {
            try {
                // 获取新序号（原子操作）
                currentFileNumber = numberManager.getAndIncrement();

                String fileName = "share" + currentFileNumber + ".jpg";

                // 创建临时文件（使用新文件名）
                File tempFile = createNumberedTempFile(selectedImageUri, fileName);

                // 使用Supabase工具类上传
                SupabaseStorageService service = new SupabaseStorageService();
                UploadResponse response = service.uploadImage(
                        "share",
                        fileName,
                        tempFile.getAbsolutePath()
                );

                requireActivity().runOnUiThread(() -> {
                    showLoading(false);
                    if (response != null) {
                        handleUploadSuccess(tempFile, fileName);
                    } else {
                        numberManager.resetCounter();
                        showToast("上传失败");
                    }
                });

            } catch (Exception e) {
                requireActivity().runOnUiThread(() -> {
                    numberManager.resetCounter();
                    showLoading(false);
                    showToast("上传出错: " + e.getMessage());
                });
            }
        }).start();
    }

    private File createNumberedTempFile(Uri uri, String fileName) throws IOException {
        InputStream inputStream = requireContext().getContentResolver().openInputStream(uri);

        // 创建带编号的临时文件
        File targetFile = new File(requireContext().getCacheDir(), fileName);

        try (FileOutputStream fos = new FileOutputStream(targetFile)) {
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                fos.write(buffer, 0, bytesRead);
            }
        }
        return targetFile;
    }

    private void downloadImage() {
        if (uploadedFileName == null) {
            showToast("请先上传图片");
            return;
        }

        showLoading(true);
        new Thread(() -> {
            try {
                String newFileName = "New" + uploadedFileName;
                File targetFile = new File(requireContext().getCacheDir(), newFileName);

                boolean success = new SupabaseStorageService().getImage(
                        "share",
                        newFileName,
                        targetFile.getAbsolutePath()
                );

                requireActivity().runOnUiThread(() -> {
                    showLoading(false);
                    if (success) {
                        cachedImageFile = targetFile;
                        Glide.with(MomentsFragment.this)
                                .load(targetFile)
                                .into(ivPreview);
                        showToast("已更新为下载版本：" + newFileName);
                    } else {
                        showToast("下载失败");
                    }
                });
            } catch (Exception e) {
                requireActivity().runOnUiThread(() -> {
                    showLoading(false);
                    showToast("下载出错：" + e.getMessage());
                });
            }
        }).start();
    }


    private void handleUploadSuccess(File imageFile, String fileName) {
        // 保存临时文件引用;
        uploadedFileName = fileName;
        cachedImageFile = imageFile;

        // 显示分享按钮
        btnShareMoment.setVisibility(View.VISIBLE);
        btnShareWechat.setVisibility(View.VISIBLE);
        btnShareQQ.setVisibility(View.VISIBLE);

        Glide.with(this)
                .load(imageFile)
                .into(ivPreview);
    }

    private void cleanCache() {
        if (cachedImageFile != null && cachedImageFile.exists()) {
            cachedImageFile.delete();
        }
    }

    private void copyToClipboard(String text) {
        ClipboardManager clipboard = (ClipboardManager) requireContext().getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("share_text", text);
        clipboard.setPrimaryClip(clip);
        showToast("文案已复制到剪贴板");
    }

    private void shareToWeChatMoment(File imageFile) throws IOException {
        if (!isWeChatInstalled() || !isMomentAvailable()) {
            showToast("请先安装最新版微信");
            return;
        }

        Uri contentUri = FileProvider.getUriForFile(
                requireContext(),
                requireContext().getPackageName() + ".provider",
                imageFile
        );

        Intent intent = new Intent();
        intent.setComponent(new ComponentName(WECHAT_PACKAGE, WECHAT_MOMENT_CLASS));
        intent.setAction(Intent.ACTION_SEND);
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_STREAM, contentUri);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivity(intent);
    }

    private boolean isWeChatInstalled() {
        try {
            requireContext().getPackageManager().getPackageInfo(WECHAT_PACKAGE, 0);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    private boolean isMomentAvailable() {
        Intent intent = new Intent();
        intent.setComponent(new ComponentName(WECHAT_PACKAGE, WECHAT_MOMENT_CLASS));
        return requireContext().getPackageManager().resolveActivity(intent, 0) != null;
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        btnGenerate.setEnabled(!show);
    }

    private void showToast(String message) {
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
    }

    private void shareToWeChatFriend(File imageFile) {
        if (!isWeChatInstalled()) {
            showToast("请先安装微信");
            return;
        }

        Uri contentUri = FileProvider.getUriForFile(
                requireContext(),
                requireContext().getPackageName() + ".provider",
                imageFile
        );

        Intent intent = new Intent();
        intent.setComponent(new ComponentName(WECHAT_PACKAGE, WECHAT_FRIEND_CLASS));
        intent.setAction(Intent.ACTION_SEND);
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_STREAM, contentUri);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivity(intent);
    }

    private void shareToQQ(File imageFile) {
        if (!isQQInstalled()) {
            showToast("请先安装QQ");
            return;
        }

        Uri contentUri = FileProvider.getUriForFile(
                requireContext(),
                requireContext().getPackageName() + ".provider",
                imageFile
        );

        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setPackage(QQ_PACKAGE);
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_STREAM, contentUri);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivity(Intent.createChooser(intent, "分享到QQ好友"));
    }

    private boolean isQQInstalled() {
        try {
            requireContext().getPackageManager().getPackageInfo(QQ_PACKAGE, 0);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }


    private int dpToPx(int dp) {
        return (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                dp,
                getResources().getDisplayMetrics()
        );
    }
}