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
