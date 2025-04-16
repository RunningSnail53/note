package edu.hebut.retrofittest.UI.fragment;

import static android.app.Activity.RESULT_OK;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
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

import edu.hebut.ActivityLifeCycle.supabaseUtil.SupabaseStorageService;
import edu.hebut.ActivityLifeCycle.supabaseUtil.UploadResponse;
import edu.hebut.retrofittest.R;
import edu.hebut.retrofittest.Util.NumberManager;

public class MomentsFragment extends Fragment {
    public static final int PICK_IMAGE_REQUEST = 1001;
    private static final String WECHAT_PACKAGE = "com.tencent.mm";
    private static final String SHARE_TEXT = "分享我的精彩瞬间！"; // 统一文案
    private static final String WECHAT_FRIEND_CLASS = "com.tencent.mm.ui.tools.ShareImgUI";
    private static final String WECHAT_MOMENT_CLASS = "com.tencent.mm.ui.tools.ShareToTimeLineUI";
    private static final String QQ_PACKAGE = "com.tencent.mobileqq";

    private Button btnUpload;
    private Button btnShareMoment;
    private Button btnShareWechat;
    private Button btnShareQQ;
    private ProgressBar progressBar;
    private NumberManager numberManager;
    private int currentFileNumber;

    // 临时文件
    private ImageView ivPreview;
    private File cachedImageFile;
    private Uri selectedImageUri;

    private Button btnDownload;
    private String uploadedFileName;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_moments, container, false);

        // 初始化视图
        btnUpload = view.findViewById(R.id.btn_upload);
        btnShareMoment = view.findViewById(R.id.btn_share_moment);
        btnShareWechat = view.findViewById(R.id.btn_share_wechat);
        btnShareQQ = view.findViewById(R.id.btn_share_qq);
        progressBar = view.findViewById(R.id.progressBar);
        ivPreview = view.findViewById(R.id.iv_preview);
        btnDownload = view.findViewById(R.id.btn_download);
        btnDownload.setOnClickListener(v -> downloadImage());
        numberManager = new NumberManager(requireContext());

        setupClickListeners();
        return view;
    }

    private void setupClickListeners() {
        // 原上传按钮
        btnUpload.setOnClickListener(v -> openImagePicker());

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
            showPreview(selectedImageUri);  // 新增预览方法
            uploadImageToSupabase();
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
                        handleUploadSuccess(tempFile,fileName);
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



    private void handleUploadSuccess(File imageFile,String fileName) {
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
        btnUpload.setEnabled(!show);
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
}