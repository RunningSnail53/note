package edu.hebut.retrofittest.UI;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Rect;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import edu.hebut.retrofittest.Adapter.PicAdapter;
import edu.hebut.retrofittest.Bean.NoteBean;
import edu.hebut.retrofittest.DB.NoteDao;
import edu.hebut.retrofittest.R;

import com.yanzhenjie.album.AlbumFile;

import java.util.ArrayList;


public class NoteActivity extends AppCompatActivity {
    private TextView tvTitle;
    private TextView tv_note_content;//记事内容
    private NoteBean note;//记事对象
    private TextView tvUpdateTime;
    private PicAdapter adapter;
    private String myTitle;
    private String myContent;
    private String myCreate_time;
    private TextView tvLink;
    private NoteDao noteDao;
    private SwitchCompat switchbt;
    private RecyclerView rvData;
    private MediaRecorder mMediaRecorder;
    private Button btnPlayMusic;
    private Button btnPlayRecord;
    private int musicRawId;
    private MediaPlayer mediaPlayer;

    private String mOutputFilePath;
    private ProgressDialog loadingDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note);
        init();
        initEvent();
//        btnPlayMusic.performClick();
        // TODO: 2020/10/21 获取音乐路径
    }

    private void initEvent() {
        tvLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String url = tvLink.getText().toString().trim(); // 要打开的链接地址
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                startActivity(Intent.createChooser(intent, "请选择浏览器"));
            }
        });
        btnPlayMusic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (btnPlayMusic.getTag() == null) {
                    playMusic();
                    btnPlayMusic.setTag(1);
                    btnPlayMusic.setText("停止播放");
                } else {
                    stopMusic();
                    btnPlayMusic.setTag(null);
                    btnPlayMusic.setText("播放音乐");
                }
            }
        });
        btnPlayRecord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!TextUtils.isEmpty(mOutputFilePath)) {
                    try {
                        mediaPlayer = new MediaPlayer();
                        mediaPlayer.setDataSource(mOutputFilePath);
                        mediaPlayer.prepareAsync();
                        mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                            @Override
                            public void onPrepared(MediaPlayer mediaPlayer) {
                                mediaPlayer.start();
                                btnPlayRecord.setVisibility(View.GONE);
                            }
                        });
                        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                            @Override
                            public void onCompletion(MediaPlayer mediaPlayer) {
                                btnPlayRecord.setVisibility(View.VISIBLE);
                            }
                        });
                    } catch (Exception e) {
                        e.printStackTrace();
                        btnPlayRecord.setVisibility(View.VISIBLE);
                    }
                }
            }
        });
    }

    private void playMusic() {
        if (musicRawId == 0) {
            return;
        }
        mediaPlayer = MediaPlayer.create(this, 1);
        mediaPlayer.start();
    }

    private void stopMusic() {
        try {
            mediaPlayer.stop();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void init() {
        btnPlayRecord = findViewById(R.id.btnPlay);
        btnPlayMusic = findViewById(R.id.btnPlayMusic);
        tvUpdateTime = findViewById(R.id.tvUpdate);
        tvLink = findViewById(R.id.tvLink);
        noteDao = new NoteDao();
        adapter = new PicAdapter(this);
        tvTitle = findViewById(R.id.tvTitle);
        rvData = findViewById(R.id.rvData);
        rvData.setLayoutManager(new GridLayoutManager(this, 4));
        rvData.setAdapter(adapter);
        rvData.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
                super.getItemOffsets(outRect, view, parent, state);
                outRect.top = 5;
                outRect.bottom = 5;
                outRect.left = 5;
                outRect.right = 5;

            }
        });
        loadingDialog = new ProgressDialog(this);
        loadingDialog.setMessage("数据加载中...");
        loadingDialog.setCanceledOnTouchOutside(false);

        tv_note_content = (TextView) findViewById(R.id.tv_note_content);//内容

        Intent intent = getIntent();
        Bundle bundle = intent.getBundleExtra("data");
        note = (NoteBean) bundle.getSerializable("note");
        musicRawId = note.getMusicId();
        myTitle = note.getTitle();
        myContent = note.getContent();
        myCreate_time = note.getCreateTime();
        mOutputFilePath = note.getRecordPath();
        tvTitle.setText(myTitle);
        tv_note_content.setText(myContent);
        tvLink.setText(note.getLink());
        tvUpdateTime.setText(note.getUpdateTime() == null ? "" : "创建（修改）时间：" + note.getUpdateTime());
        if (note.getMusicId() != 0) {
            btnPlayMusic.setVisibility(View.VISIBLE);
        }
        ArrayList<AlbumFile> list = new ArrayList<>();
        try {
            for (int k = 0; k < note.getPicPaths().size(); k++) {
                AlbumFile albumFile = new AlbumFile();
                albumFile.setPath(note.getPicPaths().get(k));
                list.add(albumFile);
            }
            ;
            adapter.setNewData(list);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (!TextUtils.isEmpty(mOutputFilePath)) {
            btnPlayRecord.setVisibility(View.VISIBLE);
        }

    }


    @Override
    protected void onDestroy() {
        if (mediaPlayer != null) {
            stopMusic();
        }

        super.onDestroy();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();

        if (itemId == R.id.action_note_edit) {      // 编辑记事
            Intent intent = new Intent(NoteActivity.this, EditActivity.class);
            Bundle bundle = new Bundle();
            bundle.putSerializable("note", note);
            intent.putExtra("data", bundle);
            intent.putExtra("flag", 1);             // 编辑标记
            startActivity(intent);
            finish();
            return true;

        } else if (itemId == R.id.action_note_delete) { // 删除记事
            new AlertDialog.Builder(NoteActivity.this)
                    .setTitle("提示")
                    .setMessage("确定删除记事？")
                    .setCancelable(false)
                    .setPositiveButton("确定", (dialog, which) -> {
                        noteDao.deleteNote(Long.valueOf(note.getId())).thenAccept(
                                ret -> {
                                    runOnUiThread(()->{
                                        Toast.makeText(NoteActivity.this, "删除成功", Toast.LENGTH_SHORT).show();
                                        finish();
                                    });

                                }
                        );
                    })
                    .setNegativeButton("取消", null)
                    .create()
                    .show();
            return true;

        } else if (itemId == R.id.action_note_mark) {  // 保留注释的标记功能
            // TODO: 待实现标记功能
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}
