package edu.hebut.retrofittest.Adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import edu.hebut.retrofittest.R;
import edu.hebut.retrofittest.UI.ImageActivity;
import com.yanzhenjie.album.Action;
import com.yanzhenjie.album.Album;
import com.yanzhenjie.album.AlbumConfig;
import com.yanzhenjie.album.AlbumFile;
import com.yanzhenjie.album.AlbumLoader;

import java.util.ArrayList;
import java.util.List;

public class PicAdapter extends RecyclerView.Adapter<PicAdapter.PicHolder> {
    private Context mContext;
    private ArrayList<AlbumFile> mList = new ArrayList<>();
    public PicAdapter(Context context) {
        this.mContext = context;
    }

    public void setNewData(ArrayList<AlbumFile> list) {
        this.mList = list;
        notifyDataSetChanged();
    }

    public List<AlbumFile> getmList() {
        return mList;
    }

    @NonNull
    @Override
    public PicHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        return new PicHolder(LayoutInflater.from(mContext).inflate(R.layout.item_album, viewGroup, false));
    }

    @Override
    public void onBindViewHolder(@NonNull PicHolder picHolder, int i) {
        final int[] pos = new int[1];
        pos[0] = i;
        if (mList.get(i) == null) {
            Glide.with(mContext).load(R.mipmap.ic_add).into(picHolder.ivPic);
        }else {
            Glide.with(mContext).load(mList.get(i).getPath()).into(picHolder.ivPic);
        }

        picHolder.ivPic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mList.get(pos[0]) != null) {
                    Intent intent = new Intent(mContext, ImageActivity.class);
                    intent.putExtra("path", mList.get(pos[0]).getPath());
                    mContext.startActivity(intent);
                    return;
                }
                // Preview path:
                ArrayList<AlbumFile> temp = new ArrayList<>();
                for (AlbumFile file : mList) {
                    if (file != null) {
                        temp.add(file);
                    }
                }

                Album.initialize(AlbumConfig.newBuilder(mContext)
                        .setAlbumLoader(new MediaLoader())
                        .build());
                Album.image(mContext) // Image selection.
                        .multipleChoice()
                        .camera(true)
                        .columnCount(4)
                        .selectCount(9)
                        .checkedList(temp)
//                        .filterSize() // Filter the file size.
//                        .filterMimeType() // Filter file format.
//                        .afterFilterVisibility() // Show the filtered files, but they are not available.
                        .onResult(new Action<ArrayList<AlbumFile>>() {
                            @Override
                            public void onAction(@NonNull ArrayList<AlbumFile> result) {
                                mList = result;
                                mList.add(null);
                                notifyDataSetChanged();
                            }
                        })
                        .onCancel(new Action<String>() {
                            @Override
                            public void onAction(@NonNull String result) {
                            }
                        })
                        .start();
            }
        });

        picHolder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                mList.remove(pos[0]);
                notifyItemRemoved(pos[0]);
                notifyItemRangeChanged(pos[0],mList.size()-pos[0]);
                return true;
            }
        });
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    public class PicHolder extends RecyclerView.ViewHolder {
        ImageView ivPic;
        public PicHolder(@NonNull View itemView) {
            super(itemView);
            ivPic = itemView.findViewById(R.id.ivPic);
        }
    }

    private class MediaLoader implements AlbumLoader {
        @Override
        public void load(ImageView imageView, AlbumFile albumFile) {
            load(imageView, albumFile.getPath());
        }

        @Override
        public void load(ImageView imageView, String url) {
            Glide.with(imageView.getContext()).load(url).into(imageView);
        }
    }

}



/*

package edu.hebut.retrofittest.Adapter;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.yanzhenjie.album.Album;
import com.yanzhenjie.album.AlbumConfig;
import com.yanzhenjie.album.AlbumFile;
import com.yanzhenjie.album.AlbumLoader;

import java.util.ArrayList;
import java.util.List;

import edu.hebut.retrofittest.R;
import edu.hebut.retrofittest.UI.ImageActivity;

public class PicAdapter extends RecyclerView.Adapter<PicAdapter.PicHolder> {
    private static final String TAG = "PicAdapter";
    private static final String ADD_BUTTON_FLAG = "ADD_BUTTON";
    private static final int MAX_IMAGES = 9; // 最大图片数量


    private final Context context;
    private List<AlbumFile> data = new ArrayList<>();
    private OnImageChangeListener listener;

    public interface OnImageChangeListener {
        void onImageAdded(List<AlbumFile> newImages);

        void onImageRemoved(int position);
    }

    public PicAdapter(Context context) {
        this.context = context;
        addAddButton(); // 初始添加加号按钮
        setOnImageChangeListener();
    }

    public void setOnImageChangeListener() {
        this.listener = new OnImageChangeListener() {
            @Override
            public void onImageAdded(List<AlbumFile> newImages) {

            }

            @Override
            public void onImageRemoved(int position) {

            }
        };
    }

    public void setNewData(List<AlbumFile> newData) {
        data = newData;
        addAddButton(); // 确保最后有加号按钮
        notifyDataSetChanged();
    }

    public ArrayList<AlbumFile> getImageFiles() {
        // 返回实际图片文件（过滤掉加号按钮）
        ArrayList<AlbumFile> files = new ArrayList<>();
        for (AlbumFile file : data) {
            if (!isAddButton(file)) {
                files.add(file);
            }
        }
        return files;
    }

    @NonNull
    @Override
    public PicHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_album, parent, false);
        return new PicHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PicHolder holder, int position) {
        if (data.isEmpty()) {
            return;
        }
        AlbumFile file = data.get(position);
        if (file == null) {
            Log.w("Adapter", "位置 " + position + " 的数据为空");
            loadErrorPlaceholder(holder);
            return;
        }

        if (isAddButton(file)) {
            loadAddButton(holder);
        } else {
//                openImagePreview(file);
            if (TextUtils.isEmpty(file.getPath())) {
                Log.w("Adapter", "图片路径为空");
                loadErrorPlaceholder(holder);
            } else {
                loadImage(holder, file);
            }
        }

        // 5. 设置点击监听（同样需要空检查）
        setupClickListeners(holder, position);

    }

    public void openAlbumSelector() {
        Album.initialize(AlbumConfig.newBuilder(context)
                .setAlbumLoader(new MediaLoader())
                .build());

        ArrayList<AlbumFile> selectedFiles = getImageFiles();

        Album.image(context)
                .multipleChoice()
                .camera(isCameraPermissionGranted()) // 根据权限动态设置
                .columnCount(4)
                .selectCount(MAX_IMAGES)
                .checkedList(selectedFiles)
                .onResult(this::handleImageResult)
                .onCancel(result -> Log.d(TAG, "Album selection cancelled"))
                .start();
    }

    private boolean isCameraPermissionGranted() {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
                ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
    }

    public static List<String> getRequiredPermissions(Context context) {
        List<String> permissions = new ArrayList<>();
        permissions.add(Manifest.permission.READ_EXTERNAL_STORAGE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.remove(Manifest.permission.READ_EXTERNAL_STORAGE);
            permissions.add(Manifest.permission.READ_MEDIA_IMAGES);
        }
        if (isCameraFeatureAvailable(context)) {
            permissions.add(Manifest.permission.CAMERA);
        }
        return permissions;
    }

    private static boolean isCameraFeatureAvailable(Context context) {
        return context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY);
    }


    private void handleImageResult(ArrayList<AlbumFile> result) {
        data = result;
        addAddButton();
        notifyDataSetChanged();
        if (listener != null) {
            listener.onImageAdded(result);
        }
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    static class PicHolder extends RecyclerView.ViewHolder {
        ImageView ivPic;

        public PicHolder(@NonNull View itemView) {
            super(itemView);
            ivPic = itemView.findViewById(R.id.ivPic);
        }
    }



    private void addAddButton() {
        // 移除现有的加号按钮
        for (int i = data.size() - 1; i >= 0; i--) {
            if (isAddButton(data.get(i))) {
                data.remove(i);
            }
        }

        // 如果未达最大数量，添加加号按钮
        if (getImageCount() < MAX_IMAGES) {
            data.add(createAddButton());
        }
    }

    private AlbumFile createAddButton() {
        AlbumFile file = new AlbumFile();
        file.setPath(ADD_BUTTON_FLAG);
        return file;
    }

    private boolean isAddButton(AlbumFile file) {
        return file != null && ADD_BUTTON_FLAG.equals(file.getPath());
    }

    private int getImageCount() {
        int count = 0;
        for (AlbumFile file : data) {
            if (!isAddButton(file)) {
                count++;
            }
        }
        return count;
    }

    private void loadAddButton(PicHolder holder) {
        Glide.with(context)
                .load(R.mipmap.ic_add)
                .transition(DrawableTransitionOptions.withCrossFade())
                .into(holder.ivPic);
    }

    private void loadImage(PicHolder holder, AlbumFile file) {
        if (!isValidImagePath(file.getPath())) {
            showErrorImage(holder);
            return;
        }

        Glide.with(context)
                .load(file.getPath())
                .placeholder(R.drawable.ic_image_placeholder)
                .error(R.drawable.ic_broken_image)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .thumbnail(Glide.with(context)
                        .load(file.getPath())
                        .override(100, 100) // 先加载极小尺寸
                )
                .transition(DrawableTransitionOptions.withCrossFade(100)) // 添加渐变效果
                .into(holder.ivPic);
    }

    private boolean isValidImagePath(String path) {
        if (TextUtils.isEmpty(path)) {
            return false;
        }
        // 检查是否是网络URL或本地文件路径
        return path.startsWith("http") || path.startsWith("content://")
                || path.startsWith("/storage") || path.startsWith("/data");
    }

    private void showErrorImage(PicHolder holder) {
        Glide.with(context)
                .load(R.drawable.ic_broken_image)
                .into(holder.ivPic);
    }

    private void setupClickListeners(PicHolder holder, int position) {
        holder.itemView.setOnClickListener(v -> {
            AlbumFile file = data.get(position);
            if (isAddButton(file)) {
                openAlbumSelector();
            } else {
                openImagePreview(file);
            }
        });

        holder.itemView.setOnLongClickListener(v -> {
            if (!isAddButton(data.get(position))) {
                removeImage(position);
                return true;
            }
            return false;
        });
    }


    private void openImagePreview(AlbumFile file) {
        try {
            Intent intent = new Intent(context, ImageActivity.class);
            intent.putExtra("path", file.getPath());
            context.startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(context, "无法预览图片", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "openImagePreview error", e);
        }
    }

    private void removeImage(int position) {
        if (position >= 0 && position < data.size()) {
            data.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, data.size() - position);
            addAddButton(); // 重新计算加号按钮

            if (listener != null) {
                listener.onImageRemoved(position);
            }
        }
    }

    private static class MediaLoader implements AlbumLoader {
        @Override
        public void load(ImageView imageView, AlbumFile albumFile) {
            load(imageView, albumFile.getPath());
        }

        @Override
        public void load(ImageView imageView, String url) {
            Glide.with(imageView.getContext())
                    .load(url)
                    .into(imageView);
        }
    }

    public ArrayList<AlbumFile> getData() {
        return (ArrayList<AlbumFile>) data;
    }

    private void loadErrorPlaceholder(@NonNull PicHolder holder) {
        // 加载错误占位图
        if (holder.ivPic != null) {
            Glide.with(holder.itemView.getContext())
                    .load(R.drawable.ic_broken_image)
                    .into(holder.ivPic);
        }
    }
}
*/
